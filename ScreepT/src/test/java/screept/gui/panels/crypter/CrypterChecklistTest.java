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
package screept.gui.panels.crypter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import screept.crypter.engine.CryptoEngine;
import screept.crypter.engine.CryptoKey;
import screept.crypter.engine.CryptoProfile;
import screept.crypter.engine.Identity;
import screept.crypter.impl.manager.CryptoMode;

/** The "what is needed" lines tell exactly what is missing, by mode and by the selected files. */
public class CrypterChecklistTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static CrypterChecklist.State state(CryptoMode m, File... sources) {
		CrypterChecklist.State s = new CrypterChecklist.State();
		s.mode = m;
		s.profile = m.profile;
		s.sources = sources;
		return s;
	}

	@Test
	public void encryptLineByMode() throws Exception {
		CrypterChecklist c = new CrypterChecklist();
		File f = tmp.newFile("a.txt");
		CrypterChecklist.State s = state(CryptoMode.SECURE_256);
		assertTrue(c.encryptLine(s).contains("add files"));
		assertTrue(c.encryptLine(s).contains("write a password"));
		assertTrue(c.encryptLine(s).contains("key image not needed"));
		s = state(CryptoMode.SECURE_256, f);
		s.passwordEntered = true;
		assertFalse(c.encryptLine(s).contains("✗"));

		s = state(CryptoMode.SECURE_KEYIMAGE, f);
		s.passwordEntered = true;
		assertTrue(c.encryptLine(s).contains("choose the key image"));
		s.keyImage = tmp.newFile("k.png");
		assertTrue(c.encryptLine(s).contains("key image k.png"));

		s = state(CryptoMode.SECURE_256, f);
		s.passwordEntered = true;
		s.hideInPicture = true;
		assertTrue(c.encryptLine(s).contains("choose the picture"));
		s.picture = tmp.newFile("cat.png");
		s.pictureCapacity = 210 * 1024;
		assertTrue(c.encryptLine(s).contains("hidden in cat.png (holds 210 KB)"));

		s = state(CryptoMode.SECURE_PUBLIC, f);
		assertTrue(c.encryptLine(s).contains("create your keys first"));
		s.haveKeys = true;
		s.people = 2;
		assertTrue(c.encryptLine(s).contains("for you + 2 people"));
		assertTrue(c.encryptLine(s).contains("password not needed"));

		assertTrue(c.encryptLine(state(CryptoMode.ONLY_AES, f)).contains("Legacy modes only decrypt"));
	}

	@Test(timeout = 60000)
	public void decryptLineReadsTheFileHeaders() throws Exception {
		CrypterChecklist c = new CrypterChecklist();
		File plain = new File(tmp.getRoot(), "note.txt.scpt");
		Files.write(plain.toPath(), CryptoEngine.encrypt("x".getBytes("UTF-8"), "pw-12345".toCharArray(), CryptoProfile.standard()));
		File keyImg = keyImage();
		File withImage = new File(tmp.getRoot(), "photo.jpg.scpt");
		Files.write(withImage.toPath(), CryptoEngine.encrypt("x".getBytes("UTF-8"), CryptoKey.of("pw-12345".toCharArray(), keyImg),
				CryptoProfile.withKeyImage()));
		File forPeople = new File(tmp.getRoot(), "contract.pdf.scpt");
		Files.write(forPeople.toPath(), CryptoEngine.encrypt("x".getBytes("UTF-8"),
				CryptoKey.of(new char[0]).withRecipients(Arrays.asList(Identity.generate().publicKey())), CryptoProfile.forRecipients()));

		CrypterChecklist.State s = state(CryptoMode.SECURE_256, plain);
		assertTrue(c.decryptLine(s).contains("write the password"));
		assertTrue(c.decryptLine(s).contains("key image not needed"));

		s = state(CryptoMode.SECURE_256, plain, withImage);
		s.passwordEntered = true;
		assertTrue(c.decryptLine(s).contains("key image needed by photo.jpg.scpt"));
		s.keyImage = keyImg;
		assertFalse(c.decryptLine(s).contains("✗"));

		s = state(CryptoMode.SECURE_256, forPeople);
		assertTrue(c.decryptLine(s).contains("create or restore it"));
		s.haveKeys = true;
		assertTrue(c.decryptLine(s).contains("write the password of your private key"));

		File other = tmp.newFile("old.txt");
		assertTrue(c.decryptLine(state(CryptoMode.SECURE_256, other)).contains("select their Legacy mode"));
		assertTrue(c.decryptLine(state(CryptoMode.ONLY_STEG, other)).contains("key image needed by old.txt"));
	}

	private File keyImage() throws Exception {
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(20, 20, java.awt.image.BufferedImage.TYPE_INT_RGB);
		img.setRGB(3, 3, 0x123456);
		File f = new File(tmp.getRoot(), "key.png");
		javax.imageio.ImageIO.write(img, "png", f);
		return f;
	}
}
