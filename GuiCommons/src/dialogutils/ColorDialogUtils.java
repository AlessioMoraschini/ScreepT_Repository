package dialogutils;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorDialogUtils extends JColorChooser{
	
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
		Color backupColor = (foreground)? targetComponent.getForeground() : targetComponent.getBackground();
		
		thisChooser.getSelectionModel().setSelectedColor(backupColor);
		thisChooser.getSelectionModel().addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if (foreground) {
					targetComponent.setForeground(thisChooser.getColor());
				}else {
					targetComponent.setBackground(thisChooser.getColor());
				}
			}
		});
		
	    JDialog dialog = JColorChooser.createDialog(targetComponent, title, true, thisChooser, new ActionListener() {
			
			@Override // OK
			public void actionPerformed(ActionEvent e) {
				Color color = thisChooser.getColor();
				if (color != null) {
					if (foreground) {
						targetComponent.setForeground(color);
					}else {
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
				}else {
					targetComponent.setBackground(backupColor);
				}
				colorEsit = false;
			}
		});
		dialog.setVisible(true);
		
		if(colorEsit) {
			return thisChooser.getColor();
		}else {
			return null;
		}
	}
}
