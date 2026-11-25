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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static screept.gui.GuiTestSupport.edt;
import static screept.gui.GuiTestSupport.flushEdt;
import static screept.gui.GuiTestSupport.get;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.concurrent.Callable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import org.ini4j.Wini;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import initializer.configs.impl.INItializer;
import screept.gui.GuiTestSupport;
import screept.gui.GuiTestSupport.Fixture;
import various.common.light.i18n.I18n;
import various.common.light.utility.string.StringWorker.EOL;

/**
 * The real General Options window, built but never shown, bound to a temporary configuration and a temporary launcher
 * ini. The options are live on the configuration; the BACKUP_* values bring them back on reset, discard or close, and
 * Save / Use settings make the current ones the new reference. Not driven: autostart (writes the Windows startup
 * folder), network settings and "open autostart folder" (windows / Explorer), the language list (global state).
 */
public class GeneralOptionsFrameGuiTest {

	@ClassRule
	public static TemporaryFolder classTmp = new TemporaryFolder();

	private static Fixture fx;
	private static GeneralOptionsFrame oldSingleton;
	private static String oldLanguage;
	private static Locale oldLocale;
	private static String oldEncoding;
	private static float oldEffectsVolume;

	private GeneralOptionsFrame frame;

	// ------------------------------------------------------------------ plumbing

	private JTextField userName() throws Exception {
		return (JTextField) get(frame, "textFieldUname");
	}

	private JCheckBox autosave() throws Exception {
		return (JCheckBox) get(frame, "chckbxAutosave");
	}

	private JSpinner interval() throws Exception {
		return (JSpinner) get(frame, "spinnerInterval");
	}

	@SuppressWarnings("unchecked")
	private JComboBox<EOL> eol() throws Exception {
		return (JComboBox<EOL>) get(frame, "comboBoxDefEOL");
	}


	@SuppressWarnings("unchecked")
	private JComboBox<various.common.light.om.SecurityLevel> security() throws Exception {
		return (JComboBox<various.common.light.om.SecurityLevel>) get(frame, "comboSecurityLevels");
	}

	private JSlider volume() throws Exception {
		return (JSlider) get(frame, "sliderVolume");
	}

	private JCheckBox hideLauncher() throws Exception {
		return (JCheckBox) get(frame, "chckbxHideLauncherAt");
	}

	private JButton button(String field) throws Exception {
		return (JButton) get(frame, field);
	}

