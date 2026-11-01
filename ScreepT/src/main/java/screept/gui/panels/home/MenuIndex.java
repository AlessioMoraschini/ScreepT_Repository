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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * Index of the menu bar of the application: every item with its path and shortcut, read from the real menus, so the
 * assistant can tell how to reach a section ("Creept > Text Editor Module, Ctrl + Alt + Shift + T") and the answer is
 * always the one of this version of the application. Built on the EDT, searched anywhere (it is an immutable snapshot).
 */
final class MenuIndex {

	/** Words that ask for a way to reach something: they are not part of the name of what is searched */
	private static final Set<String> LAUNCH_WORDS = new HashSet<>(Arrays.asList(
			"open", "start", "launc", "show", "run", "go", "where", "reach", "access", "enter", "begin", "find", "get", "menu", "section",
			"function", "feature", "tool", "panel", "window", "frame", "launch"));

	/** An item of the menu bar */
	static final class Entry {
		final String text;
		final String path;
		final String shortcut;
		final boolean submenu;
		final Set<String> stems;

		Entry(String text, String path, String shortcut, boolean submenu) {
			this.text = text;
			this.path = path;
			this.shortcut = shortcut;
			this.submenu = submenu;
			this.stems = new LinkedHashSet<>(AssistantKnowledge.stems(text + " " + path));
		}
	}

	private final List<Entry> entries;

	private MenuIndex(List<Entry> entries) {
		this.entries = entries;
	}

	static MenuIndex empty() {
		return new MenuIndex(new ArrayList<Entry>());
	}

	/** Reads the whole menu bar (call on the EDT) */
	static MenuIndex scan(JMenuBar bar) {
		List<Entry> entries = new ArrayList<>();
		if (bar != null) {
			for (int i = 0; i < bar.getMenuCount(); i++) {
				JMenu menu = bar.getMenu(i);
				if (menu != null) {
					scan(menu, "", entries);
				}
			}
		}
		return new MenuIndex(entries);
	}

	private static void scan(JMenu menu, String parentPath, List<Entry> entries) {
		String name = clean(menu.getText());
		if (name.isEmpty()) {
			return;
		}
		entries.add(new Entry(name, parentPath.isEmpty() ? "Menu bar" : parentPath, null, true));
		String path = parentPath.isEmpty() ? name : parentPath + " > " + name;
		for (int i = 0; i < menu.getItemCount(); i++) {
			JMenuItem item = menu.getItem(i);
			if (item == null) {
				continue;
			}
			if (item instanceof JMenu) {
				scan((JMenu) item, path, entries);
			} else {
				String text = clean(item.getText());
				if (!text.isEmpty()) {
					entries.add(new Entry(text, path, describe(item.getAccelerator()), false));
				}
			}
		}
	}

	private static String clean(String text) {
		return text == null ? "" : text.replaceAll("<[^>]+>", "").trim();
	}

	/** "Ctrl + Alt + N", or null when the item has no accelerator */
	static String describe(KeyStroke keyStroke) {
		if (keyStroke == null) {
			return null;
		}
		int modifiers = keyStroke.getModifiers();
		StringBuilder text = new StringBuilder();
		if ((modifiers & (InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK)) != 0) {
			text.append("Ctrl + ");
		}
		if ((modifiers & (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) != 0) {
			text.append("Alt + ");
		}
		if ((modifiers & (InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK)) != 0) {
			text.append("Shift + ");
		}
		if ((modifiers & (InputEvent.META_DOWN_MASK | InputEvent.META_MASK)) != 0) {
			text.append("Meta + ");
		}
		return text.append(KeyEvent.getKeyText(keyStroke.getKeyCode())).toString();
	}

	int size() {
		return entries.size();
	}

	/** True when the question asks how to reach something ("open the calculator", "where is the text editor") */
	static boolean asksToReach(String query) {
		for (String stem : AssistantKnowledge.queryStems(query)) {
			if (LAUNCH_WORDS.contains(stem) && !stem.equals("menu") && !stem.equals("find")) {
				return true;
			}
		}
		String normalized = AssistantKnowledge.normalize(query);
		return normalized.contains("how do i get") || normalized.contains("come arrivo") || normalized.contains("come si arriva")
				|| normalized.contains("dove si trova") || normalized.contains("dove trovo") || normalized.contains("where is");
	}

	/** Items matching the words of the question, best first (at most {@code limit}); leaves before submenus on equal match */
	List<Entry> search(String query, int limit) {
		List<String> terms = new ArrayList<>();
		for (String stem : AssistantKnowledge.queryStems(query)) {
			if (!LAUNCH_WORDS.contains(stem)) {
				terms.add(stem);
			}
		}
		List<Entry> result = new ArrayList<>();
		if (terms.isEmpty()) {
			return result;
		}

		final List<Double> scores = new ArrayList<>();
		for (Entry entry : entries) {
			int matched = 0;
			for (String term : terms) {
				if (matches(entry.stems, term)) {
					matched++;
				}
			}
			// a single matching word among many is too weak, unless the question has one word only
			if (matched == 0 || (terms.size() > 1 && matched * 2 < terms.size())) {
				continue;
			}
			double score = matched / (double) terms.size();
			// words of the name itself count more than words of the path
			Set<String> ownStems = new HashSet<>(AssistantKnowledge.stems(entry.text));
			int own = 0;
			for (String term : terms) {
				if (matches(ownStems, term)) {
					own++;
				}
			}
			score += own / (double) (terms.size() * 2);
			score -= entry.stems.size() * 0.01;
			score -= entry.submenu ? 0.05 : 0;
			result.add(entry);
			scores.add(score);
		}

		// sort by score (indexes sorted together)
		List<Integer> order = new ArrayList<>();
		for (int i = 0; i < result.size(); i++) {
			order.add(i);
		}
		order.sort((a, b) -> Double.compare(scores.get(b), scores.get(a)));
		List<Entry> sorted = new ArrayList<>();
		for (int i = 0; i < Math.min(limit, order.size()); i++) {
			sorted.add(result.get(order.get(i)));
		}
		return sorted;
	}

	/**
	 * True when every word of the question (an Italian word counts through its English terms) is found in the item:
	 * the user named it, as in "text editor" or "home page".
	 */
	boolean covers(Entry entry, String query) {
		int words = 0;
		for (String token : AssistantKnowledge.normalize(query).split("[^a-z0-9]+")) {
			if (token.length() < 2 || AssistantKnowledge.isStopWord(token)) {
				continue;
			}
			List<String> candidates = new ArrayList<>(AssistantKnowledge.queryStems(token));
			candidates.removeIf(LAUNCH_WORDS::contains);
			if (candidates.isEmpty()) {
				continue;
			}
			words++;
			boolean covered = false;
			for (String candidate : candidates) {
				if (matches(entry.stems, candidate)) {
					covered = true;
					break;
				}
			}
			if (!covered) {
				return false;
			}
		}
		return words > 0;
	}

	/** Exact stem, or a typo of it (one letter) for words long enough */
	private static boolean matches(Set<String> stems, String term) {
		if (stems.contains(term)) {
			return true;
		}
		if (term.length() < 5) {
			return false;
		}
		for (String stem : stems) {
			if (Math.abs(stem.length() - term.length()) <= 1 && AssistantKnowledge.editDistance(stem, term, 1) <= 1) {
				return true;
			}
		}
		return false;
	}
}
