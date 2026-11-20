/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.gui.panels.subpanels.texteditor.toolbar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Icon;

import com.formdev.flatlaf.util.UIScale;

/**
 * One coherent set of line icons for the file editor toolbar and its menus: drawn with Java2D on a 24x24 grid
 * (round caps, 1.8 stroke), so they stay sharp at every interface scale and take their color from the component
 * (dark and light themes, hover, selected, disabled). No image files.
 * <p>
 * Every icon is a list of small drawing commands:
 * <ul>
 * <li>{@code p:} stroked path (M L H V Q C Z, absolute) - {@code f:} filled path</li>
 * <li>{@code c:cx,cy,r} stroked circle - {@code F:cx,cy,r} filled circle</li>
 * <li>{@code r:x,y,w,h,radius} stroked rounded rectangle - {@code R:} filled</li>
 * <li>{@code a:cx,cy,r,start,extent} stroked arc (degrees, counterclockwise)</li>
 * <li>{@code t:text,x,y,size} bold text centered on x, baseline y</li>
 * </ul>
 */
public final class EditorIcons implements Icon {

	public enum Kind {
		NEW_FILE("p:M14 3H7.5Q6 3 6 4.5V19.5Q6 21 7.5 21H16.5Q18 21 18 19.5V7Z", "p:M14 3V7H18", "p:M12 11V17", "p:M9 14H15"),
		OPEN("p:M3 7.5Q3 6 4.5 6H9L11 8H19.5Q21 8 21 9.5V17.5Q21 19 19.5 19H4.5Q3 19 3 17.5Z"),
		FOLDER_OPEN("p:M3 18V6.5Q3 5.5 4 5.5H9L11 7.5H18Q19 7.5 19 8.5V10", "p:M3 18L5.4 11.6Q5.8 10.5 7 10.5H20.4Q21.8 10.5 21.4 11.9L19.4 17.4Q19 18.5 18 18.5H3Z"),
		SAVE("p:M5 3H16L21 8V19Q21 21 19 21H5Q3 21 3 19V5Q3 3 5 3Z", "p:M7 3V8H15V3", "r:7,13,10,8,1.5"),
		SAVE_AS("p:M5 3H16L21 8V19Q21 21 19 21H5Q3 21 3 19V5Q3 3 5 3Z", "p:M7 3V8H15V3", "p:M7 13.5H11", "p:M17.5 13V19", "p:M14.5 16H20.5"),
		RELOAD("a:12,12,8,20,290", "f:M19.4 16.2L18.8 20.1L15.5 16.1Z"),
		CLOSE_ALL("p:M8 6V5Q8 4 9 4H19Q20 4 20 5V15Q20 16 19 16H18", "r:4,8,13,13,2", "p:M8.5 12.5L12.5 16.5", "p:M12.5 12.5L8.5 16.5"),
		UNDO("p:M9 14L4 9L9 4", "p:M4 9H15Q20 9 20 14Q20 19 15 19H11"),
		REDO("p:M15 14L20 9L15 4", "p:M20 9H9Q4 9 4 14Q4 19 9 19H13"),
		BACK("p:M15 5L8 12L15 19"),
		FORWARD("p:M9 5L16 12L9 19"),
		SEARCH("c:10.5,10.5,6.5", "p:M15.5 15.5L21 21"),
		REPLACE("p:M4 8H17", "p:M13 4L17 8L13 12", "p:M20 16H7", "p:M11 12L7 16L11 20"),
		GOTO("p:M4 12H15", "p:M11 7L16 12L11 17", "p:M20 5V19"),
		SEARCH_FILES("p:M12 3H6.5Q5 3 5 4.5V19.5Q5 21 6.5 21H10", "p:M12 3L17 8V9", "p:M12 3V8H17", "c:15.5,14.5,3.5", "p:M18 17L21 20"),
		QUICK_OPEN("p:M13 2.5L5.5 13.5H11.5L10.5 21.5L18.5 10H12.5Z"),
		SIDEBAR("r:3,4,18,16,2", "p:M9.5 4V20", "p:M5.5 9H7", "p:M5.5 12H7"),
		MAP("r:3,4,18,16,2", "p:M13 4V20", "p:M15.5 8H18.5", "p:M15.5 11H18.5", "p:M15.5 14H17.5"),
		CLIPBOARD("r:5,5,14,16,2", "r:9,3,6,4,1.5", "p:M9 12H15", "p:M9 16H13"),
		TERMINAL("r:3,4,18,16,2", "p:M7 9.5L10.5 12.5L7 15.5", "p:M12.5 16H17"),
		NOTE("p:M5 3H19Q21 3 21 5V13L13 21H5Q3 21 3 19V5Q3 3 5 3Z", "p:M13 21V15Q13 13 15 13H21", "p:M7 8H17", "p:M7 12H11"),
		SPELL("p:M3 17L7 6L11 17", "p:M4.5 13H9.5", "p:M13.5 14.5L16.8 17.8L22 10.5"),
		INFO("c:12,12,9", "p:M12 11V17", "F:12,7.6,1.1"),
		COPY("r:9,9,12,12,2", "p:M5 15V5Q5 3 7 3H15"),
		FORMAT("p:M4 6H20", "p:M4 10H14", "p:M4 14H20", "p:M4 18H12"),
		TEMPLATE("r:3,4,18,5,1.5", "r:3,12,8,8,1.5", "r:14,12,7,8,1.5"),
		BASE64("t:64,12,16.2,11"),
		TEXT("p:M5 7V4.5H19V7", "p:M12 4.5V19.5", "p:M9 19.5H15"),
		TOOLS("r:3,9,18,11,2", "p:M9 9V7.5Q9 5.5 11 5.5H13Q15 5.5 15 7.5V9", "p:M3 14.5H21", "r:10.2,12.5,3.6,4,0.8"),
		VIEW("p:M2 12Q12 3 22 12Q12 21 2 12Z", "c:12,12,3"),
		CALCULATOR("r:5,3,14,18,2", "r:8,6,8,4,1", "F:9,14,1", "F:12,14,1", "F:15,14,1", "F:9,18,1", "F:12,18,1", "F:15,18,1"),
		PICKER("p:M14.5 6.5L18 3Q19.5 1.8 21 3.2Q22.2 4.7 21 6L17.5 9.5", "p:M13 5L19 11", "p:M15.8 8.2L5 19L3 21L3 18.5L13.2 8.3", "p:M3 21H5.5"),
		OCR("p:M4 8V5Q4 4 5 4H8", "p:M16 4H19Q20 4 20 5V8", "p:M20 16V19Q20 20 19 20H16", "p:M8 20H5Q4 20 4 19V16", "p:M8.5 9.5H15.5", "p:M12 9.5V15.5"),
		PDF("p:M14 3H7.5Q6 3 6 4.5V19.5Q6 21 7.5 21H16.5Q18 21 18 19.5V7Z", "p:M14 3V7H18", "t:PDF,12,17.2,5.6"),
		SPEAK("p:M4 9.5H8L13 5.5V18.5L8 14.5H4Z", "p:M16 9Q18 12 16 15", "p:M18.5 6.5Q22.5 12 18.5 17.5"),
		TAIL("p:M2 12Q12 3 22 12Q12 21 2 12Z", "F:12,12,3.2"),
		LOCK("r:5,11,14,10,2", "p:M8 11V8Q8 4 12 4Q16 4 16 8V11", "F:12,16,1.3"),
		SCREEPT("r:3,3,18,18,5", "t:S,12,17,13"),
		TRASH("p:M4 7H20", "p:M9 7V4H15V7", "p:M6 7L7 20H17L18 7", "p:M10 11V16", "p:M14 11V16"),
		SORT("p:M3.5 6H10", "p:M3.5 11H14", "p:M3.5 16H18", "p:M20.5 5V18", "p:M18 15.5L20.5 18L23 15.5"),
		DEDUPE("p:M4 6H20", "p:M4 12H12", "p:M4 18H12", "p:M15 15L21 21", "p:M21 15L15 21"),
		TRIM("p:M4 7H15", "p:M4 12H15", "p:M4 17H15", "p:M19.5 4V20"),
		CASE("t:Aa,12,16.5,13"),
		ESCAPE("t:\\n,12,16,13"),
		WHITESPACE("p:M4 13V17H20V13", "F:7,8,1.1", "F:12,8,1.1", "F:17,8,1.1"),
		EOL("p:M20 6V12H6", "p:M10 8L6 12L10 16"),
		WRAP("p:M3 6H21", "p:M3 12H17Q20 12 20 15Q20 18 17 18H11", "p:M13.5 15.5L11 18L13.5 20.5", "p:M3 18H7"),
		ZOOM_IN("c:10.5,10.5,6.5", "p:M15.5 15.5L21 21", "p:M7.5 10.5H13.5", "p:M10.5 7.5V13.5"),
		ZOOM_OUT("c:10.5,10.5,6.5", "p:M15.5 15.5L21 21", "p:M7.5 10.5H13.5"),
		ZOOM_RESET("c:10.5,10.5,6.5", "p:M15.5 15.5L21 21", "t:1:1,10.5,13.3,7.4"),
		ERASE("p:M4.5 15.5L13.5 6.5Q14.5 5.5 15.5 6.5L19.5 10.5Q20.5 11.5 19.5 12.5L12 20H7.5L4.5 17Q3.5 16 4.5 15.5Z", "p:M12 20H21"),
		REMOVE_SPACES("p:M4 7H20", "p:M4 12H9", "p:M15 12H20", "p:M4 17H20"),
		EMPTY_LINES("p:M4 5H20", "p:M4 19H20", "p:M8.5 9.5L15.5 14.5", "p:M15.5 9.5L8.5 14.5"),
		PALETTE("p:M12 3Q4 3 4 11Q4 19 11 20Q13 20.2 13 18.5Q13 17 14.5 17H17Q21 17 21 12Q21 3 12 3Z", "F:8,10.5,1.2", "F:12,7.5,1.2", "F:16,10.5,1.2"),
		CHEVRON("p:M7 10L12 15L17 10"),
		TAB_SIZE("p:M3 6H21", "p:M3 12H21", "p:M3 18H21", "p:M7 9L5 12L7 15");

