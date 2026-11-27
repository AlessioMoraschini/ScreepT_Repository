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

import java.security.MessageDigest;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

/**
 * The wipeable SHA-2, HMAC and PBKDF2 must give exactly the JDK results: files written with the JDK PBKDF2 (every
 * version up to 0.9.97) must still open, with the same password, byte for byte.
 */
public class WipeablePrimitivesTest {

	@Test(timeout = 30000)
	public void sha2EqualsJdk() throws Exception {
		Random rnd = new Random(1);
		for (boolean big : new boolean[] { false, true }) {
			MessageDigest jdk = MessageDigest.getInstance(big ? "SHA-512" : "SHA-256");
			Sha2 mine = new Sha2(big);
			for (int n = 0; n <= 600; n++) {
				byte[] data = new byte[n];
				rnd.nextBytes(data);
				// fed in random pieces, to cross the block buffer in every way
				for (int pos = 0; pos < n;) {
					int len = Math.min(n - pos, rnd.nextInt(150));
					mine.update(data, pos, len);
					pos += len;
				}
				byte[] out = new byte[mine.out];
				mine.digest(out);
				assertArrayEquals((big ? "SHA-512 " : "SHA-256 ") + n, jdk.digest(data), out);
			}
		}
	}

	@Test(timeout = 30000)
	public void hmacEqualsJdk() throws Exception {
		Random rnd = new Random(2);
		for (boolean big : new boolean[] { false, true }) {
			String alg = big ? "HmacSHA512" : "HmacSHA256";
			for (int keyLen : new int[] { 1, 31, 32, 63, 64, 65, 127, 128, 129, 300 }) {
				for (int msgLen : new int[] { 0, 1, 55, 56, 64, 111, 112, 128, 1000 }) {
					byte[] key = new byte[keyLen], msg = new byte[msgLen];
					rnd.nextBytes(key);
					rnd.nextBytes(msg);
					Mac mac = Mac.getInstance(alg);
					mac.init(new SecretKeySpec(key, alg));
					Pbkdf2.Hmac mine = new Pbkdf2.Hmac(big, key);
					byte[] out = new byte[mine.length()];
					mine.mac(msg, msg.length, out);
					assertArrayEquals(alg + " key " + keyLen + " msg " + msgLen, mac.doFinal(msg), out);
				}
			}
		}
	}

	@Test(timeout = 60000)
	public void pbkdf2EqualsJdkForEveryKindOfPassword() throws Exception {
		Random rnd = new Random(3);
		StringBuilder longPsw = new StringBuilder();
		for (int i = 0; i < 200; i++) {
			longPsw.append("pässwörd");
		}
		String[] passwords = { "a", "password", "pässwörd 🔑", "日本語のパスワード", "\uD800lonely surrogate", "x\u0000y", longPsw.toString(),
				"tab\tand\nnewline" };
		for (String jdkName : new String[] { "PBKDF2WithHmacSHA256", "PBKDF2WithHmacSHA512" }) {
			SecretKeyFactory f = SecretKeyFactory.getInstance(jdkName);
			for (String psw : passwords) {
				for (int iterations : new int[] { 1, 2, 3, 1000 }) {
					for (int outBytes : new int[] { 1, 32, 63, 64, 65, 128 }) {
						byte[] salt = new byte[rnd.nextInt(40) + 1];
						rnd.nextBytes(salt);
						byte[] jdk = f.generateSecret(new PBEKeySpec(psw.toCharArray(), salt, iterations, outBytes * 8)).getEncoded();
						assertArrayEquals(jdkName + " '" + psw + "' " + iterations + " " + outBytes, jdk,
								KdfAlgorithm.pbkdf2(jdkName, psw.toCharArray(), salt, iterations, outBytes));
					}
				}
			}
		}
	}

	@Test(timeout = 60000)
	public void pbkdf2IsFasterThanTheJdk() throws Exception {
		char[] psw = "speed test".toCharArray();
		byte[] salt = new byte[16];
		int it = 200000;
		long t0 = System.nanoTime();
		byte[] jdk = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512").generateSecret(new PBEKeySpec(psw, salt, it, 512)).getEncoded();
		long t1 = System.nanoTime();
		byte[] mine = KdfAlgorithm.pbkdf2("PBKDF2WithHmacSHA512", psw, salt, it, 64);
		long t2 = System.nanoTime();
		assertArrayEquals(jdk, mine);
		System.out.println(String.format("PBKDF2-SHA512 %d iterations: JDK %d ms, ScreepT %d ms", it, (t1 - t0) / 1000000, (t2 - t1) / 1000000));
	}
}
