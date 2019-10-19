package junit.GUI;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.NoSuchPaddingException;

import org.ini4j.InvalidFileFormatException;
import org.junit.Ignore;
import org.junit.Test;

import amfileutils.FileStuff;
import crypt.aes.AesCipher;
import crypt.manager.CryptoMode;
import crypt.manager.ScreeptManagerFileOrFolder;
import crypt.steg.StegCipher;
import resources.GeneralConfig;
import crypt.steg.STEGException;

public class ScreepT_Test_New_StandAlone {
	
///////////////////////////////////////////////////////////////////
////////////////// INITIALIZE VARIABLES FOR TEST //////////////////
///////////////////////////////////////////////////////////////////
	
	// DIRECTORIES 
	private final String TEMP_DIR = GeneralConfig.RESOURCES_DIR  + "temp/";
	
	private final static boolean enableAdvices = true;
	private final static boolean SHUFFLE_MODE = true;
	
	// KEY INITIALIZATION
	private final char[] AES_KEY = "ciaosonoalessio".toCharArray();
	private File keyIMG = new File("TEST_FILES/PROVA_1.png");
	
	// SOURCE FILE - COMMON FOR ALL ENCRIPTION TESTS
	private File srcFile = new File("TEST_FILES/Source_medium.txt");
	
	// ONLY AES CODED SOURCE (decript test)
	private File codedFileAES = new File("TEST_FILES/AesCrypted_Source_medium.txt");
	
	// ONLY STEG CODED SOURCE (decript test)
	private File codedFolderSTEG = new File("TEST_FILES/STEGcod_Source_medium.txt");
	
	// COMPLETE CODED SOURCE (decript test)
	private File codedFolderCOMPLETE = new File("TEST_FILES/COMPLETEcod_Source_medium.txt");
	
	// CODERS USED IN initMethod private functions (only for test purpose)
	private AesCipher AEScoder;
	private StegCipher STEGcoder;	
	private ScreeptManagerFileOrFolder screepTManager;
	
////////////////////////////////////////////////////////////////private ScreeptManagerFileOrFolder screepTManagerNew;
////////////////////// START TEST METHODS //////////////////////
////////////////////////////////////////////////////////////////
	
