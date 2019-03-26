package gUI.utils.tree.dragndrop;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import org.apache.log4j.Logger;

import gUI.utils.tree.dragndrop.om.CrypterDragFileModel;

public class DragNDropVirtualCellRenderer implements TreeCellRenderer {

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(DragNDropVirtualCellRenderer.class); 
		
	private static final String iconFolderPath ="Resources_Scrypt/Icons/Folder.png";
	private static final String iconFilePath ="Resources_Scrypt/Icons/File.png";
	private static final String iconRootPath ="Resources_Scrypt/Icons/Root.png";
	
	private JLabel iconContainer;
	private Font oldFont;
	private Font hoverFont;
	private Font boldFont;
	
	DragNDropVirtualCellRenderer() {
		iconContainer = new JLabel();
		oldFont = iconContainer.getFont();
		hoverFont = new Font(oldFont.getFontName(), oldFont.getStyle(), oldFont.getSize()+2);
		boldFont = new Font(oldFont.getFontName(), Font.BOLD, oldFont.getSize()+2);
		iconContainer.setFont(new Font(oldFont.getFontName(), oldFont.getStyle(), 15));
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        Object node = ((DefaultMutableTreeNode) value).getUserObject();
        
        // custom icon setter
        if (node instanceof CrypterDragFileModel) {
        	try {
				CrypterDragFileModel nodeModeled = (CrypterDragFileModel) node;
				if (nodeModeled.name.equalsIgnoreCase(VirtualFileDropPane.DEFAULT_ROOT)) {
					iconContainer.setIcon(new ImageIcon(iconRootPath));
				} else if (nodeModeled.realFile.isDirectory()) {
					iconContainer.setIcon(new ImageIcon(iconFolderPath));
				} else {
					iconContainer.setIcon(new ImageIcon(iconFilePath));
				}
				
				iconContainer.setText(nodeModeled.toString());
			} catch (Exception e) {
				logger.error("Error setting icons to dragNDrop Panel's jtree", e);
			}
        }
        
        // on focus custom graphics
        if(hasFocus) {
//        	iconContainer.setFont(hoverFont);
        }else {
//        	iconContainer.setFont(oldFont);
        }
        
        // selected custom graphics
        if(selected) {
        	iconContainer.setOpaque(true);
        	iconContainer.setBackground(new Color(81, 81, 81));
        	iconContainer.setForeground(Color.WHITE);
//        	iconContainer.setFont(boldFont);
        }else {
        	iconContainer.setOpaque(false);
        	iconContainer.setForeground(Color.DARK_GRAY);
//        	iconContainer.setFont(oldFont);
//        	tree.updateUI();
        }
        
        return iconContainer;
    }

}
