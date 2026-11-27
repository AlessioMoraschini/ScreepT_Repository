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
package gui.texteditor.commons.tabbededitor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gui.commons.dialogutils.JOptionHelperExtended;
import gui.texteditor.commons.encrypted.EncryptedFileSupport;
import gui.texteditor.commons.om.TabTextEditorObject;
import various.common.light.utility.string.StringWorker;
import initializer.configs.impl.INItializer;

/**
 * The tabbed editor driven with a stand-in owner: tabs, text, saving and the rules of the encrypted tabs (never written in plain
 * form, never reloaded from the encrypted bytes, secrets wiped on close).
 */
public class CustomTabbedEditorTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private CustomTabbedEditor editor;
	private EncryptedFileSupport support;
	private final List<String> warnings = new ArrayList<String>();

	/** Every method answers with the default of its return type (false, 0, null): enough for an owner that does nothing */
	@SuppressWarnings("unchecked")
	private static <T> T stub(Class<T> type, final InvocationHandler special) {
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, (proxy, method, args) -> {
			if (special != null) {
				Object r = special.invoke(proxy, method, args);
				if (r != NO_ANSWER) {
					return r;
				}
			}
			Class<?> rt = method.getReturnType();
			if (rt == boolean.class) {
				return false;
			} else if (rt == int.class) {
				return 0;
			} else if (rt == long.class) {
				return 0L;
			}
			return null;
		});
	}

	private static final Object NO_ANSWER = new Object();

	private static class FakeBinding implements EncryptedFileSupport.Binding {
		final List<String> writes = new ArrayList<String>();
		boolean closed;
		boolean failNext;

		@Override
		public void write(String text, File target) throws IOException {
			if (failNext) {
				failNext = false;
				throw new IOException("disk full");
			}
			writes.add(text);
			// a real binding encrypts: here the file gets a marker that is not the text
			Files.write(target.toPath(), ("ENC:" + new StringBuilder(text).reverse()).getBytes(StandardCharsets.UTF_8));
		}

		@Override
		public String describe() {
			return "fake";
		}

		@Override
		public void close() {
			closed = true;
		}
	}

	private FakeBinding lastBinding;
	private final AtomicInteger opened = new AtomicInteger();

	@Before
	public void setUp() throws Exception {
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		// the limits are filled at start-up: at 0 every file would count as "too big" and the LogSearch question would appear
		// filled at start-up too: the tab header needs it
		resources.GeneralConfig.BUTTON_TAB_HEAD_FONT = new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12);
		resources.GeneralConfig.MAX_FILE_SIZE_TXT_EDITOR_KB = 1024 * 20;
		resources.GeneralConfig.MAX_TEXT_EDITOR_SAFE_LINES = 1000000;
		support = new EncryptedFileSupport() {
			@Override
			public boolean isEncrypted(File file) {
				try {
					return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).startsWith("ENC:");
				} catch (IOException e) {
					return false;
				}
			}

			@Override
			public Opened open(File file) {
				opened.incrementAndGet();
				lastBinding = new FakeBinding();
				return new Opened("decrypted text of " + file.getName(), lastBinding);
			}
		};
		final JTextField target = new JTextField();
		ITabbedEditorOwner owner = stub(ITabbedEditorOwner.class, (proxy, method, args) -> {
			switch (method.getName()) {
			case "getNewFileStringTarget":
				return target;
			case "getEncryptedFileSupport":
				return support;
			case "askBeforeRemove":
				return true;
			case "getShortcutManager":
				return stub(various.common.light.om.ICommonShortcuts.class, null);
			case "getAdditionalConditionBeforeUpdateOnDocumentEvent":
				return true;
			default:
				return NO_ANSWER;
			}
		});
		SwingUtilities.invokeAndWait(() -> {
			editor = new CustomTabbedEditor(new javax.swing.JFrame(), conf(), owner);
			editor.dialogHelper = new JOptionHelperExtended(null, false) {
				@Override
				public void warn(String msg, String title) {
					warnings.add(msg);
				}
			};
			editor.addTab(null); // the application always starts with one empty tab
		});
	}

	/** A configuration bound to a temporary ini: the user's one is never touched */
	private INItializer conf() {
		try {
			File ini = new File(tmp.newFolder(), "test.ini");
			ini.createNewFile();
			INItializer conf = new INItializer();
			conf.setIniFile(new org.ini4j.Wini(ini));
			conf.setIniReadFile(new org.ini4j.Wini(ini));
			return conf;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private TabTextEditorObject open(File f) throws Exception {
		SwingUtilities.invokeAndWait(() -> editor.loadFile(f));
		SwingUtilities.invokeAndWait(() -> { });
		return editor.loadedTabObj;
	}

	private File write(String name, String content) throws Exception {
		File f = new File(tmp.newFolder(), name);
		Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
		return f;
	}

	// ------------------------------------------------------------------ tabs restored at start (lazy)

	@Test
	public void lazyTabReadsItsFileOnlyWhenShown() throws Exception {
		File f = write("lazy.txt", "late\ncontent");
		final TabTextEditorObject[] lazy = new TabTextEditorObject[1];
		int before = editor.mainTabList.getSelectedIndex();
		SwingUtilities.invokeAndWait(() -> lazy[0] = editor.addLazyTab(f, null));
		assertNotNull(lazy[0]);
		assertEquals("added, but the one in front does not change", before, editor.mainTabList.getSelectedIndex());
		assertTrue(lazy[0].isLazyPending());
		assertEquals("", lazy[0].textArea.getText());
		assertEquals(f, lazy[0].loadedFile);
		assertEquals(f.getName(), editor.mainTabList.getTitleAt(editor.mainTabList.getTabCount() - 1));

		SwingUtilities.invokeAndWait(() -> editor.mainTabList.setSelectedIndex(editor.mainTabList.getTabCount() - 1));
		assertFalse(lazy[0].isLazyPending());
		assertEquals("late\ncontent", lazy[0].textArea.getText().replace("\r\n", "\n").trim());
		assertFalse("loading a file is not an edit", lazy[0].unsaved);
		assertEquals(f, lazy[0].loadedFile);
	}

	@Test
	public void lazyTabStaysEmptyWhileTheRestoreIsDeferred() throws Exception {
		File f = write("deferred.txt", "x");
		final TabTextEditorObject[] lazy = new TabTextEditorObject[1];
		SwingUtilities.invokeAndWait(() -> {
			editor.lazyLoadDeferred = true;
			lazy[0] = editor.addLazyTab(f, null);
			editor.mainTabList.setSelectedIndex(editor.mainTabList.getTabCount() - 1);
		});
		assertTrue("shown during the restore: still not read", lazy[0].isLazyPending());
		SwingUtilities.invokeAndWait(() -> {
			editor.lazyLoadDeferred = false;
			editor.loadLazyContent(editor.mainTabList.getSelectedIndex());
		});
		assertFalse(lazy[0].isLazyPending());
		assertEquals("x", lazy[0].textArea.getText().replace("\r\n", "\n").trim());
	}

	@Test
	public void lazyTabCanBeClosedWithoutEverReadingIt() throws Exception {
		File f = write("never.txt", "never read");
		final TabTextEditorObject[] lazy = new TabTextEditorObject[1];
		SwingUtilities.invokeAndWait(() -> lazy[0] = editor.addLazyTab(f, null));
		int tabs = editor.mainTabList.getTabCount();
		SwingUtilities.invokeAndWait(() -> editor.removeTab(tabs - 1));
		SwingUtilities.invokeAndWait(() -> { });
		assertEquals(tabs - 1, editor.mainTabList.getTabCount());
		assertTrue("never loaded", lazy[0].isLazyPending());
	}

	@Test
	public void lazyImageTabOpensInThePanelNotInTheImageWindow() throws Exception {
		gui.commons.tabs.TabImplementors.register(new gui.commons.imagestudio.ImageStudioTabImplementor());
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(40, 30, java.awt.image.BufferedImage.TYPE_INT_RGB);
		File f = new File(tmp.newFolder(), "pic.png");
		javax.imageio.ImageIO.write(img, "png", f);
		final TabTextEditorObject[] lazy = new TabTextEditorObject[1];
		// the viewer of the tab was not saved (null): the image still goes to the viewer chosen for images
		SwingUtilities.invokeAndWait(() -> lazy[0] = editor.addLazyTab(f, null));
		SwingUtilities.invokeAndWait(() -> editor.mainTabList.setSelectedIndex(editor.mainTabList.getTabCount() - 1));
		assertTrue("the image is shown by the viewer panel of the tab", lazy[0].isEmbeddedViewer());
		assertEquals(gui.commons.imagestudio.ImageStudioTabImplementor.ID, lazy[0].viewerId);
	}

	@Test
	public void encryptedFileIsNotAddedAsLazyTab() throws Exception {
		File f = write("secret.scp", "ENC:abc");
		int tabs = editor.mainTabList.getTabCount();
		final TabTextEditorObject[] lazy = new TabTextEditorObject[1];
		SwingUtilities.invokeAndWait(() -> lazy[0] = editor.addLazyTab(f, null));
		assertNull(lazy[0]);
		assertEquals(tabs, editor.mainTabList.getTabCount());
	}

	// ------------------------------------------------------------------ plain tabs

	@Test
	public void plainFileOpensWithItsText() throws Exception {
		File f = write("plain.txt", "hello\nworld");
		TabTextEditorObject tab = open(f);
		assertEquals(f, tab.loadedFile);
		assertEquals("hello\nworld", tab.textArea.getText().replace("\r\n", "\n").trim());
		assertFalse(tab.isEncrypted());
		assertFalse(tab.unsaved);
	}

	@Test
	public void newEmptyTabHasNoFile() throws Exception {
		int before = editor.mainTabList.getTabCount();
		SwingUtilities.invokeAndWait(() -> editor.addTab(null));
		assertEquals(before + 1, editor.mainTabList.getTabCount());
		assertNull(editor.loadedTabObj.loadedFile);
		assertEquals("", editor.loadedTabObj.textArea.getText());
	}

	@Test
	public void writeTabToFileWritesPlainTextForAPlainTab() throws Exception {
		File f = write("p.txt", "old");
		TabTextEditorObject tab = open(f);
		assertTrue(editor.writeTabToFile(tab, "new content", f));
		assertEquals("new content", new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
	}

	// ------------------------------------------------------------------ encoding of the files

	private static final String ACCENTED = "caff\u00e8 \u20ac \u201cok\u201d pi\u00f9";

	private File writeBytes(String name, byte[] content) throws Exception {
		File f = new File(tmp.newFolder(), name);
		Files.write(f.toPath(), content);
		return f;
	}

	@Test
	public void legacyFileIsReadAndSavedAsLegacyWhenDetectionIsOn() throws Exception {
		java.nio.charset.Charset cp1252 = java.nio.charset.Charset.forName("windows-1252");
		File f = writeBytes("legacy.txt", ACCENTED.getBytes(cp1252));
		TabTextEditorObject tab = open(f);
		assertEquals("windows-1252", tab.encoding);
		assertEquals(ACCENTED + "\n", tab.textArea.getText().replace("\r\n", "\n"));
		assertTrue(editor.writeTabToFile(tab, ACCENTED + " ancora", f));
		assertArrayEquals("saved with the encoding it was read with", (ACCENTED + " ancora").getBytes(cp1252), Files.readAllBytes(f.toPath()));
	}

	@Test
	public void utf8AndBomFilesKeepTheirBytes() throws Exception {
		File f = writeBytes("u8.txt", ACCENTED.getBytes(StandardCharsets.UTF_8));
		TabTextEditorObject tab = open(f);
		assertEquals("UTF-8", tab.encoding);
		assertTrue(tab.textArea.getText().startsWith(ACCENTED));
		assertTrue(editor.writeTabToFile(tab, ACCENTED, f));
		assertArrayEquals(ACCENTED.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(f.toPath()));

		byte[] utf16 = ("\ufeff" + ACCENTED).getBytes(StandardCharsets.UTF_16LE);
		File g = writeBytes("u16.txt", utf16);
		TabTextEditorObject tab16 = open(g);
		assertEquals("UTF-16LE", tab16.encoding);
		assertTrue(tab16.textArea.getText().contains(ACCENTED));
		assertTrue(editor.writeTabToFile(tab16, "\ufeff" + ACCENTED, g));
		assertArrayEquals("byte order mark included", utf16, Files.readAllBytes(g.toPath()));
	}

	@Test
	public void withDetectionOffTheFixedEncodingIsUsed() throws Exception {
		java.nio.charset.Charset cp1252 = java.nio.charset.Charset.forName("windows-1252");
		editor.currentConfiguration.getTextEditorOpt().autoDetectEncoding = false;
		File f = writeBytes("fixed.txt", ACCENTED.getBytes(cp1252));
		TabTextEditorObject tab = open(f);
		assertEquals("UTF-8", tab.encoding);
		assertTrue("read as UTF-8 the legacy bytes are not valid", tab.textArea.getText().indexOf('\ufffd') >= 0);

		editor.currentConfiguration.getGenOpt().setCharset("windows-1252");
		File g = writeBytes("fixed2.txt", ACCENTED.getBytes(cp1252));
		TabTextEditorObject tab2 = open(g);
		assertEquals("windows-1252", tab2.encoding);
		assertTrue(tab2.textArea.getText().startsWith(ACCENTED));
	}

	@Test
	public void plainAsciiTakesTheEncodingOfThePreferences() throws Exception {
		editor.currentConfiguration.getGenOpt().setCharset("ISO-8859-1");
		TabTextEditorObject tab = open(write("ascii.txt", "only ascii"));
		assertEquals("ISO-8859-1", tab.encoding);
		// a new tab has no file to read: it keeps no encoding, the default one of the application is used when it is saved
		SwingUtilities.invokeAndWait(() -> editor.addTab(null));
		SwingUtilities.invokeAndWait(() -> { });
		assertEquals(null, editor.loadedTabObj.encoding);
	}

	@Test
	public void saveAllKeepsTheLineEndingOfTheFileUnlessTheOptionIsOff() throws Exception {
		editor.currentConfiguration.getTextEditorOpt().keepOriginalEol = true;
		File f = writeBytes("unix.txt", "a\nb\n".getBytes(StandardCharsets.UTF_8));
		TabTextEditorObject tab = open(f);
		assertEquals(StringWorker.EOL.LF, tab.eol);
		SwingUtilities.invokeAndWait(() -> tab.textArea.setText("a\r\nb\r\nc"));
		tab.unsaved = true;
		assertTrue(editor.saveAllUnsavedExisting());
		assertArrayEquals("a\nb\nc".getBytes(StandardCharsets.UTF_8), Files.readAllBytes(f.toPath()));

		editor.currentConfiguration.getTextEditorOpt().keepOriginalEol = false;
		assertEquals(StringWorker.EOL.defaultEol, editor.eolFor(tab));
	}

	@Test
	public void aNewFileKeepsTheLineEndingItWasCreatedWith() throws Exception {
		editor.currentConfiguration.getTextEditorOpt().keepOriginalEol = true;
		SwingUtilities.invokeAndWait(() -> editor.addTab(null));
		TabTextEditorObject tab = editor.loadedTabObj;
		File f = new File(tmp.newFolder(), "new.txt");
		assertTrue(editor.writeTabToFile(tab, "a\rb\r", f));
		assertEquals(StringWorker.EOL.CR, editor.eolFor(tab));
		assertEquals(System.getProperty("file.encoding"), tab.encoding);
	}

	@Test
	public void convertedAndReopenedEncodingsStayWithTheTab() throws Exception {
		File f = writeBytes("convert.txt", ACCENTED.getBytes(StandardCharsets.UTF_8));
		TabTextEditorObject tab = open(f);
		SwingUtilities.invokeAndWait(() -> EncodingActions.convert(editor, tab, "windows-1252"));
		assertTrue("the file changes only when saved", tab.unsaved);
		assertArrayEquals(ACCENTED.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(f.toPath()));
		assertTrue(editor.writeTabToFile(tab, ACCENTED, f));
		assertArrayEquals(ACCENTED.getBytes(java.nio.charset.Charset.forName("windows-1252")), Files.readAllBytes(f.toPath()));
		assertEquals("windows-1252", tab.forcedEncoding);

		tab.unsaved = false;
		SwingUtilities.invokeAndWait(() -> EncodingActions.reopen(editor, tab, "ISO-8859-1"));
		SwingUtilities.invokeAndWait(() -> { });
		assertEquals("ISO-8859-1", tab.encoding);
		assertFalse(tab.unsaved);
		assertTrue("the encoding of the preferences never changes", "UTF-8".equals(editor.currentConfiguration.getGenOpt().getCharset()));
	}

	// ------------------------------------------------------------------ encrypted tabs

	@Test
	public void encryptedFileOpensDecryptedAndBound() throws Exception {
		File f = write("secret.scp2", "ENC:garbage");
		TabTextEditorObject tab = open(f);
		assertEquals(1, opened.get());
		assertTrue(tab.isEncrypted());
		assertEquals(f, tab.loadedFile);
		assertEquals("decrypted text of secret.scp2", tab.textArea.getText());
		assertFalse("just opened: nothing to save", tab.unsaved);
	}

	@Test
	public void encryptedFileIsNeverShownAsText() throws Exception {
		File f = write("again.scp2", "ENC:garbage");
		int before = editor.mainTabList.getTabCount();
		final TabTextEditorObject[] t = new TabTextEditorObject[1];
		SwingUtilities.invokeAndWait(() -> t[0] = editor.addTab(f));
		assertNull("addTab refuses the encrypted bytes", t[0]);
		assertEquals(before, editor.mainTabList.getTabCount());
		assertEquals(0, opened.get());
	}

	@Test
	public void encryptedSaveGoesThroughTheBindingNeverPlain() throws Exception {
		File f = write("s.scp2", "ENC:garbage");
		TabTextEditorObject tab = open(f);
		tab.textArea.setText("top secret MARKER");
		assertTrue(editor.writeTabToFile(tab, tab.textArea.getText(), tab.loadedFile));
		assertEquals(1, lastBinding.writes.size());
		String onDisk = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
		assertFalse("the plain text never reaches the file", onDisk.contains("top secret"));
		assertTrue(onDisk.startsWith("ENC:"));
	}

	@Test
	public void failedEncryptedSaveReportsFalseAndKeepsTheFile() throws Exception {
		File f = write("f.scp2", "ENC:original");
		TabTextEditorObject tab = open(f);
		lastBinding.failNext = true;
		assertFalse(editor.writeTabToFile(tab, "x", f));
		assertEquals("ENC:original", new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
	}

	@Test
	public void saveAllUnsavedWritesEncryptedTabsEncrypted() throws Exception {
		File f = write("all.scp2", "ENC:garbage");
		TabTextEditorObject tab = open(f);
		tab.textArea.setText("changed secret");
		tab.unsaved = true;
		assertTrue(editor.saveAllUnsavedExisting());
		assertEquals(1, lastBinding.writes.size());
		assertEquals("changed secret", lastBinding.writes.get(0));
		assertFalse(tab.unsaved);
		assertFalse(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8).contains("changed secret"));
	}

	@Test
	public void reloadFromDiskIsRefusedForEncryptedTabs() throws Exception {
		File f = write("r.scp2", "ENC:garbage");
		TabTextEditorObject tab = open(f);
		tab.textArea.setText("edited");
		SwingUtilities.invokeAndWait(() -> editor.reloadCurrentTab(false));
		assertEquals("the decrypted text stays", "edited", tab.textArea.getText());
		assertEquals(1, warnings.size());
	}

	@Test
	public void tempFileOfAnEncryptedTabIsNeverWritten() throws Exception {
		File f = write("t.scp2", "ENC:garbage");
		TabTextEditorObject tab = open(f);
		try {
			tab.saveTempFile();
			org.junit.Assert.fail("plain text must not go to the temp folder");
		} catch (IOException expected) {
			// refused
		}
	}

	@Test
	public void closingTheTabWipesTheSecrets() throws Exception {
		File f = write("c.scp2", "ENC:garbage");
		open(f);
		FakeBinding b = lastBinding;
		final TabTextEditorObject tab = editor.loadedTabObj;
		SwingUtilities.invokeAndWait(() -> tab.closeEncrypted());
		assertTrue(b.closed);
		assertFalse(tab.isEncrypted());
	}

	@Test
	public void cancelledOpenOpensNothing() throws Exception {
		support = new EncryptedFileSupport() {
			@Override
			public boolean isEncrypted(File file) {
				return true;
			}

			@Override
			public Opened open(File file) {
				return null; // cancelled by user
			}
		};
		// the owner returns the field value at call time: rebuild the editor with the new support
		setUp2();
		File f = write("n.scp2", "ENC:garbage");
		int before = editor.mainTabList.getTabCount();
		SwingUtilities.invokeAndWait(() -> editor.loadFile(f));
		assertEquals(before, editor.mainTabList.getTabCount());
	}

	private void setUp2() throws Exception {
		final JTextField target = new JTextField();
		final EncryptedFileSupport s = support;
		ITabbedEditorOwner owner = stub(ITabbedEditorOwner.class, (proxy, method, args) -> {
			switch (method.getName()) {
			case "getNewFileStringTarget":
				return target;
			case "getEncryptedFileSupport":
				return s;
			case "getShortcutManager":
				return stub(various.common.light.om.ICommonShortcuts.class, null);
			default:
				return NO_ANSWER;
			}
		});
		SwingUtilities.invokeAndWait(() -> {
			editor = new CustomTabbedEditor(new javax.swing.JFrame(), conf(), owner);
			editor.addTab(null);
		});
	}
}
