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
package screept.gui.utils.tooltip;

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
public class KO_ComboBoxLAFRenderer extends JLabel implements ListCellRenderer<Object> {
	private static final long serialVersionUID = 1587070908709053889L;

	public KO_ComboBoxLAFRenderer() {
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
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
