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

import java.util.ArrayList;
import java.util.List;

import screept.crypter.engine.CipherAlgorithm;
import screept.crypter.engine.CipherLayer;
import screept.crypter.engine.CryptoProfile;
import screept.crypter.engine.FileCrypter;
import screept.crypter.engine.KdfAlgorithm;

/** The choices of the "Custom protection" dialog: which layers, which key derivation, how folders are exported. */
public final class CrypterSettings {

	public List<CipherLayer> layers = new ArrayList<CipherLayer>(java.util.Arrays.asList(CipherAlgorithm.AES_256_GCM));
	public KdfAlgorithm kdf = KdfAlgorithm.PBKDF2_SHA512;
	public FileCrypter.OutputMode outputMode = FileCrypter.OutputMode.SINGLE_ENCRYPTED_ARCHIVE;

	/** @throws IllegalArgumentException if the combination is not valid (for example no authenticated layer) */
	public CryptoProfile buildProfile() {
		return new CryptoProfile(kdf, kdf.defaultCost, layers);
	}

	/** Starts from a ready made mode, so that "Options..." shows (and refines) what is selected in the list. */
	public void loadFrom(CryptoProfile profile) {
		layers = new ArrayList<CipherLayer>(profile.getLayers());
		kdf = profile.kdf;
	}

	/** Layer ids as written in the INI, for example "2,3". */
	public String layersToString() {
		StringBuilder sb = new StringBuilder();
		for (CipherLayer l : layers) {
			sb.append(sb.length() == 0 ? "" : ",").append(l.id());
		}
		return sb.toString();
	}

	/** Restores saved choices; anything missing, unknown or invalid leaves the defaults untouched. */
	public void load(String layerIds, String kdfName, String folderName) {
		try {
			List<CipherLayer> loaded = new ArrayList<CipherLayer>();
			for (String id : layerIds.split(",")) {
				if (!id.trim().isEmpty()) {
					loaded.add(CipherAlgorithm.fromId(Integer.parseInt(id.trim())));
				}
			}
			KdfAlgorithm k = kdfName.isEmpty() ? kdf : KdfAlgorithm.valueOf(kdfName);
			FileCrypter.OutputMode o = folderName.isEmpty() ? outputMode : FileCrypter.OutputMode.valueOf(folderName);
			new CryptoProfile(k, k.defaultCost, loaded); // validates the combination
			layers = loaded;
			kdf = k;
			outputMode = o;
		} catch (Exception e) {
			// saved values not usable: keep the defaults
		}
	}

	public static String outputModeLabel(FileCrypter.OutputMode m) {
		switch (m) {
		case PER_FILE_TREE:
			return "File by file (keeps the folder structure)";
		case ZIP_OF_ENCRYPTED_FILES:
			return "Zip of encrypted files";
		default:
			return "One encrypted archive (hides names too)";
		}
	}

	public static String kdfLabel(KdfAlgorithm k) {
		return k == KdfAlgorithm.SCRYPT ? k.label + " - 128 MB of RAM while deriving" : k.label + " - " + k.defaultCost + " iterations";
	}
}
