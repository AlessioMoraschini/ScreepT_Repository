package gUI.panels;

import javax.swing.JFrame;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import gUI.om.ScalingAlgorithm;
import gUI.utils.filedialog.GenericFileChooserDialog;
import gUI.utils.progressDialog.TestUpdateProgress;
import gUI.utils.tooltip.ToolTipListCellRenderer;
import iNIT.INItializer;
import java.awt.GridBagLayout;
import java.awt.Image;

import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JPanel;
import java.awt.SystemColor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.border.LineBorder;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import uTILS.FileStuff;
import uTILS.HandleString;
import uTILS.ImageWorker;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.SwingConstants;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.JSeparator;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.Dimension;

public class ImageConverterFrame extends JFrame{

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(ImageConverterFrame.class); 
	
	// local reference to configuration object
	private static INItializer currentConfiguration;
	
	// flag to signal active
	public static boolean active=false;
	
	// GUI ELEMENTS
	private static JPanel panelHeader;
	private static JLabel lblImageConverterHeader;
	private static JComboBox comboBoxFormats;
	private JComboBox comboScaleQuality;
	private JCheckBox chckbxKeepRatio;
	private JSpinner spinnerHeight;
	private JSpinner spinnerWidth;
	private JSeparator separator;
	private JSeparator separator_1;
	private JLabel lblExportImageFormat;
	private JLabel lblResizeAlgorithm;
	private JLabel lblHeight;
	private JLabel lblWidth;
	private JPanel panel;
	private JCheckBox chckbxSameFormat;
	private JSeparator separator_2;
	private JLabel lblInputFile;
	private JLabel lblOutputFile;
	private JSeparator separator_3;
	private JLabel lblConvert;
	private JLabel lblType;
	private JLabel label;
	private JLabel lblSize;
	private JLabel label_1;
	private JLabel label_2;
	private JLabel lblFileSize;
	private JLabel lblFile;
	private JLabel label_4;
	private JLabel lblInFileName;
	private JLabel lblInType;
	private JLabel lblInSize;
	private JLabel lblInFileSize;
	private JLabel label_3;
	private JLabel label_5;
	private JLabel label_6;
	private JLabel label_7;
	private JLabel label_8;
	private JLabel label_9;
	private JLabel label_10;
	private JLabel label_11;
	private JLabel lblOutFileName;
	private JLabel lblOutType;
	private JLabel lblOutSize;
	private JLabel lblOutFileSize;
	private JButton btnLoadSourceImg;
	private JButton btnStartConversion;
	
	// private parent frame
	private static JFrame thisFrame;
	
	// Files selected
	private File inputImgFile;
	private File outputImgFile;
	private BufferedImage inputImg;
	private BufferedImage outputImg;
	private JButton btnConvertAddDictionary;
	private JSeparator separator_4;
	private JSeparator separator_5;
	private JButton btnOriginalSize;
	private JSeparator separator_6;
	private JSeparator separator_7;
	private JPanel panelImgDisp;
	
	// object that display wait dialog
	TestUpdateProgress progUpdater;
	
	private Image scaledImage;
	
