package junit;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.NoSuchPaddingException;

import org.ini4j.InvalidFileFormatException;
import org.junit.Ignore;
import org.junit.Test;

import crypt.aes.AesCipher;
import crypt.manager.CryptoMode;
import crypt.manager.ScreeptManagerStub;
import crypt.steg.StegCipher;
import crypt.steg.STEGException;
import uTILS.local.FileStuff;

public class ScreepT_Test {
	
///////////////////////////////////////////////////////////////////
////////////////// INITIALIZE VARIABLES FOR TEST //////////////////
///////////////////////////////////////////////////////////////////
	
	// DIRECTORIES 
	private final String TEMP_DIR = "Resources_Scrypt/temp/";
	
	// KEY INITIALIZATION
	private final String AES_KEY = "ciaosonoalessio";
	private File keyIMG = new File("TEST_FILES/keyIMG_1000.png");
	
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
	private ScreeptManagerStub screepTManager;
	
////////////////////////////////////////////////////////////////
////////////////////// START TEST METHODS //////////////////////
////////////////////////////////////////////////////////////////
	
	@Test
	@Ignore
	public void testOnlyAEScrypt() throws InvalidFileFormatException, IOException, NoSuchPaddingException, NoSuchAlgorithmException{
		
		try {
			initAES();
			
			assertTrue(screepTManager.getMode().equals(CryptoMode.ONLY_AES));
			assertTrue(screepTManager.crypt(srcFile));
			
			File resultDir =new File(TEMP_DIR);
			
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
	@Ignore
	public void testOnlyAESdecrypt() throws InvalidFileFormatException, IOException, NoSuchPaddingException, NoSuchAlgorithmException{
		
		try {
			initAES();
			
			assertTrue(screepTManager.getMode().equals(CryptoMode.ONLY_AES));
			assertTrue(screepTManager.deCrypt(codedFileAES));
			
			File resultDir =new File(TEMP_DIR);
			
			for(File result : resultDir.listFiles()) {
				
				File out = new File("TEST_FILES/" + result.getName());
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
			initSTEG();
			
			System.out.println(srcFile.getName()+" has "+srcFile.length()+"bytes");
			
			assertTrue(screepTManager.getMode().equals(CryptoMode.ONLY_STEG));
			assertTrue(screepTManager.crypt(srcFile));
			
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
	public void testOnlySTEGDecrypt() throws InvalidFileFormatException, IOException, NoSuchPaddingException, NoSuchAlgorithmException{
		
		try {
			initSTEG();
			
			System.out.println(codedFolderSTEG.getName()+" has "+codedFolderSTEG.listFiles().length+"bytes");
			
			assertTrue(screepTManager.getMode().equals(CryptoMode.ONLY_STEG));
			assertTrue(screepTManager.deCrypt(codedFolderSTEG));
			
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
			initCOMPLETE();
			
			System.out.println(srcFile.getName()+" has "+srcFile.length()+"bytes");
			
			assertTrue(screepTManager.getMode().equals(CryptoMode.COMPLETE));
			assertTrue(screepTManager.crypt(srcFile));
			
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
	public void testCompleteDeCrypt() throws InvalidFileFormatException, IOException, NoSuchPaddingException, NoSuchAlgorithmException{
		
		try {
			initCOMPLETE();
			
			System.out.println(codedFolderSTEG.getName()+" has "+codedFolderSTEG.listFiles().length+"bytes");
			
			assertTrue(screepTManager.getMode().equals(CryptoMode.COMPLETE));
			assertTrue(screepTManager.deCrypt(codedFolderCOMPLETE));
			
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
	public void test_grantAreaMultipleOfN() {
		
		// test customization
		int nCycles = 20;
		// maxVal must be no more than square root of 2^31 = 2^15.5 = almost 46340 -> to avoid overflow of int
		int maxVal = 8000; // MAX 46340 for rows and column
		int divisor = 16;
		
		//initialize test messages
		System.out.println("3/2="+3/2);
		System.out.println("7/4="+7/4+"\n");		
		System.out.println("Starting test of grantAreaMultiple of "+divisor);
		
		// start test
		Integer W,H;
		int areaBefore,areaAfter;
		
		for (int i =0 ; i<nCycles ; i++) {
			
			SecureRandom rand = new SecureRandom(); 
			W = rand.nextInt(maxVal)+1;
			H = rand.nextInt(maxVal)+1;
		
			areaBefore = W*H;
			
			System.out.println("Before-> W="+W+" , H="+H + " => Area="+areaBefore + " => Area%divisor="+areaBefore%divisor);
			W = ScreeptManagerStub.grantAreaMultipleOfN(W, H, divisor);
			
			areaAfter = W*H;
			
			System.out.println("After-> W="+W+" , H="+H + " => Area="+areaAfter + " => Area%divisor="+areaAfter%divisor);
			System.out.println("\n########################################################################\n");
			
			assertTrue((W*H)%16==0 && areaBefore >= areaAfter);
		}

	}
	
	private void initAES() throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException {
		AEScoder = new AesCipher(AES_KEY);
		screepTManager = new ScreeptManagerStub(AEScoder);
	}
	
	private void initSTEG() throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, STEGException {
		STEGcoder = new StegCipher(keyIMG);
		screepTManager = new ScreeptManagerStub(STEGcoder);
	}
	
	private void initCOMPLETE() throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, STEGException {
		AEScoder = new AesCipher(AES_KEY);
		STEGcoder = new StegCipher(keyIMG);
		screepTManager = new ScreeptManagerStub(STEGcoder, AEScoder);
	}
}
