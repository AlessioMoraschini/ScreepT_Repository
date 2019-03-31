package iNIT.om;

public class GenericOption {

	// DEFAULT VALUES (usfeul to keep valid object during iniLoad, in case of null read or exception)
	public final static String DEFAULT_USERNAME = "";
	public final static String DEFAULT_CHARSET = "UTF-8";

	// FILEDS
	private String userName;
	private String charset;
	
	public GenericOption() {
		
	}
	
	public GenericOption(String user, String chars, boolean ultraSec) {
		userName = user;
		charset = chars;
		
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

}
