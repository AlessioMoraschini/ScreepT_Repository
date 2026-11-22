/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.crypter.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Streaming authenticated encryption (container "SCP2").
 *
 * <pre>
 * header: "SCP2" | version | kdfId | kdfCost(4) | chunkLog2 | nLayers | layerIds... | salt(16) | noncePrefix(8)
 * body  : chunks of 2^chunkLog2 plaintext bytes; each chunk goes through every layer (AES-GCM, 16 byte tag per layer).
 *         The last chunk is always shorter than the others (possibly empty), so truncation is detected.
 * </pre>
 * Every layer has its own key, derived from the password-derived master key (HMAC-SHA256 with the layer index).
 * The header and the "last chunk" flag are authenticated, nonces are unique per chunk.
 * Decryption reads the header and needs only the password. Nothing here logs or stores passwords or keys.
 */
public final class CryptoEngine {

	public static final byte[] MAGIC = { 'S', 'C', 'P', '2' };
	private static final int VERSION = 1;
	private static final int SALT_BYTES = 16;
	private static final int PREFIX_BYTES = 8;
	private static final int MASTER_BYTES = 64;
	private static final SecureRandom RANDOM = new SecureRandom();

	private CryptoEngine() {
	}

	// ---------------------------------------------------------------- public API

	public static byte[] encrypt(byte[] plain, char[] password, CryptoProfile profile) throws CryptoException {
		return encrypt(plain, CryptoKey.of(password), profile);
	}

	public static byte[] decrypt(byte[] encrypted, char[] password) throws CryptoException {
		return decrypt(encrypted, CryptoKey.of(password));
	}

	public static void encrypt(InputStream in, OutputStream out, char[] password, CryptoProfile profile)
			throws CryptoException, IOException {
		encrypt(in, out, CryptoKey.of(password), profile);
	}

	public static CryptoProfile decrypt(InputStream in, OutputStream out, char[] password)
			throws CryptoException, IOException {
		return decrypt(in, out, CryptoKey.of(password));
	}

