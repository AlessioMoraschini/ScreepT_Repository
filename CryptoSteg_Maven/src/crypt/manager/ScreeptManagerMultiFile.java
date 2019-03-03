package crypt.manager;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import crypt.aes.AESException;
import crypt.aes.EncryptDecrypt;
import crypt.steg.ImageCodeDecode;
import crypt.steg.STEGException;
import uTILS.local.FileStuff;

public class ScreeptManagerMultiFile {

	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(ScreeptManagerMultiFile.class);
	
	public static final String TEMP_DECRIPTED_ROOT_PATH = System.getProperty("user.home")+"/Desktop/ScrYpT_decrypted_tempFiles";
	
	// CODERS objects - shared by methods
	private static EncryptDecrypt AEScoder;
	private static ImageCodeDecode STEGcoder;	
	private static ScreeptManager screepTManager;
	private static boolean shuffle = true;
	
	private static String opInfo = "Nothing to display";
	
	private static boolean coderUsed = false;
	
////////////////////////// MAIN METHODS ////////////////////////////////
	public static boolean crYpt(CryptoMode mode, File srcFile, File destinationDir, String aesKey, File srcKeyImageFile) {
		boolean resultOK = false;

		// notify that method is running
		coderUsed=true;
		
		// get output asking to user
		
		// boolean value representing if everything went good
		resultOK = false;
		
		if (srcFile!=null && srcFile.exists() && destinationDir!=null) {
			
			if(!destinationDir.exists()) {
				destinationDir.mkdir();
			}
			
			// ONLY AES CRYPT
			if (mode.AES && !mode.STEG) {
				try {
					// initialize coder
					screepTManager = initAES(aesKey);
					
					// if only one file
					if (srcFile.isFile()) {
						resultOK = cryptCurrentFile(srcFile, destinationDir);
					
					// if directory selected
					}else if (srcFile.isDirectory()) {
						resultOK = cryptDir(srcFile, destinationDir);
					}

				} catch (UnsupportedEncodingException e1) {
					resultOK = resetAfterException("Encoding Exception happened!", e1);
				} catch (NoSuchPaddingException e1) {
					resultOK = resetAfterException("Padding Exception happened!", e1);
				} catch (NoSuchAlgorithmException e1) {
					resultOK = resetAfterException("Algorithm Exception happened!", e1);
				} catch (IOException e1) {
					resultOK = resetAfterException("Exception occurred moving the result file :(", e1);
				} catch (Exception e1) {
					resultOK = resetAfterException("Generic Exception occurred :(", e1);
				}
			
			// ONLY STEG CRYPT
			} else if (!mode.AES && mode.STEG) {
				try {
					
					screepTManager = initSTEG(srcKeyImageFile);
					
					// if only one file
					if (srcFile.isFile()) {
						resultOK = cryptCurrentFile(srcFile, destinationDir);
					
					// if directory selected
					}else if (srcFile.isDirectory()) {
						resultOK = cryptDir(srcFile, destinationDir);
					}

				} catch (IOException e1) {
					resultOK = resetAfterException("IO Exception happened!", e1);
				} catch (STEGException e1) {
					resultOK = resetAfterException("STEG Exception happened!", e1);
				} catch (Exception e1) {
					resultOK = resetAfterException("Generic Exception occurred :(", e1);
				}
				
			// COMPLETE CRYPT
			} else if (mode.AES && mode.STEG) {
				try {
					
					screepTManager = initCOMPLETE(aesKey, srcKeyImageFile);
					
					// if only one file
					if (srcFile.isFile()) {
						resultOK = cryptCurrentFile(srcFile, destinationDir);
					
					// if directory selected
					}else if (srcFile.isDirectory()) {
						resultOK = cryptDir(srcFile, destinationDir);
					}

				} catch (UnsupportedEncodingException e1) {
					resultOK = resetAfterException("Encoding Exception happened!", e1);
				} catch (NoSuchPaddingException e1) {
					resultOK = resetAfterException("Padding Exception happened!", e1);
				} catch (NoSuchAlgorithmException e1) {
					resultOK = resetAfterException("Algorithm Exception happened!", e1);
				} catch (IOException e1) {
					resultOK = resetAfterException("IO Exception happened!", e1);
				} catch (STEGException e1) {
					resultOK = resetAfterException("STEG Exception happened!", e1);
				} catch (AESException e1) {
					resultOK = resetAfterException("AES Exception happened!", e1);
				} catch (Exception e1) {
					resultOK = resetAfterException("Generic Exception occurred :(", e1);
				}
			} 
		}
		coderUsed=false;
		
		if(screepTManager!=null) {
			screepTManager.disableProgressUpdater();
		}

		// INFORM USER ABOUT ESIT OF CRYPT OPERATION
		if(resultOK) {
			String msg = "Operation completed succesfully :)";
			JOptionPane.showMessageDialog(null, msg,"SUCCESS!", JOptionPane.INFORMATION_MESSAGE);
			logger.info(msg);
			srcFile = null;

		}else {
			if(destinationDir != null) {
				String msg = "Something went wrong :(";
				JOptionPane.showMessageDialog(null, msg,"SUCCESS!", JOptionPane.ERROR_MESSAGE);
				logger.error(msg);
			}
			else{
				JOptionPane.showMessageDialog(null, "No directory selected, export aborted!", "No directory selected", JOptionPane.WARNING_MESSAGE);
			}
		}
		
		coderUsed=false;
		opInfo = screepTManager.getOutputStats();
		
		return resultOK;
	}
	