	public ImageConverterFrame(INItializer currentConf) {
		thisFrame = this;
		
		active = true;
		
		// initialize field representing locally configuration referred
		currentConfiguration = currentConf;
		
		getContentPane().setBackground(Color.DARK_GRAY);
		
		String writerNames[] = ImageIO.getWriterFileSuffixes();
		getContentPane().setLayout(new MigLayout("", "[150px][130px,grow][130px:130px:130px][15px][100px:100px:100px][120px:120px:120px]", "[45px][10px][22px,grow][22px,grow][][25px,grow][25px,grow][5px][25px,grow]"));

		panelHeader = new JPanel();
		panelHeader.setForeground(SystemColor.menu);
		panelHeader.setBorder(new LineBorder(new Color(0, 0, 0), 4));
		panelHeader.setBackground(new Color(112, 128, 144));
		getContentPane().add(panelHeader, "cell 0 0 6 1,grow");
		panelHeader.setLayout(new MigLayout("", "[][][100%]", "[]"));
		
		lblImageConverterHeader = new JLabel("Image Converter");
		lblImageConverterHeader.setHorizontalAlignment(SwingConstants.CENTER);
		lblImageConverterHeader.setForeground(Color.BLACK);
		lblImageConverterHeader.setFont(new Font("Lithos Pro Regular", Font.BOLD, 22));
		lblImageConverterHeader.setBackground(SystemColor.activeCaptionBorder);
		panelHeader.add(lblImageConverterHeader, "cell 0 0 2 1");
		
		btnLoadSourceImg = new JButton("Load Source Image");
		panelHeader.add(btnLoadSourceImg, "cell 2 0,alignx right");
		btnLoadSourceImg.setBackground(Color.LIGHT_GRAY);
		btnLoadSourceImg.setFont(new Font("SansSerif", Font.BOLD, 13));
		

		separator = new JSeparator();
		getContentPane().add(separator, "cell 0 1 6 1,growx,aligny center");
		
		lblWidth = new JLabel("Width:");
		lblWidth.setForeground(new Color(240, 230, 140));
		lblWidth.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblWidth, "cell 0 2,alignx center");
		
		spinnerWidth = new JSpinner();
		spinnerWidth.setModel(new SpinnerNumberModel(100, 0, 8000, 5));
		getContentPane().add(spinnerWidth, "cell 1 2,growx,aligny center");
		
