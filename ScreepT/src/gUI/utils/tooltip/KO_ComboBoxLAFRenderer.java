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
package gUI.utils.tooltip;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
/**
 * class used to render JComboBox containing objects of type UImanager.LookAndFeelInfo
 * Dismissed cause of better solution with toString directly on comboBox object returned
 * @author A.M. Design&Development
 *
 */
public class KO_ComboBoxLAFRenderer extends JLabel implements ListCellRenderer {
	private static final long serialVersionUID = 1587070908709053889L;

	public KO_ComboBoxLAFRenderer() {
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		//Get the selected index. (The index param isn't
        //always valid, so just use the value.)
        UIManager.LookAndFeelInfo selectedLAF = (UIManager.LookAndFeelInfo)value;

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setText(selectedLAF.getName());
		return null;
	}
}