	public static byte[] encrypt(byte[] plain, CryptoKey password, CryptoProfile profile) throws CryptoException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(plain.length + 128);
		try {
			encrypt(new ByteArrayInputStream(plain), out, password, profile);
		} catch (IOException e) {
			throw new CryptoException("I/O error", e);
		}
		return out.toByteArray();
	}

	public static byte[] decrypt(byte[] encrypted, CryptoKey password) throws CryptoException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(encrypted.length);
		try {
			decrypt(new ByteArrayInputStream(encrypted), out, password);
		} catch (IOException e) {
			throw new CryptoException("I/O error", e);
		}
		return out.toByteArray();
	}

	/** True if the data starts with the engine magic (a cheap check, no password needed). */
	public static boolean isEngineData(byte[] head) {
		return head != null && head.length >= MAGIC.length && Arrays.equals(Arrays.copyOf(head, MAGIC.length), MAGIC);
	}

	/** Reads the profile from the first bytes of the data (no password needed). */
	public static CryptoProfile readProfile(byte[] head) throws CryptoException {
		if (head == null) {
			throw new CryptoException("Not a ScreepT encrypted file");
		}
		try {
			return Header.read(new ByteArrayInputStream(head)).profile;
		} catch (IOException e) {
			throw new CryptoException("Not a ScreepT encrypted file", e);
		}
	}

	public static void encrypt(InputStream in, OutputStream out, CryptoKey password, CryptoProfile profile)
			throws CryptoException, IOException {
		requirePassword(password);
		byte[] salt = random(SALT_BYTES);
		byte[] prefix = random(PREFIX_BYTES);
		Header header = new Header(profile, salt, prefix);
		out.write(header.raw);

		byte[][] keys = layerKeys(password, header);
		List<CipherLayer> layers = profile.getLayers();
		int chunk = profile.chunkSize();
		byte[] buf = new byte[chunk];
		try {
			for (long counter = 0;; counter++) {
				int n = readFully(in, buf);
				boolean last = n < chunk;
				byte[] data = Arrays.copyOf(buf, n);
				for (int i = 0; i < keys.length; i++) {
					data = layers.get(i).encrypt(keys[i], nonce(prefix, counter), aad(header, last, i), data);
				}
				out.write(data);
				if (last) {
					break;
				}
			}
		} finally {
			wipe(keys);
			Arrays.fill(buf, (byte) 0);
		}
		out.flush();
	}

	public static CryptoProfile decrypt(InputStream in, OutputStream out, CryptoKey password)
			throws CryptoException, IOException {
		requirePassword(password);
		Header header = Header.read(in);
		CryptoProfile profile = header.profile;
		byte[][] keys = layerKeys(password, header);
		List<CipherLayer> layers = profile.getLayers();
		int overhead = profile.overheadPerChunk();
		byte[] frame = new byte[profile.chunkSize() + overhead];
		try {
			for (long counter = 0;; counter++) {
				int n = readFully(in, frame);
				boolean last = n < frame.length;
				if (n < overhead) {
					throw new CryptoException("The file is truncated or damaged");
				}
				byte[] data = Arrays.copyOf(frame, n);
				for (int i = keys.length - 1; i >= 0; i--) {
					data = layers.get(i).decrypt(keys[i], nonce(header.noncePrefix, counter), aad(header, last, i), data);
				}
				out.write(data);
				Arrays.fill(data, (byte) 0);
				if (last) {
					break;
				}
			}
		} finally {
			wipe(keys);
			Arrays.fill(frame, (byte) 0);
		}
		out.flush();
		return profile;
	}

	// ---------------------------------------------------------------- internals

	private static void requirePassword(CryptoKey key) throws CryptoException {
		if (key == null || key.password() == null || key.password().length == 0) {
			throw new CryptoException("The password is empty");
		}
	}

	private static byte[] random(int n) {
		byte[] b = new byte[n];
		RANDOM.nextBytes(b);
		return b;
	}

	private static int readFully(InputStream in, byte[] buf) throws IOException {
		int total = 0;
		while (total < buf.length) {
			int r = in.read(buf, total, buf.length - total);
			if (r < 0) {
				break;
			}
			total += r;
		}
		return total;
	}

	private static byte[] nonce(byte[] prefix, long counter) throws CryptoException {
		if (counter > 0xFFFFFFFFL) {
			throw new CryptoException("The file is too big for this chunk size");
		}
		byte[] n = new byte[CipherAlgorithm.NONCE_BYTES];
		System.arraycopy(prefix, 0, n, 0, PREFIX_BYTES);
		n[8] = (byte) (counter >>> 24);
		n[9] = (byte) (counter >>> 16);
		n[10] = (byte) (counter >>> 8);
		n[11] = (byte) counter;
		return n;
	}

	private static byte[] aad(Header header, boolean last, int layerIndex) {
		byte[] a = Arrays.copyOf(header.raw, header.raw.length + 2);
		a[a.length - 2] = (byte) (last ? 1 : 0);
		a[a.length - 1] = (byte) layerIndex;
		return a;
	}

	private static byte[][] layerKeys(CryptoKey password, Header header) throws CryptoException {
		byte[] master = header.profile.kdf.derive(password.password(), header.salt, header.profile.kdfCost, MASTER_BYTES);
		List<CipherLayer> layers = header.profile.getLayers();
		byte[][] keys = new byte[layers.size()][];
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(master, "HmacSHA256"));
			for (int i = 0; i < keys.length; i++) {
				byte[] full = mac.doFinal(("SCP2/layer/" + i + "/" + layers.get(i).id()).getBytes("UTF-8"));
				keys[i] = layers.get(i).deriveKey(full, password);
				Arrays.fill(full, (byte) 0);
			}
		} catch (GeneralSecurityException | java.io.UnsupportedEncodingException e) {
			throw new CryptoException("Key derivation failed", e);
		} catch (CryptoException e) {
			wipe(keys);
			throw e;
		} finally {
			Arrays.fill(master, (byte) 0);
		}
		return keys;
	}

	private static void wipe(byte[][] keys) {
		for (byte[] k : keys) {
			if (k != null) {
				Arrays.fill(k, (byte) 0);
			}
		}
	}

	/** Parsed or freshly built header; {@code raw} is exactly what is written to the file. */
	private static final class Header {
		final CryptoProfile profile;
		final byte[] salt;
		final byte[] noncePrefix;
		final byte[] raw;

		Header(CryptoProfile profile, byte[] salt, byte[] noncePrefix) {
			this.profile = profile;
			this.salt = salt;
			this.noncePrefix = noncePrefix;
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			b.write(MAGIC, 0, MAGIC.length);
			b.write(VERSION);
			b.write(profile.kdf.id);
			int cost = profile.kdfCost;
			b.write(cost >>> 24);
			b.write(cost >>> 16);
			b.write(cost >>> 8);
			b.write(cost);
			b.write(profile.chunkLog2);
			b.write(profile.getLayers().size());
			for (CipherLayer c : profile.getLayers()) {
				b.write(c.id());
			}
			b.write(salt, 0, salt.length);
			b.write(noncePrefix, 0, noncePrefix.length);
			this.raw = b.toByteArray();
		}

		static Header read(InputStream in) throws IOException, CryptoException {
			byte[] fixed = new byte[MAGIC.length + 1 + 1 + 4 + 1 + 1];
			if (readFully(in, fixed) < fixed.length || !isEngineData(fixed)) {
				throw new CryptoException("Not a ScreepT encrypted file");
			}
			if ((fixed[4] & 0xff) != VERSION) {
				throw new CryptoException("Unsupported file version " + (fixed[4] & 0xff));
			}
			KdfAlgorithm kdf = KdfAlgorithm.fromId(fixed[5] & 0xff);
			int cost = ((fixed[6] & 0xff) << 24) | ((fixed[7] & 0xff) << 16) | ((fixed[8] & 0xff) << 8) | (fixed[9] & 0xff);
			int chunkLog2 = fixed[10] & 0xff;
			int n = fixed[11] & 0xff;
			if (!kdf.isCostAcceptable(cost) || chunkLog2 < 10 || chunkLog2 > 26 || n < 1 || n > CryptoProfile.MAX_LAYERS) {
				throw new CryptoException("Invalid or damaged file header");
			}
			byte[] ids = new byte[n];
			byte[] salt = new byte[SALT_BYTES];
			byte[] prefix = new byte[PREFIX_BYTES];
			if (readFully(in, ids) < n || readFully(in, salt) < SALT_BYTES || readFully(in, prefix) < PREFIX_BYTES) {
				throw new CryptoException("Invalid or damaged file header");
			}
			List<CipherLayer> layers = new ArrayList<CipherLayer>();
			for (byte id : ids) {
				layers.add(CipherAlgorithm.fromId(id & 0xff));
			}
			return new Header(new CryptoProfile(kdf, cost, chunkLog2, layers), salt, prefix);
		}
	}
}
