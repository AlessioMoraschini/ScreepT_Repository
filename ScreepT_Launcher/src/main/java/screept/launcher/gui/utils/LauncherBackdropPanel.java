package screept.launcher.gui.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.LayoutManager;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.apache.log4j.Logger;

import resources.ImagesPathConfigurator;

/**
 * Animated pixel art background of the launcher, in the style of the ScreepT home page: columns of code rain fall
 * and, where they pass, draw the ScreepT icon pixel by pixel; when the icon is complete the rain calms down and a
 * soft light sweeps over it now and then. Everything is drawn on a 4 px grid with the teal palette of the home page.
 * The animation runs only while the window is visible.
 */
public class LauncherBackdropPanel extends JPanel {

	private static final long serialVersionUID = 4120874312673485612L;
	private static final Logger logger = Logger.getLogger(LauncherBackdropPanel.class);

	/** Size of one "pixel" of the art */
	private static final int CELL = 4;
	private static final int FRAME_MS = 40;

	private static final Color RAIN_HEAD = new Color(0xE8FFFC);
	private static final Color RAIN_BODY = new Color(0x2FB5A8);
	private static final int TRAIL_SHADES = 18;
	private static final int TRAIL_MAX_ALPHA = 215;
	private static final int TRAIL_IDLE_MAX_ALPHA = 80;
	private static final float REVEAL_CHANCE = 0.65f;
	private static final float REVEAL_STEP = 0.14f;
	private static final float MIN_CELL_BRIGHTNESS = 0.07f;
	private static final long SHINE_EVERY_MS = 6500;
	private static final int SHINE_FRAMES = 36;

	private final Color[] trailActive = new Color[TRAIL_SHADES];
	private final Color[] trailIdle = new Color[TRAIL_SHADES];
	private final Random random = new Random();
	private final Timer timer;

	/** space reserved for the header and the controls: the icon is centered in what remains */
	private final int reservedTop;
	private final int reservedBottom;

	// grid
	private int cols;
	private int rows;
	private float[] head;
	private float[] speed;
	private int[] length;
	private int[] wait;

	// icon (pixel art)
	private BufferedImage logo;
	private int logoW;
	private int logoH;
	private int logoCol0;
	private int logoRow0;
	private int[] logoRgb;
	private boolean[] inLogo;
	private float[] progress;
	private int logoCells;
	private int revealedCells;
	private boolean complete;
	private long lastShineAt;
	private int shineFrame = -1;
	private long frame;

	private BufferedImage base;

	public LauncherBackdropPanel(int reservedTop, int reservedBottom) {
		super(null);
		this.reservedTop = reservedTop;
		this.reservedBottom = reservedBottom;
		setOpaque(true);

		for (int i = 0; i < TRAIL_SHADES; i++) {
			float fade = 1f - (float) i / TRAIL_SHADES;
			trailActive[i] = new Color(RAIN_BODY.getRed(), RAIN_BODY.getGreen(), RAIN_BODY.getBlue(), Math.round(TRAIL_MAX_ALPHA * fade * fade));
			trailIdle[i] = new Color(RAIN_BODY.getRed(), RAIN_BODY.getGreen(), RAIN_BODY.getBlue(), Math.round(TRAIL_IDLE_MAX_ALPHA * fade * fade));
		}

		loadLogo();
		timer = new Timer(FRAME_MS, e -> tick());
	}

	@Override
	public void setLayout(LayoutManager mgr) {
		super.setLayout(mgr);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		timer.start();
	}

	@Override
	public void removeNotify() {
		timer.stop();
		super.removeNotify();
	}

	// ------------------------------------------------------------------ setup

