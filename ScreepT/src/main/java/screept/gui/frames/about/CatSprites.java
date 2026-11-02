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
package screept.gui.frames.about;

import java.awt.Color;
import java.awt.Graphics2D;

/** Pixel art busts of Feo and Nano, front view (O outline, F fur, W muzzle, E eye, N nose), with a blink */
final class CatSprites {

	enum Cat {
		FEO(new Color(0xD0, 0x6A, 0x2C), new Color(0xF2, 0xF2, 0xF0)),
		NANO(new Color(0xE0, 0x7B, 0x2C), new Color(0xF2, 0xA7, 0x66));

		final Color fur;
		final Color muzzle;

		Cat(Color fur, Color muzzle) {
			this.fur = fur;
			this.muzzle = muzzle;
		}
	}

	static final int WIDTH = 9;
	static final int HEIGHT = 8;

	private static final String[] OPEN_EYES = {
			"O.......O",
			"OFO...OFO",
			"OFFOOOFFO",
			"OFFFFFFFO",
			"OFEFFFEFO",
			"OFFFNFFFO",
			"OFFWWWFFO",
			".OOOOOOO." };

	private static final String[] BLINK = {
			"O.......O",
			"OFO...OFO",
			"OFFOOOFFO",
			"OFFFFFFFO",
			"OOOFFFOOO",
			"OFFFNFFFO",
			"OFFWWWFFO",
			".OOOOOOO." };

	private static final Color OUTLINE = new Color(0x1A, 0x1A, 0x1E);
	private static final Color EYE = new Color(0xC9, 0xE2, 0x65);
	private static final Color NOSE = new Color(0xF2, 0xA0, 0xB0);

	private CatSprites() {
	}

	/** Draws the bust with its top left corner at (x, y), in pixel-art units of {@code unit} screen pixels */
	static void draw(Graphics2D g, Cat cat, int x, int y, int unit, boolean blink) {
		String[] bitmap = blink ? BLINK : OPEN_EYES;
		for (int row = 0; row < HEIGHT; row++) {
			for (int column = 0; column < WIDTH; column++) {
				Color color = colorOf(bitmap[row].charAt(column), cat);
				if (color != null) {
					g.setColor(color);
					g.fillRect(x + column * unit, y + row * unit, unit, unit);
				}
			}
		}
	}

	private static Color colorOf(char pixel, Cat cat) {
		switch (pixel) {
			case 'O': return OUTLINE;
			case 'F': return cat.fur;
			case 'W': return cat.muzzle;
			case 'E': return EYE;
			case 'N': return NOSE;
			default: return null;
		}
	}
}
