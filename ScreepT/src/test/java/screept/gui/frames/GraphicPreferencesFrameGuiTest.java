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
package screept.gui.frames;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static screept.gui.GuiTestSupport.edt;
import static screept.gui.GuiTestSupport.flushEdt;
import static screept.gui.GuiTestSupport.get;
import static screept.gui.GuiTestSupport.set;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;

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

/**
 * The real Graphic Preferences window, built but never shown. The options are live on the configuration and the BACKUP_*
 * values bring them back on reset, discard or close. Every text area switch of the window is clicked and the whole
 * configuration is compared with what it was, so an option that the reset forgets shows up. The look and feel choices
 * are not driven here (they change the look of every window).
 */
public class GraphicPreferencesFrameGuiTest {

	@ClassRule
	public static TemporaryFolder classTmp = new TemporaryFolder();

	private static Fixture fx;
	private static GraphicPreferencesFrame oldSingleton;

	private GraphicPreferencesFrame frame;

	// ------------------------------------------------------------------ plumbing

	/** Everything the configuration would write to its file, as sorted lines (the window position moves by design) */
	private static TreeMap<String, String> dump() throws Exception {
		fx.conf.writeCurrentConfigIniFile();
		Wini ini = new Wini(fx.iniFile);
		TreeMap<String, String> all = new TreeMap<String, String>();
		for (String section : ini.keySet()) {
			for (String key : ini.get(section).keySet()) {
				if (!key.startsWith("lastKnownPosition")) {
					all.put(section + "." + key, ini.get(section, key));
				}
			}
		}
		return all;
	}

