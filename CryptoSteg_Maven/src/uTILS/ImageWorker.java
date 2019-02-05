package uTILS;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import gUI.panels.ImageGenContainerPanel;

public class ImageWorker {

	// ImageWorker logger creation
	static Logger loGGer = Logger.getLogger(ImageWorker.class);

	//////////////////////
	// FIELDS //
	//////////////////////
	public final static String[] SUPPORTED_EXTENSIONS = ImageIO.getWriterFileSuffixes();

	public final static int MAX_WIDTH_GEN = 4000;
	public final static int MAX_HEIGHT_GEN = 4000;

	private BufferedImage image;
	private File imageFile;

	//////////////////////
	// CONSTRUCTOR // NB il file sorgente deve esistere, altrimenti lancia
	////////////////////// eccezione!
	//////////////////////

	// nascosto perch� pu� dare errori, dovrei controllare
	// che siano valorizzati i campi prima di invocare i metodi
	//
	// public ImageWorker() {
	// }

	public ImageWorker(String imgPath) throws IOException {
		imageFile = new File(imgPath);
		image = ImageIO.read(imageFile);
	}

	public ImageWorker(File imgFile) throws IOException {
		imageFile = imgFile;
		image = ImageIO.read(imgFile);
	}

	public ImageWorker(BufferedImage img) {
		image = img;
	}

	//////////////////////
	// METHODS //
	//////////////////////

	public int Height() {
		return image.getHeight();
	}

	public int Width() {
		return image.getWidth();
	}

	/**
	 * Metodo statico che, dato il nome (String) del file dell'immagine e il logger
	 * ritorna la matrice di interi x pixel (getRGB(x,y))
	 */
	public static Integer[][] readPxValue(String imgPath, File logger) throws IOException {

		// inizializzo variabili necessarie globali
		BufferedImage img = null;
		File fileImg = null;
		FileWriter writer;

		fileImg = new File(imgPath);
		img = ImageIO.read(fileImg);

		// se file di log non esiste lo creo
		if (!logger.exists()) {
			logger.createNewFile();
		}
		// associo il file e abilito la sovrascrittura
		writer = new FileWriter(logger, false);

		// calcolo larghezza e altezza immagine
		int height = img.getHeight();
		int width = img.getWidth();

		// matrice rappresentante gli interi getRGB(x,y) -> � il valore del pixel
		Integer[][] imgPixels = new Integer[width][height];

		// ciclo di scansione immagine

		// per ogni riga
		for (int i = 0; i < height; i++) {

			// per ogni colonna
			for (int j = 0; j < width; j++) {
				Integer px = img.getRGB(j, i);
				imgPixels[j][i] = px;
				loGGer.debug("[X=" + j + " ; Y=" + i + "] Value: " + px + " ");
				writer.write("[X=" + j + " ; Y=" + i + "] Value: " + px + " ");
			}

		}

		// chiudo il writer per non sprecare memoria
		writer.close();

		return imgPixels;
	}

	/**
	 * Metodo statico che, dato il file dell'immagine e il logger ritorna la matrice
	 * di interi x pixel (getRGB(x,y))
	 */
	public static Integer[][] readPxValue(File imgFile, File logger) throws IOException {

		// inizializzo variabili necessarie globali
		BufferedImage img = null;
		FileWriter writer;
		// creo variabile immagine
		img = ImageIO.read(imgFile);

		// se file di log non esiste lo creo
		if (!logger.exists()) {
			logger.createNewFile();
		}
		// associo il file e abilito la sovrascrittura
		writer = new FileWriter(logger, false);

		// calcolo larghezza e altezza immagine
		int height = img.getHeight();
		int width = img.getWidth();

		// matrice rappresentante gli interi getRGB(x,y) -> � il valore del pixel
		Integer[][] imgPixels = new Integer[width][height];

		// ciclo di scansione immagine

		// per ogni riga
		for (int i = 0; i < height; i++) {

			// per ogni colonna
			for (int j = 0; j < width; j++) {
				Integer px = img.getRGB(j, i);
				imgPixels[j][i] = px;
				loGGer.debug("[X=" + j + " ; Y=" + i + "] Value: " + px + " ");
				writer.write("[X=" + j + " ; Y=" + i + "] Value: " + px + " ");
			}
		}

		// chiudo il writer per non sprecare memoria
		writer.close();

		return imgPixels;
	}

	/**
	 * @param File
	 *            logger : this variable will contain file rapresentation Metodo con
	 *            parametro logger che ritorna la matrice di interi x pixel
	 *            (getRGB(x,y))
	 */
	public Integer[][] readPxValueAndLog(File logger) throws IOException {

		FileWriter writer;

		// se file di log non esiste lo creo
		if (!logger.exists()) {
			logger.createNewFile();
		}
		// associo il file e abilito la sovrascrittura
		writer = new FileWriter(logger, false);

		// calcolo larghezza e altezza immagine
		int height = image.getHeight();
		int width = image.getWidth();

		// matrice rappresentante gli interi getRGB(x,y) -> � il valore del pixel
		Integer[][] imgPixels = new Integer[width][height];

		// ciclo di scansione immagine

		// per ogni riga
		for (int i = 0; i < height; i++) {

			writer.write("###################\n");

			// per ogni colonna
			for (int j = 0; j < width; j++) {
				Integer px = image.getRGB(j, i);
				imgPixels[j][i] = px;
				loGGer.debug("[X=" + j + " ; Y=" + i + "] Value: " + px + " ");
				writer.write("[X=" + j + " ; Y=" + i + "] Value: " + px + " ");
			}

			writer.write("\n");
		}
		// chiudo il writer per non sprecare memoria
		writer.close();
		return imgPixels;
	}

