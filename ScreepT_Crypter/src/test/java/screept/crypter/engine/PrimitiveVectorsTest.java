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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Assume;
import org.junit.Test;

/** Known answer tests of the pure Java primitives against the RFCs and independent implementations. */
public class PrimitiveVectorsTest {

	// ---------------------------------------------------------------- BLAKE2b (RFC 7693)

	@Test(timeout = 10000)
	public void blake2bMatchesRfc7693AndPython() throws Exception {
		assertEquals("ba80a53f981c4d0d6a2797b69f12f6e94c212f14685ac4b74b12bb6fdbffa2d17d87c5392aab792dc252d5de4533cc9518d38aa8dbf1925ab92386edd4009923",
				hex(new Blake2b(64).update("abc".getBytes(StandardCharsets.US_ASCII)).digest()));
		assertEquals("786a02f742015903c6c6fd852552d272912f4740e15847618a86e217f71f5419d25e1031afee585313896444934eb04b903a685b1448b755d56f701afe9be2ce",
				hex(new Blake2b(64).digest()));
		// 1204 digests (lengths 0..300 crossing the 128 byte blocks, sizes 1/32/48/64) aggregated; reference from Python hashlib
		MessageDigest agg = MessageDigest.getInstance("SHA-256");
		for (int n = 0; n <= 300; n++) {
			byte[] data = new byte[n];
			for (int i = 0; i < n; i++) {
				data[i] = (byte) (i * 7 + 3);
			}
			for (int o : new int[] { 1, 32, 48, 64 }) {
				agg.update(new Blake2b(o).update(data).digest());
			}
		}
		assertEquals("497509fb8cf5f7cb7464a890c2b0277ed9bc7d6eed5c9d47dfb5866a3c959b9f", hex(agg.digest()));
	}

	@Test
	public void blake2bIncrementalEqualsOneShot() {
		Random rnd = new Random(1);
		for (int n = 0; n < 600; n += 37) {
			byte[] data = new byte[n];
			rnd.nextBytes(data);
			Blake2b inc = new Blake2b(64);
			for (int pos = 0; pos < n;) {
				int len = Math.min(n - pos, 1 + rnd.nextInt(200));
				inc.update(data, pos, len);
				pos += len;
			}
			assertArrayEquals(new Blake2b(64).update(data).digest(), inc.digest());
		}
	}

	// ---------------------------------------------------------------- Argon2 (RFC 9106 section 5)

	private static byte[] fill(int n, int v) {
		byte[] b = new byte[n];
		Arrays.fill(b, (byte) v);
		return b;
	}

	private static String rfc9106(int type) {
		return hex(Argon2.hash(type, fill(32, 1), fill(16, 2), fill(8, 3), fill(12, 4), 3, 32, 4, 32));
	}

	@Test(timeout = 10000)
	public void argon2dMatchesRfc9106() {
		assertEquals("512b391b6f1162975371d30919734294f868e3be3984f3c1a13a4db9fabe4acb", rfc9106(Argon2.TYPE_D));
	}

	@Test(timeout = 10000)
	public void argon2iMatchesRfc9106() {
		assertEquals("c814d9d1dc7f37aa13f0d77f2494bda1c8de6b016dd388d29952a4c4672b6ce8", rfc9106(Argon2.TYPE_I));
	}

	@Test(timeout = 10000)
	public void argon2idMatchesRfc9106() {
		assertEquals("0d640df58d78766c08c037a34a8b53c9d01ef0452d75b65eb52520e96b01e659", rfc9106(Argon2.TYPE_ID));
	}

	@Test(timeout = 20000)
	public void argon2idKdfIsDeterministicAndSensitiveToEveryInput() throws Exception {
		int cost = Argon2.cost(13, 2, 4);
		byte[] salt = fill(16, 9);
		byte[] a = KdfAlgorithm.ARGON2ID.derive("pässwörd".toCharArray(), salt, cost, 64);
		assertArrayEquals(a, KdfAlgorithm.ARGON2ID.derive("pässwörd".toCharArray(), salt, cost, 64));
		assertFalse(Arrays.equals(a, KdfAlgorithm.ARGON2ID.derive("pässwörD".toCharArray(), salt, cost, 64)));
		assertFalse(Arrays.equals(a, KdfAlgorithm.ARGON2ID.derive("pässwörd".toCharArray(), fill(16, 8), cost, 64)));
		assertFalse(Arrays.equals(a, KdfAlgorithm.ARGON2ID.derive("pässwörd".toCharArray(), salt, Argon2.cost(13, 3, 4), 64)));
		assertFalse(Arrays.equals(a, KdfAlgorithm.ARGON2ID.derive("pässwörd".toCharArray(), salt, Argon2.cost(13, 2, 2), 64)));
		assertFalse(Arrays.equals(a, KdfAlgorithm.ARGON2ID.derive("pässwörd".toCharArray(), salt, Argon2.cost(14, 2, 4), 64)));
		// composed and decomposed accents (Windows/macOS keyboards, copy and paste) give the same key
		assertArrayEquals(a, KdfAlgorithm.ARGON2ID.derive("pässwörd".toCharArray(), salt, cost, 64));
	}

