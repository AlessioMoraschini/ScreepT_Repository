package gUI.panels;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;
import uTILS.FileStuff;
import uTILS.HandleString;
import uTILS.ListCharsets;
import uTILS.Text2Speech;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.drjekyll.fontchooser.FontDialog;

import crypt.aes.EncryptDecrypt;
import iNIT.INItializer;

import java.awt.SystemColor;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.ImageIcon;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoManager;
import javax.swing.event.ChangeEvent;
import javax.swing.UIManager;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import javax.swing.border.LineBorder;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import gUI.om.SupportedFileTextEditor;
import gUI.utils.filedialog.TemplatesChoosePanel;
import gUI.utils.shortcutshandler.TextEditorListener;

import javax.swing.JRadioButton;

public class TextFileWriter extends JPanel {

	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextFileWriter.class);
	
	// name used when using icons, remember to put in the correct folder or it will lanuch nullpointerException
	public final String ICON_TEXT_CLEAR = "Resources_Scrypt/Icons/Clear.png";
	public final String ICON_TEXT_BOLD = "Resources_Scrypt/Icons/Bold.png";
	public final String ICON_TEXT_ITALIC = "Resources_Scrypt/Icons/Italic.png";
	public final String ICON_TEXT_COLOR = "Resources_Scrypt/Icons/TextColor_2.png";
	public final String ICON_TEXT_BG_COLOR = "Resources_Scrypt/Icons/BackgroundColor.png";
	public final String ICON_TEXT_FONT = "Resources_Scrypt/Icons/Font_2.png";
	public final String ICON_FILE_SAVE = "Resources_Scrypt/Icons/SaveFile.png";
	public final String ICON_FILE_LOAD = "Resources_Scrypt/Icons/LoadFile.png";
	public final String ICON_REDO = "Resources_Scrypt/Icons/Redo.png";
	public final String ICON_UNDO = "Resources_Scrypt/Icons/Undo.png";
	public final String ICON_CRYPT = "Resources_Scrypt/Icons/Crypt.png";
	public final String ICON_DECRYPT = "Resources_Scrypt/Icons/Decrypt.png";
	public final String ICON_SPEAKER = "Resources_Scrypt/Icons/Speaker.png";

	private TextFileWriter currentTextEditor;
	
	public Color LBL_ADVICE_COLOR;
	
	public String CURRENT_EXT_OUT;

	// default phrase in text_editor used to check GUI
	private final String LOREM_IPSUM = "Resources_Scrypt/LoremIpsum.txt";
	
	// values used in editor functions Tt Tt and .Tt
	private boolean uppercaseDots;
	private boolean uppercase;
	private String backupText;
	
	// value used for margins of text
	private int textMargin;
	
	// flag to signal if function is already running
	public boolean alreadySaving = false;
	public boolean alreadyLoading = false;
	
	// field containing fast crypt text user key, sinchronized from ini file as default
	private JTextField txtAesKey;
	
	// field containing the current object used to speak text
	Text2Speech objSpeaker;
	
	// GUI Objects
	public JButton btnSaveTextFile;
	public JButton btnLoadTextFile;
	public JButton btnSpeak;
	public JTextArea textArea_1;
	
	public UndoManager manager;
	
	public TextFileWriter(JFrame parent, INItializer currentConfiguration) {
		
		currentTextEditor=this;
		
		logger.debug("Text writer module selected by user");
		
		// initialize values used in gui
		LBL_ADVICE_COLOR = new Color(124, 252, 0);
		CURRENT_EXT_OUT = ".txt";
		textMargin = 14;
		uppercase = false;
		uppercaseDots=true;
		
		setBackground(Color.DARK_GRAY);
		setLayout(new MigLayout("", "[40][40][70][4%][][][][:40:40][:40:40][:40:40][:40:40][:30:30][:40:80px][20][20][30][-119.00][40.00][30,fill][70][35][35,grow][70]", "[:20:50px,grow][][20][][25][25][52.33%,grow]"));
		
		JLabel lblCurrentFileLBL = new JLabel("   Current file:");
		lblCurrentFileLBL.setForeground(SystemColor.controlHighlight);
		lblCurrentFileLBL.setFont(new Font("Tahoma", Font.BOLD, 18));
		add(lblCurrentFileLBL, "cell 0 2 3 1,alignx center");
		
		JLabel lblCurrentFile = new JLabel("\\*.txt");
		lblCurrentFile.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblCurrentFile.setForeground(SystemColor.activeCaption);
		add(lblCurrentFile, "cell 3 2 6 1,alignx left");
		
		JLabel lblVoiceSpeed = new JLabel("Rate");
		lblVoiceSpeed.setForeground(new Color(211, 211, 211));
		add(lblVoiceSpeed, "cell 10 2");
		
		JSlider sliderSpokenSpeed = new JSlider();
		sliderSpokenSpeed.setValue(90);
		sliderSpokenSpeed.setMaximum(400);
		add(sliderSpokenSpeed, "cell 11 2 2 1,alignx center");
		sliderSpokenSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(objSpeaker!=null) {
					objSpeaker.voice.setDurationStretch((sliderSpokenSpeed.getValue()));
				}
			}
		});
		
		
		JLabel lblAdvice = new JLabel("");
		lblAdvice.setBackground(SystemColor.info);
		lblAdvice.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblAdvice.setForeground(new Color(176, 224, 230));
		lblAdvice.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblAdvice, "cell 21 2 2 1,alignx center,aligny center");
		lblAdvice.setText(" [Current output: .txt]");
		
		JCheckBox btnRemembPsw = new JCheckBox("Remember");
		btnRemembPsw.setHorizontalAlignment(SwingConstants.CENTER);
		btnRemembPsw.setFont(new Font("SansSerif", Font.PLAIN, 10));
		btnRemembPsw.setForeground(new Color(100, 149, 237));
		add(btnRemembPsw, "cell 15 5 3 1,alignx center");
		
		JScrollPane textArea = new JScrollPane();
		add(textArea, "cell 0 6 23 1,grow");
		textArea.setFont(new Font("DejaVu Sans", Font.PLAIN, 18));
		
		
		textArea_1 = new JTextArea();
		textArea_1.setFont(new Font("Dialog", Font.PLAIN, 20));
		textArea.setViewportView(textArea_1);
		manager = new UndoManager();
		manager.setLimit(1000);
		textArea_1.getDocument().addUndoableEditListener(manager);
		
		JLabel lblSIZE = new JLabel("");
		lblSIZE.setForeground(SystemColor.window);
		add(lblSIZE, "cell 20 5");
		lblSIZE.setText(String.valueOf(textArea_1.getFont().getSize())+" Px");
		
		
		
		JButton btnDecrypt = new JButton("");
		btnDecrypt.setBackground(SystemColor.activeCaption);
		btnDecrypt.setIcon(new ImageIcon(ICON_DECRYPT));
		btnDecrypt.setToolTipText("Decrypt displayed text with given key");
		btnDecrypt.setForeground(SystemColor.inactiveCaption);
		btnDecrypt.setFont(new Font("Tahoma", Font.BOLD, 12));
