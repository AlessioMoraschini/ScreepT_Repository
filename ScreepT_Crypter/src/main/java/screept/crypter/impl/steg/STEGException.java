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
package screept.crypter.impl.steg;

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
