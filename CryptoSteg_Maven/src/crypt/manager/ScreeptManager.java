package crypt.manager;

import org.apache.log4j.Logger;

import crypt.aes.EncryptDecrypt;
import crypt.steg.ImageCodeDecode;
import gUI.panels.KeyGenPanel;

/**
 * 
 * @author Alessio Moraschini, Dalmine, 2018
 * 
 * This class is created to merge functionalities of aes EncrytDecrypt and ImageCodeDecode,
 * leaving the choice to implementer to use different modes that consists in the different combination
 * of usage among the two coders/decoders.
 * 
 * Needs the two classes, and also other custom stuff from subdependencies
 *
 */
public class ScreeptManager {

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(ScreeptManager.class); 
	
	//////////////////////
	//      FIELDS      //
	//////////////////////
	
	private ImageCodeDecode imgCoder;	
	
	private EncryptDecrypt aesCoder;
	
	private CryptoMode mode;
	
	//////////////////////
	//    CONSTRUCTOR   //
	//////////////////////
	
	public ScreeptManager(ImageCodeDecode imgCod, EncryptDecrypt aesCod) {
		mode = CryptoMode.COMPLETE;
		aesCoder = aesCod;
		imgCoder = imgCoder;
	}	
	
	// 
	public ScreeptManager(EncryptDecrypt aesCod) {
		mode = CryptoMode.ONLY_AES;
		aesCoder = aesCod;
		imgCoder = null;
	}
		
	public ScreeptManager(ImageCodeDecode imgCod) {
		mode = CryptoMode.ONLY_STEG;
		aesCoder = null;
		imgCoder = imgCoder;
	}
	
	//////////////////////
	//      METHODS     //
	//////////////////////
	
	//////////////////////
	// GETTERS & SETTERS /
	//////////////////////


	public ImageCodeDecode getImgCoder() {
		return imgCoder;
	}

	public void setImgCoder(ImageCodeDecode imgCoder) {
		this.imgCoder = imgCoder;
	}

	public EncryptDecrypt getAesCoder() {
		return aesCoder;
	}

	public void setAesCoder(EncryptDecrypt aesCoder) {
		this.aesCoder = aesCoder;
	}

	public CryptoMode getMode() {
		return mode;
	}

	public void setMode(CryptoMode mode) {
		this.mode = mode;
	}	
	
}
