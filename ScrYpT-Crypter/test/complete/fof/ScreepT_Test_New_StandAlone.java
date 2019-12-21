package complete.fof;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import crypt.manager.CryptoMode;
import crypt.manager.ScreeptManagerFileOrFolder;
import util.ScreeptUtils;
import utils.TestUtils;

public class ScreepT_Test_New_StandAlone {
	
///////////////////////////////////////////////////////////////////
////////////////// INITIALIZE VARIABLES FOR TEST //////////////////
///////////////////////////////////////////////////////////////////
	
	// DIRECTORIES 
	private final String TEMP_DIR = "temp/";
	
	private final static boolean enableAdvices = true;
	private final static boolean SHUFFLE_MODE = true;
	private final static boolean hashed = true;
	
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
	private ScreeptManagerFileOrFolder screepTManagerFof;
	
	
////////////////////////////////////////////////////////////////private ScreeptManagerFileOrFolder screepTManagerNew;
////////////////////// START TEST METHODS //////////////////////
////////////////////////////////////////////////////////////////
	
	@Test
	@Ignore
	public void testOnlyAEScrypt() throws Exception{
		
		try {
			screepTManagerFof = new ScreeptManagerFileOrFolder(null, null, SHUFFLE_MODE);
			screepTManagerFof.setAdvicesEnabled(enableAdvices);
			screepTManagerFof.setHashed(hashed);
			
			File resultDir =new File(TEMP_DIR);
			
//			assertTrue(screepTManagerFof.screepTManager.getMode().equals(CryptoMode.ONLY_AES));
			assertTrue(screepTManagerFof.crYpt(CryptoMode.ONLY_AES, srcFile, resultDir, AES_KEY, null, SHUFFLE_MODE));
			
			TestUtils.printl("TEMP_RESULTS_PATH -> " + screepTManagerFof.screepTManager.TEMP_PATH);
			
			for(File result : resultDir.listFiles()) {
				File out = codedFileAES;
// 			remove comment to overwrite result
				if (out.exists()) {
					out.delete();
				}
				out = ScreeptUtils.renameJavaObjFile(out);

				assertTrue(result.renameTo(out));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
//	@Ignore
	public void testOnlyAESdecrypt() throws Exception{
		
		try {
			screepTManagerFof = new ScreeptManagerFileOrFolder(null, null, SHUFFLE_MODE);
			screepTManagerFof.setAdvicesEnabled(enableAdvices);
			screepTManagerFof.setHashed(hashed);
			
			File resultDir =new File(TEMP_DIR);
			
			assertTrue(screepTManagerFof.decrYpt(CryptoMode.ONLY_AES, codedFileAES, resultDir, AES_KEY, null, false));
			
			TestUtils.printl("TEMP_RESULTS_PATH -> " + screepTManagerFof.screepTManager.TEMP_PATH);
			
			for(File result : resultDir.listFiles()) {
				
				File out = new File("TEST_FILES/aesDecrypted.txt");
				out = ScreeptUtils.renameJavaObjFile(out);
				
				assertTrue(result.renameTo(out));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Ignore
	public void testOnlySTEGcrypt() throws Exception{
		
		try {
			screepTManagerFof = new ScreeptManagerFileOrFolder(null, null, SHUFFLE_MODE);
			screepTManagerFof.setAdvicesEnabled(enableAdvices);
			
			File resultDir =new File(TEMP_DIR);
			
			TestUtils.printl(srcFile.getName()+" has "+srcFile.length()+"bytes");
			
			assertTrue(screepTManagerFof.crYpt(CryptoMode.ONLY_STEG, srcFile, resultDir, AES_KEY, keyIMG, SHUFFLE_MODE));
			
			TestUtils.printl("TEMP_RESULTS_PATH -> " + screepTManagerFof.screepTManager.TEMP_PATH);
			
			for(File result : resultDir.listFiles()) {
				// destination path - avoid duplicates
				File out = new File("TEST_FILES/" + result.getName());
				if (out.exists() && out.isDirectory()) {
					out.delete();
				}

				out = ScreeptUtils.renameJavaObjFile(out);
				assertTrue(result.renameTo(out));
				//assertTrue(result.isDirectory());
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Ignore
	public void testOnlySTEGDecrypt() throws Exception{
		
		try {
			screepTManagerFof = new ScreeptManagerFileOrFolder(null, null, SHUFFLE_MODE);
			screepTManagerFof.setAdvicesEnabled(enableAdvices);
			
			TestUtils.printl(codedFolderSTEG.getName()+" has "+codedFolderSTEG.listFiles().length+"bytes");
			
			assertTrue(screepTManagerFof.decrYpt(CryptoMode.ONLY_STEG, srcFile, codedFolderSTEG, AES_KEY, keyIMG, false));

			TestUtils.printl("TEMP_RESULTS_PATH -> " + screepTManagerFof.screepTManager.TEMP_PATH);
			
			File resultDir =new File(TEMP_DIR);
			
			for(File result : resultDir.listFiles()) {
				// destination path - avoid duplicates
				File out = new File("TEST_FILES/" + result.getName());
				if (out.exists() && out.isDirectory()) {
					out.delete();
				}

				out = ScreeptUtils.renameJavaObjFile(out);
				assertTrue(result.renameTo(out));
				//assertTrue(result.isDirectory());
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Ignore
	public void testCompleteCrypt() throws Exception{
		
		try {
			screepTManagerFof = new ScreeptManagerFileOrFolder(null, null, SHUFFLE_MODE);
			screepTManagerFof.setAdvicesEnabled(enableAdvices);
			screepTManagerFof.setHashed(hashed);
			
			File resultDir =new File(TEMP_DIR);
			
			System.out.println(srcFile.getName()+" has "+srcFile.length()+"bytes");
			
			assertTrue(screepTManagerFof.crYpt(CryptoMode.COMPLETE, srcFile, resultDir, AES_KEY, keyIMG, SHUFFLE_MODE));

			TestUtils.printl("TEMP_RESULTS_PATH -> " + screepTManagerFof.screepTManager.TEMP_PATH);
			
			for(File result : resultDir.listFiles()) {
				// destination path - avoid duplicates
				File out = new File("TEST_FILES/" + result.getName());
				if (out.exists() && out.isDirectory()) {
					out.delete();
				}

				out = ScreeptUtils.renameJavaObjFile(out);
				assertTrue(result.renameTo(out));
				//assertTrue(result.isDirectory());
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Ignore
	public void testCompleteDeCrypt() throws Exception {
		
		try {
			screepTManagerFof = new ScreeptManagerFileOrFolder(null, null, SHUFFLE_MODE);
			screepTManagerFof.setAdvicesEnabled(enableAdvices);
			screepTManagerFof.setHashed(hashed);
			
			File resultDir =new File(TEMP_DIR);
			
			System.out.println(codedFolderSTEG.getName()+" has "+codedFolderSTEG.listFiles().length+"bytes");
			
			assertTrue(screepTManagerFof.decrYpt(CryptoMode.COMPLETE, srcFile, resultDir, AES_KEY, keyIMG, false));

			TestUtils.printl("TEMP_RESULTS_PATH -> " + screepTManagerFof.screepTManager.TEMP_PATH);
			
			for(File result : resultDir.listFiles()) {
				// destination path - avoid duplicates
				File out = new File("TEST_FILES/" + result.getName());
				if (out.exists() && out.isDirectory()) {
					out.delete();
				}

				out = ScreeptUtils.renameJavaObjFile(out);
				assertTrue(result.renameTo(out));
				//assertTrue(result.isDirectory());
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
