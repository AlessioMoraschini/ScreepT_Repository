package crypt.manager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.log4j.Logger;

import crypt.aes.AESException;
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
	
	// temp file path
	private final static String RESULT_PATH = "Resources_Scrypt/temp/temp_result";
	
	// this is the size of the buffer used for file IO to avoid memory leak
	private static int BUFFER_SIZE = 1024*1024*50; // default - 50 MB of buffer

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
		imgCoder = imgCod;
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
		imgCoder = imgCod;
	}
	
	//////////////////////
	//      METHODS     //
	//////////////////////
	
	//TODO here goes method that recognize the mode and apply conversion (one method for string and one method for files)
	//     this method also has to avoid outOfMemory errors, remember to use Buffers! 
	//     NB a method for each combination of types/mode
	
	/**
	 * this method is used to crypt source file according to the mode setted in the corresponding field.
	 * 
	 * @param srcFile this is the source file to be cripted
	 * @return temp file that wraps an image (if the mode is complete/steg) or normal file(only aes mode)
	 * 
	 * @throws IOException 
	 * @throws AESException 
	 * @throws IllegalBlockSizeException 
	 * @throws BadPaddingException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchAlgorithmException 
	 * @throws NoSuchPaddingException 
	 * @throws InvalidKeyException 
	 */
	public File crypt(File srcFile) throws IOException, AESException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
		// initialize the wrapper result file container
		File result = new File(RESULT_PATH);
		
		if(result.exists()) {
			
			// if the file exists empty it
			FileWriter writer = new FileWriter(result, false);
			writer.write("");
			writer.close();
	
		}else {
		
			// if it does not exist create a new empty file
			result.createNewFile();
		
		}
		
		FileInputStream streamIN = new FileInputStream(srcFile);
		byte[] buffer = new byte[BUFFER_SIZE];
		
		// initialize writer with append flag set to true, so it don't overwrite at every cycle of buffer read
		FileOutputStream outWriter = new FileOutputStream(RESULT_PATH, true);
		
		// this integer will contain value -1 when file is ended 
		int readFlag = streamIN.read(buffer);
		
		// initialize var according to selected mode
		if(mode.AES && !mode.STEG) {

			// AES error types
			if(this.aesCoder == null || this.aesCoder.getPassword().equals("")) {

				// close streams to avoid memory leaks
				streamIN.close();
				outWriter.close();	
				
				throw new AESException();
			}
			
			// IF ONLY AES
			while(readFlag > 0) {
				
				// encrypt current buffer content and write it to temp out file
				byte[] partialResult = aesCoder.encrypt(buffer);				
				outWriter.write(partialResult);
				
				// load next bufferSize bytes from srcFile
				readFlag = streamIN.read(buffer);
			}		
			
		}else if(mode.STEG && !mode.AES) {
			
			// IF ONLY STEG
			while(readFlag > 0) {
						
				readFlag = streamIN.read(buffer);
			
			}
		}else if (mode.STEG && mode.AES) {
			
			// IF COMPLETE MODE
			while(readFlag > 0) {
				
				readFlag = streamIN.read(buffer);
			}
		}
		
		// close streams to avoid memory leaks
		streamIN.close();
		outWriter.close();		
		
		return result;
	}
	
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

	public static String getTempResultPath() {
		return RESULT_PATH;
	}

	public static int getBUFFER_SIZE() {
		return BUFFER_SIZE;
	}

	public static void setBUFFER_SIZE(int bUFFER_SIZE) {
		BUFFER_SIZE = bUFFER_SIZE;
	}	
	
}
