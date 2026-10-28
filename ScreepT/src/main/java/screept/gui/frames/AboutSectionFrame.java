/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.gui.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.log4j.Logger;

import gui.commons.frameutils.frame.arch.ParentFrame;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.commons.guistyle.TextGuiUtils;
import gui.commons.handlers.AlwaysOnTopManager;
import maingUI.MainGUI;
import maingUI.MainGuiGeneralAppStarter;
import net.miginfocom.swing.MigLayout;
import os.commons.utils.SysUtils;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import resources.ImagesPathConfigurator;
import screept.gui.easteregg.EasterEggPicker;
import screept.gui.style.CardPalette;
import various.common.light.easteregg.EasterEgg;

/**
 * About ScreepT, dark pixel-art screen (independent from the current look and feel):
 * - background: the AM binary logo pixelated, tinted with the accent color, CRT scanlines and vignette;
 * - title: text rendered at low resolution without antialiasing and scaled up with nearest neighbor;
 * - a floating pixel padlock and a terminal-like card with build data and a blinking cursor;
 * - clicking the background shows "Got an egg for you!", which launches a random easter egg.
 */
public class AboutSectionFrame extends ParentFrame {
	private static final long serialVersionUID = -1578977495369367445L;

	static Logger logger = Logger.getLogger(AboutSectionFrame.class);

	public static AboutSectionFrame singletonInstance = null;

	// PALETTE
	private static final Color BACKGROUND = new Color(0x15, 0x16, 0x1A);
	private static final Color CARD = new Color(0x1E, 0x1F, 0x22, 225);
	private static final Color CARD_BORDER = new Color(0x39, 0x3B, 0x40);
	private static final Color ACCENT = new Color(0x35, 0x74, 0xF0);
	private static final Color ACCENT_LIGHT = new Color(0x6F, 0xD3, 0xFF);
	private static final Color TEXT = new Color(0xDF, 0xE1, 0xE5);
	private static final Color MUTED = new Color(0x86, 0x8A, 0x91);
	private static final Color PROMPT = new Color(0x5F, 0xAD, 0x65);
	private static final Color EGG = new Color(0xF2, 0xC5, 0x5C);

	/** Size in screen pixels of one pixel-art pixel */
	private static final int PIXEL = 4;
	/** Background logo pixelation (bigger = bigger blocks) */
	private static final int LOGO_PIXEL = 6;
	private static final int ANIMATION_MS = 450;

	private static final String[] PADLOCK = {
			"...SSSSS...",
			"..S.....S..",
			".S.......S.",
			".S.......S.",
			".S.......S.",
			"XXXXXXXXXXX",
			"XOOOOOOOOOX",
			"XOOOO#OOOOX",
			"XOOO###OOOX",
			"XOOOO#OOOOX",
			"XOOOOOOOOOX",
			"XXXXXXXXXXX" };

	// GENERAL FIELDS //
	private final JFrame mainAppFrame;
	private final Timer animation;
	private int animationFrame;

	private JButton websiteButton;
	private JButton eggButton;
	private JTextField input;
	private JLabel responseLabel;
	private JPanel pickerHolder;
	private PixelSprite padlock;
	/** While the egg picker is shown the window stays above every other window, and so do the windows it opens */
	private AWTEventListener topmostFollower;
	private final List<Window> raisedWindows = new ArrayList<>();

	public AboutSectionFrame(MainGUI main) {
		super();

		isActive.set(true);
		logger.info(this.getClass().getName() + " Starting...");
		setTitle("About");

		mainAppFrame = main.getMainFrame();
		mainAppFrame.setEnabled(false);
		singletonInstance = this;
		GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_ABOUT, this);

		BackgroundPanel background = new BackgroundPanel(loadLogo());
		background.setLayout(new MigLayout("wrap 1, fillx, insets 28 34 18 34, hidemode 3", "[grow, center]"));
		setContentPane(background);

