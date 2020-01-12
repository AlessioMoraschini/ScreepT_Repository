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
package crypt.aes;

public class AESException extends Exception{
	
	public AESException() {
		super();
	}
	
	@Override
	public String getMessage() {
		return "Error in the AES coder object, please be sure password is not null!";
	}
	
}
