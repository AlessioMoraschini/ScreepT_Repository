package crypt.manager;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import crypt.aes.EncryptDecrypt;
import crypt.steg.ImageCodeDecode;
import crypt.steg.STEGException;
import gUI.mainGUI;
import gUI.utils.filedialog.GenericFileChooserDialog;
import iNIT.INItializer;
import uTILS.local.FileStuff;

public class ScreeptManagerImplementer_KO {

	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(ScreeptManagerImplementer_KO.class);
	
	// CODERS objects - shared by methods
	private EncryptDecrypt AEScoder;
	private ImageCodeDecode STEGcoder;	
	private ScreeptManager screepTManager;
	private INItializer currentConf;
	
	public String aesKey = "";
	public File srcKeyImageFile;
	public CryptoMode mode;
	public boolean resultOK;
	public String msg;
	public String opLog;
	
	public File outFileOrDir;
	
	// constructor
	public ScreeptManagerImplementer_KO(CryptoMode mode, File srcKeyIMG, String aesKEY, INItializer config) {
		this.mode = mode;
		this.srcKeyImageFile = srcKeyIMG;
		this.aesKey = aesKEY;
		this.currentConf = config;
	}
	
	// crypt/decrypt runners
	public Thread decripterRunner(File srcFile) {
		return new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				File destinationDir = null;
				
				// ask user if he wants to delete decripted after closing ScrYpT
				int deleteAfterClose = JOptionPane.showConfirmDialog(null, "Delete decripted file(s) after exiting ScrypT? (TEMP folder will be created on Desktop)",
						"Delete decripted when SW will be closed", JOptionPane.YES_NO_OPTION);
				
				if(deleteAfterClose == JOptionPane.YES_OPTION) {
					// if temporary mode create temp folder ScrYpT on desktop
					destinationDir = new File(mainGUI.TEMP_DECRIPTED_ROOT_PATH);
				}else {
					// get output asking to user
					destinationDir = GenericFileChooserDialog.directoWrite(null, currentConf);
				}
				
				// boolean value representing if everything went good
				resultOK = false;
								
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
							logger.error("Encoding Exception happened!", e1);
						} catch (NoSuchPaddingException e1) {
							logger.error("Padding Exception happened!", e1);
						} catch (NoSuchAlgorithmException e1) {
							logger.error("Algorithm Exception happened!", e1);
						} catch (IOException e1) {
							logger.error("Exception occurred moving the result file :(", e1);
						} catch (Exception e1) {
							logger.error("Generic Exception occurred :(", e1);
						}
					
