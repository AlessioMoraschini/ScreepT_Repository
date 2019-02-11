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
import javax.swing.JPanel;
import java.awt.SystemColor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.border.LineBorder;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import uTILS.FileStuff;
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
import javax.swing.UIManager;

public class ImageConverterFrame extends JFrame{

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(ImageConverterFrame.class); 
	
	// local reference to configuration object
	private static INItializer currentConfiguration;
	
	// flag to signal active
	public static boolean active=false;
	
	// GUI ELEMENTS
	
	// panels
	private JPanel panelImgDisp;
	private static JPanel panelHeader;
	private JPanel panel;
	
	// comboboxes
	private static JComboBox comboBoxFormats;
	private JComboBox comboScaleQuality;
	
	// checkboxes
	private JCheckBox chckbxSameFormat;
	private JCheckBox chckbxKeepRatio;
	private JCheckBox checkBoxKeepMetadata;
	
	// spinners
	private JSpinner spinnerHeight;
	private JSpinner spinnerWidth;
	private JSpinner spinnerCompFactor;

	// labels
	private JLabel lblQuality;
	private static JLabel lblImageConverterHeader;
	private JLabel lblExportImageFormat;
	private JLabel lblResizeAlgorithm;
	private JLabel lblHeight;
	private JLabel lblWidth;
	private JLabel lblInputFile;
	private JLabel lblOutputFile;
	private JLabel lblConvert;
	private JLabel lblType;
	private JLabel labelOutTypeEQ;
	private JLabel lblSize;
	private JLabel labelOutSizeEQ;
	private JLabel labelOutFSizeEQ;
	private JLabel lblFileSize;
	private JLabel lblFile;
	private JLabel labelOutFEQ;
	private JLabel lblInFileVal;
	private JLabel lblTypeVal;
	private JLabel lblInSizeVal;
	private JLabel lblInFileSizeVal;
	private JLabel lblOutFile;
	private JLabel lblOutType;
	private JLabel lblOutSize;
	private JLabel lblOutFileSize;
	private JLabel labelINFileEQ;
	private JLabel labelINTypeEQ;
	private JLabel labelINSizeEQ;
	private JLabel labelINFSizeEQ;
	private JLabel lblOutFileVal;
	private JLabel lblOutTypeVal;
	private JLabel lblOutSizeVal;
	private JLabel lblOutFileSizeVal;
	
	// private parent frame
	private static JFrame thisFrame;
	
	// Files selected
	private File inputImgFile;
	private BufferedImage inputImg;
	
	// generated image
	private Image scaledImage;
	
	// buttons
	private JButton btnLoadSourceImg;
	private JButton btnStartConversion;
	private JButton btnOriginalSize;
	private JButton btnConvertAddDictionary;
	
	// separators
	private JSeparator separator;
	private JSeparator separator_1;
	private JSeparator separator_2;
	private JSeparator separator_3;
	private JSeparator separator_4;
	private JSeparator separator_5;
	private JSeparator separator_6;
	private JSeparator separator_7;

	// object that display wait dialog
	TestUpdateProgress progUpdater;

	
	public ImageConverterFrame(INItializer currentConf) {
		thisFrame = this;
		
		active = true;
		
		// initialize field representing locally configuration referred
		currentConfiguration = currentConf;
		
		getContentPane().setBackground(Color.DARK_GRAY);
		
		String writerNames[] = ImageIO.getWriterFileSuffixes();
		getContentPane().setLayout(new MigLayout("", "[120px][120px][:100px:110px][:100px:110px][80px][:80px:80px][20px:20px:20px,grow][15px][100px:100px:100px][120px:120px:120px]", "[45px][10px][22px,grow][22px,grow][][25px][25px,grow][5px][25px,grow]"));

		panelHeader = new JPanel();
		panelHeader.setForeground(SystemColor.menu);
		panelHeader.setBorder(new LineBorder(new Color(0, 0, 0), 4));
		panelHeader.setBackground(new Color(210, 180, 140));
		getContentPane().add(panelHeader, "cell 0 0 10 1,grow");
		panelHeader.setLayout(new MigLayout("", "[100%]", "[]"));
		
		lblImageConverterHeader = new JLabel("Image Converter");
		lblImageConverterHeader.setHorizontalAlignment(SwingConstants.CENTER);
		lblImageConverterHeader.setForeground(Color.BLACK);
		lblImageConverterHeader.setFont(new Font("Lithos Pro Regular", Font.BOLD, 22));
		lblImageConverterHeader.setBackground(SystemColor.activeCaptionBorder);
		panelHeader.add(lblImageConverterHeader, "cell 0 0,alignx center");
		

		separator = new JSeparator();
		getContentPane().add(separator, "cell 0 1 10 1,growx,aligny center");
		
		lblWidth = new JLabel("Width:");
		lblWidth.setForeground(new Color(240, 230, 140));
		lblWidth.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblWidth, "cell 0 2,alignx center");
		
