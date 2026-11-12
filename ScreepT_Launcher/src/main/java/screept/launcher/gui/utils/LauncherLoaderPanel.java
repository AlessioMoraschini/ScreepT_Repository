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
package screept.launcher.gui.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.log4j.Logger;

import resources.GeneralConfig;
import various.common.light.easteregg.cats.CatsPlayground;
import various.common.light.utility.string.StringWorker;

/**
 * Loading panel of the launcher and of the application: the ScreepT image, Feo and Nano playing on the floor with a ball
 * of yarn (the pixel-art cats of the easter egg, with elastic physics), the status text and a smooth animated progress
 * bar. Everything is painted here. The animation does NOT run on the Swing thread: a dedicated thread renders every
 * frame in a back buffer and draws it on the window directly, so Feo and Nano keep moving also while the event
 * thread is busy loading the application. The thread stops when the window closes.
 * The {@link #progressBar} stays as the model of the progress (value, indeterminate) for the callers.
 */
public class LauncherLoaderPanel extends JPanel{
	private static final long serialVersionUID = 2754216601762407439L;

	// mainGUI logger
	static Logger logger = Logger.getLogger(LauncherLoaderPanel.class);

	private static final Color TEAL = new Color(0x2D, 0xD4, 0xBF);
	private static final Color CYAN = new Color(0x38, 0xBD, 0xF8);

	private final Image backgroundImage;
	/** Model of the progress: callers set its value and indeterminate flag; it is not shown as a component */
	public JProgressBar progressBar;
	private final LauncherLoaderFrame parentFrame;
	private final CatsPlayground cats;
	private volatile boolean running = true;
	private final BufferedImage[] buffers = new BufferedImage[2];
	private volatile BufferedImage front;
	private int nextBuffer;
	private final Font textFont = new Font("Segoe UI", Font.BOLD, 12);

	private volatile String statusText = "Loading...";
	private volatile Color accent = TEAL;
	private float shownProgress;
	private float shine;
	private float bounce;

	public AtomicBoolean indeterminated = new AtomicBoolean();

	public LauncherLoaderPanel(Image background, LauncherLoaderFrame parent) {

		logger.info(this.getClass().getName() + " Starting...");

		parentFrame = parent;
		backgroundImage = background;
		setBackground(Color.BLACK);
		setOpaque(true);

		progressBar = new JProgressBar();
		int scale = LauncherLoaderFrame.W >= 500 ? 3 : 2;
		cats = new CatsPlayground(scale);
		cats.setFeoVisible(false); // in the loading window only Nano plays
		cats.setArea(26, Math.max(200, LauncherLoaderFrame.W - 52), Math.max(14, LauncherLoaderFrame.H / 12));

		Thread animation = new Thread(new Runnable() {
			@Override
			public void run() {
				while (running) {
					long start = System.nanoTime();
					try {
						stepAndRender();
					} catch (Throwable t) {
						// a failed frame never stops the loader
					}
					long pause = 33 - (System.nanoTime() - start) / 1000000L;
					if (pause > 0) {
						try {
							Thread.sleep(pause);
						} catch (InterruptedException e) {
							return;
						}
					}
				}
			}
		}, "launcher-loader-animation");
		animation.setDaemon(true);
		animation.start();

		logger.info(this.getClass().getName() + " Started");
	}

	@Override
	public void removeNotify() {
		running = false;
		super.removeNotify();
	}

	/** One animation step and one frame: runs on the animation thread, never on the Swing thread */
	private void stepAndRender() {
		cats.tick();
		float target = progressBar.getValue() / 100f;
		shownProgress += (target - shownProgress) * 0.12f;
		if (Math.abs(target - shownProgress) < 0.002f) {
			shownProgress = target;
		}
		shine = (shine + 0.018f) % 1.6f;
		bounce = (bounce + 0.014f) % 2f;

		int w = getWidth();
		int h = getHeight();
		if (w <= 0 || h <= 0) {
			return;
		}
		BufferedImage buffer = buffers[nextBuffer];
		if (buffer == null || buffer.getWidth() != w || buffer.getHeight() != h) {
			buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			buffers[nextBuffer] = buffer;
		}
		Graphics2D g = buffer.createGraphics();
		try {
			renderFrame(g, w, h);
		} finally {
			g.dispose();
		}
		front = buffer;
		nextBuffer ^= 1;
		// straight on the window: it shows also while the Swing thread is blocked
		Graphics screen = getGraphics();
		if (screen != null) {
			try {
				screen.drawImage(buffer, 0, 0, null);
			} finally {
				screen.dispose();
			}
		}
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g0) {
		super.paintComponent(g0);
		BufferedImage frame = front;
		if (frame != null) {
			g0.drawImage(frame, 0, 0, null);
		} else if (backgroundImage != null) {
			g0.drawImage(backgroundImage, 0, 0, null);
		}
	}

