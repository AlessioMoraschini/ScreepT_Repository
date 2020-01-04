package gUI.frames;

import resources.GeneralConfig;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.imageio.ImageIO;

import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.SystemColor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.miginfocom.swing.MigLayout;
import om.KeyGenImageType;
import resources.IconsPathConfigurator;
import uTILS.local.ImageWorkerScrYpT;

import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import dialogutils.JOptionHelperExtended;
import frameutils.frame.arch.ParentFrame;
import frameutils.utils.GuiUtils;
import gUI.panels.subpanels.ImageGenContainerPanel;
import gUI.utils.tooltip.ToolTipBufferedImageType;
import gui.UpdateProgressPanelMin;
import maingUI.MainGUI;
import mainobj.INItializer;

import java.awt.Cursor;

public class KeyGenFrame extends ParentFrame {
	private static final long serialVersionUID = 6912477672774875873L;
	
	public static KeyGenFrame singletonInstance = null;

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(KeyGenFrame.class); 
	
	// static path of keyDictionary
	public static final String KEY_DICTIONARY = "IMG/KEY_DICTIONARY/";

	// width and height of the image container
	public final int DEF_W = 550; 
	public final int DEF_H = 500; 
	
	private int CURRENT_SEL_W;
	private int CURRENT_SEL_H;
	
	// GUI objects
	private JSpinner heightValGUI;
	private JSpinner widthValGUI;
	private JSlider sliderHeight;
	private JSlider sliderWidth;
	private JButton btnAddInDictionary;
	private JButton btnExport;
	private JButton btnGenerateKey;
	private volatile JProgressBar progressBar;
	private JPanel panelIMG;
	private JTextArea textLog;
	private JTextArea textInfo;
	private JComboBox comboImgType;
	private JLabel lblFinalSize;
	private JLabel lblThisSettingsAllow;
	
	// container of the image from which generated original image will be taken
	private volatile UpdateProgressPanelMin progUpdater;
	private ImageGenContainerPanel imageContainer;
	private KeyGenFrame thisFrame;
	
	private KeyGenImageType CURRENT_IMG_TYPE;
	
	private INItializer currentConfig;
	
	private JOptionHelperExtended dialogHelper;
	
	/**
	 * Create the application.
	 */
	public KeyGenFrame(INItializer currentConf) {

		isActive.set(true);

		logger.info(this.getClass().getName() + " Starting...");
		
		thisFrame = this;
		singletonInstance = this;
		
		currentConfig = currentConf;
		
		dialogHelper = new JOptionHelperExtended(thisFrame);
		
		// SW window icon
		GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_KEYGEN, this);
		
		// get default values to initialize panel
		CURRENT_SEL_W = currentConfig.getSteganOpt().getDefaultWidth();
		CURRENT_SEL_H = currentConfig.getSteganOpt().getDefaultHeight();
		
		// initialize img type
		CURRENT_IMG_TYPE = KeyGenImageType.TOP_SECURITY;
		
