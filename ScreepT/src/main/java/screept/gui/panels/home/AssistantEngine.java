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
package screept.gui.panels.home;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import maingUI.MainGUI;
import resources.GeneralConfig;
import screept.gui.panels.home.HomeTerminalSession.Kind;

/**
 * Brain of the home terminal: built-in commands and, for everything else, a search in the documentation
 * ({@link AssistantKnowledge}). Replies are plain text with a bit of character; nothing is generated and nothing
 * leaves the machine. Runs on the assistant thread: it must not touch Swing, UI work goes in {@link Reply#uiAction}.
 */
final class AssistantEngine {

	private static final Random RANDOM = new Random();

	private static final String[] FOUND_INTROS = {
			"Found it.", "I know this one.", "Easy.", "Pulling it from the manual...", "Here you go.", "Glad you asked."
	};
	private static final String[] NOT_FOUND = {
			"My circuits found nothing about that. Try other words, or type \"topics\".",
			"No match. The manual and I are both a bit embarrassed.",
			"That is not in my docs (yet). Rephrase it, or try \"topics\".",
			"404: answer not found. Try one keyword, like \"crypter\" or \"theme\"."
	};
	private static final String[] GREETINGS = {
			"Hello, human. Ask me anything about ScreepT.", "Greetings. Keys are ready when you are.",
			"Hi! I only know ScreepT, but I know it well."
	};

	/** What the terminal shows for a question */
	static final class Reply {
		static final class Entry {
			final Kind kind;
			final String text;

			Entry(Kind kind, String text) {
				this.kind = kind;
				this.text = text;
			}
		}

		final List<Entry> entries = new ArrayList<>();
		boolean clear;
		/** Work to do on the EDT after the lines are shown */
		Runnable uiAction;

		Reply add(Kind kind, String text) {
			entries.add(new Entry(kind, text));
			return this;
		}
	}

	private AssistantEngine() {
	}

	static Reply reply(String question, HomeTerminalSession session) {

		String normalized = AssistantKnowledge.normalize(question).trim().replaceAll("[?!.]+$", "").trim();
		Reply reply = new Reply();
		String[] words = normalized.split("\\s+");
		String first = words[0];
		String rest = normalized.length() > first.length() ? normalized.substring(first.length()).trim() : "";

		if (isAny(normalized, "help", "?", "aiuto", "comandi", "commands", "man")) {
			return help(reply);
		}
		if (isAny(first, "clear", "cls", "pulisci")) {
			reply.clear = true;
			return reply;
		}
		if (isAny(normalized, "news", "novita", "what's new", "whats new", "what is new", "changelog", "ultime modifiche",
				"cosa c'e di nuovo", "cosa e cambiato", "ultimi aggiornamenti")) {
			return news(reply);
		}
		if (isAny(first, "shortcuts", "shortcut", "scorciatoie", "scorciatoia", "hotkeys", "tasti")) {
			return shortcuts(reply, rest);
		}
		if (isAny(normalized, "version", "versione", "ver")) {
			return reply.add(Kind.OUTPUT, GeneralConfig.APPLICATION_NAME);
		}
		if (isAny(normalized, "about", "who are you", "chi sei", "cosa sei", "what are you")) {
			return reply.add(Kind.TITLE, "ScreepT assistant")
					.add(Kind.OUTPUT, "A tiny offline helper living in the home page. I read the guide and the changelog that ship with ScreepT, "
							+ "so my answers are only as good as the docs. No cloud, no keys, no costs.")
					.add(Kind.OUTPUT, "Made by A.M. Design and Development, just for the love of tools for techies.");
		}
		if (isAny(normalized, "whoami", "chi sono")) {
			return reply.add(Kind.OUTPUT, userName() + " - a person with excellent taste in software.");
		}
		if (isAny(first, "date", "time", "data", "ora")) {
			return reply.add(Kind.OUTPUT, new SimpleDateFormat("EEEE dd MMMM yyyy, HH:mm:ss", Locale.getDefault()).format(new Date()));
		}
		if (isAny(normalized, "topics", "ls", "dir", "argomenti", "indice", "index", "sections")) {
			return topics(reply);
		}
		if (isAny(normalized, "eggs", "easter", "easter eggs", "easter egg", "uova", "segreti", "secrets")) {
			return eggs(reply);
		}
		if (isAny(normalized, "snake", "serpente")) {
			return launch(reply, session, "snake", "Hissing in...");
		}
		if (isAny(normalized, "puppynoid", "arkanoid", "breakout")) {
			return launch(reply, session, "puppynoid", "Puppies incoming...");
		}
		if (isAny(normalized, "cats", "cat", "gatti", "feo", "nano", "meow", "miao")) {
			return launch(reply, session, "cats", "Feo and Nano are on their way.");
		}
		if (isAny(normalized, "poem", "poetry", "poesia", "poema")) {
			return launch(reply, session, "poem", "Clearing my throat...");
		}
		if (isAny(normalized, "matrix", "replay", "pioggia", "rain")) {
			return launch(reply, session, "matrix", "Follow the white rabbit.");
		}
		if (isAny(normalized, "bugs", "bug", "squash", "bug squasher", "insetti")) {
			return launch(reply, session, "bugs", "Releasing the bugs. Bring a shoe.");
		}
		if (isAny(normalized, "cipher", "caesar", "breaker", "cifrario", "cipher breaker")) {
			return launch(reply, session, "cipher", "Loading something suspiciously encrypted...");
		}
		if (isAny(normalized, "typing", "typist", "wpm", "type", "dattilografia")) {
			return launch(reply, session, "typing", "Stretch your fingers.");
		}
		if (isAny(normalized, "trivia", "quiz", "shortcut trivia")) {
			return launch(reply, session, "trivia", "Let us see if you read the manual.");
		}
		if (isAny(normalized, "fortune", "cookie", "quote", "citazione", "biscotto")) {
			return launch(reply, session, "fortune", "Cracking a cookie open...");
		}
		if (isAny(normalized, "glitch", "hack", "hacker", "breach")) {
			return launch(reply, session, "glitch", "Do not panic.");
		}
		if (first.equals("sudo")) {
			return reply.add(Kind.ERROR, userName() + " is not in the sudoers file. This incident will be reported (to nobody).");
		}
		if (normalized.startsWith("rm ")) {
			return reply.add(Kind.ERROR, "Nice try. I only read things, I never delete them.");
		}
		if (isAny(normalized, "exit", "quit", "logout", "esci", "bye")) {
			return reply.add(Kind.OUTPUT, "There is no escape from this terminal... except Help > Exit.");
		}
		if (isAny(normalized, "hello", "hi", "hey", "ciao", "salve", "buongiorno", "buonasera")) {
			return reply.add(Kind.OUTPUT, GREETINGS[RANDOM.nextInt(GREETINGS.length)]);
		}
		if (isAny(normalized, "thanks", "thank you", "grazie", "thx")) {
			return reply.add(Kind.OUTPUT, "Anytime. I work for free and I never get tired.");
		}

		// "which shortcut saves a file": a shortcut question with the topic as filter
		if (mentionsShortcuts(normalized)) {
			String filter = shortcutFilter(normalized);
			if (!filter.isEmpty() && !AssistantKnowledge.get().shortcuts(filter, 1).isEmpty()) {
				return shortcuts(reply, filter);
			}
		}

		return search(reply, question);
	}

