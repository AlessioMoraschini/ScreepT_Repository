package crypt.steg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import gUI.om.BufferedImageType;

/**
 * this class represent the coding/decoding of source byte list or array, implementing simil-steganography
 * algorithm designed to be concatenated with aes result bytes or with raw bytes, depending on user needs :) 
 * 
 * @author Alessio Moraschini, 2018 Dalmine
 *
 */
public class ImageCodeDecode {
	
	private final int MAX_INT = Integer.MAX_VALUE;
	private final int MIN_INT = Integer.MIN_VALUE;
	
	Integer upLim = MAX_INT - 16777216; // max - 2^24
	Integer downLim = MIN_INT + 16777216; // min+2^24

	// ImageCodeDecode logger creation		
	static Logger logger = Logger.getLogger(ImageCodeDecode.class); 
	
	// DEFAULT VALUES
	public final int DEFAULT_IMG_TYPE = BufferedImage.TYPE_4BYTE_ABGR;
	
	//////////////////////
	//      FIELDS      //
	//////////////////////
	
	public BufferedImage keyImage;  // rappresenta la password di codifica/decodifica
	int outputType;
	// available bytes that's possible to insert in every pixel, depends on imgType (color model)
	private int bytePerPix;
	
	//////////////////////
	//    CONSTRUCTORS  //   NB il file sorgente deve esistere, altrimenti lancia eccezione!
	//////////////////////
	
	// VIA File - CONSTRUCTORS
	/**
	 * this constructor needs a valid file image and use default img type set in this class.
	 * @param keyImg source image for coding
	 * @throws STEGException 
	 */
	public ImageCodeDecode(File keyImgFile) throws IOException, STEGException {
		keyImage = ImageIO.read(keyImgFile);
		outputType = keyImage.getType();
		bytePerPix = (keyImage.getColorModel().getPixelSize()/8)-1;
		if(!BufferedImageType.checkIfAdmittedType(outputType)) {
			throw new STEGException();
		}
	}
	
	/**
	 * this constructor is meant to create a new instance by reference
	 * from an existing imgCoder, without affecting the original when created one changes
	 * @param original
	 * @throws STEGException 
	 */
	public ImageCodeDecode (ImageCodeDecode original) throws STEGException {
		this.keyImage = original.keyImage;
		this.outputType = keyImage.getType();
		bytePerPix = (keyImage.getColorModel().getPixelSize()/8)-1;
		if(!BufferedImageType.checkIfAdmittedType(outputType)) {
			throw new STEGException();
		}
	}
	
	/**
	 * this does the same as single param signature, but with specified output image type 
	 * @param keyImgFile the key used for coding
	 * @param outputImgType integer value (use BufferedImage.**** to get static values specified in BufferedImage class )
	 * @throws IOException thrown if file readin' has some problems
	 * @throws STEGException 
	 */
	public ImageCodeDecode(File keyImgFile, int outputImgType) throws IOException, STEGException {
		keyImage = ImageIO.read(keyImgFile);
		outputType = outputImgType;
		bytePerPix = (keyImage.getColorModel().getPixelSize()/8)-1;
		if(!BufferedImageType.checkIfAdmittedType(outputType)) {
			throw new STEGException();
		}
	}
	
	// VIA BufferedImage - CONSTRUCTORS
	/**
	 * this constructor needs a buffered image and use default img type set in this class.
	 * @param keyImg source image for coding
	 * @throws STEGException 
	 */
	public ImageCodeDecode(BufferedImage keyImg) throws STEGException {
		keyImage = keyImg;
		outputType = keyImage.getType();
		bytePerPix = (keyImage.getColorModel().getPixelSize()/8)-1;
		if(!BufferedImageType.checkIfAdmittedType(outputType)) {
			throw new STEGException();
		}
	}
	
