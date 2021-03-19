package gui.filesearch.name;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import files.FileVarious;
import files.om.FileSearchDTO;
import files.search.FileNameSearchAdvanced;
import files.search.FileNameSearchAdvanced.SearchType;
import frameutils.frame.arch.ParentDialog;
import frameutils.frame.panels.arch.ParentPanel;
import gui.GuiUtils;
import gui.dnd.FileDropListenerSingle;
import impl.INItializer;
import impl.om.FileOptions;
import impl.om.TextEditorOption;
import list.FileDropList;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import utility.string.StringWorker;
import utility.time.AmAsyncAction;

public class FileNameSearchDialog extends ParentDialog {
	private static final long serialVersionUID = 5026956746595091072L;
	
	private static final String NO_SELECTION = "Nothing selected";
	
	private static final Long MAX_FILE_NUMBER_ALERT = 12000L;

	private JTextField textField;
	private FileDropList fileDropList;
	private JRadioButton radioUseWorkspace;
	private JLabel lblCurrentSelectionPath;
	private JLabel lblPopUpMessage;
	private JLabel lblCurrentRootFolder;
	
	private FileDropListenerSingle doubleClickItemAction;
	
	private File rootDir;
	
	private AmAsyncAction keyTimer;
	
	public FileNameSearchDialog() {
		this(null, null);
	}

	public FileNameSearchDialog(JFrame parentFrame, INItializer configuration) {
		super(parentFrame, configuration);
//		this.rootDir = rootDir != null 
//				? rootDir.isDirectory()
//						? rootDir
//						: rootDir.getParentFile()
//				: new File("./");
		
		GuiUtils.setDialogModalType(this, ModalityType.APPLICATION_MODAL);
		initGUI();
		addHandlers();
		
		setIconImage(new ImageIcon(IconsPathConfigurator.ICON_SEARCH).getImage());
		
		setDoubleClickItemAction(null);
		GuiUtils.centerComponent(this, getDefaultDimension());
		
		SwingUtilities.invokeLater(()->{
			textField.setRequestFocusEnabled(true);
			textField.requestFocus();
			textField.requestFocusInWindow();
			
			radioUseWorkspace.setSelected(getFileOptions() != null && !getFileOptions().isfNameSearchUseManual());
			doRadioAction();
			setRootDir(this.rootDir);
		});
	}
	
	private boolean isTimerActive() {
		boolean inactive = keyTimer == null || keyTimer.isAborted() || keyTimer.isExecuted();
		return !inactive || (keyTimer != null && keyTimer.isRunning());
	}
	
	private void resetTimer(Runnable action, boolean startTimer) {
		if(isTimerActive()) {
			keyTimer.abort();
		}
			
		keyTimer = new AmAsyncAction(action, 300);
		if(startTimer)
			keyTimer.executeDelayedAsync();
	}
	
