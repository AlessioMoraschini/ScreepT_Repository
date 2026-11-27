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
package screept.gui.panels;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gui.commons.dialogutils.JOptionHelperExtended;
import initializer.configs.impl.INItializer;
import maingUI.MainGUI;
import maingUI.MainGuiParent;
import screept.crypter.impl.manager.CryptoMode;
import screept.crypter.impl.manager.ScreeptManagerFileOrFolder;
import screept.crypter.om.CrypterResultDTO;

/**
 * The real CrypterPanel, built and driven through its own controls on a frame that is never shown (no window
 * appears on screen; modal dialogs are replaced by a recorder). It needs a display server, so it is skipped when
 * the JVM is headless. The encryption itself goes through the same facade object the buttons use, with the
 * password, mode, key image and sources that the panel holds.
 */
public class CrypterPanelGuiTest {

	/** Dialogs never appear: they are written here */
	private static final class RecordingDialogs extends JOptionHelperExtended {
		final List<String> infos = new ArrayList<String>();
		final List<String> warns = new ArrayList<String>();
		final List<String> errors = new ArrayList<String>();

		RecordingDialogs(java.awt.Component parent) {
			super(parent, false);
		}

		@Override
		public void info(String msg, String title) {
			infos.add(title);
		}

		@Override
		public void info(String msg) {
			infos.add(msg);
		}

		@Override
		public void warn(String msg, String title) {
			warns.add(title);
		}

		@Override
		public void warn(String msg) {
			warns.add(msg);
		}

		@Override
		public void error(String msg, String title) {
			errors.add(title);
		}

		@Override
		public void error(String msg) {
			errors.add(msg);
		}

		@Override
		public boolean yesOrNo(String msg, String title) {
			return false;
		}

		@Override
		public boolean yesOrNo(String msg) {
			return false;
		}
	}

	private static CrypterPanel panel;
	private static RecordingDialogs dialogs;

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	// ------------------------------------------------------------------ plumbing