	/**
	 * this does the same as single param signature, but with specified output image type 
	 * @param keyImgFile the key used for coding
	 * @param outputImgType integer value (use BufferedImage.**** to get static values specified in BufferedImage class )
	 * @throws STEGException 
	 * @throws IOException thrown if file readin' has some problems
	 */
	public ImageCodeDecode(BufferedImage keyImg, int outputImgType) throws STEGException {
		keyImage = keyImg;
		outputType = outputImgType;
		bytePerPix = (keyImage.getColorModel().getPixelSize()/8)-1;
		if(!BufferedImageType.checkIfAdmittedType(outputType)) {
			throw new STEGException();
		}
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
	 * Questo metodo si occupa di codificare tramite algoritmo SteganoCrypt una lista di byte passata come parametro. 
	 * Il metodo si occupa di calcolare i px dell'immagine codedImg partendo dal corrispondente px della imageKey e dal
	 * valore dell'i-esimo byte prelevato dall'array di byte sorgente.
	 * 
	 * @param AES_CODED_OUT  -> array di byte data dai byte originali codificati tramite AES
	 * @return BufferedImage codedImg  -> immagine risultante dalla codifica
	 * */
	public StegOmResult stegaCrypt(byte[] AES_CODED_OUT) {
				
		BufferedImage sourceImg = keyImage;
		BufferedImage codedImg = new BufferedImage(keyImage.getWidth(), keyImage.getHeight(), keyImage.getType());
		Integer nBytesCoded = 0;
		// calcolo larghezza e altezza immagine
        int height = sourceImg.getHeight();
	    int width = sourceImg.getWidth();
	    
	    // ciclo di scansione immagine
	    int currentByteToInt;
	    
	    int k = 0;
	    
	    // per ogni riga
	    for(int i=0; i < height;i++) { 

	    	// per ogni colonna
	    	for(int j=0; j<width;j++){ 
	    		
	    		Integer px = sourceImg.getRGB(j, i);
	    		// secure against overflow
	    		Integer securePx = secForConversion(px);
	    		
	    		if(k<AES_CODED_OUT.length) {
		    		currentByteToInt = (int)AES_CODED_OUT[k];
	    			// if source file is ended remember number of coded (k+1 because k starts from 0 being an index instead of a counter)
	    			nBytesCoded = k+1; 
	    		}
	    		else {
	    			currentByteToInt = 0;
	    		}
	    		// set new value - core of algorithm
	    		Integer codedPX = securePx - currentByteToInt;
	    		codedImg.setRGB(j, i, codedPX);
	    		//logger.debug("\n   =>  pxRGB: "+px+" - Secured pixel: "+securePx+"\n"+"-> Px: "+j+" X "+i+ " - SRC: "+px+" : byte: " + currentByteToInt + " => CODED = [" + codedPX+" ]");
	    		
	    		k++;
	    	}
	    }
	    String strBytesCoded = String.valueOf(nBytesCoded);
	    // end of image
//	    return imgPixels;		
		
		return new StegOmResult(codedImg, strBytesCoded);
	}
	
	/**
	 * Questo metodo si occupa di codificare tramite algoritmo SteganoCrypt un array di byte passato come parametro. 
	 * Il metodo si occupa di calcolare i px dell'immagine codedImg partendo dal corrispondente px della imageKey e dal
	 * valore dell'i-esimo byte prelevato dall'array di byte sorgente. A differenza degli altri metodi riconosce in base al tipo di immagine
	 * sceglie se usare 1,2, o 3 byte dal sorgente per ogni pixel.
	 * 
	 * NB -> Per non perdere valori è necessario assicurarsi che la lunghezza dell'input sia contenibile nell'immagine chiave (length<(w*h*BytePerPx))
	 * 
	 * @param AES_CODED_OUT  -> array di byte data dai byte originali codificati tramite AES
	 * @return OmResult  -> oggetto contenente l'immagine risultante dalla codifica, e il numero di byte codificati.
	 * */
	public StegOmResult stegaCryptMultiByte(byte[] AES_CODED_OUT) {
		
		SecureRandom rand = new SecureRandom();
				
		logger.debug("Started decripting byte array of " + AES_CODED_OUT.length);

		BufferedImage codedImg = new BufferedImage(keyImage.getWidth(), keyImage.getHeight(), keyImage.getType());
		
		// initialize total coded bytes and initial available bytes from source
		Integer nBytesCoded = 0;
		Integer availableSrcBytes = AES_CODED_OUT.length;
		
		// calcolo larghezza e altezza immagine
        int height = keyImage.getHeight();
	    int width = keyImage.getWidth();
	    
	    // ciclo di scansione immagine
	    int currentByteToInt = 0;
	    
	    // represents the number of bytes effectively written at each cycle
	    int cycleN = 0;
	    
	    // per ogni riga
	    for(int i=0; i < height;i++) { 

	    	// per ogni colonna
	    	for(int j=0; j<width;j++){ 
	    		
	    		if(availableSrcBytes >= bytePerPix) {
	    			
	    			// in case It's possible to write max size bytes
	    			cycleN = bytePerPix;
	    			
	    			if(bytePerPix == 3) {
	    				
	    				currentByteToInt = threeBytesToIntShift(AES_CODED_OUT[nBytesCoded], AES_CODED_OUT[nBytesCoded+1], AES_CODED_OUT[nBytesCoded+2]);
	    			}else if(bytePerPix == 2) {
	    				
	    				currentByteToInt = threeBytesToIntShift((byte)0, AES_CODED_OUT[nBytesCoded], AES_CODED_OUT[nBytesCoded+1]);
	    			}else if(bytePerPix == 1) {
	    				
	    				currentByteToInt = threeBytesToIntShift((byte)0, (byte)0, AES_CODED_OUT[nBytesCoded]);
	    			}
	    			
	    		}else if(availableSrcBytes < bytePerPix) {
	    			
	    			// If there are less than max size
	    			cycleN = availableSrcBytes;
	    			
	    			if(availableSrcBytes == 2) {
	    				
	    				currentByteToInt = threeBytesToIntShift((byte)0, AES_CODED_OUT[nBytesCoded], AES_CODED_OUT[nBytesCoded+1]);
	    			}else if(availableSrcBytes == 1) {
	    				
	    				currentByteToInt = threeBytesToIntShift((byte)0, (byte)0, AES_CODED_OUT[nBytesCoded]);
	    			}else if(availableSrcBytes == 0){
	    				
	    				// if finished available bytes current value will be a random value (to not have clear keyImage pixel in result)
		    			currentByteToInt = rand.nextInt(MAX_INT)-rand.nextInt(MAX_INT);
		    		}
	    		}   		 
	    		
	    		// retrieve current pixel value from key Image
	    		Integer pxKey = keyImage.getRGB(j, i);
	    		// make it secure against overflow dividing unsigned by two <=> right shift one position
//	    		Integer securePxKey = secureRightShift(pxKey);
	    		Integer securePxKey = secForConversion(pxKey);
	    		
	    		Integer codedPX;
	    		// set new value - core of algorithm
	    		if(availableSrcBytes!=0) {
		    		codedPX = securePxKey - currentByteToInt;
	    		}else {
	    			codedPX = currentByteToInt;
	    		}
	    		codedImg.setRGB(j, i, codedPX);
	    		
	    		// update progressive N of coded bytes from source, and decrease by the same amount the available bytes
	    		nBytesCoded += cycleN;
	    		availableSrcBytes -= cycleN;
	    	}
	    }
	    String strBytesCoded = String.valueOf(nBytesCoded);
	    // end of image
//	    return imgPixels;		
		
		logger.debug("Finished -> Bytes decripted: "+nBytesCoded+" ; Residual bytes: "+availableSrcBytes+" (must be 0)");
	    
		return new StegOmResult(codedImg, strBytesCoded);
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
	 * Questo metodo si occupa di decodificare l'immagine codificata a partire dalla imageKey (campo della classe).
	 * 
	 * @param BufferedImage codedImg -> immagine codificata da stegaCrypt da decodificare
	 * @return List<Byte> AES_CODED_IN -> lista di byte prodotta in uscita
	 */
	public byte[] stegaDeCryptToArray(BufferedImage codedImg) {
		// calcolo larghezza e altezza immagine
        int height = codedImg.getHeight();
		int width = codedImg.getWidth();
		
		byte[] stegDecodedAesIn = new byte[height*width];
		
		// olny if img and key have same dimension (first level check)
		if (keyImage.getHeight()==codedImg.getHeight() && keyImage.getWidth()==codedImg.getWidth()) {
			
			int k = 0;
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
		    		
		    		// TODO return dinamyc size array avoiding to have null bytes (0)
		    		stegDecodedAesIn[k] = (byte)(decodedPX);
		    		k++;
		    	}
		    }
		    // end of image
	        //return imgPixels;		
		}		
		return stegDecodedAesIn;
	}
	
