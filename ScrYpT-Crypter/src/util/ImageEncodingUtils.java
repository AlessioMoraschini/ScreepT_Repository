package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.imageio.ImageIO;

import crypt.steg.STEGException;

public class ImageEncodingUtils {
	
	private final static int MAX_INT = Integer.MAX_VALUE;

	public final static int MAX_WIDTH_GEN = 4000;
	public final static int MAX_HEIGHT_GEN = 4000;

	public ImageEncodingUtils() {
		
	}


	/**
	 * metodo che estrae una porzione di immagine
	 */
	public static BufferedImage getPortion(BufferedImage src, int x, int y) {

		if(x<=src.getWidth() && y<=src.getHeight()) {
			BufferedImage portion = src.getSubimage(0, 0, x, y);
			return portion;
		}else {
			return null;
		}
		
	}
	
	/**
	 * Questo metodo restituisce un array contenente il valore dei pixel ottenuti scansionando l'immagine orizzontalmente, distanziati dall'intervallo fornito
	 * iN QUESTO CASO L'INTERVALLO è CALCOLATO AUTOMATICAMENTE A SECONDA DEL VALORE DEL PRIMO PX
	 * @param image
	 * @param N E' il numero di pixel da estrarre. Può essere anche maggiore del numero di pixel contenuti nell'immagine
	 * @return array di interi rappresentanti il valore dei pixel scansionati
	 * @throws STEGException 
	 */
	public static int[] readNSpacedValuesFromImage(BufferedImage image, int N) throws STEGException {
		int H=0;
		int W=0;
		int interval=1;
		if (image!=null && N>0) {
			H = image.getHeight();
			W = image.getWidth();
			interval = Math.abs((image.getRGB(0, 0))+1)%W;
			if(interval == 0) {
				interval = (W/N)%W;
			}
		}else throw new STEGException();
		
		if(N<=(W*H)) {
			int[] read = new int[N];
			int row=0;
			int col=0;
			for(int i = 0; i<N; i++) {
				if(row>H-1) {
					row=0;
					col++;
				}
				if(col>W-1) {
					col=0;
					row++;
				}
				
				read[i] = image.getRGB(col, row);
				col += interval%W;
			}
			return read;
		}else {
			return null;
		}
	}
	
	/**
	 * metodo che crea un'immagine casuale di i*j pixel
	 * 
	 * @return BufferedImage keyImage
	 */
	public static synchronized BufferedImage generaKeyImage(int width, int height, int imgType) {

		if (width > MAX_WIDTH_GEN) {
			width = MAX_WIDTH_GEN;
		}
		if (height > MAX_HEIGHT_GEN) {
			height = MAX_HEIGHT_GEN;
		}
		BufferedImage generatedKeyimg = new BufferedImage(width, height, imgType);
		// use secure random for more entrophy on the key
		SecureRandom random;
		try {
			SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
		}
		random = new SecureRandom();
		int pixelRGBValue = 0;
		// find percentual of progress work done
		// perc= 1.0f / (float)height * 100.0f ;
		// bar.setValue(Math.round(perc));
		// per ogni riga
		for (int i = 0; i < height; i++) {
			// per ogni colonna
			for (int j = 0; j < width; j++) {
				pixelRGBValue = random.nextInt(MAX_INT)-random.nextInt(MAX_INT);
				generatedKeyimg.setRGB(j, i, pixelRGBValue);
			}
		}
		return generatedKeyimg;
	}

	/**
	 * metodo che crea un'immagine casuale di i*j pixel e la salva nel file
	 * specificato come parametro
	 * 
	 * @return null : in caso di errore ritorna null
	 * 
	 * @param extension
	 *            of the file of the type ".jpg" or others formats
	 * @return BufferedImage keyImage
	 * @throws IOException
	 */
	public synchronized static BufferedImage generaKeyImageAndWrite(int width, int height, int imgType,
			File destinationFile, String imgExtension) throws IOException {

		BufferedImage generatedKeyimg = new BufferedImage(width, height, imgType);

		// use secure random for more entrophy on the key
		SecureRandom random;
		try {
			SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
		}
		random = new SecureRandom();

		int pixelRGBValue = 0;

		// per ogni riga
		for (int i = 0; i < height; i++) {
			// per ogni colonna
			for (int j = 0; j < width; j++) {
				pixelRGBValue = random.nextInt(MAX_INT)-random.nextInt(MAX_INT);
				generatedKeyimg.setRGB(j, i, pixelRGBValue);

			}
		}
		if (!destinationFile.exists()) {
			if (!destinationFile.getParentFile().exists()) {
				destinationFile.getParentFile().mkdirs();
			}
			destinationFile.createNewFile();
		}
		boolean writeEsit = ImageIO.write(generatedKeyimg, imgExtension, destinationFile);
		// in caso di errore ritorna null
		if (!writeEsit) {
			generatedKeyimg = null;
		}
		return generatedKeyimg;
	}

}
