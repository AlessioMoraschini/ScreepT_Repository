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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.NoSuchPaddingException;
import javax.swing.JComponent;

import crypt.aes.AESException;
import crypt.aes.AesCipher;
import crypt.steg.STEGException;
import crypt.steg.StegCipher;
import gui.dialogs.msg.JOptionHelper;
import om.CrypterResultDTO;
import util.ScreeptUtils;
import util.UpdateProgressPanelCrypter;
import utility.SafeLogger;

public class ScreeptManagerFileOrFolder {

	static SafeLogger logger = new SafeLogger(ScreeptManagerFileOrFolder.class);
	
	private String tempFolderInternalPath = null;
	private String TEMP_DECRIPTED_ROOT_PATH = System.getProperty("user.home")+"/Desktop/ScreepT_decrypted_tempFiles";
	
	// CODERS objects - shared by methods
	private AesCipher AEScoder;
	private StegCipher STEGcoder;	
	public ScreeptManager screepTManager;
	private boolean shuffle = true;
	private boolean hashed = true;
	
	private String opInfo = "Nothing to display";
	
	public boolean coderUsed = false;
	public boolean defaultProgressBarVisible = true;
	
	UpdateProgressPanelCrypter generalProgress;
	
	public JOptionHelper dialogHelper;
	
	/**
	 * Initialize a general encription/decription manager for files or folders and their content.
	 * 
	 * @param generalProgress if null normal progress updater will be used, else custom will be 
	 * used for secondary progressBar updates (be careful to provide a TestUpdateProgress 
	 * with two bars in this case, and remember that secondary will be automatically setted during crypt/decrypt process)
	 * @param parent this will be used as reference for dialogs location
	 * @Param advicesEnabled if true message dialogs will be printed out during operations
	 * 
	 * NB default shuffle is false, you have to specify it each time crypting/decrypting
	 */
	public ScreeptManagerFileOrFolder(UpdateProgressPanelCrypter generalProgress, JComponent parent, boolean advicesEnabled) {
		this.shuffle = false;
		// default dialogHelper initially disabled 
		this.dialogHelper = new JOptionHelper(parent, advicesEnabled);
		this.generalProgress = generalProgress;
		if (this.generalProgress != null) {
			this.generalProgress.getDialog().setVisible(false);
		}
		this.defaultProgressBarVisible = generalProgress == null;
	}
	
////////////////////////// MAIN METHODS ////////////////////////////////
	public CrypterResultDTO crYpt(CryptoMode mode, File srcFile, File destinationDir, char[] aesKey, File srcKeyImageFile, boolean shuffle) throws Exception{
		
		CrypterResultDTO result = new CrypterResultDTO();
		
		if (this.generalProgress != null) {
			this.generalProgress.getDialog().setVisible(true);
		} else {
			this.generalProgress = new UpdateProgressPanelCrypter(true);
		}
		
		this.shuffle = shuffle;

		// notify that method is running
		coderUsed=true;
		
		try {
			if (srcFile!=null && srcFile.exists() && destinationDir!=null) {
				
				if(!destinationDir.exists()) {
					destinationDir.mkdir();
				}
				
				// ONLY AES CRYPT
				if (mode.AES && !mode.STEG) {
					
					// initialize coder
					screepTManager = initAES(aesKey);
					
					// if only one file
					if (srcFile.isFile()) {
						result.resultOK = cryptCurrentFile(srcFile, destinationDir);
					
					// if directory selected
					}else if (srcFile.isDirectory()) {
						result.resultOK = cryptDir(srcFile, destinationDir);
					}
	
				// ONLY STEG CRYPT
				} else if (!mode.AES && mode.STEG) {
						
						screepTManager = initSTEG(srcKeyImageFile);
						
						// if only one file
						if (srcFile.isFile()) {
							result.resultOK = cryptCurrentFile(srcFile, destinationDir);
						
						// if directory selected
						}else if (srcFile.isDirectory()) {
							result.resultOK = cryptDir(srcFile, destinationDir);
						}
	
				// COMPLETE CRYPT
				} else if (mode.AES && mode.STEG) {
						
					screepTManager = initCOMPLETE(aesKey, srcKeyImageFile);
					
					// if only one file
					if (srcFile.isFile()) {
						result.resultOK = cryptCurrentFile(srcFile, destinationDir);
					
					// if directory selected
					}else if (srcFile.isDirectory()) {
						result.resultOK = cryptDir(srcFile, destinationDir);
					}
				} 
			}
		} catch (STEGException e1) {
			result.resultOK = resetAfterException("STEG Exception happened!", e1);
		} catch (AESException e1) {
			result.resultOK = resetAfterException("AES Exception happened!", e1);
		} catch (UnsupportedEncodingException e1) {
			result.resultOK = resetAfterException("Encoding Exception happened!", e1);
		} catch (NoSuchPaddingException e1) {
			result.resultOK = resetAfterException("Padding Exception happened!", e1);
		} catch (NoSuchAlgorithmException e1) {
			result.resultOK = resetAfterException("Algorithm Exception happened!", e1);
		} catch (IOException e1) {
			result.resultOK = resetAfterException("Exception occurred moving the result file :(", e1);
		} catch (InterruptedException e1) {
			result.resultOK = resetAfterException("Generic Exception occurred :(", e1);
			result.interrupted = true;
		} catch (Exception e1) {
			result.resultOK = resetAfterException("Generic Exception occurred :(", e1);
		
		} finally {
			coderUsed=false;
			
			//  CLEAR DATA AND CLOSE PROGRESS BAR
			if(screepTManager!=null) {
				screepTManager.disableProgressUpdater();
				if (screepTManager.getAesCoder() != null) {
					screepTManager.getAesCoder().clearSensitiveData();
				}
			}
		}

		// INFORM USER ABOUT ESIT OF CRYPT OPERATION
		if(result.resultOK) {
			String msg = "Operation completed succesfully :)";
			dialogHelper.info(msg, "SUCCESS!");
			logger.info(msg);
			srcFile = null;

		}else {
			if(destinationDir != null) {
				String msg = "Something went wrong :(";
				dialogHelper.error(msg,"ERROR encrypting source!");
				logger.error(msg);
			}
			else{
				String msg = "No directory selected, export aborted!";
				dialogHelper.warn(msg, "No directory selected");
				logger.warn(msg);
			}
		}
		
		coderUsed=false;
		opInfo = screepTManager.getOutputStats();
		screepTManager.killOpenStreams();
		
		return result;
	}
	