	/**
	 * Questo metodo si occupa di decodificare l'immagine codificata a partire dalla imageKey (campo della classe).
	 * 
	 * @param BufferedImage codedImg -> immagine codificata da stegaCrypt da decodificare
	 * @return La lista di byte prodotta in uscita
	 */
	public byte[] stegaDeCryptToArray(BufferedImage codedImg, Integer cryptedByteNumber) {
		// calcolo larghezza e altezza immagine
        int height = codedImg.getHeight();
		int width = codedImg.getWidth();
		
		byte[] stegDecodedAesIn = new byte[cryptedByteNumber];
		
		// olny if img and key have same dimension (first level check)
		if (keyImage.getHeight()==codedImg.getHeight() && keyImage.getWidth()==codedImg.getWidth()) {
			
			int k = 0;
			// ciclo di scansione immagine
			// per ogni riga
		    for(int i=0; i < height;i++) { 

		    	// per ogni colonna
		    	for(int j=0; j<width;j++){ 
		    		// execute i-cycle only if there are further bytes crypted in image
		    		if (k<cryptedByteNumber) {
			    		Integer currentKeyPx = keyImage.getRGB(j, i);
			    		// redo secure against overflow like when coding
			    		Integer secureKeyPx = secForConversion(currentKeyPx);
			    		Integer codedPx = codedImg.getRGB(j, i);	    		
			    		
			    		// retrieve original int value from key and coded values - core of algorithm
			    		int decodedPX = secureKeyPx - codedPx;
			    		
						// TODO return dinamyc size array avoiding to have null bytes (0)
						stegDecodedAesIn[k] = (byte) (decodedPX);
					}
					k++;
		    	}
		    }
		    // end of image
	        //return imgPixels;		
		}		
		return stegDecodedAesIn;
	}
	