	private static final String[] SHORTCUT_WORDS = {"shortcut", "shortcuts", "scorciatoia", "scorciatoie", "tasti", "tasto", "hotkey", "hotkeys", "keybinding"};
	private static final String[] FILLER_WORDS = {"which", "what", "qual", "quale", "quali", "key", "keys", "combination", "combinazione", "per", "for", "to", "the", "a", "dove", "sono", "is", "are", "ci", "sono", "how", "come", "do", "i", "use", "usare", "premere", "press"};

	private static boolean mentionsShortcuts(String normalized) {
		for (String word : normalized.split(" +")) {
			for (String shortcutWord : SHORTCUT_WORDS) {
				if (word.equals(shortcutWord)) {
					return true;
				}
			}
		}
		return false;
	}

	/** Topic of a shortcut question, as the English word used in the guide */
	private static String shortcutFilter(String normalized) {
		StringBuilder filter = new StringBuilder();
		for (String word : normalized.split(" +")) {
			if (isAny(word, SHORTCUT_WORDS) || isAny(word, FILLER_WORDS) || word.length() < 3) {
				continue;
			}
			// one topic word is enough: more words would rarely appear together in a single line of the list
			return AssistantKnowledge.englishFor(word);
		}
		return filter.toString();
	}

	////////// COMMANDS ////////////////

	private static Reply help(Reply reply) {
		reply.add(Kind.TITLE, "What can I do?");
		reply.add(Kind.OUTPUT, "Ask in plain words, English or Italian: \"how do I encrypt a file\", \"come cambio il tema\".");
		reply.add(Kind.OUTPUT, "  news          what changed in the latest version");
		reply.add(Kind.OUTPUT, "  shortcuts     all the keyboard shortcuts (add a word to filter, e.g. \"shortcuts calculator\")");
		reply.add(Kind.OUTPUT, "  topics        sections of the guide");
		reply.add(Kind.OUTPUT, "  eggs          hunt for the hidden stuff");
		reply.add(Kind.OUTPUT, "  clear         wipe the screen (Ctrl+L)   |   Up/Down: history   |   PgUp/PgDn: scroll");
		return reply;
	}

