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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import resources.GeneralConfig;
import screept.gui.panels.home.AssistantKnowledge;

/**
 * The true part of an answer: facts about ScreepT coherent with what the visitor said. Fixed facts come from the
 * application itself (version, author, shortcuts of the guide...), the rest is searched in the shipped guide and changelog.
 */
final class NpcFacts {

	/** A fact for the theme, or null when there is nothing true and useful to add */
	String factFor(NpcTheme theme, String question) {
		switch (theme) {
		case WHO:
			return "ScreepT " + GeneralConfig.APP_VERSION + " is a desktop crypter and text editor with plugins, made by " + GeneralConfig.AUTHOR + ".";
		case FEATURES:
			return "Text editor with tabs and a big-file mode (LogSearch), crypter with key images, calculator, quick notes, a real terminal and an offline assistant on the home page.";
		case SECRET:
			return "The crypter encrypts with AES, and a key image can act as a second factor. Pick a long password.";
		case BIG:
			return "LogSearch (Various > LogSearch, Ctrl+Alt+L) opens huge files read-only, a portion at a time, with text or regex search.";
		case TERMINAL:
			return "The shell (Ctrl+Alt+S, or the panel of the text editor) has an Interactive checkbox: a real terminal, where even Claude Code runs.";
		case EGG:
			return "I will not spoil them. Say what you would like to see, or how you feel about ScreepT.";
		case CATS:
			return "Feo and Nano are real and live with the author. Here they are pixels, for now.";
		case SHORTCUT:
			return shortcutFact(question);
		case VERSION:
			return "You are on version " + GeneralConfig.APP_VERSION + ", build #" + GeneralConfig.BUILD_INCREMENTAL_NUMBER + ", built on "
					+ GeneralConfig.BUILD_TS_DATE + ". Updates are checked at start (Generic options).";
		case TIME:
			return "It is " + new SimpleDateFormat("EEEE d MMMM yyyy, HH:mm", Locale.ENGLISH).format(new Date()) + ".";
		case BUG:
			return "Errors are written to the LOG folder next to the application: look there first.";
		case CODE:
			return "ScreepT is written in Java 8 with Swing. Yes, Swing. No, it does not care.";
		case DEFAULT:
			return documentationFact(question);
		default:
			return null;
		}
	}

	private static String shortcutFact(String question) {
		AssistantKnowledge knowledge = AssistantKnowledge.get();
		for (String word : NpcText.normalize(question).trim().split(" ")) {
			if (word.length() < 4 || NpcTheme.detect(" " + word + " ") == NpcTheme.SHORTCUT) {
				continue;
			}
			List<String> found = knowledge.shortcuts(word, 2);
			if (!found.isEmpty()) {
				return "From the guide: " + NpcText.cut(String.join("  |  ", found), 200);
			}
		}
		return "The full list of shortcuts is in Help > Documentation, section 3.";
	}

	private static String documentationFact(String question) {
		AssistantKnowledge.Answer answer = AssistantKnowledge.get().search(question);
		if (!answer.found || answer.lines.isEmpty()) {
			return null;
		}
		String line = answer.lines.get(0);
		for (String candidate : answer.lines) {
			if (candidate.length() >= 40) {
				line = candidate;
				break;
			}
		}
		return "From the manual, " + answer.title + ": " + NpcText.cut(line, 180);
	}
}
