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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Knows which words wake up which easter egg. A sentence with a word of the first list of an egg launches that egg
 * (partial unlock, "mini powers"); a word of the second list only makes the sentence "hot"; a word about secrets, games or
 * surprises in general makes it "warm". The master key ("i love screept") opens all of them. Keys are the actions of the
 * easter egg catalog; keywords follow the rules of {@link NpcText#hasKeyword(String, String)}.
 */
final class NpcEggDetector {

	/** The sentence that opens every egg */
	static final String MASTER_KEY = "i love screept";

	private static final String[] GENERAL = { "egg", "eggs", "easter", "secret", "secrets", "hidden", "surprise*", "unlock*", "uovo", "uova",
			"segreto", "segreti", "nascost*", "game", "games", "play*", "fun", "gioc*", "diverti*", "magic", "cheat*", "trick*" };

	private static final class Keys {
		final String[] exact;
		final String[] related;

		Keys(String[] exact, String[] related) {
			this.exact = exact;
			this.related = related;
		}
	}

	private final Map<String, Keys> eggs = new LinkedHashMap<String, Keys>();

	NpcEggDetector() {
		eggs.put("snake", new Keys(a("snake", "snakes", "serpent*", "hiss*"), a("reptile*", "apple*", "eat*", "retro", "nokia", "python", "grow*", "mangia*")));
		eggs.put("puppynoid", new Keys(a("puppynoid", "arkanoid", "breakout", "puppy", "puppies", "cucciol*"), a("dog", "dogs", "cane", "cani", "ball", "palla", "brick*", "mattonc*", "paddle", "arcade")));
		eggs.put("cats", new Keys(a("meow", "miao", "feo", "nano", "parade", "yarn", "gomitolo"), a("cat", "cats", "gatto", "gatti", "kitten", "paw", "zampa", "purr", "fusa")));
		eggs.put("poem", new Keys(a("poem", "poems", "poetry", "poesia", "poesie", "poema", "verse", "verses"), a("rhyme*", "poet*", "literature", "letteratura", "shakespeare", "write*", "scrivi*", "sonnet*")));
		eggs.put("bugs", new Keys(a("squash*", "bugs", "insetti", "swat*"), a("bug", "insect*", "insetto", "spider", "ragno", "fly", "mosca", "beetle")));
		eggs.put("cipher", new Keys(a("cipher*", "caesar", "cesare", "cifrario"), a("spy", "spia", "puzzle", "enigma*", "decode*", "decifr*", "code", "codice", "crypto*")));
		eggs.put("typing", new Keys(a("typing", "typist", "wpm", "typewriter", "dattilografia", "dattilografo"), a("keyboard", "tastiera", "type", "fast", "veloce", "speed", "words", "parole", "accuracy")));
		eggs.put("trivia", new Keys(a("trivia", "quiz"), a("question*", "domand*", "shortcut*", "scorciatoi*", "test", "exam", "esame", "answer*", "rispost*")));
		eggs.put("fortune", new Keys(a("fortune", "cookie", "cookies", "biscott*", "oracle", "oracolo"), a("luck*", "fortuna", "future", "futuro", "prediction*", "predi*", "horoscope", "oroscopo")));
		eggs.put("glitch", new Keys(a("glitch*", "hack", "hacker", "hacking", "breach"), a("error*", "errore", "broken", "rotto", "screen", "schermo", "virus", "matrix", "static")));
	}

	private static String[] a(String... values) {
		return values;
	}

	/** What a sentence does to the eggs */
	static final class Result {
		final NpcHeat heat;
		/** Actions of the eggs the sentence names (all of them for the master key) */
		final Set<String> matched;
		final UnlockLevel unlock;

		Result(NpcHeat heat, Set<String> matched, UnlockLevel unlock) {
			this.heat = heat;
			this.matched = matched;
			this.unlock = unlock;
		}
	}

	/** @param normalized text as returned by {@link NpcText#normalize(String)} */
	Result inspect(String normalized) {
		if (normalized.contains(" " + MASTER_KEY + " ")) {
			return new Result(NpcHeat.MATCH, new HashSet<String>(eggs.keySet()), UnlockLevel.FULL);
		}

		Set<String> named = new HashSet<String>();
		boolean related = false;
		for (Map.Entry<String, Keys> egg : eggs.entrySet()) {
			if (anyOf(normalized, egg.getValue().exact)) {
				named.add(egg.getKey());
			} else if (anyOf(normalized, egg.getValue().related)) {
				related = true;
			}
		}
		if (!named.isEmpty()) {
			return new Result(NpcHeat.MATCH, named, UnlockLevel.PARTIAL);
		}
		NpcHeat heat = related ? NpcHeat.HOT : anyOf(normalized, GENERAL) ? NpcHeat.WARM : NpcHeat.COLD;
		return new Result(heat, new HashSet<String>(), UnlockLevel.NONE);
	}

	/** Actions of every egg known to the detector, in the order of the catalog */
	Set<String> allEggs() {
		return new HashSet<String>(eggs.keySet());
	}

	private static boolean anyOf(String normalized, String[] keywords) {
		for (String keyword : keywords) {
			if (NpcText.hasKeyword(normalized, keyword)) {
				return true;
			}
		}
		return false;
	}
}
