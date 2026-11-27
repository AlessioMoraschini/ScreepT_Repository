/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.gui.utils.tree.iniconfmanager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import org.apache.log4j.Logger;

import resources.IconsPathConfigurator;

public class FileCellRendererIni implements TreeCellRenderer {

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(FileCellRendererIni.class); 
		
	private JLabel iconContainer;
	private Font oldFont;
	
	public FileCellRendererIni() {
		iconContainer = new JLabel();
		oldFont = iconContainer.getFont();
		iconContainer.setFont(gui.commons.frameutils.frame.arch.ToolStyle.font(2f, Font.PLAIN));
    }
    /** Colors of the look and feel for the tree rows (the tree colors when the key is missing) */
    private static Color treeColor(JTree tree, String key, boolean selected) {
        Color color = javax.swing.UIManager.getColor(key);
        return color != null ? color : (selected ? tree.getBackground() : tree.getForeground());
    }


    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        File elementFile = ((File)value);
        if(elementFile.getName().toLowerCase().endsWith(".ini")) {
        	iconContainer.setIcon(new ImageIcon(IconsPathConfigurator.iconConfigFilePath));
        }else if(elementFile.isDirectory()) {
        	iconContainer.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
        }
        iconContainer.setText(elementFile instanceof ConfigPool.CurrentConfigFile ? various.common.light.i18n.I18n.t("cfg.in_use") + ": " + elementFile.getName() : elementFile.getName());
        iconContainer.setToolTipText(elementFile.getAbsolutePath());
        
        // selected custom graphics
        if(selected) {
        	iconContainer.setOpaque(false);
        	iconContainer.setForeground(treeColor(tree, "Tree.selectionForeground", true));
        	iconContainer.setBackground(javax.swing.UIManager.getColor("Tree.selectionBackground"));
        	iconContainer.setOpaque(true);
        }else {
        	iconContainer.setOpaque(false);
        	iconContainer.setForeground(treeColor(tree, "Tree.foreground", false));
        }
        
        iconContainer.revalidate();
        iconContainer.repaint();
        
        return iconContainer;
    }

}