	private void loadLogo() {
		try {
			File file = new File(ImagesPathConfigurator.MAIN_APP_ICON);
			if (file.isFile()) {
				logo = ImageIO.read(file);
			}
		} catch (Exception e) {
			logger.debug("Launcher backdrop: icon not available, only the rain is shown");
		}
		if (logo == null) {
			return;
		}
		logoW = logo.getWidth();
		logoH = logo.getHeight();
		logoRgb = new int[logoW * logoH];
		inLogo = new boolean[logoW * logoH];
		progress = new float[logoW * logoH];
		for (int y = 0; y < logoH; y++) {
			for (int x = 0; x < logoW; x++) {
				int argb = logo.getRGB(x, y);
				int alpha = (argb >>> 24) & 0xFF;
				float[] hsb = Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, null);
				boolean visible = alpha > 40 && hsb[2] >= MIN_CELL_BRIGHTNESS;
				inLogo[y * logoW + x] = visible;
				if (visible) {
					logoCells++;
					// pixel art look: a little more vivid than the original
					logoRgb[y * logoW + x] = Color.HSBtoRGB(hsb[0], Math.min(1f, hsb[1] * 1.1f), Math.min(1f, hsb[2] * 1.25f + 0.05f));
				}
			}
		}
	}

	private void initGrid(int width, int height) {
		cols = Math.max(1, width / CELL);
		rows = Math.max(1, height / CELL);
		head = new float[cols];
		speed = new float[cols];
		length = new int[cols];
		wait = new int[cols];
		for (int c = 0; c < cols; c++) {
			restart(c, true);
		}
		int stageHeight = height - reservedTop - reservedBottom;
		logoCol0 = (cols - logoW) / 2;
		logoRow0 = Math.max(0, (reservedTop + (stageHeight - logoH * CELL) / 2) / CELL);
		base = null;
		revealedCells = 0;
		complete = false;
		if (progress != null) {
			java.util.Arrays.fill(progress, 0f);
		}
	}

	private void restart(int c, boolean first) {
		head[c] = -random.nextInt(rows + 10);
		speed[c] = 0.35f + random.nextFloat() * 0.85f;
		length[c] = 8 + random.nextInt(16);
		wait[c] = first ? random.nextInt(60) : random.nextInt(complete ? 160 : 40);
	}

	// ------------------------------------------------------------------ animation

	private void tick() {
		if (!isShowing() || head == null) {
			return;
		}
		frame++;
		for (int c = 0; c < cols; c++) {
			if (wait[c] > 0) {
				wait[c]--;
				continue;
			}
			head[c] += speed[c];
			int hy = (int) head[c];

			if (logo != null && !complete) {
				int lx = c - logoCol0;
				int ly = hy - logoRow0;
				if (lx >= 0 && lx < logoW && ly >= 0 && ly < logoH) {
					int index = ly * logoW + lx;
					if (inLogo[index] && progress[index] == 0f && random.nextFloat() < REVEAL_CHANCE) {
						progress[index] = 0.0001f;
					}
				}
			}

			if (hy - length[c] > rows) {
				restart(c, false);
			}
		}

		if (logo != null) {
			int done = 0;
			for (int i = 0; i < progress.length; i++) {
				if (progress[i] > 0f && progress[i] < 1f) {
					progress[i] = Math.min(1f, progress[i] + REVEAL_STEP);
				}
				if (progress[i] >= 1f) {
					done++;
				}
			}
			revealedCells = done;
			if (!complete && logoCells > 0 && done >= logoCells * 0.97f) {
				complete = true;
				lastShineAt = System.currentTimeMillis();
				// reveal what the rain has not touched yet
				for (int i = 0; i < progress.length; i++) {
					if (inLogo[i] && progress[i] < 1f) {
						progress[i] = 1f;
					}
				}
			}
			if (complete) {
				long now = System.currentTimeMillis();
				if (shineFrame < 0 && now - lastShineAt > SHINE_EVERY_MS) {
					shineFrame = 0;
				} else if (shineFrame >= 0) {
					shineFrame++;
					if (shineFrame > SHINE_FRAMES) {
						shineFrame = -1;
						lastShineAt = now;
					}
				}
			}
		}
		repaint();
	}

	// ------------------------------------------------------------------ painting

	@Override
	protected void paintComponent(Graphics g0) {
		int w = getWidth();
		int h = getHeight();
		if (w <= 0 || h <= 0) {
			return;
		}
		if (head == null || cols != w / CELL || rows != h / CELL) {
			initGrid(w, h);
		}
		if (base == null || base.getWidth() != w || base.getHeight() != h) {
			base = renderBase(w, h);
		}

		Graphics2D g = (Graphics2D) g0.create();
		try {
			g.drawImage(base, 0, 0, null);
			paintLogo(g);
			paintRain(g);
			paintScanlines(g, w, h);
		} finally {
			g.dispose();
		}
	}

	private BufferedImage renderBase(int w, int h) {
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setPaint(new GradientPaint(0, 0, new Color(0x070B13), 0, h, new Color(0x0F1B2D)));
		g.fillRect(0, 0, w, h);
		// soft light behind the icon, vignette on the edges
		float cx = w / 2f;
		float cy = reservedTop + (h - reservedTop - reservedBottom) / 2f;
		g.setPaint(new RadialGradientPaint(cx, cy, Math.max(w, h) * 0.55f, new float[] {0f, 1f},
				new Color[] {new Color(0x1B6F6A + 0x00000000), new Color(0x070B13)}));
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.38f));
		g.fillRect(0, 0, w, h);
		g.dispose();
		return image;
	}

	private void paintLogo(Graphics2D g) {
		if (logo == null) {
			return;
		}
		double pulse = 0.93 + 0.07 * Math.sin(frame / 14.0);
		for (int ly = 0; ly < logoH; ly++) {
			for (int lx = 0; lx < logoW; lx++) {
				int index = ly * logoW + lx;
				float p = progress[index];
				if (!inLogo[index] || p <= 0f) {
					continue;
				}
				int rgb = logoRgb[index];
				int r = (rgb >> 16) & 0xFF;
				int gr = (rgb >> 8) & 0xFF;
				int b = rgb & 0xFF;

				if (p < 1f) {
					// just hit by the rain: flash from the head color to the real one
					float t = p;
					r = Math.round(RAIN_HEAD.getRed() * (1 - t) + r * t);
					gr = Math.round(RAIN_HEAD.getGreen() * (1 - t) + gr * t);
					b = Math.round(RAIN_HEAD.getBlue() * (1 - t) + b * t);
				} else if (complete) {
					r = (int) Math.min(255, r * pulse);
					gr = (int) Math.min(255, gr * pulse);
					b = (int) Math.min(255, b * pulse);
					if (shineFrame >= 0) {
						// diagonal light sweep
						double position = (double) shineFrame / SHINE_FRAMES * (logoW + logoH + 24) - 12;
						double distance = Math.abs((lx + ly) - position);
						if (distance < 7) {
							float boost = (float) (1 - distance / 7) * 0.55f;
							r = Math.min(255, Math.round(r + (255 - r) * boost));
							gr = Math.min(255, Math.round(gr + (255 - gr) * boost));
							b = Math.min(255, Math.round(b + (255 - b) * boost));
						}
					}
				}
				g.setColor(new Color(r, gr, b));
				g.fillRect((logoCol0 + lx) * CELL, (logoRow0 + ly) * CELL, CELL, CELL);
			}
		}
	}

	private void paintRain(Graphics2D g) {
		Color[] shades = complete ? trailIdle : trailActive;
		for (int c = 0; c < cols; c++) {
			if (wait[c] > 0) {
				continue;
			}
			int hy = (int) head[c];
			for (int k = 0; k < length[c]; k++) {
				int y = hy - k;
				if (y < 0 || y >= rows) {
					continue;
				}
				if (complete && k == 0) {
					g.setColor(new Color(RAIN_HEAD.getRed(), RAIN_HEAD.getGreen(), RAIN_HEAD.getBlue(), 120));
				} else if (k == 0) {
					g.setColor(RAIN_HEAD);
				} else {
					g.setColor(shades[Math.min(TRAIL_SHADES - 1, k * TRAIL_SHADES / length[c])]);
				}
				// a thin gap between the pixels gives the grid look
				g.fillRect(c * CELL, y * CELL, CELL - 1, CELL - 1);
			}
		}
	}

	private void paintScanlines(Graphics2D g, int w, int h) {
		g.setColor(new Color(0, 0, 0, 34));
		for (int y = 0; y < h; y += CELL) {
			g.fillRect(0, y + CELL - 1, w, 1);
		}
	}

	// ------------------------------------------------------------------ translucent panel for the controls

	/** Dark glass panel over the animation (keeps buttons and texts readable) */
	public static class Glass extends JPanel {

		private static final long serialVersionUID = 3481709155182340977L;

		public Glass(LayoutManager layout) {
			super(layout);
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g0) {
			Graphics2D g = (Graphics2D) g0.create();
			try {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(new Color(8, 12, 20, 205));
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(new Color(RAIN_BODY.getRed(), RAIN_BODY.getGreen(), RAIN_BODY.getBlue(), 110));
				g.fillRect(0, 0, getWidth(), 1);
			} finally {
				g.dispose();
			}
		}
	}
}