	public static boolean decrYpt(CryptoMode mode, File srcFile, File destinationDir, String aesKey, File srcKeyImageFile, boolean deleteAfterClose) {
		boolean resultOK = false;

		if(deleteAfterClose) {
			// if temporary mode create temp folder ScrYpT on desktop
			destinationDir = new File(TEMP_DECRIPTED_ROOT_PATH);
		}
		
		// notify that method is running
		coderUsed=true;

		if (srcFile!=null && srcFile.exists() && destinationDir!=null) {
			
			if(!destinationDir.exists()) {
				destinationDir.mkdir();
			}
			
			// ONLY AES DECRYPT
			if (mode.AES && !mode.STEG) {
				try {
					screepTManager = initAES(aesKey);
					
					// if only one file
					if (srcFile.isFile()) {
						resultOK = deCryptCurrentFile(srcFile, destinationDir);
					
					// if directory selected
					}else if (srcFile.isDirectory()) {
						resultOK = deCryptDirAES(srcFile, destinationDir);
					}

				} catch (UnsupportedEncodingException e1) {
					resultOK = resetAfterException("Encoding Exception happened!", e1);
				} catch (NoSuchPaddingException e1) {
					resultOK = resetAfterException("Padding Exception happened!", e1);
				} catch (NoSuchAlgorithmException e1) {
					resultOK = resetAfterException("Algorithm Exception happened!", e1);
				} catch (IOException e1) {
					resultOK = resetAfterException("Exception occurred moving the result file :(", e1);
				} catch (Exception e1) {
					resultOK = resetAfterException("Generic Exception occurred :(", e1);
				}
			
			// ONLY STEG DECRYPT
			} else if (!mode.AES && mode.STEG) {
				try {
					screepTManager = initSTEG(srcKeyImageFile);
					
					if (srcFile.isDirectory()) {
						resultOK = deCryptDirSTEG(srcFile, destinationDir);
					}

				} catch (IOException e1) {
					resultOK = resetAfterException("IO Exception happened!", e1);
				} catch (STEGException e1) {
					resultOK = resetAfterException("STEG Exception happened!", e1);
				} catch (Exception e1) {
					resultOK = resetAfterException("Generic Exception occurred :(", e1);
				}
				
			// COMPLETE DECRYPT
			} else if (mode.AES && mode.STEG) {
				try {
					screepTManager = initCOMPLETE(aesKey, srcKeyImageFile);

					if (srcFile.isDirectory()) {
						resultOK = deCryptDirSTEG(srcFile, destinationDir);
					}

				} catch (UnsupportedEncodingException e1) {
					resultOK = resetAfterException("Encoding Exception happened!", e1);
				} catch (NoSuchPaddingException e1) {
					resultOK = resetAfterException("Padding Exception happened!", e1);
				} catch (NoSuchAlgorithmException e1) {
					resultOK = resetAfterException("Algorithm Exception happened!", e1);
				} catch (IOException e1) {
					resultOK = resetAfterException("IO Exception happened!", e1);
				} catch (STEGException e1) {
					resultOK = resetAfterException("STEG Exception happened!", e1);
				} catch (Exception e1) {
					resultOK = resetAfterException("Generic Exception occurred :(", e1);
				}
			} 
		}
		coderUsed=false;
		
		if(screepTManager!=null) {
			screepTManager.disableProgressUpdater();
		}
		
		// INFORM USER ABOUT ESIT OF DECRYPT OPERATION
		if(resultOK) {
			String msg = "Operation completed succesfully :)";
			JOptionPane.showMessageDialog(null, msg,"SUCCESS!", JOptionPane.INFORMATION_MESSAGE);
			logger.info(msg);
			srcFile = null;

		}else {					
			if (destinationDir != null) {
				String msg = "Something went wrong :(";
				JOptionPane.showMessageDialog(null, msg, "SUCCESS!", JOptionPane.ERROR_MESSAGE);
				logger.error(msg);
			}else{
				JOptionPane.showMessageDialog(null, "No directory selected, export aborted!", "No directory selected", JOptionPane.WARNING_MESSAGE);
			}
		}
		
		coderUsed=false;
		opInfo = screepTManager.getOutputStats();

		return resultOK;
	}
	
