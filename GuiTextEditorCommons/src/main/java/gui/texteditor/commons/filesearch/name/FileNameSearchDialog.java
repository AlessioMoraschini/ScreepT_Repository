package gui.texteditor.commons.filesearch.name;

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
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import gui.commons.frameutils.frame.arch.ParentDialog;
import gui.commons.list.FileDropList;
import initializer.configs.impl.INItializer;
import initializer.configs.impl.om.FileOptions;
import initializer.configs.impl.om.TextEditorOption;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import various.common.light.files.FileVarious;
import various.common.light.files.om.FileSearchDTO;
import various.common.light.files.search.FileNameSearchAdvanced;
import various.common.light.files.search.FileNameSearchAdvanced.SearchType;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.ScreepTGuiFactory;
import various.common.light.gui.dnd.FileDropListenerSingle;
import various.common.light.utility.string.StringWorker;
import various.common.light.utility.time.AmAsyncAction;

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
	/** Names of the files under the root, read once in background: typing only filters it */
	private volatile FileNameIndex index;
	private static final java.util.Map<String, FileNameIndex> INDEX_CACHE = new java.util.concurrent.ConcurrentHashMap<String, FileNameIndex>();
	private static final long INDEX_MAX_AGE_MS = 5 * 60 * 1000L;
	private final java.util.concurrent.atomic.AtomicInteger searchSequence = new java.util.concurrent.atomic.AtomicInteger();
	private final java.util.concurrent.ExecutorService searchExecutor = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "file-name-search");
		t.setDaemon(true);
		return t;
	});

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

		keyTimer = new AmAsyncAction(action, 120);
		if(startTimer)
			keyTimer.executeDelayedAsync();
	}

	public void setItemJPopUpMenu(JPopupMenu menu) {
		fileDropList.setPopUpMenu(menu);
	}
	public JPopupMenu getItemJPopUpMenu() {
		return fileDropList.getPopUpMenu();
	}

	private void initGUI() {
		setLayout(new MigLayout(
				"insets 4, hidemode 2",
				"[][][60px, grow]",
				"[25px:25px][30px, grow][][25px:25px:25px]"));

		textField = ScreepTGuiFactory.getTextField(null, null, Color.LIGHT_GRAY, ScreepTGuiFactory.DEFAULT_TEXT_AREA_FONT.deriveFont(15f), false);
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

		lblPopUpMessage = ScreepTGuiFactory.getLabel("", null, null, null, true);
		lblPopUpMessage.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GEN_WARNING));
		lblPopUpMessage.setVisible(false);
		lblPopUpMessage.setBorder(new LineBorder(Color.GRAY));
		add(lblPopUpMessage, "cell 0 2 3 1,grow");

		radioUseWorkspace = ScreepTGuiFactory.getRadioButton(
				"Use workspace",
				"When selected, workspace will be used as root. If not selected, the manual root folder will be used (click on root dir name to change it)",
				false);
		add(radioUseWorkspace, "cell 0 3 1 1,grow");

		lblCurrentRootFolder = ScreepTGuiFactory.getLabel("", null, null, null, true);
		lblCurrentRootFolder.setCursor(GuiUtils.CURSOR_HAND);
		add(lblCurrentRootFolder, "cell 1 3 1 1,grow");

		lblCurrentSelectionPath = ScreepTGuiFactory.getLabel(NO_SELECTION, null, null, null, true);
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

		rebuildIndex();

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

	/** Reads the names of the files under the root in background (the previous reading is abandoned) */
	private void rebuildIndex() {
		final FileNameIndex previous = index;
		if (previous != null) {
			previous.cancel();
		}
		final String key = FileVarious.getCanonicalPathSafe(rootDir);
		final FileNameIndex cached = INDEX_CACHE.get(key);
		if (cached != null && cached.isReady() && System.currentTimeMillis() - cached.builtAt < INDEX_MAX_AGE_MS) {
			index = cached; // the same folder was read a moment ago
			setPopUpWarning(cached.isTruncated() ? "Huge folder: only the first " + cached.size() + " files are searched" : "");
			searchCurrent();
			return;
		}
		final FileNameIndex fresh = new FileNameIndex(rootDir);
		index = fresh;
		setPopUpWarning("Reading the files of the folder (the first time it can take a while)...");
		searchExecutor.execute(() -> {
			fresh.build();
			if (fresh.isReady()) {
				INDEX_CACHE.put(key, fresh);
			}
			if (index != fresh) {
				return; // root changed meanwhile
			}
			SwingUtilities.invokeLater(() -> {
				setPopUpWarning(fresh.isTruncated() ? "This folder has a huge number of files: only the first " + fresh.size() + " are searched. Use a subfolder as root" : "");
				searchCurrent();
			});
		});
	}

	private void searchCurrent() {
		final String searchString = textField.getText();
		if(StringWorker.isEmpty(searchString))
			return;

		final FileNameIndex current = index;
		if (current == null || (!current.isReady() && current.size() == 0)) {
			return; // the search runs again when the reading ends (a partial reading is already searched)
		}

		final int sequence = searchSequence.incrementAndGet();
		final int limit = (int) Math.min(Integer.MAX_VALUE, fileDropList.getFileViewLimit());
		searchExecutor.execute(() -> {
			if (sequence != searchSequence.get()) {
				return;
			}
			final List<File> matches = current.search(searchString, limit, () -> sequence != searchSequence.get());
			if (matches == null) {
				return; // a newer search is on its way
			}
			SwingUtilities.invokeLater(() -> {
				if (sequence != searchSequence.get()) {
					return;
				}
				if (matches.size() >= limit)
					setPopUpWarning("Result number limited to " + matches.size() + " results");
				else
					setPopUpWarning(current.isTruncated() ? "Huge folder: only the first " + current.size() + " files are searched" : null);

				fileDropList.setFiles(matches);
			});
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