	/**
	 * Metodo senza parametri che ritorna la matrice di interi x pixel (getRGB(x,y))
	 */
	public Integer[][] readPxValue() throws IOException {

		// calcolo larghezza e altezza immagine
		int height = image.getHeight();
		int width = image.getWidth();

		// matrice rappresentante gli interi getRGB(x,y) -> � il valore del pixel
		Integer[][] imgPixels = new Integer[width][height];

		// ciclo di scansione immagine

		// per ogni riga
		for (int i = 0; i < height; i++) {

			// per ogni colonna
			for (int j = 0; j < width; j++) {
				Integer px = image.getRGB(j, i);
				imgPixels[j][i] = px;
				loGGer.debug("[X=" + j + " ; Y=" + i + "] Value: " + px + " ");
			}
		}
		return imgPixels;
	}

	/**
	 * Metodo senza parametri che setta il valore RGB dell'immagine di destinazione
	 * (source.getRGB(x,y))
	 */
	public BufferedImage writePxValue(BufferedImage srcImg, int bufferedImageType) throws IOException {

		// calcolo larghezza e altezza immagine
		int height = srcImg.getHeight();
		int width = srcImg.getWidth();

		BufferedImage resultImg = new BufferedImage(width, height, bufferedImageType);

		// ciclo di scansione immagine
		// per ogni riga
		for (int i = 0; i < height; i++) {

			// per ogni colonna
			for (int j = 0; j < width; j++) {
				resultImg.setRGB(j, i, srcImg.getRGB(j, i));
			}
		}
		return resultImg;
	}

