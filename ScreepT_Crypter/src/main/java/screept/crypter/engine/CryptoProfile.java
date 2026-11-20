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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * What a file is protected with: how the password becomes a key (KDF) and which ciphers are stacked,
 * in order (first = innermost, applied first when encrypting). Stored in clear in the file header
 * (and authenticated by every chunk), so decryption recognises it automatically.
 * Never contains passwords or keys.
 */
public final class CryptoProfile {

	public static final int MAX_LAYERS = 6;
	public static final int DEFAULT_CHUNK_LOG2 = 20; // 1 MiB

	public final KdfAlgorithm kdf;
	public final int kdfCost;
	public final int chunkLog2;
	private final List<CipherLayer> layers;

	public CryptoProfile(KdfAlgorithm kdf, int kdfCost, List<CipherLayer> layers) {
		this(kdf, kdfCost, DEFAULT_CHUNK_LOG2, layers);
	}

	CryptoProfile(KdfAlgorithm kdf, int kdfCost, int chunkLog2, List<CipherLayer> layers) {
		if (layers.isEmpty() || layers.size() > MAX_LAYERS) {
			throw new IllegalArgumentException("1 to " + MAX_LAYERS + " cipher layers are supported");
		}
		boolean authenticated = false;
		for (CipherLayer l : layers) {
			authenticated |= l.authenticated();
		}
		if (!authenticated) {
			throw new IllegalArgumentException("At least one authenticated layer (AES-GCM) is required");
		}
		if (!kdf.isCostAcceptable(kdfCost)) {
			throw new IllegalArgumentException("Cost out of range for " + kdf.label);
		}
		this.kdf = kdf;
		this.kdfCost = kdfCost;
		this.chunkLog2 = chunkLog2;
		this.layers = Collections.unmodifiableList(new ArrayList<CipherLayer>(layers));
	}

	public List<CipherLayer> getLayers() {
		return layers;
	}

	/** Builds a profile from any combination of layers, applied in the given order. */
	public static CryptoProfile of(KdfAlgorithm kdf, int kdfCost, CipherLayer... layers) {
		return new CryptoProfile(kdf, kdfCost, Arrays.asList(layers));
	}

	/** Bytes added to every chunk by all the layers together. */
	public int overheadPerChunk() {
		int o = 0;
		for (CipherLayer l : layers) {
			o += l.overhead();
		}
		return o;
	}

	public boolean needsKeyImage() {
		for (CipherLayer l : layers) {
			if (l.needsKeyImage()) {
				return true;
			}
		}
		return false;
	}

	public int chunkSize() {
		return 1 << chunkLog2;
	}

	/** Recommended: AES-256-GCM, PBKDF2-HMAC-SHA512. */
	public static CryptoProfile standard() {
		return new CryptoProfile(KdfAlgorithm.PBKDF2_SHA512, KdfAlgorithm.PBKDF2_SHA512.defaultCost,
				Arrays.asList(CipherAlgorithm.AES_256_GCM));
	}

	/** AES-256-GCM plus StegaCREEPT: password and secret key image are both needed. */
	public static CryptoProfile withKeyImage() {
		return new CryptoProfile(KdfAlgorithm.PBKDF2_SHA512, KdfAlgorithm.PBKDF2_SHA512.defaultCost,
				Arrays.asList(CipherAlgorithm.AES_256_GCM, CipherAlgorithm.STEGA_CREEPT));
	}

	/** AES-128-GCM only (smaller and faster, still strong). */
	public static CryptoProfile aes128() {
		return new CryptoProfile(KdfAlgorithm.PBKDF2_SHA512, KdfAlgorithm.PBKDF2_SHA512.defaultCost,
				Arrays.asList(CipherAlgorithm.AES_128_GCM));
	}

	/** Memory-hard KDF and two independent AES layers (128 then 256), each with its own derived key. */
	public static CryptoProfile maximum() {
		return new CryptoProfile(KdfAlgorithm.SCRYPT, KdfAlgorithm.SCRYPT.defaultCost,
				Arrays.asList(CipherAlgorithm.AES_128_GCM, CipherAlgorithm.AES_256_GCM));
	}

	public String describe() {
		StringBuilder sb = new StringBuilder();
		for (CipherLayer c : layers) {
			sb.append(sb.length() == 0 ? "" : " + ").append(c.label());
		}
		return sb.append(" / ").append(kdf.label).toString();
	}

	@Override
	public String toString() {
		return describe();
	}
}
