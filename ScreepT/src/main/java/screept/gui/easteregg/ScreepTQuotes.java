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
package screept.gui.easteregg;

import java.util.Random;

/** Quotes and phrases of the easter eggs, all about ScreepT and the life of people who keep secrets in text files */
final class ScreepTQuotes {

	private static final Random RANDOM = new Random();

	static final String[] QUOTES = {
			"A smart crypter... and much more!",
			"Encrypt first, ask questions later.",
			"There is no cloud. There is only AES.",
			"Your secrets are safe with me. I already forgot them.",
			"Feo and Nano approve this build.",
			"Ctrl+Alt+N: because ideas have a very short shelf life.",
			"Real hackers use the dark theme. Wise ones use ScreepT Dark.",
			"Make it work, make it right, make it ScreepT.",
			"Bugs are undocumented features with attitude.",
			"Offline is the new private.",
			"Tabs or spaces? ScreepT has a setting for that, and a grudge.",
			"A password is like a toothbrush: do not share it, change it often.",
			"The best backup is the one you did before you needed it.",
			"One key image to hide them all.",
			"It works on my machine, and my machine is a Windows with two cats.",
			"Never trust a text file you did not encrypt.",
			"Code can be a poem, or even a nightmare.",
			"sudo make me a sandwich. Permission denied, but nice try.",
			"Quick notes today, brilliant ideas tomorrow.",
			"Why did the developer go broke? He used up all his cache."
	};

	/** Words and short phrases for the cipher game, from easy to hard */
	static final String[] CIPHER_EASY = {"SCREEPT", "ENCRYPT", "DECRYPT", "CRYPTER", "EDITOR", "SECRET", "FEO", "NANO", "KEYIMAGE", "MATRIX", "SNAKE", "BUGS"};
	static final String[] CIPHER_HARD = {"QUICK NOTES", "DARK THEME", "ZERO CLOUD", "ALT TAB", "FEO AND NANO", "SMART CRYPTER", "OFFLINE ASSISTANT", "CODE IS POETRY"};

	/** Lines to type in the typing game */
	static final String[] TYPING = {
			"ctrl + alt + n opens the quick notes",
			"aes encrypts, steg hides, screept does both",
			"SELECT secret FROM drawer WHERE cat = 'Feo';",
			"git commit -m \"fixed it (probably)\"",
			"while (alive) { code(); coffee(); }",
			"there is no place like 127.0.0.1",
			"feo and nano approve this build",
			"make it work, make it right, make it screept",
			"rm -rf bugs/ && echo \"done\"",
			"public static void main(String[] hope)"
	};

	private ScreepTQuotes() {
	}

	static String randomQuote() {
		return QUOTES[RANDOM.nextInt(QUOTES.length)];
	}
}
