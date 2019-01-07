package gUI.utils.tooltip;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
/**
 * class used to render JComboBox containing objects of type UImanager.LookAndFeelInfo
 * Dismissed cause of better solution with toString directly on comboBox object returned
 * @author A.M. Design&Development
 *
 */
public class KO_ComboBoxLAFRenderer extends JLabel implements ListCellRenderer {

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