		private final String[] ops;

		Kind(String... ops) {
			this.ops = ops;
		}
	}

	private static final float GRID = 24f;
	private static final float STROKE = 1.8f;

	private final Kind kind;
	private final int size;
	private final Color fixedColor;

	private EditorIcons(Kind kind, int size, Color fixedColor) {
		this.kind = kind;
		this.size = size;
		this.fixedColor = fixedColor;
	}

	/** Icon of the given size (logical pixels, scaled with the interface zoom) drawn with the foreground of its component */
	public static EditorIcons of(Kind kind, int size) {
		return new EditorIcons(kind, size, null);
	}

	public static EditorIcons of(Kind kind, int size, Color color) {
		return new EditorIcons(kind, size, color);
	}

	/** Color forced for this icon (a state shown by color, as the decode mode of the base 64 button), or null */
	public Color getFixedColor() {
		return fixedColor;
	}

	public Kind getKind() {
		return kind;
	}

	@Override
	public int getIconWidth() {
		return UIScale.scale(size);
	}

	@Override
	public int getIconHeight() {
		return UIScale.scale(size);
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Color color = fixedColor;
		if (color == null) {
			color = c != null && c.getForeground() != null ? c.getForeground() : Color.GRAY;
			if (c != null && !c.isEnabled()) {
				color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 90);
			}
		}
		paint(g, x, y, getIconWidth(), color);
	}

	/** Paints the icon in a square of the given pixel size with the given color */
	public void paint(Graphics g0, int x, int y, int pixels, Color color) {
		Graphics2D g = (Graphics2D) g0.create();
		try {
			g.translate(x, y);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			float scale = pixels / GRID;
			g.scale(scale, scale);
			g.setColor(color);
			g.setStroke(new BasicStroke(STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			for (String op : kind.ops) {
				draw(g, op);
			}
		} finally {
			g.dispose();
		}
	}

	private static void draw(Graphics2D g, String op) {
		char type = op.charAt(0);
		String body = op.substring(2);
		switch (type) {
		case 'p':
			g.draw(path(body));
			break;
		case 'f':
			g.fill(path(body));
			break;
		case 'c': {
			double[] v = numbers(body);
			g.draw(new Ellipse2D.Double(v[0] - v[2], v[1] - v[2], v[2] * 2, v[2] * 2));
			break;
		}
		case 'F': {
			double[] v = numbers(body);
			g.fill(new Ellipse2D.Double(v[0] - v[2], v[1] - v[2], v[2] * 2, v[2] * 2));
			break;
		}
		case 'r': {
			double[] v = numbers(body);
			g.draw(new RoundRectangle2D.Double(v[0], v[1], v[2], v[3], v[4] * 2, v[4] * 2));
			break;
		}
		case 'R': {
			double[] v = numbers(body);
			g.fill(new RoundRectangle2D.Double(v[0], v[1], v[2], v[3], v[4] * 2, v[4] * 2));
			break;
		}
		case 'a': {
			double[] v = numbers(body);
			g.draw(new Arc2D.Double(v[0] - v[2], v[1] - v[2], v[2] * 2, v[2] * 2, v[3], v[4], Arc2D.OPEN));
			break;
		}
		case 't': {
			int last = body.lastIndexOf(',');
			int third = body.lastIndexOf(',', last - 1);
			int second = body.lastIndexOf(',', third - 1);
			String text = body.substring(0, second);
			float cx = Float.parseFloat(body.substring(second + 1, third));
			float baseline = Float.parseFloat(body.substring(third + 1, last));
			float fontSize = Float.parseFloat(body.substring(last + 1));
			Font font = new Font(Font.SANS_SERIF, Font.BOLD, 12).deriveFont(fontSize);
			TextLayout layout = new TextLayout(text, font, new FontRenderContext(null, true, true));
			layout.draw(g, cx - (float) layout.getBounds().getWidth() / 2f - (float) layout.getBounds().getX(), baseline);
			break;
		}
		default:
			break;
		}
	}

	private static double[] numbers(String body) {
		String[] parts = body.split(",");
		double[] values = new double[parts.length];
		for (int i = 0; i < parts.length; i++) {
			values[i] = Double.parseDouble(parts[i]);
		}
		return values;
	}

	/** Minimal absolute SVG path parser: M L H V Q C Z */
	private static Path2D path(String d) {
		Path2D.Double path = new Path2D.Double();
		int i = 0;
		int n = d.length();
		double cx = 0;
		double cy = 0;
		char cmd = 'M';
		while (i < n) {
			char ch = d.charAt(i);
			if (Character.isLetter(ch)) {
				cmd = ch;
				i++;
				if (cmd == 'Z') {
					path.closePath();
				}
				continue;
			}
			if (ch == ' ' || ch == ',') {
				i++;
				continue;
			}
			int count = cmd == 'H' || cmd == 'V' ? 1 : cmd == 'Q' ? 4 : cmd == 'C' ? 6 : 2;
			double[] v = new double[count];
			for (int k = 0; k < count; k++) {
				while (i < n && (d.charAt(i) == ' ' || d.charAt(i) == ',')) {
					i++;
				}
				int start = i;
				while (i < n && (Character.isDigit(d.charAt(i)) || d.charAt(i) == '.' || d.charAt(i) == '-')) {
					i++;
				}
				v[k] = Double.parseDouble(d.substring(start, i));
			}
			switch (cmd) {
			case 'M':
				path.moveTo(v[0], v[1]);
				cx = v[0];
				cy = v[1];
				cmd = 'L';
				break;
			case 'L':
				path.lineTo(v[0], v[1]);
				cx = v[0];
				cy = v[1];
				break;
			case 'H':
				path.lineTo(v[0], cy);
				cx = v[0];
				break;
			case 'V':
				path.lineTo(cx, v[0]);
				cy = v[0];
				break;
			case 'Q':
				path.quadTo(v[0], v[1], v[2], v[3]);
				cx = v[2];
				cy = v[3];
				break;
			case 'C':
				path.curveTo(v[0], v[1], v[2], v[3], v[4], v[5]);
				cx = v[4];
				cy = v[5];
				break;
			default:
				break;
			}
		}
		return path;
	}
}