	private static <T> T edt(final Callable<T> job) throws Exception {
		if (SwingUtilities.isEventDispatchThread()) {
			return job.call();
		}
		final AtomicReference<T> result = new AtomicReference<T>();
		final AtomicReference<Throwable> error = new AtomicReference<Throwable>();
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
					result.set(job.call());
				} catch (Throwable t) {
					error.set(t);
				}
			}
		});
		if (error.get() != null) {
			throw new AssertionError(error.get());
		}
		return result.get();
	}

	private static Object field(String name) throws Exception {
		Field f = CrypterPanel.class.getDeclaredField(name);
		f.setAccessible(true);
		return f.get(panel);
	}

	private static void setField(String name, Object value) throws Exception {
		Field f = CrypterPanel.class.getDeclaredField(name);
		f.setAccessible(true);
		f.set(panel, value);
	}

	@SuppressWarnings("unchecked")
	private static JComboBox<CryptoMode> combo() throws Exception {
		return (JComboBox<CryptoMode>) field("comboModeCrypto");
	}

	private static JPasswordField psw() throws Exception {
		return (JPasswordField) field("AESKEYtext");
	}

	private static void setMode(final CryptoMode mode) throws Exception {
		edt(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				combo().setSelectedItem(mode);
				return null;
			}
		});
	}

	private static void setPassword(final String s) throws Exception {
		edt(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				psw().setText(s);
				return null;
			}
		});
	}

	private static void addSource(final File f) throws Exception {
		edt(new Callable<Void>() {
			@Override
			public Void call() {
				panel.addNewSourceFile(f);
				return null;
			}
		});
	}

	private static void invokePrivate(final String method, final Class<?>[] types, final Object... args) throws Exception {
		final Method m = CrypterPanel.class.getDeclaredMethod(method, types);
		m.setAccessible(true);
		edt(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				m.invoke(panel, args);
				return null;
			}
		});
	}

	private static void useKeyImage(File f) throws Exception {
		invokePrivate("useAsKeyImage", new Class<?>[] { File.class }, f);
	}

	private static void refreshBadge() throws Exception {
		invokePrivate("refreshSecurityBadge", new Class<?>[0]);
	}

	private static void writeKeyImage(File f, long seed) throws Exception {
		BufferedImage img = new BufferedImage(24, 24, BufferedImage.TYPE_INT_RGB);
		Random r = new Random(seed);
		for (int y = 0; y < 24; y++) {
			for (int x = 0; x < 24; x++) {
				img.setRGB(x, y, r.nextInt(0xFFFFFF));
			}
		}
		ImageIO.write(img, "png", f);
	}

	// ------------------------------------------------------------------ lifecycle

	@BeforeClass
	public static void buildPanel() throws Exception {
		Assume.assumeFalse("needs a display server (windows are never shown)", GraphicsEnvironment.isHeadless());
		edt(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Field u = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
				u.setAccessible(true);
				// MainGUI is the application window: its constructor builds the whole application, the panel only needs its frame
				MainGUI app = (MainGUI) ((sun.misc.Unsafe) u.get(null)).allocateInstance(MainGUI.class);
				MainGuiParent.mainFrameScreept = new JFrame("test (never shown)");
				MainGUI.mainAPP = app;
				INItializer conf = new INItializer();
				MainGUI.currentConfiguration = conf;
				panel = new CrypterPanel(conf, app);
				dialogs = new RecordingDialogs(panel);
				panel.dialogHelper = dialogs;
				return null;
			}
		});
		setField("customChosen", Boolean.TRUE); // the Custom mode would open its modal dialog
		setField("isKilled", Boolean.TRUE); // ends the panel's key-dictionary watcher (no dictionary folder in a test)
	}

	@AfterClass
	public static void dispose() throws Exception {
		if (panel != null) {
			MainGUI.mainAppRunning = false; // stops the panel's watcher threads
			edt(new Callable<Void>() {
				@Override
				public Void call() {
					MainGuiParent.mainFrameScreept.dispose();
					return null;
				}
			});
		}
	}

	@Before
	public void reset() throws Exception {
		Assume.assumeNotNull(panel);
		dialogs.infos.clear();
		dialogs.warns.clear();
		dialogs.errors.clear();
		setPassword("");
		setField("srcKeyImageFile", null);
		setMode(CryptoMode.SECURE_256);
	}

	@After
	public void noWindowWasShown() {
		if (panel != null) {
			assertFalse("the test frame must stay hidden", MainGuiParent.mainFrameScreept.isVisible());
		}
	}

	// ------------------------------------------------------------------ tests

	@Test
	public void panelIsBuiltWithItsMainControls() throws Exception {
		assertNotNull(panel.btnCreept);
		assertNotNull(panel.btnDeCreept);
		assertNotNull(panel.btnAddSourceFile);
		assertNotNull(panel.btnDispoKeyImg);
		assertNotNull(panel.panelVDragNDropPane);
		assertTrue(panel.btnCreept.isEnabled());
		assertTrue(panel.btnDeCreept.isEnabled());
		assertEquals(CryptoMode.values().length, combo().getItemCount());
		assertEquals(0, psw().getPassword().length);
	}

	@Test
	public void modeChoiceShowsTheControlsOfThatMode() throws Exception {
		JCheckBox shuffle = (JCheckBox) field("chckbxShuffle");
		JCheckBox hash = (JCheckBox) field("chckbxHashKey");
		JButton options = (JButton) field("btnProtectionOptions");
		for (CryptoMode m : CryptoMode.values()) {
			setMode(m);
			boolean engine = m.profile != null;
			assertEquals(m + ": options button", engine, options.isVisible());
			assertEquals(m + ": legacy shuffle switch", !engine, shuffle.isVisible());
			assertEquals(m + ": legacy hash switch", !engine, hash.isVisible());
			assertEquals(m + ": hash only for legacy AES modes", m.isAES() && !engine, hash.isEnabled());
		}
	}

	@Test
	public void modeChoiceIsWrittenInTheConfiguration() throws Exception {
		INItializer conf = (INItializer) field("currentConf");
		setMode(CryptoMode.ONLY_STEG);
		assertTrue(conf.getSteganOpt().isFlagSteg());
		assertFalse(conf.getAesOpt().isFlagAes());
		setMode(CryptoMode.ONLY_AES);
		assertTrue(conf.getAesOpt().isFlagAes());
		assertFalse(conf.getSteganOpt().isFlagSteg());
	}

	@Test
	public void showPasswordTogglesTheEchoAndKeepsTheText() throws Exception {
		final JCheckBox show = (JCheckBox) field("chckbxShowAesKey");
		JPasswordField field = psw();
		setPassword("my secret");
		assertFalse("starts hidden", show.isSelected());
		char hidden = field.getEchoChar();
		assertNotEquals((char) 0, hidden);
		edt(new Callable<Void>() {
			@Override
			public Void call() {
				show.doClick();
				return null;
			}
		});
		assertEquals((char) 0, field.getEchoChar());
		edt(new Callable<Void>() {
			@Override
			public Void call() {
				show.doClick();
				return null;
			}
		});
		assertEquals(hidden, field.getEchoChar());
		assertArrayEquals("my secret".toCharArray(), field.getPassword());
	}

	@Test
	public void securityBadgeFollowsPasswordAndKeyImage() throws Exception {
		JLabel badge = (JLabel) field("securityBadge");
		refreshBadge();
		String empty = badge.getText();
		setPassword("a much longer and stronger passphrase 2026!");
		refreshBadge();
		assertNotEquals("the badge reacts to the password", empty, badge.getText());
		// a mode that needs a key image warns until one is chosen
		setMode(CryptoMode.SECURE_KEYIMAGE);
		refreshBadge();
		String noImage = badge.getText();
		File img = tmp.newFile("k.png");
		writeKeyImage(img, 1);
		useKeyImage(img);
		refreshBadge();
		assertNotEquals("the badge notices the key image", noImage, badge.getText());
	}

	@Test
	public void sourcesAreAddedOnceAndBadInputIsReported() throws Exception {
		File a = tmp.newFile("a.txt");
		Files.write(a.toPath(), "alpha".getBytes("UTF-8"));
		File dir = tmp.newFolder("folder");
		int before = panel.panelVDragNDropPane.getTreeNodes().length;
		addSource(a);
		assertTrue(panel.isFileAlreadyLoadedInRoot(a));
		assertEquals(1, dialogs.infos.size());
		addSource(a); // again: refused
		assertEquals("already loaded is reported", 2, dialogs.infos.size());
		addSource(new File(tmp.getRoot(), "does-not-exist.txt"));
		assertEquals(1, dialogs.warns.size());
		addSource(null); // cancelled chooser: silent
		assertEquals(2, dialogs.infos.size());
		assertEquals(1, dialogs.warns.size());
		addSource(dir);
		assertTrue(panel.isFileAlreadyLoadedInRoot(dir));
		assertEquals(before + 2, panel.panelVDragNDropPane.getTreeNodes().length);
	}

	@Test
	public void keyImageIsAcceptedOnlyIfItIsARealPng() throws Exception {
		File good = tmp.newFile("good.png");
		writeKeyImage(good, 3);
		useKeyImage(good);
		assertEquals(good, field("srcKeyImageFile"));
		assertEquals(1, dialogs.infos.size());
		assertEquals(0, dialogs.errors.size());

		setField("srcKeyImageFile", null);
		File fake = tmp.newFile("fake.png");
		Files.write(fake.toPath(), "this is not an image".getBytes("UTF-8"));
		useKeyImage(fake);
		assertNull("not a real image", field("srcKeyImageFile"));
		assertEquals(1, dialogs.errors.size());

		File jpg = tmp.newFile("photo.jpg");
		ImageIO.write(new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB), "jpg", jpg);
		useKeyImage(jpg); // only png keys are lossless
		assertNull(field("srcKeyImageFile"));
		assertEquals(2, dialogs.errors.size());
	}

	/** What the Encrypt and Decrypt buttons do, with the state the panel holds */
	private static CrypterResultDTO run(boolean encrypt, CryptoMode mode, File src, File outDir) throws Exception {
		ScreeptManagerFileOrFolder m = (ScreeptManagerFileOrFolder) field("screeptManagerFoF");
		char[] pw = psw().getPassword();
		File key = (File) field("srcKeyImageFile");
		return encrypt ? m.crYpt(mode, src, outDir, pw, key, false) : m.decrYpt(mode, src, outDir, pw, key, false);
	}

	@Test
	public void panelStateEncryptsAndDecryptsAFile() throws Exception {
		File src = tmp.newFile("report.txt");
		byte[] content = new byte[150000];
		new Random(9).nextBytes(content);
		Files.write(src.toPath(), content);
		addSource(src);
		boolean listed = false;
		for (File f : panel.panelVDragNDropPane.getTreeNodes()) {
			listed |= f.equals(src);
		}
		assertTrue("the file is in the list the Encrypt button reads", listed);

		setMode(CryptoMode.SECURE_256);
		setPassword("panel driven password, long enough");
		File encDir = tmp.newFolder();
		assertTrue(run(true, (CryptoMode) combo().getSelectedItem(), src, encDir).resultOK);
		File enc = new File(encDir, "report.txt.scpt");
		assertTrue(enc.isFile());

		// decrypt with another mode selected: the file header decides
		setMode(CryptoMode.ONLY_AES);
		File ok = tmp.newFolder();
		assertTrue(run(false, (CryptoMode) combo().getSelectedItem(), enc, ok).resultOK);
		assertArrayEquals(content, Files.readAllBytes(new File(ok, "report.txt").toPath()));

		// a typo in the password field: refused, nothing is left behind
		setPassword("panel driven password, long enough ");
		File bad = tmp.newFolder();
		assertFalse(run(false, CryptoMode.ONLY_AES, enc, bad).resultOK);
		assertEquals(0, bad.list().length);
	}

	@Test
	public void keyImageModeThroughThePanel() throws Exception {
		File key = tmp.newFile("key.png");
		writeKeyImage(key, 11);
		useKeyImage(key);
		File src = tmp.newFile("secret.txt");
		Files.write(src.toPath(), "top secret".getBytes("UTF-8"));
		setMode(CryptoMode.SECURE_KEYIMAGE);
		setPassword("key image mode password!");
		File encDir = tmp.newFolder();
		assertTrue(run(true, CryptoMode.SECURE_KEYIMAGE, src, encDir).resultOK);
		File enc = new File(encDir, "secret.txt.scpt");
		File ok = tmp.newFolder();
		assertTrue(run(false, CryptoMode.SECURE_KEYIMAGE, enc, ok).resultOK);
		assertArrayEquals("top secret".getBytes("UTF-8"), Files.readAllBytes(new File(ok, "secret.txt").toPath()));
		// the key image removed from the panel: the file cannot be opened any more
		setField("srcKeyImageFile", null);
		assertFalse(run(false, CryptoMode.SECURE_KEYIMAGE, enc, tmp.newFolder()).resultOK);
	}
}