		spinnerWidth = new JSpinner();
		spinnerWidth.setModel(new SpinnerNumberModel(100, 0, 8000, 1));
		getContentPane().add(spinnerWidth, "cell 1 2,growx,aligny center");
		
		// WIDTH SPINNER LISTENER
		spinnerWidth.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// if KeepRatio is  selected 
				boolean posWValSel = (int)spinnerWidth.getValue() > 0;
				boolean posHValSel = (int)spinnerHeight.getValue() > 0;
				if(chckbxKeepRatio.isSelected() && inputImg!=null && posWValSel && posHValSel) {
					// modify height according to ratio of original image
					float r = (float) inputImg.getWidth() / (float) inputImg.getHeight();
					int newH = (int) ((int) spinnerWidth.getValue() / r);
					spinnerHeight.setValue(newH);
				}else if(!posWValSel){
					spinnerWidth.setValue(1);
				}
				lblOutSizeVal.setText((int) spinnerWidth.getValue()+" X "+(int) spinnerHeight.getValue());
			}
		});
		
		lblHeight = new JLabel("Height");
		lblHeight.setForeground(new Color(240, 230, 140));
		lblHeight.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblHeight, "cell 2 2,alignx center");
		
		spinnerHeight = new JSpinner();
		spinnerHeight.setModel(new SpinnerNumberModel(100, 0, 8000, 1));
		getContentPane().add(spinnerHeight, "cell 3 2,growx,aligny center");
		
		// HEIGHT SPINNER LISTENER
		spinnerHeight.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				boolean posHValSel = (int)spinnerHeight.getValue() > 0;
				if(!posHValSel) {
					spinnerHeight.setValue(1);
				}			
				lblOutSizeVal.setText((int) spinnerWidth.getValue()+" X "+(int) spinnerHeight.getValue());
			}
		});
		
		btnOriginalSize = new JButton("Default size");
		btnOriginalSize.setFont(new Font("SansSerif", Font.PLAIN, 12));
		getContentPane().add(btnOriginalSize, "cell 4 2 3 1,alignx center");
		btnOriginalSize.setBackground(Color.GRAY);
		
		//original size listener
		btnOriginalSize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (inputImg!=null ) {
					spinnerWidth.setValue(inputImg.getWidth());
					if (!chckbxKeepRatio.isSelected()) {
						spinnerHeight.setValue(inputImg.getHeight());
					} 
				}
			}
		});
		
		separator_6 = new JSeparator();
		separator_6.setOrientation(SwingConstants.VERTICAL);
		getContentPane().add(separator_6, "cell 7 2 1 5,alignx center,growy");
		
		panelImgDisp = new JPanel();
		panelImgDisp.setMaximumSize(new Dimension(220, 220));
		panelImgDisp.setBackground(Color.BLACK);
		JLabel iniLabel = new JLabel("Load an Image to start :)");
		iniLabel.setForeground(Color.LIGHT_GRAY);
		panelImgDisp.add(iniLabel);
		getContentPane().add(panelImgDisp, "cell 8 2 2 5,grow");
		GridBagLayout gbl_panelImgDisp = new GridBagLayout();
		gbl_panelImgDisp.columnWidths = new int[]{0};
		gbl_panelImgDisp.rowHeights = new int[]{0};
		gbl_panelImgDisp.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_panelImgDisp.rowWeights = new double[]{Double.MIN_VALUE};
		panelImgDisp.setLayout(gbl_panelImgDisp);
		
		checkBoxKeepMetadata = new JCheckBox("Keep Metadata");
		checkBoxKeepMetadata.setOpaque(false);
		checkBoxKeepMetadata.setForeground(new Color(216, 191, 216));
		getContentPane().add(checkBoxKeepMetadata, "cell 0 3 2 1,alignx center");
		
		chckbxSameFormat = new JCheckBox("Original format");
		chckbxSameFormat.setSelected(true);
		getContentPane().add(chckbxSameFormat, "cell 2 3 2 1,alignx center");
		chckbxSameFormat.setOpaque(false);
		chckbxSameFormat.setForeground(new Color(216, 191, 216));
		

		//CHECKBOX SAME FORMAT LISTENER
		chckbxSameFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// refresh gui
				refreshOutGui();
			}
		});
		
				
		chckbxKeepRatio = new JCheckBox("Original Ratio");
		chckbxKeepRatio.setSelected(true);
		getContentPane().add(chckbxKeepRatio, "cell 4 3 3 1,alignx center");
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
		getContentPane().add(separator_7, "cell 0 4 7 1,growx");
		
		
		lblResizeAlgorithm = new JLabel("Resize Algorithm");
		lblResizeAlgorithm.setForeground(new Color(244, 164, 96));
		lblResizeAlgorithm.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblResizeAlgorithm, "cell 0 5 2 1,alignx center");
		
		lblExportImageFormat = new JLabel("Output format");
		lblExportImageFormat.setForeground(new Color(244, 164, 96));
		lblExportImageFormat.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblExportImageFormat, "cell 2 5 2 1,alignx center");
		
		lblQuality = new JLabel("Quality");
		lblQuality.setForeground(new Color(244, 164, 96));
		lblQuality.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblQuality, "cell 4 5 3 1,alignx center");
		
		comboScaleQuality = new JComboBox(ScalingAlgorithm.values());
		getContentPane().add(comboScaleQuality, "cell 0 6 2 1,growx,aligny center");
		comboScaleQuality.setRenderer(new ToolTipListCellRenderer());
		
		comboBoxFormats = new JComboBox(writerNames);
		getContentPane().add(comboBoxFormats, "cell 2 6 2 1,growx,aligny center");
		
		
		// COMBO FORMATS LISTENER
		comboBoxFormats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!chckbxSameFormat.isSelected()) {
					String selectedExt = (String)comboBoxFormats.getSelectedItem();
					lblOutTypeVal.setText(selectedExt);
					lblOutFileVal.setText(FileStuff.changeExtension(new File(lblOutFileVal.getText()), "."+selectedExt).getName());
				}else {
					if(inputImgFile!= null) {
						comboBoxFormats.setSelectedItem(FilenameUtils.getExtension(inputImgFile.getName()));
					}
				}
			}
		});
		
		spinnerCompFactor = new JSpinner();
		spinnerCompFactor.setMinimumSize(new Dimension(35, 25));
		spinnerCompFactor.setBorder(UIManager.getBorder("List.cellNoFocusBorder"));
		spinnerCompFactor.setModel(new SpinnerNumberModel(100, 0, 100, 1));
		getContentPane().add(spinnerCompFactor, "cell 4 6 3 1,growx");
		
		separator_1 = new JSeparator();
		getContentPane().add(separator_1, "cell 0 7 10 1,growx");
		
		panel = new JPanel();
		panel.setBorder(new LineBorder(Color.GRAY, 3, true));
		panel.setBackground(new Color(192, 192, 192));
		getContentPane().add(panel, "cell 0 8 10 1,grow");
		panel.setLayout(new MigLayout("", "[60px][15px][90px:n:90px][10px][][15px][90px:n:90px][][grow][grow]", "[][5px][30px][30px][30px][30px]"));
		
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
		lblConvert.setForeground(new Color(105, 105, 105));
		lblConvert.setFont(new Font("Lithos Pro Regular", Font.PLAIN, 24));
		panel.add(lblConvert, "cell 8 0 2 3,alignx center");
		
		separator_4 = new JSeparator();
		panel.add(separator_4, "cell 0 1 3 1,growx");
		
		separator_5 = new JSeparator();
		panel.add(separator_5, "cell 4 1 3 1,growx");
		
		lblFile = new JLabel("File");
		panel.add(lblFile, "cell 0 2,alignx center");
		
		labelINFileEQ = new JLabel("=");
		labelINFileEQ.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(labelINFileEQ, "cell 1 2,alignx center");
		
		labelOutFEQ = new JLabel("=");
		labelOutFEQ.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(labelOutFEQ, "cell 5 2,alignx center");
		
		lblInFileVal = new JLabel("###");
		lblInFileVal.setForeground(new Color(85, 107, 47));
		panel.add(lblInFileVal, "cell 2 2,alignx center");
		
		lblOutFile = new JLabel("File");
		panel.add(lblOutFile, "cell 4 2,alignx center");
		
		lblOutFileVal = new JLabel("###");
		lblOutFileVal.setForeground(new Color(0, 0, 128));
		panel.add(lblOutFileVal, "cell 6 2,alignx center");
		
		lblType = new JLabel("Type");
		panel.add(lblType, "cell 0 3,alignx center");
		
		labelINTypeEQ = new JLabel("=");
		labelINTypeEQ.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(labelINTypeEQ, "cell 1 3,alignx center");
		
		labelOutTypeEQ = new JLabel("=");
		labelOutTypeEQ.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(labelOutTypeEQ, "cell 5 3,alignx center");
		
		lblTypeVal = new JLabel("###");
		lblTypeVal.setForeground(new Color(85, 107, 47));
		panel.add(lblTypeVal, "cell 2 3,alignx center");
		
		lblOutType = new JLabel("Type");
		panel.add(lblOutType, "cell 4 3,alignx center");
		
		lblOutTypeVal = new JLabel("###");
		lblOutTypeVal.setForeground(new Color(0, 0, 128));
		panel.add(lblOutTypeVal, "cell 6 3,alignx center");
		
		btnLoadSourceImg = new JButton("Load Source Image");
		panel.add(btnLoadSourceImg, "cell 8 3 2 1,grow");
		btnLoadSourceImg.setBackground(new Color(169, 169, 169));
		btnLoadSourceImg.setFont(new Font("SansSerif", Font.BOLD, 14));
		
		// LOAD SOURCE IMAGE BUTTON LISTENER
		btnLoadSourceImg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				btnConvertAddDictionary.setEnabled(false);
				btnLoadSourceImg.setEnabled(false);
				btnOriginalSize.setEnabled(false);
				btnStartConversion.setEnabled(false);
				
				File selected = GenericFileChooserDialog.fileImgRead(thisFrame, currentConf);
				if (selected!=null) {
					
					progUpdater = new TestUpdateProgress(currentConfiguration);
					progUpdater.getPb().setValue(20);
					progUpdater.getLabel().setText("I'm loading the image...");

					try {
						inputImg = ImageIO.read(selected);
						inputImgFile = selected;
					} catch (IOException e2) {
						String msg = "Image not loaded :("+e2.toString();
						JOptionPane.showMessageDialog(thisFrame, msg,"Some error occurred :(", JOptionPane.ERROR_MESSAGE);
						logger.error(msg);
					}
					
					if (inputImg!=null && inputImgFile!=null) {
						
						progUpdater.getPb().setValue(50);
						progUpdater.getLabel().setText("I'm creating the preview...");

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
						refreshInGui("###Error###","###Error###","###Error###","###Error###", (int)spinnerWidth.getValue(), (int)spinnerHeight.getValue());
					}
					
				progUpdater.getPb().setValue(100);
					
				}

				btnConvertAddDictionary.setEnabled(true);
				btnLoadSourceImg.setEnabled(true);
				btnOriginalSize.setEnabled(true);
				btnStartConversion.setEnabled(true);
			}
		});
		
		lblSize = new JLabel("Size");
		panel.add(lblSize, "cell 0 4,alignx center");
		
		labelINSizeEQ = new JLabel("=");
		labelINSizeEQ.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(labelINSizeEQ, "cell 1 4,alignx center");
		
		labelOutSizeEQ = new JLabel("=");
		labelOutSizeEQ.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(labelOutSizeEQ, "cell 5 4,alignx center");
		
		lblInSizeVal = new JLabel("###");
		lblInSizeVal.setForeground(new Color(85, 107, 47));
		panel.add(lblInSizeVal, "cell 2 4,alignx center");
		
		lblOutSize = new JLabel("Size");
		panel.add(lblOutSize, "cell 4 4,alignx center");
		
		lblOutSizeVal = new JLabel("###");
		lblOutSizeVal.setForeground(new Color(0, 0, 128));
		panel.add(lblOutSizeVal, "cell 6 4,alignx center");
		
		btnStartConversion = new JButton("Convert & Export");
		btnStartConversion.setFont(new Font("SansSerif", Font.BOLD, 15));
		btnStartConversion.setBackground(new Color(169, 169, 169));
		panel.add(btnStartConversion, "cell 8 4 2 1,grow");
		
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
								FileOutputStream fout=null;
								
								// if source file exists and is loaded
								if(inputImgFile!=null && inputImg!=null) {
									
									// create copy by reference
									BufferedImage imageToConvert = ImageWorker.copyImage(inputImg);
									
									int X = (int)spinnerWidth.getValue();
									int Y = (int)spinnerHeight.getValue();
									ScalingAlgorithm alg = (ScalingAlgorithm) comboScaleQuality.getSelectedItem();
									
									File selected = FileStuff.renameJavaObjFile(inputImgFile);
									selected = FileStuff.changeExtension(selected, "."+(String)comboBoxFormats.getSelectedItem());
							
									selected = GenericFileChooserDialog.fileImgWrite(thisFrame, currentConf, selected.getName());									
									
									if (selected!=null) {
										
										progUpdater = new TestUpdateProgress(currentConfiguration);
										progUpdater.getPb().setValue(20);
		
										// if not same as the original
										if(X!=imageToConvert.getWidth() || Y!=imageToConvert.getHeight() || !((String)comboBoxFormats.getSelectedItem()).equals(FilenameUtils.getExtension(inputImgFile.getName()))) {
											Image img =imageToConvert.getScaledInstance(X, Y, alg.getAlgorithm());
											// convert Image to BufferedImage
											imageToConvert = new BufferedImage(X, Y, imageToConvert.getType());
											Graphics2D g2d = imageToConvert.createGraphics();
											g2d.drawImage(img, 0, 0, null);
											g2d.dispose();						 
										}
										
										// update advisor
										progUpdater.getPb().setValue(50);
										progUpdater.getLabel().setText("I'm writing the image...");
																
										//if is selected an export file
										if(getCompressionFactor()==1f && !checkBoxKeepMetadata.isSelected()) {
											ImageWorker.convertAndWrite(imageToConvert, (String)comboBoxFormats.getSelectedItem(), selected);
										}else if(checkBoxKeepMetadata.isSelected()) {
											try {
												fout = new FileOutputStream(selected);
												fout.write(ImageWorker.compressIMG(inputImgFile, (String)comboBoxFormats.getSelectedItem(), getCompressionFactor(), true));
												fout.flush();
												fout.close();
											} catch (IOException e) {
								        		JOptionPane.showMessageDialog(thisFrame, "IO error :(", "IO Error", JOptionPane.ERROR_MESSAGE);
												logger.error("Exception happened during conversion!", e);
											} catch (UnsupportedOperationException e) {
								        		JOptionPane.showMessageDialog(thisFrame, "Unsupported operation :(", "IO Error", JOptionPane.ERROR_MESSAGE);
												logger.error("Exception happened during conversion", e);
											}
										}else {
											try {
												fout = new FileOutputStream(selected);
												fout.write(ImageWorker.compressIMG(imageToConvert, (String)comboBoxFormats.getSelectedItem(), getCompressionFactor()));
												fout.flush();
												fout.close();
											} catch (IOException e) {
								        		JOptionPane.showMessageDialog(thisFrame, "IO error :(", "IO Error", JOptionPane.ERROR_MESSAGE);
												logger.error("Exception happened during conversion", e);
											} catch (UnsupportedOperationException e) {
								        		JOptionPane.showMessageDialog(thisFrame, "Unsupported operation :(", "IO Error", JOptionPane.ERROR_MESSAGE);
												logger.error("Exception happened during conversion", e);
											}
										}
								
										// update advisor
										progUpdater.getPb().setValue(100);
										
										String msg = "Image converted: "+selected.getAbsolutePath();
										logger.debug(msg);
										JOptionPane.showMessageDialog(thisFrame, msg,"Image converted :)", JOptionPane.INFORMATION_MESSAGE);
		
									}
								}else {
									refreshInGui("###Error###","###Error###","###Error###","###Error###", (int)spinnerWidth.getValue(), (int)spinnerHeight.getValue());
									String msg = "You have to Load a Source image file before to convert it !";
									JOptionPane.showMessageDialog(thisFrame, msg,"Missing source file", JOptionPane.ERROR_MESSAGE);
									logger.error(msg);
								}
								btnConvertAddDictionary.setEnabled(true);
								btnLoadSourceImg.setEnabled(true);
								btnOriginalSize.setEnabled(true);
								btnStartConversion.setEnabled(true);
								try {
									fout.close();
								} catch (IOException e) {
									logger.error("Exception happened closing the output stream", e);
								}
							}
						}).start();
						
					}
				});
		
		lblFileSize = new JLabel("File Size");
		panel.add(lblFileSize, "cell 0 5,alignx center");
		
		labelINFSizeEQ = new JLabel("=");
		labelINFSizeEQ.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(labelINFSizeEQ, "cell 1 5,alignx center");
		
		labelOutFSizeEQ = new JLabel("=");
		labelOutFSizeEQ.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(labelOutFSizeEQ, "cell 5 5,alignx center");
		
		lblInFileSizeVal = new JLabel("###");
		lblInFileSizeVal.setForeground(new Color(85, 107, 47));
		panel.add(lblInFileSizeVal, "cell 2 5,alignx center");
		
		lblOutFileSize = new JLabel("File Size");
		panel.add(lblOutFileSize, "cell 4 5,alignx center");
		
		lblOutFileSizeVal = new JLabel("###");
		lblOutFileSizeVal.setForeground(new Color(0, 0, 128));
		panel.add(lblOutFileSizeVal, "cell 6 5,alignx center");
		
		btnConvertAddDictionary = new JButton("Convert n' Add as Dictionary Key");
		btnConvertAddDictionary.setFont(new Font("SansSerif", Font.BOLD, 13));
		btnConvertAddDictionary.setBackground(new Color(112, 128, 144));
		panel.add(btnConvertAddDictionary, "cell 8 5 2 1,grow");
		
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
								
								// create copy by reference
								BufferedImage imageToConvert = ImageWorker.copyImage(inputImg);
							
								progUpdater.getPb().setValue(30);
								
								int X = (int)spinnerWidth.getValue();
								int Y = (int)spinnerHeight.getValue();
								ScalingAlgorithm alg = (ScalingAlgorithm) comboScaleQuality.getSelectedItem();
								
								// if not to save same as the original
								if(X!=imageToConvert.getWidth() || Y!=imageToConvert.getHeight() || !((String)comboBoxFormats.getSelectedItem()).equalsIgnoreCase("png")) {
									Image img =imageToConvert.getScaledInstance(X, Y, alg.getAlgorithm());
									// convert Image to BufferedImage
									progUpdater.getPb().setValue(35);
									imageToConvert = new BufferedImage(X, Y, imageToConvert.getType());
									Graphics2D g2d = imageToConvert.createGraphics();
									g2d.drawImage(img, 0, 0, null);
									g2d.dispose();				
									progUpdater.getPb().setValue(50);
								}
								
								progUpdater.getLabel().setText("I'm writing the image...");
								
								File selected = new File(KeyGenPanel.KEY_DICTIONARY + inputImgFile.getName());
								selected = FileStuff.renameJavaObjFile(selected);
								selected = FileStuff.changeExtension(selected, ".png");
								
								if (selected!=null) {
									progUpdater.getPb().setValue(70);
									progUpdater.getPb().setValue(90);
									//if is selected an export file
									ImageWorker.convertAndWrite(imageToConvert, ".png", selected);
									String msg ="Image converted and added to dictionary: "+selected.getAbsolutePath();
									JOptionPane.showMessageDialog(thisFrame, msg, "Image converted :)", JOptionPane.INFORMATION_MESSAGE);
									logger.debug(msg);
								}
							}
							progUpdater.getPb().setValue(100);
						}else {
							refreshInGui("###Error###","###Error###","###Error###","###Error###", (int)spinnerWidth.getValue(), (int)spinnerHeight.getValue());
							String msg = "You have to Load a Source image file before to convert it !";
							JOptionPane.showMessageDialog(thisFrame, msg,"Missing source file", JOptionPane.ERROR_MESSAGE);
							logger.error(msg);
						}	
						
						// reactivate buttons
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
		lblInFileVal.setText(name);
		lblTypeVal.setText(type);
		lblInSizeVal.setText(size);
		lblInFileSizeVal.setText(fileSize);
		spinnerWidth.setValue(w);
		spinnerHeight.setValue(h);
	}
	
	/**
	 * refreshes the gui label values for output selected file
	 * 
	 */
	private void refreshOutGui(String name, String type, String size, String fileSize, int w, int h) {
		lblOutFileVal.setText(name);
		lblOutTypeVal.setText(type);
		lblOutSizeVal.setText(size);
		lblOutFileSizeVal.setText(fileSize);
		spinnerWidth.setValue(w);
		spinnerHeight.setValue(h);
	}
	
	/**
	 * refreshes the gui Different values for output selected file
	 * 
	 */
	private void refreshOutGui() {

		// operations that need either file and img to be not null
		if (inputImgFile!=null && inputImg!=null) {
			
			// if same format is selected
			if(chckbxSameFormat.isSelected()) {
				comboBoxFormats.setSelectedItem(FilenameUtils.getExtension(inputImgFile.getName()));
				lblOutSizeVal.setText((int) spinnerWidth.getValue()+" X "+(int) spinnerHeight.getValue());
			}else{
				lblOutFileSizeVal.setText("Coming soon :)");
			}
		}else {
			refreshInGui("###Error###","###Error###","###Error###","###Error###", (int)spinnerWidth.getValue(), (int)spinnerHeight.getValue());
		}
		
		
		// if KeepRatio is  selected
		if(chckbxKeepRatio.isSelected() && inputImg!=null) {
			spinnerHeight.setEnabled(false);
			if (inputImg!=null) {
				// modify height according to ratio of original image
				float r = (float) inputImg.getWidth() / (float) inputImg.getHeight();
				int newH = (int) ((int) spinnerWidth.getValue() / r);
				spinnerHeight.setValue(newH);
				int w = (int)spinnerWidth.getValue();
				int h = (int)spinnerHeight.getValue();
				String size = w + " X " + h;
				refreshOutGui(inputImgFile.getName(), (String)comboBoxFormats.getSelectedItem(), size, lblOutFileSizeVal.getText(), w, h);

			}
		}else {
			spinnerHeight.setEnabled(true);
		}
	}
	
	private float getCompressionFactor() {
		float floatingP = (int)spinnerCompFactor.getValue();
		return (floatingP/100f);
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
