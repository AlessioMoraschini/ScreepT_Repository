package iNIT.om;

public class AesOption {

	// DEFAULT VALUES (usfeul to keep valid object during iniLoad, in case of null read or exception)
	public static final String DEFAULT_AES_KEY = "ST3G4N0cr1p74009"; //steganocrypt(2018+1991)
	
	// FIELDS	
	private String aesKey;
	
	public AesOption() {
		
	}
	
	public AesOption(String aesKEY) {
		aesKey = aesKEY;
	}

	public String getAesKey() {
		return aesKey;
	}

	public void setAesKey(String aesKey) {
		this.aesKey = aesKey;
	}
	
	
	
}
