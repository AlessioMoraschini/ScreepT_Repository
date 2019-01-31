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
import java.util.ArrayList;
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
import crypt.steg.StegOmResult;
import gUI.utils.progressDialog.TestUpdateProgress;
import iNIT.INItializer;
import iNIT.om.AesOption;
import uTILS.CustomFileComparator;
import uTILS.FileStuff;
import uTILS.HandleString;
import uTILS.ImageWorker;

/**
 * 
 * @author Alessio Moraschini, Dalmine, 2018-2019
 * 
 * This class is created to merge functionalities of aes EncrytDecrypt and ImageCodeDecode,
 * leaving the choice to implementer to use different modes that consists in the different combination
 * of usage among the two coders/decoders.
 * 
 * Needs the two classes, and also other custom stuff from subdependencies
 *
 */
public class ScreeptManagerStub {

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(ScreeptManagerStub.class);
	
	// temp file path - default value after initializing with constructor
	private final String TEMP_PATH = "Resources_Scrypt/temp/";
	private String RESULT_PATH = TEMP_PATH;
	
	// temp crypted/decrypted file name
	private static String CRYPT_NAME = "temp_crypter_out";
	private static String DECRYPT_NAME = "temp_decrypter_out";
	
	// this is the size of the buffer used for file IO to avoid memory leak
	private static int AES_BUFFER_SIZE = 1024*1024; // default - 1 MB of buffer

	//////////////////////
	//      FIELDS      //
	//////////////////////
	
	private ImageCodeDecode imgCoder;	
	
	private EncryptDecrypt aesCoder;
	
	private CryptoMode mode;
	
	private int w;
	private int h;
	
