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
	
	private String ID;
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
