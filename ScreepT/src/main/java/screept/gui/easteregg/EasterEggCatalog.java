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
package screept.gui.easteregg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The easter eggs of ScreepT with a short and a long description, shown by the About window when somebody tells
 * ScreepT how they feel. Every entry is launched by name through the home terminal actions
 * (see {@code EasterEggsInitializer}): keep this list, that class and the guide aligned when an egg is added.
 */
public final class EasterEggCatalog {

	/** One easter egg */
	public static final class Entry {
		/** Name of the action registered in the home terminal session */
		public final String action;
		public final String name;
		public final String summary;
		public final String description;

		Entry(String action, String name, String summary, String description) {
			this.action = action;
			this.name = name;
			this.summary = summary;
			this.description = description;
		}

		@Override
		public String toString() {
			return name + "  -  " + summary;
		}
	}

	private static final List<Entry> ENTRIES = new ArrayList<>();

	static {
		add("snake", "Snake", "the classic, with levels",
				"The old snake game with a menu to pick the level. Eat, grow, do not bite yourself, avoid the walls.\n\n"
						+ "Keys: arrows to steer.\n"
						+ "Hidden on the home page: press S three times in a row. In the home terminal: type \"snake\".");
		add("puppynoid", "Puppynoid", "breakout with puppies",
				"A breakout game: bounce the ball with the paddle, break all the bricks, catch the falling bonuses and move on to the next level. "
						+ "Pick the difficulty in the start window.\n\n"
						+ "Keys: left and right arrows move the paddle, Ctrl launches the ball, Space pauses, Esc quits, Enter retries, N goes to the next level.\n"
						+ "Hidden on the home page: press A three times in a row. In the home terminal: type \"puppynoid\".");
		add("cats", "Feo and Nano", "a cat parade over the window",
				"Feo and Nano, in pixel art, cross the main window chasing a ball of yarn and leaving little paw prints behind. "
						+ "Any resemblance to real cats living in Dalmine is intentional.\n\n"
						+ "Hidden on the home page: press M (meow) three times in a row. In the home terminal: type \"cats\".");
		add("poem", "Code poem", "a verse for programmers",
				"A random poem or quote about code, shown in a dialog. Code can be a poem, or even a nightmare.\n\n"
						+ "Hidden in the crypter and in the text editor: press Ctrl+Alt+U four times in a row. In the home terminal: type \"poem\".");
		add("bugs", "Bug Squasher", "squash the bugs in 30 seconds",
				"Bugs crawl over the screen and you have 30 seconds to squash as many as you can. Golden bugs are fast and worth five points, "
						+ "missing costs one. Your best score is remembered.\n\n"
						+ "Controls: just click.\n"
						+ "Hidden on the home page: press B three times in a row. In the home terminal: type \"bugs\".");
		add("cipher", "Cipher Breaker", "decrypt before time runs out",
				"Words shifted with a Caesar cipher appear on screen together with the shift: type the original word back and press Enter. "
						+ "Words get longer and time shorter at every level, three mistakes end the game.\n\n"
						+ "Hidden on the home page: press C three times in a row. In the home terminal: type \"cipher\".");
		add("typing", "Code Typist", "typing speed test",
				"Five lines of programmer life to type exactly as shown. The clock starts at your first key; at the end you get your words per minute and accuracy.\n\n"
						+ "Keys: just type, Backspace fixes mistakes.\n"
						+ "Hidden on the home page: press W (words) three times in a row. In the home terminal: type \"typing\".");
		add("trivia", "Shortcut Trivia", "do you know your shortcuts?",
				"Ten questions about ScreepT keyboard shortcuts, with four answers each. Questions are taken from the guide that ships with the application, "
						+ "so the quiz is always up to date with the real shortcuts.\n\n"
						+ "Hidden on the home page: press T three times in a row. In the home terminal: type \"trivia\".");
		add("fortune", "Fortune cookie", "a ScreepT quote on a CRT console",
				"A quote about ScreepT and the life of people who keep secrets in text files appears typed on a retro console over the main window, then fades away. "
						+ "A click closes it.\n\n"
						+ "Hidden on the home page: press F three times in a row. In the home terminal: type \"fortune\".");
		add("glitch", "Glitch", "the window goes haywire",
				"The main window glitches with shifting slices, color bars and scanlines, then a fake ACCESS GRANTED appears, followed by a reassuring line. "
						+ "It works on a snapshot: nothing is touched, nobody is hacked.\n\n"
						+ "Hidden on the home page: press G three times in a row. In the home terminal: type \"glitch\".");
	}

	private EasterEggCatalog() {
	}

	private static void add(String action, String name, String summary, String description) {
		ENTRIES.add(new Entry(action, name, summary, description));
	}

	public static List<Entry> entries() {
		return Collections.unmodifiableList(ENTRIES);
	}
}
