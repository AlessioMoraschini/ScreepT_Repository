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

/**
 * What the visitor is talking about. The first theme (in declaration order) that has one of its keywords in the
 * text wins, so the specific ones come before the generic ones. A keyword ending with * matches any word that starts
 * with it. English and Italian.
 */
enum NpcTheme {
	FUCK("fuck*", "vaffanculo"),
	UNLOCK("i love screept"),
	INSULT("stupid", "idiot", "dumb", "useless", "sucks", "stronzo", "stupido", "scemo", "cretino", "merda", "shit", "crap", "bastard", "asshole", "cazzo", "moron"),
	THANKS("thanks", "thank you", "grazie", "thx", "cheers"),
	FAREWELL("bye", "goodbye", "addio", "arrivederci", "see you", "good night", "buonanotte", "quit", "exit"),
	WHO("who are you", "who r you", "chi sei", "what are you", "cosa sei", "what is screept", "che cos e screept", "about you", "about yourself", "about screept", "introduce yourself"),
	FEATURES("features", "what can you do", "cosa sai fare", "funzioni", "capabilities", "help", "aiuto", "commands", "what does screept do"),
	SECRET("password", "encrypt*", "decrypt*", "crypter", "crypt", "cifr*", "segreto", "secret", "secrets", "key image", "chiave", "security", "sicurezza", "aes"),
	BIG("big file", "huge", "large file", "logsearch", "log", "logs", "gigabyte", "gb", "grande", "enorme", "file grandi", "memory", "memoria"),
	TERMINAL("terminal*", "shell", "console", "bash", "command line", "powershell"),
	EGG("egg", "eggs", "easter", "uovo", "uova", "hidden", "nascost*"),
	CATS("cat", "cats", "gatto", "gatti", "feo", "nano", "meow", "miao", "kitten"),
	SHORTCUT("shortcut*", "hotkey*", "keyboard", "scorciatoia", "scorciatoie", "tasti", "how do i", "how to", "come si", "come faccio"),
	VERSION("version", "versione", "build", "update*", "aggiorn*", "release", "changelog"),
	TIME("time", "date", "today", "what day", "che ora", "che giorno", "oggi", "clock"),
	JOKE("joke", "funny", "battuta", "barzelletta", "make me laugh", "ridere"),
	QUOTE("quote", "quotes", "citazione", "citazioni", "movie", "movies", "film", "cinema"),
	MEANING("meaning of life", "senso della vita", "42", "universe", "life", "vita"),
	BUG("bug", "error", "crash", "broken", "errore", "non funziona", "doesnt work", "not working", "problem", "problema"),
	MUSIC("guitar", "music", "musica", "chitarra", "song", "band", "techno"),
	CODE("java", "code", "programming", "developer", "coding", "programmare", "swing"),
	LOVE("love", "like", "awesome", "great", "cool", "bravo", "amazing", "fantastic", "bello", "fantastico", "ti amo", "best"),
	GREETING("hi", "hello", "hey", "yo", "ciao", "salve", "buongiorno", "buonasera", "hola", "sup", "wassup"),
	DEFAULT();

	private final String[] keywords;

	NpcTheme(String... keywords) {
		this.keywords = keywords;
	}

	/** @param normalized text as returned by {@link NpcText#normalize(String)} */
	static NpcTheme detect(String normalized) {
		for (NpcTheme theme : values()) {
			if (theme.matches(normalized)) {
				return theme;
			}
		}
		return DEFAULT;
	}

	private boolean matches(String normalized) {
		for (String keyword : keywords) {
			if (NpcText.hasKeyword(normalized, keyword)) {
				return true;
			}
		}
		return false;
	}
}