//		btnDecrypt.setBackground(Color.LIGHT_GRAY);
		add(btnDecrypt, "cell 14 5,growx");
		btnDecrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {	
				// this function is used to update bg color of textfield according to given key's lenght
				EncryptDecrypt.check(txtAesKey);
				//if checkbutton is selected the remember passwor in current configuration 
				//(volatile until explicit save by user)
				if(btnRemembPsw.isSelected()) {
					currentConfiguration.getAesOpt().setAesKey(txtAesKey.getText());;
				}
				
				try {
					EncryptDecrypt decrypter=new EncryptDecrypt(txtAesKey.getText());
					textArea_1.setText(decrypter.decrypt(textArea_1.getText()));
//					AesKey.setText(decrypter.getDispKey());
				}catch(Exception e) {
					JOptionPane.showMessageDialog(null, "Wrong key, retry! :(", "Error", JOptionPane.ERROR_MESSAGE);
					logger.error("Exception happened!", e);
				}
				
			}
		});
		
		JButton btnBackgroundCol = new JButton("");
		btnBackgroundCol.setToolTipText("Change background color (won't be saved in .txt)");
		btnBackgroundCol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JColorChooser chooser = new JColorChooser();
			    JDialog dialog = chooser.createDialog(parent,"Choose Background color",true,chooser, new ActionListener() {
					
					@Override // OK
					public void actionPerformed(ActionEvent e) {
						textArea_1.setBackground(chooser.getColor());					
					}
				}, new ActionListener() {
					
					@Override // NO
					public void actionPerformed(ActionEvent e) {	
					}
				});
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
			}
		});
		btnBackgroundCol.setBackground(new Color(176, 196, 222));
		btnBackgroundCol.setIcon(new ImageIcon(ICON_TEXT_BG_COLOR));
		add(btnBackgroundCol, "cell 9 4,alignx center");
		
		JSlider slider = new JSlider(4,100);
		slider.setForeground(SystemColor.activeCaptionBorder);
		slider.setBackground(UIManager.getColor("Button.shadow"));
		slider.setValue(textArea_1.getFont().getSize());
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Font font=textArea_1.getFont();
				font = new Font(font.getName(), font.getStyle(), slider.getValue() );
				textArea_1.setFont(font);
				lblSIZE.setText(String.valueOf(textArea_1.getFont().getSize())+" Px");			}
		});
		add(slider, "cell 21 5 2 1,growx");
		
		JButton tglbtnLorem = new JButton("Lorem Ipsum");
		tglbtnLorem.setToolTipText("Insert classic test text \"Lorem Ipsum\"");
		tglbtnLorem.setFont(new Font("SansSerif", Font.BOLD, 12));
		tglbtnLorem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
							
				FileStuff.appendFileTxtArea(new File(LOREM_IPSUM), textArea_1);
			}
		});
		
		JButton btnFontChooser = new JButton("");
		btnFontChooser.setToolTipText("Change font and text-properties");
		btnFontChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FontDialog.showDialog(textArea_1);
				slider.setValue(textArea_1.getFont().getSize());
				lblSIZE.setText(String.valueOf(textArea_1.getFont().getSize())+" Px");			}
		});
		
		btnSpeak = new JButton("");
		btnSpeak.setToolTipText("[Ctrl+spacebar]Speak the text written in the text area (only english). Use the upper slider to change speed before to play, press again to disable.");
		btnSpeak.setIcon(new ImageIcon(ICON_SPEAKER));
		btnSpeak.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnSpeak.setBackground(new Color(176, 196, 222));
		add(btnSpeak, "cell 10 4,alignx center");
		btnSpeak.addActionListener(new ActionListener() {
			
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						if(objSpeaker!=null) {
							objSpeaker.getVoice().deallocate();
							btnSpeak.setBackground(new Color(176, 196, 222));
						}else {
							// else speak
							objSpeaker=new Text2Speech(); 
							btnSpeak.setBackground(Color.RED);
							objSpeaker.dospeak(textArea_1.getText(),"kevin16", (sliderSpokenSpeed.getValue()/100.0f)); 
							objSpeaker=null;
							btnSpeak.setBackground(new Color(176, 196, 222));
						}						
					}
				}).start();
				
			}
		});
		
		btnFontChooser.setBackground(new Color(176, 196, 222));
		btnFontChooser.setIcon(new ImageIcon(ICON_TEXT_FONT));
		add(btnFontChooser, "cell 10 5,alignx center"); 
		tglbtnLorem.setBackground(new Color(176, 196, 222));
		add(tglbtnLorem, "cell 11 5 2 1,growx");
		
		JLabel lblNewLabel = new JLabel("\u2193  (AES KEY)  \u2193");
		lblNewLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		lblNewLabel.setForeground(new Color(211, 211, 211));
		add(lblNewLabel, "cell 13 2 5 1,alignx center");
		
		JSeparator separator_1 = new JSeparator();
		add(separator_1, "cell 0 3 23 1,growx");
		
		btnSaveTextFile = new JButton("");
		btnSaveTextFile.setToolTipText("Choose name and folder, save "+CURRENT_EXT_OUT+" file!");
		btnSaveTextFile.setForeground(SystemColor.menuText);
		btnSaveTextFile.setIcon(new ImageIcon(ICON_FILE_SAVE));
		btnSaveTextFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			
				if (!alreadySaving) {
					// update signal value
					alreadySaving = true;
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setCurrentDirectory(new File(currentConfiguration.getFileOpt().getLastDstFolderPath()));
					fileChooser.setDialogTitle("Save " + CURRENT_EXT_OUT + " File");
					int returnVal = fileChooser.showSaveDialog(parent);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						boolean done = false;
						try {
							File selectedFile = fileChooser.getSelectedFile();
							String givenExtension = FilenameUtils.getExtension(selectedFile.getName());
							boolean recognized = false;
							//this boolean is used to exclude change name extension if the given one is recognized by the system
							for (SupportedFileTextEditor extension : SupportedFileTextEditor.values()) {
								if (extension.senzapunto.equals(givenExtension)) {
									recognized = true;
								}
							}
							// if not specified extension, invalid or equal to checkbox, then change extension to current in global var
							if (("." + givenExtension).equals(CURRENT_EXT_OUT) || givenExtension.equals("")
									|| (("." + givenExtension).length() > 5) || !recognized) {
								selectedFile = FileStuff.changeExtension(selectedFile, CURRENT_EXT_OUT);
							} else {
								selectedFile = FileStuff.changeExtension(selectedFile, "." + givenExtension);
							}
							if (selectedFile.exists()) {
								if (JOptionPane.showConfirmDialog(parent, "File " + selectedFile.getName()
										+ " exists, overwrite it?") == JOptionPane.OK_OPTION) {
									selectedFile.delete();
									done = selectedFile.createNewFile();
								    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(selectedFile), System.getProperty("file.encoding"));
									PrintWriter out = new PrintWriter(outputStreamWriter);
									out.write(textArea_1.getText());
									out.close();
									outputStreamWriter.close();
								} else {
									JOptionPane.showMessageDialog(parent, "Operation cancelled");
								}
							} else {
//  		            		selectedFile = FileStuff.changeExtension(selectedFile, CURRENT_EXT_OUT);
								done = selectedFile.createNewFile();
								PrintWriter out = new PrintWriter(selectedFile.getAbsolutePath());
								out.write(textArea_1.getText());
								out.close();
							}

							if (done) {
								JOptionPane.showMessageDialog(parent,
										"File " + selectedFile.getName() + " succesfully saved!");
								lblCurrentFile.setText(selectedFile.getName());
								currentConfiguration.getFileOpt()
										.setLastDstFolderPath(selectedFile.getParentFile().getAbsolutePath());
							}
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(null, "error Saving the selected file :(");
							logger.error("Exception happened!", e1);
						}
						new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									Thread.sleep(4000);
									lblAdvice.setText("");
								} catch (InterruptedException e) {
									logger.error("Exception happened!", e);
								}
							}
						}).start();

					}
					// update signal value
					alreadySaving = false;
				}
				
			}
		});
		btnSaveTextFile.setBackground(SystemColor.inactiveCaption);
		add(btnSaveTextFile, "cell 1 4,grow");
		
		
		JComboBox comboFileFormat = new JComboBox();
		comboFileFormat.setToolTipText("Choose file extension");
		comboFileFormat.setBackground(SystemColor.textHighlight);
		comboFileFormat.setModel(new DefaultComboBoxModel(SupportedFileTextEditor.values()));
		add(comboFileFormat, "cell 2 4,growx");
		comboFileFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					SupportedFileTextEditor selected = (SupportedFileTextEditor) comboFileFormat.getSelectedItem();
					CURRENT_EXT_OUT = selected.nomeStringa;
					lblAdvice.setText("[Current output: "+CURRENT_EXT_OUT+"]");
					btnSaveTextFile.setToolTipText("Choose name and folder, save "+CURRENT_EXT_OUT+" file!");
				}
			}
		);
		
		JButton tglbtnTemplates = new JButton("Templates");
		tglbtnTemplates.setToolTipText("Choose between many different patterns of code to insert in you text");
		tglbtnTemplates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame panel = new TemplatesChoosePanel(textArea_1, comboFileFormat);
		    	panel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    	panel.setBounds(750,20,500,170);
		    	panel.setAlwaysOnTop(true);
		    	panel.setVisible(true);
		    	panel.setTitle("IMG Generator (V1.1) - A.M.2018");
			}
		});
		tglbtnTemplates.setFont(new Font("SansSerif", Font.BOLD, 12));
		tglbtnTemplates.setBackground(new Color(176, 196, 222));
		add(tglbtnTemplates, "cell 11 4 2 1,growx");
		
		txtAesKey = new JTextField();
		txtAesKey.setHorizontalAlignment(SwingConstants.CENTER);
		//txtAesKey.setText("ST3G4N0cr1p74009");
		txtAesKey.setText(currentConfiguration.getAesOpt().getAesKey());
		add(txtAesKey, "cell 13 4 5 1,growx");
		txtAesKey.setColumns(10);
		EncryptDecrypt.check(txtAesKey);
			
		JButton btnCrypt = new JButton("");
		btnCrypt.setBackground(SystemColor.activeCaption);
		btnCrypt.setIcon(new ImageIcon(ICON_CRYPT));
		btnCrypt.setToolTipText("Crypt displayed text with given key");
		btnCrypt.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnCrypt.setForeground(SystemColor.inactiveCaption);