	private void click(final JButton b) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				b.doClick();
			}
		});
		flushEdt();
	}

	private void run(final Runnable r) throws Exception {
		edt(r);
		flushEdt();
	}

	private static String launcherHide() throws Exception {
		return new Wini(fx.launcherIni).get("APPLICATION_OPTIONS", "flagHideNStartDirect");
	}

	private static String iniValue(String section, String key) throws Exception {
		return new Wini(fx.iniFile).get(section, key);
	}

	// ------------------------------------------------------------------ lifecycle

	@BeforeClass
	public static void build() throws Exception {
		Assume.assumeFalse("needs a display server (windows are never shown)", GraphicsEnvironment.isHeadless());
		oldLanguage = I18n.getLanguage();
		oldLocale = Locale.getDefault();
		oldEncoding = System.getProperty("file.encoding");
		oldEffectsVolume = resources.SoundsConfigurator.DEFAULT_EFFECTS_VOLUME;
		fx = GuiTestSupport.open(classTmp.newFolder("general"));
		oldSingleton = GeneralOptionsFrame.singletonInstance;
	}

	@AfterClass
	public static void restoreGlobals() throws Exception {
		GeneralOptionsFrame.singletonInstance = oldSingleton;
		if (oldLanguage != null) {
			I18n.setLanguage(oldLanguage);
		}
		if (oldLocale != null) {
			Locale.setDefault(oldLocale);
		}
		if (oldEncoding != null) {
			System.setProperty("file.encoding", oldEncoding);
		}
		resources.SoundsConfigurator.DEFAULT_EFFECTS_VOLUME = oldEffectsVolume;
		if (fx != null) {
			fx.close();
		}
	}

	@Before
	public void openWindow() throws Exception {
		Assume.assumeNotNull(fx);
		// known starting configuration, in memory and on disk
		fx.conf.getGenOpt().setUserName("tester");
		fx.conf.getGenOpt().setGeneralVolume(0.5f);
		fx.conf.getGenOpt().setCharset("UTF-8");
		fx.conf.getFileOpt().setFlagAutosave(false);
		fx.conf.getFileOpt().setAutosaveInterval(60000);
		fx.conf.getTextEditorOpt().setEol(EOL.LF);
		fx.conf.getAesOpt().setSecurityLevel(various.common.light.om.SecurityLevel.NO_CHECK);
		fx.conf.getGenOpt().setExceptionAdvicesEnabled(true);
		fx.conf.writeCurrentConfigIniFile();
		Wini launcher = new Wini();
		launcher.put("APPLICATION_OPTIONS", "flagHideNStartDirect", "false");
		launcher.store(fx.launcherIni);
		GeneralOptionsFrame.singletonInstance = null;
		frame = edt(new Callable<GeneralOptionsFrame>() {
			@Override
			public GeneralOptionsFrame call() throws Exception {
				return new GeneralOptionsFrame(fx.conf);
			}
		});
		set(frame, "dialogHelper", fx.dialogs);
		flushEdt();
		fx.dialogs.reset();
	}

	private static void set(Object target, String name, Object value) throws Exception {
		GuiTestSupport.set(target, name, value);
	}

	@After
	public void closeWindow() throws Exception {
		if (frame != null) {
			assertFalse("the options window must stay hidden", frame.isVisible());
			edt(new Runnable() {
				@Override
				public void run() {
					frame.dispose();
				}
			});
		}
	}

	// ------------------------------------------------------------------ tests

	@Test
	public void windowShowsTheCurrentConfiguration() throws Exception {
		assertEquals("tester", userName().getText());
		assertFalse(autosave().isSelected());
		assertEquals(60, interval().getValue());
		assertEquals(EOL.LF, eol().getSelectedItem());
		assertEquals(50, volume().getValue());
		assertFalse("the interval is editable only with autosave on", interval().isEnabled());
	}

	@Test
	public void changesAreLiveOnTheConfiguration() throws Exception {
		run(new Runnable() {
			@Override
			public void run() {
				try {
					userName().setText("alessio");
					autosave().doClick();
					interval().setValue(90);
					eol().setSelectedItem(EOL.CRLF);
					security().setSelectedItem(various.common.light.om.SecurityLevel.values()[various.common.light.om.SecurityLevel.values().length - 1]);
					volume().setValue(80);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
		assertEquals("alessio", fx.conf.getGenOpt().getUserName());
		assertTrue(fx.conf.getFileOpt().isFlagAutosave());
		assertEquals("seconds in the window, milliseconds in the configuration", 90000, fx.conf.getFileOpt().getAutosaveInterval());
		assertEquals(EOL.CRLF, fx.conf.getTextEditorOpt().getEol());
		assertEquals(0.8f, fx.conf.getGenOpt().getGeneralVolume(), 0.0001f);
		assertTrue("the interval follows the autosave switch", interval().isEnabled());
		assertEquals("nothing is written to the file until Save", "tester", iniValue(INItializer.GEN_OPT, "userName"));
	}

	@Test
	public void resetBringsBackTheValuesOfWhenTheWindowOpened() throws Exception {
		run(new Runnable() {
			@Override
			public void run() {
				try {
					userName().setText("someone else");
					autosave().doClick();
					interval().setValue(300);
					eol().setSelectedItem(EOL.CRLF);
					volume().setValue(10);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
		assertEquals("someone else", fx.conf.getGenOpt().getUserName());
		click(button("btnResetValues"));
		assertEquals("tester", fx.conf.getGenOpt().getUserName());
		assertFalse(fx.conf.getFileOpt().isFlagAutosave());
		assertEquals(60000, fx.conf.getFileOpt().getAutosaveInterval());
		assertEquals(EOL.LF, fx.conf.getTextEditorOpt().getEol());
		assertEquals(0.5f, fx.conf.getGenOpt().getGeneralVolume(), 0.0001f);
		// the controls show the restored values again
		assertEquals("tester", userName().getText());
		assertFalse(autosave().isSelected());
		assertEquals(EOL.LF, eol().getSelectedItem());
		assertEquals(50, volume().getValue());
		assertEquals(60, interval().getValue());
		assertSame("the window is still open after a reset", frame, GeneralOptionsFrame.singletonInstance);
	}

	@Test
	public void closingTheWindowWithoutSavingRestoresTheConfiguration() throws Exception {
		run(new Runnable() {
			@Override
			public void run() {
				try {
					userName().setText("temporary");
					volume().setValue(5);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
		run(new Runnable() {
			@Override
			public void run() {
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		assertEquals("tester", fx.conf.getGenOpt().getUserName());
		assertEquals(0.5f, fx.conf.getGenOpt().getGeneralVolume(), 0.0001f);
		assertNull("the window is gone", GeneralOptionsFrame.singletonInstance);
		assertFalse(frame.isActive.get());
		assertEquals("tester", iniValue(INItializer.GEN_OPT, "userName"));
	}

	@Test
	public void openingTheWindowDoesNotWriteTheConfigurationFile() throws Exception {
		fx.conf.getGenOpt().setUserName("memory only");
		GeneralOptionsFrame other = edt(new Callable<GeneralOptionsFrame>() {
			@Override
			public GeneralOptionsFrame call() throws Exception {
				return new GeneralOptionsFrame(fx.conf);
			}
		});
		flushEdt();
		assertEquals("a change that was never saved must not reach the file just by opening the window", "tester",
				iniValue(INItializer.GEN_OPT, "userName"));
		other.dispose();
	}

	@Test
	public void discardButtonRestoresAndCloses() throws Exception {
		run(new Runnable() {
			@Override
			public void run() {
				try {
					userName().setText("discarded");
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
		click(button("btnDiscard"));
		assertEquals("tester", fx.conf.getGenOpt().getUserName());
		assertNull(GeneralOptionsFrame.singletonInstance);
	}

	@Test
	public void saveWritesTheOptionsAndTheyOutliveTheClosing() throws Exception {
		run(new Runnable() {
			@Override
			public void run() {
				try {
					userName().setText("saved name");
					autosave().doClick();
					interval().setValue(120);
					eol().setSelectedItem(EOL.CRLF);
					volume().setValue(30);
					hideLauncher().setSelected(true);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
		fx.dialogs.answers.add(Boolean.TRUE);
		click(button("btnSaveConf"));
		assertEquals("saved name", iniValue(INItializer.GEN_OPT, "userName"));
		assertEquals("true", iniValue(INItializer.FILE_OPT, "flagAutosave"));
		assertEquals("120000", iniValue(INItializer.FILE_OPT, "autosaveInterval"));
		assertEquals("CRLF", iniValue(INItializer.TEXT_EDITOR_OPT, "EOL"));
		assertEquals("true", launcherHide());
		// the window closed after saving, and the closing must not bring the old values back
		assertNull(GeneralOptionsFrame.singletonInstance);
		assertEquals("saved name", fx.conf.getGenOpt().getUserName());
		assertTrue(fx.conf.getFileOpt().isFlagAutosave());
		assertEquals(120000, fx.conf.getFileOpt().getAutosaveInterval());
		assertEquals(EOL.CRLF, fx.conf.getTextEditorOpt().getEol());
		assertEquals(0.3f, fx.conf.getGenOpt().getGeneralVolume(), 0.0001f);
		assertEquals(1, fx.dialogs.infos());
	}

	@Test
	public void saveDeclinedWritesNothingAndKeepsTheWindowOpen() throws Exception {
		run(new Runnable() {
			@Override
			public void run() {
				try {
					userName().setText("not saved");
					hideLauncher().setSelected(true);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
		fx.dialogs.answers.add(Boolean.FALSE);
		click(button("btnSaveConf"));
		assertEquals("tester", iniValue(INItializer.GEN_OPT, "userName"));
		assertEquals("false", launcherHide());
		assertSame(frame, GeneralOptionsFrame.singletonInstance);
		assertEquals("the live value is kept", "not saved", fx.conf.getGenOpt().getUserName());
	}

	@Test
	public void useSettingsKeepsTheValuesWithoutWritingTheConfigurationFile() throws Exception {
		run(new Runnable() {
			@Override
			public void run() {
				try {
					userName().setText("for this session");
					volume().setValue(20);
					hideLauncher().setSelected(true);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
		click(button("btnUseSettings"));
		assertEquals("for this session", fx.conf.getGenOpt().getUserName());
		assertEquals(0.2f, fx.conf.getGenOpt().getGeneralVolume(), 0.0001f);
		assertEquals("the closing of the window does not revert them", "for this session", fx.conf.getGenOpt().getUserName());
		assertEquals("the options file is untouched", "tester", iniValue(INItializer.GEN_OPT, "userName"));
		assertEquals("only the launcher option is written", "true", launcherHide());
		assertNull(GeneralOptionsFrame.singletonInstance);
	}

	/** The window must fit its content: long lists in a combo box (charsets, languages) must not make the content wider than the window */
	@Test
	public void contentIsNotWiderThanTheWindow() throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				try {
					javax.swing.UIManager.setLookAndFeel("gui.commons.guistyle.laf.ScreepTDarkLaf");
					javax.swing.SwingUtilities.updateComponentTreeUI(frame);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
		for (final String lang : new String[] {"en", "it"}) {
			I18n.setLanguage(lang);
			edt(new Runnable() {
				@Override
				public void run() {
					frame.dispose();
					GeneralOptionsFrame.singletonInstance = null;
				}
			});
			frame = edt(new Callable<GeneralOptionsFrame>() {
				@Override
				public GeneralOptionsFrame call() throws Exception {
					return new GeneralOptionsFrame(fx.conf);
				}
			});
			flushEdt();
			final java.awt.Dimension fit = frame.getDefaultDimension();
			final int content = frame.getContentPane().getPreferredSize().width;
			final int minContent = frame.getContentPane().getMinimumSize().width;
			for (String name : new String[] {"comboBoxDefEOL", "comboSecurityLevels", "comboLocale"}) {
				JComboBox<?> combo = (JComboBox<?>) get(frame, name);
			}
			widest(frame.getContentPane(), 0, 900);
			assertTrue(lang + ": the content (" + content + ") must stay narrow, whatever the language", content < com.formdev.flatlaf.util.UIScale.scale(900));
			assertTrue(lang + ": the window (" + fit.width + ") fits a 1536 px screen", fit.width <= com.formdev.flatlaf.util.UIScale.scale(1000));
			edt(new Runnable() {
				@Override
				public void run() {
					frame.setSize(fit);
					frame.getContentPane().setSize(fit.width, fit.height);
					layoutAll(frame.getContentPane());
					java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(fit.width, fit.height, java.awt.image.BufferedImage.TYPE_INT_RGB);
					frame.getContentPane().paint(img.getGraphics());
					try {
						javax.imageio.ImageIO.write(img, "png", new java.io.File("target/genopt-" + lang + ".png"));
					} catch (java.io.IOException e) {
						throw new AssertionError(e);
					}
					for (String name : new String[] {"comboBoxDefEOL", "comboSecurityLevels", "comboLocale"}) {
						try {
							int w = ((JComboBox<?>) get(frame, name)).getWidth();
							assertTrue(lang + " " + name + " width " + w + " must not fill the whole window", w <= com.formdev.flatlaf.util.UIScale.scale(600));
						} catch (Exception e) {
							throw new AssertionError(e);
						}
					}
				}
			});
		}
	}

	private static void widest(java.awt.Container c, int depth, int limit) {
		for (java.awt.Component k : c.getComponents()) {
			if (k.getPreferredSize().width > limit)
				System.out.println("GENOPT wide " + depth + " " + k.getClass().getSimpleName() + " " + k.getPreferredSize().width + (k instanceof javax.swing.AbstractButton ? " " + ((javax.swing.AbstractButton) k).getText() : ""));
			if (k instanceof java.awt.Container)
				widest((java.awt.Container) k, depth + 1, limit);
		}
	}

	private static void layoutAll(java.awt.Container c) {
		c.doLayout();
		for (java.awt.Component k : c.getComponents()) {
			if (k instanceof java.awt.Container)
				layoutAll((java.awt.Container) k);
		}
	}
}
