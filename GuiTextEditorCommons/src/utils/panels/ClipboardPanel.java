package utils.panels;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import amfileutils.FileStuff;
import dialogutils.JOptionHelper;
import frameutils.utils.GuiUtils;
import guistyle.GUIStyelSettings;
import mainobj.INItializer;
import net.miginfocom.swing.MigLayout;
import om.TextEditorOption;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import utils.RsyntaxUtils;
import utils.dialog.ITextAreaDialogs;
import utils.panels.arch.IParentPanel;
import utils.panels.arch.ParentPanel;

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.border.LineBorder;
import java.awt.Font;

public class ClipboardPanel  extends ParentPanel implements IParentPanel{

	// ClipboardPanel logger creation
	static Logger logger = Logger.getLogger(ClipboardPanel.class);
	JOptionHelper dialogHelper;
	
	public static ClipboardPanel singletonInstance;

	// FIRST is main clipboard
	public String KEY_clipBoardMain = GeneralConfig.CLIPBOARD_PANEL_PREFIX+0;
	public String currentClipboard = KEY_clipBoardMain;
	
	public RSyntaxTextArea txtAreaClipboard;
	private JScrollPane txtAreaContainer;
	private JScrollPane currentScrollPane;
	
	JButton btnSave;
	JButton btnReload;
	JButton btnReloadClipboards;
	JComboBox<String> comboClipboards;
	private JLabel lblClipboardPanel;
	private JSeparator separator;
	private JSeparator separator_1;
	private JSeparator separator_2;
	
	private TextEditorOption options;
	private INItializer config;
	