//		btnCrypt.setBackground(Color.LIGHT_GRAY);
		add(btnCrypt, "cell 13 5,growx");
		btnCrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// this function is used to change color and tooltip text according to key lenght
				EncryptDecrypt.check(txtAesKey);
				
				//if checkbutton is selected the remember passwor in current configuration 
				//(volatile until explicit save by user)
				if(btnRemembPsw.isSelected()) {
					currentConfiguration.getAesOpt().setAesKey(txtAesKey.getText());;
				}
				
				try {
					EncryptDecrypt crypter=new EncryptDecrypt(txtAesKey.getText());
					textArea_1.setText(crypter.encrypt(textArea_1.getText()));
				}catch(Exception e) {
					JOptionPane.showMessageDialog(null, "Wrong key, retry! :(", "Error", JOptionPane.ERROR_MESSAGE);
					logger.error("Exception happened!", e);
				}	
			}
		});
				
		
		JPanel panel = new JPanel();
		panel.setForeground(SystemColor.menu);
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 4));
		panel.setBackground(new Color(100, 149, 237));
		add(panel, "cell 0 0 23 1,growx,aligny center");
		panel.setLayout(new MigLayout("", "[100px,grow 10000,left][][][][][][][][][][][][][][][][][][][][][][grow][grow][]", "[]"));
		
		JLabel lblTextEditor = new JLabel("Text Editor");
		panel.add(lblTextEditor, "cell 0 0,alignx left");
		lblTextEditor.setHorizontalAlignment(SwingConstants.CENTER);
		lblTextEditor.setForeground(Color.BLACK);
		lblTextEditor.setFont(new Font("Lithos Pro Regular", Font.BOLD, 19));
		lblTextEditor.setBackground(SystemColor.activeCaptionBorder);
