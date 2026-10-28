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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import screept.gui.panels.home.AssistantKnowledge;

/**
 * Easter egg: ScreepT shortcut trivia. Questions come from the shortcuts list of the guide that ships with the
 * application, so the quiz is always up to date with the documentation.
 */
public final class ShortcutTriviaFrame extends EggFrame {

	private static final long serialVersionUID = 1L;

	private static final String GAME_ID = "shortcutTrivia";
	private static final int QUESTIONS = 10;
	private static final Pattern SHORTCUT_LINE = Pattern.compile("^(.+?)\\s+-\\s+\\[(.+)\\]\\s*$");

	public static void open() {
		if (focusIfOpen(ShortcutTriviaFrame.class)) {
			return;
		}
		SwingUtilities.invokeLater(() -> new ShortcutTriviaFrame().setVisible(true));
	}

	private final JLabel headerLabel = new JLabel("SHORTCUT TRIVIA", SwingConstants.CENTER);
	private final JLabel questionLabel = new JLabel(" ", SwingConstants.CENTER);
	private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
	private final JButton[] answerButtons = new JButton[4];
	private final JButton nextButton = new JButton("Start");

	/** function name -> keys, from the guide */
	private final Map<String, String> shortcuts = new LinkedHashMap<>();
	private final List<String> questionOrder = new ArrayList<>();
	private int index;
	private int score;
	private int best = bestScore(GAME_ID);
	private String correctKeys = "";
	private boolean answered;

