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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tips about how to discover the easter eggs, dropped every two or three answers: the generic ones and one riddle per egg.
 * Tips come in random order and none is repeated until all of them have been told.
 */
final class NpcHints {

	private static final List<String> ALL = Arrays.asList(
			"Psst: some words are keys. Try naming a game, an animal, a kind of poem.",
			"Psst: on the home page, a letter typed three times in a row can do things.",
			"Psst: tell me what you would like to see and I might arrange it. Just name it.",
			"Psst: say how you feel about this software. Honestly. All of it. The full vault likes declarations.",
			"Psst: ask me about the eggs and I will tell you if you are warm or cold.",
			"Psst: the home terminal obeys the names of the eggs too.",
			"Psst: the crypter and the text editor have a secret of their own, four taps on Ctrl+Alt and a letter.",
			"Psst: something here hisses and eats apples.",
			"Psst: there are puppies, a ball and a wall of bricks somewhere.",
			"Psst: two cats, a ball of yarn and an unreasonable amount of paw prints.",
			"Psst: there is a verse for programmers, if you ask the right way.",
			"Psst: a swarm of bugs, a deadline and a very fast golden one.",
			"Psst: a spy's cipher is waiting for somebody patient.",
			"Psst: ten fingers, a clock and five lines of programmer life.",
			"Psst: a quiz about your own keyboard shortcuts. Do you know them?",
			"Psst: there is a cookie with a message inside. You have to crack it, in a manner of speaking.",
			"Psst: a window that glitches for show. Nobody gets hacked, promise.");

	private final List<String> queue = new ArrayList<String>();
	private int untilNext = nextGap();

	/** Called once per answer: a tip when it is time (every two or three answers), else null */
	synchronized String nextIfDue() {
		untilNext--;
		if (untilNext > 0) {
			return null;
		}
		untilNext = nextGap();
		if (queue.isEmpty()) {
			queue.addAll(ALL);
			Collections.shuffle(queue);
		}
		return queue.remove(queue.size() - 1);
	}

	private static int nextGap() {
		return 2 + ThreadLocalRandom.current().nextInt(2);
	}
}
