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

import various.common.light.om.KeyGenImageType;

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
	