	private List<Field> switches() {
		List<Field> found = new ArrayList<Field>();
		for (Field f : GraphicPreferencesFrame.class.getDeclaredFields()) {
			if (JCheckBox.class.isAssignableFrom(f.getType()) && !Modifier.isStatic(f.getModifiers())
					&& !f.getName().equals("chckbxDarklight")) {
				found.add(f);
			}
		}
		return found;
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

	private JButton button(String field) throws Exception {
		return (JButton) get(frame, field);
	}

	// ------------------------------------------------------------------ lifecycle

	@BeforeClass
	public static void build() throws Exception {
		Assume.assumeFalse("needs a display server (windows are never shown)", GraphicsEnvironment.isHeadless());
		fx = GuiTestSupport.open(classTmp.newFolder("graphic"));
		oldSingleton = GraphicPreferencesFrame.singletonInstance;
	}

	@AfterClass
	public static void restore() throws Exception {
		GraphicPreferencesFrame.singletonInstance = oldSingleton;
		if (fx != null) {
			fx.close();
		}
	}

	@Before
	public void openWindow() throws Exception {
		Assume.assumeNotNull(fx);
		fx.dialogs.reset();
		fx.conf.writeCurrentConfigIniFile();
		GraphicPreferencesFrame.singletonInstance = null;
		frame = edt(new Callable<GraphicPreferencesFrame>() {
			@Override
			public GraphicPreferencesFrame call() throws Exception {
				return new GraphicPreferencesFrame(fx.conf);
			}
		});
		set(frame, "dialogHelper", fx.dialogs);
		flushEdt();
	}

	@After
	public void closeWindow() throws Exception {
		if (frame != null) {
			assertFalse("the preferences window must stay hidden", frame.isVisible());
			edt(new Runnable() {
				@Override
				public void run() {
					frame.dispose();
				}
			});
			flushEdt();
		}
	}

	// ------------------------------------------------------------------ tests

	@Test
	public void windowHasTheTextAreaSwitches() throws Exception {
		assertTrue("the text area options are in the window: " + switches().size(), switches().size() >= 20);
		for (Field f : switches()) {
			f.setAccessible(true);
			assertTrue(f.getName() + " exists", f.get(frame) != null);
		}
	}

	@Test
	public void everySwitchChangesTheConfigurationAndResetBringsItAllBack() throws Exception {
		TreeMap<String, String> original = dump();
		int effective = 0;
		for (final Field f : switches()) {
			f.setAccessible(true);
			final JCheckBox box = (JCheckBox) f.get(frame);
			TreeMap<String, String> before = dump();
			edt(new Runnable() {
				@Override
				public void run() {
					box.doClick();
				}
			});
			flushEdt();
			if (!dump().equals(before)) {
				effective++;
			}
		}
		assertTrue("most switches change the configuration at once: " + effective, effective >= 15);
		assertFalse("something was changed", dump().equals(original));
		click(button("btnResetValues"));
		assertEquals("reset brings back every option the window changed", original, dump());
	}

	@Test
	public void discardRestoresEverythingAndClosesTheWindow() throws Exception {
		TreeMap<String, String> original = dump();
		for (Field f : switches()) {
			f.setAccessible(true);
			final JCheckBox box = (JCheckBox) f.get(frame);
			edt(new Runnable() {
				@Override
				public void run() {
					box.doClick();
				}
			});
		}
		flushEdt();
		assertFalse(dump().equals(original));
		click(button("btnDiscard"));
		assertEquals(original, dump());
		assertFalse("the window knows it is closed", frame.isActive.get());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void encodingChoiceAndDetectionSwitchAreLiveAndRestored() throws Exception {
		final javax.swing.JComboBox<String> encoding = (javax.swing.JComboBox<String>) get(frame, "comboEncoding");
		final JCheckBox detect = (JCheckBox) get(frame, "chckbxAutoEncoding");
		final String charsetBefore = fx.conf.getGenOpt().getCharset();
		final boolean detectBefore = fx.conf.getTextEditorOpt().autoDetectEncoding;
		assertEquals("the list shows the configured encoding", charsetBefore, encoding.getSelectedItem());
		assertEquals(detectBefore, detect.isSelected());
		edt(new Runnable() {
			@Override
			public void run() {
				encoding.setSelectedItem("ISO-8859-1");
				detect.doClick();
			}
		});
		assertEquals("ISO-8859-1", fx.conf.getGenOpt().getCharset());
		assertEquals(!detectBefore, fx.conf.getTextEditorOpt().autoDetectEncoding);
		assertEquals("ISO-8859-1", System.getProperty("file.encoding"));
		click(button("btnResetValues"));
		assertEquals(charsetBefore, fx.conf.getGenOpt().getCharset());
		assertEquals(detectBefore, fx.conf.getTextEditorOpt().autoDetectEncoding);
		assertEquals(charsetBefore, System.getProperty("file.encoding"));
		assertEquals(charsetBefore, encoding.getSelectedItem());
	}

	@Test
	public void spinnersAreLiveAndRestored() throws Exception {
		final JSpinner interline = (JSpinner) get(frame, "spinnerInterline");
		final JSpinner maxSize = (JSpinner) get(frame, "spinnerMaxFileSize");
		final int interlineBefore = fx.conf.getTextEditorOpt().getInterline();
		final int maxBefore = fx.conf.getTextEditorOpt().getMaxFileSizeMb();
		edt(new Runnable() {
			@Override
			public void run() {
				interline.setValue(((Number) interline.getValue()).intValue() + 2);
				maxSize.setValue(((Number) maxSize.getValue()).intValue() + 3);
			}
		});
		assertEquals(interlineBefore + 2, fx.conf.getTextEditorOpt().getInterline());
		assertEquals(maxBefore + 3, fx.conf.getTextEditorOpt().getMaxFileSizeMb());
		click(button("btnResetValues"));
		assertEquals(interlineBefore, fx.conf.getTextEditorOpt().getInterline());
		assertEquals(maxBefore, fx.conf.getTextEditorOpt().getMaxFileSizeMb());
		assertEquals("the control shows it again", interlineBefore, ((Number) interline.getValue()).intValue());
	}

	@Test
	public void saveWritesOnlyAfterTheQuestionIsConfirmed() throws Exception {
		JCheckBox indent = (JCheckBox) get(frame, "chckbxAutoindentation");
		final boolean before = indent.isSelected();
		edt(new Runnable() {
			@Override
			public void run() {
				try {
					((JCheckBox) get(frame, "chckbxAutoindentation")).doClick();
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
		assertEquals(!before, fx.conf.getTextEditorOpt().autoIndentEnabled);
		String onDisk = new Wini(fx.iniFile).get(INItializer.TEXT_EDITOR_OPT, "autoIndentEnabled");
		assertEquals("a live change is not in the file yet", String.valueOf(before), onDisk);

		fx.dialogs.answers.add(Boolean.FALSE);
		click(button("btnSaveConf"));
		assertEquals(String.valueOf(before), new Wini(fx.iniFile).get(INItializer.TEXT_EDITOR_OPT, "autoIndentEnabled"));
		assertSame("declining keeps the window open", frame, GraphicPreferencesFrame.singletonInstance);

		fx.dialogs.answers.add(Boolean.TRUE);
		click(button("btnSaveConf"));
		assertEquals(String.valueOf(!before), new Wini(fx.iniFile).get(INItializer.TEXT_EDITOR_OPT, "autoIndentEnabled"));
		assertEquals("the saved value is the new reference: closing does not revert it", !before, fx.conf.getTextEditorOpt().autoIndentEnabled);
		flushEdt();
		assertNull("the window closed after saving", GraphicPreferencesFrame.singletonInstance);
		fx.conf.getTextEditorOpt().autoIndentEnabled = before; // back to the common state of the tests
	}

	@Test
	public void useSettingsKeepsTheChangeWithoutWritingTheFile() throws Exception {
		JCheckBox indent = (JCheckBox) get(frame, "chckbxAutoindentation");
		final boolean before = indent.isSelected();
		edt(new Runnable() {
			@Override
			public void run() {
				try {
					((JCheckBox) get(frame, "chckbxAutoindentation")).doClick();
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
		click(button("btnUseSettings"));
		flushEdt();
		assertEquals("kept for this session", !before, fx.conf.getTextEditorOpt().autoIndentEnabled);
		assertEquals("but not written", String.valueOf(before), new Wini(fx.iniFile).get(INItializer.TEXT_EDITOR_OPT, "autoIndentEnabled"));
		assertNull(GraphicPreferencesFrame.singletonInstance);
		fx.conf.getTextEditorOpt().autoIndentEnabled = before; // back to the common state of the tests
	}
}