	/**
	 * metodo che crea un'immagine casuale di i*j pixel e aggiorna la progress bar
	 * passata come parametro
	 * 
	 * @param progressBar
	 *            this is the bar to update
	 * 
	 * @return BufferedImage keyImage
	 */
	public static synchronized BufferedImage generaKeyImage(int width, int height, int imgType, ImageGenContainerPanel container ) {
		// use secure random for more entrophy on the key
		SecureRandom random;
		
		try {
			SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
		}
		random = new SecureRandom();

		if (width > MAX_WIDTH_GEN) {
			width = MAX_WIDTH_GEN;
		}
		if (height > MAX_HEIGHT_GEN) {
			height = MAX_HEIGHT_GEN;
		}
				
		BufferedImage generatedKeyimg = new BufferedImage(width, height, imgType);

		int pixelRGBValue = 0;

		for (int i = 0; i < height; i++) {
			// for each row update progress bar with %
			container.getProgUpdater().getPb().setValue((int)(i/(float) height*100));
			
			for (int j = 0; j < width; j++) {
				// per ogni colonna
				pixelRGBValue = random.nextInt(Integer.MAX_VALUE);
				generatedKeyimg.setRGB(j, i, pixelRGBValue);
			}
		}
	   	loGGer.debug("Key generated!");
		return generatedKeyimg;
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
				pixelRGBValue = random.nextInt(Integer.MAX_VALUE);
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
				pixelRGBValue = random.nextInt(Integer.MAX_VALUE);
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

	/**
	 * this method resize an image to the given wanted dimensions keeping the same
	 * width/height ratio. returns a BufferedImage resized
	 * 
	 * @param image
	 * @param imageType
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	public static BufferedImage scaleImage(BufferedImage image, int imageType, int newWidth, int newHeight) {
		// Make sure the aspect ratio is maintained, so the image is not distorted
		double thumbRatio = (double) newWidth / (double) newHeight;
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		double aspectRatio = (double) imageWidth / (double) imageHeight;

		if (thumbRatio < aspectRatio) {
			newHeight = (int) (newWidth / aspectRatio);
		} else {
			newWidth = (int) (newHeight * aspectRatio);
		}

		// Draw the scaled image
		BufferedImage newImage = new BufferedImage(newWidth, newHeight, imageType);
		Graphics2D graphics2D = newImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(image, 0, 0, newWidth, newHeight, null);

		return newImage;
	}

	/**
	 * this method generate a random color of the type argb
	 * 
	 * @return
	 */
	public static Color randomColorRGB() {
		Random rand = new Random();
		float r = rand.nextFloat() / 1f;
		float g = rand.nextFloat() / 1f;
		float b = rand.nextFloat() / 1f;
		float a = rand.nextFloat();
		if (a < 0.4) {
			// guarantee opaque colors
			a += 0.5;
		}

		return new Color(r, g, b, a);
	}

	/**
	 * this method is used to convert an image in another format and write it on
	 * file. It can manipulate image output type, algorithm, and output extension.
	 */
	public static boolean convertAndWrite(BufferedImage srcImage, String outExtension, File destination) {

		try (OutputStream os = new FileOutputStream(destination)) {
			ImageIO.write(srcImage, outExtension, os);
			os.close();
			return true;
		} catch (Exception exp) {
			return false;
		}
	}
	
	/**
	 * method to clone BufferedImage by reference
	 * 
	 * @param source image to clone
	 * 
	 * @return the cloned BufferedImage
	 */
	public static BufferedImage copyImage(BufferedImage source){
	    BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
	    Graphics g = b.getGraphics();
	    g.drawImage(source, 0, 0, null);
	    g.dispose();
	    return b;
	}
	
	/**
	 * This method compress an image with given params and returns the compressed image as byte array
	 * 
	 * @param sourceImage the image to be compressed
	 * @param extension is the string extension for the output bytes (loseless modes as "png" do not lose information)
	 * @param factor the compression factor between 0.0 and 1.0
	 * 
	 * @author Alessio Moraschini
	 */
	public static byte[] compressIMG(BufferedImage sourceImage, String extension, float factor) throws UnsupportedOperationException {
		
		ByteArrayOutputStream compressed = new ByteArrayOutputStream();
			
		try {
			// intialize ImageStream to byteArrayStream
			ImageOutputStream outputStream = ImageIO.createImageOutputStream(compressed);
			
			// initialize type use it to create outWriter
			ImageTypeSpecifier type = ImageTypeSpecifier.createFromBufferedImageType(sourceImage.getType());
			ImageWriter writer = ImageIO.getImageWriters(type, extension).next();
			
			// calculate param for compression
			ImageWriteParam param = writer.getDefaultWriteParam();
			if (param.canWriteCompressed()) {
				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				param.setCompressionQuality(factor);
			}else {
				// free used in-memory structures
				compressed.flush();
				outputStream.flush();
				outputStream.close();
				writer.dispose();
				throw new UnsupportedOperationException();
			}
			// now write image to in-memory buffer
			writer.setOutput(outputStream);
			writer.write(null, new IIOImage(sourceImage, null, null), param);
			
			// free used in-memory structures
			outputStream.close();
			writer.dispose();
			
		} catch (IOException e) {
			loGGer.error("Exception happened!", e);
		}catch (IllegalArgumentException e) {
			loGGer.error("Error ", e);
		}
	
		return compressed.toByteArray();
	}
	
	/**
	 * This method compress an image with given params and returns the compressed image as byte array
	 * 
	 * @param sourceImage the image to be compressed
	 * @param extension is the string extension for the output bytes (loseless modes as "png" do not lose information)
	 * @param factor the compression factor between 0.0 and 1.0
	 * @param keepMetadata true if want to keep metadata, false to discard metadata
	 * 
	 * @author Alessio Moraschini
	 * 
	 * @throws IOException 
	 */
	public static byte[] compressIMG(File sourceImageFile, String extension, float factor, boolean keepMetadata) throws IOException, UnsupportedOperationException {
		
		ByteArrayOutputStream compressed = new ByteArrayOutputStream();
		BufferedImage srcImage = null;
		IIOMetadata metadata = null;
		
		if(keepMetadata) {
			try {
				ImageInputStream in = ImageIO.createImageInputStream(sourceImageFile);
				srcImage = ImageIO.read(sourceImageFile);
				ImageReader readerImg = ImageIO.getImageReadersByFormatName(FilenameUtils.getExtension(sourceImageFile.getName())).next();
				readerImg.setInput(in, true, false);
				metadata = readerImg.getImageMetadata(0);
			} catch (IOException e) {
				loGGer.error("Exception retrieving image: "+sourceImageFile.getAbsolutePath(), e);
			}
		}else {
			srcImage = ImageIO.read(sourceImageFile);
		}
		
		try {
			// intialize ImageStream to byteArrayStream
			ImageOutputStream outputStream = ImageIO.createImageOutputStream(compressed);
			
			// initialize type use it to create outWriter
			ImageTypeSpecifier type = ImageTypeSpecifier.createFromBufferedImageType(srcImage.getType());
			ImageWriter writer = ImageIO.getImageWriters(type, extension).next();
			
			// calculate param for compression
			ImageWriteParam param = writer.getDefaultWriteParam();
			if (param.canWriteCompressed()) {
				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				param.setCompressionQuality(factor);
			}else {
				// free used in-memory structures
				outputStream.close();
				writer.dispose();
				throw new UnsupportedOperationException();
			}
			
			// now write image to in-memory buffer
			writer.setOutput(outputStream);
			writer.write(null, new IIOImage(srcImage, null, metadata), param);
			
			// free used in-memory structures
			compressed.flush();
			outputStream.flush();
			outputStream.close();
			writer.dispose();
			
		} catch (FileNotFoundException e) {
			loGGer.error("Exception happened!", e);
		}
	
		return compressed.toByteArray();
	}

	/////////////////////
	// GETTERS & SETTERS /
	//////////////////////

	public File getImageFile() {
		return imageFile;
	}

	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

}
