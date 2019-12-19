package om;

public class AesOption {

	// DEFAULT VALUES (usfeul to keep valid object during iniLoad, in case of null read or exception)
	public final static boolean DEFAULT_FLAG_AES = true;
	public final static boolean DEF_FLAG_HASHKEY = true;
	
	
	// FIELDS	
	private boolean flagAes;  // flag che indica se � attiva la fase di codifica/decodifica AES
	private boolean hashKey;
	
	// constructors //
	
	public AesOption() {
		flagAes = DEFAULT_FLAG_AES;
		hashKey = DEF_FLAG_HASHKEY;
	}
	
	public AesOption(boolean AES) {
		this();
		flagAes = AES;
	}
	
	// getters and setters //
	
	public boolean isHashKey() {
		return hashKey;
	}

	public void setHashKey(boolean hashKey) {
		this.hashKey = hashKey;
	}

	public boolean isFlagAes() {
		return flagAes;
	}

	public void setFlagAes(boolean flagAes) {
		this.flagAes = flagAes;
	}

}
