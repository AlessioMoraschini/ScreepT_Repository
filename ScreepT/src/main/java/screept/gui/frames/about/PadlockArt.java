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

/** Pixel art of the padlock (S = shackle, X = outline, O = body, # = keyhole): closed, ajar (mini powers) and open (super powers) */
public final class PadlockArt {

	public static final String[] CLOSED = {
			"...SSSSS...",
			"..S.....S..",
			".S.......S.",
			".S.......S.",
			".S.......S.",
			"XXXXXXXXXXX",
			"XOOOOOOOOOX",
			"XOOOO#OOOOX",
			"XOOO###OOOX",
			"XOOOO#OOOOX",
			"XOOOOOOOOOX",
			"XXXXXXXXXXX" };

	/** The shackle has just left its seat on one side */
	public static final String[] AJAR = {
			"...SSSSS...",
			"..S.....S..",
			".S.......S.",
			".S.......S.",
			".S.........",
			"XXXXXXXXXXX",
			"XOOOOOOOOOX",
			"XOOOO#OOOOX",
			"XOOO###OOOX",
			"XOOOO#OOOOX",
			"XOOOOOOOOOX",
			"XXXXXXXXXXX" };

	/** The shackle is free on the right */
	public static final String[] OPEN = {
			"..SSSSS....",
			".S.....S...",
			"S.......S..",
			"S..........",
			"S..........",
			"XXXXXXXXXXX",
			"XOOOOOOOOOX",
			"XOOOO#OOOOX",
			"XOOO###OOOX",
			"XOOOO#OOOOX",
			"XOOOOOOOOOX",
			"XXXXXXXXXXX" };

	private PadlockArt() {
	}
}
