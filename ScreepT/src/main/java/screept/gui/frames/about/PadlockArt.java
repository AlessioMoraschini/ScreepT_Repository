/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
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
