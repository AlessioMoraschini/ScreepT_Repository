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

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import various.common.light.om.ScalingAlgorithm;

public class ToolTipScalingAlgorithm extends DefaultListCellRenderer {
	private static final long serialVersionUID = -8632379055811951011L;

	@Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); 
        ScalingAlgorithm comboVal =(ScalingAlgorithm)value;
        setToolTipText(comboVal.getDescription());
        return this;
    }

}
	

