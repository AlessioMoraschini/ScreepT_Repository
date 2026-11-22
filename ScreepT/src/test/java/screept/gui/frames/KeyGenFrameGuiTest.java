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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static screept.gui.GuiTestSupport.edt;
import static screept.gui.GuiTestSupport.flushEdt;
import static screept.gui.GuiTestSupport.get;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import screept.gui.GuiTestSupport;
import screept.gui.GuiTestSupport.Fixture;
import various.common.light.om.KeyGenImageType;

/**
 * The real key image generator window, built but never shown. Its size controls are driven (spinner, slider, image type)
 * and the Generate button is clicked for each image type; the image that appears in the window is checked. Export and
 * "add to dictionary" open a file chooser / write in the application folder, so they are not driven.
 */
public class KeyGenFrameGuiTest {

	@ClassRule
	public static TemporaryFolder classTmp = new TemporaryFolder();

	private static Fixture fx;
	private static KeyGenFrame frame;
	private static KeyGenFrame oldSingleton;

	private static JSpinner width() throws Exception {
		return (JSpinner) get(frame, "widthValGUI");
	}

	private static JSpinner height() throws Exception {
		return (JSpinner) get(frame, "heightValGUI");
	}

	private static JSlider sliderW() throws Exception {
		return (JSlider) get(frame, "sliderWidth");
	}

	private static JSlider sliderH() throws Exception {
		return (JSlider) get(frame, "sliderHeight");
	}

	@SuppressWarnings("unchecked")
	private static JComboBox<KeyGenImageType> type() throws Exception {
		return (JComboBox<KeyGenImageType>) get(frame, "comboImgType");
	}

	private static String allowText() throws Exception {
		return ((JLabel) get(frame, "lblThisSettingsAllow")).getText();
	}

	private static String sizeText() throws Exception {
		return ((JLabel) get(frame, "lblFinalSize")).getText();
	}

	@BeforeClass
	public static void build() throws Exception {
		Assume.assumeFalse("needs a display server (windows are never shown)", GraphicsEnvironment.isHeadless());
		fx = GuiTestSupport.open(classTmp.newFolder("keygen"));
		oldSingleton = KeyGenFrame.singletonInstance;
		frame = edt(new Callable<KeyGenFrame>() {
			@Override
			public KeyGenFrame call() throws Exception {
				return new KeyGenFrame(fx.conf);
			}
		});
		flushEdt();
	}

	@AfterClass
	public static void dispose() throws Exception {
		if (frame != null) {
			edt(new Runnable() {
				@Override
				public void run() {
					frame.dispose();
				}
			});
			KeyGenFrame.singletonInstance = oldSingleton;
		}
		if (fx != null) {
			fx.close();
		}
	}

