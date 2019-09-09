package gUI.utils.tree.texteditor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import frameutils.FilePropertiesFrame;
import frameutils.GuiUtils;
import gUI.panels.TextEditor;
import menusettings.MenuSetter;
import resources.IconsPathConfigurator;
import splitpanel.FileNamed;
import treeutils.TreeUtils;
import utils.ColorDialogUtils;
import utils.RsyntaxUtils;
import variousutils.HandleString;

public class TreeFileMouseListenerTxtEditor {
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TreeFileMouseListenerTxtEditor.class); 
	
	// gui elements
	private JTree tree;
	private FileNamed selectedNode;
	private TextEditor textEditor;
	private JFrame propertiesFrame = null; 
	
	public JMenuItem itemLoadFile;
	public JMenuItem itemNewFile;
	public JMenuItem sysExplorerItem;
	public JMenuItem refreshItem;
	public JMenuItem propertiesItem;
	public JMenuItem changeBckgItem;
	
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
	    
	    itemLoadFile = new JMenuItem("Open File");
	    itemLoadFile.addActionListener(getLoadActionListener());
	    menu.add(itemLoadFile);

	    itemNewFile = new JMenuItem("New File");
	    itemNewFile.addActionListener(getNewFileActionListener());
	    menu.add(itemNewFile);
	    
	    sysExplorerItem = new JMenuItem("View in system explorer");
	    sysExplorerItem.addActionListener(openInSystemExplorer());
	    menu.add(sysExplorerItem);
	    
	    refreshItem = new JMenuItem("Refresh tree");
	    refreshItem.addActionListener(refreshListener());
	    menu.add(refreshItem);
	    
	    propertiesItem = new JMenuItem("File Properties");
	    propertiesItem.addActionListener(propertiesListener());
	    menu.add(propertiesItem);
	    
	    changeBckgItem = new JMenuItem("Change file Panel background");
	    changeBckgItem.addActionListener(ChangeBckgNewFileActionListener());
	    menu.add(changeBckgItem);
	       
	    // set menu icons
	    MenuSetter.setIconTextGap(menu, 12);
	    
	    sysExplorerItem.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
	    itemLoadFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LOAD));
	    refreshItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
	    propertiesItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
	    itemNewFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_LOAD));
	    changeBckgItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_BG_COLOR));

	    return menu;
	}
	
	
	private ActionListener refreshListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (TextEditor.loadedTabObj != null) {
					refreshPathFromAncestor(TextEditor.loadedTabObj.loadedFile);
				}
			}
		};
	}
	
	private boolean refreshPathFromAncestor(File currentFile) {
		File ancestor = null;
		if (currentFile != null) {
			ancestor = currentFile.getParentFile().getParentFile();
			if(!currentFile.exists()) {
				TextEditor.loadedTabObj.unsaved = true;
				TextEditor.updateSavedLabelNTab(null);
			}
		}else {
			return false;
		}
		
		if(ancestor != null) {
			tree.setModel(new FileTreeNamedModel(new FileNamed(ancestor)));
			tree.revalidate();
			tree.repaint();
			TreeUtils.selectFileInNamedFileTree(currentFile, tree);
		}
		return true;
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
								propertiesFrame = new FilePropertiesFrame(selectedNode.mFile, true);
								propertiesFrame.setTitle(selectedNode.mFile.getAbsolutePath());
								propertiesFrame.setVisible(true);
								GuiUtils.centerComponent(propertiesFrame, 650, 240);
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
	public ActionListener ChangeBckgNewFileActionListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color esit = new ColorDialogUtils().askForColor(TextEditor.leftSplitContainer, false);
				if (esit != null) {
					textEditor.currentConfiguration.getTextEditorOpt().setFilePanelBackCol(esit);
					TextEditor.leftSplitContainer.setBackground(esit);
					logger.debug("File panel Background color changed to : " + HandleString.getRgbString(esit));
				}
			}
		};
	}
	
	private ActionListener getNewFileActionListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				TextEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
				File selected = new File(""); 
				if(selectedNode != null && selectedNode.mFile != null && !TextEditor.alreadyLoading) {
					selected = selectedNode.mFile;
				}else if(textEditor.loadedTabObj.loadedFile != null) {
					selected = textEditor.loadedTabObj.loadedFile;
				}else {
					TextEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
					return;
				}
				
				selected = FileStuff.renameJavaObjFile(selected);
				String newName = JOptionPane.showInputDialog("Plese specify a name for the file to save in "+selected.getParent(), selected.getName());
				if(newName == null) {
					TextEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
					return;
				}
				newName = (!newName.equals(""))? newName : "NewFile.txt";
				selected = new File(selected.getParentFile().getAbsolutePath() + "/" + newName);
				selected = FileStuff.renameJavaObjFile(selected);
				
				try {
					// create, load, and refresh tree
					if (!selected.createNewFile()) {
						String msg = "File not created";
						logger.warn(msg);
						JOptionPane.showMessageDialog(null, msg, msg, JOptionPane.WARNING_MESSAGE);
						TextEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
						return;
					}
					textEditor.loadFile(selected);
					if (TextEditor.loadedTabObj != null) {
						refreshPathFromAncestor(TextEditor.loadedTabObj.loadedFile);
					}
				} catch (IOException e1) {
					String msg = "Error creating new file";
					logger.error(msg, e1);
					JOptionPane.showMessageDialog(null, msg, msg, JOptionPane.ERROR_MESSAGE);
				}
				
				TextEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
						
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

	public JMenuItem getItemLoadFile() {
		return itemLoadFile;
	}

	public void setItemLoadFile(JMenuItem itemLoadFile) {
		this.itemLoadFile = itemLoadFile;
	}

	public JMenuItem getItemNewFile() {
		return itemNewFile;
	}

	public void setItemNewFile(JMenuItem itemNewFile) {
		this.itemNewFile = itemNewFile;
	}

	public JMenuItem getSysExplorerItem() {
		return sysExplorerItem;
	}

	public void setSysExplorerItem(JMenuItem sysExplorerItem) {
		this.sysExplorerItem = sysExplorerItem;
	}

	public JMenuItem getRefreshItem() {
		return refreshItem;
	}

	public void setRefreshItem(JMenuItem refreshItem) {
		this.refreshItem = refreshItem;
	}

	public JMenuItem getPropertiesItem() {
		return propertiesItem;
	}

	public void setPropertiesItem(JMenuItem propertiesItem) {
		this.propertiesItem = propertiesItem;
	}
	
	// GETTERS AND SETTERS FOR MENU CLICKABLE
	
	
}