	/**
	 * Questo metodo si occupa di decodificare tramite algoritmo SteganoCrypt un'immagine passata come parametro. 
	 * Il metodo si occupa di calcolare i px dell'immagine decodedImg partendo dal corrispondente px della codedImage e dal
	 * valore dell'i-esimo pixel della chiave. A differenza degli altri metodi riconosce in base al tipo di immagine
	 * sceglie se estrapolare 1,2, o 3 byte da ogni pixel.
	 * 
	 * NB -> il risultato è di lunghezza variabile tra 0 e w*h*codedImgType
	 * 
	 * @param BufferedImage codedImg -> immagine codificata da stegaCrypt da decodificare
	 * @param cryptedBytes -> il numero di bytes criptati in codifica
	 * 
	 * @return La lista di byte prodotta in uscita
	 * @throws STEGException 
	 * */
	public byte[] stegaDeCryptMultiByte(BufferedImage codedImg, Integer cryptedByteNumber) throws STEGException {
		
		logger.debug("Started decripting "+cryptedByteNumber+"from image with "+bytePerPix+" bytes for each pixel..." );
		
		int origLength = cryptedByteNumber;
		// calcolo larghezza e altezza dell'immagine codificata passata
        int height = codedImg.getHeight();
		int width = codedImg.getWidth();
		
		byte[] decodedBytes = new byte[cryptedByteNumber];
	    byte[] cycleBytes;
		
		int nBytesDeCoded = 0;
		
		// mi assicuro che chiave e result abbiano la stessa dimensione
	    if (keyImage.getHeight()==codedImg.getHeight() && keyImage.getWidth()==codedImg.getWidth()) {
			
	    	// per ogni riga
			for (int i = 0; i < height; i++) {

				// per ogni colonna
				for (int j = 0; j < width; j++) {

					// retrieve current pixel value from key Image
					Integer pxKey = keyImage.getRGB(j, i);
					// make it secure against overflow dividing unsigned by two <=> right shift one position
//					Integer securePxKey = secureRightShift(pxKey);
		    		Integer securePxKey = secForConversion(pxKey);

					Integer codedPx = codedImg.getRGB(j, i);
					
					// get decoded value - core of algorithm
					Integer deodedPX = securePxKey - codedPx;
					
					// get back integer into four bytes array for current pixel
					cycleBytes = retrieveBytesFromInt(deodedPX);
					
					// NOW GET BACK BYTES THAT ARE ENCODED INTO DECODED INTEGER:
					
					if (cryptedByteNumber >= bytePerPix) {

						if (bytePerPix == 3) {

							decodedBytes[nBytesDeCoded] = cycleBytes[1];
							decodedBytes[nBytesDeCoded+1] = cycleBytes[2];
							decodedBytes[nBytesDeCoded+2] = cycleBytes[3];
							nBytesDeCoded += 3;
							
						} else if (bytePerPix == 2) {

							decodedBytes[nBytesDeCoded] = cycleBytes[2];
							decodedBytes[nBytesDeCoded+1] = cycleBytes[3];
							nBytesDeCoded += 2;
							
						} else if (bytePerPix == 1) {

							decodedBytes[nBytesDeCoded] = cycleBytes[3];
							nBytesDeCoded ++;						}

					} else if (cryptedByteNumber < bytePerPix) {

						if (cryptedByteNumber == 2) {
							
							decodedBytes[nBytesDeCoded] = cycleBytes[2];
							decodedBytes[nBytesDeCoded+1] = cycleBytes[3];
							nBytesDeCoded += 2;
							
						} else if (cryptedByteNumber == 1) {

							decodedBytes[nBytesDeCoded] = cycleBytes[3];
							nBytesDeCoded ++;		
						}
					}

					// decrease initial coded bytes number from source the same as decoded bytes at this cycle
					cryptedByteNumber = origLength - nBytesDeCoded;
					
					// END PIXEL
				}// END LINE
			}// END ROW
			
			logger.debug("Bytes decripted: "+nBytesDeCoded);
		}else throw new STEGException(); // se dimensioni non corrispondono alla chiave
		
		return decodedBytes;
	}
	