	@Test
	public void argon2CostBoundsRejectTamperedHeaders() {
		KdfAlgorithm k = KdfAlgorithm.ARGON2ID;
		assertEquals(true, k.isCostAcceptable(k.defaultCost));
		for (int bad : new int[] { 0, Argon2.cost(12, 3, 4), Argon2.cost(23, 3, 4), Argon2.cost(18, 0, 4), Argon2.cost(18, 11, 4),
				Argon2.cost(18, 3, 0), Argon2.cost(18, 3, 9), 0x01000000 | k.defaultCost, -1 }) {
			assertFalse("cost " + Integer.toHexString(bad), k.isCostAcceptable(bad));
		}
		assertEquals(64L << 20, k.memoryBytes(k.defaultCost));
		assertEquals(256L << 20, k.memoryBytes(CryptoProfile.ultra().kdfCost));
	}

	// ---------------------------------------------------------------- ChaCha20-Poly1305 (RFC 8439)

	@Test
	public void poly1305MatchesRfc8439Section252() {
		byte[] key = unhex("85d6be7857556d337f4452fe42d506a80103808afb0db2fd4abff6af4149f51b");
		byte[] msg = "Cryptographic Forum Research Group".getBytes(StandardCharsets.US_ASCII);
		ChaCha20Poly1305Layer.Poly1305 p = new ChaCha20Poly1305Layer.Poly1305(key);
		p.update(msg, 0, msg.length, false);
		assertEquals("a8061dc1305136c6c22b8baf0c0127a9", hex(p.finish()));
	}

	@Test
	public void aeadMatchesRfc8439Section282() throws Exception {
		byte[] key = new byte[32];
		for (int i = 0; i < 32; i++) {
			key[i] = (byte) (0x80 + i);
		}
		byte[] nonce = unhex("070000004041424344454647");
		byte[] aad = unhex("50515253c0c1c2c3c4c5c6c7");
		byte[] plain = ("Ladies and Gentlemen of the class of '99: If I could offer you only one tip for the future, sunscreen would be it.")
				.getBytes(StandardCharsets.US_ASCII);
		byte[] out = CipherAlgorithm.CHACHA20_POLY1305.encrypt(key, nonce, aad, plain);
		assertEquals("d31a8d34648e60db7b86afbc53ef7ec2", hex(Arrays.copyOf(out, 16)));
		assertEquals("1ae10b594f09e26a7e902ecbd0600691", hex(Arrays.copyOfRange(out, out.length - 16, out.length)));
		assertArrayEquals(plain, CipherAlgorithm.CHACHA20_POLY1305.decrypt(key, nonce, aad, out));
	}

	/** Same output as the JDK implementation (Java 11+; skipped on Java 8). */
	@Test(timeout = 30000)
	public void aeadEqualsJdkImplementation() throws Exception {
		Cipher jdk;
		try {
			jdk = Cipher.getInstance("ChaCha20-Poly1305");
		} catch (Exception e) {
			Assume.assumeTrue("ChaCha20-Poly1305 not in this JDK", false);
			return;
		}
		Random rnd = new Random(7);
		for (int n : new int[] { 0, 1, 15, 16, 17, 63, 64, 65, 127, 128, 129, 1000, 4096, 70001 }) {
			byte[] key = new byte[32], nonce = new byte[12], aad = new byte[rnd.nextInt(40)], plain = new byte[n];
			rnd.nextBytes(key);
			rnd.nextBytes(nonce);
			rnd.nextBytes(aad);
			rnd.nextBytes(plain);
			jdk.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "ChaCha20"), new IvParameterSpec(nonce));
			jdk.updateAAD(aad);
			assertArrayEquals("length " + n, jdk.doFinal(plain), CipherAlgorithm.CHACHA20_POLY1305.encrypt(key, nonce, aad, plain));
		}
	}

	@Test
	public void aeadRejectsAnyModification() throws Exception {
		byte[] key = fill(32, 5), nonce = fill(12, 6), aad = fill(20, 7), plain = fill(100, 8);
		byte[] ct = CipherAlgorithm.CHACHA20_POLY1305.encrypt(key, nonce, aad, plain);
		for (int i = 0; i < ct.length; i++) {
			for (int bit = 0; bit < 8; bit++) {
				byte[] t = ct.clone();
				t[i] ^= 1 << bit;
				expectReject(key, nonce, aad, t);
			}
		}
		byte[] aad2 = aad.clone();
		aad2[0] ^= 1;
		expectReject(key, nonce, aad2, ct);
		expectReject(key, fill(12, 9), aad, ct);
		expectReject(fill(32, 4), nonce, aad, ct);
		expectReject(key, nonce, aad, Arrays.copyOf(ct, ct.length - 1));
		expectReject(key, nonce, aad, new byte[5]);
	}

	private static void expectReject(byte[] key, byte[] nonce, byte[] aad, byte[] ct) {
		try {
			CipherAlgorithm.CHACHA20_POLY1305.decrypt(key, nonce, aad, ct);
			fail("modified data accepted");
		} catch (CryptoException expected) {
			// ok
		}
	}

	// ---------------------------------------------------------------- helpers

	static String hex(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (byte x : b) {
			sb.append(String.format("%02x", x & 0xff));
		}
		return sb.toString();
	}

	static byte[] unhex(String s) {
		byte[] b = new byte[s.length() / 2];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
		}
		return b;
	}
}