	@Before
	public void reset() throws Exception {
		Assume.assumeNotNull(frame);
		edt(new Runnable() {
			@Override
			public void run() {
				try {
					type().setSelectedItem(KeyGenImageType.TOP_SECURITY);
					width().setValue(1000);
					height().setValue(1000);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
		fx.dialogs.reset();
	}

	@After
	public void noWindowWasShown() {
		if (frame != null) {
			assertFalse("the generator window must stay hidden", frame.isVisible());
		}
	}

	private static void setSizes(final int w, final int h) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				try {
					width().setValue(w);
					height().setValue(h);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
	}

	@Test
	public void windowOffersTheConfiguredSizeAndAllImageTypes() throws Exception {
		assertEquals(KeyGenImageType.TOP_SECURITY, type().getSelectedItem());
		assertEquals(KeyGenImageType.values().length, type().getItemCount());
		assertEquals(100, ((Number) ((javax.swing.SpinnerNumberModel) width().getModel()).getMinimum()).intValue());
		assertTrue(frame.getBtnGenerateKey().isEnabled());
		assertFalse("nothing to stop yet", frame.getBtnStop().isEnabled());
	}

	@Test
	public void spinnerAndSliderStayInSyncAndUpdateTheConfiguration() throws Exception {
		setSizes(250, 400);
		assertEquals("spinner moves the slider", 250, sliderW().getValue());
		assertEquals(400, sliderH().getValue());
		assertEquals("and the default size of the configuration", 250, fx.conf.getSteganOpt().getDefaultWidth());
		assertEquals(400, fx.conf.getSteganOpt().getDefaultHeight());

		edt(new Runnable() {
			@Override
			public void run() {
				try {
					sliderW().setValue(640);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
		assertEquals("slider moves the spinner", 640, width().getValue());
		assertEquals(640, fx.conf.getSteganOpt().getDefaultWidth());
	}

	@Test
	public void sizePredictionFollowsSizeAndImageType() throws Exception {
		setSizes(300, 200);
		int mult = KeyGenImageType.TOP_SECURITY.getMultiplicator();
		assertTrue("data allowed: " + allowText(), allowText().contains(String.valueOf(mult * 300 * 200 / 1024)));
		assertTrue("file size: " + sizeText(), sizeText().contains(String.valueOf(mult * 300 * 200 / 1024)));

		edt(new Runnable() {
			@Override
			public void run() {
				try {
					type().setSelectedItem(KeyGenImageType.LOW_SECURITY);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
		int low = KeyGenImageType.LOW_SECURITY.getMultiplicator();
		assertTrue("a lighter image type allows less data: " + allowText(), allowText().contains(String.valueOf(low * 300 * 200 / 1024)));
		assertFalse(allowText().contains(String.valueOf(mult * 300 * 200 / 1024)));
	}

	private static BufferedImage generate(KeyGenImageType t, int w, int h) throws Exception {
		setSizes(w, h);
		final KeyGenImageType chosen = t;
		edt(new Runnable() {
			@Override
			public void run() {
				try {
					type().setSelectedItem(chosen);
					frame.getBtnGenerateKey().doClick();
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
		long end = System.currentTimeMillis() + 15000;
		while (System.currentTimeMillis() < end) {
			if (frame.getImageContainer() != null && frame.getImageContainer().getImage() != null) {
				break;
			}
			Thread.sleep(20);
		}
		flushEdt();
		assertNotNull("the image was generated in time", frame.getImageContainer());
		assertNotNull("the image was generated in time", frame.getImageContainer().getImage());
		return frame.getImageContainer().getImage();
	}

	@Test
	public void generateButtonCreatesAnImageOfTheChosenSizeAndType() throws Exception {
		for (KeyGenImageType t : KeyGenImageType.values()) {
			BufferedImage img = generate(t, 120, 100);
			assertEquals(t + " width", 120, img.getWidth());
			assertEquals(t + " height", 100, img.getHeight());
			assertEquals(t + " type", t.getType(), img.getType());
			Set<Integer> distinct = new HashSet<Integer>();
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					distinct.add(img.getRGB(x, y));
				}
			}
			assertTrue(t + " is random, not a flat colour: " + distinct.size(), distinct.size() > 1000);
			// after the work the window is ready for another one
			assertTrue(frame.getBtnGenerateKey().isEnabled());
			assertTrue(frame.getBtnExport().isEnabled());
			assertTrue(frame.getBtnAddInDictionary().isEnabled());
			assertFalse(frame.getBtnStop().isEnabled());
			assertEquals(100, frame.getProgressBar().getValue());
			assertTrue("the log tells the work was done", frame.getTextLog().getText().contains("120 x 100"));
		}
	}

	@Test
	public void twoGenerationsNeverGiveTheSameKey() throws Exception {
		BufferedImage a = generate(KeyGenImageType.MID_SECURITY, 100, 100);
		BufferedImage b = generate(KeyGenImageType.MID_SECURITY, 100, 100);
		int same = 0;
		for (int y = 0; y < 100; y++) {
			for (int x = 0; x < 100; x++) {
				if (a.getRGB(x, y) == b.getRGB(x, y)) {
					same++;
				}
			}
		}
		assertTrue("two keys must not coincide (" + same + " equal pixels of 10000)", same < 100);
	}
}
