package screept.gui.panels.quicknotes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * The quick notes are the user's own data: what is written must come back identical, a crash or a bad write must not
 * lose them, and tags, done state and order are part of the data. Everything runs on files of a temporary folder.
 */
public class QuickNotesStoreTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private File file() {
		return new File(tmp.getRoot(), "notes/quick_notes.md");
	}

	private static List<String> texts(QuickNotesStore s) {
		List<String> out = new ArrayList<String>();
		for (QuickNote n : s.getNotes()) {
			out.add(n.getText());
		}
		return out;
	}

	// ------------------------------------------------------------------ the note itself

	@Test
	public void tagsAreNormalisedDeduplicatedAndShortened() {
		assertEquals(Arrays.asList("work", "home-office", "ab"), QuickNote.parseTags("Work, Home  Office;  ; a/b ,work"));
		assertEquals(Arrays.asList("x23456789012345678901".substring(0, 20)), QuickNote.parseTags("X23456789012345678901"));
		assertTrue(QuickNote.parseTags(null).isEmpty());
		assertTrue(QuickNote.parseTags(" , ; ").isEmpty());
		assertEquals(Arrays.asList("già", "ok_1.2"), QuickNote.parseTags("Già, ok_1.2"));
	}

	@Test
	public void noteKeepsMinutesTagsAndRefusesNulls() {
		LocalDateTime t = LocalDateTime.of(2026, 11, 23, 22, 15, 42, 999);
		QuickNote n = new QuickNote(null, true, t, Arrays.asList("a", "b", "a"));
		assertEquals("", n.getText());
		assertTrue(n.isBlank());
		assertTrue(n.isDone());
		assertEquals("seconds are not kept", LocalDateTime.of(2026, 11, 23, 22, 15), n.getCreated());
		assertEquals(Arrays.asList("a", "b"), n.getTags());
		assertTrue(n.hasTag("b"));
		assertFalse(n.hasTag("c"));
		n.setTags(null);
		assertTrue(n.getTags().isEmpty());
		n.setText("  \n ");
		assertTrue(n.isBlank());
		n.setText("x");
		assertFalse(n.isBlank());
		try {
			n.getTags().add("z");
			org.junit.Assert.fail("tags are read only from outside");
		} catch (UnsupportedOperationException expected) {
			// ok
		}
	}

	// ------------------------------------------------------------------ persistence

	@Test
	public void everythingWrittenComesBackIdentical() {
		QuickNotesStore s = new QuickNotesStore(file());
		QuickNote a = s.addFirst("buy milk");
		QuickNote b = s.addFirst("call [Anna] - about \"the\" thing <b>x</b> & more");
		b.setTags(Arrays.asList("home", "urgent"));
		a.setDone(true);
		s.addKnownTag("home");
		s.addKnownTag("work");
		s.setCompletedCollapsed(true);
		s.flush();
		assertTrue(file().isFile());

		QuickNotesStore back = new QuickNotesStore(file());
		assertEquals(Arrays.asList("call [Anna] - about \"the\" thing <b>x</b> & more", "buy milk"), texts(back));
		assertEquals(Arrays.asList("home", "urgent"), back.getNotes().get(0).getTags());
		assertTrue(back.getNotes().get(1).isDone());
		assertFalse(back.getNotes().get(0).isDone());
		assertEquals(b.getCreated(), back.getNotes().get(0).getCreated());
		assertEquals(Arrays.asList("home", "work"), back.getKnownTags());
		assertTrue(back.isCompletedCollapsed());
	}

	@Test
	public void multiLineTextAndBlankNotes() {
		QuickNotesStore s = new QuickNotesStore(file());
		s.addFirst("first line\nsecond line\n\nfourth");
		s.addFirst("   ");
		s.addFirst("");
		s.flush();
		QuickNotesStore back = new QuickNotesStore(file());
		assertEquals("blank notes are not saved", 1, back.getNotes().size());
		assertEquals("first line\nsecond line\n\nfourth", back.getNotes().get(0).getText());
	}

	@Test
	public void changesAreWrittenByTheTimerWithoutAnExplicitFlush() throws Exception {
		QuickNotesStore s = new QuickNotesStore(file());
		s.addFirst("autosaved");
		long end = System.currentTimeMillis() + 5000;
		while (!file().exists() && System.currentTimeMillis() < end) {
			Thread.sleep(50);
		}
		assertTrue("saved within the delay", file().isFile());
		assertTrue(new String(Files.readAllBytes(file().toPath()), StandardCharsets.UTF_8).contains("autosaved"));
	}

	@Test
	public void previousVersionIsKeptAndUsedWhenTheMainFileIsGone() throws Exception {
		QuickNotesStore s = new QuickNotesStore(file());
		s.addFirst("one");
		s.flush();
		s.addFirst("two");
		s.flush();
		File bak = new File(file().getPath() + ".bak");
		assertTrue("the version before the last write is kept", bak.isFile());
		assertTrue(file().delete());
		QuickNotesStore recovered = new QuickNotesStore(file());
		assertEquals("only what the backup had", Arrays.asList("one"), texts(recovered));
	}

	@Test
	public void discardedStoreNeverWrites() {
		QuickNotesStore s = new QuickNotesStore(file());
		s.addFirst("never saved");
		s.discard();
		s.flush();
		assertFalse(file().exists());
	}

	@Test
	public void damagedLinesAreIgnoredAndTheRestLoads() throws Exception {
		File f = file();
		assertTrue(f.getParentFile().mkdirs());
		String text = "# ScreepT quick notes\r\n\r\nnot a note\r\n- [ ] good one  <!-- 2026-01-02T03:04 tags=a,b -->\r\n- [x] done one  <!-- 2026-01-02T03:05 -->\r\n- [?] broken\r\n";
		Files.write(f.toPath(), text.getBytes(StandardCharsets.UTF_8));
		QuickNotesStore s = new QuickNotesStore(f);
		assertEquals(Arrays.asList("good one", "done one"), texts(s));
		assertEquals(Arrays.asList("a", "b"), s.getNotes().get(0).getTags());
		assertEquals(LocalDateTime.of(2026, 1, 2, 3, 4), s.getNotes().get(0).getCreated());
		assertTrue(s.getNotes().get(1).isDone());
	}

	// ------------------------------------------------------------------ editing

	@Test
	public void orderingAndRemoval() {
		QuickNotesStore s = new QuickNotesStore(file());
		QuickNote c = s.addFirst("c");
		QuickNote b = s.addFirst("b");
		QuickNote a = s.addFirst("a");
		assertEquals(Arrays.asList("a", "b", "c"), texts(s));
		QuickNote after = s.addAfter(a);
		after.setText("a2");
		assertEquals(Arrays.asList("a", "a2", "b", "c"), texts(s));
		assertTrue(s.move(c, -1));
		assertEquals(Arrays.asList("a", "a2", "c", "b"), texts(s));
		assertFalse("cannot go above the first", s.move(a, -1));
		assertFalse("cannot go below the last", s.move(b, 1));
		s.insert(new QuickNote("z"), 99);
		s.insert(new QuickNote("y"), -5);
		assertEquals(Arrays.asList("y", "a", "a2", "c", "b", "z"), texts(s));
		s.remove(after);
		assertFalse(s.contains(after));
		assertEquals(5, s.getNotes().size());
	}

	@Test
	public void movingSkipsNotesWithAnotherDoneStateAndCompletedOnesCanBeCleared() {
		QuickNotesStore s = new QuickNotesStore(file());
		QuickNote c = s.addFirst("c");
		QuickNote done = s.addFirst("done");
		done.setDone(true);
		QuickNote a = s.addFirst("a");
		assertTrue(s.move(a, 1));
		assertEquals("a jumps over the done note, notes stay in their group", Arrays.asList("done", "c", "a"), texts(s));
		assertEquals(1, s.removeCompleted());
		assertEquals(Arrays.asList("c", "a"), texts(s));
		assertEquals(0, s.removeCompleted());
		assertNotEquals(c, a);
	}

	@Test
	public void knownTagsAreUniqueAndSorted() {
		QuickNotesStore s = new QuickNotesStore(file());
		assertTrue(s.addKnownTag("zeta"));
		assertTrue(s.addKnownTag("alpha"));
		assertFalse(s.addKnownTag("alpha"));
		assertFalse(s.addKnownTag(""));
		assertFalse(s.addKnownTag(null));
		assertEquals(Arrays.asList("alpha", "zeta"), s.getKnownTags());
	}

	// ------------------------------------------------------------------ backup and import

	@Test
	public void backupCanBeCountedAndImportedBack() throws Exception {
		QuickNotesStore s = new QuickNotesStore(file());
		s.addFirst("one");
		s.addFirst("two").setDone(true);
		s.addKnownTag("t");
		File folder = new File(tmp.getRoot(), "backups");
		File backup = s.backupTo(folder, QuickNotesStore.BACKUP_FILE_NAME);
		assertTrue(backup.isFile());
		assertEquals(2, QuickNotesStore.countNotesIn(backup));

		QuickNotesStore other = new QuickNotesStore(new File(tmp.getRoot(), "other/notes.md"));
		other.addFirst("will be replaced");
		other.importFrom(backup);
		assertEquals(Arrays.asList("two", "one"), texts(other));
		assertTrue(other.getNotes().get(0).isDone());
		assertEquals(Arrays.asList("t"), other.getKnownTags());

		File notABackup = new File(tmp.getRoot(), "random.txt");
		Files.write(notABackup.toPath(), "hello".getBytes(StandardCharsets.UTF_8));
		assertEquals(-1, QuickNotesStore.countNotesIn(notABackup));
		assertEquals(-1, QuickNotesStore.countNotesIn(new File(tmp.getRoot(), "missing.md")));
	}

	@Test
	public void backupFolderChoiceIsRemembered() {
		QuickNotesStore s = new QuickNotesStore(file());
		assertEquals(null, s.getBackupFolder());
		File chosen = new File(tmp.getRoot(), "my backups");
		s.setBackupFolder(chosen);
		assertEquals(chosen.getAbsoluteFile(), new QuickNotesStore(file()).getBackupFolder().getAbsoluteFile());
	}

	// ------------------------------------------------------------------ workspaces

	@Test
	public void workspacesKeepSeparateNotesAndRememberTheCurrentOne() throws Exception {
		File base = new File(tmp.getRoot(), "ws/quick_notes.md");
		QuickNotesWorkspaces w = new QuickNotesWorkspaces(base);
		assertTrue(w.ids().contains(QuickNotesWorkspaces.MAIN_ID));
		assertEquals(QuickNotesWorkspaces.MAIN_ID, w.currentId());
		w.current().addFirst("in main");

		String work = w.create("Work");
		assertTrue(w.ids().contains(work));
		assertEquals("Work", w.name(work));
		w.store(work).addFirst("in work");
		assertEquals("main is untouched", Arrays.asList("in main"), texts(w.store(QuickNotesWorkspaces.MAIN_ID)));
		assertEquals(Arrays.asList("in work"), texts(w.store(work)));

		w.setCurrent(work);
		assertEquals(work, w.currentId());
		assertTrue(w.rename(work, "Job"));
		assertEquals("Job", w.name(work));
		w.flushAll();

		QuickNotesWorkspaces again = new QuickNotesWorkspaces(base);
		assertEquals(work, again.currentId());
		assertEquals("Job", again.name(work));
		assertEquals(Arrays.asList("in work"), texts(again.current()));

		again.setCurrent(QuickNotesWorkspaces.MAIN_ID);
		assertTrue(again.delete(work));
		assertFalse(again.ids().contains(work));
		assertEquals(Arrays.asList("in main"), texts(again.current()));
	}
}