		// WIDTH SPINNER LISTENER
		spinnerWidth.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// if KeepRatio is  selected 
				if(chckbxKeepRatio.isSelected()) {
					if (inputImg!=null) {
						// modify height according to ratio of original image
						float r = (float) inputImg.getWidth() / (float) inputImg.getHeight();
						int newH = (int) ((int) spinnerWidth.getValue() / r);
						spinnerHeight.setValue(newH);
					}
				}
				lblOutSize.setText((int) spinnerWidth.getValue()+" X "+(int) spinnerHeight.getValue());
			}
		});
		
		btnOriginalSize = new JButton("Original Size");
		getContentPane().add(btnOriginalSize, "cell 2 2,growx");
		btnOriginalSize.setBackground(Color.GRAY);
		
		//original size listener
		btnOriginalSize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (inputImg!=null) {
					spinnerWidth.setValue(inputImg.getWidth());
					if (!chckbxKeepRatio.isSelected()) {
						spinnerHeight.setValue(inputImg.getHeight());
					} 
				}
			}
		});
		
		separator_6 = new JSeparator();
		separator_6.setOrientation(SwingConstants.VERTICAL);
		getContentPane().add(separator_6, "cell 3 2 1 5,alignx center,growy");
		
		panelImgDisp = new JPanel();
		panelImgDisp.setMaximumSize(new Dimension(220, 220));
		panelImgDisp.setBackground(Color.BLACK);
		getContentPane().add(panelImgDisp, "cell 4 2 2 5,grow");
		GridBagLayout gbl_panelImgDisp = new GridBagLayout();
		gbl_panelImgDisp.columnWidths = new int[]{0};
		gbl_panelImgDisp.rowHeights = new int[]{0};
		gbl_panelImgDisp.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_panelImgDisp.rowWeights = new double[]{Double.MIN_VALUE};
		panelImgDisp.setLayout(gbl_panelImgDisp);
		
		lblHeight = new JLabel("Height");
		lblHeight.setForeground(new Color(240, 230, 140));
		lblHeight.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblHeight, "cell 0 3,alignx center");
		
		spinnerHeight = new JSpinner();
		spinnerHeight.setModel(new SpinnerNumberModel(100, 0, 8000, 5));
		getContentPane().add(spinnerHeight, "cell 1 3,growx,aligny center");
		
		// HEIGHT SPINNER LISTENER
		spinnerHeight.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				lblOutSize.setText((int) spinnerWidth.getValue()+" X "+(int) spinnerHeight.getValue());
			}
		});
		
				
		chckbxKeepRatio = new JCheckBox("Original Ratio");
		getContentPane().add(chckbxKeepRatio, "cell 2 3,alignx center");
		chckbxKeepRatio.setForeground(new Color(216, 191, 216));
		chckbxKeepRatio.setOpaque(false);	
		
		
		// KEEP RATIO SELECTOR LISTENER
		chckbxKeepRatio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// refresh gui
				refreshOutGui();
			}
		});	
		
		separator_7 = new JSeparator();
		getContentPane().add(separator_7, "cell 0 4 3 1,growx");
		
		lblExportImageFormat = new JLabel("Export image format:");
		lblExportImageFormat.setForeground(new Color(240, 230, 140));
		lblExportImageFormat.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblExportImageFormat, "cell 0 5,alignx center");
		
		comboBoxFormats = new JComboBox(writerNames);
		getContentPane().add(comboBoxFormats, "cell 1 5,growx,aligny center");
		
		
		// COMBO FORMATS LISTENER
		comboBoxFormats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!chckbxSameFormat.isSelected()) {
					String selectedExt = (String)comboBoxFormats.getSelectedItem();
					lblOutType.setText(selectedExt);
					lblOutFileName.setText(FileStuff.changeExtension(new File(lblOutFileName.getText()), "."+selectedExt).getName());
				}else {
					if(inputImgFile!= null) {
						comboBoxFormats.setSelectedItem(FilenameUtils.getExtension(inputImgFile.getName()));
					}
				}
			}
		});
		
		chckbxSameFormat = new JCheckBox("Original format");
		getContentPane().add(chckbxSameFormat, "cell 2 5,alignx center");
		chckbxSameFormat.setOpaque(false);
		chckbxSameFormat.setForeground(new Color(216, 191, 216));
		

		//CHECKBOX SAME FORMAT LISTENER
		chckbxSameFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// refresh gui
				refreshOutGui();
			}
		});
		
		
		lblResizeAlgorithm = new JLabel("Resize Algorithm:");
		lblResizeAlgorithm.setForeground(new Color(240, 230, 140));
		lblResizeAlgorithm.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblResizeAlgorithm, "cell 0 6,alignx center");
		
		comboScaleQuality = new JComboBox(ScalingAlgorithm.values());
		getContentPane().add(comboScaleQuality, "cell 1 6 2 1,growx,aligny center");
		comboScaleQuality.setRenderer(new ToolTipListCellRenderer());
		
		separator_1 = new JSeparator();
		getContentPane().add(separator_1, "cell 0 7 6 1,growx");
		
		panel = new JPanel();
		panel.setBorder(new LineBorder(Color.GRAY, 3, true));
		panel.setBackground(new Color(192, 192, 192));
		getContentPane().add(panel, "cell 0 8 6 1,grow");
		panel.setLayout(new MigLayout("", "[80px][15px][grow][10px][grow][15px][grow][][grow][grow]", "[][5px][30px][30px][30px][30px]"));
		
		lblInputFile = new JLabel("Input File");
		lblInputFile.setFont(new Font("Lithos Pro Regular", Font.PLAIN, 16));
		lblInputFile.setForeground(new Color(0, 100, 0));
		lblInputFile.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblInputFile, "cell 0 0 3 1,alignx center");
		
		separator_2 = new JSeparator();
		separator_2.setOrientation(SwingConstants.VERTICAL);
		panel.add(separator_2, "cell 3 0 1 6,grow");
		
		lblOutputFile = new JLabel("Output File");
		lblOutputFile.setHorizontalAlignment(SwingConstants.CENTER);
		lblOutputFile.setForeground(new Color(0, 0, 255));
		lblOutputFile.setFont(new Font("Lithos Pro Regular", Font.PLAIN, 16));
		panel.add(lblOutputFile, "cell 4 0 3 1,alignx center");
		
		separator_3 = new JSeparator();
		separator_3.setOrientation(SwingConstants.VERTICAL);
		panel.add(separator_3, "cell 7 0 1 6,grow");
		
		lblConvert = new JLabel("Convert!");
		lblConvert.setHorizontalAlignment(SwingConstants.CENTER);
		lblConvert.setForeground(new Color(0, 0, 0));
		lblConvert.setFont(new Font("Lithos Pro Regular", Font.PLAIN, 24));
		panel.add(lblConvert, "cell 8 0 2 3,alignx center");
		
		separator_4 = new JSeparator();
		panel.add(separator_4, "cell 0 1 3 1,growx");
		
		separator_5 = new JSeparator();
		panel.add(separator_5, "cell 4 1 3 1,growx");
		
		lblFile = new JLabel("File");
		panel.add(lblFile, "cell 0 2,alignx center");
		
		label_8 = new JLabel("=");
		label_8.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(label_8, "cell 1 2,alignx center");
		
		label_4 = new JLabel("=");
		label_4.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(label_4, "cell 5 2,alignx center");
		
		lblInFileName = new JLabel("###");
		lblInFileName.setForeground(new Color(85, 107, 47));
		panel.add(lblInFileName, "cell 2 2,alignx center");
		
		label_3 = new JLabel("File");
		panel.add(label_3, "cell 4 2,alignx center");
		
		lblOutFileName = new JLabel("###");
		lblOutFileName.setForeground(new Color(0, 0, 128));
		panel.add(lblOutFileName, "cell 6 2,alignx center");
		
		lblType = new JLabel("Type");
		panel.add(lblType, "cell 0 3,alignx center");
		
		label_9 = new JLabel("=");
		label_9.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(label_9, "cell 1 3,alignx center");
		
		label = new JLabel("=");
		label.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(label, "cell 5 3,alignx center");
		
		lblInType = new JLabel("###");
		lblInType.setForeground(new Color(85, 107, 47));
		panel.add(lblInType, "cell 2 3,alignx center");
		
		label_5 = new JLabel("Type");
		panel.add(label_5, "cell 4 3,alignx center");
		
		lblOutType = new JLabel("###");
		lblOutType.setForeground(new Color(0, 0, 128));
		panel.add(lblOutType, "cell 6 3,alignx center");
		
		lblSize = new JLabel("Size");
		panel.add(lblSize, "cell 0 4,alignx center");
		
		label_10 = new JLabel("=");
		label_10.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(label_10, "cell 1 4,alignx center");
		
		label_1 = new JLabel("=");
		label_1.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(label_1, "cell 5 4,alignx center");
		
		lblInSize = new JLabel("###");
		lblInSize.setForeground(new Color(85, 107, 47));
		panel.add(lblInSize, "cell 2 4,alignx center");
		
		label_6 = new JLabel("Size");
		panel.add(label_6, "cell 4 4,alignx center");
		
		lblOutSize = new JLabel("###");
		lblOutSize.setForeground(new Color(0, 0, 128));
		panel.add(lblOutSize, "cell 6 4,alignx center");
		
		btnStartConversion = new JButton("Convert & Export");
		btnStartConversion.setBorder(new LineBorder(new Color(64, 64, 64), 0));
		btnStartConversion.setFont(new Font("Lithos Pro Regular", Font.BOLD, 15));
		btnStartConversion.setBackground(new Color(205, 92, 92));
		panel.add(btnStartConversion, "cell 8 3 2 2,grow");
		
		lblFileSize = new JLabel("File Size");
		panel.add(lblFileSize, "cell 0 5,alignx center");
		
		label_11 = new JLabel("=");
		label_11.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(label_11, "cell 1 5,alignx center");
		
		label_2 = new JLabel("=");
		label_2.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(label_2, "cell 5 5,alignx center");
		
		lblInFileSize = new JLabel("###");
		lblInFileSize.setForeground(new Color(85, 107, 47));
		panel.add(lblInFileSize, "cell 2 5,alignx center");
		
		label_7 = new JLabel("File Size");
		panel.add(label_7, "cell 4 5,alignx center");
		
		lblOutFileSize = new JLabel("###");
		lblOutFileSize.setForeground(new Color(0, 0, 128));
		panel.add(lblOutFileSize, "cell 6 5,alignx center");
		
		btnConvertAddDictionary = new JButton("Convert n' Add as Dictionary Key");
		btnConvertAddDictionary.setFont(new Font("SansSerif", Font.BOLD, 13));
		btnConvertAddDictionary.setBackground(new Color(112, 128, 144));
		panel.add(btnConvertAddDictionary, "cell 8 5 2 1,growx");
		
		// convert and save to dictionary - almost equals to convert and export
		btnConvertAddDictionary.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				btnConvertAddDictionary.setEnabled(false);
				btnLoadSourceImg.setEnabled(false);
				btnOriginalSize.setEnabled(false);
				btnStartConversion.setEnabled(false);
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// if source file exists and is loaded
						if(inputImgFile!=null) {
							
							progUpdater = new TestUpdateProgress(currentConfiguration);
							progUpdater.getPb().setValue(20);
							
							if (inputImg != null) {
								try {
									inputImg = ImageIO.read(inputImgFile);
								} catch (IOException e1) {
									logger.error("error retrieving the image :(");
								} 
							}
							
							progUpdater.getPb().setValue(30);
							
							int X = (int)spinnerWidth.getValue();
							int Y = (int)spinnerHeight.getValue();
							ScalingAlgorithm alg = (ScalingAlgorithm) comboScaleQuality.getSelectedItem();
							
							// if not to save same as the original
							if(X!=inputImg.getWidth() || Y!=inputImg.getHeight() || !((String)comboBoxFormats.getSelectedItem()).equals(FilenameUtils.getExtension(inputImgFile.getName()))) {
								Image img =inputImg.getScaledInstance(X, Y, alg.getAlgorithm());
								// convert Image to BufferedImage
								progUpdater.getPb().setValue(35);
								inputImg = new BufferedImage(X, Y, inputImg.getType());
								Graphics2D g2d = inputImg.createGraphics();
								g2d.drawImage(img, 0, 0, null);
								g2d.dispose();				
								progUpdater.getPb().setValue(50);
							}
							
							File selected = new File(KeyGenPanel.KEY_DICTIONARY + inputImgFile.getName());
							selected = FileStuff.changeExtension(selected, "."+(String)comboBoxFormats.getSelectedItem());
							if (selected!=null) {
								progUpdater.getPb().setValue(70);
								progUpdater.getPb().setValue(90);
								//if is selected an export file
								ImageWorker.convertAndWrite(inputImg, (String)comboBoxFormats.getSelectedItem(), selected);
								String msg ="Image converted and added to dictionary: "+selected.getAbsolutePath();
								JOptionPane.showMessageDialog(thisFrame, msg, "Image converted :)", JOptionPane.INFORMATION_MESSAGE);
								logger.debug(msg);
							}
						}else {
							refreshInGui("###Error###","###Error###","###Error###","###Error###", 0, 0);
							String msg = "You have to Load a Source image file before to convert it !";
							JOptionPane.showMessageDialog(thisFrame, msg,"Missing source file", JOptionPane.ERROR_MESSAGE);
							logger.error(msg);
						}	
						progUpdater.getPb().setValue(100);
						
						// reactivate buttons
						btnConvertAddDictionary.setEnabled(true);
						btnLoadSourceImg.setEnabled(true);
						btnOriginalSize.setEnabled(true);
						btnStartConversion.setEnabled(true);
					}
					
				}).start();
				
			}
		});
		

		// LOAD SOURCE IMAGE BUTTON LISTENER
		btnLoadSourceImg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				btnConvertAddDictionary.setEnabled(false);
				btnLoadSourceImg.setEnabled(false);
				btnOriginalSize.setEnabled(false);
				btnStartConversion.setEnabled(false);
				
				File selected = GenericFileChooserDialog.fileRead(thisFrame, currentConf);
				if (selected!=null) {
					
					try {
						inputImg = ImageIO.read(selected);
						inputImgFile = selected;
					} catch (IOException e2) {
						String msg = "Image not loaded :("+e2.toString();
						JOptionPane.showMessageDialog(thisFrame, msg,"Some error occurred :(", JOptionPane.ERROR_MESSAGE);
						logger.error(msg);
					}
					
					if (inputImg!=null) {
						int w = inputImg.getWidth();
						int h = inputImg.getHeight();
						String size = w + " X " + h;
						
						// refresh gui
						comboBoxFormats.setSelectedItem(FilenameUtils.getExtension(inputImgFile.getName()));
						refreshOutGui();						
						refreshInGui(inputImgFile.getName(), FilenameUtils.getExtension(inputImgFile.getName()), size,
								FileStuff.coolFileSize(inputImgFile), w, h);				
						
						// refresh image preview in gui
						// way with custom repainter (keep type)
						scaledImage = ImageWorker.scaleImage(inputImg, inputImg.getType(), panelImgDisp.getWidth(), panelImgDisp.getHeight());
						refreshImage(scaledImage);
						
						logger.debug("Image Loaded!");
					}else {
						refreshInGui("###Error###","###Error###","###Error###","###Error###", 0, 0);
					}
				}
				
				btnConvertAddDictionary.setEnabled(true);
				btnLoadSourceImg.setEnabled(true);
				btnOriginalSize.setEnabled(true);
				btnStartConversion.setEnabled(true);
			}
		});

		// START CONVERSION BUTTON LISTENER
		btnStartConversion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				new Thread(new Runnable() {
					
					@Override
					public void run() {
						
						btnConvertAddDictionary.setEnabled(false);
						btnLoadSourceImg.setEnabled(false);
						btnOriginalSize.setEnabled(false);
						btnStartConversion.setEnabled(false);
						
						// if source file exists and is loaded
						if(inputImgFile!=null) {
							if (inputImg == null) {
								try {
									inputImg = ImageIO.read(inputImgFile);
								} catch (IOException e1) {
									logger.error("error retrieving the image in pre-conversion-phase:(");
								} 
							}
							
							int X = (int)spinnerWidth.getValue();
							int Y = (int)spinnerHeight.getValue();
							ScalingAlgorithm alg = (ScalingAlgorithm) comboScaleQuality.getSelectedItem();
							
							File outPutFileDeflt = inputImgFile;
							outPutFileDeflt = FileStuff.changeExtension(outPutFileDeflt, "."+(String)comboBoxFormats.getSelectedItem());
							File selected = GenericFileChooserDialog.fileWrite(thisFrame, currentConf, outPutFileDeflt.getName());
							if (selected!=null) {
								
								progUpdater = new TestUpdateProgress(currentConfiguration);
								progUpdater.getPb().setValue(20);

								// if not same as the original
								if(X!=inputImg.getWidth() || Y!=inputImg.getHeight() || !((String)comboBoxFormats.getSelectedItem()).equals(FilenameUtils.getExtension(inputImgFile.getName()))) {
									Image img =inputImg.getScaledInstance(X, Y, alg.getAlgorithm());
									// convert Image to BufferedImage
									inputImg = new BufferedImage(X, Y, inputImg.getType());
									Graphics2D g2d = inputImg.createGraphics();
									g2d.drawImage(img, 0, 0, null);
									g2d.dispose();						 
								}
								
								// update advisor
								progUpdater.getPb().setValue(50);
														
								//if is selected an export file
								ImageWorker.convertAndWrite(inputImg, (String)comboBoxFormats.getSelectedItem(), selected);
								String msg = "Image converted: "+selected.getAbsolutePath();
								logger.debug(msg);
								// update advisor
								progUpdater.getPb().setValue(100);
								JOptionPane.showMessageDialog(thisFrame, msg,"Image converted :)", JOptionPane.INFORMATION_MESSAGE);

							}
						}else {
							refreshInGui("###Error###","###Error###","###Error###","###Error###", 0, 0);
							String msg = "You have to Load a Source image file before to convert it !";
							JOptionPane.showMessageDialog(thisFrame, msg,"Missing source file", JOptionPane.ERROR_MESSAGE);
							logger.error(msg);
						}
						btnConvertAddDictionary.setEnabled(true);
						btnLoadSourceImg.setEnabled(true);
						btnOriginalSize.setEnabled(true);
						btnStartConversion.setEnabled(true);
					}
				}).start();
				
			}
		});

		// window on close operations
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing "+getClass().getName());
	        	active = false;
	        	dispose();
		    }
		});
		
		logger.debug("## IMAGE CONVERTER started");
	}
	
	/**
	 * refreshes the gui label values for input selected file
	 * 
	 */
	private void refreshInGui(String name, String type, String size, String fileSize, int w, int h) {
		lblInFileName.setText(name);
		lblInType.setText(type);
		lblInSize.setText(size);
		lblInFileSize.setText(fileSize);
		spinnerWidth.setValue(w);
		spinnerHeight.setValue(h);
	}
	
	/**
	 * refreshes the gui label values for output selected file
	 * 
	 */
	private void refreshOutGui(String name, String type, String size, String fileSize, int w, int h) {
		lblOutFileName.setText(name);
		lblOutType.setText(type);
		lblOutSize.setText(size);
		lblOutFileSize.setText(fileSize);
		spinnerWidth.setValue(w);
		spinnerHeight.setValue(h);
	}
	
	/**
	 * refreshes the gui Different values for output selected file
	 * 
	 */
	private void refreshOutGui() {

		// operations that need file to be not null
		if (inputImgFile!=null) {
			
			// if same format is selected
			if(chckbxSameFormat.isSelected()) {
				comboBoxFormats.setSelectedItem(FilenameUtils.getExtension(inputImgFile.getName()));
				lblOutSize.setText(FileStuff.coolFileSize(inputImgFile));
			}else{
				lblOutFileSize.setText("Coming soon :)");
			}
		}else {
			refreshInGui("###Error###","###Error###","###Error###","###Error###", 0, 0);
		}
		
		
		// if KeepRatio is  selected
		if(chckbxKeepRatio.isSelected()) {
			spinnerHeight.setEnabled(false);
			if (inputImg!=null) {
				// modify height according to ratio of original image
				float r = (float) inputImg.getWidth() / (float) inputImg.getHeight();
				int newH = (int) ((int) spinnerWidth.getValue() / r);
				spinnerHeight.setValue(newH);
				int w = (int)spinnerWidth.getValue();
				int h = (int)spinnerHeight.getValue();
				String size = w + " X " + h;
				refreshOutGui(inputImgFile.getName(), (String)comboBoxFormats.getSelectedItem(), size, lblOutFileSize.getText(), w, h);

			}
		}else {
			spinnerHeight.setEnabled(true);
		}
	}
	
	/**
	 * This method is used only locally to refresh image with the given one
	 */
	private void refreshImage(Image image) {
		
		JLabel picLabel = new JLabel(new ImageIcon(image));
		panelImgDisp.removeAll();
		panelImgDisp.add(picLabel);
		picLabel.setHorizontalAlignment(JLabel.CENTER);
		picLabel.setVerticalAlignment(JLabel.CENTER);
		panelImgDisp.revalidate(); 
		panelImgDisp.repaint();	
	}
	
}
