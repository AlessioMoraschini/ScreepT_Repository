package gUI.utils.tree.texteditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import org.apache.log4j.Logger;

import dialogutils.MyFileView;
import splitpanel.FileNamed;

public class FileNameCellRenderer implements TreeCellRenderer {

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(FileNameCellRenderer.class); 
		
	private JLabel iconContainer;
	private Font oldFont;
	
	public FileNameCellRenderer() {
		iconContainer = new JLabel();
		oldFont = iconContainer.getFont();
		iconContainer.setFont(new Font(oldFont.getFontName(), oldFont.getStyle(), 13));
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        
        if (value instanceof FileNamed) {
			// custom icon setter
			FileNamed nodeFileObj = (FileNamed) value;
			MyFileView fileView = new MyFileView();
			iconContainer.setIcon(fileView.getIcon(nodeFileObj.mFile));
			iconContainer.setText(nodeFileObj.mFile.getName());
		}
        
        iconContainer.setForeground(Color.LIGHT_GRAY);
		return iconContainer;
    }

}
