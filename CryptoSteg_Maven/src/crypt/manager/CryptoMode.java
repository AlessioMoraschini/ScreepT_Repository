package crypt.manager;

/**
 * this enum is created to represent the different modalities in which
 * the screept crypter can work with.
 * 
 * @author Alessio Moraschini
 *
 */
public enum CryptoMode {COMPLETE("AS", true, true),
						ONLY_AES("A", true, false),
						ONLY_STEG("S", false, true);
	
	private String ID;
	boolean AES;
	
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

	boolean STEG;
	
	private CryptoMode(String id, boolean aes, boolean steg ) {
		ID = id;
		AES = aes;
		STEG = steg;
	}
}
