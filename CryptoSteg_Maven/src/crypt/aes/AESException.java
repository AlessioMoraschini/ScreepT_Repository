package crypt.aes;

public class AESException extends Exception{
	
	public AESException() {
		super();
	}
	
	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return "Error in the AES coder object, please be sure password is not null!";
	}
	
}