	// exception handler
	private static boolean resetAfterException(String msg, Exception e) {
		logger.error(msg, e);
		return false;
	}
	
///////////////////////////////////////////////////////// CRYPT INITIALIZERS /////////////////////////////////////////////////////////
	private static ScreeptManager initAES(String AES_KEY)
			throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException {
		AEScoder = new EncryptDecrypt(AES_KEY);
		screepTManager = new ScreeptManager(AEScoder);
		return screepTManager;
	}

	private static ScreeptManager initSTEG(File imageFile) throws IOException, STEGException {
		if (imageFile == null) {
			throw new STEGException();
		}
		STEGcoder = new ImageCodeDecode(imageFile);
		screepTManager = new ScreeptManager(STEGcoder);
		screepTManager.setShuffleResult(shuffle);
		return screepTManager;
	}

	private static ScreeptManager initCOMPLETE(String AES_KEY, File imageFile)
			throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, STEGException, AESException {
		if (imageFile == null) {
			throw new STEGException();
		}
		if (AES_KEY.length() == 0) {
			throw new AESException();
		}
		AEScoder = new EncryptDecrypt(AES_KEY);
		STEGcoder = new ImageCodeDecode(imageFile);
		screepTManager = new ScreeptManager(STEGcoder, AEScoder);
		screepTManager.setShuffleResult(shuffle);
		return screepTManager;
	}

	///////////////////////////////////////////////////////// CRYPT METHODS
	///////////////////////////////////////////////////////// /////////////////////////////////////////////////////////
	private static boolean cryptCurrentFile(File src, File destinationDir) throws IOException {
		boolean resultOK = false;
		if (destinationDir != null) {

			if (!destinationDir.exists()) {
				destinationDir.mkdirs();
			}

			screepTManager.setOutPath(destinationDir.getCanonicalPath());
			resultOK = screepTManager.crypt(src);

		}

		return resultOK;
	}

