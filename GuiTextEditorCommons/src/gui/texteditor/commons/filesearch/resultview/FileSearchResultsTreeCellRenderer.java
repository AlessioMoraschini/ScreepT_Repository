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
package gui.texteditor.commons.filesearch.resultview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import org.apache.log4j.Logger;

import gui.texteditor.commons.filesearch.resultview.FileSearchResultViewer.ResultViewDTO;
import resources.IconsPathConfigurator;

public class FileSearchResultsTreeCellRenderer implements TreeCellRenderer {
	
	public static Font normalFont = new Font("Segoe UI", Font.PLAIN, 14);
	public static Font boldFont = new Font("Segoe UI", Font.BOLD, 13);
	
	private static final int FOLDER_TEXT_HEIGHT = 15;
	private static final int FILE_TEXT_HEIGHT = 8;

	static Logger logger = Logger.getLogger(FileSearchResultsTreeCellRenderer.class); 
		
	public JLabel iconContainer;
	public JPanel container;
	
	public JTree treeRef;
	
	public FileSearchResultsTreeCellRenderer(JTree treeRef) {
		container = new JPanel(new BorderLayout());
		
		treeRef = treeRef != null ? treeRef : new JTree();
		
		iconContainer = new JLabel();
		iconContainer.setForeground(Color.LIGHT_GRAY);
		container.add(iconContainer);
    }

    public Component getTreeCellRendererComponent(final JTree tree, Object value, final boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
    	
        if (iconContainer != null && container != null && value != null) {

        	if (value instanceof DefaultMutableTreeNode) {

        		DefaultMutableTreeNode valueMutableNode = (DefaultMutableTreeNode)value;
        		ResultViewDTO valueCast = (ResultViewDTO)valueMutableNode.getUserObject();
        		
				// custom icon setter
				ResultViewDTO nodeFileObj = (ResultViewDTO) valueCast;
//				ResultViewDTO nodeFileObjRoot = (ResultViewDTO) tree.getModel().getRoot();

				iconContainer.setText(valueCast.toString());

				if (nodeFileObj.root || nodeFileObj.fileHead) {
					iconContainer.setIcon(nodeFileObj.root 
							? new ImageIcon(IconsPathConfigurator.ICON_SEARCH)
							: new ImageIcon(IconsPathConfigurator.iconFilePath)
					);
					iconContainer.setFont(boldFont);
					iconContainer.setMinimumSize(new Dimension(container.getWidth(), FOLDER_TEXT_HEIGHT));
				} else {
					iconContainer.setIcon(new ImageIcon(IconsPathConfigurator.ICON_NEXT));
					iconContainer.setFont(normalFont);
					iconContainer.setMinimumSize(new Dimension(container.getWidth(), FILE_TEXT_HEIGHT));
					
					container.setToolTipText(nodeFileObj != null 
							? nodeFileObj.toString() + " - line results: " + nodeFileObj.result.getOccurrencesFoundAtLine(nodeFileObj.line) 
							: "element is null!"
					);
				}
			}
			
			iconContainer.setOpaque(false);
			iconContainer.setVisible(true);
			container.setOpaque(false);
			container.setVisible(true);
			
			iconContainer.setForeground(selected ? Color.WHITE : Color.LIGHT_GRAY);
			container.setForeground(selected ? Color.WHITE : Color.LIGHT_GRAY);
			
			iconContainer.revalidate();
			iconContainer.repaint();
			container.revalidate();
			container.repaint();
		}
		return container;
    }
    
    public Object getTreeCellRendererComponent() {
    	return container;
    }
    
}
