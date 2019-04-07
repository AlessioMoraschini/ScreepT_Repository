package gUI.utils.tree.iniconfmanager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import org.apache.log4j.Logger;

public class FileCellRendererIni implements TreeCellRenderer {

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(FileCellRendererIni.class); 
		
	private static final String iconFilePath ="Resources_Scrypt/Icons/configFile.png";
	private static final String iconFolderPath = "Resources_Scrypt/Icons/Folder.png";
	
	private JLabel iconContainer;
	private Font oldFont;
	
	public FileCellRendererIni() {
		iconContainer = new JLabel();
		oldFont = iconContainer.getFont();
		iconContainer.setFont(new Font(oldFont.getFontName(), oldFont.getStyle(), 15));
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        File elementFile = ((File)value);
        if(elementFile.getName().toLowerCase().endsWith(".ini")) {
        	iconContainer.setIcon(new ImageIcon(iconFilePath));
        }else if(elementFile.isDirectory()) {
        	iconContainer.setIcon(new ImageIcon(iconFolderPath));
        }
        iconContainer.setText(elementFile.getName());
        
        // selected custom graphics
        if(selected) {
        	iconContainer.setOpaque(false);
        	iconContainer.setForeground(Color.WHITE);
        }else {
        	iconContainer.setOpaque(false);
        	iconContainer.setForeground(Color.DARK_GRAY);
        }
        
        return iconContainer;
    }

}
