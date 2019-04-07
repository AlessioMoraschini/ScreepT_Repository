package menusettings;

import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MenuSetter {

	/**
	 * set the distance between icon and text for all menuitems and menu in the given menuBar
	 * @param menuBar
	 */
	public static void setIconTextGap(JMenuBar menuBar, int distanceInPx) {
		for(Component currentComponent : menuBar.getComponents()) {
			if(currentComponent instanceof JMenu) {
				setIconTextGap((JMenu) currentComponent, distanceInPx);
				((JMenu) currentComponent).setIconTextGap(distanceInPx);
			}else if(currentComponent instanceof JMenuItem) {
				((JMenuItem) currentComponent).setIconTextGap(distanceInPx);
			}
		}
	}
	
	public static void setIconTextGap(JMenu menuBar, int distanceInPx) {
		for(int i=0; i < menuBar.getItemCount(); i++) {
			Component currentComponent = menuBar.getItem(i);
			if(currentComponent instanceof JMenu) {
				setIconTextGap((JMenu) currentComponent, distanceInPx);
				((JMenu) currentComponent).setIconTextGap(distanceInPx);
			}else if(currentComponent instanceof JMenuItem) {
				((JMenuItem) currentComponent).setIconTextGap(distanceInPx);
			}
		}
	}
}
