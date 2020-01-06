package sTEG;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;

import crypt.aes.AesCipher;
import crypt.steg.STEGException;
import crypt.steg.StegCipher;

public class AesStegTest {

	public static void main(String[] args) throws IOException, STEGException {
		char[] key= "ST3G4N0cr1p74009".toCharArray();
		String srcText = "Ciao, sono un testo di prova";
		String coded = srcText;
		String decoded = "not yet decoded :(";
				
		try {
			AesCipher crypter=new AesCipher(key);
			coded = crypter.encrypt(srcText);
		
			byte[] codedBytes = coded.getBytes();
			ArrayList<Byte> byteList = new ArrayList<Byte>();
	
			// translate byte[] to ArrayList<Byte>
			for(byte currentByte : codedBytes) {
				byteList.add(currentByte);
			}
			
			StegCipher imgCoder = new StegCipher(new File("IMG/KEY_DICTIONARY/small_agbr.png"));
			BufferedImage keyImg = imgCoder.getKeyImage();
			
			BufferedImage result = imgCoder.stegaCrypt(byteList);
			File dest = new File("IMG/file.png");
			dest.delete();
			ImageIO.write(result, "png", dest);
			System.out.println("Coded Image:" + result.toString());
			
			ArrayList<Byte> decodedList = new ArrayList<Byte>();
			imgCoder = new StegCipher(new File("IMG/KEY_DICTIONARY/small_agbr.png"));
			decodedList = imgCoder.stegaDeCrypt(result);
	
			byte[] decodedBytes = new byte[decodedList.size()];
			int i = 0;
			for(Byte currentByte : decodedList) {
				decodedBytes[i] = currentByte;
				i++;
			}
			i=0;
			decoded = new String(decodedBytes);
			System.out.println("\n Steg Decoded image = aes coded input = "+decoded);
			
			decoded = crypter.decrypt(decoded);
			System.out.println("\n AES final Decoded bytes = aes coded input = "+decoded);
		
		} catch (UnsupportedEncodingException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
			e.printStackTrace();
		}
	}

}
