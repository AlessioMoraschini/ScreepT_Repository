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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import screept.crypter.engine.FileCrypter.OutputMode;
import screept.crypter.impl.manager.CryptoMode;
import screept.crypter.impl.manager.ScreeptManagerFileOrFolder;
import screept.crypter.om.CrypterResultDTO;

/**
 * The same matrix as {@link LayerMatrixRoundTripTest}, through the facade the Crypter window calls (what its Creept and
 * DeCreept buttons run): custom profile, every output mode, file and folder. Decryption is started with a mode that
 * does not match (legacy AES) on purpose: the facade must recognise the files by itself.
 */
public class FacadeLayerMatrixTest {

	private static final char[] PSW = "facade-matrix-password-longer-than-16".toCharArray();

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private File folder() throws IOException {
		File root = new File(tmp.newFolder(), "panel dir");
		LayerMatrixRoundTripTest.write(new File(root, "a.txt"), 900, 1);
		LayerMatrixRoundTripTest.write(new File(root, "empty.bin"), 0, 2);
		LayerMatrixRoundTripTest.write(new File(root, "sub/b.dat"), 4000, 3);
		return root;
	}

	@Test
	public void everyCombinationThroughTheFacadeRecognisesAndRestoresFileAndFolder() throws Exception {
		File keyImage = LayerMatrixRoundTripTest.keyImage(tmp.newFolder());
		File dir = folder();
		File file = LayerMatrixRoundTripTest.write(new File(tmp.newFolder(), "single.bin"), 5000, 9);
		int done = 0;
		for (CryptoProfile profile : LayerMatrixRoundTripTest.allProfiles()) {
			for (File source : new File[] { file, dir }) {
				for (OutputMode mode : OutputMode.values()) {
					String where = profile.describe() + " / " + source.getName() + " / " + mode;
					ScreeptManagerFileOrFolder m = new ScreeptManagerFileOrFolder(null, null, false);
					m.setProfileOverride(profile);
					m.setOutputMode(mode);
					File image = profile.needsKeyImage() ? keyImage : null;

					File encDir = tmp.newFolder();
					CrypterResultDTO r = m.crYpt(CryptoMode.SECURE_256, source, encDir, PSW.clone(), image, false);
					assertTrue(where + ": " + m.getOutputStats(), r.resultOK);
					File[] produced = encDir.listFiles();
					assertEquals(where, 1, produced.length);

					File decDir = tmp.newFolder();
					ScreeptManagerFileOrFolder back = new ScreeptManagerFileOrFolder(null, null, false);
					r = back.decrYpt(CryptoMode.ONLY_AES, produced[0], decDir, PSW.clone(), image, false);
					assertTrue(where + ": " + back.getOutputStats(), r.resultOK);
					File restored = new File(decDir, source.getName());
					LayerMatrixRoundTripTest.assertSameTree(source, restored, mode == OutputMode.ZIP_OF_ENCRYPTED_FILES);
					done++;
				}
			}
		}
		assertEquals((Boolean.getBoolean("crypter.fullMatrix") ? 81 : 18) * 2 * 3, done);
	}
}
