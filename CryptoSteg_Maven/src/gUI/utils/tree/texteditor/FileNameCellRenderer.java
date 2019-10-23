package gUI.utils.tree.texteditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import org.apache.log4j.Logger;

import amfileutils.om.FileNamed;
import general.MyFileView;
import media.ImageGeneralWorker;

public class FileNameCellRenderer implements TreeCellRenderer {
	
	private static final int FOLDER_TEXT_HEIGHT = 15;
	private static final int FILE_TEXT_HEIGHT = 8;

	static Logger logger = Logger.getLogger(FileNameCellRenderer.class); 
		
	public JLabel iconContainer;
	
	public FileNameCellRenderer() {
		iconContainer = new JLabel();
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        
        if (value instanceof FileNamed) {
			// custom icon setter
			FileNamed nodeFileObj = (FileNamed) value;
			File selectedFile = nodeFileObj.mFile;
			FileNamed nodeFileObjRoot = (FileNamed) tree.getModel().getRoot();
			File root = nodeFileObjRoot.getFile();
			
			MyFileView fileView = new MyFileView();
			iconContainer.setIcon(fileView.getIcon(nodeFileObj.mFile));
			try {
				File absoluteFile = selectedFile.getAbsoluteFile();
				if (root.getAbsolutePath().equals(selectedFile.getAbsolutePath())) {
					iconContainer.setText(selectedFile.getCanonicalPath());
				}else {
					iconContainer.setText(absoluteFile.getName());
				}
			} catch (IOException e) {
				logger.error(e);
			}
			
			if(selectedFile.isFile()) {
				iconContainer.setFont(new Font("Segoe UI", Font.PLAIN, 15));
				iconContainer.setMinimumSize(new Dimension(iconContainer.getWidth(), FILE_TEXT_HEIGHT));
			}else {
				iconContainer.setFont(new Font("Segoe UI", Font.BOLD, 14));
				iconContainer.setMinimumSize(new Dimension(iconContainer.getWidth(), FOLDER_TEXT_HEIGHT));
			}
		}
        
        iconContainer.setForeground(ImageGeneralWorker.getComplimentColor(tree.getBackground()));
		return iconContainer;
    }

}
