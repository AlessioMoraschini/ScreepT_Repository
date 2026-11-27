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
package screept.gui.panels.crypter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import screept.crypter.engine.Identity;
import screept.crypter.impl.manager.CryptoMode;

/** The key store behind "Keys...": own key, people, selection; never the real folder of the user. */
public class KeysDialogStoreTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Before
	public void useTemporaryFolder() {
		System.setProperty(KeysDialog.FOLDER_PROPERTY, tmp.getRoot().getAbsolutePath());
	}

	@After
	public void restore() {
		System.clearProperty(KeysDialog.FOLDER_PROPERTY);
	}

	@Test(timeout = 60000)
	public void recipientsAreMeAndTheTickedPeople() throws Exception {
		Identity me = Identity.generate(), anna = Identity.generate(), bob = Identity.generate();
		me.save(KeysDialog.identityFile(), "my-password".toCharArray());
		Properties p = KeysDialog.loadStore();
		p.setProperty("my.public", me.publicKeyText());
		p.setProperty("key.Anna", anna.publicKeyText());
		p.setProperty("use.Anna", "true");
		p.setProperty("key.Bob = strange: name", bob.publicKeyText());
		p.setProperty("use.Bob = strange: name", "false");
		KeysDialog.saveStore(p);

		assertEquals(2, KeysDialog.contacts(KeysDialog.loadStore()).size());
		assertEquals(1, KeysDialog.selectedCount());
		List<byte[]> r = KeysDialog.recipients(null);
		assertEquals(2, r.size());
		assertArrayEquals(me.publicKey(), r.get(0));
		assertArrayEquals(anna.publicKey(), r.get(1));
		assertTrue(KeysDialog.identityFile().getParentFile().equals(tmp.getRoot()));
	}

	@Test
	public void legacyModesOnlyDecrypt() {
		for (CryptoMode m : CryptoMode.values()) {
			if (m.profile != null) {
				assertFalse(m + " refused", KeysDialog.refuseLegacyEncryption(null, m));
			}
		}
		assertFalse(KeysDialog.refuseLegacyEncryption(null, null));
	}
}