	@Test
	@Ignore
	public void testOnlyAEScrypt() throws InvalidFileFormatException, IOException, NoSuchPaddingException, NoSuchAlgorithmException{
		
		try {
			screepTManager = new ScreeptManagerFileOrFolder(null, null, SHUFFLE_MODE);
			screepTManager.setAdvicesEnabled(enableAdvices);
			
			File resultDir =new File(TEMP_DIR);
			
//			assertTrue(screepTManager.screepTManager.getMode().equals(CryptoMode.ONLY_AES));
			assertTrue(screepTManager.crYpt(CryptoMode.ONLY_AES, srcFile, resultDir, AES_KEY, null, SHUFFLE_MODE));
			
			
			for(File result : resultDir.listFiles()) {
				File out = new File("TEST_FILES/" + result.getName());
// 			remove comment to overwrite result
//				if (out.exists()) {
//					out.delete();
//				}
				out = FileStuff.renameJavaObjFile(out);

//				out = FileStuff.renameJavaObjFile(out);
				assertTrue(result.renameTo(out));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
//	@Ignore
	public void testOnlyAESdecrypt() throws InvalidFileFormatException, IOException, NoSuchPaddingException, NoSuchAlgorithmException{
		
		try {
			screepTManager = new ScreeptManagerFileOrFolder(null, null, SHUFFLE_MODE);
			screepTManager.setAdvicesEnabled(enableAdvices);
			
			File resultDir =new File(TEMP_DIR);
			
//			assertTrue(screepTManager.screepTManager.getMode().equals(CryptoMode.ONLY_AES));
			assertTrue(screepTManager.decrYpt(CryptoMode.ONLY_AES, codedFileAES, resultDir, AES_KEY, null, false));
			
			for(File result : resultDir.listFiles()) {
				
				File out = new File("TEST_FILES/aesDecrypted.txt");
				out = FileStuff.renameJavaObjFile(out);
				
				assertTrue(result.renameTo(out));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Ignore
	public void testOnlySTEGcrypt() throws InvalidFileFormatException, IOException, NoSuchPaddingException, NoSuchAlgorithmException{
		
		try {
			screepTManager = new ScreeptManagerFileOrFolder(null, null, SHUFFLE_MODE);
			screepTManager.setAdvicesEnabled(enableAdvices);
			
			File resultDir =new File(TEMP_DIR);
			System.out.println(srcFile.getName()+" has "+srcFile.length()+"bytes");
			
//			assertTrue(screepTManager.screepTManager.getMode().equals(CryptoMode.ONLY_STEG));
			assertTrue(screepTManager.crYpt(CryptoMode.ONLY_STEG, srcFile, resultDir, AES_KEY, keyIMG, SHUFFLE_MODE));
			
			
			for(File result : resultDir.listFiles()) {
				// destination path - avoid duplicates
				File out = new File("TEST_FILES/" + result.getName());
				if (out.exists() && out.isDirectory()) {
					out.delete();
				}

				out = FileStuff.renameJavaObjFile(out);
				assertTrue(result.renameTo(out));
				//assertTrue(result.isDirectory());
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Ignore
	public void testOnlySTEGDecrypt() throws InvalidFileFormatException, IOException, NoSuchPaddingException, NoSuchAlgorithmException{
		
		try {
			screepTManager = new ScreeptManagerFileOrFolder(null, null, SHUFFLE_MODE);
			screepTManager.setAdvicesEnabled(enableAdvices);
			
			System.out.println(codedFolderSTEG.getName()+" has "+codedFolderSTEG.listFiles().length+"bytes");
			
//			assertTrue(screepTManager.screepTManager.getMode().equals(CryptoMode.ONLY_STEG));
			assertTrue(screepTManager.decrYpt(CryptoMode.ONLY_STEG, srcFile, codedFolderSTEG, AES_KEY, keyIMG, false));
			
			File resultDir =new File(TEMP_DIR);
			
			for(File result : resultDir.listFiles()) {
				// destination path - avoid duplicates
				File out = new File("TEST_FILES/" + result.getName());
				if (out.exists() && out.isDirectory()) {
					out.delete();
				}

				out = FileStuff.renameJavaObjFile(out);
				assertTrue(result.renameTo(out));
				//assertTrue(result.isDirectory());
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Ignore
	public void testCompleteCrypt() throws InvalidFileFormatException, IOException, NoSuchPaddingException, NoSuchAlgorithmException{
		
		try {
			screepTManager = new ScreeptManagerFileOrFolder(null, null, SHUFFLE_MODE);
			screepTManager.setAdvicesEnabled(enableAdvices);
			
			File resultDir =new File(TEMP_DIR);
			System.out.println(srcFile.getName()+" has "+srcFile.length()+"bytes");
			
			assertTrue(screepTManager.screepTManager.getMode().equals(CryptoMode.COMPLETE));
			assertTrue(screepTManager.crYpt(CryptoMode.COMPLETE, srcFile, resultDir, AES_KEY, keyIMG, SHUFFLE_MODE));
			
			for(File result : resultDir.listFiles()) {
				// destination path - avoid duplicates
				File out = new File("TEST_FILES/" + result.getName());
				if (out.exists() && out.isDirectory()) {
					out.delete();
				}

				out = FileStuff.renameJavaObjFile(out);
				assertTrue(result.renameTo(out));
				//assertTrue(result.isDirectory());
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Ignore
	public void testCompleteDeCrypt() throws InvalidFileFormatException, IOException, NoSuchPaddingException, NoSuchAlgorithmException{
		
		try {
			screepTManager = new ScreeptManagerFileOrFolder(null, null, SHUFFLE_MODE);
			screepTManager.setAdvicesEnabled(enableAdvices);
			
			File resultDir =new File(TEMP_DIR);
			System.out.println(codedFolderSTEG.getName()+" has "+codedFolderSTEG.listFiles().length+"bytes");
			
			assertTrue(screepTManager.screepTManager.getMode().equals(CryptoMode.COMPLETE));
			assertTrue(screepTManager.decrYpt(CryptoMode.COMPLETE, srcFile, resultDir, AES_KEY, keyIMG, false));
			
			for(File result : resultDir.listFiles()) {
				// destination path - avoid duplicates
				File out = new File("TEST_FILES/" + result.getName());
				if (out.exists() && out.isDirectory()) {
					out.delete();
				}

				out = FileStuff.renameJavaObjFile(out);
				assertTrue(result.renameTo(out));
				//assertTrue(result.isDirectory());
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void initAES() throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException {
		AEScoder = new AesCipher(AES_KEY);
		screepTManager = new ScreeptManagerFileOrFolder(null, null, SHUFFLE_MODE);
		screepTManager.setAdvicesEnabled(enableAdvices);
	}
	
	private void initSTEG() throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, STEGException, media.STEGException {
		STEGcoder = new StegCipher(keyIMG);
		screepTManager = new ScreeptManagerFileOrFolder(null, null, SHUFFLE_MODE);
		screepTManager.setAdvicesEnabled(enableAdvices);
	}
	
	private void initCOMPLETE() throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, STEGException, media.STEGException {
		AEScoder = new AesCipher(AES_KEY);
		STEGcoder = new StegCipher(keyIMG);
		screepTManager = new ScreeptManagerFileOrFolder(null, null, SHUFFLE_MODE);
		screepTManager.setAdvicesEnabled(enableAdvices);
	}
}