	public CrypterResultDTO decrYpt(CryptoMode mode, File srcFile, File destinationDir, char[] aesKey, File srcKeyImageFile, boolean shuffle) throws Exception{
		
		CrypterResultDTO result = new CrypterResultDTO();
		
		if (this.generalProgress != null) {
			this.generalProgress.getDialog().setVisible(true);
		}
		
		this.shuffle = shuffle;
		
		// notify that method is running
		coderUsed=true;

		try {
			if (srcFile!=null && srcFile.exists() && destinationDir!=null) {
				
				if(!destinationDir.exists()) {
					destinationDir.mkdir();
				}
				
				// ONLY AES DECRYPT
				if (mode.AES && !mode.STEG) {
					
					screepTManager = initAES(aesKey);
					
					// if only one file
					if (srcFile.isFile()) {
						result.resultOK = deCryptCurrentFile(srcFile, destinationDir);
					
					// if directory selected
					}else if (srcFile.isDirectory()) {
						result.resultOK = deCryptDirAES(srcFile, destinationDir);
					}
	
				
				// ONLY STEG DECRYPT
				} else if (!mode.AES && mode.STEG) {
						
					screepTManager = initSTEG(srcKeyImageFile);
						
					if (srcFile.isDirectory()) {
						result.resultOK = deCryptDirSTEG(srcFile, destinationDir);
					}
	
				// COMPLETE DECRYPT
				} else if (mode.AES && mode.STEG) {
					
					screepTManager = initCOMPLETE(aesKey, srcKeyImageFile);

					if (srcFile.isDirectory()) {
						result.resultOK = deCryptDirSTEG(srcFile, destinationDir);
					}
				} 
			}
			
		} catch (UnsupportedEncodingException e1) {
			result.resultOK = resetAfterException("Encoding Exception happened!", e1);
		} catch (NoSuchPaddingException e1) {
			result.resultOK = resetAfterException("Padding Exception happened!", e1);
		} catch (NoSuchAlgorithmException e1) {
			result.resultOK = resetAfterException("Algorithm Exception happened!", e1);
		} catch (IOException e1) {
			result.resultOK = resetAfterException("Exception occurred moving the result file :(", e1);
		} catch (STEGException e1) {
			result.resultOK = resetAfterException("STEG Exception happened!", e1);
		} catch (InterruptedException e1) {
			result.resultOK = resetAfterException("Generic Exception occurred :(", e1);
			result.interrupted = true;
		} catch (Exception e1) {
			result.resultOK = resetAfterException("Generic Exception occurred :(", e1);
		
		} finally {
			
			coderUsed=false;
			
			if(screepTManager!=null) {
				screepTManager.disableProgressUpdater();
				if (screepTManager.getAesCoder() != null) {
					screepTManager.getAesCoder().clearSensitiveData();
				}
			}
		}
		
		// INFORM USER ABOUT ESIT OF DECRYPT OPERATION
		if(result.resultOK) {
			String msg = "Operation completed succesfully :)";
			dialogHelper.info(msg,"SUCCESS!");
			logger.info(msg);
			srcFile = null;

		}else {					
			if (destinationDir != null) {
				String msg = "Something went wrong :(";
				dialogHelper.error(msg, "Error decrypting source");
				logger.error(msg);
			}else{
				String msg = "No directory selected, export aborted!";
				dialogHelper.warn(msg, "No directory selected");
				logger.warn(msg);
			}
		}
		
		coderUsed=false;
		opInfo = screepTManager.getOutputStats();
		screepTManager.killOpenStreams();

		return result;
	}
	