	private ShortcutTriviaFrame() {
		super("Shortcut Trivia - ScreepT easter egg", 700, 420);
		loadShortcuts();

		JPanel content = new JPanel(new BorderLayout(0, 12));
		content.setBackground(BACKGROUND);
		content.setBorder(BorderFactory.createEmptyBorder(18, 26, 18, 26));

		headerLabel.setFont(mono(Font.BOLD, 22));
		headerLabel.setForeground(TEAL);
		questionLabel.setFont(mono(Font.BOLD, 20));
		questionLabel.setForeground(TEXT);
		statusLabel.setFont(mono(Font.PLAIN, 15));
		statusLabel.setForeground(MUTED);

		JPanel answers = new JPanel(new GridLayout(2, 2, 10, 10));
		answers.setOpaque(false);
		for (int i = 0; i < answerButtons.length; i++) {
			JButton button = new JButton(" ");
			style(button);
			final JButton clicked = button;
			button.addActionListener(e -> answer(clicked));
			answerButtons[i] = button;
			answers.add(button);
		}
		style(nextButton);
		nextButton.addActionListener(e -> next());

		JPanel top = new JPanel(new GridLayout(2, 1, 0, 10));
		top.setOpaque(false);
		top.add(headerLabel);
		top.add(questionLabel);
		JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 8));
		bottom.setOpaque(false);
		bottom.add(statusLabel);
		bottom.add(nextButton);

		content.add(top, BorderLayout.NORTH);
		content.add(answers, BorderLayout.CENTER);
		content.add(bottom, BorderLayout.SOUTH);
		setContentPane(content);
		pack();

		showIntro();
	}

	private static void style(JButton button) {
		button.setFont(mono(Font.BOLD, 17));
		button.setBackground(PANEL);
		button.setForeground(TEXT);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TEAL), BorderFactory.createEmptyBorder(8, 10, 8, 10)));
		button.setPreferredSize(new Dimension(300, 52));
	}

	/** Reads "FUNCTION NAME - [Keys]" lines from the guide, keeping unique names and unique key combinations */
	private void loadShortcuts() {
		List<String> lines = AssistantKnowledge.get().shortcuts("", 600);
		List<String> usedKeys = new ArrayList<>();
		for (String line : lines) {
			Matcher matcher = SHORTCUT_LINE.matcher(line.trim());
			if (matcher.matches()) {
				String name = matcher.group(1).trim();
				String keys = matcher.group(2).trim();
				if (name.length() < 60 && !shortcuts.containsKey(name) && !usedKeys.contains(keys)) {
					shortcuts.put(name, keys);
					usedKeys.add(keys);
				}
			}
		}
	}

	private void showIntro() {
		setAnswersEnabled(false);
		if (shortcuts.size() < 8) {
			questionLabel.setText("I cannot find enough shortcuts in the guide.");
			statusLabel.setText("Is the Readme folder next to the Application one?");
			nextButton.setEnabled(false);
			return;
		}
		questionLabel.setText("Do you really know your ScreepT shortcuts?");
		statusLabel.setText(QUESTIONS + " questions from the guide. Best: " + best + "/" + QUESTIONS);
		nextButton.setText("Start");
	}

	private void startQuiz() {
		questionOrder.clear();
		questionOrder.addAll(shortcuts.keySet());
		Collections.shuffle(questionOrder);
		index = 0;
		score = 0;
		showQuestion();
	}

	private void showQuestion() {
		String name = questionOrder.get(index);
		correctKeys = shortcuts.get(name);
		questionLabel.setText("<html><div style='text-align:center'>" + (index + 1) + "/" + QUESTIONS + " - which keys do<br><span style='color:#F2B84B'>"
				+ escape(name) + "</span> ?</div></html>");

		List<String> options = new ArrayList<>();
		options.add(correctKeys);
		List<String> others = new ArrayList<>(shortcuts.values());
		Collections.shuffle(others);
		for (String keys : others) {
			if (options.size() < 4 && !options.contains(keys)) {
				options.add(keys);
			}
		}
		Collections.shuffle(options);
		for (int i = 0; i < answerButtons.length; i++) {
			answerButtons[i].setText(escapeButton(options.get(i)));
			answerButtons[i].putClientProperty("keys", options.get(i));
			answerButtons[i].setBackground(PANEL);
		}
		answered = false;
		setAnswersEnabled(true);
		statusLabel.setForeground(MUTED);
		statusLabel.setText("Score " + score);
		nextButton.setText("...");
		nextButton.setEnabled(false);
	}

	private void answer(JButton clicked) {
		if (answered) {
			return;
		}
		answered = true;
		boolean right = correctKeys.equals(clicked.getClientProperty("keys"));
		if (right) {
			score++;
			clicked.setBackground(new Color(0x1F5F3C));
			statusLabel.setForeground(GREEN);
			statusLabel.setText("Correct! Score " + score);
		} else {
			clicked.setBackground(new Color(0x6B2A2A));
			for (JButton button : answerButtons) {
				if (correctKeys.equals(button.getClientProperty("keys"))) {
					button.setBackground(new Color(0x1F5F3C));
				}
			}
			statusLabel.setForeground(RED);
			statusLabel.setText("Nope, it was " + correctKeys);
		}
		nextButton.setEnabled(true);
		nextButton.setText(index + 1 >= QUESTIONS ? "Results" : "Next");
		nextButton.requestFocusInWindow();
	}

	private void next() {
		if (questionOrder.isEmpty()) {
			startQuiz();
		} else if (answered) {
			if (index + 1 >= QUESTIONS) {
				showResult();
			} else {
				index++;
				showQuestion();
			}
		}
	}

	private void showResult() {
		setAnswersEnabled(false);
		boolean record = saveBestScore(GAME_ID, score);
		best = Math.max(best, score);
		String verdict = score >= 9 ? "Keyboard wizard" : score >= 6 ? "Solid power user" : score >= 3 ? "Mouse lover" : "Did you read the guide?";
		questionLabel.setText("<html><div style='text-align:center'>" + score + "/" + QUESTIONS + " - " + verdict + "</div></html>");
		statusLabel.setForeground(AMBER);
		statusLabel.setText(record ? "New record! Play again?" : "Best: " + best + "/" + QUESTIONS + ". Play again?");
		questionOrder.clear();
		nextButton.setText("Play again");
		nextButton.setEnabled(true);
	}

	private void setAnswersEnabled(boolean enabled) {
		for (JButton button : answerButtons) {
			button.setEnabled(enabled);
			if (!enabled) {
				button.setText(" ");
				button.setBackground(PANEL);
			}
		}
	}

	private static String escape(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private static String escapeButton(String text) {
		return text;
	}
}
