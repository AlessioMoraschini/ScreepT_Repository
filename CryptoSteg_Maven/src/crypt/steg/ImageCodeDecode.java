package crypt.steg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

public class ImageCodeDecode {

	// ImageCodeDecode logger creation		
	static Logger logger = Logger.getLogger(ImageCodeDecode.class); 
	
	// DEFAULT VALUES
	public final int DEFAULT_IMG_TYPE = BufferedImage.TYPE_4BYTE_ABGR;
	
	//////////////////////
	//      FIELDS      //
	//////////////////////
	
	private BufferedImage keyImage;  // rappresenta la password di codifica/decodifica
	int outputType;
	
	//////////////////////
	//    CONSTRUCTORS  //   NB il file sorgente deve esistere, altrimenti lancia eccezione!
	//////////////////////
	
	// VIA File - CONSTRUCTORS
		/**
		 * this constructor needs a valid file image and use default img type set in this class.
		 * @param keyImg source image for coding
		 */
	public ImageCodeDecode(File keyImgFile) throws IOException {
		keyImage = ImageIO.read(keyImgFile);
		outputType = DEFAULT_IMG_TYPE;
	}
	
	/**
	 * this does the same as single param signature, but with specified output image type 
	 * @param keyImgFile the key used for coding
	 * @param outputImgType integer value (use BufferedImage.**** to get static values specified in BufferedImage class )
	 * @throws IOException thrown if file readin' has some problems
	 */
	public ImageCodeDecode(File keyImgFile, int outputImgType) throws IOException {
		keyImage = ImageIO.read(keyImgFile);
		outputType = outputImgType;
	}
	
	// VIA BufferedImage - CONSTRUCTORS
	/**
	 * this constructor needs a buffered image and use default img type set in this class.
	 * @param keyImg source image for coding
	 */
	public ImageCodeDecode(BufferedImage keyImg) {
		keyImage = keyImg;
		outputType = DEFAULT_IMG_TYPE;
	}
	
	/**
	 * this does the same as single param signature, but with specified output image type 
	 * @param keyImgFile the key used for coding
	 * @param outputImgType integer value (use BufferedImage.**** to get static values specified in BufferedImage class )
	 * @throws IOException thrown if file readin' has some problems
	 */
	public ImageCodeDecode(BufferedImage keyImg, int outputImgType) {
		keyImage = keyImg;
		outputType = outputImgType;
	}
	
	//////////////////////
	//      METHODS     //
	//////////////////////
	
	/**
	 * Questo metodo si occupa di codificare tramite algoritmo SteganoCrypt una lista di byte passata come parametro. 
	 * Il metodo si occupa di calcolare i px dell'immagine codedImg partendo dal corrispondente px della imageKey e dal
	 * valore dell'i-esimo byte prelevato dalla lista.
	 * 
	 * @param List<Byte> AES_CODED_OUT  -> lista di byte data dai byte originali codificati tramite AES
	 * @return BufferedImage codedImg  -> immagine risultante dalla codifica
	 * */
	public BufferedImage stegaCrypt(ArrayList<Byte> AES_CODED_OUT) {
		
		Iterator<Byte> iter = AES_CODED_OUT.iterator();
		
		BufferedImage sourceImg = keyImage;
		BufferedImage codedImg = new BufferedImage(keyImage.getWidth(), keyImage.getHeight(), keyImage.getType());
		
		// calcolo larghezza e altezza immagine
        int height = sourceImg.getHeight();
	    int width = sourceImg.getWidth();
	    
	    // ciclo di scansione immagine
	    Integer currentByteToInt;
	    // per ogni riga
	    for(int i=0; i < height;i++) { 

	    	// per ogni colonna
	    	for(int j=0; j<width;j++){ 
	    		currentByteToInt = 0;
	    		Integer px = sourceImg.getRGB(j, i);
	    		// secure against overflow
	    		Integer securePx = secForConversion(px);
	    		if(iter.hasNext()) {
		    		currentByteToInt = (int)iter.next().byteValue();
	    		}
	    		// set new value - core of algorithm
	    		Integer codedPX = securePx - currentByteToInt;
	    		codedImg.setRGB(j, i, codedPX);
	    		//logger.debug("\n   =>  pxRGB: "+px+" - Secured pixel: "+securePx+"\n"+"-> Px: "+j+" X "+i+ " - SRC: "+px+" : byte: " + currentByteToInt + " => CODED = [" + codedPX+" ]");
	    	}
	    }
	    // end of image
//	    return imgPixels;		
		
		return codedImg;
	}
	
	/**
	 * Questo metodo si occupa di decodificare l'immagine codificata a partire dalla imageKey (campo della classe).
	 * 
	 * @param BufferedImage codedImg -> immagine codificata da stegaCrypt da decodificare
	 * @return List<Byte> AES_CODED_IN -> lista di byte prodotta in uscita
	 */
	public ArrayList<Byte> stegaDeCrypt(BufferedImage codedImg) {
		ArrayList<Byte> stegDecodedAesIn = new ArrayList<Byte>();
		
		// olny if img and key have same dimension (first level check)
		if (keyImage.getHeight()==codedImg.getHeight() && keyImage.getWidth()==codedImg.getWidth()) {
			// calcolo larghezza e altezza immagine
	        int height = codedImg.getHeight();
			int width = codedImg.getWidth();
			// ciclo di scansione immagine
			// per ogni riga
		    for(int i=0; i < height;i++) { 

		    	// per ogni colonna
		    	for(int j=0; j<width;j++){ 
		    		Integer currentKeyPx = keyImage.getRGB(j, i);
		    		// redo secure against overflow like when coding
		    		Integer secureKeyPx = secForConversion(currentKeyPx);
		    		Integer codedPx = codedImg.getRGB(j, i);	    		
		    		
		    		// retrieve original int value from key and coded values - core of algorithm
		    		int decodedPX = secureKeyPx - codedPx;
		    		
		    		// TODO int to Byte
		    		Byte decodedLastEightBits = (byte)(decodedPX);
		    		stegDecodedAesIn.add(decodedLastEightBits);
		    	}
		    }
		    // end of image
	        //return imgPixels;		
		}		
		return stegDecodedAesIn;
	}
	
	/**
	 * This method is used locally to secure that a integer value is suitable to be
	 * used as start value for encryption process. This done by ensuring that
	 * a byte can be summed or subtraced without overflow, that would cause
	 * inability to decrypt!
	 * 
	 * @param val this is the integer value to be sured before encryption
	 * 
	 * @return secured value redy to be used for encryption
	 */
	private Integer secForConversion(Integer val) {
		Integer upLim = Integer.MAX_VALUE - Byte.MAX_VALUE;
		Integer downLim = Integer.MIN_VALUE - Byte.MIN_VALUE;
		if(val > upLim) {
			val = upLim;
		}else if(val < downLim) {
			val = downLim;
		}
		return val;
	}

	//////////////////////
	// GETTERS & SETTERS /
	//////////////////////
	
	public void setKeyImage(BufferedImage keyImage) {
		this.keyImage = keyImage;
	}

	public int getOutputType() {
		return outputType;
	}

	public void setOutputType(int outputType) {
		this.outputType = outputType;
	}

	public BufferedImage getKeyImage() {
		return keyImage;
	}

	public int getDEFAULT_IMG_TYPE() {
		return DEFAULT_IMG_TYPE;
	}	
	
}
