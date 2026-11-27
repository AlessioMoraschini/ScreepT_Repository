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
	

