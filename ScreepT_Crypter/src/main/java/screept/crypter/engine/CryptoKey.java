/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
	/** Public keys a file is encrypted for (X25519 profiles); null otherwise. */
	private final java.util.List<byte[]> recipients;
	/** Private key that opens public key files; null otherwise. */
	private final byte[] identity;

	private CryptoKey(char[] password, byte[] keyImageDigest) {
		this(password, keyImageDigest, null, null);
	}

	private CryptoKey(char[] password, byte[] keyImageDigest, java.util.List<byte[]> recipients, byte[] identity) {
		this.password = password;
		this.keyImageDigest = keyImageDigest;
		this.recipients = recipients;
		this.identity = identity;
	}

	/** Same secrets, encrypting for these public keys (see {@link Identity#parsePublicKey}). */
	public CryptoKey withRecipients(java.util.List<byte[]> publicKeys) {
		java.util.List<byte[]> copy = new java.util.ArrayList<byte[]>();
		for (byte[] k : publicKeys) {
			copy.add(k.clone());
		}
		return new CryptoKey(password, keyImageDigest, java.util.Collections.unmodifiableList(copy), identity);
	}

	/** Same secrets, able to open public key files made for this identity. */
	public CryptoKey withIdentity(Identity id) {
		return new CryptoKey(password, keyImageDigest, recipients, id.privateKey().clone());
	}

	java.util.List<byte[]> recipients() {
		return recipients;
	}

	byte[] identity() {
		return identity;
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
		if (identity != null) {
			Arrays.fill(identity, (byte) 0);
		}
		if (password != null) {
			Arrays.fill(password, '\0');
		}
		if (keyImageDigest != null) {
			Arrays.fill(keyImageDigest, (byte) 0);
		}
	}

	private static void wipePixels(BufferedImage img) {
		java.awt.image.DataBuffer db = img.getRaster().getDataBuffer();
		for (int b = 0; b < db.getNumBanks(); b++) {
			if (db instanceof java.awt.image.DataBufferByte) {
				Arrays.fill(((java.awt.image.DataBufferByte) db).getData(b), (byte) 0);
			} else if (db instanceof java.awt.image.DataBufferInt) {
				Arrays.fill(((java.awt.image.DataBufferInt) db).getData(b), 0);
			} else if (db instanceof java.awt.image.DataBufferUShort) {
				Arrays.fill(((java.awt.image.DataBufferUShort) db).getData(b), (short) 0);
			} else if (db instanceof java.awt.image.DataBufferShort) {
				Arrays.fill(((java.awt.image.DataBufferShort) db).getData(b), (short) 0);
			}
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
			Arrays.fill(row.array(), (byte) 0);
			wipePixels(img); // the key image is a secret too: do not leave its pixels around
			return md.digest();
		} catch (IOException | GeneralSecurityException e) {
			throw new CryptoException("The key image cannot be read", e);
		}
	}
}