	/**
	 * Initialize panel with target directory to write in memory files, which has custom behaviour 
	 * according to the interface passed as parameter.
	 *  
	 *  The file will be inserted as clipboardMain in current map
	 *  
	 * @param dialogManager
	 * @param clipboardPers
	 */
	public ClipboardPanel(ITextAreaDialogs dialogManager, INItializer options) {
		
		this.options = (config != null)? config.getTextEditorOpt() : new TextEditorOption();
		this.config = (config != null)? config : new INItializer();
		
		this.dialogHelper = new JOptionHelper(this);
		
		this.dataMap = GeneralConfig.CLIPBOARD_PANEL_MAP;
		this.txtAreaClipboard = new RSyntaxTextArea();
		txtAreaClipboard.setMaximumSize(new Dimension(1500, GuiUtils.getScreenSize().height));
		this.txtAreaContainer = new JScrollPane();
		txtAreaContainer.setMinimumSize(new Dimension(18, 18));
		txtAreaContainer.setPreferredSize(new Dimension(20, 20));
		txtAreaContainer.setViewportView(txtAreaClipboard);
		this.currentScrollPane = new JScrollPane();
		setLayout(new MigLayout("fill, insets 1, hidemode 2", "[200px,grow]", "[270px,grow]"));
		
		setBorder(new EmptyBorder(0,0,0,0));

		add(currentScrollPane, "cell 0 0,grow");
		currentScrollPane.setBorder(new EmptyBorder(0,0,0,0));
		currentScrollPane.setBackground(Color.black);
//		currentScrollPane.getVerticalScrollBar().setUnitIncrement(GeneralConfig.FAST_WHEEL_SPEED);
		currentScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		currentScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.dialogManager = dialogManager;
		
		// main panel layout
		MigLayout panelLayout = new MigLayout("insets 10, hidemode 2", "[::20px,grow][::20px,grow][5px,grow][90px][5px,grow][][]", "[:12px:15px][25px][5px][30px,grow][30px,grow][][]");

		// main layout
		setLayout(new MigLayout("fill, insets 1, hidemode 2", "[50px,grow]", "[50px,grow]"));
		
		// add main pane to main scroller
		add(currentScrollPane, "cell 0 0,grow");
		JPanel mainPane = new JPanel();
		mainPane.setBackground(Color.DARK_GRAY);
		mainPane.setLayout(panelLayout);
		currentScrollPane.setViewportView(mainPane);
		
		separator = new JSeparator();
		separator.setBackground(Color.GRAY);
		mainPane.add(separator, "cell 0 0 3 1,growx");
		
		lblClipboardPanel = new JLabel("ClipBoard Panel");
		lblClipboardPanel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblClipboardPanel.setForeground(Color.LIGHT_GRAY);
		mainPane.add(lblClipboardPanel, "cell 3 0,alignx center");
		
		separator_1 = new JSeparator();
		separator_1.setBackground(Color.GRAY);
		mainPane.add(separator_1, "cell 4 0 3 1,growx");
		
		// Add subcomponents to main pane
		btnSave = new JButton("");
		btnSave.setMinimumSize(new Dimension(20, 20));
		btnSave.setMaximumSize(new Dimension(20, 20));
		mainPane.add(btnSave, "cell 0 1,alignx center");
		btnSave.setCursor(GuiUtils.CURSOR_HAND);
		GuiUtils.makeTransparentJButton(btnSave);
		btnSave.setToolTipText("Save current Clipboard");
		btnSave.setForeground(Color.BLACK);
		btnSave.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_SAVE));
		
		btnReload = new JButton("");
		btnReload.setPreferredSize(new Dimension(20, 20));
		btnReload.setMinimumSize(new Dimension(20, 9));
		btnReload.setMaximumSize(new Dimension(20, 20));
		mainPane.add(btnReload, "cell 1 1,alignx center");
		btnReload.setCursor(GuiUtils.CURSOR_HAND);
		GuiUtils.makeTransparentJButton(btnReload);
		btnReload.setToolTipText("Reload current Clipboard (be careful, unsaved changes will be lost)");
		btnReload.setForeground(Color.BLACK);
		btnReload.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
		
		String[] clipBoards = dataMap.keySet().toArray(new String[2]);
		comboClipboards = new JComboBox<>(clipBoards);
		comboClipboards.setCursor(GuiUtils.CURSOR_HAND);
		comboClipboards.setToolTipText("Choose between loaded Clipboards");
		comboClipboards.setForeground(Color.BLACK);
		mainPane.add(comboClipboards, "cell 2 1 4 1,growx");
		
		btnReloadClipboards = new JButton("");
		btnReloadClipboards.setToolTipText("Reload Clipboard List");
		btnReloadClipboards.setPreferredSize(new Dimension(20, 20));
		btnReloadClipboards.setMinimumSize(new Dimension(20, 9));
		btnReloadClipboards.setMaximumSize(new Dimension(20, 20));
		btnReloadClipboards.setCursor(GuiUtils.CURSOR_HAND);
		GuiUtils.makeTransparentJButton(btnReloadClipboards);
		btnReloadClipboards.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD2));
		btnReloadClipboards.setForeground(Color.BLACK);
		mainPane.add(btnReloadClipboards, "cell 6 1");
		
		separator_2 = new JSeparator();
		separator_2.setBackground(Color.GRAY);
		mainPane.add(separator_2, "cell 0 2 7 1,growx");
		
		mainPane.add(txtAreaContainer, "cell 0 3 7 4,grow");
		txtAreaClipboard.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		txtAreaClipboard.setFont(GeneralConfig.TXT_EDITOR_CLIPBOARD_FONT);
		txtAreaClipboard.setAntiAliasingEnabled(true);
		txtAreaClipboard.setWrapStyleWord(true);
		txtAreaClipboard.setLineWrap(true);
		txtAreaClipboard.setEditable(true);

		addHandlers();
		
		GUIStyelSettings.applyNimbusSize(0);
		
		File mainClip = dataMap.get(KEY_clipBoardMain);
		if(!mainClip.exists()) {
			try {
				mainClip.createNewFile();
			} catch (IOException e) {
				logger.error("Error when trying to load file", e);
			}
		}else {
			try {
				reload(KEY_clipBoardMain);
			} catch (IOException e) {
				logger.error("Error initializing clipboard Main at start");
			}
		}
		
		FileStuff.loadFileTxtArea(dataMap.get(KEY_clipBoardMain), txtAreaClipboard, null, null);
	}
	
	private void addHandlers() {
		
		// Load action
		btnReload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (currentClipboard != null) {
							try {
								reload(currentClipboard);
							} catch (IOException e1) {
								logger.error("Error reloading current Clipboard panel from file.");
							}
						}
					}
				});
			}
		});

		// Save action
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (currentClipboard != null) {
							try {
								doAction(currentClipboard);
							} catch (IOException e1) {
								String msg = "Error Saving current changes to file: " + GeneralConfig.CLIPBOARD_PANEL_MAP.get(currentClipboard);
								logger.error(msg);
								dialogHelper.warn(msg, null);
							}
						}
					}
				});
			}
		});
		
		// Combo list refresh when clicking on reload
		comboClipboards.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentClipboard != null) {
					saveCurrClipboardSilent();
				}
				currentClipboard = (String)comboClipboards.getSelectedItem();
				if (currentClipboard != null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								reload(currentClipboard);
								options.lastFileOpenedClipboard = currentClipboard;
								config.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "lastFileOpenedClipboard", options.lastFileOpenedClipboard);
							} catch (IOException e1) {
								logger.error("Error reloading current Clipboard panel from file.");
							}
						}
					});
				}
			}
		});
		
		// reload list of selectable elems from props
		btnReloadClipboards.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						saveCurrClipboardSilent();
						reloadComboClipboards();
					}
				});
			}
		});
	}

	/**
	 * This method writes clipBoard content into the file memorized in this map with key equals to the given string
	 * As target you should use one that is present in generalConfig panel clipboard map
	 * @throws IOException 
	 */
	@Override
	public void doAction(String target) throws IOException {
		File selected = dataMap.get(target);
		if(selected != null) {
			selected = new File(GeneralConfig.PANEL_TEMP_FOLDER+selected.getName());
			selected.mkdirs();
			if(!selected.exists()) {
				selected.createNewFile();
			}
			
			if(FileStuff.writeStringToFile(txtAreaClipboard.getText(), selected, true)) {
				logger.debug("Current clipboard written: " + selected);
				dialogHelper.info("Current clipboard written: " + selected.getAbsolutePath(), " saved!");
			}else {
				dialogHelper.error("There was an error writing to "+ selected, "Error during file write");
				logger.error("There was an error writing to " + selected);
			}
		}else {
			dialogHelper.warn("Given file is NULL, cannot write.", null);
			logger.error("Given file is NULL, cannot write.");
		}
	}
	
	public void saveCurrClipboardSilent() {
		try {
			File selected = dataMap.get(currentClipboard);
			FileStuff.writeStringToFile(txtAreaClipboard.getText(), selected, true);
			logger.info(selected + "Written to file.");
		} catch (Exception e) {
			logger.warn("Erro saving current clipboard");
		}
	}
	
	/**
	 * This method reinitialize the panel based on data contained in file identified by target key.
	 * You can refer to generalConfig to know which kind of panel you can call and their relative files
	 * 
	 * @throws IOException 
	 */
	@Override
	public void reload(String target) throws IOException {
		File selected = dataMap.get(target);
		// Create fake safe unexisting file to avoid null pointer
		if(selected == null) {
			dialogHelper.warn("Given file is NULL, cannot load content.", null);
			logger.error("Given file is NULL, cannot load content.");
			return;
		}
		
		if(!selected.exists()) {
			selected.createNewFile();
		}
		
		// Update text from file
		txtAreaClipboard.setText("");
		FileStuff.appendFileTxtArea(selected, txtAreaClipboard, true);
		
		logger.debug("Current clipboard reloaded from file: " + selected);
		currentClipboard = target;
		
	}

	@Override
	public void sync() {
		RSyntaxTextArea reference = dialogManager.getTargetTextArea();
		if (reference != null && txtAreaClipboard != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// Get diyamic reference from interface taken from main editor utils class
					txtAreaClipboard.setBackground(reference.getBackground());
					RsyntaxUtils.setBackground(txtAreaClipboard, reference.getBackground());
					txtAreaClipboard.setForeground(reference.getForeground());
					RsyntaxUtils.setForeground(txtAreaClipboard, reference.getCaretColor());
					txtAreaClipboard.setCurrentLineHighlightColor(txtAreaClipboard.getBackground().brighter());
					txtAreaClipboard.setFont(GeneralConfig.TXT_EDITOR_CLIPBOARD_FONT);
					dialogManager.loadThemeIntoTarget(txtAreaClipboard);
					currentScrollPane.setBackground(Color.black);
					txtAreaClipboard.setWrapStyleWord(true);
					txtAreaClipboard.setLineWrap(true);
				}
			});
		}
	}
	
	// COMBO METHODS
	
	public void reloadComboClipboards() {
		comboClipboards.removeAllItems();
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>( GeneralConfig.getRefreshedClipBoardsMap().keySet().toArray(new String[2]) );
		comboClipboards.setModel( model );
		comboClipboards.setSelectedItem(options.lastFileOpenedClipboard);
		comboClipboards.revalidate();
		comboClipboards.repaint();
	}
	
	public void selectFromComboClipboardsByFile(String fileName) {
		for(String key : dataMap.keySet()){
			String currentName = dataMap.get(key).getName();
			if(currentName.equals(fileName)) {
				comboClipboards.setSelectedItem(key);
			}
			
			comboClipboards.revalidate();
			comboClipboards.repaint();
		}
	}

	public void selectFromComboClipboards(String key) {
		comboClipboards.setSelectedItem(key);
		comboClipboards.revalidate();
		comboClipboards.repaint();
	}
	
	public String getCrrentFromCombo() {
		return (String)comboClipboards.getSelectedItem();
	}
	
	
	public void addClipBoard(String ID, File fileRef) {
		dataMap.put(ID, fileRef);
	}

	public void addClipBoards(HashMap<String, File> map) {
		dataMap.putAll(map);
	}

	public ITextAreaDialogs getDialogManager() {
		return dialogManager;
	}
	
	/**
	 * Get a shared instance of text area, useful if used only one at once
	 */
	public static ClipboardPanel getSingletonLazy(ITextAreaDialogs dialogManager, INItializer config) {
		singletonInstance = (singletonInstance == null)? new ClipboardPanel(dialogManager, config) : singletonInstance;
		return singletonInstance;
	}
}