	// exception handler
	private static boolean resetAfterException(String msg, Exception e) {
		logger.error(msg, e);
		if(e instanceof IOException) {
			new JOptionHelper(null).error("IO Error, remember that destination cannot be inside source!", "IO error");
		}
		return false;
	}
	
///////////////////////////////////////////////////////// CRYPT INITIALIZERS /////////////////////////////////////////////////////////
	private ScreeptManager initAES(char[] AES_KEY)
			throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException {
		AEScoder = new AesCipher(AES_KEY);
		screepTManager = new ScreeptManager(AEScoder);
		screepTManager.setProgressBarVisible(new AtomicBoolean(defaultProgressBarVisible));
		screepTManager.setShuffleResult(shuffle);
		screepTManager.setHashed(hashed);
		screepTManager.TEMP_PATH = tempFolderInternalPath != null ? tempFolderInternalPath : screepTManager.TEMP_PATH;
		
		return screepTManager;
	}

	private ScreeptManager initSTEG(File imageFile) throws IOException, STEGException, STEGException {
		if (imageFile == null) {
			throw new STEGException();
		}
		STEGcoder = new StegCipher(imageFile);
		screepTManager = new ScreeptManager(STEGcoder);
		screepTManager.setProgressBarVisible(new AtomicBoolean(defaultProgressBarVisible));
		screepTManager.setShuffleResult(shuffle);
		screepTManager.setHashed(hashed);
		screepTManager.TEMP_PATH = tempFolderInternalPath != null ? tempFolderInternalPath : screepTManager.TEMP_PATH;
		
		return screepTManager;
	}

	private ScreeptManager initCOMPLETE(char[] AES_KEY, File imageFile)
			throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, STEGException, AESException, STEGException {
		if (imageFile == null) {
			throw new STEGException();
		}
		if (AES_KEY.length == 0) {
			throw new AESException();
		}
		AEScoder = new AesCipher(AES_KEY);
		STEGcoder = new StegCipher(imageFile);
		screepTManager = new ScreeptManager(STEGcoder, AEScoder);
		screepTManager.setProgressBarVisible(new AtomicBoolean(defaultProgressBarVisible));
		screepTManager.setShuffleResult(shuffle);
		screepTManager.setHashed(hashed);
		screepTManager.TEMP_PATH = tempFolderInternalPath != null ? tempFolderInternalPath : screepTManager.TEMP_PATH;
		
		return screepTManager;
	}

	///////////////////////////////////////////////////////// CRYPT METHODS
	///////////////////////////////////////////////////////// /////////////////////////////////////////////////////////
	private boolean cryptCurrentFile(File src, File destinationDir) throws IOException, InterruptedException, Exception {
		boolean resultOK = false;
		if (destinationDir != null) {

			if (!destinationDir.exists()) {
				destinationDir.mkdirs();
			}

			screepTManager.setOutPath(destinationDir.getCanonicalPath());
			resultOK = screepTManager.crypt(src, generalProgress);
		}

		return resultOK;
	}

