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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Timer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import various.common.light.i18n.I18n;
import gui.commons.dialogutils.NativeFileChooser;
import gui.commons.imagestudio.DialogFit;
import gui.commons.frameutils.frame.arch.ParentFrame;
import gui.commons.frameutils.frame.arch.ToolIcons;
import gui.commons.frameutils.frame.arch.ToolStyle;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.commons.treeutils.om.FileTreeModel;
import initializer.configs.impl.INItializer;
import maingUI.MainGUI;
import net.miginfocom.swing.MigLayout;
import resources.IconsPathConfigurator;
import screept.gui.utils.tree.keydictionary.TreeFileMouseListenerDictionary;
import various.common.light.files.FileWorker;
import various.common.light.om.BufferedImageType;
import various.common.light.utility.manipulation.ConversionUtils;

/**
 * Key images (png) used for the encryption. The list refreshes by itself when files change in the folder
 * (checked every 2 seconds, the tree is rebuilt only when something changed).
 */
public class KeyDictionaryFrame extends ParentFrame {
	private static final long serialVersionUID = -2183390950352993867L;

	public static KeyDictionaryFrame singletonInstance = null;

	// KeyDictionary logger
	static Logger logger = Logger.getLogger(KeyDictionaryFrame.class);

	// static path of keyDictionary
	public static final String KEY_DICTIONARY = KeyGenFrame.KEY_DICTIONARY;

	// file representing dictionary
	public static final File KEY_DICTIONARY_FILE = new File(KEY_DICTIONARY);

	private static final int WATCH_INTERVAL_MS = 2000;

	private static final String WELCOME = "Key Dictionary\n\nSelect a key image from the list to see its details.\nRight click on a file for more actions.";

	//INItializer confuguration
	public INItializer currentConf;

	private JFrame thisPanel;
	private JTree tree;
	private JTextArea textArea;
	private JCheckBox chkOverwrite;
	private Timer watcher;
	private long lastSignature;
	private Dimension fitDimension;
	private static final int PREVIEW_SIZE = 220;
	private JLabel previewLabel;
	private volatile File previewFile;
	private JButton btnDelete;
	private JButton btnReload;
	private JButton btnViewInSystem;
	private JButton btnAddKey;
	private JButton btnImport;
	private JButton btnExport;

	// constructor
	public KeyDictionaryFrame(INItializer currentConfiguration) {

		logger.info(this.getClass().getName() + " Starting...");

		isActive.set(true);

		// SW window icon
		GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_KEY_DICT, this);
		GuiUtilsExtended.showOnSameScreen(this, MainGUI.mainFrameScreept);

		thisPanel = this;
		singletonInstance = this;

		// initialize configuration
		currentConf = currentConfiguration;

		buildGui();
		addHandlers();

		DialogFit.apply(this, null, 860, 580);
		fitDimension = getSize();

		new TreeFileMouseListenerDictionary(currentConfiguration, thisPanel, tree, textArea, null);

