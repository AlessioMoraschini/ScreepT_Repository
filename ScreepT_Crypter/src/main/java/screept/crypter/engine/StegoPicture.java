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

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Steganography: hides already encrypted data in the least significant bits of a colour picture, which stays a
 * normal looking picture (saved as PNG, lossless).
 * <ul>
 * <li>Keys: Argon2id(password, salt = picture size) gives a position key and a whitening key, so without the password
 * nobody can tell where the bits are, and the extracted bits look random (no magic, no length in clear).</li>
 * <li>Positions: a keyed Fisher-Yates over all the R, G, B values; at most one bit every {@value #SLOTS_PER_BIT} values
 * (low payload makes statistical detection much harder).</li>
 * <li>Embedding by LSB matching: a value whose last bit is wrong moves randomly by +1 or -1 (never the plain LSB
 * replacement, which classic tests detect).</li>
 * </ul>
 * The data hidden is meant to be a SCP2 container: it carries its own authentication, so a wrong password, a picture
 * without hidden data or a picture changed afterwards (resized, saved as JPEG...) always ends in a clean error.
 */
public final class StegoPicture {

	public static final int SLOTS_PER_BIT = 4;
	/** Biggest picture accepted (memory: one int per colour value). */
	public static final long MAX_PIXELS = 24_000_000L;
	private static final byte[] MAGIC = { 'S', 'P', 'I', 'C' };
	private static final int FRAME_BYTES = MAGIC.length + 4;
	private static final int KDF_COST = Argon2.cost(16, 3, 4);
	private static final SecureRandom RANDOM = new SecureRandom();

	private StegoPicture() {
	}

	/** Bytes that a picture of this size can hide. */
	public static long capacity(int width, int height) {
		return Math.max(0, (long) width * height * 3 / SLOTS_PER_BIT / 8 - FRAME_BYTES);
	}

	/** A copy of the cover (as RGB, alpha dropped) with the data hidden in it. */
	public static BufferedImage hide(BufferedImage cover, byte[] data, char[] password) throws CryptoException {
		check(cover);
		int w = cover.getWidth(), h = cover.getHeight();
		if (data.length > capacity(w, h)) {
			throw new CryptoException("The picture can hide at most " + (capacity(w, h) / 1024) + " KB: choose a bigger picture or less data");
		}
		int[] rgb = cover.getRGB(0, 0, w, h, null, 0, w);
		byte[][] keys = keys(password, w, h);
		try {
			byte[] frame = new byte[FRAME_BYTES + data.length];
			System.arraycopy(MAGIC, 0, frame, 0, 4);
			frame[4] = (byte) (data.length >>> 24);
			frame[5] = (byte) (data.length >>> 16);
			frame[6] = (byte) (data.length >>> 8);
			frame[7] = (byte) data.length;
			System.arraycopy(data, 0, frame, FRAME_BYTES, data.length);
			ChaCha20Poly1305Layer.xor(keys[1], new byte[12], 0, frame, frame.length); // whitening
			Positions pos = new Positions(keys[0], (long) w * h * 3);
			for (int i = 0; i < frame.length * 8; i++) {
				int bit = (frame[i >>> 3] >>> (7 - (i & 7))) & 1;
				int slot = pos.next();
				int px = slot / 3, shift = 16 - 8 * (slot % 3);
				int v = (rgb[px] >>> shift) & 0xff;
				if ((v & 1) != bit) {
					v = v == 0 ? 1 : v == 255 ? 254 : v + (RANDOM.nextBoolean() ? 1 : -1);
					rgb[px] = (rgb[px] & ~(0xff << shift)) | (v << shift);
				}
			}
			pos.wipe();
			Arrays.fill(frame, (byte) 0);
			BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			out.setRGB(0, 0, w, h, rgb, 0, w);
			return out;
		} finally {
			Arrays.fill(keys[0], (byte) 0);
			Arrays.fill(keys[1], (byte) 0);
		}
	}

	/** The hidden data; CryptoException if the password is wrong or the picture holds nothing. */
	public static byte[] reveal(BufferedImage img, char[] password) throws CryptoException {
		check(img);
		int w = img.getWidth(), h = img.getHeight();
		int[] rgb = img.getRGB(0, 0, w, h, null, 0, w);
		byte[][] keys = keys(password, w, h);
		try {
			Positions pos = new Positions(keys[0], (long) w * h * 3);
			byte[] frame = read(rgb, pos, FRAME_BYTES);
			ChaCha20Poly1305Layer.xor(keys[1], new byte[12], 0, frame, FRAME_BYTES);
			long len = ((frame[4] & 0xffL) << 24) | ((frame[5] & 0xff) << 16) | ((frame[6] & 0xff) << 8) | (frame[7] & 0xff);
			if (!MessageDigest.isEqual(Arrays.copyOf(frame, 4), MAGIC) || len > capacity(w, h)) {
				throw new CryptoException("No hidden data found in this picture (or wrong password)");
			}
			// the whitening keystream is continuous: read the rest and decode the whole frame again
			byte[] all = new byte[(int) (FRAME_BYTES + len)];
			System.arraycopy(read(rgb, pos, (int) len), 0, all, FRAME_BYTES, (int) len);
			pos.wipe();
			ChaCha20Poly1305Layer.xor(keys[1], new byte[12], 0, all, all.length);
			return Arrays.copyOfRange(all, FRAME_BYTES, all.length);
		} finally {
			Arrays.fill(keys[0], (byte) 0);
			Arrays.fill(keys[1], (byte) 0);
		}
	}

	private static byte[] read(int[] rgb, Positions pos, int n) {
		byte[] out = new byte[n];
		for (int i = 0; i < n * 8; i++) {
			int slot = pos.next();
			int bit = (rgb[slot / 3] >>> (16 - 8 * (slot % 3))) & 1;
			out[i >>> 3] |= bit << (7 - (i & 7));
		}
		return out;
	}

	private static void check(BufferedImage img) throws CryptoException {
		if (img == null) {
			throw new CryptoException("The picture cannot be read");
		}
		if (img.getColorModel().getNumColorComponents() < 3) {
			throw new CryptoException("Use a colour picture (grey pictures would show the hidden data)");
		}
		if ((long) img.getWidth() * img.getHeight() > MAX_PIXELS) {
			throw new CryptoException("The picture is too big (at most " + MAX_PIXELS / 1000000 + " megapixels)");
		}
		if ((long) img.getWidth() * img.getHeight() * 3 * 4 > Runtime.getRuntime().maxMemory() / 3) {
			throw new CryptoException("Not enough memory for a picture this big");
		}
	}

	/** [position key, whitening key] from the password; the picture size is the salt. */
	private static byte[][] keys(char[] password, int w, int h) throws CryptoException {
		if (password == null || password.length == 0) {
			throw new CryptoException("The password is empty");
		}
		byte[] salt;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update("SCP2/stego/v1".getBytes(StandardCharsets.US_ASCII));
			md.update(new byte[] { (byte) (w >>> 24), (byte) (w >>> 16), (byte) (w >>> 8), (byte) w, (byte) (h >>> 24), (byte) (h >>> 16),
					(byte) (h >>> 8), (byte) h });
			salt = Arrays.copyOf(md.digest(), 16);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException("SHA-256 missing", e);
		}
		byte[] m = KdfAlgorithm.ARGON2ID.derive(password, salt, KDF_COST, 64);
		byte[][] k = { Arrays.copyOf(m, 32), Arrays.copyOfRange(m, 32, 64) };
		Arrays.fill(m, (byte) 0);
		return k;
	}

	/** Keyed partial Fisher-Yates over the colour values: every slot at most once, unpredictable without the key. */
	private static final class Positions {
		private final int[] perm;
		private final int[] state;
		private final int[] block = new int[16];
		private int used = 16;
		private int i = 0;

		Positions(byte[] key, long slots) {
			perm = new int[(int) slots];
			for (int k = 0; k < perm.length; k++) {
				perm[k] = k;
			}
			state = ChaCha20Poly1305Layer.initState(key, new byte[12], 0);
		}

		int next() {
			int j = i + bounded(perm.length - i);
			int v = perm[j];
			perm[j] = perm[i];
			perm[i] = v;
			i++;
			return v;
		}

		private int bounded(int bound) {
			int bits, val;
			do {
				bits = nextInt() >>> 1;
				val = bits % bound;
			} while (bits - val + (bound - 1) < 0);
			return val;
		}

		private int nextInt() {
			if (used == 16) {
				ChaCha20Poly1305Layer.block(state, block);
				state[12]++;
				used = 0;
			}
			return block[used++];
		}

		void wipe() {
			Arrays.fill(perm, 0);
			Arrays.fill(state, 0);
			Arrays.fill(block, 0);
		}
	}
}