	private static boolean cryptDir(File src, File destinationDir) throws IOException {
		boolean resultOK = false;

		if (src.isFile()) {

			resultOK = cryptCurrentFile(src, destinationDir);

		} else if (src.isDirectory()) {
			// create new folder in destination
			File destSonDir = new File(destinationDir.getCanonicalPath() + "/" + screepTManager.getMode().ID + "_" + src.getName());
			// if dir to create inside destination exists create unique name
			if (destSonDir.exists()) {
				destSonDir = FileStuff.renameJavaObjFile(destSonDir);
			}
			destSonDir.mkdir();

			// scan source folder content
			for (File iFile : src.listFiles()) {
				if (iFile.isDirectory()) {
					resultOK = cryptDir(iFile, destSonDir);
				} else if (iFile.isFile()) {
					resultOK = cryptCurrentFile(iFile, destSonDir);
				}
			}

		}

		return resultOK;
	}

///////////////////////////////////////////////////////// DECRYPT METHODS /////////////////////////////////////////////////////////
	private static boolean deCryptCurrentFile(File src, File destinationDir) throws IOException {
		boolean resultOK = false;
		if (destinationDir != null) {

			if (!destinationDir.exists()) {
				destinationDir.mkdirs();
			}

			screepTManager.setOutPath(destinationDir.getCanonicalPath());
			resultOK = screepTManager.deCrypt(src);

		}
		return resultOK;
	}

	private static boolean deCryptDirAES(File src, File destinationDir) throws IOException {
		boolean resultOK = false;

		if (src.isFile()) {

			resultOK = deCryptCurrentFile(src, destinationDir);

		} else if (src.isDirectory()) {

			// create new folder in destination
			File destSonDir = new File(destinationDir.getCanonicalPath() + "/Decod_" + src.getName());
			// if dir to create inside destination exists create unique name
			destSonDir = FileStuff.renameJavaObjFile(destSonDir);
			destSonDir.mkdir();

			// scan source folder content
			for (File iFile : src.listFiles()) {
				if (iFile.isDirectory()) {
					resultOK = deCryptDirAES(iFile, destSonDir);
					if (!resultOK) {
						FileUtils.deleteQuietly(iFile);
					}
				} else if (iFile.isFile()) {
					resultOK = deCryptCurrentFile(iFile, destSonDir);
				}
			}

		}

		return resultOK;
	}

	// use this to recognize folder containing STEG/COMPLETE folders that contains
	// result instead of generic folder
	private static boolean deCryptDirSTEG(File src, File destinationDir) throws IOException {
		boolean resultOK = false;

		if (src.isDirectory()) {
			// create new folder in destination
			File destSonDir = new File(destinationDir.getCanonicalPath() + "/" + src.getName());
			destSonDir = FileStuff.renameJavaObjFile(destSonDir);
			// if dir to create inside destination exists create unique name

			// scan source folder content
			for (File iFile : src.listFiles()) {
				if (iFile.isDirectory()) {

					destSonDir.mkdir();
					// now check if sons are files or dir
					File[] retrieved = iFile.listFiles(new FileFilter() {

						@Override // don't scan contained files further if they are not directories
						public boolean accept(File sonFile) {
							if (sonFile.isFile()) {
								return false;
							} else
								return true;
						}
					});

					if (retrieved.length != 0 && retrieved[0].isDirectory()) {
						// if generic folder it contains other folders
						resultOK = deCryptDirSTEG(iFile, destSonDir);
					} else {
						// if STEG/COMPLETE crypted only png files are contained, then decript current
						// folder
						resultOK = deCryptCurrentFile(iFile, destSonDir);
					}

				} else {
					// if STEG/COMPLETE crypted only png files are contained, then decript current
					// folder
					resultOK = deCryptCurrentFile(src, destinationDir);
					break;
				}
			}

		}

		return resultOK;
	}

	public static boolean isShuffle() {
		return shuffle;
	}

	public static void setShuffle(boolean shuffle) {
		ScreeptManagerMultiFile.shuffle = shuffle;
	}

	public String getOpInfo() {
		return opInfo;
	}
}
