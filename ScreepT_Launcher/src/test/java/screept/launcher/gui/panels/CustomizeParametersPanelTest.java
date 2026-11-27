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
package screept.launcher.gui.panels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Callable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import mainlauncher.ScreepTLauncher;
import screept.launcher.utils.IniFileManager;
import screept.launcher.utils.JvmHelper;

/**
 * The window of the start options of the launcher (memory, profile, collector, scale, log level), built over a launcher
 * ini in a temporary folder. It is never shown, "Save" (it restarts the launcher analysis) and "Download Java" are not pressed.
 */
public class CustomizeParametersPanelTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private IniFileManager previousIni;
	private JvmHelper previousJvmHelper;
	private CustomizeParametersPanel panel;

	@Before
	public void build() throws Exception {
		Assume.assumeFalse("needs a display server (windows are never shown)", GraphicsEnvironment.isHeadless());
		File ini = new File(tmp.getRoot(), "Launcher_Configuration.ini");
		Files.write(ini.toPath(), ("[JVM_OPTIONS]\nmemoryArgs=-Xmx2048m -Xms512m\nstackSizeArgs=4\ngcArgs=\nmemoryDefaultMigrated=true\n"
				+ "[APPLICATION_OPTIONS]\nscaleGuiPerc=1.0\nviewCmdConsole=false\nlogLevelSetting=INFO\n").getBytes(StandardCharsets.UTF_8));
		previousIni = ScreepTLauncher.iniFileManager;
		ScreepTLauncher.iniFileManager = new IniFileManager(ini.getAbsolutePath());
		ScreepTLauncher.iniFileManager.initFromINIFile();
		previousJvmHelper = ScreepTLauncher.jvmHelper;
		ScreepTLauncher.jvmHelper = new JvmHelper(IniFileManager.MAX_JRE_VERSION, IniFileManager.MIN_JRE_VERSION);
		panel = edt(new Callable<CustomizeParametersPanel>() {
			@Override
			public CustomizeParametersPanel call() {
				return new CustomizeParametersPanel("test", null);
			}
		});
	}

	@After
	public void close() throws Exception {
		edt(new Callable<Void>() {
			@Override
			public Void call() {
				panel.dispose();
				return null;
			}
		});
		CustomizeParametersPanel.active = false;
		ScreepTLauncher.iniFileManager = previousIni;
		ScreepTLauncher.jvmHelper = previousJvmHelper;
	}

	private static <T> T edt(final Callable<T> c) throws Exception {
		final Object[] box = new Object[2];
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
					box[0] = c.call();
				} catch (Throwable t) {
					box[1] = t;
				}
			}
		});
		if (box[1] != null)
			throw new AssertionError("EDT: " + box[1], (Throwable) box[1]);
		@SuppressWarnings("unchecked")
		T r = (T) box[0];
		return r;
	}

	private static void edt(final Runnable r) throws Exception {
		edt(new Callable<Void>() {
			@Override
			public Void call() {
				r.run();
				return null;
			}
		});
		// listeners that defer with invokeLater
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
			}
		});
	}

	@SuppressWarnings("unchecked")
	private <T> T f(String name) throws Exception {
		Field field = CustomizeParametersPanel.class.getDeclaredField(name);
		field.setAccessible(true);
		return (T) field.get(panel);
	}

	private int spinner(String name) throws Exception {
		return (Integer) ((JSpinner) f(name)).getValue();
	}

	private void set(final String name, final int value) throws Exception {
		final JSpinner s = f(name);
		edt(new Runnable() {
			@Override
			public void run() {
				s.setValue(value);
			}
		});
	}

	@Test
	public void startsFromTheValuesOfTheIni() throws Exception {
		assertNotNull(panel);
		assertTrue(CustomizeParametersPanel.active);
		assertEquals(2048, spinner("spinnerMaxMemoryMB"));
		assertTrue("the initial heap never exceeds the maximum", spinner("spinnerMinMemoryMB") <= spinner("spinnerMaxMemoryMB"));
		assertEquals(4, spinner("spinnerXssMemory"));
		JComboBox<?> log = f("comboLogLevel");
		assertEquals("INFO", log.getSelectedItem());
		assertFalse(((JCheckBox) f("chckbxActiveShell")).isSelected());
	}

	@Test
	public void maximumAndMinimumMemoryPushEachOther() throws Exception {
		set("spinnerMaxMemoryMB", 1000);
		set("spinnerMinMemoryMB", 800);
		assertEquals(800, spinner("spinnerMinMemoryMB"));
		// a maximum lower than the minimum drags the minimum down
		set("spinnerMaxMemoryMB", 600);
		assertEquals(600, spinner("spinnerMinMemoryMB"));
		// a minimum higher than the maximum drags the maximum up
		set("spinnerMinMemoryMB", 700);
		assertEquals(700, spinner("spinnerMaxMemoryMB"));
	}

	@Test
	public void profilesFillTheValuesAndHandEditsMakeThemCustom() throws Exception {
		final JComboBox<?> profile = f("comboProfile");
		final JComboBox<?> gc = f("comboGc");
		int custom = profile.getItemCount() - 1;
		assertTrue("at least one profile besides custom", custom >= 1);
		for (int i = 0; i < custom; i++) {
			final int index = i;
			int maxBefore = spinner("spinnerMaxMemoryMB");
			edt(new Runnable() {
				@Override
				public void run() {
					profile.setSelectedIndex(index);
				}
			});
			assertEquals("the maximum heap is never touched by a profile", maxBefore, spinner("spinnerMaxMemoryMB"));
			assertEquals("the profile stays selected", i, profile.getSelectedIndex());
		}
		// editing a value by hand: the combination is no longer a profile
		set("spinnerXssMemory", spinner("spinnerXssMemory") + 1);
		assertEquals(custom, profile.getSelectedIndex());
		// every collector can be chosen, and each one updates the description
		for (int i = 0; i < gc.getItemCount(); i++) {
			final int index = i;
			edt(new Runnable() {
				@Override
				public void run() {
					gc.setSelectedIndex(index);
				}
			});
			assertEquals(i, gc.getSelectedIndex());
		}
	}

	@Test
	public void scaleSliderUpdatesItsLabel() throws Exception {
		final JSlider slider = f("sliderGuiScale");
		edt(new Runnable() {
			@Override
			public void run() {
				slider.setValue(slider.getMaximum());
			}
		});
		String top = ((javax.swing.JLabel) f("dynLblScaleGui")).getText();
		edt(new Runnable() {
			@Override
			public void run() {
				slider.setValue(slider.getMinimum());
			}
		});
		String bottom = ((javax.swing.JLabel) f("dynLblScaleGui")).getText();
		assertFalse("the label follows the slider: " + top + " / " + bottom, top.equals(bottom));
	}

	@Test
	public void defaultsAndReloadRestoreValues() throws Exception {
		set("spinnerMaxMemoryMB", 4000);
		set("spinnerXssMemory", 9);
		JButton reload = f("btnReloadInitialConfig");
		click(reload);
		assertEquals("back to what the ini had", 2048, spinner("spinnerMaxMemoryMB"));
		assertEquals(4, spinner("spinnerXssMemory"));
		set("spinnerMaxMemoryMB", 4000);
		JButton defaults = f("btnLoadScreeptDefault");
		click(defaults);
		Thread.sleep(300);
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
			}
		});
		assertFalse("the ScreepT defaults replaced the edit", spinner("spinnerMaxMemoryMB") == 4000);
	}

	@Test
	public void exitButtonClosesTheWindow() throws Exception {
		JButton exit = f("btnExitConfiguration");
		click(exit);
		assertFalse(CustomizeParametersPanel.active);
		assertFalse(panel.isDisplayable());
	}

	private void click(final JButton b) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				b.doClick();
			}
		});
	}
}
