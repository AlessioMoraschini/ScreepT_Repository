/**
 * The Crypter engine: authenticated, streaming, modular file encryption (container "SCP2").
 *
 * <h3>Pieces</h3>
 * <ul>
 * <li>{@link screept.crypter.engine.KdfAlgorithm}: how a password becomes key material
 *     (PBKDF2-SHA256/512 from the JDK; scrypt and Argon2id in pure Java, no dependencies).</li>
 * <li>{@link screept.crypter.engine.CipherLayer}: one stackable step. Built-ins in
 *     {@link screept.crypter.engine.CipherAlgorithm}: AES-128-GCM, AES-256-GCM, ChaCha20-Poly1305 (pure Java, RFC 8439),
 *     StegaCREEPT (AES-256-GCM that also needs a secret key image) and Shuffle (keyed byte permutation, an obfuscation on top of the others).</li>
 * <li>{@link screept.crypter.engine.CryptoProfile}: KDF + ordered list of layers. Ready made: standard, aes128,
 *     withKeyImage, maximum, ultra (Argon2id + AES-256-GCM + ChaCha20-Poly1305); or any combination with {@link screept.crypter.engine.CryptoProfile#of}.</li>
 * <li>{@link screept.crypter.engine.CryptoKey}: password and optional key image, in memory only.</li>
 * <li>{@link screept.crypter.engine.CryptoEngine}: encrypts/decrypts streams and byte arrays.</li>
 * <li>{@link screept.crypter.engine.FileCrypter}: files, folders, archives and zips on top of the engine, no GUI.</li>
 * </ul>
 *
 * <h3>File layout</h3>
 * <pre>
 * "SCP2" | version | kdfId | kdfCost(4) | chunkLog2 | nLayers | layerIds... | salt(16) | noncePrefix(8)
 * then chunks of 2^chunkLog2 bytes, each one passed through every layer; the last chunk is always shorter.
 * </pre>
 * The header is not secret: it lets decryption find out the algorithms by itself, so the user only gives the
 * password (and the key image if the file needs one). The header and the position of every chunk are authenticated,
 * so tampering, truncation, reordering and wrong keys are all detected. Every layer gets its own key (HMAC-SHA256 of
 * the master key with the layer index), so stacking layers never reuses key material.
 * The pure Java primitives are checked against the RFC test vectors (RFC 7693, 7914, 8439, 9106) and, on Java 11+,
 * against the JDK ChaCha20-Poly1305.
 *
 * <h3>Adding an algorithm</h3>
 * Implement {@link screept.crypter.engine.CipherLayer} (stateless, no logging of secrets), pick a NEW id and call
 * {@link screept.crypter.engine.CipherAlgorithm#register}. Never change the id or the behaviour of an existing layer:
 * files already written depend on it. A profile needs at least one authenticated layer.
 *
 * <h3>Legacy</h3>
 * Files written by the old crypter (AES-128 CBC and PNG key images, no header) are still handled by
 * {@code screept.crypter.impl}; the engine files are recognised by their header, whatever mode is selected.
 */
package screept.crypter.engine;
