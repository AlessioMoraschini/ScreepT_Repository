package guistyle;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.Map;

public class TextGuiUtils {

	public static void underlineText(Component sourceElem) {
		Map attributes = sourceElem.getFont().getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		sourceElem.setFont(sourceElem.getFont().deriveFont(attributes));
	}
	
	public static void hoverForeground(Component sourceElem, Color hoverColor) {
		Color backupCol = sourceElem.getForeground();
		sourceElem.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				sourceElem.setForeground(backupCol);
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				sourceElem.setForeground(backupCol);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				sourceElem.setForeground(hoverColor);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
	}
}
