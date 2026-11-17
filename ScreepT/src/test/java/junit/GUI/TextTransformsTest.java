package junit.GUI;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import screept.gui.panels.subpanels.texteditor.toolbar.TextTransforms;

public class TextTransformsTest {

	@Test
	public void sortKeepsFinalEolAndIgnoresCase() {
		assertEquals("a\nB\nc\n", TextTransforms.sortLines("c\nB\na\n", true));
		assertEquals("c\nB\na", TextTransforms.sortLines("a\nB\nc", false));
	}

	@Test
	public void sortKeepsCrLf() {
		assertEquals("a\r\nb\r\n", TextTransforms.sortLines("b\r\na\r\n", true));
	}

	@Test
	public void duplicatesKeepFirstOccurrence() {
		assertEquals("x\ny\nz", TextTransforms.removeDuplicateLines("x\ny\nx\nz\ny"));
	}

	@Test
	public void emptyLinesAreRemoved() {
		assertEquals("a\nb\n", TextTransforms.removeEmptyLines("a\n\n  \nb\n\n"));
	}

	@Test
	public void trailingSpacesAreTrimmed() {
		assertEquals("a\n b\n\nc", TextTransforms.trimTrailingSpaces("a  \n b\t\n\nc "));
	}

	@Test
	public void titleCase() {
		assertEquals("Hello World It's 2nd", TextTransforms.toTitleCase("hELLO wORLD it's 2nd"));
	}

	@Test
	public void emptyAndNullAreSafe() {
		assertEquals("", TextTransforms.sortLines("", true));
		assertEquals(null, TextTransforms.removeDuplicateLines(null));
	}
}
