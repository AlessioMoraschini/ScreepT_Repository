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
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.imageio.ImageIO;

/**
 * The secrets of an operation: the password and, optionally, a key image. Lives only in memory;
 * call {@link #clear()} when done. Never printed or logged.
 */
public final class CryptoKey {

	private final char[] password;
	private final byte[] keyImageDigest;

	private CryptoKey(char[] password, byte[] keyImageDigest) {
		this.password = password;
		this.keyImageDigest = keyImageDigest;
	}

	public static CryptoKey of(char[] password) {
		return new CryptoKey(password, null);
	}

	/** The key image counts through its decoded pixels, so a lossless re-save of the same picture still works. */
	public static CryptoKey of(char[] password, File keyImage) throws CryptoException {
		if (keyImage == null) {
			return of(password);
		}
		return new CryptoKey(password, digestOf(keyImage));
	}

	char[] password() {
		return password;
	}

	byte[] keyImageDigest() {
		return keyImageDigest;
	}

	public boolean hasKeyImage() {
		return keyImageDigest != null;
	}

	public void clear() {
		if (password != null) {
			Arrays.fill(password, '\0');
		}
		if (keyImageDigest != null) {
			Arrays.fill(keyImageDigest, (byte) 0);
		}
	}

	private static byte[] digestOf(File file) throws CryptoException {
		try {
			BufferedImage img = ImageIO.read(file);
			if (img == null) {
				throw new CryptoException("The key image cannot be read (unsupported image format)");
			}
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			ByteBuffer dims = ByteBuffer.allocate(8).putInt(img.getWidth()).putInt(img.getHeight());
			md.update(dims.array());
			ByteBuffer row = ByteBuffer.allocate(img.getWidth() * 4);
			for (int y = 0; y < img.getHeight(); y++) {
				row.clear();
				for (int x = 0; x < img.getWidth(); x++) {
					row.putInt(img.getRGB(x, y));
				}
				md.update(row.array());
			}
			return md.digest();
		} catch (IOException | GeneralSecurityException e) {
			throw new CryptoException("The key image cannot be read", e);
		}
	}
}