//		
//		JComboBox<String> comboCharsets = new JComboBox<String>(ListCharsets.getCharsets());
//		panel.add(comboCharsets, "cell 22 0,growx");
//		comboCharsets.setSelectedItem(System.getProperty("file.encoding"));
//		comboCharsets.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				System.setProperty("file.encoding",(String)comboCharsets.getSelectedItem());
//				try {
//					String rewrite = new String(textArea_1.getText().getBytes(System.getProperty("file.encoding")), System.getProperty("file.encoding"));
//					textArea_1.setText(rewrite);
//				} catch (UnsupportedEncodingException e1) {
//					logger.error("Error retrieving charset");
//				}
//				Font old = textArea_1.getFont();
//				textArea_1.setFont(new Font((String)comboCharsets.getSelectedItem(), old.getStyle(), old.getSize()));
//				logger.info("Default charset: " + System.getProperty("file.encoding"));
//				logger.info("Jtext area charset: " + textArea_1.getFont().toString());
//			}
//		});
		
		JCheckBox rdbtnCutWordsTo = new JCheckBox("Adapt Text Width");
		panel.add(rdbtnCutWordsTo, "cell 23 0");
		rdbtnCutWordsTo.setToolTipText("When selected, the text will be cropped to adapt the size of the text area");
		rdbtnCutWordsTo.setSelected(true);
		rdbtnCutWordsTo.setForeground(SystemColor.control);
		rdbtnCutWordsTo.setBackground(Color.DARK_GRAY);
		
		JCheckBox rdbtnFitmantainwords = new JCheckBox("and...Mantain Words!");
		panel.add(rdbtnFitmantainwords, "cell 24 0");
		rdbtnFitmantainwords.setToolTipText("When selected, the text will be cropped to adapt the size of the text area (without breaking words into fragments)");
		rdbtnFitmantainwords.setForeground(SystemColor.control);
		rdbtnFitmantainwords.setBackground(Color.DARK_GRAY);
		rdbtnFitmantainwords.setEnabled(false);
		
	
		
		rdbtnFitmantainwords.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(rdbtnFitmantainwords.isSelected()) {
					textArea_1.setWrapStyleWord(true);
				}else {
					textArea_1.setWrapStyleWord(false);
				}
			}
		});
		
		
		// START checkbox listeners
		
		rdbtnCutWordsTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(rdbtnCutWordsTo.isSelected()) {
					textArea_1.setLineWrap(true);
					rdbtnFitmantainwords.setEnabled(true);
				}else {
					textArea_1.setLineWrap(false);
					textArea_1.setWrapStyleWord(false);
					rdbtnFitmantainwords.setEnabled(false);
				}
			}
		});
		
		
		JLabel lblMargin = new JLabel("Margin:");
		lblMargin.setForeground(SystemColor.menu);
		lblMargin.setBackground(UIManager.getColor("CheckBox.darkShadow"));
		add(lblMargin, "cell 19 4,alignx center");
		
		JSlider slider_margin = new JSlider(0,100,0);
		slider_margin.setBackground(UIManager.getColor("Button.shadow"));
		add(slider_margin, "cell 20 4 3 1,growx");
		
		slider_margin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
					textMargin=slider_margin.getValue();
					textArea_1.setMargin(new Insets(textMargin/2, textMargin, textMargin/2, textMargin));
					textArea_1.revalidate();
					textArea_1.repaint();
