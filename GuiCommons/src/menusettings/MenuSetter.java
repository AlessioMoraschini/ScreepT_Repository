package menusettings;

import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class MenuSetter {

	/**
	 * set the distance between icon and text for all menuitems and menu in the given menuBar
	 * @param menuBar
	 */
	public static void setIconTextGap(JMenuBar menuBar, int distanceInPx) {
		for(Component currentComponent : menuBar.getComponents()) {
			if(currentComponent instanceof JMenu) {
				((JMenu) currentComponent).setIconTextGap(distanceInPx);
				setIconTextGap((JMenu) currentComponent, distanceInPx);
			}else if(currentComponent instanceof JMenuItem) {
				((JMenuItem) currentComponent).setIconTextGap(distanceInPx);
			}
		}
	}
	
	public static void setIconTextGap(JMenu menuBar, int distanceInPx) {
		JMenu menu;
		JMenuItem menuItem;
		
		for(int i=0; i < menuBar.getItemCount(); i++) {
			Component currentComponent = menuBar.getItem(i);
			if(currentComponent instanceof JMenu) {
				menu = (JMenu) currentComponent;
				setIconTextGap(menu, distanceInPx);
				menu.setIconTextGap(distanceInPx);
			}else if(currentComponent instanceof JMenuItem) {
				menuItem = (JMenuItem) currentComponent;
				menuItem.setIconTextGap(distanceInPx);
			}
		}
	}
	
	public static void setIconTextGap(JPopupMenu menuBar, int distanceInPx) {
		JMenu menu;
		JMenuItem menuItem;
		
		for(int i=0; i < menuBar.getComponentCount(); i++) {
			Component currentComponent = menuBar.getComponent(i);
			if(currentComponent instanceof JMenu) {
				menu = (JMenu) currentComponent;
				setIconTextGap(menu, distanceInPx);
				menu.setIconTextGap(distanceInPx);
			}else if(currentComponent instanceof JMenuItem) {
				menuItem = (JMenuItem) currentComponent;
				menuItem.setIconTextGap(distanceInPx);
			}
		}
	}
	
}
