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
package screept.gui.utils.tree.crypter.dragndrop;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import org.apache.log4j.Logger;

import resources.IconsPathConfigurator;
import screept.gui.utils.tree.crypter.om.CrypterDragFileModel;

public class DragNDropVirtualCellRenderer implements TreeCellRenderer {

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(DragNDropVirtualCellRenderer.class); 
		
	private JLabel iconContainer;
	private Font oldFont;
	
	DragNDropVirtualCellRenderer() {
		iconContainer = new JLabel();
		oldFont = iconContainer.getFont();
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
					iconContainer.setIcon(new ImageIcon(IconsPathConfigurator.iconRootPath));
				} else if (nodeModeled.realFile.isDirectory()) {
					iconContainer.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
				} else {
					iconContainer.setIcon(new ImageIcon(IconsPathConfigurator.iconFilePath));
				}
				
				iconContainer.setText(nodeModeled.toString());
			} catch (Exception e) {
				logger.error("Error setting icons to dragNDrop Panel's jtree", e);
			}
        }
        
        // selected custom graphics
        if(selected) {
        	iconContainer.setOpaque(true);
        	iconContainer.setBackground(new Color(81, 81, 81));
        	iconContainer.setForeground(Color.WHITE);
        }else {
        	iconContainer.setOpaque(false);
        	iconContainer.setForeground(Color.DARK_GRAY);
        }
        
        return iconContainer;
    }

}
