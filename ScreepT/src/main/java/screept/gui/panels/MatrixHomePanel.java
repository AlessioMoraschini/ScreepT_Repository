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
package screept.gui.panels;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Locale;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;

import screept.gui.panels.home.HomeTerminalView;

/**
 * Home page background as a terminal: a rain of code reveals the home image glyph by glyph
 * (each glyph tinted with the image color below it), then the real image fades in and the
 * rain keeps falling softly. Double click replays the animation.
 */
public class MatrixHomePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final char[] GLYPHS = "01010101010123456789ABCDEF#$%&*+=<>/\\|".toCharArray();
	private static final Font GLYPH_FONT = glyphFont();

	/** About 18 frames per second */
	private static final int FRAME_MS = 55;
	/** Movements were tuned at one frame every 40 ms: what changes at each frame is scaled to keep the same speed */
	private static final float PACE = FRAME_MS / 40f;
	/** Linux queues the drawing requests towards the screen: they are flushed at each frame so they cannot pile up */
	private static final boolean SYNC_SCREEN = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("linux");
	private static final long REVEAL_MS = 5000;
	private static final long FADE_MS = 2500;
	private static final long IDLE_FLICKER_MS = 400;

	private static final float REVEAL_CHANCE = 0.7f;
	private static final float MIN_CELL_BRIGHTNESS = 0.05f;
	private static final float GLYPH_ALPHA_IDLE = 0.3f;

	private static final Color RAIN_HEAD = new Color(0xE8FFFC);
	private static final Color RAIN_BODY = new Color(0x2FB5A8);
	private static final int TRAIL_SHADES = 16;
	private static final int TRAIL_MAX_ALPHA = 210;
	private static final int TRAIL_IDLE_MAX_ALPHA = 110;

	private final Image source;
	private final HomeTerminalView terminal = new HomeTerminalView(this);
	private final Random random = new Random();
	private final Timer timer;
	private final Color[] trailShades = new Color[TRAIL_SHADES];
	private final Color[] idleTrailShades = new Color[TRAIL_SHADES];
	private final Color[] trailBacks = new Color[TRAIL_SHADES];
	private final char[] glyphBuffer = new char[1];

	// grid
	private int cellW, cellH, ascent, cols, rows;
	private Color[] cellColors; // image color per cell, null where the image is dark
	private char[] rainGlyphs;

	// layers
	private BufferedImage fittedImage; // source fitted and centered on black, panel sized
	private BufferedImage glyphLayer; // revealed glyphs, transparent elsewhere
	private BufferedImage frameBase; // fittedImage + glyphLayer, rebuilt only when dirty
	private BufferedImage scene; // the whole frame (base, rain, terminal) at screen resolution, drawn in one shot
	private boolean sceneDirty;
	private Graphics2D glyphGraphics;
	private boolean baseDirty;

	// revealed glyphs
	private boolean[] revealed;
	private int[] revealedCells;
	private int revealedCount;

	// drops, one per column
	private float[] dropRow;
	private float[] dropSpeed;
	private int[] dropTrail;

	// animation state
	private long startNanos;
	private long lastFlickerMs;
	private boolean completed;

	public MatrixHomePanel(Image source) {
		this.source = source;
		setOpaque(true);
		setBackground(Color.BLACK);

		for (int i = 0; i < TRAIL_SHADES; i++) {
			int alpha = TRAIL_MAX_ALPHA * (TRAIL_SHADES - i) / TRAIL_SHADES;
			trailShades[i] = new Color(RAIN_BODY.getRed(), RAIN_BODY.getGreen(), RAIN_BODY.getBlue(), alpha);
			trailBacks[i] = new Color(0, 0, 0, alpha);
			int idleAlpha = TRAIL_IDLE_MAX_ALPHA * (TRAIL_SHADES - i) / TRAIL_SHADES;
			idleTrailShades[i] = new Color(RAIN_BODY.getRed(), RAIN_BODY.getGreen(), RAIN_BODY.getBlue(), idleAlpha);
		}

		timer = new Timer(FRAME_MS, e -> tick());
		timer.setCoalesce(true);

		addMouseWheelListener(e -> terminal.onWheel(e.getWheelRotation()));
		// a page that is not shown does not need the memory of its frame
		addHierarchyListener(e -> {
			if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && !isShowing()) {
				scene = null;
			}
		});

		// easter egg: after 30 seconds on the home page Feo and Nano come to play, then leave together
		various.common.light.easteregg.cats.CatsVisit.attach(this, 10, terminal::promptBounds);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					replay();
				}
			}
		});
	}

	@Override
	public void addNotify() {
		super.addNotify();
		timer.start();
		terminal.attach();
	}

	@Override
	public void removeNotify() {
		timer.stop();
		terminal.detach();
		super.removeNotify();
	}

	public void replay() {
		completed = false;
		startNanos = 0;
		lastFlickerMs = 0;
		fittedImage = null; // forces a rebuild with an empty glyph layer
		repaint();
	}

	/** Consolas when the system has it, else the monospaced font of the system: the grid needs glyphs all of the same width */
	private static Font glyphFont() {
		Font consolas = new Font("Consolas", Font.BOLD, 12);
		return "Consolas".equalsIgnoreCase(consolas.getFamily()) ? consolas : new Font(Font.MONOSPACED, Font.BOLD, 12);
	}

	////////// ANIMATION ////////////////

	private void tick() {
		if (!isShowing() || fittedImage == null) {
			return;
		}
		if (startNanos == 0) {
			startNanos = System.nanoTime();
		}
		long elapsed = elapsedMs();

		for (int c = 0; c < cols; c++) {
			int before = (int) dropRow[c];
			dropRow[c] += dropSpeed[c] * PACE;
			int after = (int) dropRow[c];
			for (int r = Math.max(0, before + 1); r <= after && r < rows; r++) {
				revealCell(r * cols + c, false);
			}
			if (after - dropTrail[c] > rows) {
				resetDrop(c, false);
			}
		}

		// the rain glyphs mutate slowly, like a live terminal
		for (int i = (int) (rainGlyphs.length * PACE / 200); i >= 0; i--) {
			rainGlyphs[random.nextInt(rainGlyphs.length)] = randomGlyph();
		}

		if (!completed) {
			flickerRevealed();
			baseDirty = true; // the fade changes the base at every frame
			if (elapsed >= REVEAL_MS + FADE_MS) {
				completed = true;
				revealAll();
			}
		} else if (elapsed - lastFlickerMs >= IDLE_FLICKER_MS) {
			flickerRevealed();
			lastFlickerMs = elapsed;
		}

		terminal.tick();
		sceneDirty = true;
		if (SYNC_SCREEN) {
			Toolkit.getDefaultToolkit().sync();
		}
		repaint();
	}

	private long elapsedMs() {
		return startNanos == 0 ? 0 : (System.nanoTime() - startNanos) / 1000000L;
	}

	private void resetDrop(int column, boolean initial) {
		dropTrail[column] = 6 + random.nextInt(18);
		if (completed) {
			// idle: fewer, slower and fainter drops (see paintRain)
			dropSpeed[column] = 0.25f + random.nextFloat() * 0.45f;
			dropRow[column] = -random.nextFloat() * rows * 4f;
		} else {
			dropSpeed[column] = 0.6f + random.nextFloat() * 0.6f;
			dropRow[column] = -random.nextFloat() * rows * (initial ? 0.35f : 0.6f);
		}
	}

	private char randomGlyph() {
		return GLYPHS[random.nextInt(GLYPHS.length)];
	}

	////////// GLYPH LAYER ////////////////

	private void revealCell(int index, boolean force) {
		if (cellColors[index] == null || revealed[index] || (!force && random.nextFloat() > REVEAL_CHANCE)) {
			return;
		}
		revealed[index] = true;
		revealedCells[revealedCount++] = index;
		drawRevealedGlyph(index);
	}

	private void revealAll() {
		for (int i = 0; i < cellColors.length; i++) {
			revealCell(i, true);
		}
	}

	private void flickerRevealed() {
		for (int i = (int) (revealedCount * PACE / 150); i > 0; i--) {
			drawRevealedGlyph(revealedCells[random.nextInt(revealedCount)]);
		}
	}

	private void drawRevealedGlyph(int index) {
		int x = (index % cols) * cellW;
		int y = (index / cols) * cellH;
		glyphGraphics.setComposite(AlphaComposite.Clear);
		glyphGraphics.fillRect(x, y, cellW, cellH);
		glyphGraphics.setComposite(AlphaComposite.SrcOver);
		glyphGraphics.setColor(cellColors[index]);
		glyphBuffer[0] = randomGlyph();
		glyphGraphics.drawChars(glyphBuffer, 0, 1, x + 1, y + ascent);
		baseDirty = true;
	}

	////////// LAYOUT ////////////////

	private void rebuild(int width, int height) {
		FontMetrics fm = getFontMetrics(GLYPH_FONT);
		cellW = fm.charWidth('0') + 2;
		ascent = fm.getAscent();
		cellH = ascent + 1;
		cols = (width + cellW - 1) / cellW;
		rows = (height + cellH - 1) / cellH;

		fittedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = fittedImage.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
		int srcW = source == null ? -1 : source.getWidth(null);
		int srcH = source == null ? -1 : source.getHeight(null);
		if (srcW > 0 && srcH > 0) {
			double scale = Math.min((double) width / srcW, (double) height / srcH);
			int w = (int) Math.round(srcW * scale);
			int h = (int) Math.round(srcH * scale);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.drawImage(source, (width - w) / 2, (height - h) / 2, w, h, null);
		}
		g.dispose();

		sampleCellColors(width, height);

		if (glyphGraphics != null) {
			glyphGraphics.dispose();
		}
		glyphLayer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		glyphGraphics = glyphLayer.createGraphics();
		glyphGraphics.setFont(GLYPH_FONT);
		glyphGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		frameBase = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		int cells = cols * rows;
		revealed = new boolean[cells];
		revealedCells = new int[cells];
		revealedCount = 0;
		rainGlyphs = new char[cells];
		for (int i = 0; i < cells; i++) {
			rainGlyphs[i] = randomGlyph();
		}

		dropRow = new float[cols];
		dropSpeed = new float[cols];
		dropTrail = new int[cols];
		for (int c = 0; c < cols; c++) {
			resetDrop(c, true);
		}

		if (completed) {
			revealAll();
		}
		baseDirty = true;
		scene = null;
	}

	/** Averages the fitted image under each cell; dark cells stay null and never get a glyph. */
	private void sampleCellColors(int width, int height) {
		int[] pixels = ((DataBufferInt) fittedImage.getRaster().getDataBuffer()).getData();
		cellColors = new Color[cols * rows];
		float[] hsb = new float[3];

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				int x0 = c * cellW, y0 = r * cellH;
				int x1 = Math.min(x0 + cellW, width), y1 = Math.min(y0 + cellH, height);
				long red = 0, green = 0, blue = 0, count = 0;
				for (int y = y0; y < y1; y += 2) {
					for (int x = x0; x < x1; x += 2) {
						int p = pixels[y * width + x];
						red += (p >> 16) & 0xFF;
						green += (p >> 8) & 0xFF;
						blue += p & 0xFF;
						count++;
					}
				}
				if (count == 0) {
					continue;
				}
				Color.RGBtoHSB((int) (red / count), (int) (green / count), (int) (blue / count), hsb);
				if (hsb[2] >= MIN_CELL_BRIGHTNESS) {
					// boost: thin strokes average to dark tones
					cellColors[r * cols + c] = Color.getHSBColor(hsb[0], hsb[1], Math.min(1f, hsb[2] * 2.2f + 0.15f));
				}
			}
		}
	}

	////////// PAINT ////////////////

	@Override
	protected void paintComponent(Graphics g) {
		int width = getWidth(), height = getHeight();
		if (width <= 0 || height <= 0) {
			return;
		}
		if (fittedImage == null || fittedImage.getWidth() != width || fittedImage.getHeight() != height) {
			rebuild(width, height);
		}
		Graphics2D screen = (Graphics2D) g.create();
		// screen pixels for each unit of the component (HiDPI): the frame is composed at that resolution, nothing gets blurred
		double scale = Math.max(1d, screen.getTransform().getScaleX());
		int frameWidth = (int) Math.ceil(width * scale), frameHeight = (int) Math.ceil(height * scale);
		boolean newScene = scene == null || scene.getWidth() != frameWidth || scene.getHeight() != frameHeight;
		if (newScene) {
			scene = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
		}
		// composed once per frame: the repaints in between (the cats walking above, for instance) only copy it
		if (newScene || sceneDirty) {
			composeScene(screen, scale, width, height);
		}
		screen.scale(1 / scale, 1 / scale);
		screen.drawImage(scene, 0, 0, null);
		screen.dispose();
	}

	private void composeScene(Graphics2D screen, double scale, int width, int height) {
		if (baseDirty) {
			composeBase();
		}
		Graphics2D g = scene.createGraphics();
		g.addRenderingHints(screen.getRenderingHints());
		g.scale(scale, scale);
		g.drawImage(frameBase, 0, 0, null);
		paintRain(g);
		terminal.paint(g, width, height);
		g.dispose();
		sceneDirty = false;
	}

	private void composeBase() {
		float imageAlpha = completed ? 1f
				: Math.max(0f, Math.min(1f, (elapsedMs() - REVEAL_MS) / (float) FADE_MS));
		if (startNanos == 0) {
			imageAlpha = 0f;
		}

		Graphics2D g = frameBase.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, frameBase.getWidth(), frameBase.getHeight());
		if (imageAlpha > 0f) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, imageAlpha));
			g.drawImage(fittedImage, 0, 0, null);
		}
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - (1f - GLYPH_ALPHA_IDLE) * imageAlpha));
		g.drawImage(glyphLayer, 0, 0, null);
		g.dispose();
		baseDirty = false;
	}

	private void paintRain(Graphics2D graphics) {
		Graphics2D g = (Graphics2D) graphics.create();
		g.setFont(GLYPH_FONT);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		// once the image is drawn the rain becomes a faint overlay that does not cover it
		Color[] shades = completed ? idleTrailShades : trailShades;

		for (int c = 0; c < cols; c++) {
			int head = (int) dropRow[c];
			int trail = dropTrail[c];
			int x = c * cellW;
			for (int k = 0; k <= trail; k++) {
				int r = head - k;
				if (r < 0) {
					break;
				}
				if (r >= rows) {
					continue;
				}
				int shade = Math.min(TRAIL_SHADES - 1, k * TRAIL_SHADES / (trail + 1));
				int y = r * cellH;
				if (!completed) {
					// dark backing so the rain glyph does not overlap the revealed one below
					g.setColor(trailBacks[shade]);
					g.fillRect(x, y, cellW, cellH);
				}
				g.setColor(k == 0 && !completed ? RAIN_HEAD : shades[shade]);
				glyphBuffer[0] = rainGlyphs[r * cols + c];
				g.drawChars(glyphBuffer, 0, 1, x + 1, y + ascent);
			}
		}
		g.dispose();
	}
}
