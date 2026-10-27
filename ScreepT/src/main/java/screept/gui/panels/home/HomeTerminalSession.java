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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

/**
 * State of the home terminal, independent from the home panel (which is rebuilt every time the home page is shown):
 * lines, typed text, history and the running questions live here, so leaving the home page (text editor, crypter...)
 * never loses anything and answers computed meanwhile are waiting when the user comes back.
 * Questions are answered on a single background daemon thread that exits when idle, the UI is never blocked.
 * Everything but {@link #submit()} internals is meant to be called on the EDT.
 */
public final class HomeTerminalSession {

	public static final String PROMPT = "screept@home:~$ ";

	private static final int MAX_LINES = 400;
	private static final int MAX_HISTORY = 100;
	private static final int MAX_PENDING = 3;
	private static final int MAX_INPUT = 300;

	/** Kind of line, used for the color */
	public enum Kind {
		INPUT, OUTPUT, TITLE, SOURCE, ERROR, SYSTEM
	}

	public static final class Line {
		public final Kind kind;
		public final String text;
		/** Characters already revealed by the typewriter effect */
		int shown;

		Line(Kind kind, String text, boolean animated) {
			this.kind = kind;
			this.text = text;
			this.shown = animated ? 0 : text.length();
		}

		public String visibleText() {
			return shown >= text.length() ? text : text.substring(0, shown);
		}

		public boolean isComplete() {
			return shown >= text.length();
		}
	}

	private static HomeTerminalSession instance;

	public static synchronized HomeTerminalSession get() {
		if (instance == null) {
			instance = new HomeTerminalSession();
		}
		return instance;
	}

	private final List<Line> lines = new ArrayList<>();
	private final List<String> history = new ArrayList<>();
	private final StringBuilder input = new StringBuilder();
	private final Map<String, Runnable> actions = new ConcurrentHashMap<>();
	private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
	private final ThreadPoolExecutor executor;

	private int caret;
	private int historyPosition;
	private int pending;
	private int version;
	private boolean userInteracted;

	private HomeTerminalSession() {
		// no core thread: the worker is created on demand and dies after 20 seconds of inactivity
		executor = new ThreadPoolExecutor(0, 1, 20, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), runnable -> {
			Thread thread = new Thread(runnable, "screept-assistant");
			thread.setDaemon(true);
			thread.setPriority(Thread.NORM_PRIORITY - 1);
			return thread;
		});

