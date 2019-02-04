package iNIT.om;

public class GenericOption {

	// DEFAULT VALUES (usfeul to keep valid object during iniLoad, in case of null read or exception)
	public final String DEFAULT_USERNAME = "";
	public final String DEFAULT_CHARSET = "UTF-8";
	public final boolean DEFAULT_FLAG_AES = true;
	public final boolean DEFAULT_FLAG_STEG = true;

	// FILEDS
	private String userName;
	private String charset;
	private boolean flagAes;  // flag che indica se � attiva la fase di codifica/decodifica AES
	private boolean flagSteg; // flag che indica se � attiva la fase di codifica/decodifica Steganografica
	
	public GenericOption() {
		
	}
	
	public GenericOption(String user, String chars, boolean AES, boolean STEG) {
		userName = user;
		charset = chars;
		flagAes = AES;
		flagSteg = STEG;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public boolean isFlagAes() {
		return flagAes;
	}

	public void setFlagAes(boolean flagAes) {
		this.flagAes = flagAes;
	}

	public boolean isFlagSteg() {
		return flagSteg;
	}

	public void setFlagSteg(boolean flagSteg) {
		this.flagSteg = flagSteg;
	}
	
	
}
