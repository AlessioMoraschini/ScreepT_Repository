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
package screept.gui.frames;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import various.common.light.i18n.I18n;
import gui.commons.dialogutils.NativeFileChooser;
import gui.commons.frameutils.frame.arch.ParentFrame;
import gui.commons.frameutils.frame.arch.ToolIcons;
import gui.commons.frameutils.frame.arch.ToolStyle;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.commons.imagestudio.DialogFit;
import initializer.configs.impl.INItializer;
import maingUI.MainGUI;
import net.miginfocom.swing.MigLayout;
import resources.IconsPathConfigurator;
import screept.gui.panels.subpanels.ImageGenContainerPanel;
import screept.gui.utils.ImageWorkerScreepT;
import screept.gui.utils.tooltip.ToolTipBufferedImageType;
import various.common.light.files.FileVarious;
import various.common.light.om.KeyGenImageType;

public class KeyGenFrame extends ParentFrame {
	private static final long serialVersionUID = 6912477672774875873L;

	public static KeyGenFrame singletonInstance = null;

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(KeyGenFrame.class);

	// static path of keyDictionary
	public static final String KEY_DICTIONARY = "IMG/KEY_DICTIONARY/";

	// how many recently used paths are remembered
	private static final int RECENT_PATHS_MAX = 10;

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
	private JButton btnStop;
	private JProgressBar progressBar;
	private JPanel panelIMG;
	private JTextArea textLog;
	private JTextArea textInfo;
	private JComboBox<KeyGenImageType> comboImgType;
	private JLabel lblFinalSize;
	private JLabel lblThisSettingsAllow;

	// preview of the key image being generated (or generated)
	private ImageGenContainerPanel imageContainer;
	private KeyGenFrame thisFrame;

	private KeyGenImageType CURRENT_IMG_TYPE;

	// size of the window, computed once the content is built
	private Dimension fitDimension;

	/**
	 * Create the application.
	 */
	public KeyGenFrame(INItializer currentConf) {

		super(null, currentConf, false);

		isActive.set(true);

		logger.info(this.getClass().getName() + " Starting...");

		thisFrame = this;
		singletonInstance = this;

		// SW window icon
		GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_KEYGEN, this);

		// get default values to initialize panel
		CURRENT_SEL_W = configuration.getSteganOpt().getDefaultWidth();
		CURRENT_SEL_H = configuration.getSteganOpt().getDefaultHeight();

		// initialize img type
		CURRENT_IMG_TYPE = KeyGenImageType.TOP_SECURITY;

		buildGui();
		addHandlers();

		DialogFit.apply(this, null, 1100, 0);
		fitDimension = getSize();

