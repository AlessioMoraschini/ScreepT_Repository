package gui.filesearch.name;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import files.FileVarious;
import files.om.FileSearchDTO;
import files.search.FileNameSearchAdvanced;
import files.search.FileNameSearchAdvanced.SearchType;
import frameutils.frame.arch.ParentFrame;
import frameutils.frame.panels.arch.ParentPanel;
import gui.GuiUtils;
import gui.dnd.FileDropListenerSingle;
import impl.INItializer;
import list.FileDropList;
import net.miginfocom.swing.MigLayout;
import resources.IconsPathConfigurator;
import utility.string.StringWorker;

public class FileNameSearchDialog extends ParentFrame {
	private static final long serialVersionUID = 5026956746595091072L;
	
	private static final String NO_SELECTION = "Nothing selected";

	private JTextField textField;
	private FileDropList fileDropList;
	private JLabel lblCurrentSelectionPath;
	
	private FileDropListenerSingle doubleClickItemAction;
	
	private File rootDir;
	
	public FileNameSearchDialog(File rootDir) {
		this(null, null, rootDir);
	}

	public FileNameSearchDialog(JFrame parentFrame, INItializer configuration, File rootDir) {
		super(parentFrame, configuration);
		this.rootDir = rootDir != null 
				? rootDir.isDirectory()
						? rootDir
						: rootDir.getParentFile()
				: new File("./");
		
		initGUI();
		addHandlers();
		
		GuiUtils.setFrameIcon(new ImageIcon(IconsPathConfigurator.ICON_SEARCH), this);
		
		setDoubleClickItemAction(null);
		GuiUtils.centerComponent(this, getDefaultDimension());
		
		SwingUtilities.invokeLater(()->{
			textField.setRequestFocusEnabled(true);
			textField.requestFocus();
			textField.requestFocusInWindow();
		});
	}
	
	private void initGUI() {
		setLayout(new MigLayout(
				"insets 4, hidemode 2", 
				"[60px, grow]", 
				"[25px:25px][30px, grow][25px:25px:25px]"));
		
		textField = ParentPanel.getTextField(null, null, Color.LIGHT_GRAY, ParentPanel.DEFAULT_TEXT_AREA_FONT.deriveFont(15f), false);
		add(textField, "cell 0 0 1 1,grow");
		
		fileDropList = new FileDropList();
		fileDropList.setDropEnabled(false);
		add(fileDropList, "cell 0 1 1 1,grow");
		
		fileDropList.setListSingleClickAction((fileClicked)->{
			if(fileClicked != null && fileClicked.exists())
				lblCurrentSelectionPath.setText("Selected file: " + FileVarious.getNlevelsPathString(fileClicked, 4));
			else
				lblCurrentSelectionPath.setText(NO_SELECTION);
		});
		
		lblCurrentSelectionPath = ParentPanel.getLabel(NO_SELECTION, null, null, null, true);
		add(lblCurrentSelectionPath, "cell 0 2 1 1,grow");
		
		applyDarkColor();
		
		setUndecorated(true);
		this.setFocusable(true);
		
		KeyAdapter adapter = getEscAdapter();
		this.addKeyListener(adapter);
		fileDropList.addKeyListener(adapter);
		fileDropList.getList().addKeyListener(adapter);
		textField.addKeyListener(adapter);
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

		setBackground(Color.DARK_GRAY);
		lblCurrentSelectionPath.setBackground(Color.DARK_GRAY);
    	lblCurrentSelectionPath.setForeground(Color.LIGHT_GRAY);
    }

	private void addHandlers() {
		
		textField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				searchCurrent();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				searchCurrent();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				searchCurrent();
			}
		});
	}
	
	private FileSearchDTO getDto(String searchString) {
		return new FileSearchDTO(rootDir, searchString, false, SearchType.FILE_ONLY, true, false, true);
	}
	
	private void searchCurrent() {
		String searchString = textField.getText();
		if(StringWorker.isEmpty(searchString))
			return;
		
		// Match everything that starts with current string entered
		searchString = "*".equals(searchString.trim()) ? "*" : (searchString.trim() + "*");
		
		List<File> matches = FileNameSearchAdvanced.findByMatch(getDto(searchString));
		
		fileDropList.setFiles(matches);
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
		return new Dimension(650, 250);
	}
}
