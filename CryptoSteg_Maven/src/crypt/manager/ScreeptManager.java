package crypt.manager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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
import gUI.utils.progressDialog.TestUpdateProgress;
import iNIT.INItializer;
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
	
	private int w;
	private int h;
	
	private byte[] buffer;
	
	private volatile TestUpdateProgress progUpdater;
	
	boolean wellDone = false;
		
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
	public Boolean crypt(File srcFile, INItializer currentConf) {
		
    	progUpdater = new TestUpdateProgress(currentConf);
		progUpdater.getPb().setValue(0);
		progUpdater.getLabel().setText("Starting ecnryption...");
		
		wellDone = false;
		
		logger.debug("Started cripting with selected mode: "+mode);
		
		w = this.imgCoder.getKeyImage().getWidth();
		h = this.imgCoder.getKeyImage().getHeight();
		
		emptyTempPath() ;
			
		// initialize method according to selected mode:
		
		try {
			// CASE 1 - ONLY AES
			if(mode.AES && !mode.STEG) {
				
				progUpdater.getPb().setValue(5);
				progUpdater.getLabel().setText("AES mode detected!");

				logger.debug("AES mode detected!");
				
				onlyAESCrypt(srcFile);
				wellDone = true;
				
				logger.debug("AES crypt phase ended :)");
				
			// CASE 2 - ONLY STEG
			}else if(mode.STEG && !mode.AES) {
				
				progUpdater.getPb().setValue(5);
				progUpdater.getLabel().setText("STEG mode detected!");
				
				logger.debug("STEG mode detected!");

				onlySTEGCrypt(srcFile);
				wellDone = true;
				
				logger.debug("STEG crypt phase ended :)");
				
			// CASE 3 - COMPLETE MODE
			}else if (mode.STEG && mode.AES) {
				
				progUpdater.getPb().setValue(5);
				progUpdater.getLabel().setText("COMPLETE mode detected!");

				logger.debug("AES mode detected!");
				
				completeCrypt(srcFile);
				wellDone = true;
				
				logger.debug("AES crypt phase ended :)");
				
			}	
		}catch(STEGException e) {
			resetAfterException(e);
		} catch (InvalidKeyException e) {
			resetAfterException(e);
		} catch (NoSuchPaddingException e) {
			resetAfterException(e);
		} catch (NoSuchAlgorithmException e) {
			resetAfterException(e);
		} catch (InvalidAlgorithmParameterException e) {
			resetAfterException(e);
		} catch (BadPaddingException e) {
			resetAfterException(e);
		} catch (IllegalBlockSizeException e) {
			resetAfterException(e);
		} catch (IOException e) {
			resetAfterException(e);
		} catch (AESException e) {
			resetAfterException(e);
		}
			
		logger.debug("Streams closed, exiting method.");
		
		return wellDone;
	}
	
	// Function that set values before to throw exception
	private void resetAfterException(Exception e) {
		progUpdater.getPb().setValue(100);
		progUpdater.getLabel().setText("OOOPS! An error occurred :(");
		logger.error(Arrays.toString(e.getStackTrace()));
		wellDone = false;
	}
	
	// Function that delete the content of the temporaneous folder.
	private void emptyTempPath(){
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
	}
	
	// Function that implements encryption of source file into another crypted file with same extension.
	private void onlyAESCrypt(File srcFile) throws IOException, AESException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
		
		FileInputStream streamIN = new FileInputStream(srcFile);
		buffer = new byte[BUFFER_SIZE];
		
		// this integer will contain value -1 when file is ended 
		int readFlag = streamIN.read(buffer);
		
		File result = new File(RESULT_PATH+CRYPT_NAME+"_AesCrypted."+FilenameUtils.getExtension(srcFile.getName()));
		
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
		
		int i = 1;
		
		progUpdater.getLabel().setText("Writing "+result.getName());
		
		while(readFlag > 0) {
			
			// encrypt current buffer content and write it to temp out file
			byte[] partialResult = aesCoder.encrypt(buffer);				
			outWriter.write(partialResult);
			
			// load next bufferSize bytes from srcFile
			readFlag = streamIN.read(buffer);
			
			progUpdater.getPb().setValue(Math.round((float)buffer.length/(float)srcFile.length())*i*100);
			
			i++;
		}		
		// close streams to avoid memory leaks
		streamIN.close();
		outWriter.close();
		progUpdater.getPb().setValue(100);
		progUpdater.getLabel().setText("Encryption completed! ");
		
		logger.debug("AES crypt phase ended: "+result.getName()+" created :)");
		
	}
	
	// Function that implements encryption of source file into several .png images without using pre-AES coding.
	// the images are written in the temp folder with incremental name, each one of the same size of the key image.
	private void onlySTEGCrypt(File srcFile) throws IOException, STEGException {

		// set buffer at 1 byte per px of image key
		buffer = new byte[w*h];
		
		FileInputStream streamIN = new FileInputStream(srcFile);
		
		// this integer will contain value -1 when file is ended 
		int readFlag = streamIN.read(buffer);
		
		// STEG error types
		if(this.imgCoder == null || this.imgCoder.getKeyImage()==null) {
			
			// close streams to avoid memory leaks
			streamIN.close();
			logger.error("Error retrieving the STEG coder -> null coder or keyImage!");
			throw new STEGException();
		}

		int i = 1;
		
		while(readFlag > 0) {
					
			// cypt current buffer into image
			BufferedImage imgResult = imgCoder.stegaCrypt(buffer);
			
			// first time overwrite no index.
			// and use png extension 
			File outFile = new File(RESULT_PATH+CRYPT_NAME+"_"+FilenameUtils.getExtension(srcFile.getName())+"_StegCrypted.png");
			progUpdater.getLabel().setText("Writing "+outFile.getName());
			
			boolean OK = ImageIO.write(imgResult, "png", outFile);
							
			// after first image is created create a new incremental-named file for next image until source byte buffer is ended
			FileStuff.renameJavaObjFile(outFile);
			
			readFlag = streamIN.read(buffer);
			
			if(OK) {
				logger.debug("Image written: "+outFile.getAbsolutePath());	
			}else {
				streamIN.close();
				logger.error("Error ocurred writing "+outFile.getAbsolutePath());
				throw new IOException();
			}
			
			progUpdater.getPb().setValue(Math.round((float)buffer.length/(float)srcFile.length())*i*100);
			i++;
		}
		// close streams to avoid memory leaks
		streamIN.close();
		progUpdater.getPb().setValue(100);
		progUpdater.getLabel().setText("Encryption completed! ");
					
	}
	
	
	// Function that implements encryption of source file into several .png images using pre-AES coding.
	// the images are written in the temp folder with incremental name, each one of the same size of the key image.
	// this is method for top security
	private void completeCrypt(File srcFile) throws IOException, AESException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, STEGException {
		
		FileInputStream streamIN = new FileInputStream(srcFile);

		// set buffer at 1 byte per px of image key
		buffer = new byte[w*h];		
		
		// this integer will contain value -1 when file is ended 
		int readFlag = streamIN.read(buffer);
		
		// AES error types
		if(this.aesCoder == null || this.aesCoder.getPassword().equals("")) {

			// close streams to avoid memory leaks
			streamIN.close();
			logger.error("Error retrieving the AES coder -> null or void passsword!");
			throw new AESException();
		}
		
		// STEG error types
		if(this.imgCoder == null || this.imgCoder.getKeyImage()==null) {
			
			// close streams to avoid memory leaks
			streamIN.close();
			logger.error("Error retrieving the STEG coder -> null coder or keyImage!");
			throw new STEGException();
		}
		
		int i = 1;
		
		while(readFlag > 0) {
			
			File outFile = new File(RESULT_PATH+CRYPT_NAME+"_"+FilenameUtils.getExtension(srcFile.getName())+"_ScreepTed.png");
			progUpdater.getLabel().setText("Writing "+outFile.getName());

			// encrypt (AES) current buffer content with aes key string
			byte[] partialResult = aesCoder.encrypt(buffer);				
			
			// encrypt (STEG) current partial aes-coded result into image using key image
			BufferedImage imgResult = imgCoder.stegaCrypt(partialResult);
			
			// write i-image on the file and then calculate new incremental name for next one
			boolean OK = ImageIO.write(imgResult, "png", outFile);
			
			// after first image is created create a new incremental-named file for next image until source byte buffer is ended
			FileStuff.renameJavaObjFile(outFile);
			
			readFlag = streamIN.read(buffer);
			
			if(OK) {
				logger.debug("Image written: "+outFile.getAbsolutePath());	
			}else {
				streamIN.close();
				logger.error("Error ocurred writing "+outFile.getAbsolutePath());
				throw new IOException();	
			}
			
			progUpdater.getPb().setValue(Math.round((float)buffer.length/(float)srcFile.length())*i*100);
			i++;
		}		
		// close streams to avoid memory leaks
		streamIN.close();
		progUpdater.getPb().setValue(100);
		progUpdater.getLabel().setText("Encryption completed! ");

	}
	
	//////////////////////
	// GETTERS & SETTERS /
	//////////////////////


	public ImageCodeDecode getImgCoder() {
		return imgCoder;
	}

	/**
	 * set the Coder used for STEG ecryption/decryption
	 * @return
	 */
	public void setImgCoder(ImageCodeDecode imgCoder) {
		this.imgCoder = imgCoder;
	}

	public EncryptDecrypt getAesCoder() {
		return aesCoder;
	}
	
	/**
	 * set the Coder used for AES ecryption/decryption
	 * @return
	 */
	public void setAesCoder(EncryptDecrypt aesCoder) {
		this.aesCoder = aesCoder;
	}

	public CryptoMode getMode() {
		return mode;
	}

	/**
	 * Set the modality to use -> NB remember to set also the corresponding coders 
	 * when you use this method or you'll occur into an Exception when ecnrypting/decrypting
	 * @param mode
	 */
	public void setMode(CryptoMode mode) {
		this.mode = mode;
	}

	/**
	 * this is the name of the folder containing temp results
	 * @return
	 */
	public static String getTempResultPath() {
		return RESULT_PATH;
	}

	public static int getBUFFER_SIZE() {
		return BUFFER_SIZE;
	}

	/**
	 * @param bUFFER_SIZE integer representing size of buffer - maximum 2 GB 
	 */
	public static void setBUFFER_SIZE(int bUFFER_SIZE) {
		BUFFER_SIZE = bUFFER_SIZE;
	}

	/**
	 * @return CRYTPT_NAME this is the name of the default outFile without extension
	 */
	public static String getCryptName() {
		return CRYPT_NAME;
	}	
}