	private void initGUI() {
		setLayout(new MigLayout(
				"insets 4, hidemode 2", 
				"[][][60px, grow]", 
				"[25px:25px][30px, grow][][25px:25px:25px]"));
		
		textField = ParentPanel.getTextField(null, null, Color.LIGHT_GRAY, ParentPanel.DEFAULT_TEXT_AREA_FONT.deriveFont(15f), false);
		add(textField, "cell 0 0 3 1,grow");
		
		fileDropList = new FileDropList();
		fileDropList.setDropEnabled(false);
		add(fileDropList, "cell 0 1 3 1,grow");
		
		fileDropList.setListSingleClickAction((fileClicked)->{
			if(fileClicked != null && fileClicked.exists())
				try {
					lblCurrentSelectionPath.setText(FileVarious.getPathRelativeToOtherFile(
							fileClicked,
							rootDir));
				} catch (IOException e) {
					logger.warn("Error relativizing path", e);
					lblCurrentSelectionPath.setText(FileVarious.getNlevelsPathString(fileClicked, 4));
				}
			else
				lblCurrentSelectionPath.setText(NO_SELECTION);
		});
		
		lblPopUpMessage = ParentPanel.getLabel("", null, null, null, true);
		lblPopUpMessage.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GEN_WARNING));
		lblPopUpMessage.setVisible(false);
		lblPopUpMessage.setBorder(new LineBorder(Color.GRAY));
		add(lblPopUpMessage, "cell 0 2 3 1,grow");

		radioUseWorkspace = ParentPanel.getRadioButton(
				"Use workspace", 
				"When selected, workspace will be used as root. If not selected, the manual root folder will be used (click on root dir name to change it)", 
				false);
		add(radioUseWorkspace, "cell 0 3 1 1,grow");

		lblCurrentRootFolder = ParentPanel.getLabel("", null, null, null, true);
		lblCurrentRootFolder.setCursor(GuiUtils.CURSOR_HAND);
		add(lblCurrentRootFolder, "cell 1 3 1 1,grow");
		
		lblCurrentSelectionPath = ParentPanel.getLabel(NO_SELECTION, null, null, null, true);
		add(lblCurrentSelectionPath, "cell 2 3 1 1,grow");
		
		applyDarkColor();
		
		setUndecorated(false);
		setTitle("Search files by name");
		this.setFocusable(true);
		
		KeyAdapter adapter = getEscAdapter();
		this.addKeyListener(adapter);
		fileDropList.addKeyListener(adapter);
		fileDropList.getList().addKeyListener(adapter);
		textField.addKeyListener(adapter);
		
		setAlwaysOnTop(true);
	}
	
	public void setPopUpWarning(String message) {
		if(StringWorker.isEmpty(message)) {
			lblPopUpMessage.setVisible(false);
			lblPopUpMessage.setText("");
		} else {
			lblPopUpMessage.setVisible(true);
			lblPopUpMessage.setText(message);
		}
	}
	
	public File getRootDir() {
		return rootDir;
	}

	public void setRootDir(File rootDir) {
		this.rootDir = rootDir;
		lblCurrentRootFolder.setText(FileVarious.getCanonicalFileSafe(rootDir).getName());
		lblCurrentRootFolder.setToolTipText(FileVarious.getCanonicalPathSafe(rootDir));
		
		if(radioUseWorkspace.isSelected())
			radioUseWorkspace.setText("Use workspace: ");
		else
			radioUseWorkspace.setText("Use custom folder: ");
		
		new Thread(()-> {
			if(FileVarious.checkIfContainsMoreFilesThanMax(this.rootDir, true, MAX_FILE_NUMBER_ALERT)) {
				setPopUpWarning("This root directory seems to be huge, searching here can be very slow! Try to use a subfolder as root");
			} else {
				setPopUpWarning("");
			}
		}).start();
			
	}

	private KeyAdapter getEscAdapter() {
		return new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));
			}
		};
	}
	
	public void applyDarkColor() {
		textField.setBackground(Color.DARK_GRAY);
		textField.setForeground(Color.LIGHT_GRAY);

		getContentPane().setBackground(Color.DARK_GRAY.darker());
		setBackground(Color.DARK_GRAY.darker());
		lblPopUpMessage.setBackground(Color.DARK_GRAY.darker());
		lblPopUpMessage.setForeground(Color.LIGHT_GRAY);
		lblCurrentSelectionPath.setBackground(Color.DARK_GRAY);
    	lblCurrentSelectionPath.setForeground(Color.LIGHT_GRAY);
    	lblCurrentRootFolder.setBackground(Color.DARK_GRAY);
    	lblCurrentRootFolder.setForeground(Color.LIGHT_GRAY);
    	radioUseWorkspace.setBackground(Color.DARK_GRAY);
    	radioUseWorkspace.setForeground(Color.LIGHT_GRAY);
    }

	private void addHandlers() {
		
		textField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				resetTimer(()->{searchCurrent();}, true);
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				resetTimer(()->{searchCurrent();}, true);
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				resetTimer(()->{searchCurrent();}, true);
			}
		});
		
		lblCurrentRootFolder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(!radioUseWorkspace.isSelected())
					chooseManualDir();
			}
		});
		
		radioUseWorkspace.addActionListener((e) -> {
			doRadioAction();
		});
		
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));
			}
		});
	}
	
	private void doRadioAction() {
		if(getFileOptions() != null)
			getFileOptions().setfNameSearchUseManual(!radioUseWorkspace.isSelected());
		
		lblCurrentRootFolder.setCursor(radioUseWorkspace.isSelected() ? GuiUtils.CURSOR_DEFAULT : GuiUtils.CURSOR_HAND);

		if(radioUseWorkspace.isSelected()) {
			File workspace = getCurrentWorkspace();
			if(workspace != null && workspace.exists() && workspace.isDirectory()) {
				setRootDir(workspace);
				searchCurrent();
				return;
			}
		} 

		File manualDir = getFileOptions().getfNameSearchManualDir();
		if(manualDir.exists() && manualDir.isDirectory()) {
			setRootDir(manualDir);
		} else {
			chooseManualDir();
		}
		
		searchCurrent();
	}
	
	private void chooseManualDir() {
		File chosen = fileChooser.directoRead(parentFrame, "Choose new root folder to search into");
		if(chosen != null && chosen.exists() && chosen.isDirectory()) {
			synchronized (rootDir) {
				setRootDir(chosen);
			}
			if(getFileOptions() != null) {
				getFileOptions().setfNameSearchManualDir(chosen);
			}
		}
	}
	
	private FileOptions getFileOptions() {
		if(configuration != null)
			return configuration.getFileOpt();
		else
			return null;
	}
	private TextEditorOption getTxtOptions() {
		if(configuration != null)
			return configuration.getTextEditorOpt();
		else
			return null;
	}

	private File getCurrentWorkspace() {
		if(getTxtOptions() != null) {
			File workspace = new File(getTxtOptions().defaultWorkspacePath);
			if(workspace.exists()) {
				if(!workspace.isDirectory())
					workspace = workspace.getParentFile();
				
				return workspace;
			}
			
		}

		return GeneralConfig.USER_DESKTOP_FILE;
	}
	
	private FileSearchDTO getDto(String searchString) {
		return new FileSearchDTO(rootDir, searchString, false, SearchType.FILE_ONLY, true, false, true);
	}
	
	private void searchCurrent() {
		GuiUtils.launchThreadSafeSwingV2(() -> {
			String searchString = textField.getText();
			if(StringWorker.isEmpty(searchString))
				return;
			
			// Match everything that starts with current string entered
			searchString = "*".equals(searchString.trim()) ? "*" : (searchString.trim() + "*");
			
			List<File> matches = FileNameSearchAdvanced.findByMatch(getDto(searchString), fileDropList.getFileViewLimit());
			
			if(matches != null && matches.size() >= fileDropList.getFileViewLimit())
				setPopUpWarning("Result number limited to " + matches.size() + " results");
			else
				setPopUpWarning(null);
			
			fileDropList.setFiles(matches);
		});
	}
	
	public FileDropListenerSingle getDoubleClickItemAction() {
		return doubleClickItemAction;
	}

	public void setDoubleClickItemAction(FileDropListenerSingle doubleClickItemAction) {
		this.doubleClickItemAction = doubleClickItemAction != null ? doubleClickItemAction : (e) -> {};
		fileDropList.setListDoubleClickAction((e)->{
			this.doubleClickItemAction.fileDropped(e);
			dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));
		});
	}

	@Override
	protected void setCloseAction() {
		super.setCloseAction();
	}

	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(950, 500);
	}
}
