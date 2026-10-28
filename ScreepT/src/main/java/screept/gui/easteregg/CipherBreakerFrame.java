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
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Easter egg: decrypt the Caesar-ciphered words before the time runs out. The shift is shown, the rest is up to you.
 * Words get longer and time shorter at every level, three mistakes end the game.
 */
public final class CipherBreakerFrame extends EggFrame {

	private static final long serialVersionUID = 1L;

	private static final String GAME_ID = "cipherBreaker";
	private static final int START_LIVES = 3;

	public static void open() {
		if (focusIfOpen(CipherBreakerFrame.class)) {
			return;
		}
		SwingUtilities.invokeLater(() -> new CipherBreakerFrame().setVisible(true));
	}

	private final Random random = new Random();
	private final JLabel titleLabel = new JLabel("CIPHER BREAKER", SwingConstants.CENTER);
	private final JLabel cipherLabel = new JLabel(" ", SwingConstants.CENTER);
	private final JLabel hintLabel = new JLabel(" ", SwingConstants.CENTER);
	private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
	private final JProgressBar timeBar = new JProgressBar(0, 1000);
	private final JTextField input = new JTextField();
	private final Timer timer = new Timer(50, e -> tick());

	private String plain = "";
	private int shift;
	private int level;
	private int score;
	private int lives;
	private int best = bestScore(GAME_ID);
	private long roundStart;
	private long roundMs;
	private boolean playing;

	private CipherBreakerFrame() {
		super("Cipher Breaker - ScreepT easter egg", 640, 360);

		JPanel content = new JPanel(new BorderLayout(0, 10));
		content.setBackground(BACKGROUND);
		content.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

		titleLabel.setFont(mono(Font.BOLD, 22));
		titleLabel.setForeground(TEAL);

		cipherLabel.setFont(mono(Font.BOLD, 34));
		cipherLabel.setForeground(AMBER);
		hintLabel.setFont(mono(Font.PLAIN, 16));
		hintLabel.setForeground(MUTED);
		statusLabel.setFont(mono(Font.PLAIN, 16));
		statusLabel.setForeground(TEXT);

		JPanel center = new JPanel(new GridLayout(3, 1, 0, 6));
		center.setOpaque(false);
		center.add(cipherLabel);
		center.add(hintLabel);
		center.add(statusLabel);

		timeBar.setForeground(TEAL);
		timeBar.setBackground(PANEL);
		timeBar.setBorderPainted(false);
		timeBar.setPreferredSize(new Dimension(10, 10));

		input.setFont(mono(Font.BOLD, 24));
		input.setHorizontalAlignment(SwingConstants.CENTER);
		input.setBackground(PANEL);
		input.setForeground(TEXT);
		input.setCaretColor(TEXT);
		input.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TEAL), BorderFactory.createEmptyBorder(6, 8, 6, 8)));
		input.addActionListener(e -> submit());

		JPanel bottom = new JPanel(new BorderLayout(0, 8));
		bottom.setOpaque(false);
		bottom.add(timeBar, BorderLayout.NORTH);
		bottom.add(input, BorderLayout.CENTER);

		content.add(titleLabel, BorderLayout.NORTH);
		content.add(center, BorderLayout.CENTER);
		content.add(bottom, BorderLayout.SOUTH);
		setContentPane(content);
		pack();

		showIntro();
	}

	@Override
	protected void onClosed() {
		timer.stop();
	}

	private void showIntro() {
		playing = false;
		cipherLabel.setText("???");
		hintLabel.setText("Each word is shifted by a Caesar cipher. Type it back, then press Enter.");
		statusLabel.setText("Press Enter to start. Best: " + best);
		timeBar.setValue(1000);
		input.requestFocusInWindow();
	}

	private void startGame() {
		level = 1;
		score = 0;
		lives = START_LIVES;
		playing = true;
		nextWord();
		timer.start();
	}

	private void nextWord() {
		String[] pool = level <= 4 ? ScreepTQuotes.CIPHER_EASY : ScreepTQuotes.CIPHER_HARD;
		plain = pool[random.nextInt(pool.length)];
		shift = 1 + random.nextInt(25);
		cipherLabel.setText(caesar(plain, shift));
		hintLabel.setText("Decrypt it: every letter was moved forward by " + shift);
		roundMs = Math.max(7000, 20000 - level * 1200L);
		roundStart = System.currentTimeMillis();
		updateStatus("Level " + level);
		input.setText("");
		input.requestFocusInWindow();
	}

	private void updateStatus(String prefix) {
		statusLabel.setText(prefix + "   Score " + score + "   Lives " + lives + "   Best " + best);
	}

	static String caesar(String text, int shift) {
		StringBuilder out = new StringBuilder();
		for (char c : text.toCharArray()) {
			out.append(c >= 'A' && c <= 'Z' ? (char) ('A' + (c - 'A' + shift) % 26) : c);
		}
		return out.toString();
	}

	private void submit() {
		if (!playing) {
			startGame();
			return;
		}
		if (input.getText().trim().equalsIgnoreCase(plain)) {
			long left = Math.max(0, roundMs - (System.currentTimeMillis() - roundStart));
			score += 10 + plain.length() + (int) (left / 1000);
			level++;
			nextWord();
		} else {
			lives--;
			input.setText("");
			if (lives <= 0) {
				gameOver("Out of lives. The word was " + plain + ".");
			} else {
				updateStatus("Wrong!");
				statusLabel.setForeground(RED);
			}
		}
	}

	private void tick() {
		if (!playing) {
			return;
		}
		long left = roundMs - (System.currentTimeMillis() - roundStart);
		timeBar.setValue((int) Math.max(0, left * 1000 / roundMs));
		timeBar.setForeground(left < 4000 ? RED : TEAL);
		if (statusLabel.getForeground() == RED && left % 1000 > 700) {
			statusLabel.setForeground(TEXT);
		}
		if (left <= 0) {
			lives--;
			if (lives <= 0) {
				gameOver("Time is up. The word was " + plain + ".");
			} else {
				updateStatus("Too slow!");
				nextWord();
			}
		}
	}

	private void gameOver(String reason) {
		playing = false;
		timer.stop();
		boolean record = saveBestScore(GAME_ID, score);
		best = Math.max(best, score);
		cipherLabel.setText("GAME OVER");
		hintLabel.setText(reason);
		statusLabel.setForeground(TEXT);
		statusLabel.setText("Score " + score + (record ? " - NEW RECORD!" : "") + "   Enter to play again");
		timeBar.setValue(0);
		input.setText("");
	}
}