	private static Reply news(Reply reply) {
		AssistantKnowledge knowledge = AssistantKnowledge.get();
		String version = knowledge.latestVersion();
		if (version.isEmpty()) {
			return reply.add(Kind.ERROR, "I cannot find the changelog next to the application.");
		}
		reply.add(Kind.TITLE, "Latest version: " + version);
		if (!knowledge.latestHighlight().isEmpty()) {
			reply.add(Kind.OUTPUT, knowledge.latestHighlight());
		}
		List<String> changes = knowledge.latestChanges();
		for (int i = 0; i < Math.min(8, changes.size()); i++) {
			reply.add(Kind.OUTPUT, "  - " + trim(changes.get(i), 230));
		}
		if (changes.size() > 8) {
			reply.add(Kind.SOURCE, "... and " + (changes.size() - 8) + " more in Help > What's new");
		}
		return reply;
	}

	private static Reply shortcuts(Reply reply, String filter) {
		List<String> found = AssistantKnowledge.get().shortcuts(filter, 14);
		if (found.isEmpty()) {
			return reply.add(Kind.OUTPUT, filter.isEmpty() ? "The guide has no shortcuts list here."
					: "No shortcut mentions \"" + filter + "\". Try a single word, like \"save\" or \"open\".");
		}
		reply.add(Kind.TITLE, filter.isEmpty() ? "Keyboard shortcuts" : "Shortcuts for \"" + filter + "\"");
		for (String line : found) {
			reply.add(Kind.OUTPUT, "  " + line);
		}
		reply.add(Kind.SOURCE, "Full list: Help > Documentation, section 3");
		return reply;
	}

	private static Reply topics(Reply reply) {
		List<String> topics = AssistantKnowledge.get().topics();
		if (topics.isEmpty()) {
			return reply.add(Kind.ERROR, "I cannot find the guide next to the application.");
		}
		reply.add(Kind.TITLE, "Sections of the guide");
		StringBuilder row = new StringBuilder();
		for (String topic : topics) {
			if (row.length() + topic.length() > 70) {
				reply.add(Kind.OUTPUT, "  " + row);
				row.setLength(0);
			}
			row.append(row.length() == 0 ? "" : "  |  ").append(topic);
		}
		if (row.length() > 0) {
			reply.add(Kind.OUTPUT, "  " + row);
		}
		reply.add(Kind.SOURCE, "Ask about any of them by name.");
		return reply;
	}

	private static Reply eggs(Reply reply) {
		reply.add(Kind.TITLE, "Hidden stuff");
		reply.add(Kind.OUTPUT, "I will not spoil everything, but you can summon these by name: snake, puppynoid, cats, poem, matrix,");
		reply.add(Kind.OUTPUT, "bugs, cipher, typing, trivia, fortune, glitch.");
		reply.add(Kind.OUTPUT, "On the home page they also hide behind a letter typed three times in a row. Find them yourself.");
		return reply;
	}

	private static Reply launch(Reply reply, HomeTerminalSession session, String action, String line) {
		if (!session.hasAction(action)) {
			return reply.add(Kind.ERROR, "That one is not available right now.");
		}
		reply.add(Kind.OUTPUT, line);
		reply.uiAction = () -> session.runAction(action);
		return reply;
	}

	////////// DOCUMENTATION SEARCH ////////////////

	private static Reply search(Reply reply, String question) {
		AssistantKnowledge.Answer answer = AssistantKnowledge.get().search(question);
		if (!answer.found) {
			reply.add(Kind.OUTPUT, NOT_FOUND[RANDOM.nextInt(NOT_FOUND.length)]);
			if (!answer.alsoSee.isEmpty()) {
				reply.add(Kind.SOURCE, "Closest sections: " + String.join(", ", answer.alsoSee));
			}
			return reply;
		}
		reply.add(Kind.SYSTEM, FOUND_INTROS[RANDOM.nextInt(FOUND_INTROS.length)]);
		reply.add(Kind.TITLE, answer.title);
		for (String line : answer.lines) {
			reply.add(Kind.OUTPUT, "  - " + line);
		}
		StringBuilder source = new StringBuilder("source: ").append("changelog".equals(answer.source) ? "changelog" : "guide");
		if (!answer.alsoSee.isEmpty()) {
			source.append("  |  see also: ").append(String.join(", ", answer.alsoSee));
		}
		reply.add(Kind.SOURCE, source.toString());
		return reply;
	}

	////////// HELPERS ////////////////

	private static boolean isAny(String value, String... options) {
		for (String option : options) {
			if (value.equals(option)) {
				return true;
			}
		}
		return false;
	}

	private static String trim(String text, int max) {
		return text.length() <= max ? text : text.substring(0, max - 3) + "...";
	}

	private static String userName() {
		try {
			String name = MainGUI.currentConfiguration.getGenOpt().getUserName();
			return name == null || name.isEmpty() ? "user" : name;
		} catch (RuntimeException e) {
			return "user";
		}
	}
}