	private boolean cryptDir(File src, File destinationDir) throws IOException, InterruptedException, Exception {
		
		// set initially to true to avoid having KO when no file is contained in directory - BUGfix
		boolean resultOK = true;

		if (src.isFile()) {

			resultOK = cryptCurrentFile(src, destinationDir);

		} else if (src.isDirectory()) {
			// check that destination is not contained in source to avoid loop
			if(ScreeptUtils.checkIfContainedInDir(destinationDir, src, logger)) {
				throw new IOException("Destination directory cannot be inside source directory, to avoid infinite loop");
			}
			
			// create new folder in destination
			File destSonDir = new File(destinationDir.getCanonicalPath() + "/" + src.getName());
			// if dir to create inside destination exists create unique name
			if (destSonDir.exists()) {
				destSonDir = ScreeptUtils.renameJavaObjFile(destSonDir);
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
	private boolean deCryptCurrentFile(File src, File destinationDir) throws IOException, InterruptedException, Exception {
		boolean resultOK = false;
		
		if (destinationDir != null) {

			if (!destinationDir.exists()) {
				destinationDir.mkdirs();
			}

			screepTManager.setOutPath(destinationDir.getCanonicalPath());
			resultOK = screepTManager.deCrypt(src, generalProgress);

		}
		return resultOK;
	}

	private boolean deCryptDirAES(File src, File destinationDir) throws IOException, InterruptedException, Exception {
		
		// set initially to true to avoid having KO when no file is contained in directory - BUGfix
		boolean resultOK = true;

		if (src.isFile()) {

			resultOK = deCryptCurrentFile(src, destinationDir);

		} else if (src.isDirectory()) {
			// check that destination is not contained in source to avoid loop
			if(ScreeptUtils.checkIfContainedInDir(destinationDir, src, logger)) {
				throw new IOException("Destination directory cannot be inside source directory, to avoid infinite loop");
			}
			
			// create new folder in destination
			File destSonDir = new File(destinationDir.getCanonicalPath() + "/" + src.getName());
			// if dir to create inside destination exists create unique name
			destSonDir = ScreeptUtils.renameJavaObjFile(destSonDir);
			destSonDir.mkdir();

			// scan source folder content
			for (File iFile : src.listFiles()) {
				if (iFile.isDirectory()) {
					resultOK = deCryptDirAES(iFile, destSonDir);
					if (!resultOK) {
						try {
							iFile.delete();
						}catch(Throwable e) {
							logger.warn("Cannot delete " + iFile, e);
						}
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
	private boolean deCryptDirSTEG(File src, File destinationDir) throws IOException, InterruptedException, Exception {
		
		// set initially to true to avoid having KO when no file is contained in directory - BUGfix
		boolean resultOK = true;

		if (src.isDirectory()) {
			// check that destination is not contained in source to avoid loop
			if(ScreeptUtils.checkIfContainedInDir(destinationDir, src, logger)) {
				throw new IOException("Destination directory cannot be inside source directory, to avoid infinite loop");
			}
			
			// create new folder in destination
			File destSonDir = new File(destinationDir.getCanonicalPath() + "/" + src.getName());
			destSonDir = ScreeptUtils.renameJavaObjFile(destSonDir);
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
	

	public boolean isDefaultProgressBarVisible() {
		return defaultProgressBarVisible;
	}

	public void setDefaultProgressBarVisible(boolean defaultProgressBarVisible) {
		this.defaultProgressBarVisible = defaultProgressBarVisible;
	}

	public String getOutputStats() {
		return (screepTManager != null) ? screepTManager.getOutputStats() : "";
	}
	
	/**
	 * Retrieve output stats resetting them
	 * @return
	 */
	public String pollOutputStats() {
		return (screepTManager != null) ? screepTManager.pollOutputStats() : "";
	}

	public void setAdvicesEnabled(boolean enabled) {
		this.dialogHelper.setAdvicesEnabled(enabled);
	}
	
	public boolean isShuffle() {
		return shuffle;
	}

	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
	}

	public String getOpInfo() {
		return opInfo;
	}
	
	public boolean isHashed() {
		return hashed;
	}

	public void setHashed(boolean hashed) {
		this.hashed = hashed;
	}

	public UpdateProgressPanelCrypter getGeneralProgress() {
		return generalProgress;
	}
	
	public void setGeneralProgress(UpdateProgressPanelCrypter generalProgress) {
		this.generalProgress = generalProgress;
	}

	public boolean isLoggerEnabled() {
		return logger.isLoggerEnabled();
	}

	public void setLoggerEnabled(boolean loggerEnabled) {
		logger.setLoggerEnabled(loggerEnabled);
	}

	public String getTempFolderInternalPath() {
		return screepTManager != null ? screepTManager.TEMP_PATH : tempFolderInternalPath;
	}

	public void setTempFolderInternalPath(String tempFolderInternalPath) {
		this.tempFolderInternalPath = tempFolderInternalPath;
	}

	public String getTEMP_DECRIPTED_ROOT_PATH() {
		return TEMP_DECRIPTED_ROOT_PATH;
	}

	public void setTEMP_DECRIPTED_ROOT_PATH(String tEMP_DECRIPTED_ROOT_PATH) {
		TEMP_DECRIPTED_ROOT_PATH = tEMP_DECRIPTED_ROOT_PATH;
	}
}
