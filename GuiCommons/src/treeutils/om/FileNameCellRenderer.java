package treeutils.om;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeCellRenderer;

import org.apache.log4j.Logger;

import files.om.FileNamed;
import general.MyFileView;
import treeutils.TreeUtils;

public class FileNameCellRenderer implements TreeCellRenderer {
	
	private static final int FOLDER_TEXT_HEIGHT = 15;
	private static final int FILE_TEXT_HEIGHT = 8;

	static Logger logger = Logger.getLogger(FileNameCellRenderer.class); 
		
	public JLabel iconContainer;
	public JPanel container;
	
	public JTree treeRef;
	
	public FileNameCellRenderer() {
		container = new JPanel(new BorderLayout());
		
		treeRef = new JTree();
		
		iconContainer = new JLabel();
		iconContainer.setForeground(Color.LIGHT_GRAY);
		container.add(iconContainer);
    }

    public Component getTreeCellRendererComponent(final JTree tree, Object value, final boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
    	
    	treeRef = tree;
    	
        if (iconContainer != null && container != null && value != null) {
        	
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
					} else {
						iconContainer.setText(absoluteFile.getName());
					}
				} catch (IOException e) {
					logger.error(e);
				}

				if (selectedFile.isFile()) {
					iconContainer.setFont(new Font("Segoe UI", Font.PLAIN, 15));
					iconContainer.setMinimumSize(new Dimension(iconContainer.getWidth(), FILE_TEXT_HEIGHT));
				} else {
					iconContainer.setFont(new Font("Segoe UI", Font.BOLD, 14));
					iconContainer.setMinimumSize(new Dimension(iconContainer.getWidth(), FOLDER_TEXT_HEIGHT));
				}

				container.setToolTipText(selectedFile != null ? selectedFile.getAbsolutePath() : "file is null!");
			}
			iconContainer.setBackground(selected ? TreeUtils.DEF_BACK_SELECTION : tree.getBackground());
			iconContainer.setForeground(selected ? Color.WHITE : Color.GRAY);
			container.setBackground(selected ? TreeUtils.DEF_BACK_SELECTION : tree.getBackground());
			container.setForeground(selected ? Color.WHITE : Color.GRAY);
			//		container.setBounds(tree.getBounds().x, container.getBounds().y, tree.getWidth(), GeneralConfig.DEFAULT_TREE_ROW_HEIGHT);
			//		container.setPreferredSize(new Dimension(tree.getWidth(), GeneralConfig.DEFAULT_TREE_ROW_HEIGHT));
//			container.revalidate();
//			iconContainer.revalidate();
//			container.repaint();
//			iconContainer.repaint();
		}
		return container;
    }
    
    public Object getTreeCellRendererComponent() {
    	return container;
    }
    
    public void setHoverBackgroundEffect(final boolean hover) {
		iconContainer.setBackground(hover ? TreeUtils.DEF_BACK_SELECTION : treeRef.getBackground());
		iconContainer.setForeground(hover ? Color.WHITE : Color.GRAY);
		container.setBackground(hover ? TreeUtils.DEF_BACK_SELECTION : treeRef.getBackground());
		container.setForeground(hover ? Color.WHITE : Color.GRAY);
		
		container.repaint();
		iconContainer.repaint();
    }

}
