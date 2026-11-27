/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
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
package screept.gui.panels.subpanels;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import gui.commons.frameutils.frame.arch.ToolStyle;
import initializer.configs.impl.INItializer;
import screept.gui.frames.KeyGenFrame;
import screept.gui.utils.ImageWorkerScreepT;
import various.common.light.i18n.I18n;
import various.common.light.om.KeyGenImageType;

/**
 * Preview of a key image. The image is generated in background, showing the progress in the progress bar of the
 * key generator. It can be stopped: then no image is produced at all (the preview stays empty, export and dictionary
 * have nothing to save). The image is drawn scaled to fit this panel, proportions kept.
 */
public class ImageGenContainerPanel extends JPanel {
	private static final long serialVersionUID = 3880705375830247529L;

	// ImageGenContainerPanel logger
	static Logger logger = Logger.getLogger(ImageGenContainerPanel.class);

	// null until the generation ends successfully
	private volatile BufferedImage image;

	// set by the user with the stop button
	private final AtomicBoolean cancel = new AtomicBoolean(false);

	// elements of gui taken from the main key generator panel
	private final JProgressBar bar;
	private final JTextArea jtext;
	private final JButton btnGen;
	private final JButton btnAddDict;
	private final JButton btnExp;
	private final JButton btnStop;
	private final int width;
	private final int height;

	// constructor that starts the creation of a random img, shown here when ready
	public ImageGenContainerPanel(KeyGenImageType imgEnumType, int w, int h, KeyGenFrame thisFrame, INItializer currentConf) {

		logger.info(this.getClass().getName() + " Starting...");

		width = w;
		height = h;
		setOpaque(false);

		jtext = thisFrame.getTextLog();
		btnAddDict = thisFrame.getBtnAddInDictionary();
		btnExp = thisFrame.getBtnExport();
		btnGen = thisFrame.getBtnGenerateKey();
		btnStop = thisFrame.getBtnStop();
		bar = thisFrame.getProgressBar();

		btnStop.setEnabled(true);

		Thread starterAsync = new Thread(new Runnable() {
			@Override
			public void run() {
				generate(imgEnumType);
			}
		}, "key-image-generation");
		starterAsync.start();

		logger.info(this.getClass().getName() + " Started");
	}

	private void generate(KeyGenImageType imgEnumType) {
		try {
			log("\n -> Creation of random " + width + " x " + height + " image");
			showProgress(0, false);

			BufferedImage generated = ImageWorkerScreepT.generateKeyImage(width, height, imgEnumType.getType(), new ProgressSink(), cancel);

			if (generated == null) {
				// stopped by the user: as if the image was never generated
				log(" -> Stopped: no image generated");
				logger.debug("Key generation stopped by the user");
			} else {
				image = generated;
				log(" -> Finished!");
				logger.debug("Generated Image of " + width + "Px X " + height + "Px");
			}
		} catch (Throwable e) {
			logger.error("There was an exception/error!", e);
			log(" -> Error while generating the image");
		} finally {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					bar.setIndeterminate(false);
					bar.setValue(image != null ? 100 : 0);
					btnStop.setEnabled(false);
					btnGen.setEnabled(true);
					btnExp.setEnabled(true);
					btnAddDict.setEnabled(true);
					repaint();
				}
			});
		}
	}

	/** Stops the generation: the image is discarded */
	public void cancelGeneration() {
		cancel.set(true);
		btnStop.setEnabled(false);
		log(" -> Stop requested...");
	}

	/** Receives the progress of the generator (called from the generation thread) */
	private class ProgressSink implements java.util.function.IntConsumer {
		private int last = -1;

		@Override
		public void accept(int percent) {
			if (percent != last) {
				last = percent;
				showProgress(percent, false);
			}
		}
	}

	private void showProgress(final int percent, final boolean indeterminate) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				bar.setIndeterminate(indeterminate);
				bar.setValue(percent);
			}
		});
	}

	/** Appends a line to the log of the key generator (from any thread) */
	private void log(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				jtext.append(text);
				jtext.setCaretPosition(jtext.getDocument().getLength());
			}
		});
	}

	@Override
	protected void paintComponent(Graphics g0) {
		super.paintComponent(g0);
		BufferedImage img = image;
		Graphics2D g = (Graphics2D) g0.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			if (img == null) {
				g.setColor(ToolStyle.muted());
				g.setFont(ToolStyle.font(0f, Font.PLAIN));
				String hint = I18n.t("keygen.generate_key");
				int x = (getWidth() - g.getFontMetrics().stringWidth(hint)) / 2;
				g.drawString(hint, Math.max(0, x), getHeight() / 2);
				return;
			}
			double scale = Math.min(getWidth() / (double) img.getWidth(), getHeight() / (double) img.getHeight());
			if (scale <= 0) {
				return;
			}
			int dw = (int) Math.round(img.getWidth() * scale);
			int dh = (int) Math.round(img.getHeight() * scale);
			g.drawImage(img, (getWidth() - dw) / 2, (getHeight() - dh) / 2, dw, dh, null);
		} finally {
			g.dispose();
		}
	}

	public BufferedImage getImage() {
		return image;
	}

}
