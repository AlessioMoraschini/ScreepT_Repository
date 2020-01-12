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
package crypt.manager;

/**
 * this enum is created to represent the different modalities in which
 * the screept crypter can work with.
 * 
 * @author Alessio Moraschini
 *
 */
public enum CryptoMode {COMPLETE("AS", true, true, true),
						ONLY_AES("A", true, false, false),
						ONLY_STEG("S", false, true, false);
	
	public String ID;
	public boolean AES;
	public boolean STEG;
	public boolean COMPL;
	
	// private constructor
	private CryptoMode(String id, boolean aes, boolean steg, boolean comp) {
		ID = id;
		AES = aes;
		STEG = steg;
		COMPL = comp;
	}
	
	public static CryptoMode getModeFromBoolean(boolean aes, boolean steg) {
		if(aes && !steg) {
			return ONLY_AES;
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
