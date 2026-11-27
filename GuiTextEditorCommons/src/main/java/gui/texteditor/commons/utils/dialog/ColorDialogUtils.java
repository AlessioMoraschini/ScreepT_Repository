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
package gui.texteditor.commons.utils.dialog;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import gui.texteditor.commons.utils.RsyntaxUtils;

public class ColorDialogUtils extends JColorChooser{
	private static final long serialVersionUID = 7495639155315531462L;
	
	private boolean colorEsit;
	private JColorChooser thisChooser;
	
	/**
	 * Create a colorChooser dialog updating in real time the color as the selected one.
	 * If OK color is maintained, else the original is restored
	 * @param targetComponent the component to change the color of
	 * @param foreground true if wanna change foreground, false if the background is to change
	 * @return the new color if user pressed ok, null in the other case
	 */
	public Color askForColor(JComponent targetComponent, boolean foreground) {
		
		thisChooser = this;
		colorEsit = false;
		String title = (foreground)? "Choose Text color" : "Choose Background color";
		Color selectedLineBackup;
		Color backupColor;
		if (targetComponent instanceof RSyntaxTextArea) {
			selectedLineBackup = ((RSyntaxTextArea)targetComponent).getCurrentLineHighlightColor();
			backupColor = (foreground)? targetComponent.getForeground() : targetComponent.getBackground();
		}else {
			selectedLineBackup = targetComponent.getBackground();
			backupColor = (foreground)? targetComponent.getForeground() : targetComponent.getBackground();
		}
		
		thisChooser.getSelectionModel().setSelectedColor(backupColor);
		thisChooser.getSelectionModel().addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if (foreground) {
					targetComponent.setForeground(thisChooser.getColor());
					if (targetComponent instanceof RSyntaxTextArea) {
						RsyntaxUtils.setForeground((RSyntaxTextArea)targetComponent, thisChooser.getColor());
					}
				}else {
					targetComponent.setBackground(thisChooser.getColor());
					if (targetComponent instanceof RSyntaxTextArea) {
						RsyntaxUtils.setBackground((RSyntaxTextArea)targetComponent, thisChooser.getColor());
					}
				}
			}
		});
		
	    JDialog dialog = JColorChooser.createDialog(targetComponent, title, true, thisChooser, new ActionListener() {
			
			@Override // OK
			public void actionPerformed(ActionEvent e) {
				Color color = thisChooser.getColor();
				if (color != null) {
					if (foreground) {
						if (targetComponent instanceof RSyntaxTextArea) {
							RsyntaxUtils.setForeground((RSyntaxTextArea)targetComponent, color);
						}
						targetComponent.setForeground(color);
					}else {
						if (targetComponent instanceof RSyntaxTextArea) {
							RsyntaxUtils.setBackground((RSyntaxTextArea)targetComponent, color);
						}
						targetComponent.setBackground(color);
					}
					colorEsit = true;
		      }			
			}
		}, new ActionListener() {
			@Override // NO
			public void actionPerformed(ActionEvent e) {
				if (foreground) {
					targetComponent.setForeground(backupColor);
					if (targetComponent instanceof RSyntaxTextArea) {
						RsyntaxUtils.setForeground((RSyntaxTextArea)targetComponent, backupColor);
					}
				}else {
					targetComponent.setBackground(backupColor);
					if (targetComponent instanceof RSyntaxTextArea) {
						RsyntaxUtils.setBackground((RSyntaxTextArea)targetComponent, backupColor);
					}
				}
				colorEsit = false;
			}
		});
		dialog.setVisible(true);
		
		if(colorEsit) {
			return thisChooser.getColor();
		}else {
			if (targetComponent instanceof RSyntaxTextArea) {
				((RSyntaxTextArea)targetComponent).setCurrentLineHighlightColor(selectedLineBackup);
			}
			return null;
		}
	}
}