		logger.debug("## KEY GENERATOR started");
	}

	private void buildGui() {

		JPanel content = new JPanel(new MigLayout("wrap 1, fill, insets 14 18 14 18, gapy 10", "[grow,fill]", "[][grow,fill]"));
		setContentPane(content);

		JPanel top = new JPanel(new MigLayout("insets 0, fillx", "[grow,fill][]", "[]"));
		top.setOpaque(false);
		top.add(ToolStyle.header(I18n.t("keygen.key_image_generator"), null), "growx");
		top.add(ToolStyle.link("Go to Key Image Vault", new Runnable() {
			
			public void run() {
				MainGUI.mainAPP.windowCreator.newKeyDIctionaryFrame();
			}
		}), "aligny top");
		content.add(top, "growx");

		// LEFT: settings, conversion info and log
		JPanel left = new JPanel(new MigLayout("wrap 1, fill, insets 0, gapy 10", "[grow,fill]", "[][][grow,fill]"));
		left.setOpaque(false);
		left.add(buildSettings(), "growx");
		left.add(buildInfo(), "growx");
		left.add(buildLog(), "grow");

		// RIGHT: preview of the key image (drawn to fit its card), then the actions
		panelIMG = new JPanel(new BorderLayout());
		panelIMG.setOpaque(false);
		panelIMG.add(new JLabel(), BorderLayout.CENTER);
		JPanel preview = ToolStyle.card(new MigLayout("insets 0, fill", "[grow,fill]", "[grow,fill]"));
		preview.setPreferredSize(new Dimension(440, 380));
		preview.add(panelIMG, "grow");

		btnGenerateKey = ToolStyle.accent(ToolStyle.button(I18n.t("keygen.generate_key"), ToolIcons.Kind.DICE, null), ToolStyle.BLUE);
		btnGenerateKey.setRequestFocusEnabled(false);

		btnStop = ToolStyle.button("Stop", ToolIcons.Kind.STOP, "Stop the generation: no image is produced");
		btnStop.setRequestFocusEnabled(false);
		btnStop.setEnabled(false);

		btnExport = ToolStyle.button(I18n.t("common.export"), ToolIcons.Kind.FOLDER, "Save the key image as png");
		btnExport.setRequestFocusEnabled(false);

		progressBar = new JProgressBar();
		progressBar.setOpaque(true);
		progressBar.setStringPainted(false);

		btnAddInDictionary = ToolStyle.button(I18n.t("keygen.add_to_dictionary"), ToolIcons.Kind.PASTE, "Copy the key image into the key dictionary");
		btnAddInDictionary.setRequestFocusEnabled(false);

		JPanel actions = ToolStyle.card(new MigLayout("insets 0, fillx, gap 8 8", "[grow,fill][grow,fill][grow,fill]", "[][][]"));
		actions.add(btnGenerateKey);
		actions.add(btnStop);
		actions.add(btnExport);
		actions.add(progressBar, "span 3, growx");
		actions.add(btnAddInDictionary, "span 3, growx");

		JPanel right = new JPanel(new MigLayout("wrap 1, fill, insets 0, gapy 10", "[grow,fill]", "[grow,fill][]"));
		right.setOpaque(false);
		right.add(preview, "grow");
		right.add(actions, "growx");

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
		split.setResizeWeight(0.5);
		split.setContinuousLayout(true);
		split.setOpaque(false);
		split.setBorder(null);
		content.add(split, "grow");

		textLog.setText("Hi "+configuration.getGenOpt().getUserName()+I18n.t("keygen.generate_your_random_custom_key_image"));
	}

	/** Width and height (slider and spinner each), type of image and the sizes they give */
	private JPanel buildSettings() {
		JPanel settings = ToolStyle.card(new MigLayout("insets 0, fillx, gap 8 6", "[][grow,fill][]", "[][][]"));

		widthValGUI = ToolStyle.hand(new JSpinner());
		widthValGUI.setModel(new SpinnerNumberModel(CURRENT_SEL_W, 100, ImageWorkerScreepT.MAX_WIDTH_GEN, 5));
		widthValGUI.setEditor(new JSpinner.NumberEditor(widthValGUI, "0"));
		sliderWidth = ToolStyle.hand(new JSlider(JSlider.HORIZONTAL, 100, ImageWorkerScreepT.MAX_WIDTH_GEN, CURRENT_SEL_W));

		heightValGUI = ToolStyle.hand(new JSpinner());
		heightValGUI.setModel(new SpinnerNumberModel(CURRENT_SEL_H, 100, ImageWorkerScreepT.MAX_HEIGHT_GEN, 5));
		heightValGUI.setEditor(new JSpinner.NumberEditor(heightValGUI, "0"));
		sliderHeight = ToolStyle.hand(new JSlider(JSlider.HORIZONTAL, 100, ImageWorkerScreepT.MAX_HEIGHT_GEN, CURRENT_SEL_H));

		comboImgType = new JComboBox<>(KeyGenImageType.values());
		comboImgType.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		comboImgType.setRenderer(new ToolTipBufferedImageType());
		comboImgType.setSelectedItem(KeyGenImageType.TOP_SECURITY);

		settings.add(ToolStyle.caption(I18n.t("keygen.image_width_px")));
		settings.add(sliderWidth, "growx");
		settings.add(widthValGUI, "wrap");
		settings.add(ToolStyle.caption(I18n.t("keygen.image_height_px")));
		settings.add(sliderHeight, "growx");
		settings.add(heightValGUI, "wrap");
		settings.add(ToolStyle.caption(I18n.t("keygen.image_type")));
		settings.add(comboImgType, "span 2, growx, wrap");

		lblFinalSize = ToolStyle.mutedLabel(" ");
		lblThisSettingsAllow = ToolStyle.mutedLabel(" ");
		settings.add(lblThisSettingsAllow, "span 3, growx, wrap");
		settings.add(lblFinalSize, "span 3, growx");
		refreshPrevision();
		return settings;
	}

	/** Description of the conversion, as before */
	private JPanel buildInfo() {
		JPanel info = ToolStyle.card(new MigLayout("insets 0, fillx, wrap 1", "[grow,fill]", "[][]"));
		info.add(ToolStyle.caption(I18n.t("keygen.info")));

		textInfo = new JTextArea();
		textInfo.setRequestFocusEnabled(false);
		textInfo.setLineWrap(true);
		textInfo.setWrapStyleWord(true);
		textInfo.setEditable(false);
		textInfo.setFont(ToolStyle.font(0f, java.awt.Font.PLAIN));
		textInfo.setColumns(30);
		textInfo.setText(I18n.t("keygen.conversion_settings_width_of_generated_key") + CURRENT_SEL_W + I18n.t("keygen.px_height_of_generated_key") + CURRENT_SEL_H + "Px");
		textInfo.append(I18n.t("keygen.this_will_be_the_same_image"));
		textInfo.append(I18n.t("keygen.after_key_is_generated_pressing_the"));
		textInfo.append(I18n.t("keygen.nb_it_s_recommended_to_use"));
		textInfo.setCaretPosition(0);
		JScrollPane infoScroll = new JScrollPane(textInfo);
		infoScroll.setPreferredSize(new Dimension(200, 110));
		info.add(infoScroll, "growx");
		return info;
	}

	/** Log of the operations: generation, export, dictionary */
	private JPanel buildLog() {
		JPanel log = ToolStyle.card(new MigLayout("insets 0, fill", "[grow,fill]", "[grow,fill]"));

		textLog = new JTextArea();
		textLog.setRequestFocusEnabled(false);
		textLog.setEditable(false);
		textLog.setLineWrap(true);
		textLog.setWrapStyleWord(true);
		textLog.setColumns(30);
		textLog.setFont(ToolStyle.mono(0f));
		textLog.setMargin(new Insets(6, 8, 6, 8));

		JScrollPane logScroll = new JScrollPane(textLog);
		logScroll.setPreferredSize(new Dimension(200, 120));
		log.add(logScroll, "grow");
		return log;
	}

	private void addHandlers() {

		// window on close operations
		addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent windowEvent) {

	        	logger.debug("Closing "+getClass().getName());
	        	isActive.set(false);
	        	singletonInstance = null;

	        	dispose();
		    }
		});

		// STOP BUTTON: the image being generated is discarded
		btnStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (imageContainer != null) {
					imageContainer.cancelGeneration();
				}
			}
		});

		// EXPORT BUTTON HANDLER
		btnExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				new Thread(new Runnable() {

					@Override
					public void run() {

						try {
							updateBTNenabled(false);
							Date now = new Date();

							JFileChooser fileChooser = new NativeFileChooser();
							// default home dir is the last file saved's parent dir (if any)
							List<String> recent = configuration.getSteganOpt().getImgRecentPath();
							if (!recent.isEmpty()) {
								File lastDir = new File(recent.get(0)).getParentFile();
								if (lastDir != null) {
									fileChooser.setCurrentDirectory(lastDir);
								}
							}
							fileChooser.setDialogTitle(I18n.t("keygen.export_key_image_only_png_format"));

							// only if the container contains an image!
							if(imageContainer!=null && imageContainer.getImage()!=null) {

								logger.debug("EXPORTING IMAGE...");

								int returnVal = fileChooser.showSaveDialog(thisFrame);
							    if (returnVal == JFileChooser.APPROVE_OPTION) {

							        boolean done= false;
							    	try {
							        	File selectedFile = fileChooser.getSelectedFile();

							        	//  change extension to png (crittografy in ScreepT need loseless algorithm)
							    		selectedFile = FileVarious.changeExtension(selectedFile, ".png");

							        	setBarBusy(true);
							        	textLog.append("\n"+now.toString()+I18n.t("keygen.saving_file")+selectedFile+" ...");
							    		textLog.setCaretPosition(textLog.getText().length());
							        	if(selectedFile.exists()) {
							        		if(dialogHelper.yesOrNo(I18n.t("keygen.file")+selectedFile.getName()+I18n.t("keygen.exists_overwrite_it"), "overwrite?")) {
							        			selectedFile.delete();
							            	   	done = ImageIO.write(imageContainer.getImage(), "png" ,selectedFile);
							    				logger.debug(selectedFile.getCanonicalPath()+" deleted & re-saved.");

							        		}else {
							        			// cancelled by user: no message needed
							    				logger.debug("Canceled by user.");
							        		}
							        	}else {
							               	selectedFile.createNewFile();
							            	done = ImageIO.write(imageContainer.getImage(), "png" ,selectedFile);
											logger.debug(selectedFile.getCanonicalPath()+" Created.");
							        	}

							        	if(done) {
							           		dialogHelper.info(I18n.t("keygen.file")+selectedFile.getName()+I18n.t("keygen.succesfully_saved"), "");
											addRecentPath(selectedFile.getAbsolutePath());
								        	textLog.append("\n #### "+selectedFile+I18n.t("keygen.saved"));
							           	}
									} catch (Exception e1) {
							    		dialogHelper.error(I18n.t("common.error_saving_the_selected_file"), "");
										logger.error(" Error saving the file!"+e1.toString());
								        textLog.append(I18n.t("keygen.error_saving_file"));
									} finally {
										setBarBusy(false);
									}
							    }else {
							    	// cancelled by user: no message needed
							    	logger.debug("EXPORT cancelled by user");
							    }

							}else {
								dialogHelper.error(I18n.t("keygen.image_is_null_first_you_have"), I18n.t("keygen.null_image_error"));
							}
						} catch (Throwable e) {
							logger.error("An error occurred while exporting image.", e);
						} finally {
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
					@Override
					public void run() {

						try {
							// only if the container contains an image!
							if (imageContainer != null && imageContainer.getImage() != null) {
								BufferedImage IMG = imageContainer.getImage();
								textLog.append("\n [Started] adding key to dictionary...");
								String inputName = dialogHelper.askForString(
										I18n.t("app.please_insert_a_name_for_the"),
										I18n.t("keygen.choose_a_name_for_the_file"));
								// cancelled by user: null input, nothing to do
								if (inputName != null) {
									if (inputName.equals("")) {
										inputName = "ScreepT-Key" + df.format(now) + ".png";
										logger.debug("No input by user... current date used as name: " + inputName);
									}

									setBarBusy(true);
									String out = I18n.t("app.an_error_occurred");
									try {
										textLog.append(I18n.t("keygen.i_m_workin_just_a_second"));
										textLog.setSelectionStart(textLog.getText().length());
										File result = ImageWorkerScreepT.addKeyImgToDictionary(inputName, IMG, thisFrame);
										out = result.getName() + I18n.t("keygen.added_to_dictionary");
										textLog.append(out);
										logger.debug(out);
									} catch (Exception e2) {
										logger.error("An error occurred importing to dictionary :(" + e2);
									} finally {
										setBarBusy(false);
									}

									dialogHelper.info(out, "");
								}
							} else {
								dialogHelper.info(I18n.t("keygen.image_is_null_first_you_have"), I18n.t("keygen.null_image_error"));
							}
						} catch (Exception e) {
							logger.error("Error while adding the key to the dictionary", e);
						} finally {
							updateBTNenabled(true);
						}
					}
				});
				runnerThread.start();
			}
		});

		// BUTTON KEY GENERATION LISTENER
		btnGenerateKey.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Date now = new Date();
				// generate and stop buttons are set by the container: it enables them again when the work ends
				updateBTNenabled(false);
				textInfo.setText(I18n.t("keygen.conversion_started_with_width_of_generated") + CURRENT_SEL_W + I18n.t("keygen.px_height_of_generated_key")
						+ CURRENT_SEL_H + "Px");
				textInfo.append(I18n.t("keygen.after_key_is_generated_pressing_the"));
				textInfo.append(I18n.t("keygen.nb_it_s_recommended_to_use"));
				textInfo.setCaretPosition(0);
				textLog.setText("\n[Started] " + now.toString() + I18n.t("keygen.i_m_reading_input_values"));
				//use config copy to avoid overwrite when changin values
				imageContainer = new ImageGenContainerPanel(CURRENT_IMG_TYPE, CURRENT_SEL_W, CURRENT_SEL_H, thisFrame, configuration);
				panelIMG.removeAll();
				panelIMG.add(imageContainer, BorderLayout.CENTER);
				panelIMG.revalidate();
				panelIMG.repaint();
			}
		});

		// WIDTH SPINNER CHANGE HANDLER
		widthValGUI.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				int value = (int)widthValGUI.getValue();
				sliderWidth.setValue(value);
				CURRENT_SEL_W = value;
				configuration.getSteganOpt().setDefaultWidth(CURRENT_SEL_W);
				refreshPrevision();
			}
		});

		// HEIGTH SPINNER CHANGE HANDLER
		heightValGUI.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				int value = (int)heightValGUI.getValue();
				sliderHeight.setValue(value);
				CURRENT_SEL_H = value;
				configuration.getSteganOpt().setDefaultHeight(CURRENT_SEL_H);
				refreshPrevision();
			}
		});

		// WIDTH SLIDER CHANGE HANDLER
		sliderWidth.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				int value = sliderWidth.getValue();
				widthValGUI.setValue(value);
				CURRENT_SEL_W = value;
				configuration.getSteganOpt().setDefaultWidth(CURRENT_SEL_W);
			}
		});

		// HEIGTH SLIDER CHANGE HANDLER
		sliderHeight.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				int value = sliderHeight.getValue();
				heightValGUI.setValue(value);
				CURRENT_SEL_H = value;
				configuration.getSteganOpt().setDefaultHeight(CURRENT_SEL_H);
			}
		});

		// COMBO IMAGE TYPE HANDLER
		comboImgType.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CURRENT_IMG_TYPE = (KeyGenImageType)comboImgType.getSelectedItem();
				refreshPrevision();
			}
		});
	}

	/** Progress bar of the window while a file is being saved or added: indeterminate while working */
	private void setBarBusy(final boolean busy) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				progressBar.setIndeterminate(busy);
				if (!busy) {
					progressBar.setValue(0);
				}
			}
		});
	}

	/** Remembers the last saved files: the newest first, at most RECENT_PATHS_MAX */
	private void addRecentPath(String path) {
		List<String> recent = configuration.getSteganOpt().getImgRecentPath();
		recent.add(0, path);
		while (recent.size() > RECENT_PATHS_MAX) {
			recent.remove(recent.size() - 1);
		}
	}

	// SOME USEFUL METHOD

	/** Size of the image file and of the data it can hide, for the current settings */
	private void refreshPrevision() {
		int mult = ((KeyGenImageType) comboImgType.getSelectedItem()).getMultiplicator();
		lblThisSettingsAllow.setText(I18n.t("keygen.this_settings_allow_to_code") + (mult*CURRENT_SEL_H*CURRENT_SEL_W)/1024 + I18n.t("keygen.kbytes_of_data_inside_image"));
		lblFinalSize.setText(I18n.t("keygen.image_size_prevision") + getImgSizePrevision(mult)/1024 + I18n.t("keygen.kbytes"));
	}

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

	public JButton getBtnExport() {
		return btnExport;
	}

	public JButton getBtnGenerateKey() {
		return btnGenerateKey;
	}

	public JButton getBtnStop() {
		return btnStop;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public JPanel getPanelIMG() {
		return panelIMG;
	}

	public JTextArea getTextLog() {
		return textLog;
	}

	@Override
	public Dimension getDefaultDimension() {
		return fitDimension != null ? new Dimension(fitDimension) : new Dimension(1100, 720);
	}

	public static KeyGenFrame getInstance(INItializer currentConf) {
		return singletonInstance == null ? new KeyGenFrame(currentConf) : singletonInstance;
	}
}