		getContentPane().setBackground(SystemColor.textInactiveText);
		getContentPane().setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[88.00px,grow][93.00][89.00px,grow][89.00px,grow][:100:10%][:100:10%][:100:8%][:100:8%][:100:14%]", "[35px][50][30%,grow][10%][30px][80px:130,grow][50]"));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.DARK_GRAY);
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0), 4));
		getContentPane().add(panel_1, "cell 0 2 4 4,grow");
		panel_1.setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[8][:55px:100px,grow][65px][55px,grow][8]", "[:10:15][:60:100][:60:100][:60:100][10][10][10][10][10][150,grow][10]"));
		
		JLabel lblNewLabel = new JLabel("Image Width (Px):");
		lblNewLabel.setForeground(Color.WHITE);
		lblNewLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(lblNewLabel, "cell 0 1 2 1,alignx center,aligny center");
		
		JPanel panel_2 = new JPanel();
		panel_2.setBackground(Color.DARK_GRAY);
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0), 4));
		getContentPane().add(panel_2, "cell 4 5 5 1,grow");
		panel_2.setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[grow]", "[grow]"));
		
		JScrollPane textAreaLog = new JScrollPane();
		panel_2.add(textAreaLog, "cell 0 0,grow");
		
		textLog = new JTextArea();
		textLog.setRequestFocusEnabled(false);
		textLog.setEditable(false);
		textAreaLog.setViewportView(textLog);
		textLog.setFont(new Font("Dialog", Font.PLAIN, currentConfig.getGuiOpt().getLogFontSize()));
		textLog.setBackground(new Color(211, 211, 211));
		
		lblFinalSize = new JLabel("-> Image size Prevision: ");
		lblFinalSize.setForeground(new Color(176, 196, 222));
		lblFinalSize.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
		panel_1.add(lblFinalSize, "cell 1 6 3 1,alignx left");

		lblThisSettingsAllow = new JLabel("-> This settings allow to code 0 Kbytes of data inside Image");
		lblThisSettingsAllow.setForeground(new Color(100, 149, 237));
		lblThisSettingsAllow.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
		panel_1.add(lblThisSettingsAllow, "cell 1 5 3 1,alignx left");
			
		// initialize spinner and sliders with max, min, and selected values
		widthValGUI = new JSpinner();
		widthValGUI.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		widthValGUI.setModel(new SpinnerNumberModel(CURRENT_SEL_W, 100, ImageWorkerScrYpT.MAX_WIDTH_GEN, 5));
		panel_1.add(widthValGUI, "cell 2 1,growx");
		
		sliderWidth = new JSlider(JSlider.HORIZONTAL, 100,ImageWorkerScrYpT.MAX_WIDTH_GEN, CURRENT_SEL_W );
		sliderWidth.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panel_1.add(sliderWidth, "cell 3 1,growx");
		
		JLabel lblImageHeight = new JLabel("Image Height (Px):");
		lblImageHeight.setForeground(Color.WHITE);
		lblImageHeight.setFont(new Font("SansSerif", Font.BOLD, 12));
		panel_1.add(lblImageHeight, "cell 0 2 2 1,alignx center,aligny center");
		
		sliderHeight = new JSlider(JSlider.HORIZONTAL, 100,ImageWorkerScrYpT.MAX_HEIGHT_GEN, CURRENT_SEL_H);
		sliderHeight.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panel_1.add(sliderHeight, "cell 3 2,growx");
		
		heightValGUI = new JSpinner();
		heightValGUI.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		heightValGUI.setModel(new SpinnerNumberModel(CURRENT_SEL_H, 100, ImageWorkerScrYpT.MAX_HEIGHT_GEN, 5));
		panel_1.add(heightValGUI, "cell 2 2,growx");
		
		JLabel lblImageType = new JLabel("Image Type:");
		lblImageType.setForeground(Color.WHITE);
		lblImageType.setFont(new Font("SansSerif", Font.BOLD, 12));
		panel_1.add(lblImageType, "cell 0 3 2 1,alignx center");
		
		comboImgType = new JComboBox(KeyGenImageType.values());
		comboImgType.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panel_1.add(comboImgType, "cell 2 3 2 1,growx");
		comboImgType.setRenderer(new ToolTipBufferedImageType());
		comboImgType.setSelectedItem(KeyGenImageType.TOP_SECURITY);
		
		// update prevision of size according to type of image, the function calculate size
		int mult=((KeyGenImageType)comboImgType.getSelectedItem()).getMultiplicator();
		lblFinalSize.setText("-> Image size Prevision: "+getImgSizePrevision(mult)/1024+" KBytes");
		lblThisSettingsAllow.setText("-> This settings allow to code "+(mult*CURRENT_SEL_H*CURRENT_SEL_W)/1024+" KBytes of data inside Image");
	
		JSeparator separator = new JSeparator();
		separator.setForeground(SystemColor.activeCaption);
		separator.setBackground(Color.LIGHT_GRAY);
		panel_1.add(separator, "cell 1 4 3 1,growx");
		
		JSeparator separator_2 = new JSeparator();
		panel_1.add(separator_2, "cell 1 7 3 1,growx");
		
		JLabel lblOutput = new JLabel("Info");
		lblOutput.setFont(new Font("Segoe UI Black", Font.BOLD, 16));
		panel_1.add(lblOutput, "cell 0 8 5 1,alignx center");
		
		JScrollPane textArea = new JScrollPane();
		panel_1.add(textArea, "cell 1 9 3 1,grow");
		textArea.setFont(new Font("SansSerif", Font.PLAIN, 18));

		textInfo = new JTextArea();
		textInfo.setRequestFocusEnabled(false);
		textInfo.setLineWrap(true);
		textInfo.setWrapStyleWord(true);
		textInfo.setEditable(false);
		textInfo.setFont(new Font("SansSerif", Font.PLAIN, currentConfig.getGuiOpt().getLogFontSize()));
		textInfo.setForeground(Color.DARK_GRAY);
		textArea.setViewportView(textInfo);
		textInfo.setBackground(new Color(255, 228, 181));
		//panel_1.add(textInfo, "cell 1 8 3 2,grow");
		textInfo.setBorder(new LineBorder(new Color(128, 128, 128), 3));
		textInfo.setText("Conversion settings:\n\n# width of generated key: "+CURRENT_SEL_W+"Px\n# height of generated key: "+CURRENT_SEL_H+"Px");
		textInfo.append("\n\nThis will be the same image dimensions of the output converted img(es) when cripting file with ScreepT.\n -> Bigger the image and more the values it can contain! ");
		textInfo.append("\nAfter Key is generated pressing the 'generate' button,you can see a quick preview on the right :)");
		textInfo.append("\nNB -> It's recommended to use 4byte types to cript files!");
		
		panelIMG = new JPanel();
		panelIMG.setRequestFocusEnabled(false);
		panelIMG.setBackground(SystemColor.black);
		getContentPane().add(panelIMG, "cell 4 2 5 2,alignx right,growy");
		panelIMG.setMaximumSize(new Dimension(DEF_W, DEF_H));
		panelIMG.setMinimumSize(new Dimension(DEF_W, DEF_H));
		//panelIMG.setSize(DEF_W, super.getMaximumSize().height);
		BorderLayout layoutB=new BorderLayout(0, 0);
		layoutB.setVgap(0);
		panelIMG.setLayout(layoutB);
	   	panelIMG.setBorder(BorderFactory.createLineBorder(SystemColor.DARK_GRAY,4));
		
		progressBar = new JProgressBar();
		progressBar.setOpaque(true);
		progressBar.setBackground(new Color(0, 0, 0));
		progressBar.setForeground(new Color(0, 0, 128));
		progressBar.setStringPainted(true);
		getContentPane().add(progressBar, "cell 5 4 2 1,grow");
		
	   	
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(Color.DARK_GRAY, 6));
		panel.setForeground(SystemColor.menu);
		panel.setBackground(SystemColor.activeCaption);
		getContentPane().add(panel, "cell 0 0 9 1,grow");
		panel.setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[][][][][][][][][100%][]", "[]"));
		
		
		JLabel lblKeyImageGenerator = new JLabel("Key Image Generator");
		lblKeyImageGenerator.setHorizontalAlignment(SwingConstants.CENTER);
		lblKeyImageGenerator.setBackground(SystemColor.activeCaptionBorder);
		panel.add(lblKeyImageGenerator, "cell 0 0 10 1,alignx center");
		lblKeyImageGenerator.setForeground(SystemColor.controlText);
		lblKeyImageGenerator.setFont(new Font("Lithos Pro Regular", Font.BOLD, 22));
		
		textLog.setText("Hi "+currentConfig.getGenOpt().getUserName()+", generate your random custom key Image ;) ");
		btnGenerateKey = new JButton("Generate Key");
		btnGenerateKey.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnGenerateKey.setRequestFocusEnabled(false);
		btnGenerateKey.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnGenerateKey.setForeground(new Color(0, 0, 0));
		btnGenerateKey.setBackground(new Color(255, 127, 80));
		
		
		btnExport = new JButton("Export");
		btnExport.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnExport.setRequestFocusEnabled(false);
		btnExport.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnExport.setForeground(new Color(0, 0, 0));
		btnExport.setBackground(new Color(230, 230, 250));
		getContentPane().add(btnExport, "cell 7 4,grow");
		
		btnAddInDictionary = new JButton("Add to Dictionary");
		btnAddInDictionary.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnAddInDictionary.setRequestFocusEnabled(false);
		btnAddInDictionary.setBackground(new Color(230, 230, 250));
		btnAddInDictionary.setFont(new Font("SansSerif", Font.BOLD, 12));
		getContentPane().add(btnAddInDictionary, "cell 8 4,grow");
		
		addHandlers();
		
		logger.debug("## KEY GENERATOR started");
	}
	
	private void addHandlers() {
		
		// window on close operations
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing "+getClass().getName());
	        	isActive.set(false);
	        	singletonInstance = null;
	        	
	        	dispose();
		    }
		});
		
		// EXPORT BUTTON HANDLER
		btnExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						
						try {
							updateBTNenabled(false);						
							Date now = new Date();
							
							JFileChooser fileChooser = new JFileChooser();		
							// default home dir is the last file saved's parent dir
							fileChooser.setCurrentDirectory(new File(currentConfig.getSteganOpt().getImgRecentPath().get(0)).getParentFile());
							fileChooser.setDialogTitle("Export Key-Image (only .png format)");
							
							// only if the container contains an image!
							if(imageContainer!=null && imageContainer.getImage()!=null) {
								
								logger.debug("EXPORTING IMAGE...");
								
								int returnVal = fileChooser.showSaveDialog(thisFrame);
							    if (returnVal == JFileChooser.APPROVE_OPTION) {
							    	
							    	progUpdater = new UpdateProgressPanelMin(false);
							    	progUpdater.setLocationRelativeTo(MainGUI.mainAPP.getMainFrame());
									progUpdater.getPb().setValue(10);
									progUpdater.getPb().setIndeterminate(true);
							    	
							        boolean done= false;
							    	try {
							        	File selectedFile = fileChooser.getSelectedFile();

							        	//  change extension to png (crittografy in ScreepT need loseless algorithm)
							    		selectedFile = FileStuff.changeExtension(selectedFile, ".png");
							        	
							    		progUpdater.getPb().setValue(20);
							    		
							        	textLog.append("\n"+now.toString()+" -> Saving file "+selectedFile+" ...");
							    		textLog.setCaretPosition(textLog.getText().length());
							        	if(selectedFile.exists()) {
							        		if(dialogHelper.yesOrNo("File "+selectedFile.getName()+" exists, overwrite it?", "overwrite?")) {
							        			selectedFile.delete();	
							                   	done = selectedFile.createNewFile();
									    		progUpdater.getPb().setValue(30);
							            	   	done = ImageIO.write(imageContainer.getImage(), "png" ,selectedFile);
									    		progUpdater.getPb().setValue(90);
							    				logger.debug(selectedFile.getCanonicalPath()+" deleted & re-saved.");

							        		}else {
							        			dialogHelper.info("Operation cancelled", null);
							    				logger.debug("Canceled by user.");
							        		}
							        	}else {
								    		progUpdater.getPb().setValue(40);
							               	done = selectedFile.createNewFile();
							            	done = ImageIO.write(imageContainer.getImage(), "png" ,selectedFile);
											logger.debug(selectedFile.getCanonicalPath()+" Created.");
								    		progUpdater.getPb().setValue(90);
							        	}
							           	
							        	if(done) {
							           		dialogHelper.info("File "+selectedFile.getName()+" succesfully saved!", "");
											currentConfig.getSteganOpt().getImgRecentPath().add(0, selectedFile.getAbsolutePath());
											if(currentConfig.getSteganOpt().getImgRecentPath().size()>10) {
												currentConfig.getSteganOpt().getImgRecentPath().remove(currentConfig.getSteganOpt().getImgRecentPath().size());
											}
								        	textLog.append("\n #### "+selectedFile+" saved!");
							           	}
									} catch (Exception e1) {
							    		dialogHelper.error("error Saving the selected file :(", "");
										logger.error(" Error saving the file!"+e1.toString());
								        textLog.append("\n -> ERROR Saving file :(");
									}		        	
							    }else {
							    	logger.warn("EXPORT ABORTED by user");
							    	dialogHelper.warn("Process killed by user, not exported.", "Cancelled by user");
							    }
								
							}else {
								dialogHelper.error("Image is null, first you have to generate an image!", "NULL image Error");
							}
						} catch (Throwable e) {
							logger.error("An error occurred while exporting image.", e);
						} finally {
							UpdateProgressPanelMin.forceDisposeSafe(progUpdater);
							updateBTNenabled(true);
						}
					}
				}).start();
				
			}
		});
		
		// ADD IN DICTIONARY HANDLER
		btnAddInDictionary.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				updateBTNenabled(false);				
				final Date now = new Date();
				final DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
	
				Thread runnerThread =  new Thread(new Runnable() {
					public void run() {
						
						try {
							// only if the container contains an image!
							if (imageContainer != null && imageContainer.getImage() != null) {
								BufferedImage IMG = imageContainer.getImage();
								textLog.append("\n [Started] adding key to dictionary...");
								String inputName = dialogHelper.askForString(
										"Please insert a name for the image to be added (Otherwise current date will be used as filename)",
										"Choose a name for the File.");
								if (inputName != null && inputName.equals("")) {
									inputName = "ScreepT-Key" + df.format(now) + ".png";
									logger.debug("No input by user... current date used as name: " + inputName);
								}
								// now if user pressed cancel input is null, let's check
								if (inputName != null) {
									progUpdater = new UpdateProgressPanelMin(false);
									progUpdater.setLocationRelativeTo(MainGUI.mainAPP.getMainFrame());
									progUpdater.getPb().setValue(0);
									progUpdater.getPb().setIndeterminate(true);

									String out = "An error occurred :(";
									try {
										textLog.append("\nI'm workin'... just a second (or maybe two!) :)");
										textLog.setSelectionStart(textLog.getText().length());
										File result = ImageWorkerScrYpT.addKeyImgToDictionary(inputName, IMG, thisFrame);
										out = result.getName() + " => Added to dictionary :)";
										textLog.append(out);
										logger.debug(out);
									} catch (Exception e2) {
										logger.error("An error occurred importing to dictionary :(" + e2);
									}

									dialogHelper.info(out, "");
								} else {
									// case of null input (cancel pressed by user)
									dialogHelper.warn("Process killed by user, not added to dictionary.", "Cancelled");
								}
							} else {
								dialogHelper.info("Image is null, first you have to generate an image!", "NULL image Error");
							}
						} catch (HeadlessException e) {
							e.printStackTrace();
						} finally {
							UpdateProgressPanelMin.forceDisposeSafe(progUpdater);
							updateBTNenabled(true);
						}
					}
				});
				runnerThread.start();
			}
		});
		
		// BUTTON KEY GENERATION LISTENER
		btnGenerateKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GuiUtils.launchThreadSafeSwing(new Runnable() {
					public void run() {
						Date now = new Date();
						panelIMG.setSize(DEF_W, panelIMG.getSize().height);
						updateBTNenabled(false);
						textInfo.setText("\n\nConversion started with:\n\n# width of generated key: " + CURRENT_SEL_W + "Px\n# height of generated key: "
								+ CURRENT_SEL_H + "Px");
						textInfo.append("\nAfter Key is generated pressing the 'generate' button,you can see a quick preview on the right :)");
						textInfo.append("\nNB -> It's recommended to use 4byte types to cript files!");
						textLog.setText("\n[Started] " + now.toString() + "\n-> I'm reading input values...");
						//use config copy to avoid overwrite when changin values
						imageContainer = new ImageGenContainerPanel(CURRENT_IMG_TYPE, CURRENT_SEL_W, CURRENT_SEL_H, thisFrame, currentConfig);
						logger.debug("Exit keygen function");
						panelIMG.removeAll();
						panelIMG.add(imageContainer);
						imageContainer.setBackground(Color.DARK_GRAY);
						panelIMG.revalidate();
						panelIMG.repaint();
						updateBTNenabled(true);
					}
				});
				
			}
		});
		getContentPane().add(btnGenerateKey, "cell 4 4,grow");
		
		// WIDTH SPINNER CHANGE HANDLER
		widthValGUI.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				int value = (int)widthValGUI.getValue();
				sliderWidth.setValue(value);
				CURRENT_SEL_W = value;
				currentConfig.getSteganOpt().setDefaultWidth(CURRENT_SEL_W);
				
				// update prevision of size according to type of image, the function calculate size
				int mult=((KeyGenImageType)comboImgType.getSelectedItem()).getMultiplicator();
				lblThisSettingsAllow.setText("-> This settings allow to code "+(mult*CURRENT_SEL_H*CURRENT_SEL_W)/1024+" KBytes of data inside Image");
				lblFinalSize.setText("-> Image size Prevision: "+getImgSizePrevision(mult)/1024+" KBytes");
			}
		});		
		
		// HEIGTH SPINNER CHANGE HANDLER
		heightValGUI.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				int value = (int)heightValGUI.getValue();
				sliderHeight.setValue(value);
				CURRENT_SEL_H = value;		
				currentConfig.getSteganOpt().setDefaultHeight(CURRENT_SEL_H);
				
				// update prevision of size according to type of image, the function calculate size
				int mult=((KeyGenImageType)comboImgType.getSelectedItem()).getMultiplicator();
				lblThisSettingsAllow.setText("-> This settings allow to code "+(mult*CURRENT_SEL_H*CURRENT_SEL_W)/1024+" KBytes of data inside Image");
				lblFinalSize.setText("-> Image size Prevision: "+getImgSizePrevision(mult)/1024+" KBytes");
			}
		});
		
		// WIDTH SLIDER CHANGE HANDLER
		sliderWidth.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				int value = (int)sliderWidth.getValue();
				widthValGUI.setValue(value);
				CURRENT_SEL_W = value;
				currentConfig.getSteganOpt().setDefaultWidth(CURRENT_SEL_W);
			}
		});		
		
		// HEIGTH SLIDER CHANGE HANDLER
		sliderHeight.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				int value = (int)sliderHeight.getValue();
				heightValGUI.setValue(value);
				CURRENT_SEL_H = value;		
				currentConfig.getSteganOpt().setDefaultHeight(CURRENT_SEL_H);
			}
		});
		
		// COMBO IMAGE TYPE HANDLER
		comboImgType.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				CURRENT_IMG_TYPE = (KeyGenImageType)comboImgType.getSelectedItem();
				// update prevision of size according to type of image, the function calculate size
				int mult=((KeyGenImageType)comboImgType.getSelectedItem()).getMultiplicator();
				lblThisSettingsAllow.setText("-> This settings allow to code "+(mult*CURRENT_SEL_H*CURRENT_SEL_W)/1024+" KBytes of data inside Image");
				lblFinalSize.setText("-> Image size Prevision: "+getImgSizePrevision(mult)/1024+" KBytes");
			}
		});
	}
	
	// SOME USEFUL METHOD
	
	private int getImgSizePrevision(int mult) {
		int size = CURRENT_SEL_H*CURRENT_SEL_W*mult;
		return size;
	}
	
	private void updateBTNenabled(boolean enabled) {
		btnExport.setEnabled(enabled);
		btnGenerateKey.setEnabled(enabled);
		btnAddInDictionary.setEnabled(enabled);
	}
	
	// GETTERS AND SETTERS

	public ImageGenContainerPanel getImageContainer() {
		return imageContainer;
	}

	public void setImageContainer(ImageGenContainerPanel imageContainer) {
		this.imageContainer = imageContainer;
	}

	public JButton getBtnAddInDictionary() {
		return btnAddInDictionary;
	}

	public void setBtnAddInDictionary(JButton btnAddInDictionary) {
		this.btnAddInDictionary = btnAddInDictionary;
	}

	public JButton getBtnExport() {
		return btnExport;
	}

	public void setBtnExport(JButton btnExport) {
		this.btnExport = btnExport;
	}

	public JButton getBtnGenerateKey() {
		return btnGenerateKey;
	}

	public void setBtnGenerateKey(JButton btnGenerateKey) {
		this.btnGenerateKey = btnGenerateKey;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public JPanel getPanelIMG() {
		return panelIMG;
	}

	public void setPanelIMG(JPanel panelIMG) {
		this.panelIMG = panelIMG;
	}

	public JTextArea getTextLog() {
		return textLog;
	}

	public void setTextLog(JTextArea textLog) {
		this.textLog = textLog;
	}
	
	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(1100, 800);
	}
	
	public static KeyGenFrame getInstance(INItializer currentConf) {
		return singletonInstance == null ? new KeyGenFrame(currentConf) : singletonInstance;
	}
}