		lastSignature = dictionarySignature(KEY_DICTIONARY_FILE);
		watcher = new Timer(WATCH_INTERVAL_MS, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				long signature = dictionarySignature(KEY_DICTIONARY_FILE);
				if (signature != lastSignature) {
					reloadTree();
				}
			}
		});
		watcher.start();

		logger.debug("## KEY DICTIONARY started");
	}

	private void buildGui() {

		JPanel content = new JPanel(new MigLayout("wrap 1, fill, insets 14 18 14 18, gapy 10", "[grow,fill]", "[][grow,fill][]"));
		setContentPane(content);

		JPanel top = new JPanel(new MigLayout("insets 0, fillx", "[grow,fill][]", "[]"));
		top.setOpaque(false);
		top.add(ToolStyle.header("Key Image Vault", "Key images (.png) used for the encryption. Select one to see its details, right click for more actions"), "growx");
		top.add(ToolStyle.link("Go to Key Image Generator", new Runnable() {
			
			public void run() {
				MainGUI.mainAPP.windowCreator.newKeyGenFrame();
			}
		}), "aligny top");
		content.add(top, "growx");

		// FILES (left)
		JPanel filesCard = ToolStyle.card(new MigLayout("insets 0, fill, wrap 1", "[grow,fill]", "[][][][grow,fill]"));
		filesCard.add(ToolStyle.caption("Files in the dictionary"));

		JLabel pathLabel = ToolStyle.mutedLabel(KEY_DICTIONARY_FILE.getPath());
		pathLabel.setToolTipText(KEY_DICTIONARY_FILE.getAbsolutePath());
		filesCard.add(pathLabel, "growx, wmin 0");

		JPanel filesButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		filesButtons.setOpaque(false);
		filesButtons.add(btnReload = ToolStyle.button("Refresh", ToolIcons.Kind.REFRESH, "Reload the list of files in the dictionary"));
		filesButtons.add(btnViewInSystem = ToolStyle.button("View in System explorer", ToolIcons.Kind.FOLDER, "Open the dictionary folder"));
		filesCard.add(filesButtons, "growx");

		tree = new JTree(new FileTreeModel(KEY_DICTIONARY_FILE));
		tree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		tree.setVisibleRowCount(10);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				File thisFile = (File) tree.getLastSelectedPathComponent();
				if (thisFile != null && !thisFile.isDirectory()) {
					if ("png".equalsIgnoreCase(FilenameUtils.getExtension(thisFile.getName()))) {
						textArea.setText("Key image: " + thisFile.getName() + "\n\nSize: " + ConversionUtils.coolFileSize(thisFile) + "\n\nPath: " + thisFile.getAbsolutePath());
					} else {
						textArea.setText("Not a key image:\n\n" + thisFile.getName());
					}
					textArea.setCaretPosition(0);
				}
			}
		});
		filesCard.add(new JScrollPane(tree), "grow, push");

		// DETAILS (right)
		textArea = new JTextArea(WELCOME);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setFont(ToolStyle.font(0f, Font.PLAIN));
		textArea.setMargin(new Insets(10, 10, 10, 10));
		textArea.setColumns(24);

		previewLabel = new JLabel("", SwingConstants.CENTER);
		JPanel detailsCard = ToolStyle.card(new MigLayout("insets 0, fill, wrap 1", "[grow,fill]", "[][grow,fill]"));
		detailsCard.add(previewLabel, "growx, h " + PREVIEW_SIZE + "!");
		JScrollPane textScroll = new JScrollPane(textArea);
		textScroll.setPreferredSize(new Dimension(260, 150));
		detailsCard.add(textScroll, "grow");

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filesCard, detailsCard);
		split.setResizeWeight(0.4);
		split.setContinuousLayout(true);
		split.setOpaque(false);
		split.setBorder(null);
		content.add(split, "grow");

		// ACTIONS (bottom)
		btnAddKey = ToolStyle.accent(ToolStyle.button("Add Key Image", ToolIcons.Kind.PASTE, "Copy a key image (png) into the dictionary"), ToolStyle.GREEN);
		btnImport = ToolStyle.button("Import Dictionary", ToolIcons.Kind.FOLDER, "Copy the key images of a folder into the dictionary");
		btnExport = ToolStyle.button("Export Dictionary", ToolIcons.Kind.FOLDER, "Copy the dictionary key images into a folder");

		chkOverwrite = ToolStyle.hand(new JCheckBox("Overwrite same names"));
		chkOverwrite.setToolTipText("Replace the files that have the same name when adding or importing");
		chkOverwrite.setSelected(currentConf.getFileOpt().isFlagOverwrite());

		btnDelete = ToolStyle.accent(ToolStyle.button("Delete", ToolIcons.Kind.CLEAR, "Delete the selected key images from the dictionary"), ToolStyle.RED);
		btnDelete.setEnabled(false);

		JPanel actions = ToolStyle.card(new MigLayout("insets 0, fillx, gap 8 8", "[][][][][grow,fill][]", "[]"));
		actions.add(btnAddKey);
		actions.add(btnImport);
		actions.add(btnExport);
		actions.add(btnDelete);
		actions.add(new JLabel(), "growx");
		actions.add(chkOverwrite, "alignx right");
		content.add(actions, "growx");
	}

	private void addHandlers() {

		// window on close operations
		addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent windowEvent) {

		    	watcher.stop();
		    	isActive.set(false);
		    	singletonInstance = null;
		    	logger.debug("Closing "+getClass().getName());
		    	dispose();
		    }
	    });

		chkOverwrite.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConf.getFileOpt().setFlagOverwrite(chkOverwrite.isSelected());
			}
		});

		btnReload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reloadTree();
			}
		});

		btnViewInSystem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GuiUtilsExtended.openInFileSystem(KEY_DICTIONARY_FILE);
			}
		});

		btnAddKey.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addKeyImage();
			}
		});

		btnImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File selectedImportDir = directoRead(thisPanel);

				if (selectedImportDir != null) {
					try {
						FileWorker.copyDirectoryContents(selectedImportDir, KEY_DICTIONARY_FILE, currentConf.getFileOpt().isFlagOverwrite(), "png");
						dialogHelper.info(
								"Directory :" + selectedImportDir.getName() + " added in the dictionary -> " + KEY_DICTIONARY,
								"Directory added to dictionary :)");
						logger.debug("Directory :" + selectedImportDir.getName() + " added in the dictionary -> " + KEY_DICTIONARY);
					} catch (IOException e1) {
						dialogHelper.error("Directory not imported in " + KEY_DICTIONARY, ". Some error occurred :(");
						logger.error("Directory not imported, some error occurred :( ");
					} finally {
						reloadTree();
					}
				}
			}
		});

		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				java.util.List<File> files = selectedFiles();
				if (!files.isEmpty() && dialogHelper.yesOrNo(I18n.t("common.are_you_sure_to_delete"))) {
					deleteFiles(files);
				}
			}
		});

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				File thisFile = (File) tree.getLastSelectedPathComponent();
				showPreview(thisFile != null && !thisFile.isDirectory() ? thisFile : null);
				btnDelete.setEnabled(!selectedFiles().isEmpty());
			}
		});

		btnExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File selectedExportDir = directoRead(thisPanel);

				if (selectedExportDir != null) {
					// if input fonder not exists then create it
					if (!selectedExportDir.exists()) {
						selectedExportDir.mkdirs();
					}
					try {
						String msg = "Export completed in Target Directory ->" + selectedExportDir.getAbsolutePath();
						FileWorker.copyDirectoryContents(KEY_DICTIONARY_FILE, selectedExportDir, false, "png");
						dialogHelper.info(msg, "Dictionary exported");
						logger.debug(msg);
					} catch (IOException e1) {
						String msg = "Directory not exported" + e1.toString();
						dialogHelper.error(msg, ". Some error occurred :(");
						logger.error(msg);
					}
				}
			}
		});
	}

	/** Files selected in the list (folders excluded) */
	private java.util.List<File> selectedFiles() {
		java.util.List<File> files = new java.util.ArrayList<File>();
		TreePath[] paths = tree.getSelectionPaths();
		if (paths != null) {
			for (TreePath path : paths) {
				Object node = path.getLastPathComponent();
				if (node instanceof File && ((File) node).isFile()) {
					files.add((File) node);
				}
			}
		}
		return files;
	}

	/** Deletes the given files of the dictionary and refreshes the list */
	void deleteFiles(java.util.List<File> files) {
		for (File file : files) {
			if (!file.delete()) {
				logger.error("Key image not deleted: " + file.getAbsolutePath());
			}
		}
		reloadTree();
	}

	/** Thumbnail of the selected key image, read in background so the list stays responsive */
	private void showPreview(final File file) {
		previewFile = file;
		previewLabel.setIcon(null);
		previewLabel.setText("");
		if (file == null || !"png".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
			return;
		}
		new SwingWorker<ImageIcon, Void>() {
			@Override
			protected ImageIcon doInBackground() throws Exception {
				BufferedImage img = ImageIO.read(file);
				return img == null ? null : new ImageIcon(scaleToFit(img, PREVIEW_SIZE));
			}

			@Override
			protected void done() {
				try {
					ImageIcon icon = get();
					if (file.equals(previewFile)) {
						previewLabel.setIcon(icon);
						previewLabel.setText(icon == null ? "No preview" : "");
					}
				} catch (Exception e) {
					logger.debug("Preview not available for " + file.getName(), e);
				}
			}
		}.execute();
	}

	/** Copy of the image that fits a square box, proportions kept, never bigger than the original */
	private static BufferedImage scaleToFit(BufferedImage img, int box) {
		double scale = Math.min(1.0, Math.min(box / (double) img.getWidth(), box / (double) img.getHeight()));
		int w = Math.max(1, (int) Math.round(img.getWidth() * scale));
		int h = Math.max(1, (int) Math.round(img.getHeight() * scale));
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = out.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(img, 0, 0, w, h, null);
		} finally {
			g.dispose();
		}
		return out;
	}

	/** Copies a png chosen by the user into the dictionary, if it is a valid image */
	private void addKeyImage() {
		// cancelled by user: fileRead returns null and shows nothing
		File selected = fileRead(thisPanel);
		if (selected == null) {
			return;
		}
		try {
			BufferedImage tempImg = ImageIO.read(selected);
			if (tempImg != null && BufferedImageType.checkIfAdmittedType(tempImg.getType())) {
				if (FileWorker.copyFile(selected, new File(KEY_DICTIONARY + selected.getName()), currentConf.getFileOpt().isFlagOverwrite()) != null) {
					dialogHelper.info("File correctly saved in " + KEY_DICTIONARY, "File added to dictionary :)");
					logger.debug("File :" + selected.getName() + " added in the dictionary -> " + KEY_DICTIONARY);
				} else {
					erorrLogNmsg();
				}
			} else {
				erorrLogNmsg();
			}
		} catch (IOException e1) {
			logger.error("IO error retrieving image from selected file", e1);
			erorrLogNmsg();
		} finally {
			reloadTree();
		}
	}

	/**
	 * this method select a file from the user (only png files),if nothing is selected returns null
	 */
	private File fileRead(JFrame parent) {

		JFileChooser fileChooser = new NativeFileChooser();
		fileChooser.setDialogTitle("Load .png File");
		fileChooser.setCurrentDirectory(new File(currentConf.getFileOpt().getLastSrcFolderPath()));
		File correctFile = null;

		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

        	File selectedFile = fileChooser.getSelectedFile();
        	String givenExtension = FilenameUtils.getExtension(selectedFile.getName());
        	if(selectedFile==null||!givenExtension.equalsIgnoreCase("png")) {
        		dialogHelper.error(I18n.t("app.please_select_a_valid_png_file"), "Wrong File format");
        	}else {
        		correctFile = selectedFile;
        		currentConf.getFileOpt().setLastSrcFolderPath(correctFile.getAbsolutePath());

        	}
        }else {
        	// cancelled by user: no message needed
        }
        return correctFile;
	}

	/**
	 * this method select a directory from the user (only directories),if nothing is selected returns null
	 */
	private File directoRead(JFrame parent) {

		JFileChooser fileChooser = new NativeFileChooser();
		fileChooser.setDialogTitle("Choose Directory");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File(currentConf.getFileOpt().getLastSrcFolderPath()));
		File correctFile = null;

		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

        	File selectedFile = fileChooser.getSelectedFile();
        	if(selectedFile==null||!selectedFile.isDirectory()) {
        		dialogHelper.error(I18n.t("common.please_select_a_directory"), "Select a directory, please");
        	}else {
        		correctFile = selectedFile;
        		currentConf.getFileOpt().setLastSrcFolderPath(correctFile.getAbsolutePath());
        	}
        }else {
        	// cancelled by user: no message needed
        }
        return correctFile;
	}

	private void erorrLogNmsg() {
		dialogHelper.error( "File not saved in " + KEY_DICTIONARY,
				"Some error occurred :(");
		logger.error("File not saved, some error occurred :( ");
	}

	/** Rebuilds the list keeping the selected files selected */
	private void reloadTree() {
		TreePath[] selection = tree.getSelectionPaths();
		tree.setModel(new FileTreeModel(KEY_DICTIONARY_FILE));
		if (selection != null) {
			java.util.List<TreePath> kept = new java.util.ArrayList<TreePath>();
				for (TreePath path : selection) {
					if (path.getLastPathComponent() instanceof File && ((File) path.getLastPathComponent()).exists()) {
						kept.add(path);
					}
				}
				tree.setSelectionPaths(kept.toArray(new TreePath[0]));
		}
		lastSignature = dictionarySignature(KEY_DICTIONARY_FILE);
	}

	/** Changes with names, sizes and dates of the files inside the folder (recursive) */
	private static long dictionarySignature(File dir) {
		File[] files = dir.listFiles();
		if (files == null) {
			return 0;
		}
		long signature = 1;
		for (File file : files) {
			signature = signature * 31 + file.getName().hashCode();
			signature = signature * 31 + file.lastModified();
			signature = signature * 31 + file.length();
			if (file.isDirectory()) {
				signature = signature * 31 + dictionarySignature(file);
			}
		}
		return signature;
	}

	@Override
	public Dimension getDefaultDimension() {
		return fitDimension != null ? new Dimension(fitDimension) : new Dimension(860, 580);
	}

	public static KeyDictionaryFrame getInstance(INItializer currentConfiguration) {
		return singletonInstance == null ? new KeyDictionaryFrame(currentConfiguration): singletonInstance;
	}
}
