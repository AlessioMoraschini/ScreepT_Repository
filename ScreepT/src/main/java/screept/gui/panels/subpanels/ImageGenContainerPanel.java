/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.gui.panels.subpanels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import gui.commons.utils.ImageWorkerExtended;
import initializer.configs.impl.INItializer;
import screept.gui.frames.KeyGenFrame;
import screept.gui.utils.ImageWorkerScreepT;
import various.common.light.gui.UpdateProgressPanel;
import various.common.light.om.KeyGenImageType;

public class ImageGenContainerPanel extends JPanel{
	private static final long serialVersionUID = 3880705375830247529L;

	// ImageGenContainerPanel logger
	static Logger logger = Logger.getLogger(ImageGenContainerPanel.class); 
	
    public BufferedImage image;
    
	// object that display wait dialog
	private volatile UpdateProgressPanel progUpdater;
    
	Image scaledImage;
	
	// elements of gui taken from the main key generator panel
	volatile private JProgressBar bar;
	private JTextArea jtext;
	private JButton btnGen;
	private JButton btnAddDict;
	private JButton btnExp;
	private int width;
	private int height;
	
	private ImageGenContainerPanel genContPane;

	
	// constructor that create a random img and adds it to obj panel (this class extends JPanel)
    public ImageGenContainerPanel(KeyGenImageType imgEnumType, int w, int h, KeyGenFrame thisFrame, INItializer currentConf) {
    	
    	logger.info(this.getClass().getName() + " Starting...");
    	
    	genContPane = this;
    	width= w;
    	height= h;
    	
    	jtext = thisFrame.getTextLog();
    	btnAddDict = thisFrame.getBtnAddInDictionary();
    	btnExp = thisFrame.getBtnExport();
    	btnGen = thisFrame.getBtnGenerateKey();
    	
    	ImageGenContainerPanel actualPanel = this;
    	Thread starterAsync = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					progUpdater = new UpdateProgressPanel(false);
					progUpdater.getDialog().setLocationRelativeTo(thisFrame);
					progUpdater.setProgBarEnabled(true);
					progUpdater.getPb().setValue(0);
					bar = thisFrame.getProgressBar();
					
					bar.setValue(20);
					bar.setString("Workin...");
					bar.setIndeterminate(true);
					jtext.append(" Values loaded :)");
					FlowLayout layout = new FlowLayout();
					layout.setVgap(0);
					actualPanel.setLayout(layout);
					bar.setValue(35);
					Dimension backup = new Dimension(thisFrame.getSize());
					actualPanel.setSize(thisFrame.getSize());
					jtext.append("\n -> Creation of random "+w+" x "+h+" image");
					bar.setValue(40);
					progUpdater.getPb().setValue(40);
					image = ImageWorkerScreepT.generateKeyImage(width, height, imgEnumType.getType(), genContPane);
					scaledImage = image.getScaledInstance((int)backup.getWidth(), (int)backup.getHeight(),Image.SCALE_FAST);
					JLabel picLabel = new JLabel(new ImageIcon(scaledImage));
					removeAll();

					bar.setValue(85);
					progUpdater.getPb().setValue(85);
					setOpaque(false);
					add(picLabel, BorderLayout.NORTH);
					btnAddDict.setEnabled(true);
					btnExp.setEnabled(true);
					btnGen.setEnabled(true);
					revalidate(); 
					repaint();	
					picLabel.setBounds(0, 0,(int)backup.getWidth(), (int)backup.getHeight());
					bar.setValue(100);
					bar.setString("Ready :)");
					bar.setIndeterminate(false);
					progUpdater.getPb().setValue(100);
					progUpdater.forceClose();
					jtext.append("\n -> Finished!");
					logger.debug("Generated Image of " + width + "Px X " + height + "Px");
				} catch (Throwable e) {
					logger.error("There was an exception/error!", e);
				} finally {
					UpdateProgressPanel.forceDisposeSafe(progUpdater);
				}


			}
		});
    	starterAsync.start();
    	
    	logger.info(this.getClass().getName() + " Started");
    }
    

    // constructor to print image on this.JPanel object extender, and save it on the given file
    public ImageGenContainerPanel(int width, int height,int type, File dest, String imgExtension, Panel thisPanel) throws IOException {
        
        image = ImageWorkerExtended.generateKeyImageAndWrite(width, height, type, dest, imgExtension);
        scaledImage = image.getScaledInstance(thisPanel.getWidth(),thisPanel.getHeight(),Image.SCALE_FAST);
        JLabel picLabel = new JLabel(new ImageIcon(scaledImage));
        add(picLabel);        
   
  }
    
    public static void loadImageInPanel(BufferedImage image, JPanel thisPanel) {
        Dimension backup = new Dimension(thisPanel.getSize());
        Image scaledImage = image.getScaledInstance((int)backup.getWidth(), (int)backup.getHeight(),Image.SCALE_REPLICATE);
        JLabel picLabel = new JLabel(new ImageIcon(scaledImage));
        thisPanel.removeAll();
        thisPanel.setOpaque(false);
        thisPanel.add(picLabel, BorderLayout.NORTH);
//        revalidate(); 
		  //repaint();	
        picLabel.setBounds(0, 0,(int)backup.getWidth(), (int)backup.getHeight());
        
  }
//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        g.drawImage(scaledImage, 0, 0, this); // see javadoc for more info on the parameters            
//    }

    public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public Image getScaledImage() {
		return scaledImage;
	}


	public UpdateProgressPanel getProgUpdater() {
		return progUpdater;
	}


	public void setProgUpdater(UpdateProgressPanel progUpdater) {
		this.progUpdater = progUpdater;
	}


	public JProgressBar getBar() {
		return bar;
	}


	public void setBar(JProgressBar bar) {
		this.bar = bar;
	}
	
}
