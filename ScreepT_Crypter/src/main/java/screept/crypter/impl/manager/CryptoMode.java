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
package screept.crypter.impl.manager;

import screept.crypter.engine.CryptoProfile;

/**
 * this enum is created to represent the different modalities in which
 * the screept crypter can work with.
 * 
 * @author Alessio Moraschini
 *
 */
public enum CryptoMode {SECURE_256("G2", true, false, false, CryptoProfile.standard(), "Secure: AES-256-GCM (recommended)"),
						SECURE_128("G1", true, false, false, CryptoProfile.aes128(), "Secure: AES-128-GCM"),
						SECURE_MAX("GM", true, false, false, CryptoProfile.maximum(), "Maximum: AES-128 + AES-256, scrypt"),
						SECURE_KEYIMAGE("GK", true, true, false, CryptoProfile.withKeyImage(), "Secure + key image (StegaCREEPT)"),
						CUSTOM("GX", true, false, false, CryptoProfile.standard(), "Custom (choose layers)..."),
						COMPLETE("AS", true, true, true, null, "Legacy: AES + key image"),
						ONLY_AES("A", true, false, false, null, "Legacy: AES-128 (CBC)"),
						ONLY_STEG("S", false, true, false, null, "Legacy: key image only");

	public String ID;
	public boolean AES;
	public boolean STEG;
	public boolean COMPL;
	/** Not null for the modes handled by the authenticated engine (password only, no key image). */
	public final CryptoProfile profile;
	private final String label;

	// private constructor
	private CryptoMode(String id, boolean aes, boolean steg, boolean comp, CryptoProfile profile, String label) {
		ID = id;
		AES = aes;
		STEG = steg;
		COMPL = comp;
		this.profile = profile;
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}

	public static CryptoMode getModeFromBoolean(boolean aes, boolean steg) {
		if(aes && !steg) {
			return SECURE_256; // new default: authenticated engine (legacy AES stays selectable)
		}else if(!aes && steg) {
			return ONLY_STEG;
		}else {
			return COMPLETE;
		}
	}

	// GETTER AND SETTERS
	
	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public boolean isAES() {
		return AES;
	}

	public void setAES(boolean aES) {
		AES = aES;
	}

	public boolean isSTEG() {
		return STEG;
	}

	public void setSTEG(boolean sTEG) {
		STEG = sTEG;
	}

	public boolean isCOMPL() {
		return COMPL;
	}

	public void setCOMPL(boolean cOMPL) {
		COMPL = cOMPL;
	}
	
}
