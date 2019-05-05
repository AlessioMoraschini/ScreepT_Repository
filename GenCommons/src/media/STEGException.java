package media;

public class STEGException extends Exception{
	
	public STEGException() {
		super();
	}
	
	@Override
	public String getMessage() {
		return "Error in the STEG coder object, please be sure password is not null and obj initialized before cript/decript!";
	}
	
}
