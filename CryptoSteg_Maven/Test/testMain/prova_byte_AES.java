package testMain;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

import org.ini4j.Ini;
import org.ini4j.Wini;

import crypt.aes.AesCipher;
import gUI.panels.KeyGenPanel;
import iNIT.INItializer;
import uTILS.local.FileStuff;
import uTILS.local.ImageWorker;

public class prova_byte_AES {
	
	public static void main(String[] args) {

		String psw_user = "";
		String msg = "Questo � un messaggio di prova";

		AesCipher cripter;
		try {
			cripter = new AesCipher(psw_user);
			if(cripter != null) {
				String result = cripter.encrypt(msg);
				System.out.println("Risultato codificato: \n\n" + result);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.out.println("!!!! -> Errore: Codifica non supportata! \n");
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			System.out.println("!!!! -> Errore: Padding non corretto! \n");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("!!!! -> Errore: Algoritmo non corretto! \n");
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			System.out.println("!!!! -> Errore: Dimensione blocco errata! \n");
			e.printStackTrace();
		} catch (BadPaddingException e) {
			System.out.println("!!!! -> Errore: Padding non corretto! \n");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("!!!! -> Errore generico! \n");
			e.printStackTrace();
		}
		
		// TEST WRITE IN DICTIONARY() - FileStuff static method
		String IMG = "keygens";
//		File result = FileStuff.addKeyImgToDictionary(IMG, ImageWorker.generaKeyImage(100, 1000, BufferedImage.TYPE_4BYTE_ABGR));
//		 System.out.println("esit: "+result+" - "+result.getName()+" - "+result.length()/1024.0+" KB");

		//  TEST IMAGEWORKER
//		
//		try {
//			//File output = new File("IMG/OUT.txt");
//			//File imgCasuale = new File("IMG/keygen.jpg");
//			File IMG = new File("IMG/keygen.png");
//			IMG.createNewFile();
//			ImageWorker workImg = new ImageWorker(IMG);
//			//ImageWorker workImg2 = new ImageWorker("IMG/image.jpg");
//			//Integer[][] pixels = ImageWorker.readPxValue("IMG/image.jpg", output);
//
//			workImg.setImage(workImg.generaKeyImageAndWrite(4000, 4000, BufferedImage.TYPE_INT_ARGB_PRE, IMG, "png"));
//			//System.out.println(imgCasuale.getAbsolutePath());
//			System.out.println("\nAltezza: "+workImg.getImage().getWidth()+" Px <=> Larghezza: "+workImg.getImage().getWidth()+" Px\n");
//			//System.out.println(workImg.getImageFile().equals(workImg2.getImageFile())); 
//			System.out.println("\n"+(workImg.getImageFile().length()/1024)
//			+" KB"); //KB size 
//			
//			workImg = new ImageWorker(IMG);
//			System.out.println("\nAltezza: "+workImg.getImage().getWidth()+" Px <=> Larghezza: "+workImg.getImage().getWidth()+" Px\n");
//		} 
//		catch (IOException e) { e.printStackTrace(); }
		
		
		// TEST INITIALIZER
		
//		INItializer INI = new INItializer();
//		try {
//			INI = new INItializer(new Wini(new File("INI_FILES/ciao.ini")));
//		} catch (IOException e) {
//			System.out.println("Errore nella lettura del file CONFIG.ini in /INI_FILES");
//		}
//		INI.readConfigIniFile();
//		
//		// prova modifica INI file
//		File prova = new File("INI_FILES/CONFIG2.ini");
//		prova = FileStuff.changeExtension(prova, ".xml");
//		System.out.println(prova.getName());
//		//INI.getAesOpt().setAesKey("BAUBAUMICIOMICIO");
//		INI.writeCurrentConfigIniFile();
//		
//		System.out.println(INI.toStringa());
//		File root_dir = new File ("INI_FILES/A");
//		FileStuff.upperCaseSubFolder(root_dir);
		
	}

}
