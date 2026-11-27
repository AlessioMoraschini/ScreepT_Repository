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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static screept.crypter.engine.PrimitiveVectorsTest.hex;
import static screept.crypter.engine.PrimitiveVectorsTest.unhex;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import screept.crypter.impl.manager.CryptoMode;
import screept.crypter.impl.manager.ScreeptManagerFileOrFolder;

/** Public key files: X25519 and HKDF vectors, sealing for many recipients, identities, and every kind of misuse. */
public class PublicKeyTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final CryptoProfile PUB = new CryptoProfile(KdfAlgorithm.X25519, 1, 10,
			Arrays.asList(CipherAlgorithm.AES_256_GCM, CipherAlgorithm.CHACHA20_POLY1305));

	// ---------------------------------------------------------------- vectors

	@Test
	public void x25519MatchesRfc7748() {
		byte[] alice = unhex("77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a");
		byte[] bob = unhex("5dab087e624a8a4b79e17f8b83800ee66f3bb1292618b6fd1c2f8b27ff88e0eb");
		assertEquals("8520f0098930a754748b7ddcb43ef75a0dbf3a0d26381af4eba4a98eaa9b4e6a", hex(X25519.publicKey(alice)));
		assertEquals("de9edb7d7b7dc1b4d35b61c2ece435373f8343c85b78674dadfc7e146f882b4f", hex(X25519.publicKey(bob)));
		String shared = "4a5d9d5ba4ce2de1728e3bf480350f25e07e21c947d19e3376f09b3c1e161742";
		assertEquals(shared, hex(X25519.scalarMult(alice, X25519.publicKey(bob))));
		assertEquals(shared, hex(X25519.scalarMult(bob, X25519.publicKey(alice))));
		assertEquals("c3da55379de9c6908e94ea4df28d084f32eccf03491c71f754b4075577a28552",
				hex(X25519.scalarMult(unhex("a546e36bf0527c9d3b16154b82465edd62144c0ac1fc5a18506a2244ba449ac4"),
						unhex("e6db6867583030db3594c1a424b15f7c726624ec26b3353b10a903a6d0ab1c4c"))));
	}

	@Test(timeout = 60000)
	public void x25519IteratedMatchesRfc7748() {
		byte[] k = new byte[32], u = new byte[32];
		k[0] = 9;
		u[0] = 9;
		for (int i = 1; i <= 1000; i++) {
			byte[] r = X25519.scalarMult(k, u);
			u = k;
			k = r;
			if (i == 1) {
				assertEquals("422c8e7a6227d7bca1350b3e2bb7279f7897b87bb6854b783c60e80311ae3079", hex(k));
			}
		}
		assertEquals("684cf59ba83309552800ef566f2f4d3c1c3887c49360e3875f2eb94d99532c51", hex(k));
	}

	@Test
	public void hkdfMatchesRfc5869() {
		byte[] okm = Pbkdf2.hkdfSha256(unhex("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b"), unhex("000102030405060708090a0b0c"),
				unhex("f0f1f2f3f4f5f6f7f8f9"), 42);
		assertEquals("3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf34007208d5b887185865", hex(okm));
	}

	@Test
	public void lowOrderPointsAreRefused() {
		for (byte[] bad : new byte[][] { new byte[32], unhex("0100000000000000000000000000000000000000000000000000000000000000"),
				unhex("e0eb7a7c3b41b8ae1656e3faf19fc46ada098deb9c32b1fd866205165f49b800") }) {
			try {
				Recipients.seal(new byte[64], bad, new byte[16]);
				fail("low order key accepted: " + hex(bad));
			} catch (CryptoException expected) {
				// ok
			}
		}
	}

	// ---------------------------------------------------------------- engine

	private static List<Identity> people(int n) {
		List<Identity> l = new ArrayList<Identity>();
		for (int i = 0; i < n; i++) {
			l.add(Identity.generate());
		}
		return l;
	}

	private static List<byte[]> pubs(List<Identity> ids) {
		List<byte[]> l = new ArrayList<byte[]>();
		for (Identity i : ids) {
			l.add(i.publicKey());
		}
		return l;
	}

	private static CryptoKey keyOf(Identity id) {
		return CryptoKey.of("".toCharArray()).withIdentity(id);
	}

	@Test(timeout = 60000)
	public void everyRecipientAndOnlyThemCanOpen() throws Exception {
		for (int n : new int[] { 1, 2, 5, KdfAlgorithm.MAX_RECIPIENTS }) {
			List<Identity> ids = people(n);
			byte[] plain = CrypterTortureTest.data(3000 + n, n);
			byte[] enc = CryptoEngine.encrypt(plain, CryptoKey.of(new char[0]).withRecipients(pubs(ids)), PUB);
			CryptoProfile seen = CryptoEngine.readProfile(Arrays.copyOf(enc, CryptoEngine.MAX_HEADER_BYTES));
			assertEquals(KdfAlgorithm.X25519, seen.kdf);
			assertEquals(n, seen.kdfCost);
			for (Identity id : ids) {
				assertArrayEquals(plain, CryptoEngine.decrypt(enc, keyOf(id)));
			}
			expectReject(enc, keyOf(Identity.generate()), "a stranger");
			expectReject(enc, CryptoKey.of("password".toCharArray()), "a password");
		}
	}

	@Test(timeout = 60000)
	public void everyModificationOfTheEnvelopesIsRejected() throws Exception {
		List<Identity> ids = people(3);
		byte[] enc = CryptoEngine.encrypt(CrypterTortureTest.data(1500, 9), CryptoKey.of(new char[0]).withRecipients(pubs(ids)), PUB);
		int headerEnd = 12 + 2 + 16 + 8 + 3 * Recipients.STANZA_BYTES;
		for (int i = 0; i < headerEnd + 40; i++) {
			if (i >= 6 && i <= 9) {
				continue; // cost = number of envelopes: covered below
			}
			byte[] t = enc.clone();
			t[i] ^= 0x01;
			for (Identity id : ids) {
				expectReject(t, keyOf(id), "bit flipped at " + i);
			}
		}
		for (int cost : new int[] { 0, 2, 4, 33, 255 }) {
			byte[] t = enc.clone();
			t[9] = (byte) cost;
			expectReject(t, keyOf(ids.get(0)), "envelope count " + cost);
		}
		// envelopes swapped: the header changes, so every chunk refuses it
		byte[] swapped = enc.clone();
		int s0 = 12 + 2 + 16 + 8;
		System.arraycopy(enc, s0 + Recipients.STANZA_BYTES, swapped, s0, Recipients.STANZA_BYTES);
		System.arraycopy(enc, s0, swapped, s0 + Recipients.STANZA_BYTES, Recipients.STANZA_BYTES);
		expectReject(swapped, keyOf(ids.get(0)), "envelopes swapped");
	}

	@Test
	public void badRecipientListsAreRefused() throws Exception {
		List<List<byte[]>> bad = new ArrayList<List<byte[]>>();
		bad.add(Collections.<byte[]>emptyList());
		bad.add(pubs(people(KdfAlgorithm.MAX_RECIPIENTS + 1)));
		bad.add(Arrays.asList(new byte[32]));
		bad.add(Arrays.asList(new byte[31]));
		for (List<byte[]> l : bad) {
			try {
				CryptoEngine.encrypt(new byte[10], CryptoKey.of(new char[0]).withRecipients(l), PUB);
				fail("accepted " + l.size() + " recipients");
			} catch (CryptoException expected) {
				// ok
			}
		}
		try {
			CryptoEngine.encrypt(new byte[10], CryptoKey.of(new char[0]), PUB);
			fail("no recipients accepted");
		} catch (CryptoException expected) {
			// ok
		}
	}

	@Test(timeout = 60000)
	public void publicKeysCombineWithTheKeyImageLayer() throws Exception {
		File img = keyImage(tmp.newFolder(), 1);
		File other = keyImage(tmp.newFolder(), 2);
		Identity me = Identity.generate();
		CryptoProfile p = new CryptoProfile(KdfAlgorithm.X25519, 1, 10, Arrays.asList(CipherAlgorithm.AES_256_GCM, CipherAlgorithm.STEGA_CREEPT));
		byte[] plain = CrypterTortureTest.data(2000, 3);
		byte[] enc = CryptoEngine.encrypt(plain, CryptoKey.of(new char[0], img).withRecipients(Arrays.asList(me.publicKey())), p);
		assertArrayEquals(plain, CryptoEngine.decrypt(enc, CryptoKey.of(new char[0], img).withIdentity(me)));
		expectReject(enc, CryptoKey.of(new char[0], other).withIdentity(me), "other key image");
		expectReject(enc, keyOf(me), "no key image");
	}

	@Test(timeout = 60000)
	public void filesAndFoldersForManyRecipientsAreRecognisedFromTheHeader() throws Exception {
		List<Identity> ids = people(KdfAlgorithm.MAX_RECIPIENTS); // biggest header (3.6 KB)
		File src = tmp.newFolder("shared docs");
		Files.write(new File(src, "a.txt").toPath(), "hello".getBytes("UTF-8"));
		new File(src, "sub").mkdirs();
		Files.write(new File(src, "sub/b.bin").toPath(), CrypterTortureTest.data(5000, 1));
		for (FileCrypter.OutputMode mode : FileCrypter.OutputMode.values()) {
			File enc = FileCrypter.encrypt(src, tmp.newFolder(), CryptoKey.of(new char[0]).withRecipients(pubs(ids)), PUB, mode);
			assertTrue(mode + " recognised", FileCrypter.isEngineInput(enc));
			if (enc.isFile() && mode == FileCrypter.OutputMode.SINGLE_ENCRYPTED_ARCHIVE) {
				assertEquals(KdfAlgorithm.X25519, FileCrypter.inspect(enc).kdf);
			}
			File dec = tmp.newFolder();
			File out = FileCrypter.decrypt(enc, dec, keyOf(ids.get(KdfAlgorithm.MAX_RECIPIENTS - 1)));
			File restored = out.getName().equals(src.getName()) ? out : new File(dec, src.getName());
			LayerMatrixRoundTripTest.assertSameTree(src, restored, false);
		}
	}

	// ---------------------------------------------------------------- identities

	@Test(timeout = 60000)
	public void identityFileRoundTripsAndRefusesWrongPasswordsAndDamage() throws Exception {
		Identity id = Identity.generate();
		File f = new File(tmp.newFolder(), "me.scptkey");
		id.save(f, "identity-password".toCharArray());
		Identity back = Identity.load(f, "identity-password".toCharArray());
		assertArrayEquals(id.publicKey(), back.publicKey());
		assertEquals(KdfAlgorithm.ARGON2ID, FileCrypter.inspect(f).kdf);
		for (String wrong : new String[] { "identity-passwor", "Identity-password", "x" }) {
			try {
				Identity.load(f, wrong.toCharArray());
				fail("wrong password accepted");
			} catch (CryptoException expected) {
				// ok
			}
		}
		byte[] b = Files.readAllBytes(f.toPath());
		b[b.length - 5] ^= 1;
		Files.write(f.toPath(), b);
		try {
			Identity.load(f, "identity-password".toCharArray());
			fail("damaged key file accepted");
		} catch (CryptoException expected) {
			// ok
		}
		// a normal encrypted file is not a key
		File notKey = new File(tmp.getRoot(), "note.scpt");
		Files.write(notKey.toPath(), CryptoEngine.encrypt("hello".getBytes("UTF-8"), "identity-password".toCharArray(), CryptoProfile.standard()));
		try {
			Identity.load(notKey, "identity-password".toCharArray());
			fail("not a key accepted");
		} catch (CryptoException expected) {
			// ok
		}
	}

	@Test
	public void publicKeyTextSurvivesCopyAndCatchesTypos() throws Exception {
		Identity id = Identity.generate();
		String text = id.publicKeyText();
		assertTrue(text.startsWith("scpt1"));
		assertArrayEquals(id.publicKey(), Identity.parsePublicKey(text));
		assertArrayEquals(id.publicKey(), Identity.parsePublicKey("  " + text.substring(0, 20) + "\n " + text.substring(20) + "\t"));
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
		Random rnd = new Random(1);
		for (int i = 5; i < text.length(); i++) {
			char c = text.charAt(i);
			char other;
			do {
				other = alphabet.charAt(rnd.nextInt(alphabet.length()));
			} while (other == c);
			try {
				byte[] got = Identity.parsePublicKey(text.substring(0, i) + other + text.substring(i + 1));
				// the very last character carries 2 unused bits: a change there may decode to the same key
				assertTrue("typo at " + i + " accepted as another key", Arrays.equals(got, id.publicKey()) && i == text.length() - 1);
			} catch (CryptoException expected) {
				// ok
			}
		}
		for (String bad : new String[] { "", "scpt1", "pub1" + text.substring(5), text.substring(0, text.length() - 3), text + "AA" }) {
			try {
				Identity.parsePublicKey(bad);
				fail("accepted: " + bad);
			} catch (CryptoException expected) {
				// ok
			}
		}
	}

	// ---------------------------------------------------------------- manager (what the Crypter window calls)

	@Test(timeout = 120000)
	public void managerEncryptsForPeopleAndOpensWithThePrivateKeyFile() throws Exception {
		Identity me = Identity.generate(), friend = Identity.generate();
		File myKey = new File(tmp.newFolder(), "me.scptkey"), friendKey = new File(tmp.getRoot(), "friend.scptkey");
		me.save(myKey, "my-password-1".toCharArray());
		friend.save(friendKey, "friend-password".toCharArray());
		File src = new File(tmp.newFolder(), "contract.txt");
		Files.write(src.toPath(), "signed".getBytes("UTF-8"));
		File encDir = tmp.newFolder();
		ScreeptManagerFileOrFolder m = new ScreeptManagerFileOrFolder(null, null, false);
		m.setRecipients(Arrays.asList(me.publicKey(), friend.publicKey()));
		assertTrue(m.crYpt(CryptoMode.SECURE_PUBLIC, src, encDir, new char[0], null, false).resultOK);
		File enc = new File(encDir, "contract.txt.scpt");
		assertTrue(enc.isFile());

		for (Object[] who : new Object[][] { { friendKey, "friend-password" }, { myKey, "my-password-1" } }) {
			File out = tmp.newFolder();
			m.setIdentityFile((File) who[0]);
			// whatever mode is selected: the header decides
			assertTrue(m.decrYpt(CryptoMode.SECURE_256, enc, out, ((String) who[1]).toCharArray(), null, false).resultOK);
			assertEquals("signed", new String(Files.readAllBytes(new File(out, "contract.txt").toPath()), "UTF-8"));
		}
		m.setIdentityFile(friendKey);
		File out = tmp.newFolder();
		assertFalse(m.decrYpt(CryptoMode.SECURE_256, enc, out, "wrong".toCharArray(), null, false).resultOK);
		assertEquals(0, out.list().length);
		m.setIdentityFile(null);
		assertFalse(m.decrYpt(CryptoMode.SECURE_256, enc, tmp.newFolder(), "friend-password".toCharArray(), null, false).resultOK);
		m.setRecipients(null);
		assertFalse(m.crYpt(CryptoMode.SECURE_PUBLIC, src, tmp.newFolder(), new char[0], null, false).resultOK);
	}

	private static void expectReject(byte[] enc, CryptoKey key, String what) {
		try {
			CryptoEngine.decrypt(enc, key);
			fail("accepted: " + what);
		} catch (CryptoException expected) {
			// ok
		} catch (Throwable t) {
			throw new AssertionError("not a CryptoException for " + what + ": " + t, t);
		}
	}

	static File keyImage(File dir, int seed) throws Exception {
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(40, 30, java.awt.image.BufferedImage.TYPE_INT_RGB);
		Random r = new Random(seed);
		for (int y = 0; y < 30; y++) {
			for (int x = 0; x < 40; x++) {
				img.setRGB(x, y, r.nextInt());
			}
		}
		File f = new File(dir, "key.png");
		javax.imageio.ImageIO.write(img, "png", f);
		return f;
	}

	static void assertNotEmpty(byte[] b) {
		assertNotNull(b);
		assertTrue(b.length > 0);
	}
}
