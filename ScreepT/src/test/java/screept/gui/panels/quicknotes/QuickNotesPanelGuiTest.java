/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.gui.panels.quicknotes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static screept.gui.GuiTestSupport.call;
import static screept.gui.GuiTestSupport.edt;
import static screept.gui.GuiTestSupport.flushEdt;
import static screept.gui.GuiTestSupport.get;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import screept.gui.GuiTestSupport;
import screept.gui.GuiTestSupport.Fixture;

/**
 * The real quick notes panel on a workspace folder of a temporary directory: notes are added, edited, searched, tagged, moved,
 * deleted and undone through the same fields, buttons and key bindings the user touches. Windows are never shown and the
 * flows that open dialogs or file choosers are not driven.
 */
public class QuickNotesPanelGuiTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static Fixture fx;
	private QuickNotesWorkspaces workspaces;
	private QuickNotesPanel panel;
	private final AtomicInteger closed = new AtomicInteger();

	@BeforeClass
	public static void openFixture() throws Exception {
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		fx = GuiTestSupport.open(Files.createTempDirectory("qnpanel").toFile());
	}

	@AfterClass
	public static void closeFixture() throws Exception {
		if (fx != null) {
			fx.close();
		}
	}

	@Before
	public void openPanel() throws Exception {
		workspaces = new QuickNotesWorkspaces(tmp.newFolder("notes"));
		panel = edt(new Callable<QuickNotesPanel>() {
			@Override
			public QuickNotesPanel call() {
				QuickNotesPanel p = new QuickNotesPanel(workspaces, new Runnable() {
					@Override
					public void run() {
						closed.incrementAndGet();
					}
				});
				p.setSize(380, 640);
				p.doLayout();
				return p;
			}
		});
	}

	@After
	public void flush() {
		workspaces.flushAll();
	}

	// ------------------------------------------------------------------ plumbing

	/** Runs a key binding of a component (the keys of the panel are bound by name) */
	private static void key(final JComponent c, final String name) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				Action a = c.getActionMap().get(name);
				assertNotNull("binding " + name, a);
				a.actionPerformed(new ActionEvent(c, ActionEvent.ACTION_PERFORMED, name));
			}
		});
		flushEdt();
	}

	private JTextField field(String name) throws Exception {
		return (JTextField) get(panel, name);
	}

	private static void write(final JTextField f, final String text) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				f.setText(text);
			}
		});
	}

	private void add(String text) throws Exception {
		write(field("addField"), text);
		key(field("addField"), "qn-add");
	}

	/** Note cards on screen, in order */
	private List<JComponent> cards() throws Exception {
		JComponent list = (JComponent) get(panel, "listPanel");
		List<JComponent> out = new ArrayList<JComponent>();
		for (Component c : list.getComponents()) {
			if (c.getClass().getSimpleName().equals("NoteCard")) {
				out.add((JComponent) c);
			}
		}
		return out;
	}

	private List<String> shownTexts() throws Exception {
		List<String> out = new ArrayList<String>();
		for (JComponent card : cards()) {
			out.add(((JTextArea) get(card, "area")).getText());
		}
		return out;
	}

	private JTextArea area(int index) throws Exception {
		return (JTextArea) get(cards().get(index), "area");
	}

	private String counter() throws Exception {
		return ((JLabel) get(panel, "counterLbl")).getText();
	}

	private static void click(final AbstractButton b) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				b.doClick();
			}
		});
		flushEdt();
	}

	private AbstractButton button(String name) throws Exception {
		return (AbstractButton) get(panel, name);
	}

	private static void mouse(final Component c, final int id, final int x, final int y) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				c.dispatchEvent(new MouseEvent(c, id, System.currentTimeMillis(), 0, x, y, 1, false));
			}
		});
	}

	/** A mouse event of the left button, as the card receives it while the user drags */
	private static void drag(final Component c, final int id, final int x, final int y) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				int modifiers = id == MouseEvent.MOUSE_RELEASED ? 0 : java.awt.event.InputEvent.BUTTON1_MASK;
				c.dispatchEvent(new MouseEvent(c, id, System.currentTimeMillis(), modifiers, x, y, 1, false, MouseEvent.BUTTON1));
			}
		});
		flushEdt();
	}

	/** Y, in the coordinates of card `from`, of a point at the given fraction of the height of card `to` */
	private int yOver(int from, int to, double fraction) throws Exception {
		List<JComponent> cards = cards();
		JComponent target = cards.get(to);
		return target.getY() + (int) (target.getHeight() * fraction) - cards.get(from).getY();
	}

	private boolean dragging() throws Exception {
		return (Boolean) call(panel, "isDragging");
	}

	private static void paint(final Component c) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				if (c.getWidth() <= 0 || c.getHeight() <= 0) {
					c.setSize(360, Math.max(30, c.getPreferredSize().height));
				}
				c.doLayout();
				c.paint(new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB).createGraphics());
			}
		});
	}

	private static void resize(final JComponent c, final int width) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				c.setSize(width, c.getPreferredSize().height + 2);
			}
		});
	}

	// ------------------------------------------------------------------ add, edit, done

	@Test
	public void notesAreAddedOnTopTrimmedAndBlankOnesIgnored() throws Exception {
		add("  first  ");
		add("second");
		assertEquals("the field is emptied after each note", "", field("addField").getText());
		add("   ");
		assertEquals(Arrays.asList("second", "first"), shownTexts());
		assertEquals("2 to do · 0 done", counter());
		assertEquals(2, workspaces.current().getNotes().size());
	}

	@Test
	public void editingTheTextUpdatesTheNoteAndSavesIt() throws Exception {
		add("draft");
		final JTextArea a = area(0);
		edt(new Runnable() {
			@Override
			public void run() {
				a.setText("final text");
			}
		});
		assertEquals("final text", workspaces.current().getNotes().get(0).getText());
		workspaces.current().flush();
		assertTrue(new String(Files.readAllBytes(workspaces.current().getFile().toPath()), "UTF-8").contains("final text"));
	}

	@Test
	public void doneNotesMoveToTheCollapsibleCompletedSection() throws Exception {
		add("a");
		add("b"); // b on top
		click((AbstractButton) get(cards().get(0), "check"));
		assertEquals("1 to do · 1 done", counter());
		assertTrue(workspaces.current().getNotes().get(0).isDone());
		assertEquals("done note is listed after the open ones", Arrays.asList("a", "b"), shownTexts());
		JComponent list = (JComponent) get(panel, "listPanel");
		JLabel header = null;
		for (Component c : list.getComponents()) {
			if (c instanceof JLabel && ((JLabel) c).getText().contains("Completed")) {
				header = (JLabel) c;
			}
		}
		assertNotNull(header);
		assertTrue(header.getText().contains("Completed · 1"));
		mouse(header, MouseEvent.MOUSE_CLICKED, 2, 2);
		flushEdt();
		assertTrue(workspaces.current().isCompletedCollapsed());
		assertEquals(Arrays.asList("a"), shownTexts());
		// searching shows the completed notes even when collapsed
		write(field("searchField"), "b");
		assertEquals(Arrays.asList("b"), shownTexts());
	}

	@Test
	public void ctrlEnterTogglesDoneAndEnterOpensANewNoteBelow() throws Exception {
		add("one");
		key(area(0), "qn-toggle");
		assertTrue(workspaces.current().getNotes().get(0).isDone());
		key(area(0), "qn-toggle");
		assertFalse(workspaces.current().getNotes().get(0).isDone());
		key(area(0), "qn-new-below");
		assertEquals("a blank note follows", 2, workspaces.current().getNotes().size());
		assertTrue(workspaces.current().getNotes().get(1).isBlank());
		assertEquals(2, cards().size());
	}

	@Test
	public void enterOnADoneNoteGoesBackToTheAddField() throws Exception {
		add("one");
		key(area(0), "qn-toggle");
		key(area(0), "qn-new-below");
		assertEquals("no blank note is created below a done one", 1, workspaces.current().getNotes().size());
	}

	@Test
	public void altUpDownMovesTheNote() throws Exception {
		add("c");
		add("b");
		add("a"); // a, b, c
		key(area(0), "qn-down");
		assertEquals(Arrays.asList("b", "a", "c"), shownTexts());
		key(area(2), "qn-up");
		assertEquals(Arrays.asList("b", "c", "a"), shownTexts());
		key(area(0), "qn-up"); // already first: nothing changes
		assertEquals(Arrays.asList("b", "c", "a"), shownTexts());
	}

	@Test
	public void backspaceOnAnEmptyNoteDeletesItAndUndoBringsItBack() throws Exception {
		add("keep");
		add("gone");
		final JTextArea a = area(0);
		edt(new Runnable() {
			@Override
			public void run() {
				a.setText("");
			}
		});
		key(a, "qn-backspace");
		assertEquals(Arrays.asList("keep"), shownTexts());
		assertTrue(button("undoBtn").isVisible());
		click(button("undoBtn"));
		assertFalse(button("undoBtn").isVisible());
		assertEquals(2, workspaces.current().getNotes().size());
	}

	@Test
	public void backspaceOnATextNoteJustDeletesACharacter() throws Exception {
		add("abc");
		final JTextArea a = area(0);
		edt(new Runnable() {
			@Override
			public void run() {
				a.setCaretPosition(3);
			}
		});
		key(a, "qn-backspace");
		assertEquals("ab", workspaces.current().getNotes().get(0).getText());
	}

	// ------------------------------------------------------------------ drag and drop

	private void threeNotes() throws Exception {
		add("first");
		add("second");
		add("third");
		assertEquals(Arrays.asList("third", "second", "first"), shownTexts());
		layoutNow();
	}

	/** The window is never shown: lay the panel out as a screen would */
	private void layoutNow() throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				panel.setSize(380, 640);
				layoutTree(panel);
			}
		});
	}

	private static void layoutTree(java.awt.Container c) {
		c.doLayout();
		for (Component k : c.getComponents()) {
			if (k instanceof java.awt.Container)
				layoutTree((java.awt.Container) k);
		}
	}

	@Test
	public void dragAndDropMovesACardBelowAnother() throws Exception {
		threeNotes();
		JComponent card = cards().get(0);
		drag(card, MouseEvent.MOUSE_PRESSED, 8, card.getHeight() / 2);
		assertTrue("the grip starts a drag", dragging());
		JComponent list = (JComponent) get(panel, "listPanel");
		drag(card, MouseEvent.MOUSE_DRAGGED, 8, yOver(0, 2, 0.8));
		Rectangle mark = (Rectangle) call(list, "getDropMark");
		assertNotNull("a drop mark is shown while dragging", mark);
		assertTrue("the mark is under the last card", mark.y >= cards().get(2).getY() + cards().get(2).getHeight());
		drag(card, MouseEvent.MOUSE_RELEASED, 8, yOver(0, 2, 0.8));
		assertFalse(dragging());
		layoutNow();
		assertEquals(Arrays.asList("second", "first", "third"), shownTexts());
		assertEquals("the new order is in the store", Arrays.asList("second", "first", "third"), noteTexts());
		assertFalse("the mark is gone", (Boolean) call(list, "isDragOverlayShown"));
	}

	@Test
	public void dragAndDropMovesACardAboveAnother() throws Exception {
		threeNotes();
		JComponent card = cards().get(2);
		drag(card, MouseEvent.MOUSE_PRESSED, 6, card.getHeight() / 2);
		drag(card, MouseEvent.MOUSE_DRAGGED, 6, yOver(2, 0, 0.2));
		Rectangle mark = (Rectangle) call(get(panel, "listPanel"), "getDropMark");
		assertTrue("the mark is above the first card", mark.y <= cards().get(0).getY());
		drag(card, MouseEvent.MOUSE_RELEASED, 6, yOver(2, 0, 0.2));
		assertEquals(Arrays.asList("first", "third", "second"), shownTexts());
	}

	@Test
	public void dragFromOutsideTheGripOrDroppedWhereItWasChangesNothing() throws Exception {
		threeNotes();
		JComponent card = cards().get(0);
		drag(card, MouseEvent.MOUSE_PRESSED, 120, card.getHeight() / 2);
		assertFalse("only the grip strip starts a drag", dragging());
		drag(card, MouseEvent.MOUSE_RELEASED, 120, card.getHeight() / 2);

		drag(card, MouseEvent.MOUSE_PRESSED, 8, card.getHeight() / 2);
		assertTrue(dragging());
		drag(card, MouseEvent.MOUSE_DRAGGED, 8, card.getHeight() / 2 + 2);
		drag(card, MouseEvent.MOUSE_RELEASED, 8, card.getHeight() / 2 + 2);
		assertEquals(Arrays.asList("third", "second", "first"), shownTexts());
	}

	@Test
	public void gripAndGhostArePaintedWhileDragging() throws Exception {
		threeNotes();
		final JComponent list = (JComponent) get(panel, "listPanel");
		JComponent card = cards().get(1);
		drag(card, MouseEvent.MOUSE_PRESSED, 8, card.getHeight() / 2);
		drag(card, MouseEvent.MOUSE_DRAGGED, 40, yOver(1, 0, 0.1));
		final BufferedImage[] shot = new BufferedImage[1];
		edt(new Runnable() {
			@Override
			public void run() {
				list.setSize(380, Math.max(list.getHeight(), list.getPreferredSize().height));
				shot[0] = new BufferedImage(list.getWidth(), list.getHeight(), BufferedImage.TYPE_INT_ARGB);
				list.paint(shot[0].createGraphics());
			}
		});
		java.awt.Color accent = screept.gui.style.CardPalette.current().accent;
		int hits = 0;
		for (int y = 0; y < shot[0].getHeight(); y++)
			for (int x = 0; x < shot[0].getWidth(); x++)
				if ((shot[0].getRGB(x, y) & 0xFFFFFF) == (accent.getRGB() & 0xFFFFFF))
					hits++;
		assertTrue("the mark and the border of the ghost use the accent color: " + hits, hits > 300);
		if (new File("target/qn.dump").exists()) {
			javax.imageio.ImageIO.write(shot[0], "png", new File("target/qn_drag.png"));
		}
		drag(card, MouseEvent.MOUSE_RELEASED, 40, yOver(1, 0, 0.1));
		assertEquals(Arrays.asList("second", "third", "first"), shownTexts());
	}

	private List<String> noteTexts() throws Exception {
		List<String> out = new ArrayList<String>();
		for (QuickNote n : workspaces.current().getNotes())
			out.add(n.getText());
		return out;
	}

	@Test
	public void deleteButtonRemovesAndUndoRestoresAtTheSamePlace() throws Exception {
		add("c");
		add("b");
		add("a");
		click((AbstractButton) get(cards().get(1), "deleteBtn"));
		assertEquals(Arrays.asList("a", "c"), shownTexts());
		assertEquals("Note deleted", ((JLabel) get(panel, "footerLbl")).getText());
		click(button("undoBtn"));
		assertEquals(Arrays.asList("a", "b", "c"), shownTexts());
		assertTrue(((JLabel) get(panel, "footerLbl")).getText().contains("Enter add"));
	}

	@Test
	public void hoverShowsTheCardButtons() throws Exception {
		add("note");
		JComponent card = cards().get(0);
		AbstractButton delete = (AbstractButton) get(card, "deleteBtn");
		assertFalse(delete.isVisible());
		mouse(card, MouseEvent.MOUSE_ENTERED, 2, 2);
		assertTrue(delete.isVisible());
		assertTrue(((AbstractButton) get(card, "tagBtn")).isVisible());
		paint(card);
	}

	// ------------------------------------------------------------------ search and filters

	@Test
	public void searchMatchesTextAndWildcardsAndExplainsWhenEmpty() throws Exception {
		add("buy milk");
		add("call mum");
		add("milk shake recipe");
		write(field("searchField"), "milk");
		assertEquals(Arrays.asList("milk shake recipe", "buy milk"), shownTexts());
		write(field("searchField"), "milk shake");
		assertEquals(Arrays.asList("milk shake recipe"), shownTexts());
		write(field("searchField"), "c*l");
		assertEquals(Arrays.asList("call mum"), shownTexts());
		write(field("searchField"), "zzz");
		assertTrue(shownTexts().isEmpty());
		JComponent list = (JComponent) get(panel, "listPanel");
		assertEquals("No notes match \"zzz\"", ((JLabel) list.getComponent(0)).getText());
		// Escape on a filled search empties it, on an empty one hides it
		key(field("searchField"), "qn-search-esc");
		assertEquals("", field("searchField").getText());
		assertEquals(3, cards().size());
		call(panel, "showSearch");
		assertTrue(field("searchField").isVisible());
		key(field("searchField"), "qn-search-esc");
		assertFalse(field("searchField").isVisible());
	}

	@Test
	public void searchAlsoMatchesTags() throws Exception {
		add("plain");
		add("tagged");
		workspaces.current().getNotes().get(0).setTags(Arrays.asList("urgent"));
		write(field("searchField"), "urg");
		assertEquals(Arrays.asList("tagged"), shownTexts());
		write(field("searchField"), "urgent plain");
		assertTrue("every word must match", shownTexts().isEmpty());
	}

	@Test
	public void emptyMessagesExplainWhyNothingIsShown() throws Exception {
		JComponent list = (JComponent) get(panel, "listPanel");
		assertTrue(((JLabel) list.getComponent(0)).getText().contains("Nothing here yet"));
		click(button("todoToggle"));
		click(button("doneToggle"));
		assertEquals("Turn on To do or Done to see the notes", ((JLabel) list.getComponent(0)).getText());
		click(button("doneToggle"));
		assertEquals("No completed notes", ((JLabel) list.getComponent(0)).getText());
		click(button("todoToggle"));
		click(button("doneToggle"));
		assertEquals("No notes to do", ((JLabel) list.getComponent(0)).getText());
	}

	@Test
	public void toggleHidesTheNotesOfTheOtherState() throws Exception {
		add("open");
		add("finished");
		key(area(0), "qn-toggle"); // "finished" done
		click(button("todoToggle"));
		assertEquals(Arrays.asList("finished"), shownTexts());
		click(button("todoToggle"));
		click(button("doneToggle"));
		assertEquals(Arrays.asList("open"), shownTexts());
	}

	// ------------------------------------------------------------------ tags

	@Test
	public void newTagsExistWithoutNotesAndCanBePickedForANote() throws Exception {
		add("note");
		call(panel, "createTags", "Work, home office");
		assertEquals(Arrays.asList("home-office", "work"), call(panel, "knownTags"));
		JComponent card = cards().get(0);
		call(card, "editTags");
		@SuppressWarnings("unchecked")
		final JComboBox<String> pick = (JComboBox<String>) get(card, "tagPick");
		assertTrue(pick.isVisible());
		assertEquals(2, pick.getItemCount());
		edt(new Runnable() {
			@Override
			public void run() {
				pick.setSelectedItem("work");
			}
		});
		flushEdt();
		assertEquals(Arrays.asList("work"), workspaces.current().getNotes().get(0).getTags());
		call(panel, "createTags", " , "); // nothing to create
		assertEquals(2, ((List<?>) call(panel, "knownTags")).size());
	}

	@Test
	public void tagButtonDoesNothingWhenNoOtherTagExists() throws Exception {
		add("note");
		JComponent card = cards().get(0);
		call(card, "editTags");
		assertFalse(((JComboBox<?>) get(card, "tagPick")).isVisible());
	}

	@Test
	public void tagFilterShowsNotesWithAnyChosenTagAndTheStripClicksWork() throws Exception {
		add("c");
		add("b");
		add("a");
		workspaces.current().getNotes().get(0).setTags(Arrays.asList("x"));
		workspaces.current().getNotes().get(2).setTags(Arrays.asList("y", "x"));
		call(panel, "rebuild", (Object) null);
		final JComponent strip = (JComponent) get(panel, "tagStrip");
		resize(strip, 360);
		Map<?, ?> counts = (Map<?, ?>) get(strip, "counts");
		assertEquals(2, counts.size());
		assertEquals(2, counts.get("x"));
		int y = strip.getHeight() / 2;
		int hit = -1;
		for (int x = 0; x < strip.getWidth() && hit < 0; x += 4) {
			Object slot = call(strip, "slotAt", new Point(x, y));
			if (slot != null && "x".equals(get(slot, "tag"))) {
				hit = x;
			}
		}
		assertTrue("chip #x found", hit >= 0);
		assertTrue(strip.getToolTipText(new MouseEvent(strip, MouseEvent.MOUSE_MOVED, 0, 0, hit, y, 0, false)).contains("#x"));
		mouse(strip, MouseEvent.MOUSE_MOVED, hit, y);
		mouse(strip, MouseEvent.MOUSE_CLICKED, hit, y);
		flushEdt();
		assertEquals(Arrays.asList("a", "c"), shownTexts());
		mouse(strip, MouseEvent.MOUSE_CLICKED, 3, y); // "All" clears the filter
		flushEdt();
		assertEquals(3, cards().size());
		paint(strip);
		paint(cards().get(0));
	}

	@Test
	public void narrowStripHidesTagsBehindTheArrowAndTheMenuListsThemAll() throws Exception {
		add("note");
		workspaces.current().getNotes().get(0).setTags(Arrays.asList("one", "two", "three", "four", "five"));
		call(panel, "rebuild", (Object) null);
		final JComponent strip = (JComponent) get(panel, "tagStrip");
		resize(strip, 120);
		List<?> slots = (List<?>) call(strip, "layout", 120);
		Object more = slots.get(slots.size() - 1);
		assertTrue((Boolean) get(more, "more"));
		assertTrue(((String) get(more, "label")).startsWith("+"));
		paint(strip);
		assertTrue("the arrow says how many tags are hidden", ((String) get(more, "label")).startsWith("+") && ((String) get(more, "tip")).contains("(5)"));
	}

	@Test
	public void tagLineRemovesATagOnClickAndExpandsWhenTooManyForOneLine() throws Exception {
		add("note");
		workspaces.current().getNotes().get(0).setTags(Arrays.asList("alpha", "bravo", "charlie", "delta", "echo", "foxtrot"));
		call(panel, "rebuild", (Object) null);
		final JComponent line = (JComponent) get(cards().get(0), "tagLine");
		assertTrue(line.isVisible());
		resize(line, 150);
		List<?> chips = (List<?>) call(line, "layout", 150);
		Object last = chips.get(chips.size() - 1);
		assertTrue("a +N control hides the tags that do not fit", (Boolean) get(last, "control"));
		assertTrue(((String) get(last, "label")).startsWith("+"));
		Rectangle r = (Rectangle) get(last, "bounds");
		mouse(line, MouseEvent.MOUSE_MOVED, r.x + 1, 2);
		mouse(line, MouseEvent.MOUSE_CLICKED, r.x + 1, 2);
		flushEdt();
		// expanded: one chip per line plus "less"
		final JComponent line2 = (JComponent) get(cards().get(0), "tagLine");
		resize(line2, 150);
		List<?> expanded = (List<?>) call(line2, "layout", 150);
		assertEquals(7, expanded.size());
		Rectangle first = (Rectangle) get(expanded.get(0), "bounds");
		mouse(line2, MouseEvent.MOUSE_MOVED, first.x + 2, first.y + 2);
		paint(line2); // hovered chip is drawn as "Remove"
		mouse(line2, MouseEvent.MOUSE_CLICKED, first.x + 2, first.y + 2);
		flushEdt();
		assertEquals(Arrays.asList("bravo", "charlie", "delta", "echo", "foxtrot"), workspaces.current().getNotes().get(0).getTags());
		mouse(line2, MouseEvent.MOUSE_EXITED, 0, 0);
	}

	// ------------------------------------------------------------------ workspaces, close

	@Test
	public void workspaceComboSwitchesTheNotesShown() throws Exception {
		add("main note");
		final String other = workspaces.create("Shopping");
		workspaces.setCurrent(QuickNotesWorkspaces.MAIN_ID);
		workspaces.store(other).addFirst("eggs");
		call(panel, "refreshWorkspaceCombo");
		@SuppressWarnings("unchecked")
		final JComboBox<Object> box = (JComboBox<Object>) get(panel, "workspaceBox");
		assertEquals(2, box.getItemCount());
		edt(new Runnable() {
			@Override
			public void run() {
				box.setSelectedIndex(1);
			}
		});
		flushEdt();
		assertEquals(other, workspaces.currentId());
		assertEquals(Arrays.asList("eggs"), shownTexts());
		assertTrue("the main notes were saved when leaving it", new File(tmp.getRoot(), "notes/" + QuickNotesStore.DEFAULT_FILE.getName()).isFile());
	}

	@Test
	public void escapeAndCloseButtonCallTheCloseAction() throws Exception {
		key(panel, "qn-close");
		assertEquals(1, closed.get());
		click(button("closeBtn"));
		assertEquals(2, closed.get());
		call(panel, "showSearch"); // with the search open, Escape closes the search first
		key(panel, "qn-close");
		assertEquals(2, closed.get());
		assertFalse(field("searchField").isVisible());
	}

	@Test
	public void searchButtonAndShortcutShowAndHideTheField() throws Exception {
		click(button("searchBtn"));
		assertTrue(field("searchField").isVisible());
		click(button("searchBtn"));
		assertFalse(field("searchField").isVisible());
		key(panel, "qn-find");
		assertTrue(field("searchField").isVisible());
	}

	@Test
	public void paletteChangeAndPaintingKeepTheNotes() throws Exception {
		add("note one");
		key(area(0), "qn-toggle");
		add("note two");
		edt(new Runnable() {
			@Override
			public void run() {
				panel.applyPalette();
			}
		});
		paint(panel);
		assertEquals(2, cards().size());
	}
}