		append(Kind.SYSTEM, "ScreepT assistant - offline, no cloud, no keys.", false);
		append(Kind.SYSTEM, "Type a question about ScreepT, or \"help\".", false);
		// documents are read in the background, so the first answer is immediate
		executor.execute(() -> AssistantKnowledge.get().ensureLoaded());
	}

	////////// VIEW SUPPORT ////////////////

	public void addListener(Runnable listener) {
		listeners.add(listener);
	}

	public void removeListener(Runnable listener) {
		listeners.remove(listener);
	}

	private void changed() {
		version++;
		for (Runnable listener : listeners) {
			listener.run();
		}
	}

	public int version() {
		return version;
	}

	public List<Line> lines() {
		return Collections.unmodifiableList(lines);
	}

	public String input() {
		return input.toString();
	}

	public int caret() {
		return caret;
	}

	public int pending() {
		return pending;
	}

	public boolean hasInput() {
		return input.length() > 0;
	}

	/** True once the user typed anything: before that the console stays hidden behind the home image */
	public boolean userInteracted() {
		return userInteracted;
	}

	public boolean hasUnrevealed() {
		for (int i = lines.size() - 1; i >= 0 && i >= lines.size() - 30; i--) {
			if (!lines.get(i).isComplete()) {
				return true;
			}
		}
		return false;
	}

	/** Reveals up to {@code chars} characters of the first incomplete line, returns true when something changed */
	public boolean advanceReveal(int chars) {
		int remaining = 0;
		for (int i = Math.max(0, lines.size() - 30); i < lines.size(); i++) {
			remaining += lines.get(i).text.length() - lines.get(i).shown;
		}
		// long answers speed up, so they never take more than about a second to appear
		int step = Math.max(chars, remaining / 30);
		for (int i = Math.max(0, lines.size() - 30); i < lines.size(); i++) {
			Line line = lines.get(i);
			if (!line.isComplete()) {
				line.shown = Math.min(line.text.length(), line.shown + step);
				version++;
				return true;
			}
		}
		return false;
	}

	public void completeReveal() {
		boolean any = false;
		for (Line line : lines) {
			if (!line.isComplete()) {
				line.shown = line.text.length();
				any = true;
			}
		}
		if (any) {
			version++;
		}
	}

	////////// INPUT ////////////////

	public void insert(String text) {
		userInteracted = true;
		String clean = text.replaceAll("[\\r\\n\\t]+", " ");
		int room = MAX_INPUT - input.length();
		if (room <= 0 || clean.isEmpty()) {
			return;
		}
		if (clean.length() > room) {
			clean = clean.substring(0, room);
		}
		input.insert(caret, clean);
		caret += clean.length();
		historyPosition = history.size();
		changed();
	}

	public void backspace() {
		if (caret > 0) {
			input.deleteCharAt(--caret);
			changed();
		}
	}

	public void delete() {
		if (caret < input.length()) {
			input.deleteCharAt(caret);
			changed();
		}
	}

	public void moveCaret(int delta) {
		caret = Math.max(0, Math.min(input.length(), caret + delta));
		changed();
	}

	public void caretToStart() {
		caret = 0;
		changed();
	}

	public void caretToEnd() {
		caret = input.length();
		changed();
	}

	public void clearScreen() {
		lines.clear();
		changed();
	}

	public void clearInput() {
		input.setLength(0);
		caret = 0;
		historyPosition = history.size();
		changed();
	}

	/** Removes the last {@code count} typed characters when they are the same letter (the keys of an easter egg shortcut) */
	public void undoRepeatedTyping(int count) {
		int length = input.length();
		if (length < count) {
			return;
		}
		char letter = Character.toLowerCase(input.charAt(length - 1));
		for (int i = 1; i < count; i++) {
			if (Character.toLowerCase(input.charAt(length - 1 - i)) != letter) {
				return;
			}
		}
		input.setLength(length - count);
		caret = Math.min(caret, input.length());
		changed();
	}

	public void historyUp() {
		if (historyPosition > 0) {
			setInputFromHistory(--historyPosition);
		}
	}

	public void historyDown() {
		if (historyPosition < history.size() - 1) {
			setInputFromHistory(++historyPosition);
		} else if (historyPosition < history.size()) {
			historyPosition = history.size();
			input.setLength(0);
			caret = 0;
			changed();
		}
	}

	private void setInputFromHistory(int position) {
		input.setLength(0);
		input.append(history.get(position));
		caret = input.length();
		changed();
	}

	////////// QUESTIONS ////////////////

	public void registerAction(String name, Runnable action) {
		actions.put(name, action);
	}

	boolean hasAction(String name) {
		return actions.containsKey(name);
	}

	/** Runs a registered action (easter eggs...) on the EDT, false when unknown */
	boolean runAction(String name) {
		Runnable action = actions.get(name);
		if (action == null) {
			return false;
		}
		SwingUtilities.invokeLater(action);
		return true;
	}

	/** Sends the typed text to the assistant: the answer arrives later, whatever is shown meanwhile */
	public void submit() {
		String question = input.toString().trim();
		input.setLength(0);
		caret = 0;
		if (question.isEmpty()) {
			changed();
			return;
		}
		userInteracted = true;
		if (history.isEmpty() || !history.get(history.size() - 1).equals(question)) {
			history.add(question);
			if (history.size() > MAX_HISTORY) {
				history.remove(0);
			}
		}
		historyPosition = history.size();
		append(Kind.INPUT, PROMPT + question, false);

		if (pending >= MAX_PENDING) {
			append(Kind.ERROR, "Too many questions in a row, give me a second.", true);
			return;
		}
		pending++;
		changed();
		executor.execute(() -> {
			AssistantEngine.Reply reply;
			try {
				reply = AssistantEngine.reply(question, this);
			} catch (Throwable error) {
				reply = new AssistantEngine.Reply();
				reply.add(Kind.ERROR, "Something went wrong while thinking: " + error.getClass().getSimpleName());
			}
			final AssistantEngine.Reply done = reply;
			SwingUtilities.invokeLater(() -> deliver(done));
		});
	}

	private void deliver(AssistantEngine.Reply reply) {
		pending = Math.max(0, pending - 1);
		if (reply.clear) {
			lines.clear();
		}
		for (AssistantEngine.Reply.Entry entry : reply.entries) {
			append(entry.kind, entry.text, true);
		}
		if (reply.uiAction != null) {
			try {
				reply.uiAction.run();
			} catch (RuntimeException e) {
				append(Kind.ERROR, "That did not work: " + e.getMessage(), true);
			}
		}
		changed();
	}

	private void append(Kind kind, String text, boolean animated) {
		lines.add(new Line(kind, text, animated && kind != Kind.INPUT));
		while (lines.size() > MAX_LINES) {
			lines.remove(0);
		}
		changed();
	}
}
