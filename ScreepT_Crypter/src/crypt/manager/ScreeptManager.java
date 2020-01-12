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
package crypt.manager;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;
import javax.swing.text.BadLocationException;

import crypt.aes.AESException;
import crypt.aes.AesCipher;
import crypt.steg.StegCipher;
import crypt.steg.StegOmResult;
import files.CustomFileComparator;
import crypt.steg.STEGException;
import util.ImageEncodingUtils;
import util.ScreeptUtils;
import util.UpdateProgressPanelCrypter;
import utility.SafeLogger;

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
public class ScreeptManager {

	private static SafeLogger logger;
	static {
		logger = new SafeLogger(ScreeptManager.class);
	}
	
	private static String prefixFromConfig = "";
	static {
		try {
			Field field = Class.forName("resources.GeneralConfig").getField("RESOURCES_DIR");
			field.setAccessible(true);
			String tempPrefix = (String) field.get(null);
			prefixFromConfig = tempPrefix;
		} catch (Throwable throwable) {
			prefixFromConfig = "";
		}
	}
	
	public String TEMP_PATH = prefixFromConfig + "temp/";
	
	// temp file path - default value after initializing with constructor
	private String OUT_PATH = TEMP_PATH;
	private String RESULT_PATH = TEMP_PATH;
	
	// temp crypted/decrypted file name
	private static String CRYPT_NAME = "temp_crypter_out";
	private static String DECRYPT_NAME = "temp_decrypter_out";
	
	// NB AES_BUFFER SIZE MUST BE THE SAME FOR ENCRIPTION AND DECRIPTION!
	// this is the size of the buffer used for file IO to avoid memory leak
	private static int AES_BUFFER_SIZE = grantMultipleOfN(1024*1024, 16); // default - 1 MB of buffer - have to multiple of 16
	
	//////////////////////
	//      FIELDS      //
	//////////////////////
	
	private StegCipher imgCoder;	
	
	private AesCipher aesCoder;
	
	private CryptoMode mode;
	
	private boolean shuffleResult = false;
	
	private int w;
	private int h;
	
	private byte[] buffer;
	
	private volatile UpdateProgressPanelCrypter progUpdater;
	
	boolean wellDone = false;
	
	// use this to keep memory of written files
	public ArrayList<String> writtenPathsList = new ArrayList<String>();
	
	private String tempMSG="";
		
	// list to keep memory of open streams in case of error
	private List<Closeable> closeables = new Vector<Closeable>();
	
	// reference to keep memory of outFile/dir (in case of steg delete all out dir representing crypted single file) in case of error
	public File currentOutFile;
	