//					textArea_1.setText(backupText); -> caused a bug with redo
			}
		});
		
		

		
		JLabel lblFontSize = new JLabel("Font size: ");
		lblFontSize.setForeground(SystemColor.menu);
		add(lblFontSize, "cell 19 5,alignx center");
		

		
//		ColorChooserButton colorChooser = new ColorChooserButton(Color.WHITE);
//		colorChooser.addColorChangedListener(new ColorChangedListener() {
//		    @Override
//		    public void colorChanged(Color newColor) {
//		            // do something with newColor ...
//		    }
//		});
		
		
		
		JButton btnXmlFormat = new JButton("Format");
		btnXmlFormat.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnXmlFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String source = new String(textArea_1.getText());
				String old = new String(source);
				// XML format handler (if xml formatter works then is a xml recognized file)
				if(CURRENT_EXT_OUT.equals(".xml")&&!HandleString.xmlPrettyFormat(source,4).equals("ERROR :(")) {
					source=HandleString.xmlPrettyFormat(source, 4);
					if(source.equals(old)) {
						// already formatted
						textArea_1.setText(HandleString.removeLines(source));
					}else{
						// not yet formatted
						textArea_1.setText(source);
					}
				}
				//if error :(
				else {
					if(CURRENT_EXT_OUT.equals(".xml")) {
						JOptionPane.showMessageDialog(parent, "Error reading xml format, maybe it's not a valid xml String?", "XML format Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		btnXmlFormat.setBackground(new Color(176, 196, 222));
		add(btnXmlFormat, "cell 3 4,growx");
		
		final Font DEFAULT = textArea_1.getFont();
		final Color DEFAULT_BCKG_COL = textArea_1.getBackground(); 
		final Color DEFAULT_FORE_COL = textArea_1.getForeground();
		
		
		
		// this thread will check every 0.2s the value contained in the textfield
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					EncryptDecrypt.check(txtAesKey);				
					try {
						Thread.sleep(400);
					} catch (InterruptedException e) {
						logger.error(e.getMessage());
					}
				}
			}
		}).start();
		
		JSeparator separator = new JSeparator();
		add(separator, "cell 0 1 23 1,growx");
		
		btnLoadTextFile = new JButton("");
		btnLoadTextFile.setToolTipText("Load source File");
		btnLoadTextFile.setForeground(SystemColor.menuText);
		btnLoadTextFile.setBackground(SystemColor.inactiveCaption);
		btnLoadTextFile.setIcon(new ImageIcon(ICON_FILE_LOAD));
		btnLoadTextFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			
				
				if (!alreadyLoading) {
					alreadyLoading = true;
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Load File");
					fileChooser.setCurrentDirectory(new File(currentConfiguration.getFileOpt().getLastSrcFolderPath()));
					int returnVal = fileChooser.showOpenDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION) {

						File source = fileChooser.getSelectedFile();
						if (source == null) {
							JOptionPane.showMessageDialog(null, "Please select a valid file!");
						} else {
							BufferedReader reader;
							textArea_1.setText("");

							try {
								//reader = new BufferedReader(new FileReader(source.getAbsolutePath()));
								reader = new BufferedReader(new InputStreamReader(new FileInputStream(source), System.getProperty("file.encoding")));
								String line = reader.readLine();
								while (line != null) {
									textArea_1.append(line);
									textArea_1.append("\n");
									line = reader.readLine();
								}
								lblAdvice.setText("[New file loaded]");
								reader.close();
								lblCurrentFile.setText(source.getParentFile().getName() + "\\"
										+ fileChooser.getSelectedFile().getName());
								currentConfiguration.getFileOpt()
										.setLastSrcFolderPath(source.getParentFile().getAbsolutePath());

							} catch (Exception e1) {
								JOptionPane.showMessageDialog(null, "[Error loading the selected file]");
							}
							new Thread(new Runnable() {
								// new thread to show advice of loaded file in label without block GUI
								@Override
								public void run() {
									try {
										Thread.sleep(4000);
										lblAdvice.setText("");
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}).start();

						}
					} 
					alreadyLoading = false;
				}// end if !already loading
			}
		});
		
