package gUI.panels.subpanels.texteditor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import crypt.aes.AesCipher;
import frameutils.utils.GuiUtilsExtended;
import gUI.panels.TextEditor;
import gUI.utils.texteditor.TextEditorUtils;
import gui.TextEditTooltips;
import mainobj.INItializer;
import net.miginfocom.swing.MigLayout;
import om.TabTextEditorObj;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import util.ScreeptUtils;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class TextEditorHeaderPanel extends JPanel{
	private static final long serialVersionUID = -457587326299200369L;
	
	// TextEditorHeaderPanel logger creation
	static Logger logger = Logger.getLogger(TextEditorHeaderPanel.class);
	
	public static final String AES_KEY_PLACEHOLDER = "";
	public static final char DEFAULT_PSW_CHAR = '\u25CF';
	
	public TextEditor parent;
	public TextEditorUtils parentUtils;
	public INItializer currentConfiguration;
	
	private JLabel lblVoiceSpeed;
	public JSlider sliderSpokenSpeed;
	public JCheckBox checkBoxInlineTabs;
	public JCheckBox chkCutWordsTo;
	private JButton btnNextHistory;
	private JButton btnPrevHistory;
	
	public boolean isCrypterOn = false;
	
	private JPasswordField txtAesKey; // field containing fast crypt text user key, sinchronized from ini file as
	public JCheckBox chckbxAesShowKey;
	public JButton btnDecrypt;
	public JButton btnCrypt;

	public TextEditorHeaderPanel(TextEditor parent) {
		
		logger.info(this.getClass().getName() + " Starting...");
		
		this.parent = parent;
		this.parentUtils = parent.textEditorUtils;
		this.currentConfiguration = parent.currentConfiguration;
		
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setLayout(new MigLayout("insets 6, fill, hidemode 3", "[left][][][][::35px][100px:240px:300px,trailing][40px:45px:45px][40px:45px:45px][10px:10px,grow][::20px][::20px][10px:10px:10px][::120px][::140px][::130px]", "[:0px:55px,grow]"));
		setForeground(SystemColor.menu);
		setBorder(new LineBorder(Color.BLACK, 1));
		setBackground(new Color(30,30,30));
		
		lblVoiceSpeed = new JLabel("Voice rate");
		lblVoiceSpeed.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVoiceSpeed.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		lblVoiceSpeed.setForeground(SystemColor.control);
		add(lblVoiceSpeed, "cell 0 0,alignx right,aligny center");
		
		sliderSpokenSpeed = new JSlider();
		sliderSpokenSpeed.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		sliderSpokenSpeed.setValue(currentConfiguration.getTextEditorOpt().getVoiceRate());
		sliderSpokenSpeed.setMaximum(400);
		add(sliderSpokenSpeed, "cell 1 0 2 1,growy");
		
		btnCrypt = new JButton();
		btnCrypt.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnCrypt.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CRYPT));
		btnCrypt.setToolTipText(TextEditTooltips.tooltipCrypt);
		btnCrypt.setForeground(SystemColor.inactiveCaption);
		add(btnCrypt, "cell 6 0,alignx center,grow");

		btnDecrypt = new JButton("");
		btnDecrypt.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnDecrypt.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DECRYPT));
		btnDecrypt.setToolTipText(TextEditTooltips.tooltipDecrypt);
		btnDecrypt.setForeground(SystemColor.inactiveCaption);
		add(btnDecrypt, "cell 7 0,alignx center,grow");

		chckbxAesShowKey = new JCheckBox("");
		chckbxAesShowKey.setIcon(new ImageIcon(IconsPathConfigurator.ICON_INVISIBLE));
		chckbxAesShowKey.setSelectedIcon(new ImageIcon(IconsPathConfigurator.ICON_VISIBLE));
		chckbxAesShowKey.setRolloverIcon(chckbxAesShowKey.getIcon());
		chckbxAesShowKey.setOpaque(false);
		chckbxAesShowKey.setForeground(Color.WHITE);
		chckbxAesShowKey.setIconTextGap(1);
		chckbxAesShowKey.setHorizontalAlignment(SwingConstants.LEFT);
		chckbxAesShowKey.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxAesShowKey.setFont(new Font("SansSerif", Font.BOLD, 12));
		add(chckbxAesShowKey, "cell 4 0,alignx center,aligny center");
		
		txtAesKey = new JPasswordField();
		txtAesKey.putClientProperty("JPasswordField.cutCopyAllowed",true);
		txtAesKey.setMinimumSize(new Dimension(12, 25));
		txtAesKey.setHorizontalAlignment(SwingConstants.CENTER);
		txtAesKey.setEchoChar((char) 0);
		txtAesKey.setText(AES_KEY_PLACEHOLDER);
		add(txtAesKey, "cell 5 0,growx");
		txtAesKey.setColumns(10);
		txtAesKey.setFont(GeneralConfig.BUTTON_DEFAULT_FONT.deriveFont(Font.PLAIN).deriveFont(15f));
		txtAesKey.setEchoChar(DEFAULT_PSW_CHAR);
		AesCipher.check(txtAesKey);	
		// set default placeholder visible
		txtAesKey.setEchoChar((char) 0);
		
		btnPrevHistory = new JButton("");
		btnPrevHistory.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnPrevHistory.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PREV));
		btnPrevHistory.setToolTipText(TextEditTooltips.tooltipHystoryPrev);
		GuiUtilsExtended.makeTransparentJButton(btnPrevHistory);
		add(btnPrevHistory, "cell 9 0,alignx center,growy");
		
		btnNextHistory = new JButton("");
		btnNextHistory.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnNextHistory.setIcon(new ImageIcon(IconsPathConfigurator.ICON_NEXT));
		btnNextHistory.setToolTipText(TextEditTooltips.tooltipHystoryNext);
		GuiUtilsExtended.makeTransparentJButton(btnNextHistory);
		add(btnNextHistory, "cell 10 0,alignx center,growy");
		
		checkBoxInlineTabs = new JCheckBox("Inline Tabs  ");
		checkBoxInlineTabs.setOpaque(false);
		checkBoxInlineTabs.setSelected(true);
		checkBoxInlineTabs.setToolTipText(TextEditTooltips.tooltipInlineTabs);
		checkBoxInlineTabs.setCursor(GuiUtilsExtended.CURSOR_HAND);
		checkBoxInlineTabs.setForeground(SystemColor.control);
		checkBoxInlineTabs.setBackground(Color.DARK_GRAY);
		checkBoxInlineTabs.setIconTextGap(1);
		checkBoxInlineTabs.setFont(new Font("SansSerif", Font.BOLD, 12));
		add(checkBoxInlineTabs, "cell 12 0,alignx center,aligny center");
		
		chkCutWordsTo = new JCheckBox("Adapt Text Width  ");
		chkCutWordsTo.setOpaque(false);
		chkCutWordsTo.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chkCutWordsTo.setToolTipText(TextEditTooltips.tooltipCutWordsTo);
		chkCutWordsTo.setSelected(false);
		chkCutWordsTo.setForeground(SystemColor.control);
		chkCutWordsTo.setBackground(Color.DARK_GRAY);
		chkCutWordsTo.setIconTextGap(1);
		chkCutWordsTo.setFont(new Font("SansSerif", Font.BOLD, 12));
		add(chkCutWordsTo, "cell 13 0,alignx center,aligny center");
		
		addHandlers();
		
		startAesKeyCheckThread();
		
		logger.info(this.getClass().getName() + ": ready!");
	}
	
	private void startAesKeyCheckThread() {
		// this thread will check every 0.2s the value contained in the textfield
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					AesCipher.check(txtAesKey);
					try {
						Thread.sleep(400);
					} catch (InterruptedException e) {
						logger.error("ERROR in AES checker!", e);
					}
				}
			}
		}).start();
	}
	
	private void addHandlers() {
		
		// PLACEHOLDER AES KEY MANAGER
		txtAesKey.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
				if (txtAesKey.getPassword().length == AES_KEY_PLACEHOLDER.length()) {
					String digits = Arrays.toString(txtAesKey.getPassword());
					digits = digits.replace(", ", "");
					if (digits.substring(1, digits.length() - 1).equals(AES_KEY_PLACEHOLDER)) {
						txtAesKey.setText("");
						if (!chckbxAesShowKey.isSelected()) {
							txtAesKey.setEchoChar(DEFAULT_PSW_CHAR);
						}
					}
				}
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
			}
		});
		
		// SHOW/HIDE PASSWORD DISPLAYED
		chckbxAesShowKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chckbxAesShowKey.isSelected()) {
					txtAesKey.setEchoChar((char) 0);
				} else {
					txtAesKey.setEchoChar(DEFAULT_PSW_CHAR);
				}
			}
		});
		
		// CRYPT TEXT
		btnCrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				if (!isCrypterOn && ScreeptUtils.checkPasswordStrengthDynamic(currentConfiguration.getAesOpt().getSecurityLevel(), txtAesKey, 10)) {
					loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);
					btnCrypt.setCursor(GuiUtilsExtended.CURSOR_WAIT);
					isCrypterOn = true;
					AesCipher.check(txtAesKey);
					String encoded = "";
					if (txtAesKey.getPassword().length != 0) {
						try {
							// loadedTabObj.textArea.getDocument().removeDocumentListener(textAreaChangedListener);
							AesCipher crypter = new AesCipher(txtAesKey.getPassword());
							String displayedSource = loadedTabObj.textArea.getText();
							encoded = crypter.encryptLines(displayedSource);
							// if lentgh more than admitted suggest to use screept
							loadedTabObj.textArea.setText("");
							loadedTabObj.textArea.append(encoded);

						} catch (Throwable e) {
							parent.dialogHelper.error("Something has gone wrong, it's possible that UTF charset can be causing problems :(", "Error");
							logger.error("Exception happened!", e);
							
							txtAesKey.requestFocusInWindow();
						} finally {
							parentUtils.updateCurrentUnsavedFromFileRead();
							parentUtils.revalidateFoldingPainted(loadedTabObj.textArea);
						}
					} else {
						parent.dialogHelper.warn("Please enter a password, empty key text!","Empty password");
						
						txtAesKey.requestFocusInWindow();
					}
					isCrypterOn = false;
					loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
					btnCrypt.setCursor(GuiUtilsExtended.CURSOR_HAND);
				}
			}
		});

		// DECRIPT TEXT
		btnDecrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				if (!isCrypterOn) {
					loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);
					btnDecrypt.setCursor(GuiUtilsExtended.CURSOR_WAIT);
					isCrypterOn = true;
					AesCipher.check(txtAesKey);
					String decoded = "";
					if (txtAesKey.getPassword().length != 0) {
						try {
							AesCipher decrypter = new AesCipher(txtAesKey.getPassword());
							String displayedSource = loadedTabObj.textArea.getText();
							decoded = decrypter.decryptLines(displayedSource);
							loadedTabObj.textArea.setText("");
						} catch (Exception e) {
							parent.dialogHelper.error("Something has gone wrong, it's possible that UTF charset can be causing problems :(", "Error");

							logger.error("Exception happened!", e);
							txtAesKey.requestFocusInWindow();
						} finally {
							loadedTabObj.textArea.append(decoded);
							parentUtils.updateCurrentUnsavedFromFileRead();
							parentUtils.revalidateFoldingPainted(loadedTabObj.textArea);
						}
					} else {
						parent.dialogHelper.warn("Please enter a password, empty key text!","Empty password");
						
						txtAesKey.requestFocusInWindow();
					}
					isCrypterOn = false;
					loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
					btnDecrypt.setCursor(GuiUtilsExtended.CURSOR_HAND);
				}
			}
		});

		// SET SPEAKER'S VOICE SPEED
		sliderSpokenSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int rate = sliderSpokenSpeed.getValue();
				currentConfiguration.getTextEditorOpt().setVoiceRate(rate);
				if (parent.subHeaderPanel.objSpeaker != null) {
					parent.subHeaderPanel.objSpeaker.voice.setDurationStretch((rate));
				}
			}
		});
		
		// CHANGE TAB VISUALIZATION MODE
		checkBoxInlineTabs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doInlinedCheck();
			}
		});
		
		// ADAPT TO WIDTH
		if (chkCutWordsTo.isSelected()) {
			parent.getCurrentTabEditor().loadedTabObj.textArea.setLineWrap(true);
		}
		chkCutWordsTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				if (chkCutWordsTo.isSelected()) {
					loadedTabObj.textArea.setLineWrap(true);
					currentConfiguration.getTextEditorOpt().breaklineActive = true;
				} else {
					loadedTabObj.textArea.setLineWrap(false);
					loadedTabObj.textArea.setWrapStyleWord(false);
					currentConfiguration.getTextEditorOpt().breaklineActive = false;
				}
			}
		});
		
		// PREV IN HISTORY
		btnPrevHistory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.getCurrentTabEditor().goToPreviousInHistory();
			}
		});

		// NEXT IN HISTORY
		btnNextHistory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.getCurrentTabEditor().goToNextInHistory();
			}
		});
	}
	
	public void doInlinedCheck() {
		if (checkBoxInlineTabs.isSelected()) {
			currentConfiguration.getTextEditorOpt().inlinedTabs = true;
			parent.getCurrentTabEditor().mainTabList.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		} else {
			currentConfiguration.getTextEditorOpt().inlinedTabs = false;
			parent.getCurrentTabEditor().mainTabList.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		}
		currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "inlineTabs", currentConfiguration.getTextEditorOpt().inlinedTabs);
	}
	
}
