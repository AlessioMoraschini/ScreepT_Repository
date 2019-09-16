package general;

import java.awt.Component;
import java.awt.event.KeyListener;

public class GuiGenUtils {

	public static boolean removeKeyListeners(Component target) {
		try {
			for (KeyListener key : target.getKeyListeners()) {
				target.removeKeyListener(key);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
