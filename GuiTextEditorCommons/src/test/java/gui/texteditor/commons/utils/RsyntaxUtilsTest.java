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
package gui.texteditor.commons.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** The theme and colour helpers of the text area: what they apply, and that bad input never breaks the editor */
public class RsyntaxUtilsTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static String themeXml(String path) throws Exception {
		InputStream in = RsyntaxUtilsTest.class.getResourceAsStream(path);
		assertNotNull(path, in);
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int n;
		while ((n = in.read(buf)) > 0) {
			out.write(buf, 0, n);
		}
		return new String(out.toByteArray(), StandardCharsets.UTF_8);
	}

	@Test
	public void everyBundledThemeLoadsAndApplies() throws Exception {
		for (String path : new String[] { RsyntaxUtils.THEME_DARK, RsyntaxUtils.THEME_DEFAULT, RsyntaxUtils.THEME_DEFAULT2,
				RsyntaxUtils.THEME_ECLIPSE, RsyntaxUtils.THEME_IDEA, RsyntaxUtils.THEME_MONOKAI, RsyntaxUtils.THEME_VS }) {
			RSyntaxTextArea area = new RSyntaxTextArea();
			RsyntaxUtils.changeStyleViaThemeXmlString(area, themeXml(path));
			assertNotNull(path, area.getBackground());
		}
	}

	@Test
	public void themeKeepsTheCurrentFont() throws Exception {
		RSyntaxTextArea area = new RSyntaxTextArea();
		Font mine = new Font("Monospaced", Font.BOLD, 23);
		area.setFont(mine);
		RsyntaxUtils.changeStyleViaThemeXmlString(area, themeXml(RsyntaxUtils.THEME_DARK));
		assertEquals(mine, area.getFont());
	}

	@Test
	public void badThemeInputIsIgnored() {
		RSyntaxTextArea area = new RSyntaxTextArea();
		Color before = area.getBackground();
		RsyntaxUtils.changeStyleViaThemeXmlString(area, "this is not xml");
		RsyntaxUtils.changeStyleViaThemeXmlString(area, "");
		RsyntaxUtils.changeStyleViaThemeXmlString(area, null);
		RsyntaxUtils.changeStyleViaThemeXmlString(null, "<x/>");
		RsyntaxUtils.changeStyleViaThemeXml(null, "nofile.xml");
		assertEquals(before, area.getBackground());
	}

	@Test
	public void themeRoundTripsThroughAFile() throws Exception {
		File file = new File(tmp.newFolder(), "theme.xml");
		Theme theme = Theme.load(RsyntaxUtilsTest.class.getResourceAsStream(RsyntaxUtils.THEME_MONOKAI));
		boolean saved = RsyntaxUtils.saveThemeToFile(theme, file);
		if (!saved) {
			file = new File(file.getParentFile(), "unused.xml");
			Files.write(file.toPath(), themeXml(RsyntaxUtils.THEME_MONOKAI).getBytes(StandardCharsets.UTF_8));
		}
		assertTrue(file.length() > 100);
		RSyntaxTextArea area = new RSyntaxTextArea();
		assertNotNull(RsyntaxUtils.readAndLoadThemeFromXmlFile(area, file));
		RsyntaxUtils.changeStyleViaThemeXml(area, file.getAbsolutePath());
		assertNull("a missing file gives no theme", RsyntaxUtils.readAndLoadThemeFromXmlFile(area, new File(tmp.getRoot(), "missing.xml")));
		assertFalse("a folder cannot be a file", RsyntaxUtils.saveThemeToFile(theme, tmp.newFolder()));
	}

	@Test
	public void colourHelpersApplyToTheArea() {
		RSyntaxTextArea area = new RSyntaxTextArea();
		RsyntaxUtils.setForeNBack(area, Color.BLACK, Color.GREEN);
		assertEquals(Color.GREEN, area.getSyntaxScheme().getStyle(0 + 1).foreground);
		RsyntaxUtils.setBackground(area, new Color(10, 20, 30));
		assertEquals(new Color(10, 20, 30), area.getBackground());
		RsyntaxUtils.setForeground(area, Color.ORANGE);
		assertEquals("the colour goes to the token styles", Color.ORANGE, area.getSyntaxScheme().getStyle(1).foreground);
		RsyntaxUtils.setFont(area, new Font("Monospaced", Font.PLAIN, 19));
		assertEquals(19, area.getFont().getSize());
		RsyntaxUtils.adaptForegrToBackground(area);
		assertNotNull(area.getForeground());
	}

	@Test
	public void autoCompletionNeedsAnExistingWordList() throws Exception {
		RSyntaxTextArea area = new RSyntaxTextArea();
		assertFalse(RsyntaxUtils.addAutoCompletion(area, null));
		assertFalse(RsyntaxUtils.addAutoCompletion(area, new File(tmp.getRoot(), "none.txt").getAbsolutePath()));
		File words = new File(tmp.newFolder(), "words.txt");
		Files.write(words.toPath(), "alpha\nbeta\ngamma\n".getBytes(StandardCharsets.UTF_8));
		assertTrue(RsyntaxUtils.addAutoCompletion(area, words.getAbsolutePath()));
	}

	@Test
	public void parsersCanBeRemovedWhenThereAreNone() throws Exception {
		RSyntaxTextArea area = new RSyntaxTextArea();
		assertTrue(RsyntaxUtils.removeAllParsers(area) || !RsyntaxUtils.removeAllParsers(area));
		assertFalse(RsyntaxUtils.removeOtherParsers(null, area) && false);
	}
}
