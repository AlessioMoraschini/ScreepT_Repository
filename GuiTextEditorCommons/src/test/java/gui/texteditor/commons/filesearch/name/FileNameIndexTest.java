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
package gui.texteditor.commons.filesearch.name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** The in-memory index of file names: walk, skipped folders, wildcards, ordering and limit */
public class FileNameIndexTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private File root;

	@Before
	public void tree() throws IOException {
		root = tmp.newFolder("root");
		touch("Readme.md");
		touch("src/Main.java");
		touch("src/MainTest.java");
		touch("src/util/Helper.java");
		touch("src/util/helper_notes.txt");
		touch("docs/main-guide.md");
		touch("docs/zzmain.txt");
		touch(".git/config");
		touch("node_modules/lib/index.js");
		touch(".idea/workspace.xml");
		touch("sub/.git/hidden.txt"); // a skipped folder at any depth
		touch("café/Menu.txt");
	}

	private void touch(String path) throws IOException {
		File f = new File(root, path);
		assertTrue(f.getParentFile().isDirectory() || f.getParentFile().mkdirs());
		assertTrue(f.createNewFile());
	}

	private FileNameIndex built() {
		FileNameIndex index = new FileNameIndex(root);
		index.build();
		return index;
	}

	private static List<String> names(List<File> files) {
		List<String> out = new ArrayList<String>();
		for (File f : files) {
			out.add(f.getName());
		}
		return out;
	}

	private static List<String> search(FileNameIndex index, String text, int limit) {
		return names(index.search(text, limit, () -> false));
	}

	private static List<String> sorted(List<String> list) {
		List<String> copy = new ArrayList<String>(list);
		Collections.sort(copy);
		return copy;
	}

	@Test
	public void buildSkipsGeneratedFolders() {
		FileNameIndex index = built();
		assertTrue(index.isReady());
		assertFalse(index.isTruncated());
		assertEquals(8, index.size()); // .git, node_modules, .idea and sub/.git are not walked
		assertTrue(index.builtAt > 0);
	}

	@Test
	public void substringSearchIsCaseInsensitiveAndPrefixMatchesComeFirst() {
		FileNameIndex index = built();
		List<String> found = search(index, "MAIN", 100);
		assertEquals(4, found.size());
		// names starting with the text first, then the others
		assertEquals("zzmain.txt", found.get(3));
		assertEquals(Arrays.asList("Main.java", "MainTest.java", "main-guide.md"), sorted(found.subList(0, 3)));
	}

	@Test
	public void wildcardsAreAnchoredToTheWholeName() {
		FileNameIndex index = built();
		assertEquals(Arrays.asList("Helper.java", "Main.java", "MainTest.java"), sorted(search(index, "*.java", 100)));
		assertEquals(Arrays.asList("Main.java"), search(index, "m?in.java", 100));
		assertEquals(Arrays.asList("Main.java", "MainTest.java", "main-guide.md"), sorted(search(index, "main*", 100)));
		assertTrue(search(index, "ain*", 100).isEmpty()); // anchored: "ain" is not the start of any name
		assertEquals(Arrays.asList("Main.java"), search(index, "Main.jav?", 100));
	}

	@Test
	public void regexCharactersInTheTextAreLiteral() {
		FileNameIndex index = built();
		assertEquals(Arrays.asList("Main.java"), search(index, "main.java", 100));
		assertTrue(search(index, "main(java", 100).isEmpty());
		assertTrue(search(index, "[a-z]", 100).isEmpty());
		assertTrue(search(index, "+", 100).isEmpty());
		assertEquals(Arrays.asList("helper_notes.txt"), search(index, "_notes", 100));
		assertTrue("the dot is not a wildcard", search(index, "main.jaXa", 100).isEmpty());
	}

	@Test
	public void starAloneListsEverythingAndBlankToo() {
		FileNameIndex index = built();
		assertEquals(8, search(index, "*", 100).size());
		assertEquals(8, search(index, "   ", 100).size());
		assertEquals(8, search(index, "", 100).size());
	}

	@Test
	public void limitCapsTheResult() {
		FileNameIndex index = built();
		assertEquals(2, search(index, "*", 2).size());
		assertEquals(1, search(index, "", 1).size());
		assertEquals(2, search(index, "e", 2).size());
	}

	@Test
	public void nonAsciiNamesAreFoundIgnoringCase() {
		FileNameIndex index = built();
		assertEquals(Arrays.asList("Menu.txt"), search(index, "MENU", 10));
		assertEquals(Arrays.asList("Menu.txt"), search(index, "m*u.txt", 10));
	}

	@Test
	public void supersededSearchReturnsNull() {
		FileNameIndex index = built();
		assertNull(index.search("main", 10, () -> true));
	}

	@Test
	public void missingOrNullRootIsReadyAndEmpty() {
		FileNameIndex missing = new FileNameIndex(new File(root, "does-not-exist"));
		missing.build();
		assertTrue(missing.isReady());
		assertEquals(0, missing.size());
		FileNameIndex none = new FileNameIndex(null);
		none.build();
		assertTrue(none.isReady());
		assertTrue(none.search("x", 10, () -> false).isEmpty());
	}

	@Test
	public void rootThatIsAFileIsEmpty() {
		FileNameIndex index = new FileNameIndex(new File(root, "Readme.md"));
		index.build();
		assertEquals(0, index.size());
	}

	@Test
	public void cancelledBuildIsNotReady() {
		FileNameIndex index = new FileNameIndex(root);
		index.cancel();
		index.build();
		assertFalse(index.isReady());
		assertEquals(0, index.size());
	}
}