		padlock = new PixelSprite(PADLOCK);
		background.add(padlock, "split 2, center, gapright 18, aligny bottom");
		background.add(new PixelTitle(GeneralConfig.APPLICATION_SHORT_NAME), "aligny bottom");
		background.add(label("A smart crypter... and much more!", MUTED, Font.ITALIC, 14), "gaptop 6");
		background.add(createInfoCard(), "growx, gaptop 22");
		background.add(createEggButton(), "gaptop 12");
		background.add(label("© 2019-" + Year.now().getValue() + " " + GeneralConfig.AUTHOR + "   ·   Esc to close",
				MUTED, Font.PLAIN, 12), "gaptop 12");

		animation = new Timer(ANIMATION_MS, e -> animate());
		animation.start();

		addHandlers(background);
		SwingUtilities.invokeLater(() -> input.requestFocusInWindow());
		logger.info(this.getClass().getName() + " Started");
	}

	private JPanel createInfoCard() {

		JPanel card = new CardPanel();
		card.setLayout(new MigLayout("wrap 2, insets 18 24 18 24, gap 16 7, hidemode 3", "[right][grow, left]"));

		addInfoRow(card, "version", GeneralConfig.APP_VERSION);
		addInfoRow(card, "built on", GeneralConfig.BUILD_TS_DATE);
		addInfoRow(card, "build", "#" + GeneralConfig.BUILD_INCREMENTAL_NUMBER);
		addInfoRow(card, "made by", GeneralConfig.AUTHOR);

		card.add(promptLabel("website"));
		websiteButton = linkButton(GeneralConfig.WEBSITE, ACCENT_LIGHT);
		card.add(websiteButton);

		// the terminal line is real: type something and press Enter (some words get an answer)
		input = new JTextField();
		input.setFont(monospaced(Font.PLAIN, 15));
		input.setForeground(TEXT);
		input.setCaretColor(TEXT);
		input.setBorder(null);
		CardPalette.flatten(input);
		input.setOpaque(false);
		input.setBackground(new Color(0, 0, 0, 0));
		input.addActionListener(e -> answer(input.getText()));
		card.add(promptLabel(""), "gaptop 4");
		card.add(input, "gaptop 4, growx");

		responseLabel = new JLabel(" ");
		responseLabel.setFont(monospaced(Font.BOLD, 15));
		responseLabel.setForeground(EGG);
		responseLabel.setVisible(false);
		card.add(responseLabel, "span 2, growx, gaptop 8");

		pickerHolder = new JPanel(new BorderLayout());
		pickerHolder.setOpaque(false);
		pickerHolder.setVisible(false);
		card.add(pickerHolder, "span 2, growx, gaptop 6");
		return card;
	}

	/** Answers to what was typed in the terminal line */
	private void answer(String typed) {
		String text = typed == null ? "" : typed.trim().toLowerCase(Locale.ROOT);
		while (text.contains("  ")) {
			text = text.replace("  ", " ");
		}
		input.setText("");
		pickerHolder.removeAll();
		pickerHolder.setVisible(false);
		responseLabel.setVisible(false);
		setTopmost(false);

		if (text.equals("fuck")) {
			responseLabel.setText("fuck you");
			responseLabel.setVisible(true);
		} else if (text.equals("i love screept")) {
			responseLabel.setText("<html>I love you too, " + userName() + ". Have an egg, you earned it:</html>");
			responseLabel.setVisible(true);
			pickerHolder.add(new EasterEggPicker(), BorderLayout.CENTER);
			pickerHolder.setVisible(true);
			setTopmost(true);
		}
		getContentPane().revalidate();
		getContentPane().repaint();
		SwingUtilities.invokeLater(this::fitToContent);
	}

	/**
	 * Keeps the window above everything, also above other applications, and the windows opened from it (the eggs it launches)
	 * above it. Turned off, everything goes back to the normal always-on-top rule of the application.
	 */
	private void setTopmost(boolean on) {
		AlwaysOnTopManager.keepOnTop(this, on);
		if (on && topmostFollower == null) {
			topmostFollower = event -> {
				if (event.getID() == WindowEvent.WINDOW_OPENED) {
					Window opened = ((WindowEvent) event).getWindow();
					if (opened != this && opened != mainAppFrame && opened instanceof RootPaneContainer) {
						AlwaysOnTopManager.keepOnTop(opened, true);
						raisedWindows.add(opened);
					}
				}
			};
			Toolkit.getDefaultToolkit().addAWTEventListener(topmostFollower, AWTEvent.WINDOW_EVENT_MASK);
		} else if (!on && topmostFollower != null) {
			Toolkit.getDefaultToolkit().removeAWTEventListener(topmostFollower);
			topmostFollower = null;
			for (Window window : raisedWindows) {
				AlwaysOnTopManager.keepOnTop(window, false);
			}
			raisedWindows.clear();
		}
	}

	private static String userName() {
		try {
			String name = MainGUI.currentConfiguration.getGenOpt().getUserName();
			return name == null || name.isEmpty() ? "friend" : name;
		} catch (RuntimeException e) {
			return "friend";
		}
	}

	/** Grows the window (never shrinks it) so that the answer and the egg picker are fully visible, keeping it on its monitor */
	private void fitToContent() {
		Dimension wanted = getContentPane().getPreferredSize();
		Insets insets = getInsets();
		int height = Math.max(getHeight(), wanted.height + insets.top + insets.bottom);
		if (height != getHeight()) {
			Rectangle screen = getGraphicsConfiguration() != null ? getGraphicsConfiguration().getBounds() : new Rectangle(0, 0, 1920, 1080);
			int y = Math.max(screen.y, Math.min(getY(), screen.y + screen.height - height));
			setBounds(getX(), y, getWidth(), Math.min(height, screen.height));
		}
	}

	/** Hidden until the background is clicked */
	private JButton createEggButton() {
		eggButton = linkButton("▶ Got an egg for you!", EGG);
		eggButton.setFont(monospaced(Font.BOLD, 15));
		eggButton.setVisible(false);
		return eggButton;
	}

	private JButton linkButton(String text, Color color) {
		JButton button = new JButton(text);
		button.setFont(monospaced(Font.PLAIN, 15));
		button.setForeground(color);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.setOpaque(false);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setBorder(null);
		TextGuiUtils.underlineText(button);
		TextGuiUtils.hoverForeground(button, TEXT);
		return button;
	}

	private void addInfoRow(JPanel card, String key, String value) {
		card.add(promptLabel(key));
		JLabel valueLabel = new JLabel(value);
		valueLabel.setFont(monospaced(Font.PLAIN, 15));
		valueLabel.setForeground(TEXT);
		card.add(valueLabel);
	}

	private JLabel promptLabel(String key) {
		JLabel prompt = new JLabel("> " + key);
		prompt.setFont(monospaced(Font.BOLD, 15));
		prompt.setForeground(PROMPT);
		return prompt;
	}

	private static JLabel label(String text, Color color, int style, int size) {
		JLabel label = new JLabel(text);
		label.setFont(new Font("Segoe UI", style, size));
		label.setForeground(color);
		return label;
	}

	private static Font monospaced(int style, int size) {
		Font consolas = new Font("Consolas", style, size);
		return "Consolas".equals(consolas.getFamily()) ? consolas : new Font(Font.MONOSPACED, style, size);
	}

	private void animate() {
		animationFrame++;
		// floating padlock
		padlock.setOffset(animationFrame % 4 < 2 ? 0 : PIXEL);
	}

	/** Launches one of the registered easter eggs, at random */
	private void launchRandomEasterEgg() {
		List<EasterEgg> eggs = new ArrayList<>();
		try {
			for (EasterEgg egg : MainGuiGeneralAppStarter.easterEggInitializer.manager.getEasterEggs()) {
				if (egg.getAction() != null) {
					eggs.add(egg);
				}
			}
		} catch (Exception e) {
			logger.warn("Easter eggs not available", e);
		}
		if (eggs.isEmpty()) {
			return;
		}
		EasterEgg egg = eggs.get(ThreadLocalRandom.current().nextInt(eggs.size()));
		logger.info("About easter egg launched: " + egg.getId());
		SwingUtilities.invokeLater(egg.getAction());
	}

	private static BufferedImage loadLogo() {
		try {
			return ImageIO.read(new File(ImagesPathConfigurator.IMAGE_BCKG_ABOUT));
		} catch (Exception e) {
			logger.warn("Cannot load about background image", e);
			return null;
		}
	}

	private void addHandlers(JPanel background) {

		// DEFAULT CLOSE OPERATION
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {

				isActive.set(false);
				animation.stop();
				setTopmost(false);

				mainAppFrame.setEnabled(true);
				singletonInstance = null;
				SwingUtilities.invokeLater(() -> mainAppFrame.toFront());

				logger.info(this.getClass().getName() + " Closed");
				dispose();
			}
		});

		websiteButton.addActionListener(e -> SysUtils.openUrlInBrowser("https://" + GeneralConfig.WEBSITE));

		// click on the background: the egg button shows up
		background.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				input.requestFocusInWindow();
				if (!eggButton.isVisible()) {
					eggButton.setVisible(true);
					background.revalidate();
					background.repaint();
				}
			}
		});
		eggButton.addActionListener(e -> launchRandomEasterEgg());
	}

	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(560, 560);
	}

	public static AboutSectionFrame getInstance(MainGUI mainApplication) {
		return singletonInstance == null ? new AboutSectionFrame(mainApplication) : singletonInstance;
	}

	// ------------------------------------------------------------------ PIXEL ART COMPONENTS

	/** Dark background with the pixelated, tinted logo, CRT scanlines and a vignette */
	private static final class BackgroundPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private final BufferedImage pixelLogo;

		BackgroundPanel(BufferedImage logo) {
			setBackground(BACKGROUND);
			pixelLogo = logo == null ? null : tintedLowResolution(logo);
		}

		/** Logo reduced to big blocks: brightness becomes accent color opacity (the source logo is very dark) */
		private static BufferedImage tintedLowResolution(BufferedImage logo) {
			int width = Math.max(1, logo.getWidth() / LOGO_PIXEL);
			int height = Math.max(1, logo.getHeight() / LOGO_PIXEL);
			BufferedImage small = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = small.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(logo, 0, 0, width, height, null);
			g.dispose();

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int rgb = small.getRGB(x, y);
					float brightness = (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF)) / (3f * 255f);
					int alpha = Math.min(150, Math.round(brightness * 4.5f * 255f));
					Color tint = (x + y) % 7 == 0 ? ACCENT_LIGHT : ACCENT;
					small.setRGB(x, y, (alpha << 24) | (tint.getRGB() & 0xFFFFFF));
				}
			}
			return small;
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			Graphics2D g = (Graphics2D) graphics.create();
			int width = getWidth();
			int height = getHeight();

			if (pixelLogo != null) {
				// cover the panel keeping the aspect ratio, with hard pixel edges
				float scale = Math.max(width / (float) pixelLogo.getWidth(), height / (float) pixelLogo.getHeight());
				int logoWidth = Math.round(pixelLogo.getWidth() * scale);
				int logoHeight = Math.round(pixelLogo.getHeight() * scale);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				g.drawImage(pixelLogo, (width - logoWidth) / 2, (height - logoHeight) / 2, logoWidth, logoHeight, null);
			}

			// CRT scanlines
			g.setColor(new Color(0, 0, 0, 55));
			for (int y = 0; y < height; y += 3) {
				g.drawLine(0, y, width, y);
			}

			// vignette
			float radius = Math.max(width, height) * 0.75f;
			g.setPaint(new RadialGradientPaint(width / 2f, height / 2f, radius, new float[] { 0.45f, 1f },
					new Color[] { new Color(0, 0, 0, 0), new Color(0, 0, 0, 190) }));
			g.fillRect(0, 0, width, height);
			g.dispose();
		}
	}

	/** Semi-transparent terminal card with stepped (pixel) corners */
	private static final class CardPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		CardPanel() {
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			Graphics2D g = (Graphics2D) graphics.create();
			fillStepped(g, 0, 0, getWidth(), getHeight(), CARD_BORDER);
			fillStepped(g, 2, 2, getWidth() - 4, getHeight() - 4, CARD);
			g.dispose();
			super.paintComponent(graphics);
		}

		private static void fillStepped(Graphics2D g, int x, int y, int width, int height, Color color) {
			g.setColor(color);
			g.fillRect(x + PIXEL, y, width - 2 * PIXEL, height);
			g.fillRect(x, y + PIXEL, PIXEL, height - 2 * PIXEL);
			g.fillRect(x + width - PIXEL, y + PIXEL, PIXEL, height - 2 * PIXEL);
		}
	}

	/** Text drawn at low resolution without antialiasing, scaled up with nearest neighbor: real pixel art */
	private static final class PixelTitle extends JComponent {
		private static final long serialVersionUID = 1L;
		private final BufferedImage pixels;
		private final BufferedImage shadow;

		PixelTitle(String text) {
			Font font = new Font("SansSerif", Font.BOLD, 13);
			BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
			FontMetrics metrics = probe.getGraphics().getFontMetrics(font);
			int width = metrics.stringWidth(text) + 1;
			int height = metrics.getAscent() + metrics.getDescent();

			BufferedImage mask = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = mask.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			g.setFont(font);
			g.setColor(Color.WHITE);
			g.drawString(text, 0, metrics.getAscent());
			g.dispose();

			pixels = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			shadow = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < height; y++) {
				// arcade gradient: light on top, accent at the bottom
				Color row = blend(ACCENT_LIGHT, ACCENT, y / (float) Math.max(1, height - 1));
				for (int x = 0; x < width; x++) {
					if ((mask.getRGB(x, y) >>> 24) > 0) {
						pixels.setRGB(x, y, row.getRGB());
						shadow.setRGB(x, y, 0xC0000000);
					}
				}
			}
			setPreferredSize(new Dimension((width + 1) * PIXEL, (height + 1) * PIXEL));
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			int width = pixels.getWidth() * PIXEL;
			int height = pixels.getHeight() * PIXEL;
			g.drawImage(shadow, PIXEL, PIXEL, width, height, null);
			g.drawImage(pixels, 0, 0, width, height, null);
			g.dispose();
		}

		private static Color blend(Color from, Color to, float amount) {
			return new Color(
					Math.round(from.getRed() + (to.getRed() - from.getRed()) * amount),
					Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * amount),
					Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * amount));
		}
	}

	/** Small bitmap sprite (S = shackle, X = outline, O = body, # = keyhole) with a floating offset */
	private static final class PixelSprite extends JComponent {
		private static final long serialVersionUID = 1L;
		private final String[] bitmap;
		private int offset;

		PixelSprite(String[] bitmap) {
			this.bitmap = bitmap;
			setPreferredSize(new Dimension(bitmap[0].length() * PIXEL, (bitmap.length + 1) * PIXEL));
		}

		void setOffset(int offset) {
			if (this.offset != offset) {
				this.offset = offset;
				repaint();
			}
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			for (int row = 0; row < bitmap.length; row++) {
				for (int column = 0; column < bitmap[row].length(); column++) {
					Color color = colorOf(bitmap[row].charAt(column));
					if (color != null) {
						graphics.setColor(color);
						graphics.fillRect(column * PIXEL, row * PIXEL + offset, PIXEL, PIXEL);
					}
				}
			}
		}

		private static Color colorOf(char pixel) {
			switch (pixel) {
				case 'S': return MUTED;
				case 'X': return ACCENT_LIGHT;
				case 'O': return ACCENT;
				case '#': return BACKGROUND;
				default: return null;
			}
		}
	}
}
