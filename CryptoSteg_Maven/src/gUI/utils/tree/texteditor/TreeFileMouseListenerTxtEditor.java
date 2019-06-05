package gUI.utils.tree.texteditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import frameutils.FilePropertiesFrame;
import frameutils.GuiUtils;
import gUI.panels.TextEditor;
import menusettings.MenuSetter;
import resources.IconsPathConfigurator;
import splitpanel.FileNamed;
import treeutils.TreeUtils;

public class TreeFileMouseListenerTxtEditor {
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TreeFileMouseListenerTxtEditor.class); 
	
	// gui elements
	private JTree tree;
	private FileNamed selectedNode;
	private TextEditor textEditor;
	private JFrame propertiesFrame = null; 
	
	// constructor
	public TreeFileMouseListenerTxtEditor(JTree tree, TextEditor textEdit){
		this.tree = tree;
		this.tree.setComponentPopupMenu(getPopUpMenu());
		this.tree.addMouseListener(getMouseListener());
		this.tree.addMouseListener(getDoubleCLickListener());
		textEditor = textEdit;
	}
	
	public MouseListener getMouseListener() {
	    return new MouseAdapter() {

	        @Override
	        public void mousePressed(MouseEvent arg0) {
	            if(arg0.getButton() == MouseEvent.BUTTON3){
	                TreePath pathForLocation = tree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
	                if(pathForLocation != null){
	                    selectedNode = (FileNamed) pathForLocation.getLastPathComponent();
	                } else{
	                    selectedNode = null;
	                }

	            }
	            super.mousePressed(arg0);
	        }
	    };
	}
	
	private MouseAdapter getDoubleCLickListener() {
		return new MouseAdapter() {
		    public void mousePressed(MouseEvent e) {
		        int selRow = tree.getRowForLocation(e.getX(), e.getY());
		        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		        if(selRow != -1 && e.getClickCount() == 2) {
		        	if(selPath != null){
	                    selectedNode = (FileNamed) selPath.getLastPathComponent();
	                    if (selectedNode.mFile != null && selectedNode.mFile.exists()) {
	                    	//	                myDoubleClick(selRow, selPath);
	                    	textEditor.loadFile(selectedNode.mFile);
	                    }
	                } else{
	                    selectedNode = null;
	                }
		        }
		    }
		};
	}

	private JPopupMenu getPopUpMenu() {
	    JPopupMenu menu = new JPopupMenu();
	    
	    JMenuItem itemLoadFile = new JMenuItem("Open File");
	    itemLoadFile.addActionListener(getLoadActionListener());
	    menu.add(itemLoadFile);
	    
	    JMenuItem sysExplorerItem = new JMenuItem("View in system explorer");
	    sysExplorerItem.addActionListener(openInSystemExplorer());
	    menu.add(sysExplorerItem);
	    
	    JMenuItem refreshItem = new JMenuItem("Refresh tree");
	    refreshItem.addActionListener(refreshListener());
	    menu.add(refreshItem);
	    
	    JMenuItem propertiesItem = new JMenuItem("File Properties");
	    propertiesItem.addActionListener(propertiesListener());
	    menu.add(propertiesItem);
	       
	    // set menu icons
	    MenuSetter.setIconTextGap(menu, 12);
	    
	    sysExplorerItem.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
	    itemLoadFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LOAD));
	    refreshItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
	    propertiesItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));

	    return menu;
	}
	
	private ActionListener refreshListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (TextEditor.loadedTabObj != null && TextEditor.loadedTabObj.loadedFile != null) {
					tree.setModel(new FileTreeNamedModel(new FileNamed(TextEditor.loadedTabObj.loadedFile.getParentFile().getParentFile())));
					tree.revalidate();
					tree.repaint();
					TreeUtils.selectFileInNamedFileTree(TextEditor.loadedTabObj.loadedFile, tree);
				}
			}
		};
	}
	
	private ActionListener propertiesListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (propertiesFrame == null || !propertiesFrame.isVisible()) {
					TextEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								if (selectedNode != null && selectedNode.mFile != null) {
								propertiesFrame = new FilePropertiesFrame(selectedNode.mFile);
								propertiesFrame.setTitle(selectedNode.mFile.getAbsolutePath());
								propertiesFrame.setVisible(true);
								GuiUtils.centerComponent(propertiesFrame, 600, 200);
								}
							} catch (Exception e) {
								logger.error(e);
							}
							TextEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
						}
					});
				}else {
					propertiesFrame.toFront();
					propertiesFrame.setVisible(true);
				}
			}
		};
	}

	private ActionListener getLoadActionListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				TextEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							if (selectedNode != null && selectedNode.mFile != null && !TextEditor.alreadyLoading) {
								textEditor.loadFile(selectedNode.mFile);
								TextEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
							}
						} catch (Exception e) {
							logger.error(e);
						}
					}
				});
			}
		};
	}
	
	private ActionListener openInSystemExplorer() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GuiUtils.openInFileSystem(selectedNode.mFile);
			}
		};
	}
	
}