	/**
	 * This method is used locally to secure that a integer value is suitable to be
	 * used as start value for encryption process. This done by ensuring that
	 * a byte can be summed or subtraced without overflow, that would cause
	 * inability to decrypt! [OLD version for one byte Per pixel!]
	 * 
	 * @param val this is the integer value to be sured before encryption
	 * 
	 * @return secured value redy to be used for encryption
	 */
	private Integer secForConversion(Integer val) {
		
		if(val > upLim) {
			val = upLim;
		}else if(val < downLim) {
			val = downLim;
		}
		return val;
	}	
	
	/**
	 * @return the 24 bytes integer(8 zeros and 24 bits) made by the three bytes in the order: [0,b1,b2,b3]
	 * So, result is wrapped in integer padding left with zeros (useful to avoid overflows in operations with integer retrieved from byte stream)
	 */
	public static int threeBytesToIntShift(byte b1, byte b2, byte b3) {
		int r = (b3 & 0xFF) | ((b2 & 0xFF) << 8) | ((b1 & 0xFF) << 16);
		return r;
	}
	
	/**
	 * @return the 16 bytes equivalent integer(16 zeros and 16 bits) made by the three bytes in the order: [0,0,b1,b2]. 
	 * So, result is wrapped in integer padding left with zeros (useful to avoid overflows in operations with integer retrieved from byte stream)
	 */
	// TODO da testare
	public static int twoBytesToIntShift(byte b1, byte b2) {
		int r = (b2 & 0xFF) | ((b1 & 0xFF) << 8) ;
		return r;
	}

	
	/**
	 * This method Extracts 4 bytes forming the given integer from left to right. 
	 * In bits it's equivalent to: [00000000 11111111 22222222 33333333], where the number is array index.
	 * @param source the integer to retrieve bytes from
	 * @return array of four bytes representing the integer.
	 */
	public static byte[] retrieveBytesFromInt(Integer source) {
		byte[] result = new byte[4];

		result[0] = (byte) ((source & 0xFF000000) >> 24);
		result[1] = (byte) ((source & 0x00FF0000) >> 16);
		result[2] = (byte) ((source & 0x0000FF00) >> 8);
		result[3] = (byte) ((source & 0x000000FF) >> 0);

		return result;
	}
	
	/**
	 * This method grant that src+N (where N<2^30) cannot suffer from overflow. Useful when src value is only random or not important to mantain intact
	 * @param src the source integer to secure
	 * @return src*2^(-1) = right shifted 
	 */
	public static int secureRightShift(int src) {
		return src >> 1;
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

	public int getBytePerPix() {
		return bytePerPix;
	}	
	
}
