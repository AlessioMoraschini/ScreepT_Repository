package gUI.utils.tooltip;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import enums.BufferedImageType;
import enums.KeyGenImageType;
import enums.ScalingAlgorithm;

public class ToolTipBufferedImageType extends DefaultListCellRenderer {
	private static final long serialVersionUID = 4402489559317164416L;

	@Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        // I'd extract the basic "text" representation of the value
        // and pass that to the super call, which will apply it to the 
        // JLabel via the setText method, otherwise it will use the
        // objects toString method to generate a representation
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); 
        KeyGenImageType comboVal =(KeyGenImageType)value;
        setToolTipText(comboVal.getDescription());
        return this;
    }

}
	