//		JCheckBox tglbtntxt = new JCheckBox(".txt");
//		tglbtntxt.setSelected(true);
//		tglbtntxt.setHorizontalAlignment(SwingConstants.CENTER);
//		tglbtntxt.setBackground(SystemColor.controlShadow);
//		add(tglbtntxt, "cell 2 2,growx");
//		
//		JCheckBox rdbtnXml = new JCheckBox(".xml");
//		rdbtnXml.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				if (rdbtnXml.isSelected()) {
//					CURRENT_EXT_OUT = ".xml";
//					tglbtntxt.setSelected(false);
//					lblAdvice.setText("[Current output: "+CURRENT_EXT_OUT+"]");
//				}
//			}
//		});
//		rdbtnXml.setHorizontalAlignment(SwingConstants.CENTER);
//		rdbtnXml.setBackground(SystemColor.controlShadow);
//		add(rdbtnXml, "cell 0 2 2 1,growx");
//		
//		
//		
//		
//		tglbtntxt.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				if(tglbtntxt.isSelected()) {
//					CURRENT_EXT_OUT = ".txt";
//					rdbtnXml.setSelected(false);
//					lblAdvice.setText("[Current output: "+CURRENT_EXT_OUT+"]");
//				}
//			}
//		});
		
		
		add(btnLoadTextFile, "cell 0 4,grow");
		
		
				
		JButton btnDefault = new JButton("Default Opt");
		btnDefault.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnDefault.setToolTipText("Reset default text-graphics values");
		btnDefault.setForeground(SystemColor.menuText);
		btnDefault.setBackground(new Color(176, 196, 222));
		btnDefault.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea_1.setFont(DEFAULT);
				textArea_1.setBackground(DEFAULT_BCKG_COL);
				textArea_1.setForeground(DEFAULT_FORE_COL);
				txtAesKey.setText(currentConfiguration.getAesOpt().getAesKey());
				slider_margin.setValue(10);
				slider.setValue(textArea_1.getFont().getSize());
				lblSIZE.setText(String.valueOf(textArea_1.getFont().getSize())+" Px");
			}
		});
		add(btnDefault, "cell 4 4 2 1,growx");
		
		
		
		JButton btnUndo = new JButton("");
		btnUndo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(manager.canUndo()) {
					manager.undo();
				}
			}
		});
		btnUndo.setToolTipText("Undo Last [Ctrl+z]");
		btnUndo.setBackground(new Color(176, 196, 222));
		add(btnUndo, "cell 7 4,alignx center");
		btnUndo.setIcon(new ImageIcon(ICON_UNDO));
		
		JButton btnRedo = new JButton("");
		btnRedo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(manager.canRedo()) {
					manager.redo();
				}
				
			}
		});
		btnRedo.setToolTipText("Redo Last [Alt+z]");
		btnRedo.setBackground(new Color(176, 196, 222));
		add(btnRedo, "cell 8 4,alignx center");
		btnRedo.setIcon(new ImageIcon(ICON_REDO));
		
		
		JButton btnClear = new JButton("");
		btnClear.setToolTipText("Erase the content of the current opened file displayed (without save)");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(JOptionPane.showConfirmDialog(parent, "Clear Text?")==JOptionPane.OK_OPTION) {
					textArea_1.setText("");
				}
			}
		});
		btnClear.setBackground(new Color(176, 196, 222));
		btnClear.setIcon(new ImageIcon(ICON_TEXT_CLEAR));
		btnClear.setFont(new Font("Wide Latin", Font.BOLD, 14));
		add(btnClear, "cell 6 4,growx,aligny center");
		
		JButton btnUpperPostDots = new JButton(".Tt");
		btnUpperPostDots.setToolTipText("Makes text uppercase after dots a significat line brokers");
		btnUpperPostDots.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnUpperPostDots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (uppercaseDots) {
					String result = HandleString.upperAfterDots(textArea_1.getText(), '.');
					textArea_1.setText(result);
//					btnUpperPostDots.setBackground(SystemColor.gray);
					uppercaseDots=false;
				}else {
					String result = HandleString.lowerAfterDots(textArea_1.getText(), '.');
					textArea_1.setText(result);
//					btnUpperPostDots.setBackground(SystemColor.activeCaptionBorder);
					uppercaseDots=true;
				}
			}
		});
		btnUpperPostDots.setBackground(new Color(176, 196, 222));
		add(btnUpperPostDots, "cell 6 5,growx");
		textArea_1.setMargin(new Insets(textMargin/2, textMargin, textMargin/2, textMargin));
		
		JButton btnBold = new JButton("");
		btnBold.setToolTipText("Set bold style to all text");
		btnBold.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 if(!textArea_1.getFont().isBold()) {
					textArea_1.setFont(textArea_1.getFont().deriveFont(Font.BOLD, textArea_1.getFont().getSize()));
				 }else {
					textArea_1.setFont(textArea_1.getFont().deriveFont(Font.PLAIN, textArea_1.getFont().getSize()));
				 }
				
			}
		});
		btnBold.setBackground(new Color(176, 196, 222));
		btnBold.setIcon(new ImageIcon(ICON_TEXT_BOLD));
		add(btnBold, "cell 8 5,alignx center");
		
		JButton btnDark = new JButton("Dark/Light");
		btnDark.setToolTipText("Switch upon dark and light text combination");
		btnDark.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnDark.setForeground(SystemColor.menuText);
		btnDark.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(textArea_1.getForeground()==Color.BLACK || textArea_1.getBackground()==DEFAULT_BCKG_COL || textArea_1.getBackground()==Color.WHITE) {
					textArea_1.setForeground(SystemColor.LIGHT_GRAY);
					textArea_1.setBackground(Color.BLACK);
					btnDark.setForeground(Color.DARK_GRAY);
					btnDark.setBackground(SystemColor.controlHighlight);	
				 }else {
					textArea_1.setForeground(Color.BLACK);
					textArea_1.setBackground(SystemColor.controlHighlight);	
					btnDark.setForeground(SystemColor.LIGHT_GRAY);
					btnDark.setBackground(Color.DARK_GRAY);
				 }
			}
		});
		btnDark.setBackground(SystemColor.controlShadow);
		btnDark.setIcon(null);
		btnDark.setPreferredSize(btnBold.size());
		add(btnDark, "cell 0 5 4 1,growx,aligny center");
		btnDark.setForeground(Color.LIGHT_GRAY);
		btnDark.setBackground(Color.DARK_GRAY);
				
		
		JButton btnTt = new JButton("TT");
		btnTt.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnTt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = textArea_1.getText();
				String textBackup = new String(text);
				String selected = textArea_1.getSelectedText();
				String start="";
				String end = "";
				Boolean none = false;
				int strtN=0;
				int endN=text.length();
				if(selected==null) {
					//select ALL if no selection
					none=true;
					selected = new String(text);
				}else {
					strtN=textArea_1.getSelectionStart();
					endN=textArea_1.getSelectionEnd();
					start = textBackup.substring(0, strtN);
					end = textBackup.substring(endN, textBackup.length());
				}				
				backupText = new String(selected);// create backup copy by value
				
				if(!uppercase) {
//					btnTt.setBackground(Color.gray);
					selected = selected.toUpperCase();
					uppercase=true;
				}
				else {
//					btnTt.setBackground(SystemColor.activeCaptionBorder);
					selected = selected.toLowerCase();
					uppercase=false;
				}
				
				selected = start + selected + end;
				textArea_1.setText(selected);
				if(!none) {
					textArea_1.requestFocus();
					textArea_1.setSelectionStart(strtN);
					textArea_1.setSelectionEnd(endN);
				}
				
				logger.debug("TT function start selection: "+strtN);
				logger.debug("TT function start selection: "+endN);
			}
		});
		btnTt.setBackground(new Color(176, 196, 222));
		btnTt.setToolTipText("Makes textArea content uppercase");
		add(btnTt, "cell 4 5,growx");
		
		JButton btnTtTt = new JButton("Tt Tt");
		btnTtTt.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnTtTt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String text = textArea_1.getText();
				String textBackup = new String(text);
				String selected = textArea_1.getSelectedText();
				String start="";
				String end = "";
				int strtN=0;
				int endN=text.length();
				if(selected==null) {
					//select ALL if no selection
					selected = new String(text);
				}else {
					strtN=textArea_1.getSelectionStart();
					endN=textArea_1.getSelectionEnd();
					start = textBackup.substring(0, strtN);
					end = textBackup.substring(endN, textBackup.length());
				}				
				backupText = new String(selected);// create backup copy by value
				
				selected = HandleString.uppercaseEveryFirstLetterNoSpace(selected);
				if (selected.equals(backupText)) {
					selected = HandleString.lowerCaseEveryFirstLetterNoSpace(selected);
//					btnTtTt.setBackground(SystemColor.activeCaptionBorder);
				}else {
//					btnTtTt.setBackground(Color.gray);
				}
				selected = start + selected + end;
				textArea_1.setText(selected);
				textArea_1.requestFocus();
				textArea_1.setSelectionStart(strtN);
				textArea_1.setSelectionEnd(endN);
				logger.debug("Tt Tt function start selection: "+strtN);
				logger.debug("Tt Tt function start selection: "+endN);
				
			}
		});
		btnTtTt.setBackground(new Color(176, 196, 222));
		btnTtTt.setToolTipText("Makes al initial characters uppercase");
		add(btnTtTt, "cell 5 5,growx");
		
		if(rdbtnCutWordsTo.isSelected()) {
			textArea_1.setLineWrap(true);
			rdbtnFitmantainwords.setEnabled(true);
		}
		
		// END checkbox listeners

		
		
		JButton btnItaliC = new JButton("");
		btnItaliC.setToolTipText("Set italics style to all text");
		btnItaliC.setBackground(new Color(176, 196, 222));
		btnItaliC.setIcon(new ImageIcon(ICON_TEXT_ITALIC));
		btnItaliC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 if(!textArea_1.getFont().isItalic()) {
					textArea_1.setFont(textArea_1.getFont().deriveFont(Font.ITALIC, textArea_1.getFont().getSize()));
				 }else {
					textArea_1.setFont(textArea_1.getFont().deriveFont(Font.PLAIN, textArea_1.getFont().getSize()));
				 }		
			}
		});
		add(btnItaliC, "cell 7 5,alignx center");
		
		JButton btnForegroundCol = new JButton("");
		btnForegroundCol.setToolTipText("Change text color (won't be saved in .txt)");
		btnForegroundCol.setBackground(new Color(176, 196, 222));
		btnForegroundCol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {      
			      
					JColorChooser chooser = new JColorChooser();
				    JDialog dialog = chooser.createDialog(parent,"Choose Foreground color",true,chooser, new ActionListener() {
						
						@Override // OK
						public void actionPerformed(ActionEvent e) {
							Color c = chooser.getColor();
							if (c != null) {
						        textArea_1.setForeground(c);
					      }			
						}
					}, new ActionListener() {
						@Override // NO
						public void actionPerformed(ActionEvent e) {	
						}
					});
					dialog.setAlwaysOnTop(true);
					dialog.setVisible(true);
				}
		});
		btnForegroundCol.setIcon(new ImageIcon(ICON_TEXT_COLOR));
		add(btnForegroundCol, "cell 9 5,alignx center");
		
		JScrollBar scrollBar = new JScrollBar();
		
		parent.getContentPane().add(scrollBar);
		
		// add custom keyboard listener
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(getKeyEventHandler());
		
		logger.debug("### TEXT WRITER module started");

	}
	

	// initialize listener
	/**
	 *  KEYBOARD SHORTCUTS - use custom listener
	 */
	private KeyEventDispatcher getKeyEventHandler() {
		return new TextEditorListener(currentTextEditor); 
	}
	
}
