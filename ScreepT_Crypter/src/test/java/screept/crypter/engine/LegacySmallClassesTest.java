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

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JPasswordField;

import org.junit.Test;

import screept.crypter.impl.aes.AESException;
import screept.crypter.impl.aes.AesCipher;
import screept.crypter.impl.aes.AesCipher256;
import screept.crypter.impl.manager.CryptoMode;
import screept.crypter.om.CrypterResultDTO;
import screept.crypter.util.ImageEncodingUtils;

/** Small classes of the crypter that the GUI and the legacy paths rely on: modes, result, exceptions, key image helpers. */
public class LegacySmallClassesTest {

	@Test
	public void everyModeHasLabelIdAndConsistentFlags() {
		for (CryptoMode m : CryptoMode.values()) {
			assertNotNull(m.toString());
			assertNotNull(m.getID());
			assertEquals(m.AES, m.isAES());
			assertEquals(m.STEG, m.isSTEG());
			assertEquals(m.COMPL, m.isCOMPL());
			if (m.profile != null) {
				assertFalse("engine modes never use the legacy steg flag", m.COMPL);
			}
		}
		assertEquals(CryptoMode.SECURE_256, CryptoMode.getModeFromBoolean(true, false));
		assertEquals(CryptoMode.ONLY_STEG, CryptoMode.getModeFromBoolean(false, true));
		assertEquals(CryptoMode.COMPLETE, CryptoMode.getModeFromBoolean(true, true));
		CryptoMode mode = CryptoMode.ONLY_AES;
		boolean aes = mode.AES;
		mode.setSTEG(true);
		mode.setSTEG(false);
		mode.setCOMPL(false);
		assertEquals(aes, mode.AES);
	}

	@Test
	public void resultDtoAndException() {
		CrypterResultDTO r = new CrypterResultDTO();
		assertFalse(r.isResultOK());
		r.setResultOK(true);
		r.setInterrupted(true);
		assertTrue(r.isResultOK());
		assertTrue(r.isInterrupted());
		assertNotNull(r.toString());
		assertTrue(new CrypterResultDTO(true, false).resultOK);
		assertNotNull(new AESException().getMessage());
		assertNotNull(new AESException("custom").getMessage());
	}

	@Test
	public void aes256AccessorsAndKeyCheck() throws Exception {
		AesCipher256 c = new AesCipher256("a long enough 256 bit phrase");
		assertNotNull(c.getCipher());
		assertNotNull(c.getIvParameterSpec());
		assertNotNull(c.getPassword());
		c.setDisplayedKey("shown");
		assertEquals("shown", c.getDisplayedKey());
		c.setIvParameterSpec(c.getIvParameterSpec());
		c.setCipher(c.getCipher());
		assertNotNull(new AesCipher256());
	}

	@Test
	public void aesAccessorsAndSensitiveDataWipe() throws Exception {
		char[] psw = "wipe me please 123".toCharArray();
		AesCipher c = new AesCipher(psw);
		assertNotNull(c.getSecretKeySpec());
		assertNotNull(c.getCipher());
		assertNotNull(c.getIvParameterSpec());
		assertNotNull(c.getPassword());
		c.setSecretKeySpec(c.getSecretKeySpec());
		c.setIvParameterSpec(c.getIvParameterSpec());
		c.setCipher(c.getCipher());
		c.setDisplayedKey("abc".toCharArray());
		assertArrayEquals("abc".toCharArray(), c.getDisplayedKey());
		assertEquals(16, c.charKeyToByteArray().length);
		assertTrue(c.charKeyToByteArrayV2("another key".toCharArray()).length > 0);
		c.clearSensitiveData();
		assertNotNull(c);
	}

	@Test
	public void passwordFieldCheckHandlesPlaceholderAndText() throws Exception {
		JPasswordField field = new JPasswordField("");
		AesCipher.check(field);
		field.setText("something typed");
		AesCipher.check(field);
		assertEquals("something typed", new String(field.getPassword()));
		javax.swing.JTextField text = new javax.swing.JTextField("plain");
		AesCipher256.check(text);
		assertNotNull(AesCipher.isPasswordSafeNoSpecials("A-very-Long-and-Strong-1234".toCharArray(), 10));
		assertNotNull(AesCipher.isPasswordSafeBestSecurity("A-very-Long-and-Strong-1234!".toCharArray(), 10));
	}

	@Test
	public void keyImageHelpers() throws Exception {
		BufferedImage img = ImageEncodingUtils.generaKeyImage(60, 40, BufferedImage.TYPE_3BYTE_BGR);
		assertEquals(60, img.getWidth());
		assertEquals(40, img.getHeight());
		BufferedImage part = ImageEncodingUtils.getPortion(img, 20, 20);
		assertEquals(20, part.getWidth());
		int[] values = ImageEncodingUtils.readNSpacedValuesFromImage(img, 7);
		assertEquals(7, values.length);
		assertFalse(Arrays.equals(values, ImageEncodingUtils.readNSpacedValuesFromImage(ImageEncodingUtils.generaKeyImage(60, 40, BufferedImage.TYPE_3BYTE_BGR), 7)));
		assertNotNull(new Random());
	}
}
