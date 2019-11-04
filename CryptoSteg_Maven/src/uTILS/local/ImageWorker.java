package uTILS.local;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;


import amfileutils.FileStuff;
import gUI.frames.KeyGenFrame;
import gUI.panels.subpanels.ImageGenContainerPanel;

public class ImageWorker {

	// ImageWorker logger creation
	static Logger loGGer = Logger.getLogger(ImageWorker.class);

	//////////////////////
	// FIELDS //
	//////////////////////
	public final static int ORIZ_OPTION = 0; 
	public final static int VERT_OPTION = 1; 
	
	public final static String[] SUPPORTED_EXTENSIONS = ImageIO.getWriterFileSuffixes();
	
	private final static int MAX_INT = Integer.MAX_VALUE;
	private final static int MIN_INT = Integer.MIN_VALUE;
	SecureRandom rand;

	public final static int MAX_WIDTH_GEN = 4000;
	public final static int MAX_HEIGHT_GEN = 4000;

	private BufferedImage image;
	private File imageFile;

	//////////////////////
	// CONSTRUCTOR // NB il file sorgente deve esistere, altrimenti lancia
	////////////////////// eccezione!
	//////////////////////

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
				pixelRGBValue = random.nextInt(MAX_INT)-random.nextInt(MAX_INT);
				generatedKeyimg.setRGB(j, i, pixelRGBValue);
			}
		}
	   	loGGer.info("Key generated!");
		return generatedKeyimg;
	}
	
	/**
	 * 
	 * @param imageName is the name of the image to be written in the dictionary folder
	 * 			(too see that please look at FileStuff class) 
	 * @param img the image to write on file
	 * @return esit of the write operation
	 */
	public static File addKeyImgToDictionary(String imageName, BufferedImage img, KeyGenFrame parent){
				
//		updateProgressBar(20, progBar);
		boolean esit = false;
		File destFile = new File(KeyGenFrame.KEY_DICTIONARY+imageName);
	
		destFile = FileStuff.changeExtension(destFile, ".png");
		// if directory does not exist create it
		
		if (!destFile.exists()) {
			esit = destFile.mkdirs();
			try {
//				updateProgressBar(30, progBar);
				esit = ImageIO.write(img, "png", destFile);
				loGGer.debug("Image written: "+destFile.getAbsolutePath());
			} catch (IOException e) {
				loGGer.error("Exception happened!", e);
			}
			if (esit) {
				loGGer.debug(destFile.getAbsolutePath() + " Created!");
			}
//			updateProgressBar(80, progBar);
		} else if(destFile.exists()) {
			int choice = JOptionPane.showConfirmDialog(parent, "File "+destFile.getName()+" exists, overwrite it?");
    		if(choice == JOptionPane.OK_OPTION) {
//    			updateProgressBar(30, progBar);
    			destFile.delete();	
//    			updateProgressBar(40, progBar);
               	try {
                  	esit = destFile.createNewFile();
        			esit = ImageIO.write(img, "png", destFile);
        			if(esit) {
        				loGGer.info("Image written: "+destFile.getAbsolutePath());
        			}
        		} catch (IOException e) {
        			loGGer.error("Exception happened!", e);
        		}
//               	updateProgressBar(80, progBar);
    		}else if(choice == JOptionPane.NO_OPTION){
               	try {
               		destFile = FileStuff.renameJavaObjFile(destFile);
        			esit = ImageIO.write(img, "png", destFile);
        			if(esit) {
        				loGGer.info("Image written: "+destFile.getAbsolutePath());
        			}
        		} catch (IOException e) {
        			loGGer.error("Exception happened!", e);
        		}
    		}
    		
    	}
//		updateProgressBar(100, progBar);
		parent.getBtnGenerateKey().setEnabled(true);
		parent.getBtnExport().setEnabled(true);
		return destFile;
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
