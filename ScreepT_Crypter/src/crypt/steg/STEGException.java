package crypt.steg;

public class STEGException extends Exception{
	private static final long serialVersionUID = 3426033056967540223L;

	public STEGException() {
		super();
	}

	public STEGException(String msg) {
		super(msg);
	}
	
	@Override
	public String getMessage() {
		return "Error in the STEG coder object, please be sure password is not null and obj initialized before cript/decript!";
	}
	
}
