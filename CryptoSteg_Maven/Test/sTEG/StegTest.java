package sTEG;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;

import crypt.steg.StegCipher;
import media.ImageGeneralWorker;

public class StegTest {

	@Test
	public void testCryptByte_1() throws media.STEGException {
		int testTypeImg = BufferedImage.TYPE_3BYTE_BGR;
		
		ArrayList<Byte> sourceBytes = generateRandomByteList(400);
		BufferedImage generatedImage = ImageGeneralWorker.generaKeyImage(20, 20, testTypeImg);
		StegCipher STEG = new StegCipher(generatedImage, testTypeImg); 
		STEG.stegaCrypt(sourceBytes);
		
	}
	
	private ArrayList<Byte> generateRandomByteList(int lenght){
		ArrayList<Byte> generatedList = new ArrayList<Byte>();
		Random random = new Random();
		// calculate (length)-times random byte and assign to bytes with cast
		byte[] bytes = new byte[lenght];
		random.nextBytes(bytes);
		
		for(int i=0; i<lenght; i++ ) {
			Byte genByte = bytes[i];
			generatedList.add(genByte);
		}
		
		return generatedList;
	}
	
}