	public boolean hashed = true;
	
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
		aesCoder = new AesCipher(("").toCharArray());
		imgCoder = null;
		TEMP_PATH = prefixFromConfig + "temp/";
	}
	
	/**
	 * Constructor for complete mode Crypto-Conversion
	 * 
	 * @param imgCod the initialized imageCoder
	 * @param aesCod the initialized aesCoder
	 */
	public ScreeptManager(StegCipher imgCod, AesCipher aesCod) {
		mode = CryptoMode.COMPLETE;
		aesCoder = aesCod;
		imgCoder = imgCod;
		TEMP_PATH = prefixFromConfig + "temp/";
	}	
	
	/**
	 *  Constructor for only AES Crypto-conversion
	 *  
	 * @param aesCod the initialized aesCoder
	 */
	public ScreeptManager(AesCipher aesCod) {
		mode = CryptoMode.ONLY_AES;
		aesCoder = aesCod;
		imgCoder = null;
		TEMP_PATH = prefixFromConfig + "temp/";
	}
		
	/**
	 *  Constructor for only image Crypto-Conversion
	 *  
	 * @param imgCod the initialized imageCoder
	 */
	public ScreeptManager(StegCipher imgCod) {
		mode = CryptoMode.ONLY_STEG;
		aesCoder = null;
		imgCoder = imgCod;
		TEMP_PATH = prefixFromConfig + "temp/";
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
	public Boolean crypt(File srcFile, UpdateProgressPanelCrypter progressBarGeneral) throws Exception, InterruptedException{

		buffer = new byte[AES_BUFFER_SIZE];
		
		progUpdater = (progressBarGeneral != null) ? progressBarGeneral : new UpdateProgressPanelCrypter(false);
		progUpdater.getPb().setValue(0);
		progUpdater.getLabel().setText("Starting encryption...");

		wellDone = true;
		
		tempMSG += "Start encripting "+srcFile.getAbsolutePath()+" ...\n[Selected mode: "+mode+" ]\n";
		tempMSG += "[ Out folder: "+OUT_PATH+" ]\n";

		logger.debug("Started Crypt with selected mode: "+mode);

		if(OUT_PATH.equals(TEMP_PATH)) {
			emptyTempPath() ;
		}
		// initialize method according to selected mode:
		
		try {
			// CASE 1 - ONLY AES
			if(mode.AES && !mode.STEG) {
				
				progUpdater.getPb().setValue(5);
				progUpdater.getLabel().setText("AES mode detected!");

				logger.debug("AES mode detected!");

				onlyAESCrypt(srcFile);
				
				progUpdater.getPb().setValue(100);
				logger.debug("AES Crypt phase - EXITING");
				
			// CASE 2 - ONLY STEG
			}else if(mode.STEG && !mode.AES) {

				// create container folder for result coded images with same name of source file
				RESULT_PATH = OUT_PATH+srcFile.getName()+"/";
				File outFolder = ScreeptUtils.renameJavaObjFile(new File(RESULT_PATH));
				outFolder.mkdir();
				RESULT_PATH = outFolder.getCanonicalPath()+"/";
				
				// update reference to currentOutFile/folder
				currentOutFile = outFolder;
				
				w = this.imgCoder.getKeyImage().getWidth();
				h = this.imgCoder.getKeyImage().getHeight();
				
				progUpdater.getPb().setValue(5);
				progUpdater.getLabel().setText("STEG mode detected!");
				
				tempMSG += "\n => Using folder [ "+outFolder.getAbsolutePath()+" ] to contain crypted Images!\n";
				logger.debug("STEG mode detected!");

				onlySTEGCrypt(srcFile);
				
				progUpdater.getPb().setValue(100);
				logger.debug("STEG Crypt phase - EXITING");
				
			// CASE 3 - COMPLETE MODE
			}else if (mode.STEG && mode.AES) {

				// create container folder for result coded images with same name of source file
				RESULT_PATH = OUT_PATH+srcFile.getName()+"/";
				File outFolder = ScreeptUtils.renameJavaObjFile(new File(RESULT_PATH));
				outFolder.mkdir();
				RESULT_PATH = outFolder.getCanonicalPath()+"/";
				
				// update reference to currentOutFile/folder
				currentOutFile = outFolder;

				w = this.imgCoder.getKeyImage().getWidth();
				h = this.imgCoder.getKeyImage().getHeight();
				
				progUpdater.getPb().setValue(5);
				progUpdater.getLabel().setText("COMPLETE mode detected!");

				tempMSG += "\n => Using folder [ "+outFolder.getAbsolutePath()+" ] to contain crypted Images!\n";
				logger.debug("COMPLETE mode detected!");
				
				completeCrypt(srcFile);
				
				progUpdater.getPb().setValue(100);
				logger.debug("COMPLETE Crypt phase - EXITING");
				
			}	
		}catch(Exception e) {
			resetAfterException(e);
			throw e;
		}
			
		logger.debug("Streams closed, exiting encription.");
		tempMSG += "\n => Streams closed, exiting encription.";
		
		return wellDone;
	}
	
	/**
	 * this method is used to decrypt source file(s) according to the mode setted in the corresponding field.
	 * you can set the mode either with constructor or with setter method 
	 * (remember to define also correct coders instances in that case).
	 * 
	 * @param srcFile this is the source file to be cripted. In case of multiple files it uses 
	 * incremental names found in the same folder
	 * 
	 */
	public Boolean deCrypt(File srcFile, UpdateProgressPanelCrypter progressBarGeneral) throws Exception, InterruptedException {
		
		progUpdater = progressBarGeneral;
		
		// aes source arrays were multiple of 16, so result array each time is 32 byte more cause of aes padding structure and random IV size
		// to avoid errors i've to include that bytes during the read plus the IV size
		buffer = new byte[AES_BUFFER_SIZE+32];
		
		progUpdater = (progressBarGeneral != null) ? progressBarGeneral : new UpdateProgressPanelCrypter(false);
		progUpdater.getPb().setValue(0);
		progUpdater.getLabel().setText("Starting Decryption...");
		
		wellDone = true;
		
		tempMSG += "Start decripting "+srcFile.getAbsolutePath()+" ...\nSelected mode: [ "+mode+" ]\n\n";
		tempMSG += "[ Out folder: "+OUT_PATH+" ]\n";
		logger.debug("Started Decrypting with selected mode: "+mode);
		
		if(OUT_PATH.equals(TEMP_PATH)) {
			emptyTempPath() ;
		}
		
		// initialize method according to selected mode:
		
		try {
			// CASE 1 - ONLY AES
			if(mode.AES && !mode.STEG) {
				
				progUpdater.getPb().setValue(5);
				progUpdater.getLabel().setText("AES mode detected!");

				logger.debug("AES mode detected!");
				
				onlyAESDeCrypt(srcFile);
				
				progUpdater.getPb().setValue(100);
				logger.debug("AES Decrypt phase - EXITING");
				
			// CASE 2 - ONLY STEG
			}else if(mode.STEG && !mode.AES) {
				logger.debug("STEG mode detected!");

				w = this.imgCoder.getKeyImage().getWidth();
				h = this.imgCoder.getKeyImage().getHeight();
				
				progUpdater.getPb().setValue(5);
				progUpdater.getLabel().setText("STEG mode detected!");
				
				logger.debug("STEG mode detected!");

				onlySTEGDeCrypt(srcFile);
				
				progUpdater.getPb().setValue(100);
				logger.debug("STEG Decrypt phase - EXITING");
				
			// CASE 3 - COMPLETE MODE
			}else if (mode.STEG && mode.AES) {

				w = this.imgCoder.getKeyImage().getWidth();
				h = this.imgCoder.getKeyImage().getHeight();
				
				progUpdater.getPb().setValue(5);
				progUpdater.getLabel().setText("COMPLETE mode detected!");

				logger.debug("COMPLETE mode detected!");
				
				completeDeCrypt(srcFile);
				
				progUpdater.getPb().setValue(100);
				logger.debug("COMPLETE Decrypt phase - EXITING");
				
			}	
		}catch(Exception e) {
			resetAfterException(e);
			throw e;
		}
		
		logger.debug("Streams closed, exiting decription.");
		tempMSG += "\n => Streams closed, exiting decription.";

		return wellDone;
	}
	
	// Function that set values before to throw exception
	private void resetAfterException(Exception e) {
		
		String msg = "\n => OOOPS! An error occurred :(";
		progUpdater.getPb().setValue(100);
		progUpdater.forceClose();
		logger.error(msg, e);
		logger.debug("Streams closed, exiting encription.");
		tempMSG += msg;
		tempMSG += "\n => Streams closed after error or interruption, exiting coding process.";
		wellDone = false;
		killOpenStreams();
	}
	
	// Function that delete the content of the temporaneous folder.
	private void emptyTempPath(){
		// initialize the temp directory reference
		File temp_folder = new File(OUT_PATH);
		String msg = "";
		if(temp_folder.exists() && temp_folder.listFiles().length!=0) {
			
			// empty temp folder content files before to start encription
			boolean deleted = ScreeptUtils.deleteDirContentRecursive(temp_folder, logger);
			if(deleted) {
				msg ="\n => Temp dir ("+OUT_PATH+") succesfully emptied :)";
				logger.debug(msg);
				tempMSG += msg;
				
			}else {
				msg = "\n => Some error occurred deleting "+OUT_PATH+" content :(";
				logger.error(msg);
				tempMSG += msg;
			}
		}else {
			msg = "\n => Temp dir already empty or not existing";
			logger.warn(msg);
			tempMSG += msg;
		}
	}
	
	// Function that implements encryption of source file into another crypted file with same extension.
	private void onlyAESCrypt(File srcFile) throws 
			IOException, AESException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, 
			InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, BadLocationException, InterruptedException {
		
		if(progUpdater == null) progUpdater = new UpdateProgressPanelCrypter(false);
		progUpdater.getPb().setValue(0);
		progUpdater.getLabel().setText("Encripting "+srcFile.getName());
		
		FileInputStream streamIN = new FileInputStream(srcFile);
//		int available = streamIN.available();
		
		// calculate available avoiding overflow
		long progN = 0;
		long available = getAvailable(srcFile, progN);
		if (AES_BUFFER_SIZE >= available) {
			buffer = new byte[(int)available];
		}
		
		// this integer will contain value -1 when file is ended 
		int readFlag = streamIN.read(buffer);
		
		CRYPT_NAME = srcFile.getName();
		File result = ScreeptUtils.renameJavaObjFile(new File(OUT_PATH+CRYPT_NAME));
		
		// update reference to currentOutFile/folder
		currentOutFile = result;
		
		// create file if it does not exist
		result.createNewFile();
		
		// initialize writer with append flag set to true, so it don't overwrite at every cycle of buffer read
		FileOutputStream outWriter = new FileOutputStream(result, true);
		
		closeables.add(streamIN);
		closeables.add(outWriter);
		
		// AES error types
		if(this.aesCoder == null || this.aesCoder.getPassword().length == 0) {

			// close streams to avoid memory leaks
			streamIN.close();
			outWriter.close();	
			logger.error("Error retrieving the AES coder -> null or void passsword!");
			throw new AESException();
		}

		int i=1;
		
		while(readFlag > 0) {
			
			checkInterruptedProgress();
			
			// encrypt current buffer content and write it to temp out file
			byte[] partialResult = aesCoder.encrypt(buffer, hashed, shuffleResult);	
			outWriter.write(partialResult);
			outWriter.flush();
			// load next bufferSize bytes from srcFile
			//available = streamIN.available(); 
			
			progN += buffer.length; 
			available = getAvailable(srcFile, progN);
			
			if (AES_BUFFER_SIZE >= available) {
				buffer = new byte[(int)available];
			}
			readFlag = streamIN.read(buffer);
			
			progUpdater.getPb().setValue((int)(((float)buffer.length/(float)srcFile.length())*i*100));
			
			i++;
		}		
		
		// close streams to avoid memory leaks
		streamIN.close();
		outWriter.close();
		progUpdater.getPb().setValue(100);
		progUpdater.getLabel().setText("Encryption completed! ");
		
		String msg = "AES crypt phase ended: Output file => "+result.getName()+"\n";
		tempMSG += msg;
		logger.info(msg);
		
	}
	
	// Function that implements aes decryption of source file into another decrypted file with same extension.
	private void onlyAESDeCrypt(File srcFile) throws 
			IOException, AESException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, 
			InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, BadLocationException, InterruptedException {
		
		if(progUpdater == null) progUpdater = new UpdateProgressPanelCrypter(false);
		progUpdater.getPb().setValue(0);
		progUpdater.getLabel().setText("Decripting "+srcFile.getName());
		
		FileInputStream streamIN = new FileInputStream(srcFile);
		//int available = streamIN.available();
		
		long progN = 0;
		long available = getAvailable(srcFile, progN);
		if (AES_BUFFER_SIZE >= available) {
			buffer = new byte[(int)available];
		}
		
		// this integer will contain value -1 when file is ended 
		int readFlag = streamIN.read(buffer);
		
		DECRYPT_NAME = srcFile.getName();
		File result = ScreeptUtils.renameJavaObjFile(new File(OUT_PATH+DECRYPT_NAME));
		
		// update reference to currentOutFile/folder
		currentOutFile = result;
				
		// create file if it does not exist
		result.createNewFile();
		
		// initialize writer with append flag set to true, so it don't overwrite at every cycle of buffer read
		FileOutputStream outWriter = new FileOutputStream(result.getAbsolutePath(), true);
		
		closeables.add(streamIN);
		closeables.add(outWriter);
		
		// AES error types
		if(this.aesCoder == null || this.aesCoder.getPassword().length == 0) {

			// close streams to avoid memory leaks
			streamIN.close();
			outWriter.close();	
			logger.error("Error retrieving the AES coder -> null or void passsword!");
			throw new AESException();
		}

		int i =1;
		
		while(readFlag > 0) {
			
			checkInterruptedProgress();
			
			// encrypt current buffer content and write it to temp out file
			byte[] partialResult = aesCoder.decrypt(buffer);				
			outWriter.write(partialResult);
			
			// load next bufferSize bytes from srcFile
			// available = streamIN.available();
			
			progN += buffer.length; 
			available = getAvailable(srcFile, progN);
			
			if (AES_BUFFER_SIZE >= available) {
				buffer = new byte[(int)available];
			}
			readFlag = streamIN.read(buffer);
			
			progUpdater.getPb().setValue((int)(((float)buffer.length/(float)srcFile.length())*i*100));
			i++;
		}		
		// close streams to avoid memory leaks
		streamIN.close();
		outWriter.close();
		progUpdater.getPb().setValue(100);
		progUpdater.getLabel().setText("Decryption completed! ");
		
		String msg = "AES decrypt phase ended: Output file => "+result.getName()+"\n";
		tempMSG += msg;
		logger.info(msg);
		
	}
	
	// Function that implements encryption of source file into several .png images without using pre-AES coding.
	// the images are written in the temp folder with incremental name, each one of the same size of the key image.
	private void onlySTEGCrypt(File srcFile) throws IOException, STEGException, InterruptedException {

		if(progUpdater == null) 
			progUpdater = new UpdateProgressPanelCrypter(false);
		
		progUpdater.getPb().setValue(0);
		progUpdater.getLabel().setText("Encripting "+srcFile.getName());
		
		// get (available for write) bytes per pix
		int mult = this.imgCoder.getBytePerPix();

		// initialize buffer to store one source's byte for every available px of keyImg
		FileInputStream streamIN = new FileInputStream(srcFile);
		int STEG_BUFFER_SIZE = w*h*mult;
		//int STEG_BUFFER_SIZE = w*h;
		
		closeables.add(streamIN);
		
		// check availabe bytes from source to avoid filling to buffer_size with null values
		// set buffer at 1 byte per px of image key if <= than available
		//int available = streamIN.available();
		long progN = 0;
		long available = getAvailable(srcFile, progN);
		if (STEG_BUFFER_SIZE >= available) {
			buffer = new byte[(int)available];
		}else {
			buffer = new byte[STEG_BUFFER_SIZE];
		}
		
		// this integer will contain value -1 when file is ended 
		int readFlag = streamIN.read(buffer);

		// STEG error types
		if(this.imgCoder == null || this.imgCoder.getKeyImage() == null) {
			
			// close streams to avoid memory leaks
			streamIN.close();
			logger.error("Error retrieving the STEG coder -> null coder or keyImage!");
			throw new STEGException();
		}

		int n = 1;
		File outFile;
		while(readFlag > 0) {
			
			checkInterruptedProgress();
			
			// crypt current buffer into image
			StegOmResult result = imgCoder.stegaCrypt4ByteXor(buffer, shuffleResult);
			BufferedImage imgResult = result.getImgRes();
			
			// sequence of info stored in name is the following:
			// [path of src dir] / [progressive file number] + [bytes of src stegged] + [shuffledBytes: 0|1] + .png 
			//
			// this value will be used during decription to recognize where px value have to be ignored
			// (cause img is of fixed dimension)
			String shuffleSign = shuffleResult ? "1" : "0";
			outFile = new File(RESULT_PATH + n + "_" + result.getnBytesCoded() + "_" + shuffleSign + "_.png");

			boolean OK = ImageIO.write(imgResult, "png", outFile);
							
			progN += buffer.length; 
			available = getAvailable(srcFile, progN);
			
			if (STEG_BUFFER_SIZE >= available) {
				buffer = new byte[(int)available];
			}
			readFlag = streamIN.read(buffer);
			
			if(OK) {
				String msg = "\n=> STEG image number "+n+" written: Output file => "+outFile.getAbsolutePath();
				tempMSG += msg;
				logger.info(msg);
				
			}else {
				streamIN.close();
				String msg = "\n=> Error ocurred writing "+outFile.getAbsolutePath()+" ... exiting";
				tempMSG += msg;
				logger.error(msg);
				
				throw new STEGException(msg);
			}
			n++;
			
			progUpdater.getPb().setValue((int)(((float)buffer.length/srcFile.length())*n*100));

		}
		// close streams to avoid memory leaks
		streamIN.close();
		progUpdater.getPb().setValue(100);
		progUpdater.getLabel().setText("Encryption completed! ");
					
	}
	
	// Function that implements decryption of source file into several .png images without using pre-AES coding.
	// the images are written in the temp folder with incremental name, each one of the same size of the key image.
	private void onlySTEGDeCrypt(File srcFile) throws IOException, STEGException, InterruptedException {

		if(progUpdater == null) progUpdater = new UpdateProgressPanelCrypter(false);
		progUpdater.getPb().setValue(0);
		progUpdater.getLabel().setText("Decripting "+srcFile.getName());
		
		// second element of folder result name is name of original file
		DECRYPT_NAME = srcFile.getName();
		
		// this time outfile is one and source can be multiple files incremental from 1st srcFile
		File outFile = new File(OUT_PATH+DECRYPT_NAME);
		outFile = ScreeptUtils.renameJavaObjFile(outFile);
		
		// update reference to currentOutFile/folder
		currentOutFile = outFile;
				
		// create out Stream setting append mode (do not overwrite every cycle)
		FileOutputStream fout = new FileOutputStream(outFile, true);
		
		closeables.add(fout);
		
		// STEG error types
		if(this.imgCoder == null || this.imgCoder.getKeyImage()==null) {
			
			// close streams to avoid memory leaks
			String msg = "Error retrieving the STEG decoder -> null coder or keyImage!";
			fout.close();
			logger.error(msg);
			throw new STEGException(msg);
		}

		// get file array and scan all files till the end and sort them by first element in name name
		File[] cryptedImgList = srcFile.listFiles(new FilenameFilter() {
			// accept only png files
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".png");
			}
		});
		// order files in source folder using first element(see STEGcrypter) with "_" separator
		Arrays.sort(cryptedImgList, new CustomFileComparator("_", 0));
		
		int i=1;
		
		for(File iFile : cryptedImgList) {
			
			checkInterruptedProgress();
			
			// retrieve number of coded bytes into image from file name - the second element in name
			int bytesToGet = Integer.valueOf(iFile.getName().split("_")[1]);
			// retrieve shuffled mode from file name - the third element in name
			boolean shuffleAtTheEnd = iFile.getName().split("_")[2].equals("1");
			
			// retrieve current image from file
			BufferedImage codedImg = ImageIO.read(iFile);
			
			byte[] buffer = imgCoder.stegaDeCrypt4ByteXor(codedImg, bytesToGet, shuffleAtTheEnd);
			
			fout.write(buffer);

			progUpdater.getPb().setValue((int)((float)i/cryptedImgList.length*100));
			
			String msg = "\nSTEG decripting current coded image: " + iFile.getName();
			tempMSG += msg;
			logger.info(msg);
			
			i++;
		}
		
		// close streams to avoid memory leaks
		fout.close();
		
		progUpdater.getPb().setValue(100);
		progUpdater.getLabel().setText("Decryption completed! ");
		
		String msg = "STEG decrypt phase ended: Output file => "+outFile.getAbsolutePath()+"\n";
		tempMSG += msg;
		logger.info(msg);
					
	}
	
	
	// Function that implements encryption of source file into several .png images using pre-AES coding.
	// the images are written in the temp folder with incremental name, each one of the same size of the key image.
	// this is method for top security
	private void completeCrypt(File srcFile) throws 
			IOException, AESException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, 
			InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, STEGException, BadLocationException, InterruptedException {
				
		if(progUpdater == null) progUpdater = new UpdateProgressPanelCrypter(false);
		progUpdater.getPb().setValue(0);
		progUpdater.getLabel().setText("Encripting "+srcFile.getName());
		
		// get (available for write) bytes per pix
		int mult = this.imgCoder.getBytePerPix();
		
		FileInputStream streamIN = new FileInputStream(srcFile);
		// grant that generated bytes can be all contained in image without loss (consider 16 max for padding and 16 for iv space) 
		int STEG_BUFFER_SIZE = (w*h*mult)-32;
		
		closeables.add(streamIN);
		
		// set buffer at 1 byte per px of image key if <= than available
		//int available = streamIN.available();
		long srcSize = srcFile.length();
		if(srcSize>Integer.MAX_VALUE)
			srcSize = Integer.MAX_VALUE;
		int available = (int)srcSize;
		
		if (STEG_BUFFER_SIZE >= available) {
			buffer = new byte[available];
		}else {
			buffer = new byte[STEG_BUFFER_SIZE];
		}
		
		// this integer will contain value -1 when file is ended 
		int readFlag = streamIN.read(buffer);
		
		// AES error types
		if(this.aesCoder == null || this.aesCoder.getPassword().length == 0) {

			// close streams to avoid memory leaks
			streamIN.close();
			logger.error("Error retrieving the AES coder -> null coder or void passsword!");
			throw new AESException();
		}
		
		// STEG error types
		if(imgCoder == null || imgCoder.getKeyImage()==null) {
			
			// close streams to avoid memory leaks
			streamIN.close();
			logger.error("Error retrieving the STEG coder -> null coder or keyImage!");
			throw new STEGException();
		}
		
		File outFile;
		
		int n = 1;
		int progN = 0;
		
		while(readFlag > 0) {
			
			checkInterruptedProgress();
			
			progUpdater.getLabel().setText("Crypting "+srcFile.getName());

			// encrypt (AES) current buffer content with aes key string
			// remember that in case of available%16!=0 -> partial result's size approximate in excess to reach multiple of 16 (that's how AES works)
			byte[] partialResult = aesCoder.encrypt(buffer, hashed, shuffleResult);				
			
			// encrypt (STEG) current partial aes-coded result into image using key image
			StegOmResult result = imgCoder.stegaCrypt4ByteXor(partialResult, shuffleResult);
			BufferedImage imgResult = result.getImgRes();
			
			// sequence of info stored in name is the following:
			// [path of src dir] / [progressive file number] + [bytes of src stegged] + [shuffledBytes: 0|1] + .png 
			String shuffleSign = shuffleResult ? "1" : "0";
			outFile = new File(RESULT_PATH + n + "_" + result.getnBytesCoded() + "_" + shuffleSign + "_.png");
			
			// write i-image on the file and then calculate new incremental name for next one
			boolean OK = ImageIO.write(imgResult, "png", outFile);
			
			//available = streamIN.available();
			progN += buffer.length; 
			srcSize = srcFile.length();
			if(srcSize>Integer.MAX_VALUE)
				srcSize = Integer.MAX_VALUE;
			available = (int)srcSize-progN;
			
			if (STEG_BUFFER_SIZE >= available) {
				buffer = new byte[available];
			}
			readFlag = streamIN.read(buffer);
			
			if(OK) {
				String msg = "\n=> COMPLETE image number "+n+" written: Output file => "+outFile.getAbsolutePath();
				tempMSG += msg;
				logger.info(msg);
			}else {
				streamIN.close();
				String msg = "\n=> Error ocurred writing "+outFile.getAbsolutePath()+" ... exiting";
				tempMSG += msg;
				logger.error(msg);
				
				throw new STEGException(msg);	
			}
			
			progUpdater.getPb().setValue((int)(((float)buffer.length/(float)srcFile.length())*n*100));
			String msg = "\n COMPLETE crypt curent file ended: Output file => "+outFile.getAbsolutePath();
			tempMSG += msg;
			logger.info(msg);			
			
			n++;
		}		
		// close streams to avoid memory leaks
		streamIN.close();
		progUpdater.getPb().setValue(100);
		progUpdater.getLabel().setText("Encryption completed! ");
		
	}
	
	// this function implement the decription of several (0-n) images containing crypted values, using the encryption key image.
	// the resulting bytes are decripted also with AES key once STEG-decripted, before to be written on the output file.
	private void completeDeCrypt(File srcFile) throws 
			IOException, AESException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, STEGException, BadLocationException, InterruptedException {
		
		if(progUpdater == null) progUpdater = new UpdateProgressPanelCrypter(false);
		progUpdater.getPb().setValue(0);
		progUpdater.getLabel().setText("Decripting "+srcFile.getName());
		
//		// create a copy by reference of the original coder - THIS WILL USE SAME PORTION USED FOR ENCRYPTION
//		ImageCodeDecode copyOfImgCoder = new ImageCodeDecode(imgCoder);
//		// calculate w to grant n� of pixels%16=0 and use the portion of key image with calculated
//		// now update key image with portion calculated
//		copyOfImgCoder.keyImage = (subImgMultOfNpx(imgCoder.getKeyImage(), 16));
//		
		// second element of folder result name is name of original file
		DECRYPT_NAME = srcFile.getName();
		
		// this time outfile is one and source can be multiple files incremental from 1st srcFile
		File outFile = new File(OUT_PATH + DECRYPT_NAME);		
		outFile = ScreeptUtils.renameJavaObjFile(outFile);
		
		// update reference to currentOutFile/folder
		currentOutFile = outFile;
				
		// create out Stream setting append mode (do not overwrite every cycle)
		FileOutputStream fout = new FileOutputStream(outFile, true);

		closeables.add(fout);
		
		// STEG error types
		if(imgCoder == null || imgCoder.getKeyImage()==null) {
			
			// close streams to avoid memory leaks
			String msg = "Error retrieving the STEG decoder -> null coder or keyImage!";
			fout.close();
			logger.error(msg);
			throw new STEGException(msg);
		}

		// get file array and scan all files till the end and sort them by first element in name name
		File[] cryptedImgList = srcFile.listFiles(new FilenameFilter() {
			// accept only png files
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".png");
			}
		});
		
		// order files in source folder using first element(see STEGcrypter) with "_" separator
		Arrays.sort(cryptedImgList, new CustomFileComparator("_", 0));
		
		int i = 1;
		
		for(File iFile : cryptedImgList) {
			
			checkInterruptedProgress();
			
			// retrieve number of coded bytes into image from file name - the second element in name
			int bytesToGet = Integer.valueOf(iFile.getName().split("_")[1]);
			// retrieve shuffled mode from file name - the third element in name
			boolean shuffleAtTheEnd = iFile.getName().split("_")[2].equals("1");
						
			// retrieve current image from file
			BufferedImage codedImg = ImageIO.read(iFile);
			
			// decript with steg current coded image
			byte[] partial_res = imgCoder.stegaDeCrypt4ByteXor(codedImg, bytesToGet, shuffleAtTheEnd);
			
			// now decript with aes and write to out file
			fout.write(aesCoder.decrypt(partial_res));
			
			progUpdater.getPb().setValue((int)((float)i/cryptedImgList.length*100));
			String msg = "\nCOMPLETE decripting current coded image: "+iFile.getName();
			tempMSG += msg;
			logger.info(msg);
			
			i++;
		}
		
		// close streams to avoid memory leaks
		fout.close();
		
		String msg = "COMPLETE decrypt phase ended: Output file => "+outFile.getAbsolutePath()+"\n";
		tempMSG += msg;
		logger.info(msg);
	}
	
	private long getAvailable(File source, long progN) {
		long srcSize = source.length();
		if(srcSize>Integer.MAX_VALUE)
			srcSize = Integer.MAX_VALUE;
		return (srcSize-progN);
	}
	
	/**
	 * This method will grant that the buffer created with w*h will contain all aes coded bytes
	 * In other words this make sure number of elements in a 2-Dimensional array of W*H elements.
	 * 
	 * It updates parameters W and H, making the n� of pixels multiple of K
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
		// Make W or H multiple of N => now if I multiply a number to it, i've chosen Width.
		// The result it's still multiple of N. the method chooses the nearest number
		
		W = (W/N)*N;
		
		return W;
		
// - V2 -	W*H has to be multiple of N		
//
//			Integer tempRes = W*H;
//			if(tempRes%N != 0) {
//			tempRes = Math.round(tempRes/N)*N;
//			// now tempRes divides N, so also 8
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
	 * @param N this is the divisor to which N�pixels must be multiple of
	 * @return sub-image: the extracted image with [new width <= original width] and N�px%divisor = 0
	 */
	static public BufferedImage subImgMultOfNpx(BufferedImage key, int N) {
		int newWidth = grantAreaMultipleOfN(key.getWidth(), key.getHeight(), N);
		BufferedImage multNIMG = ImageEncodingUtils.getPortion(key, newWidth, key.getHeight());
		return multNIMG;
	}
	
	public void disableProgressUpdater() {
		
		if(progUpdater != null) {
			progUpdater.getPb().setValue(100);
			progUpdater.forceClose();
		}
	}
	
	public void killOpenStreams(){
		for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                }catch (Exception e) {
					logger.error("An error occurred closing opened streams", e);
				}
            }
        }
	}
	
	public void setProgressBarVisible(AtomicBoolean enabled) {
		if (this.progUpdater != null && this.progUpdater.progBarEnabled != null) {
			this.progUpdater.progBarEnabled.set(enabled.get());
		}
	}
	
	public void checkInterruptedProgress() throws InterruptedException {
		if (this.progUpdater != null && this.progUpdater.progBarEnabled != null) {
			if(this.progUpdater.stopProcess.get()) {
				this.progUpdater.forceCloseNExceptionSignal();
			}
		}
	}
	
	//////////////////////
	// GETTERS & SETTERS /
	//////////////////////

	public StegCipher getImgCoder() {
		return imgCoder;
	}

	/**
	 * set the Coder used for STEG ecryption/decryption
	 * @return
	 */
	public void setImgCoder(StegCipher imgCoder) {
		this.imgCoder = imgCoder;
	}

	public AesCipher getAesCoder() {
		return aesCoder;
	}
	
	public boolean isHashed() {
		return hashed;
	}

	public void setHashed(boolean hashed) {
		this.hashed = hashed;
	}

	/**
	 * set the Coder used for AES ecryption/decryption
	 * @return
	 */
	public void setAesCoder(AesCipher aesCoder) {
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
	public String getOutPath() {
		return OUT_PATH;
	}
	
	/**
	 * This method is used to set the source/destination path to which decrypt or encrypt source file(s)
	 * @param resPath
	 */
	public void setOutPath(String outPath) {
		OUT_PATH = outPath+"/";
	}

	public static int getAES_BUFFER_SIZE() {
		return AES_BUFFER_SIZE;
	}
	
//			DISABLED TO KEEP SAFE ENCRIPTION AND DECRIPTION - DIFFERENT SIZE MAKES FILE UNDECRIPTABLE
//	/**
//	 * @param bUFFER_SIZE integer representing size of buffer - maximum 2 GB 
//	 */
//	public static void setAES_BUFFER_SIZE(int bUFFER_SIZE) {
//		AES_BUFFER_SIZE = grantMultipleOfN(bUFFER_SIZE, 16);
//	}

	/**
	 * @return CRYTPT_NAME this is the name of the default outFile without extension
	 */
	public static String getCryptName() {
		return CRYPT_NAME;
	}

	/**
	 * used to retrieve message by screept manager
	 * @return
	 */
	public String getOutputStats() {
		return tempMSG;
	}

	/**
	 * used to retrieve message by screept manager and reset them
	 * @return
	 */
	public String pollOutputStats() {
		String outputStatsCloned = new String(tempMSG);
		tempMSG = "";
		return outputStatsCloned;
	}

	public boolean isShuffleResult() {
		return shuffleResult;
	}

	public void setShuffleResult(boolean shuffleResult) {
		this.shuffleResult = shuffleResult;
		logger.info("Manually Setting ShuffleMode (Coding only - Decoding it will be read from second bit) to: " + this.shuffleResult);
	}

	public boolean isLoggerEnabled() {
		return logger.isLoggerEnabled();
	}

	public void setLoggerEnabled(boolean loggerEnabled) {
		logger.setLoggerEnabled(loggerEnabled);
	}
}
