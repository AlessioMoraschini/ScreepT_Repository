package mouseutils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import frameutils.frame.FilePropertiesFrame;
import frameutils.utils.GuiUtils;
import menusettings.MenuSetter;
import resources.IconsPathConfigurator;

/**
 * This represents a basic extensible component, when each node is a File Object
 * @author xx
 *
 */
public class TreeFileMouseListenerBasic {
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TreeFileMouseListenerBasic.class); 
	
	// gui elements
	public JTree tree;
	public File selectedNode;
	public JFrame propertiesFrame = null; 

	// constructor
	public TreeFileMouseListenerBasic() {
		
	}
	
	public TreeFileMouseListenerBasic(JTree treeIn) {
		tree = treeIn;
		tree.setComponentPopupMenu(getPopUpMenu());
		tree.addMouseListener(getMouseListener());
	}
	
	public MouseListener getMouseListener() {
	    return new MouseAdapter() {

	        @Override
	        public void mousePressed(MouseEvent arg0) {
	            if(arg0.getButton() == MouseEvent.BUTTON3){
	            
		            TreePath pathForLocation = tree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
		            if(pathForLocation != null){
		            	selectedNode = (File) pathForLocation.getLastPathComponent();
		            } else{
		            	selectedNode = null;
		            }
		           
		            super.mousePressed(arg0);
	            }
	        }
	    };
	}

	private JPopupMenu getPopUpMenu() {
	    JPopupMenu menu = new JPopupMenu();
	    
	    JMenuItem sysExplorerItem = new JMenuItem("View in system explorer");
	    sysExplorerItem.addActionListener(openInSystemExplorer());
	    menu.add(sysExplorerItem);
	    
	    JMenuItem deleteItem = new JMenuItem("Delete selected Configuration files");
	    deleteItem.addActionListener(getDeleteActionListener());
	    menu.add(deleteItem);
	    
	    JMenuItem refreshItem = new JMenuItem("Refresh tree");
	    refreshItem.addActionListener(refreshListener());
	    menu.add(refreshItem);
	    
	    JMenuItem propertiesItem = new JMenuItem("File Properties");
	    propertiesItem.addActionListener(propertiesListener());
	    menu.add(propertiesItem);

	    // set menu icons
	    MenuSetter.setIconTextGap(menu, 12);
	    
	    sysExplorerItem.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
	    deleteItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
	    refreshItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
	    propertiesItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
	    
	    return menu;
	}
	
	private ActionListener openInSystemExplorer() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GuiUtils.openInFileSystem(selectedNode);
			}
		};
	}
	

	private ActionListener getDeleteActionListener() {
	    return new ActionListener() {

	        @Override
	        public void actionPerformed(ActionEvent arg0) {

	            TreePath[] treeSelArr = tree.getSelectionPaths();
            	if(treeSelArr.length!= 0 && JOptionPane.showConfirmDialog(null, "Are you sure to delete?")==JOptionPane.OK_OPTION){	        		
		            for(TreePath tp : treeSelArr) {
		            	
	            		String path = tp.getLastPathComponent().toString();
		            	File selectedFile = new File(path);
		            	if(selectedFile.isFile()) {	
		            		logger.debug(path+" Deleted: "+selectedFile.delete());	
		            	}else {
		            		logger.info(path + " Not deleted beacuse it's in usage");
		            	}
			        }

	            }else {
	            	String msg = "Skipped after user request!";
			    	logger.debug(msg);
	            }
            	
            	refreshTree();
	        }
	    };
	}
	
	private ActionListener refreshListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshTree();
			}
		};
	}
	
	public ActionListener propertiesListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (propertiesFrame == null || !propertiesFrame.isVisible()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								if (selectedNode != null && selectedNode.exists()) {
								propertiesFrame = new FilePropertiesFrame(selectedNode, true);
								propertiesFrame.setTitle(selectedNode.getAbsolutePath());
								propertiesFrame.setVisible(true);
								GuiUtils.centerComponent(propertiesFrame, 650, 240);
								}
							} catch (Exception e) {
								logger.error(e);
							}
						}
					});
				}else {
					propertiesFrame.toFront();
					propertiesFrame.setVisible(true);
				}
			}
		};
	}
	
	
	// utility methods //
	
	private void refreshTree() {
		tree.setModel(tree.getModel());
		tree.revalidate();
		tree.repaint();
	}
	
}
