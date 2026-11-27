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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * An X25519 key pair for public key files. The public key is shared as text ({@code scpt1...}, with a checksum that
 * catches typos); the private key lives in a file encrypted with the password (SCP2, Argon2id), never in clear.
 */
public final class Identity {

	private static final String PUBLIC_PREFIX = "scpt1";
	private static final String FILE_HEADER = "SCREEPT-IDENTITY-1\n";

	private final byte[] privateKey;
	private final byte[] publicKey;

	private Identity(byte[] privateKey) {
		this.privateKey = privateKey;
		this.publicKey = X25519.publicKey(privateKey);
	}

	public static Identity generate() {
		return new Identity(X25519.newPrivateKey());
	}

	byte[] privateKey() {
		return privateKey;
	}

	public byte[] publicKey() {
		return publicKey.clone();
	}

	public String publicKeyText() {
		return formatPublicKey(publicKey);
	}

	public static String formatPublicKey(byte[] pub) {
		byte[] full = Arrays.copyOf(pub, 36);
		System.arraycopy(checksum(pub), 0, full, 32, 4);
		return PUBLIC_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(full);
	}

	/** Accepts a key copied with spaces or line breaks; refuses anything mistyped or truncated. */
	public static byte[] parsePublicKey(String text) throws CryptoException {
		String t = text == null ? "" : text.replaceAll("\\s", "");
		if (!t.startsWith(PUBLIC_PREFIX)) {
			throw new CryptoException("Not a ScreepT public key (it starts with " + PUBLIC_PREFIX + ")");
		}
		byte[] full;
		try {
			full = Base64.getUrlDecoder().decode(t.substring(PUBLIC_PREFIX.length()));
		} catch (IllegalArgumentException e) {
			throw new CryptoException("The public key is damaged");
		}
		if (full.length != 36) {
			throw new CryptoException("The public key is incomplete");
		}
		byte[] pub = Arrays.copyOf(full, 32);
		if (!MessageDigest.isEqual(checksum(pub), Arrays.copyOfRange(full, 32, 36))) {
			throw new CryptoException("The public key has a typo (checksum does not match)");
		}
		return pub;
	}

	private static byte[] checksum(byte[] pub) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(PUBLIC_PREFIX.getBytes(StandardCharsets.US_ASCII));
			return Arrays.copyOf(md.digest(pub), 4);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	/** Writes the private key encrypted with the password (Argon2id + AES-256-GCM); replaces the file only when complete. */
	public void save(File file, char[] password) throws CryptoException, IOException {
		byte[] head = FILE_HEADER.getBytes(StandardCharsets.US_ASCII);
		byte[] plain = Arrays.copyOf(head, head.length + 32);
		System.arraycopy(privateKey, 0, plain, head.length, 32);
		try {
			byte[] enc = CryptoEngine.encrypt(plain, CryptoKey.of(password), CryptoProfile.standard());
			File dir = file.getAbsoluteFile().getParentFile();
			if (!dir.isDirectory() && !dir.mkdirs()) {
				throw new IOException("Cannot create " + dir);
			}
			File tmp = new File(dir, file.getName() + ".tmp");
			Files.write(tmp.toPath(), enc);
			Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} finally {
			Arrays.fill(plain, (byte) 0);
		}
	}

	public static Identity load(File file, char[] password) throws CryptoException, IOException {
		byte[] plain = CryptoEngine.decrypt(Files.readAllBytes(file.toPath()), CryptoKey.of(password));
		try {
			byte[] head = FILE_HEADER.getBytes(StandardCharsets.US_ASCII);
			if (plain.length != head.length + 32 || !Arrays.equals(Arrays.copyOf(plain, head.length), head)) {
				throw new CryptoException("Not a ScreepT private key file");
			}
			return new Identity(Arrays.copyOfRange(plain, head.length, plain.length));
		} finally {
			Arrays.fill(plain, (byte) 0);
		}
	}

	public void clear() {
		Arrays.fill(privateKey, (byte) 0);
	}
}