	private void renderFrame(Graphics2D g, int w, int h) {
		g.drawImage(backgroundImage, 0, 0, null);

		// darkens the lower part, so cats and text read well on top of the image
		g.setPaint(new GradientPaint(0, h * 0.62f, new Color(0, 0, 0, 0), 0, h, new Color(8, 10, 14, 235)));
		g.fillRect(0, 0, w, h);

		int barHeight = 10;
		int barX = 26;
		int barWidth = w - 52;
		int barY = h - 30;
		int textBaseline = barY - 12;
		int ground = (int) (h * 0.66); // the cats stand on the picture, above the text

		cats.paint(g, ground);

		// status text and percentage
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(textFont);
		FontMetrics metrics = g.getFontMetrics();
		g.setColor(new Color(0xDC, 0xDF, 0xE4));
		g.drawString(fit(statusText, metrics, barWidth - 60), barX, textBaseline);
		if (!progressBar.isIndeterminate()) {
			String percent = Math.round(shownProgress * 100) + "%";
			g.setColor(new Color(0xA8, 0xAE, 0xB8));
			g.drawString(percent, barX + barWidth - metrics.stringWidth(percent), textBaseline);
		}

		// progress bar, pixel style: dark frame and blocks that light up one by one (no smoothing, no glow)
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		Color frame = new Color(0x12, 0x14, 0x18);
		g.setColor(frame);
		g.fillRect(barX, barY, barWidth, barHeight + 4);
		g.setColor(new Color(0x2A, 0x2E, 0x36));
		g.fillRect(barX + 2, barY + 2, barWidth - 4, barHeight);
		int blockWidth = 8;
		int blockGap = 2;
		int step = blockWidth + blockGap;
		int blocks = Math.max(1, (barWidth - 6) / step);
		Color lit = accent;
		Color litLight = lighter(accent);
		int first;
		int count;
		if (progressBar.isIndeterminate()) {
			count = Math.max(3, blocks / 6);
			float t = bounce <= 1f ? bounce : 2f - bounce;
			first = Math.round((blocks - count) * t);
		} else {
			first = 0;
			count = Math.round(blocks * shownProgress);
		}
		for (int i = first; i < first + count && i < blocks; i++) {
			int bx = barX + 3 + i * step;
			g.setColor(lit);
			g.fillRect(bx, barY + 3, blockWidth, barHeight - 2);
			g.setColor(litLight);
			g.fillRect(bx, barY + 3, blockWidth, 2);
			g.setColor(new Color(0, 0, 0, 70));
			g.fillRect(bx, barY + 2 + barHeight - 2, blockWidth, 2);
		}
		// a bright block runs along the lit part, like a cursor
		if (count > 0) {
			int runner = first + (int) ((shine * 12) % Math.max(1, count));
			if (runner < first + count && runner < blocks) {
				g.setColor(new Color(255, 255, 255, 150));
				g.fillRect(barX + 3 + runner * step, barY + 3, blockWidth, barHeight - 2);
			}
		}
		// thin border of the window
		g.setColor(new Color(255, 255, 255, 40));
		g.drawRect(0, 0, w - 1, h - 1);
	}

	private static Color lighter(Color c) {
		return new Color(Math.min(255, c.getRed() + 50), Math.min(255, c.getGreen() + 50), Math.min(255, c.getBlue() + 50));
	}

	private static String fit(String text, FontMetrics metrics, int maxWidth) {
		if (text == null || metrics.stringWidth(text) <= maxWidth) {
			return text == null ? "" : text;
		}
		String cut = text;
		while (cut.length() > 4 && metrics.stringWidth(cut + "...") > maxWidth) {
			cut = cut.substring(0, cut.length() - 1);
		}
		return cut + "...";
	}

	/** METHODS **/

	public void setProgBarColor(Color target) {
		if (target != null) {
			accent = target;
		}
	}

	public void setText(String txt) {
		statusText = txt == null ? "" : txt;
		repaint();
	}

	public void setProgress(int progress, int msDelay) {
		if (progressBar != null) {
			progressBar.setValue(progress);
			if(progress >= 100){
				try {
					// this is to show last message before loader closes
					if(msDelay > 1)
						Thread.sleep(msDelay);
				} catch (InterruptedException e) {
					logger.warn("Cannot wait at end of loading");
				}
				running = false;
				parentFrame.dispose();
			}
			repaint();
		}
	}

	public int getProgress(){
		return progressBar.getValue();
	}

	public void update(String txt, int progress){
		indeterminated.set(false);
		setText(txt);
		setProgress(progress, 0);
	}

	public void update(String txt, int progress, int msDelayIfEnd){
		try {
			indeterminated.set(false);
			if (progress >= 100) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < 10; i++) {
							String dots = new String(new char[i + 1]).replace("\0", ".");
							String msg = txt + dots;
							setText(msg);
						}
						setProgress(100, 0);
					}
				}).start();
			} else {
				setText(txt);
				setProgress(progress, msDelayIfEnd);
			}
		} catch (Exception e) {
			logger.warn("Error trying to update progressbar", e);
		}

	}

	public void setIndeterminatedProgress(boolean indeterminated, String text) {

		if(indeterminated && !this.indeterminated.get()) {
			this.indeterminated.set(true);
			indeterminatedDotsAnimation(text).start();
			progressBar.setIndeterminate(indeterminated);
		} else {
			this.indeterminated.set(false);
			setText(text);
		}
	}

	public void setIndeterminatedProgress(boolean indeterminated) {

		if(indeterminated && !this.indeterminated.get()) {
			this.indeterminated.set(true);
			indeterminatedDotsAnimation(statusText).start();
			progressBar.setIndeterminate(indeterminated);
		} else {
			this.indeterminated.set(false);
		}
	}

	public Thread indeterminatedDotsAnimation(final String text) {
		return new Thread(new Runnable() {
			@Override
			public void run() {

				final String safeText = StringWorker.trimToEmpty(text);

				while(indeterminated.get()) {
					for (int i = 0; i < 20; i++) {
						String dots = safeText + new String(new char[i + 1]).replace("\0", ".");
						String msg = "Initializing " + GeneralConfig.APPLICATION_NAME + dots;
						setText(msg);
						try {
							Thread.sleep(60);
						} catch (InterruptedException e) {
							logger.error("Error waiting before starting launcher", e);
						}
					}
				}
			}
		});
	}

}