	private byte[] buffer;
	
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
	public ScreeptManagerStub() throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException {
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
	public ScreeptManagerStub(ImageCodeDecode imgCod, EncryptDecrypt aesCod) {
		mode = CryptoMode.COMPLETE;
		aesCoder = aesCod;
		imgCoder = imgCod;
	}	
	
	/**
	 *  Constructor for only AES Crypto-conversion
	 *  
	 * @param aesCod the initialized aesCoder
	 */
	public ScreeptManagerStub(EncryptDecrypt aesCod) {
		mode = CryptoMode.ONLY_AES;
		aesCoder = aesCod;
		imgCoder = null;
	}
		
	/**
	 *  Constructor for only image Crypto-Conversion
	 *  
	 * @param imgCod the initialized imageCoder
	 */
	public ScreeptManagerStub(ImageCodeDecode imgCod) {
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
	public Boolean crypt(File srcFile) {
		
		buffer = new byte[AES_BUFFER_SIZE];
		
		wellDone = false;
		
		logger.debug("Started cripting with selected mode: "+mode);
		
		emptyTempPath() ;
			
		// initialize method according to selected mode:
		
		try {
			// CASE 1 - ONLY AES
			if(mode.AES && !mode.STEG) {

				logger.debug("AES mode detected!");
				
				onlyAESCrypt(srcFile);
				wellDone = true;
				
				logger.debug("AES Crypt phase - EXITING");
				
			// CASE 2 - ONLY STEG
			}else if(mode.STEG && !mode.AES) {
				logger.debug("STEG mode detected!");

				// create container folder for result coded images with same name of source file
				RESULT_PATH = TEMP_PATH+"STEGcod_"+srcFile.getName()+"/";
				File outFolder = new File(RESULT_PATH);
				outFolder.mkdir();
				
				w = this.imgCoder.getKeyImage().getWidth();
				h = this.imgCoder.getKeyImage().getHeight();
				
				onlySTEGCrypt(srcFile);
				wellDone = true;
				
				logger.debug("STEG Crypt phase - EXITING");
				
			// CASE 3 - COMPLETE MODE
			}else if (mode.STEG && mode.AES) {

				logger.debug("COMPLETE mode detected!");

				// create container folder for result coded images with same name of source file
				RESULT_PATH = TEMP_PATH+"COMPLETEcod_"+srcFile.getName()+"/";
				File outFolder = new File(RESULT_PATH);
				outFolder.mkdir();
				
				w = this.imgCoder.getKeyImage().getWidth();
				h = this.imgCoder.getKeyImage().getHeight();
				
				completeCrypt(srcFile);
				wellDone = true;
				
				logger.debug("COMPLETE Crypt phase - EXITING");
				
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
	
	/**
	 * this method is used to decrypt source file(s) according to the mode setted in the corresponding field.
	 * you can set the mode either with constructor or with setter method 
	 * (remember to define also correct coders instances in that case).
	 * 
	 * @param srcFile this is the source file to be cripted. In case of multiple files it uses 
	 * incremental names found in the same folder
	 * @param currentConf this is the configuration instance used to get current gui style
	 * 
	 */
	public Boolean deCrypt(File srcFile) {
		
		buffer = new byte[AES_BUFFER_SIZE];
		
		wellDone = false;
		
		logger.debug("Started decription with selected mode: "+mode);
		
		emptyTempPath() ;
		
		// initialize method according to selected mode:
		
		try {
			// CASE 1 - ONLY AES
			if(mode.AES && !mode.STEG) {
				

				logger.debug("AES mode detected!");
				
				onlyAESDeCrypt(srcFile);
				wellDone = true;
				
				logger.debug("AES Decrypt phase - EXITING");
				
			// CASE 2 - ONLY STEG
			}else if(mode.STEG && !mode.AES) {
				logger.debug("STEG mode detected!");

				w = this.imgCoder.getKeyImage().getWidth();
				h = this.imgCoder.getKeyImage().getHeight();
				
				onlySTEGDeCrypt(srcFile);
				wellDone = true;
				
				logger.debug("STEG Decrypt phase - EXITING");
				
			// CASE 3 - COMPLETE MODE
			}else if (mode.STEG && mode.AES) {
				
				w = this.imgCoder.getKeyImage().getWidth();
				h = this.imgCoder.getKeyImage().getHeight();
				
				logger.debug("AES mode detected!");
				
				//completeDeCrypt(srcFile);
				wellDone = true;
				
				logger.debug("AES Decrypt phase - EXITING");
				
			}	
		}catch(Exception e) {
			resetAfterException(e);
		}
		
		// TODO scrivi il metodo sulla falsa riga del codificatore: 
		// adatta a struttura file e usa decrypt corrispondenti alla modalità
		
		return wellDone;
	}
	
	// Function that set values before to throw exception
	private void resetAfterException(Exception e) {
		logger.error(Arrays.toString(e.getStackTrace()));
		wellDone = false;
	}
	
	// Function that delete the content of the temporaneous folder.
	private void emptyTempPath(){
		// initialize the temp directory reference
		File temp_folder = new File(TEMP_PATH);
		
		if(temp_folder.exists() && temp_folder.listFiles().length!=0) {
			
			// empty temp folder content files before to start encription
			boolean deleted = FileStuff.deleteDirContentRecursive(temp_folder);
			if(deleted) {
				logger.debug("Temp dir ("+TEMP_PATH+") succesfully emptied :)");
			}else {
				logger.error("Some error occurred deleting "+TEMP_PATH+" content :(");
			}
		}else {
			logger.warn("Temp dir already empty or not existing");
		}
	}
	
	// Function that implements encryption of source file into another crypted file with same extension.
	private void onlyAESCrypt(File srcFile) throws IOException, AESException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
		
		FileInputStream streamIN = new FileInputStream(srcFile);
		int available = streamIN.available();
		if (AES_BUFFER_SIZE >= available) {
			buffer = new byte[available];
		}
		
		// this integer will contain value -1 when file is ended 
		int readFlag = streamIN.read(buffer);
		
		CRYPT_NAME = srcFile.getName();
		File result = new File(TEMP_PATH+"AesCrypted_"+CRYPT_NAME);
		
		// create file if it does not exist
		result.createNewFile();
		
		// initialize writer with append flag set to true, so it don't overwrite at every cycle of buffer read
		FileOutputStream outWriter = new FileOutputStream(result, true);
		
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
			outWriter.flush();
			// load next bufferSize bytes from srcFile
			available = streamIN.available();
			if (AES_BUFFER_SIZE >= available) {
				buffer = new byte[available];
			}
			readFlag = streamIN.read(buffer);

		}		
		
		// close streams to avoid memory leaks
		streamIN.close();
		outWriter.close();
		logger.debug("AES crypt phase ended: "+result.getName()+" created :)");
		
	}
	
	// Function that implements aes decryption of source file into another decrypted file with same extension.
	private void onlyAESDeCrypt(File srcFile) throws IOException, AESException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
		
		FileInputStream streamIN = new FileInputStream(srcFile);
		int available = streamIN.available();
		if (AES_BUFFER_SIZE >= available) {
			buffer = new byte[available];
		}
		
		// this integer will contain value -1 when file is ended 
		int readFlag = streamIN.read(buffer);
		
		DECRYPT_NAME = srcFile.getName();
		File result = new File(TEMP_PATH+"AesDeCrypted_"+DECRYPT_NAME);
		
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
			byte[] partialResult = aesCoder.decrypt(buffer);				
			outWriter.write(partialResult);
			
			// load next bufferSize bytes from srcFile
			available = streamIN.available();
			if (AES_BUFFER_SIZE >= available) {
				buffer = new byte[available];
			}
			readFlag = streamIN.read(buffer);
			
		}		
		// close streams to avoid memory leaks
		streamIN.close();
		outWriter.close();
		
		logger.debug("AES decrypt phase ended: "+result.getName()+" created :)");
		
	}
	
	// Function that implements encryption of source file into several .png images without using pre-AES coding.
	// the images are written in the temp folder with incremental name, each one of the same size of the key image.
	private void onlySTEGCrypt(File srcFile) throws IOException, STEGException {

		// initialize buffer to store one source's byte for every available px of keyImg
		FileInputStream streamIN = new FileInputStream(srcFile);
		int STEG_BUFFER_SIZE = w*h;
		
		// check availabe bytes from source to avoid filling to buffer_size with null values
		// set buffer at 1 byte per px of image key if <= than available
		int available = streamIN.available();
		if (STEG_BUFFER_SIZE >= available) {
			buffer = new byte[available];
		}else {
			buffer = new byte[STEG_BUFFER_SIZE];
		}
		
		// this integer will contain value -1 when file is ended 
		int readFlag = streamIN.read(buffer);
		
		// STEG error types
		if(this.imgCoder == null || this.imgCoder.getKeyImage()==null) {
			
			// close streams to avoid memory leaks
			streamIN.close();
			logger.error("Error retrieving the STEG coder -> null coder or keyImage!");
			throw new STEGException();
		}

		int n = 1;
		File outFile;
		while(readFlag > 0) {
			
			// this value will be used during decription to recognize where px value have to be ignored
			// (cause img is of fixed dimension)
			
			// crypt current buffer into image
			StegOmResult result = imgCoder.stegaCrypt(buffer);
			BufferedImage imgResult = result.getImgRes();
			
			// sequence of info stored in name is the following:
			// [path of src dir] + [progressive file number] + [bytes of src stegged] + [src ext] + [type] + [outName] .png 
			outFile = new File(RESULT_PATH+n+"_"+result.getnBytesCoded()+"_.png");

			boolean OK = ImageIO.write(imgResult, "png", outFile);
							
			
			available = streamIN.available();
			if (STEG_BUFFER_SIZE >= available) {
				buffer = new byte[available];
			}
			readFlag = streamIN.read(buffer);
			
			if(OK) {
				logger.debug("Image written: "+outFile.getAbsolutePath());	
			}else {
				streamIN.close();
				logger.error("Error ocurred writing "+outFile.getAbsolutePath());
				throw new IOException();
			}
			n++;
			
		}
		// close streams to avoid memory leaks
		streamIN.close();
					
	}
	
	// Function that implements decryption of source file into several .png images without using pre-AES coding.
	// the images are written in the temp folder with incremental name, each one of the same size of the key image.
	private void onlySTEGDeCrypt(File srcFile) throws IOException, STEGException {

		// second element of folder result name is name of original file
		DECRYPT_NAME = HandleString.retrieveFromNelements(srcFile.getName(), "_",1);
		
		// this time outfile is one and source can be multiple files incremental from 1st srcFile
		File outFile = new File(TEMP_PATH+"StegDeCrypted_"+DECRYPT_NAME);		
		
		// create out Stream setting append mode (do not overwrite every cycle)
		FileOutputStream fout = new FileOutputStream(outFile, true);
		
		// STEG error types
		if(this.imgCoder == null || this.imgCoder.getKeyImage()==null) {
			
			// close streams to avoid memory leaks
			fout.close();
			logger.error("Error retrieving the STEG decoder -> null coder or keyImage!");
			throw new STEGException();
		}

		// TODO get file array and scan all files till the end and sort them by first element in name name
		File[] cryptedImgList = srcFile.listFiles();
		// order files in source folder using first element(see STEGcrypter) with "_" separator
		Arrays.sort(cryptedImgList, new CustomFileComparator("_", 0));
		
		for(File iFile : cryptedImgList) {
			
			// retrieve number of coded bytes into image from file name - the second element in name
			int bytesToGet = Integer.valueOf(iFile.getName().split("_")[1]);
			
			// retrieve current image from file
			BufferedImage codedImg = ImageIO.read(iFile);
			
			fout.write(imgCoder.stegaDeCryptToArray(codedImg, bytesToGet));

		}
		
		// close streams to avoid memory leaks
		fout.close();
					
	}
	
	
	// Function that implements encryption of source file into several .png images using pre-AES coding.
	// the images are written in the temp folder with incremental name, each one of the same size of the key image.
	// this is method for top security
	private void completeCrypt(File srcFile) throws IOException, AESException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, STEGException {
		
		FileInputStream streamIN = new FileInputStream(srcFile);
		int STEG_BUFFER_SIZE = w*h;
		
		// set buffer at 1 byte per px of image key if <= than available
		int available = streamIN.available();
		if (STEG_BUFFER_SIZE >= available) {
			buffer = new byte[available];
		}else {
			buffer = new byte[STEG_BUFFER_SIZE];
		}	
		
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
		
		File outFile = new File(RESULT_PATH+CRYPT_NAME+"_"+FilenameUtils.getExtension(srcFile.getName())+"_ScreepTed.png");
		
		int i = 1;
		
		while(readFlag > 0) {
			
			String nBytesStegged;
			
			// encrypt (AES) current buffer content with aes key string
			byte[] partialResult = aesCoder.encrypt(buffer);				
			
			// encrypt (STEG) current partial aes-coded result into image using key image
			StegOmResult result = imgCoder.stegaCrypt(partialResult);
			BufferedImage imgResult = result.getImgRes();
			
			outFile = new File(result.getnBytesCoded()+"_"+outFile.getName());
			
			// write i-image on the file and then calculate new incremental name for next one
			boolean OK = ImageIO.write(imgResult, "png", outFile);
			
			// after first image is created create a new incremental-named file for next image until source byte buffer is ended
			outFile = FileStuff.renameJavaObjFile(outFile);
			
			available = streamIN.available();
			if (STEG_BUFFER_SIZE >= available) {
				buffer = new byte[available];
			}
			readFlag = streamIN.read(buffer);
			
			if(OK) {
				logger.debug("Image written: "+outFile.getAbsolutePath());	
			}else {
				streamIN.close();
				logger.error("Error ocurred writing "+outFile.getAbsolutePath());
				throw new IOException();	
			}
			
			i++;
		}		
		// close streams to avoid memory leaks
		streamIN.close();

	}
	
	/**
	 * This method will grant that the buffer created with w*h will contain all aes coded bytes
	 * In other words this make sure number of elements in a 2-Dimensional array of W*H elements.
	 * 
	 * It updates parameters W and H, making the n° of pixels multiple of K
	 * 
	 * @param W positive number of rows
	 * @param H positive number of columns
	 * @param K positive integer divisor to grant [(W*H)%K==0]
	 * 
	 * @return new value of W.
	 */
	static public int grantAreaMultipleOfN(Integer W, Integer H, int N) {

		if(W==0 || H==0) {
			return 0;
		}
		if(W<N || W*H<N) {
			return 0;
		}
		// Make W or H multiple of 16 => now if I multiply a number to it, i've chosen Width.
		// The result it's still multiple of 16. the method chooses the nearest number
		
		W = (W/N)*N;
		
		return W;
		
// - V2 -	W*H has to be multiple of 16		
//
//			Integer tempRes = W*H;
//			if(tempRes%16 != 0) {
//			tempRes = Math.round(tempRes/16)*16;
//			// now tempRes divides 16, so also 8
//			// now sub-divide into h and w => W*H= (tempRes^2)/W = 
//			W = tempRes/4;
//			H = tempRes/W;
//		}
	
	}
	
	/**
	 * same of the area version, but with only one parameter number to check
	 * @param number
	 * @param N
	 * @return
	 */
	static public int grantMultipleOfN(Integer number, int N) {
		if(number<N) {
			return 0;
		}
		number = (number/N)*N;
		return number;
	}
	
	/**
	 * similar to grantAreaMultipleOfN buf this one extract sub-image from source
	 * granting that resultPixels number is multiple of given N (divisor)
	 * @param key this is the source image
	 * @param N this is the divisor to which N°pixels must be multiple of
	 * @return sub-image: the extracted image with [new width <= original width] and N°px%divisor = 0
	 */
	static public BufferedImage subImgMultOfNpx(BufferedImage key, int N) {
		int newWidth = grantAreaMultipleOfN(key.getWidth(), key.getHeight(), N);
		BufferedImage mult16IMG = ImageWorker.getPortion(key, newWidth, key.getHeight());
		return mult16IMG;
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
	public String getTempResultPath() {
		return RESULT_PATH;
	}
	
	/**
	 * This method is used to set the source/destination path to which decrypt or encrypt source file(s)
	 * @param resPath
	 */
	public void setTempResultPath(String resPath) {
		RESULT_PATH = resPath;
	}

	public static int getAES_BUFFER_SIZE() {
		return AES_BUFFER_SIZE;
	}

	/**
	 * @param bUFFER_SIZE integer representing size of buffer - maximum 2 GB 
	 */
	public static void setAES_BUFFER_SIZE(int bUFFER_SIZE) {
		AES_BUFFER_SIZE = bUFFER_SIZE;
	}

	/**
	 * @return CRYTPT_NAME this is the name of the default outFile without extension
	 */
	public static String getCryptName() {
		return CRYPT_NAME;
	}	
}