					// ONLY STEG DECRYPT
					} else if (!mode.AES && mode.STEG) {
						try {
							screepTManager = initSTEG(srcKeyImageFile);
							
							if (srcFile.isDirectory()) {
								resultOK = deCryptDirSTEG(srcFile, destinationDir);
							}

						} catch (IOException e1) {
							logger.error("IO Exception happened!", e1);
						} catch (STEGException e1) {
							logger.error("STEG Exception happened!", e1);
						} catch (Exception e1) {
							logger.error("Generic Exception occurred :(", e1);
						}
						
					// COMPLETE DECRYPT
					} else if (mode.AES && mode.STEG) {
						try {
							screepTManager = initCOMPLETE(aesKey, srcKeyImageFile);

							if (srcFile.isDirectory()) {
								resultOK = deCryptDirSTEG(srcFile, destinationDir);
							}

						} catch (UnsupportedEncodingException e1) {
							logger.error("Encoding Exception happened!", e1);
						} catch (NoSuchPaddingException e1) {
							logger.error("Padding Exception happened!", e1);
						} catch (NoSuchAlgorithmException e1) {
							logger.error("Algorithm Exception happened!", e1);
						} catch (IOException e1) {
							logger.error("IO Exception happened!", e1);
						} catch (STEGException e1) {
							logger.error("STEG Exception happened!", e1);
						} catch (Exception e1) {
							logger.error("Generic Exception occurred :(", e1);
						}
					} 
				}
				opLog = screepTManager.getOutputStats();
				// INFORM USER ABOUT ESIT OF DECRYPT OPERATION
				if(resultOK) {
					msg = "Operation completed succesfully :)";
					JOptionPane.showMessageDialog(null, msg,"SUCCESS!", JOptionPane.INFORMATION_MESSAGE);
					logger.info(msg);
//					textAreaLog.setText(msg);
//					textAreaMain.setText(screepTManager.getOutputStats());
					//tabbedPane.setSelectedIndex(0);
					//lblSrcFileSel.setForeground(Color.yellow);
					//lblSrcFileSel.setText("Deselected after decription");
					//srcFile = null;

				}else {					
					if (destinationDir != null) {
						msg = "Something went wrong :(";
						JOptionPane.showMessageDialog(null, msg, "SUCCESS!", JOptionPane.ERROR_MESSAGE);
						logger.error(msg);
//						textAreaLog.setText(msg);
//						textAreaMain.setText(screepTManager.getOutputStats());
//						tabbedPane.setSelectedIndex(0);
					}else{
						JOptionPane.showMessageDialog(null, "No directory selected, export aborted!", "No directory selected", JOptionPane.WARNING_MESSAGE);
					}
				}
				
			}
		});
	}
	
	public Thread cripterRunner(File srcFile) {
		return new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				// get output asking to user
				File destinationDir = GenericFileChooserDialog.directoWrite(null, currentConf);
				
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
							logger.error("Encoding Exception happened!", e1);
						} catch (NoSuchPaddingException e1) {
							logger.error("Padding Exception happened!", e1);
						} catch (NoSuchAlgorithmException e1) {
							logger.error("Algorithm Exception happened!", e1);
						} catch (IOException e1) {
							logger.error("Exception occurred moving the result file :(", e1);
						} catch (Exception e1) {
							logger.error("Generic Exception occurred :(", e1);
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
							logger.error("IO Exception happened!", e1);
						} catch (STEGException e1) {
							logger.error("STEG Exception happened!", e1);
						} catch (Exception e1) {
							logger.error("Generic Exception occurred :(", e1);
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
							logger.error("Encoding Exception happened!", e1);
						} catch (NoSuchPaddingException e1) {
							logger.error("Padding Exception happened!", e1);
						} catch (NoSuchAlgorithmException e1) {
							logger.error("Algorithm Exception happened!", e1);
						} catch (IOException e1) {
							logger.error("IO Exception happened!", e1);
						} catch (STEGException e1) {
							logger.error("STEG Exception happened!", e1);
						} catch (Exception e1) {
							logger.error("Generic Exception occurred :(", e1);
						}
					} 
				}
				opLog = screepTManager.getOutputStats();
				// INFORM USER ABOUT ESIT OF CRYPT OPERATION
				if(resultOK) {
					String msg = "Operation completed succesfully :)";
					JOptionPane.showMessageDialog(null, msg,"SUCCESS!", JOptionPane.INFORMATION_MESSAGE);
					logger.info(msg);
//					textAreaLog.setText(msg);
//					textAreaMain.setText(screepTManager.getOutputStats());
//					tabbedPane.setSelectedIndex(0);
//					lblSrcFileSel.setForeground(Color.yellow);
//					lblSrcFileSel.setText("Deselected after encription");
//					srcFile = null;

				}else {
					if(destinationDir != null) {
						String msg = "Something went wrong :(";
						JOptionPane.showMessageDialog(null, msg,"SUCCESS!", JOptionPane.ERROR_MESSAGE);
						logger.error(msg);
//						textAreaLog.setText(msg);
//						textAreaMain.setText(screepTManager.getOutputStats());
//						tabbedPane.setSelectedIndex(0);
					}
					else{
						JOptionPane.showMessageDialog(null, "No directory selected, export aborted!", "No directory selected", JOptionPane.WARNING_MESSAGE);
					}
				}
				
			}
		});
	}
	
	private ScreeptManager initAES(String AES_KEY) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException {
		AEScoder = new EncryptDecrypt(AES_KEY);
		screepTManager = new ScreeptManager(AEScoder);
		return screepTManager;
	}
	
	private ScreeptManager initSTEG(File imageFile) throws IOException, STEGException {
		STEGcoder = new ImageCodeDecode(imageFile);
		screepTManager = new ScreeptManager(STEGcoder);
		return screepTManager;
	}
	
	private ScreeptManager initCOMPLETE(String AES_KEY, File imageFile) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, STEGException {
		AEScoder = new EncryptDecrypt(AES_KEY);
		STEGcoder = new ImageCodeDecode(imageFile);
		screepTManager = new ScreeptManager(STEGcoder, AEScoder);
		return screepTManager;
	}
	
	// CRYPT METHODS
	private boolean cryptCurrentFile(File src, File destinationDir) throws IOException{
		boolean resultOK = false;
		if (destinationDir!=null) {
			
			if(!destinationDir.exists()) {
				destinationDir.mkdirs();
			}
			
			screepTManager.setOutPath(destinationDir.getCanonicalPath());
			resultOK = screepTManager.crypt(src, currentConf);
			
		}

		return resultOK;
	}
	
	private boolean cryptDir(File src, File destinationDir) throws IOException {
		boolean resultOK = false;
		
		if (src.isFile()) {
			
			resultOK = cryptCurrentFile(src, destinationDir);

		}else if (src.isDirectory()) {
			// create new folder in destination
			File destSonDir = new File(destinationDir.getCanonicalPath()+"/"+mode.ID+"_"+src.getName());
			// if dir to create inside destination exists create unique name
			if(destSonDir.exists()) {
				destSonDir = FileStuff.renameJavaObjFile(destSonDir);
			}
			destSonDir.mkdir();
			
			// scan source folder content
			for(File iFile : src.listFiles()) {
				if(iFile.isDirectory()) {
					resultOK = cryptDir(iFile, destSonDir);
				}else if (iFile.isFile()) {
					resultOK = cryptCurrentFile(iFile, destSonDir);
				}
			}
			
		}
		
		return resultOK;
	}
	
	// DECRYPT METHODS	
	private boolean deCryptCurrentFile(File src, File destinationDir) throws IOException{
		boolean resultOK = false;
		if (destinationDir!=null) {
			
			if(!destinationDir.exists()) {
				destinationDir.mkdirs();
			}
			
			screepTManager.setOutPath(destinationDir.getCanonicalPath());
			resultOK = screepTManager.deCrypt(src, currentConf);
			
		}
		return resultOK;
	}
	
	private boolean deCryptDirAES(File src, File destinationDir) throws IOException {
		boolean resultOK = false;
		
		if (src.isFile()) {
			
			resultOK = deCryptCurrentFile(src, destinationDir);

		}else if (src.isDirectory()) {

			// create new folder in destination
			File destSonDir = new File(destinationDir.getCanonicalPath()+"/Decod_"+src.getName());
			// if dir to create inside destination exists create unique name
			destSonDir = FileStuff.renameJavaObjFile(destSonDir);
			destSonDir.mkdir();
			
			// scan source folder content
			for(File iFile : src.listFiles()) {
				if(iFile.isDirectory()) {
					resultOK = deCryptDirAES(iFile, destSonDir);
				}else if (iFile.isFile()) {
					resultOK = deCryptCurrentFile(iFile, destSonDir);
				}
			}
			
		}
		
		return resultOK;
	}
	
	// use this to recognize folder containing STEG/COMPLETE folders that contains result instead of generic folder
	private boolean deCryptDirSTEG(File src, File destinationDir) throws IOException {
		boolean resultOK = false;
		
		if (src.isDirectory()) {
			// create new folder in destination
			File destSonDir = new File(destinationDir.getCanonicalPath()+"/"+src.getName());
			destSonDir = FileStuff.renameJavaObjFile(destSonDir);
			// if dir to create inside destination exists create unique name
			
			// scan source folder content
			for(File iFile : src.listFiles()) {
				if(iFile.isDirectory()) {
					
					destSonDir.mkdir();
					// now check if sons are files or dir
					File[] retrieved = iFile.listFiles(new FileFilter() {
						
						@Override // don't scan contained files further if they are not directories
						public boolean accept(File sonFile) {
							if(sonFile.isFile()) {
								return false;
							}
							else return true;
						}
					});
					
					if(retrieved.length!=0 && retrieved[0].isDirectory()) {
						// if generic folder it contains other folders
						resultOK = deCryptDirSTEG(iFile, destSonDir);
					}else {
						// if STEG/COMPLETE crypted only png files are contained, then decript current folder
						resultOK = deCryptCurrentFile(iFile, destSonDir);
					}
					
				}else {
					// if STEG/COMPLETE crypted only png files are contained, then decript current folder
					resultOK = deCryptCurrentFile(src, destinationDir);
					break;
				}
			}
			
		}
		
		return resultOK;
	}
}
