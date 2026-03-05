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
package screept.crypter.impl.aes;

public class AESException extends Exception{
	private static final long serialVersionUID = -6838200282561901212L;

	public AESException() {
		super();
	}
	
	public AESException(String msg) {
		super(msg);
	}
	
	@Override
	public String getMessage() {
		return "Error in the AES coder object, please be sure password is not null!";
	}
	
}
