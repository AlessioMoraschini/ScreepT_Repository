package crypt.manager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import crypt.aes.AESException;
import crypt.aes.EncryptDecrypt;
import crypt.steg.ImageCodeDecode;
import crypt.steg.STEGException;
import iNIT.om.AesOption;
import uTILS.FileStuff;

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
	private final static String RESULT_PATH = "Resources_Scrypt/temp/";
	
	// temp crypted file name
	private final static String CRYPT_NAME = "temp_crypt_out";
	
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
	
	/**
	 * Default Constructor - AES Crypto-Conversion with default psw
	 * 
	 * @throws NoSuchAlgorithmException 
	 * @throws NoSuchPaddingException 
	 * @throws UnsupportedEncodingException 
	 * 
	 */
	public ScreeptManager() throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException {
		mode = CryptoMode.COMPLETE;
		aesCoder = new EncryptDecrypt(AesOption.DEFAULT_AES_KEY);
		imgCoder = null;
	}
	
	/**
	 * Constructor for complete mode Crypto-Conversion
	 * 
	 * @param imgCod the initialized imageCoder
	 * @param aesCod the initialized aesCoder
	 */
	public ScreeptManager(ImageCodeDecode imgCod, File keyImgFile, EncryptDecrypt aesCod) {
		mode = CryptoMode.COMPLETE;
		aesCoder = aesCod;
		imgCoder = imgCod;
	}	
	
	/**
	 *  Constructor for only AES Crypto-conversion
	 *  
	 * @param aesCod the initialized aesCoder
	 */
	public ScreeptManager(EncryptDecrypt aesCod) {
		mode = CryptoMode.ONLY_AES;
		aesCoder = aesCod;
		imgCoder = null;
	}
		
	/**
	 *  Constructor for only image Crypto-Conversion
	 *  
	 * @param imgCod the initialized imageCoder
	 */
	public ScreeptManager(ImageCodeDecode imgCod, File keyImgFile) {
		mode = CryptoMode.ONLY_STEG;
		aesCoder = null;
		imgCoder = imgCod;
	}
	
	//////////////////////
	//      METHODS     //
	//////////////////////
	
	// here goes method that recognize the mode and apply conversion (one method for string and one method for files)
	// this method also has to avoid outOfMemory errors, remember to use Buffers! 
	// NB a method for each combination of types/mode
	
	/**
	 * this method is used to crypt source file according to the mode setted in the corresponding field.
	 * 
	 * @param srcFile this is the source file to be cripted
	 * @return temp directory path, with out files that wrap an image (if the mode is complete/steg) or normal file(only aes mode)
	 * 
	 * @throws IOException 
	 * @throws AESException 
	 * @throws IllegalBlockSizeException 
	 * @throws BadPaddingException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchAlgorithmException 
	 * @throws NoSuchPaddingException 
	 * @throws InvalidKeyException 
	 * @throws STEGException 
	 */
	public String crypt(File srcFile) throws IOException, AESException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, STEGException {
		
		logger.debug("Started cripting with selected mode: "+mode);
		
		// initialize the temp directory reference
		File temp_folder = new File(RESULT_PATH);
		
		if(temp_folder.exists()) {
			
			// empty temp folder content files before to start encription
			boolean deleted = FileStuff.deleteDirContent(temp_folder);
			if(deleted) {
				logger.debug("Temp dir ("+RESULT_PATH+") succesfully emptied :)");
			}else {
				logger.error("Some error occurred deleting "+RESULT_PATH+" content :(");
			}
		}
		
		FileInputStream streamIN = new FileInputStream(srcFile);
		byte[] buffer = new byte[BUFFER_SIZE];
			
		// initialize var according to selected mode:
		
		// CASE 1 - ONLY AES
		if(mode.AES && !mode.STEG) {

			logger.debug("AES mode detected!");
			
			// this integer will contain value -1 when file is ended 
			int readFlag = streamIN.read(buffer);
			
			File result = new File(RESULT_PATH+CRYPT_NAME+"_aesCrypted."+FilenameUtils.getExtension(srcFile.getName()));
			
			// create file if it does not exist
			result.createNewFile();
			
			// initialize writer with append flag set to true, so it don't overwrite at every cycle of buffer read
			FileOutputStream outWriter = new FileOutputStream(result.getAbsolutePath(), true);
			
			// AES error types
			if(this.aesCoder == null || this.aesCoder.getPassword().equals("")) {

				// close streams to avoid memory leaks
				streamIN.close();
				outWriter.close();	
				logger.error("Error retrieving the AES coder -> null or void passsword!");

				throw new AESException();
			}
			
			while(readFlag > 0) {
				
				// encrypt current buffer content and write it to temp out file
				byte[] partialResult = aesCoder.encrypt(buffer);				
				outWriter.write(partialResult);
				
				// load next bufferSize bytes from srcFile
				readFlag = streamIN.read(buffer);
			}		
			
			outWriter.close();
			logger.debug("AES crypt phase ended: "+result.getName()+" created :)");
			
		// CASE 2 - ONLY STEG
		}else if(mode.STEG && !mode.AES) {
			
			logger.debug("STEG mode detected!");

			int w = this.imgCoder.getKeyImage().getWidth();
			int h = this.imgCoder.getKeyImage().getHeight();
			
			// set buffer at 1 byte per px of image key
			buffer = new byte[w*h];
			
			// this integer will contain value -1 when file is ended 
			int readFlag = streamIN.read(buffer);
			
			// STEG error types
			if(this.imgCoder == null || this.imgCoder.getKeyImage()==null) {
				
				// close streams to avoid memory leaks
				streamIN.close();
				logger.error("Error retrieving the STEG coder -> null coder or keyImage!");
				
				throw new STEGException();
			}

			while(readFlag > 0) {
						
				// cypt current buffer into image
				BufferedImage imgResult = imgCoder.stegaCrypt(buffer);
				
				// first time overwrite no index.
				// and use png extension 
				File outFile = new File(RESULT_PATH+CRYPT_NAME+".png");
				
				ImageIO.write(imgResult, "png", outFile);
								
				// after first image is created create a new incremental-named file for next image until source byte buffer is ended
				FileStuff.renameJavaObjFile(outFile);
				
				readFlag = streamIN.read(buffer);
			
			}
		
			logger.debug("STEG crypt phase ended :)");
			
		// CASE 3 - COMPLETE MODE
		}else if (mode.STEG && mode.AES) {
			
			// this integer will contain value -1 when file is ended 
			int readFlag = streamIN.read(buffer);
			
			while(readFlag > 0) {
				
				readFlag = streamIN.read(buffer);
			}
		}
		
		// close streams to avoid memory leaks
		streamIN.close();		
		
		logger.debug("Streams closed, exiting method.");
		
		return RESULT_PATH;
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

	public static String getCryptName() {
		return CRYPT_NAME;
	}
	
}
