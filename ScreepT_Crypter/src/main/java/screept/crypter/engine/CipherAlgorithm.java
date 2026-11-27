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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Catalogue and registry of the available {@link CipherLayer}s. Built-ins are listed here; third parties can add
 * their own with {@link #register(CipherLayer)} (ids written in file headers: never change or reuse one).
 */
public final class CipherAlgorithm {

	public static final int NONCE_BYTES = 12;

	public static final CipherLayer AES_128_GCM = new AesGcmLayer(1, "AES-128", 16);
	public static final CipherLayer AES_256_GCM = new AesGcmLayer(2, "AES-256", 32);
	/** StegaCREEPT: AES-256-GCM keyed also by a secret key image (second factor). */
	public static final CipherLayer STEGA_CREEPT = new KeyImageLayer(3, "StegaCREEPT key image");
	/** ChaCha20-Poly1305 (RFC 8439): a second cipher family, for stacking with AES. */
	public static final CipherLayer CHACHA20_POLY1305 = new ChaCha20Poly1305Layer(4, "ChaCha20-Poly1305");
	public static final CipherLayer SHUFFLE = new ShuffleLayer(16, "Shuffle");

	private static final Map<Integer, CipherLayer> REGISTRY = new TreeMap<Integer, CipherLayer>();

	static {
		register(AES_128_GCM);
		register(AES_256_GCM);
		register(STEGA_CREEPT);
		register(CHACHA20_POLY1305);
		register(SHUFFLE);
	}

	private CipherAlgorithm() {
	}

	/** Adds a layer so that files using it can be written and read. Rejects ids already taken. */
	public static synchronized void register(CipherLayer layer) {
		if (layer.id() < 1 || layer.id() > 255) {
			throw new IllegalArgumentException("Layer id must be in 1..255");
		}
		CipherLayer old = REGISTRY.get(layer.id());
		if (old != null && old != layer) {
			throw new IllegalArgumentException("Layer id " + layer.id() + " already used by " + old.label());
		}
		REGISTRY.put(layer.id(), layer);
	}

	public static synchronized CipherLayer fromId(int id) throws CryptoException {
		CipherLayer l = REGISTRY.get(id);
		if (l == null) {
			throw new CryptoException("Unknown cipher id " + id + " (file made by a newer version?)");
		}
		return l;
	}

	public static synchronized List<CipherLayer> all() {
		return Collections.unmodifiableList(new ArrayList<CipherLayer>(REGISTRY.values()));
	}
}
