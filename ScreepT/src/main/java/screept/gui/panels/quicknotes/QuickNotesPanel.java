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

import various.common.light.gui.ShortcutKeys;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.TextAttribute;
import java.awt.geom.Path2D;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.filechooser.FileNameExtensionFilter;

import gui.commons.dialogutils.NativeFileChooser;
import various.common.light.i18n.I18n;
import various.common.light.utility.string.WildcardPattern;

import net.miginfocom.swing.MigLayout;
import screept.gui.style.CardPalette;
import screept.gui.style.RoundTextField;
import screept.gui.style.ScrollableListPanel;

/**
 * Keep-like quick notes: to-do items with checkbox, inline editing and instant search.
 * Every change goes to {@link QuickNotesStore}, which saves automatically.
 *
 * Keys: Enter = new note below, Shift+Enter = new line, Ctrl+Enter = done/undone,
 * Alt+Up/Down = move, Backspace on an empty note = delete, Ctrl+F = search, Esc = clear search / close.
 */
public class QuickNotesPanel extends JPanel {

	private static final long serialVersionUID = -3482610279425811736L;

	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
	private static final DateTimeFormatter DAY_YEAR = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
	private static final int ARC = 14;

	private final QuickNotesWorkspaces workspaces;
	private QuickNotesStore store;
	/** Tags chosen in the tag filter: a note is shown when it has all of them; empty = every note */
	private final Set<String> activeTags = new LinkedHashSet<String>();
	private boolean refreshingCombos;
	private final Runnable closeAction;
	private final Map<QuickNote, NoteCard> cards = new IdentityHashMap<>();

	private CardPalette palette = CardPalette.current();

	private final JLabel titleLbl = new JLabel(I18n.t("app.quick_notes"));
	private final JLabel counterLbl = new JLabel();
	private JPanel headerPanel;
	private final FlatButton menuBtn = new FlatButton("\u2261", false);
	private final FlatButton newTagBtn = new FlatButton("+", false);
	private final JPopupMenu newTagMenu = new JPopupMenu();
	/** Notes whose tags are shown one per line ("+N" pressed) */
	private final Set<QuickNote> expandedTags = Collections.newSetFromMap(new IdentityHashMap<QuickNote, Boolean>());
	private final FlatButton closeBtn = new FlatButton("\u00D7", true);
	/** Search pop-up: hidden until Ctrl+F or the search button opens it, and Esc closes it (emptying it first) */
	private final FlatButton searchBtn = new FlatButton("⌕", false);
	private final RoundTextField searchField = new RoundTextField("Search notes and tags (* and ? wildcards)", true, () -> palette);
	private final RoundTextField addField = new RoundTextField("Add a note and press Enter", false, () -> palette);
	private final ScrollableListPanel listPanel = new ScrollableListPanel("4 12 14 12", 6);
	private final JScrollPane scroll = new JScrollPane(listPanel);
	private final JComboBox<WorkspaceItem> workspaceBox = new JComboBox<WorkspaceItem>();
	private final TagStrip tagStrip = new TagStrip();
	private final JPopupMenu tagMenu = new JPopupMenu();
	private final JCheckBox todoToggle = new JCheckBox("To do", true);
	private final JCheckBox doneToggle = new JCheckBox("Done", true);
	private static final String SHORTCUT_HINT = "Enter add  \u00B7  Ctrl+Enter done  \u00B7  Alt+Up/Down move";
	private static final int CHIP_PAD_X = 10;
	private static final int CHIP_PAD_Y = 3;
	private final JLabel footerLbl = new JLabel(SHORTCUT_HINT);
	private final FlatButton undoBtn = new FlatButton("Undo", false);
	private final Timer undoTimer = new Timer(6000, e -> hideUndo());
	private DeletedNote lastDeleted;

	public QuickNotesPanel(QuickNotesWorkspaces workspaces, Runnable closeAction) {
		this.workspaces = workspaces;
		this.store = workspaces.current();
		this.closeAction = closeAction;

		setLayout(new BorderLayout());
		setOpaque(true);

		JPanel header = new JPanel(new MigLayout("insets 14 14 8 14, fillx, gapy 8, hidemode 2", "[grow][][]", "[][][][][]"));
		header.setOpaque(false);
		header.add(titleLbl, "split 2, gapright 8");
		header.add(counterLbl, "aligny baseline");
		header.add(searchBtn);
		header.add(menuBtn);
		header.add(closeBtn, "wrap");
		header.add(workspaceBox, "span 3, growx, wrap");
		header.add(tagStrip, "span 2, growx");
		header.add(newTagBtn, "wrap");
		header.add(searchField, "span 3, growx, wrap");
		searchField.setVisible(false);
		header.add(todoToggle, "span 3, split 2, gapleft 2");
		header.add(doneToggle, "wrap");
		header.add(addField, "span 3, growx");
		headerPanel = header;
		add(header, BorderLayout.NORTH);

		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		add(scroll, BorderLayout.CENTER);

		JPanel footer = new JPanel(new BorderLayout(8, 0));
		footer.setOpaque(false);
		footer.setBorder(BorderFactory.createEmptyBorder(4, 14, 8, 14));
		footer.add(footerLbl, BorderLayout.CENTER);
		footer.add(undoBtn, BorderLayout.EAST);
		undoBtn.setVisible(false);
		undoBtn.setFont(UIManager.getFont("Label.font"));
		undoBtn.addActionListener(e -> undoDelete());
		add(footer, BorderLayout.SOUTH);

		searchBtn.setToolTipText("Search notes (Ctrl+F)");
		searchBtn.addActionListener(e -> toggleSearch());
		menuBtn.setToolTipText(I18n.t("app.backup_and_import"));
		menuBtn.addActionListener(e -> showBackupMenu());
		newTagBtn.setToolTipText("New tag");
		newTagBtn.addActionListener(e -> toggleNewTagField());
		closeBtn.setToolTipText("Close (Esc)");
		closeBtn.addActionListener(e -> closeAction.run());
		workspaceBox.setToolTipText("Workspace");
		workspaceBox.addActionListener(e -> {
			WorkspaceItem picked = (WorkspaceItem) workspaceBox.getSelectedItem();
			if (!refreshingCombos && picked != null && !picked.id.equals(workspaces.currentId())) {
				switchWorkspace(picked.id);
			}
		});
		refreshWorkspaceCombo();

		searchField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> rebuild(null)));
		todoToggle.setToolTipText("Show the notes to do");
		doneToggle.setToolTipText("Show the completed notes");
		todoToggle.addActionListener(e -> rebuild(null));
		doneToggle.addActionListener(e -> rebuild(null));
		bindKey(searchField, KeyEvent.VK_ESCAPE, 0, "qn-search-esc", () -> {
			if (searchField.getText().isEmpty()) {
				hideSearch();
			} else {
				searchField.setText("");
			}
		});
		bindKey(searchField, KeyEvent.VK_DOWN, 0, "qn-search-down", this::focusFirstNote);
		bindKey(searchField, KeyEvent.VK_ENTER, 0, "qn-search-enter", this::focusFirstNote);

		addField.setToolTipText("Enter = add on top");
		bindKey(addField, KeyEvent.VK_ENTER, 0, "qn-add", () -> {
			String text = addField.getText();
			if (!text.trim().isEmpty()) {
				store.addFirst(text.trim());
				todoToggle.setSelected(true); // a new note to do must stay visible
				addField.setText("");
				rebuild(null);
			}
		});
		bindKey(addField, KeyEvent.VK_ESCAPE, 0, "qn-add-esc", this::escape);
		bindKey(addField, KeyEvent.VK_DOWN, 0, "qn-add-down", this::focusFirstNote);

		// Ctrl+F and Esc anywhere inside the panel
		bindKey(this, KeyEvent.VK_F, ShortcutKeys.MENU_DOWN_MASK, "qn-find", this::showSearch);
		bindKey(this, KeyEvent.VK_ESCAPE, 0, "qn-close", this::escape);

		applyPalette();
	}

	// ---- backup / import ----

	private void showBackupMenu() {
		JPopupMenu menu = new JPopupMenu();
		File folder = store.getBackupFolder();
		JMenuItem backupAs = new JMenuItem(I18n.t("app.back_up_notes_to"));
		backupAs.addActionListener(e -> backupNotes(true));
		JMenuItem update = new JMenuItem(I18n.t("app.update_backup"));
		update.setEnabled(folder != null);
		update.setToolTipText(folder == null ? "Choose a backup folder first" : folder.getAbsolutePath());
		update.addActionListener(e -> backupNotes(false));
		JMenuItem restore = new JMenuItem(I18n.t("app.import_notes_from_backup"));
		restore.addActionListener(e -> importNotes());
		menu.add(backupAs);
		menu.add(update);
		menu.addSeparator();
		menu.add(restore);
		JMenuItem newWs = new JMenuItem("New workspace...");
		newWs.addActionListener(e -> newWorkspace());
		JMenuItem renameWs = new JMenuItem("Rename workspace...");
		renameWs.addActionListener(e -> renameWorkspace());
		JMenuItem deleteWs = new JMenuItem("Delete workspace...");
		deleteWs.setEnabled(workspaces.ids().size() > 1);
		deleteWs.addActionListener(e -> deleteWorkspace());
		menu.addSeparator();
		menu.add(newWs);
		menu.add(renameWs);
		menu.add(deleteWs);
		menu.addSeparator();
		JMenuItem export = new JMenuItem("Export notes to file...");
		export.addActionListener(e -> exportNotes());
		JMenuItem importNew = new JMenuItem("Import as new workspace...");
		importNew.addActionListener(e -> importAsWorkspace());
		menu.add(export);
		menu.add(importNew);
		JMenuItem clearDone = new JMenuItem("Clear completed notes...");
		clearDone.addActionListener(e -> clearCompleted());
		menu.addSeparator();
		menu.add(clearDone);
		menu.show(menuBtn, 0, menuBtn.getHeight());
	}

	/** Deletes a note and offers to undo it from the footer (the note goes back to the workspace it came from) */
	private void deleteNote(QuickNote note) {
		int index = store.getNotes().indexOf(note);
		store.remove(note);
		lastDeleted = new DeletedNote(workspaces.currentId(), store, note, index);
		footerLbl.setText("Note deleted");
		undoBtn.setVisible(true);
		undoTimer.restart();
		rebuild(null);
	}

	private void hideUndo() {
		lastDeleted = null;
		undoBtn.setVisible(false);
		footerLbl.setText(SHORTCUT_HINT);
		undoTimer.stop();
	}

	private void undoDelete() {
		DeletedNote deleted = lastDeleted;
		hideUndo();
		if (deleted == null || !workspaces.ids().contains(deleted.workspaceId)) {
			return;
		}
		deleted.store.insert(deleted.note, deleted.index);
		if (!deleted.workspaceId.equals(workspaces.currentId())) {
			switchWorkspace(deleted.workspaceId);
		} else {
			rebuild(null);
		}
	}

	/** Removes the completed notes of the workspace, after a confirmation */
	private void clearCompleted() {
		int done = 0;
		for (QuickNote note : store.getNotes()) {
			if (note.isDone()) {
				done++;
			}
		}
		if (done == 0) {
			JOptionPane.showMessageDialog(this, "There are no completed notes in this workspace.", "Clear completed", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		int choice = JOptionPane.showConfirmDialog(this, "Delete the " + done + " completed note(s) of this workspace?", "Clear completed",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (choice == JOptionPane.YES_OPTION) {
			store.removeCompleted();
			rebuild(null);
		}
	}

	/** Asks for the name of a new workspace and switches to it */
	private void newWorkspace() {
		String name = JOptionPane.showInputDialog(this, "Name of the new workspace (max 40 characters)", "");
		if (name == null) {
			return; // cancelled by user
		}
		String id = workspaces.create(name);
		if (id == null) {
			warnName();
			return;
		}
		switchWorkspace(id);
	}

	private void renameWorkspace() {
		String id = workspaces.currentId();
		String name = JOptionPane.showInputDialog(this, "New name of the workspace", workspaces.name(id));
		if (name == null) {
			return; // cancelled by user
		}
		if (!workspaces.rename(id, name)) {
			warnName();
			return;
		}
		refreshWorkspaceCombo();
	}

	private void deleteWorkspace() {
		String id = workspaces.currentId();
		if (workspaces.ids().size() <= 1) {
			JOptionPane.showMessageDialog(this, "The last workspace cannot be deleted.", "Delete workspace", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		int choice = JOptionPane.showConfirmDialog(this, "Delete the workspace \"" + workspaces.name(id) + "\"?\nIts notes are moved to the deleted folder, not erased.",
				"Delete workspace", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (choice != JOptionPane.YES_OPTION) {
			return; // cancelled by user
		}
		try {
			workspaces.delete(id);
			switchWorkspace(workspaces.currentId());
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Cannot delete the workspace: " + ex.getMessage(), "Delete failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void warnName() {
		JOptionPane.showMessageDialog(this, "The name cannot be empty and must be different from the other workspaces.", "Workspace", JOptionPane.WARNING_MESSAGE);
	}

	/** Saves the current workspace and shows another one */
	private void switchWorkspace(String id) {
		workspaces.flushAll();
		workspaces.setCurrent(id);
		store = workspaces.current();
		activeTags.clear();
		refreshWorkspaceCombo();
		rebuild(null);
	}

	/** Rebuilds the workspace list without firing the listener */
	private void refreshWorkspaceCombo() {
		refreshingCombos = true;
		try {
			workspaceBox.removeAllItems();
			WorkspaceItem selected = null;
			for (String id : workspaces.ids()) {
				WorkspaceItem item = new WorkspaceItem(id, workspaces.name(id));
				workspaceBox.addItem(item);
				if (id.equals(workspaces.currentId())) {
					selected = item;
				}
			}
			workspaceBox.setSelectedItem(selected);
		} finally {
			refreshingCombos = false;
		}
	}

	/** Tags of the current workspace with their counts; a chosen tag that no longer exists leaves the filter */
	private void refreshTagChips() {
		TreeMap<String, Integer> counts = new TreeMap<String, Integer>();
		int total = 0;
		for (QuickNote note : store.getNotes()) {
			if (!note.isBlank()) {
				total++;
			}
			for (String tag : note.getTags()) {
				Integer n = counts.get(tag);
				counts.put(tag, n == null ? 1 : n + 1);
			}
		}
		for (String tag : store.getKnownTags()) {
			if (!counts.containsKey(tag)) {
				counts.put(tag, 0);
			}
		}
		activeTags.retainAll(counts.keySet());
		tagStrip.setTags(total, counts);
	}

	/** Writes the notes of this workspace to a file the user chooses (same format as the backup) */
	private void exportNotes() {
		JFileChooser chooser = new NativeFileChooser();
		chooser.setDialogTitle("Export the notes of this workspace");
		chooser.setFileFilter(new FileNameExtensionFilter("Markdown (*.md)", "md"));
		chooser.setSelectedFile(new File(workspaces.name(workspaces.currentId()).replaceAll("[^\\p{L}\\p{N} _-]", "_") + ".md"));
		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
			return; // cancelled by user
		}
		File target = chooser.getSelectedFile().getAbsoluteFile();
		if (!target.getName().toLowerCase(Locale.ROOT).endsWith(".md")) {
			target = new File(target.getParentFile(), target.getName() + ".md");
		}
		try {
			store.flush();
			File written = store.backupTo(target.getParentFile(), target.getName());
			JOptionPane.showMessageDialog(this, I18n.fmt("app.quick_notes_saved_to_x", written.getAbsolutePath()), I18n.t("app.backup_saved"), JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Cannot export the notes: " + ex.getMessage(), "Export failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	/** Imports a quick notes file as a new workspace, named after the file */
	private void importAsWorkspace() {
		JFileChooser chooser = new NativeFileChooser();
		chooser.setDialogTitle("Choose a quick notes file to import as a new workspace");
		chooser.setFileFilter(new FileNameExtensionFilter("Markdown (*.md)", "md"));
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
			return; // cancelled by user
		}
		File source = chooser.getSelectedFile();
		if (QuickNotesStore.countNotesIn(source) < 0) {
			JOptionPane.showMessageDialog(this, I18n.t("app.this_file_is_not_a_screept"), I18n.t("app.import_failed"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		String base = source.getName().replaceFirst("\\.md$", "");
		try {
			String id = workspaces.importAsNew(base, source);
			for (int n = 2; id == null && n < 100; n++) {
				id = workspaces.importAsNew(base + " " + n, source);
			}
			if (id == null) {
				warnName();
				return;
			}
			switchWorkspace(id);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, I18n.fmt("app.cannot_import_the_notes_x", String.valueOf(ex.getMessage())), I18n.t("app.import_failed"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/** Asks for a folder (null if cancelled) */
	private File chooseFolder(String title, File start) {
		JFileChooser chooser = new NativeFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (start != null && start.exists()) {
			chooser.setCurrentDirectory(start);
		}
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
			return null; // cancelled by user
		}
		return chooser.getSelectedFile();
	}

	/** Writes the backup file in the remembered folder (or asks for one) and tells where it went */
	private void backupNotes(boolean askFolder) {
		File folder = askFolder ? null : store.getBackupFolder();
		if (folder == null) {
			folder = chooseFolder("Choose the backup folder", store.getBackupFolder());
			if (folder == null) {
				return;
			}
		}
		try {
			store.flush();
			File written = store.backupTo(folder, QuickNotesStore.BACKUP_FILE_NAME);
			store.setBackupFolder(folder);
			JOptionPane.showMessageDialog(this, I18n.fmt("app.quick_notes_saved_to_x", written.getAbsolutePath()), I18n.t("app.backup_saved"), JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, I18n.fmt("app.cannot_save_the_backup_x", String.valueOf(ex.getMessage())), I18n.t("app.backup_failed"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/** Replaces the current notes with the ones of a backup file, after a clear warning */
	private void importNotes() {
		File folder = store.getBackupFolder();
		JFileChooser chooser = new NativeFileChooser();
		chooser.setDialogTitle("Choose a quick notes backup");
		chooser.setFileFilter(new FileNameExtensionFilter("Markdown (*.md)", "md"));
		if (folder != null && folder.exists()) {
			chooser.setCurrentDirectory(folder);
			File last = new File(folder, QuickNotesStore.BACKUP_FILE_NAME);
			if (last.exists()) {
				chooser.setSelectedFile(last);
			}
		}
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
			return; // cancelled by user
		}
		File backup = chooser.getSelectedFile();
		int incoming = QuickNotesStore.countNotesIn(backup);
		if (incoming < 0) {
			JOptionPane.showMessageDialog(this, I18n.t("app.this_file_is_not_a_screept"), I18n.t("app.import_failed"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		store.flush();
		String message = I18n.fmt("app.importing_replaces_your_x_current_notes", String.valueOf(store.getNotes().size()), String.valueOf(incoming));
		String backupFirst = I18n.t("app.back_up_current_notes_first");
		String replace = I18n.t("common.replace");
		int choice = JOptionPane.showOptionDialog(this, message, I18n.t("app.import_notes"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
				new Object[] { backupFirst, replace, I18n.t("common.cancel") }, I18n.t("common.cancel"));
		if (choice != 0 && choice != 1) {
			return; // cancelled by user
		}
		try {
			if (choice == 0) {
				File target = chooseFolder("Choose where to back up the current notes", folder);
				if (target == null) {
					return; // cancelled by user: nothing was changed
				}
				String name = "quick_notes_before_import_" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".md";
				store.backupTo(target, name);
			}
			store.importFrom(backup);
			rebuild(null);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, I18n.fmt("app.cannot_import_the_notes_x", String.valueOf(ex.getMessage())), I18n.t("app.import_failed"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/** Re-reads the current look and feel colors (dark or light) */
	/** The title row is the handle to move the drawer: the listener gets the mouse events of the header and of its title */
	public void installDragHandle(java.awt.event.MouseAdapter drag) {
		for (JComponent handle : new JComponent[] { titleLbl, counterLbl, headerPanel }) {
			if (handle != null) {
				handle.addMouseListener(drag);
				handle.addMouseMotionListener(drag);
			}
		}
		titleLbl.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.E_RESIZE_CURSOR));
		titleLbl.setToolTipText(I18n.t("app.quick_notes_drag_tip"));
	}

	public void applyPalette() {
		palette = CardPalette.current();
		Font base = UIManager.getFont("Label.font");
		if (base == null) {
			base = new Font("Segoe UI", Font.PLAIN, 12);
		}
		setBackground(palette.background);
		titleLbl.setFont(base.deriveFont(Font.BOLD, base.getSize2D() + 5f));
		titleLbl.setForeground(palette.text);
		counterLbl.setFont(base.deriveFont(base.getSize2D() - 1f));
		counterLbl.setForeground(palette.muted);
		footerLbl.setFont(base.deriveFont(base.getSize2D() - 1f));
		footerLbl.setForeground(palette.muted);
		for (JCheckBox box : new JCheckBox[] { todoToggle, doneToggle }) {
			box.setFont(base.deriveFont(base.getSize2D() - 1f));
			box.setForeground(palette.muted);
			box.setOpaque(false);
		}
		tagStrip.repaint();
		closeBtn.setFont(base.deriveFont(Font.PLAIN, base.getSize2D() + 8f));
		menuBtn.setFont(base.deriveFont(Font.PLAIN, base.getSize2D() + 6f));
		newTagBtn.setFont(base.deriveFont(Font.PLAIN, base.getSize2D() + 6f));
		for (RoundTextField field : new RoundTextField[] { searchField, addField }) {
			field.setFont(base.deriveFont(base.getSize2D() + 1f));
			field.applyPalette();
		}
		rebuild(null);
	}

	/** Focuses the "add" field, ready to type */
	public void focusAddField() {
		addField.requestFocusInWindow();
	}

	public void rebuild(QuickNote focusNote) {
		String[] terms = searchTerms();
		List<QuickNote> open = new ArrayList<>();
		List<QuickNote> done = new ArrayList<>();
		refreshTagChips();
		for (QuickNote note : store.getNotes()) {
			if (!matches(note, terms) || !matchesTags(note)) {
				continue;
			}
			if (note.isDone()) {
				if (doneToggle.isSelected()) {
					done.add(note);
				}
			} else if (todoToggle.isSelected()) {
				open.add(note);
			}
		}

		listPanel.removeAll();
		cards.clear();
		for (QuickNote note : open) {
			addCard(note, terms);
		}
		if (!done.isEmpty()) {
			// while searching completed notes are always visible
			boolean collapsed = store.isCompletedCollapsed() && terms.length == 0;
			listPanel.add(completedHeader(done.size(), collapsed), "gaptop 10");
			if (!collapsed) {
				for (QuickNote note : done) {
					addCard(note, terms);
				}
			}
		}
		if (open.isEmpty() && done.isEmpty()) {
			JLabel empty = new JLabel(emptyMessage(terms), SwingConstants.CENTER);
			empty.setForeground(palette.muted);
			listPanel.add(empty, "gaptop 30");
		}
		updateCounter();
		listPanel.revalidate();
		listPanel.repaint();

		NoteCard card = focusNote == null ? null : cards.get(focusNote);
		if (card != null) {
			SwingUtilities.invokeLater(() -> {
				card.area.requestFocusInWindow();
				card.area.setCaretPosition(card.area.getDocument().getLength());
				listPanel.scrollRectToVisible(card.getBounds());
			});
		}
	}

	private void addCard(QuickNote note, String[] terms) {
		NoteCard card = new NoteCard(note, terms);
		cards.put(note, card);
		listPanel.add(card);
	}

	private JComponent completedHeader(int count, boolean collapsed) {
		JLabel lbl = new JLabel((collapsed ? "\u25B8  " : "\u25BE  ") + "Completed \u00B7 " + count);
		lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
		lbl.setForeground(palette.muted);
		lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lbl.setToolTipText(collapsed ? "Show completed notes" : "Hide completed notes");
		lbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				store.setCompletedCollapsed(!store.isCompletedCollapsed());
				rebuild(null);
			}
		});
		return lbl;
	}

	private void updateCounter() {
		int open = 0;
		int done = 0;
		for (QuickNote note : store.getNotes()) {
			if (!note.isBlank()) {
				if (note.isDone()) {
					done++;
				} else {
					open++;
				}
			}
		}
		counterLbl.setText(open + " to do \u00B7 " + done + " done");
	}

	private void focusFirstNote() {
		if (!cards.isEmpty()) {
			Component first = listPanel.getComponent(0);
			if (first instanceof NoteCard) {
				((NoteCard) first).area.requestFocusInWindow();
			}
		}
	}

	private String[] searchTerms() {
		String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
		return query.isEmpty() ? new String[0] : query.split("\\s+");
	}

	/** A note matches when every word of the search is in its text or in one of its tags */
	private static boolean matches(QuickNote note, String[] terms) {
		String text = (note.getText() + " " + String.join(" ", note.getTags())).toLowerCase(Locale.ROOT);
		for (String term : terms) {
			boolean found = WildcardPattern.hasWildcard(term) ? WildcardPattern.matchesAnyPart(text, term) : text.contains(term);
			if (!found) {
				return false;
			}
		}
		return true;
	}

	/** Several chosen tags work as OR: a note is shown when it has any of them; with no tag chosen every note is shown */
	private boolean matchesTags(QuickNote note) {
		if (activeTags.isEmpty()) {
			return true;
		}
		for (String tag : note.getTags()) {
			if (activeTags.contains(tag)) {
				return true;
			}
		}
		return false;
	}

	/** Text shown when no note is visible: says why (toggles, search, tags) */
	private String emptyMessage(String[] terms) {
		boolean todo = todoToggle.isSelected();
		boolean done = doneToggle.isSelected();
		if (!todo && !done) {
			return "Turn on To do or Done to see the notes";
		}
		if (terms.length > 0) {
			return "No notes match \"" + searchField.getText().trim() + "\"";
		}
		if (activeTags.size() == 1) {
			return "No notes tagged #" + activeTags.iterator().next();
		}
		if (!activeTags.isEmpty()) {
			return "No notes with any of the " + activeTags.size() + " chosen tags";
		}
		if (!todo) {
			return "No completed notes";
		}
		if (!done) {
			return "No notes to do";
		}
		return "<html><div style='text-align:center'>Nothing here yet.<br>Write above and press Enter.</div></html>";
	}

	/** Esc closes the small popups first (tag list, new tag field), then the panel */
	private void escape() {
		if (tagMenu.isVisible() || newTagMenu.isVisible()) {
			tagMenu.setVisible(false);
			newTagMenu.setVisible(false);
		} else if (searchField.isVisible()) {
			hideSearch();
		} else {
			closeAction.run();
		}
	}

	/** Shows the search bar and gives it the focus */
	private void showSearch() {
		searchField.setVisible(true);
		revalidate();
		searchField.requestFocusInWindow();
		searchField.selectAll();
	}

	/** Hides the search bar and empties it: no note stays filtered out of sight */
	private void hideSearch() {
		searchField.setText("");
		searchField.setVisible(false);
		revalidate();
		addField.requestFocusInWindow();
	}

	private void toggleSearch() {
		if (searchField.isVisible()) {
			hideSearch();
		} else {
			showSearch();
		}
	}

	/** The "+" of the tag bar: a field to create tags (commas make several); a tag exists even without notes */
	private void toggleNewTagField() {
		if (newTagMenu.isVisible()) {
			newTagMenu.setVisible(false);
			return;
		}
		JTextField field = new JTextField(16);
		field.setToolTipText("Name of the new tag, Enter to create");
		bindKey(field, KeyEvent.VK_ENTER, 0, "qn-new-tag", () -> createTags(field.getText()));
		bindKey(field, KeyEvent.VK_ESCAPE, 0, "qn-new-tag-esc", this::escape);
		newTagMenu.removeAll();
		newTagMenu.add(field);
		newTagMenu.show(newTagBtn, 0, newTagBtn.getHeight());
		field.requestFocusInWindow();
	}

	/** Creates the tags written in the field (letters, digits, - and _; commas separate them) */
	void createTags(String text) {
		List<String> tags = QuickNote.parseTags(text);
		if (tags.isEmpty()) {
			return;
		}
		for (String tag : tags) {
			store.addKnownTag(tag);
		}
		newTagMenu.setVisible(false);
		rebuild(null);
	}

	/** Every tag of the workspace: the ones of the notes and the ones created without notes, alphabetical */
	List<String> knownTags() {
		TreeSet<String> all = new TreeSet<String>(store.getKnownTags());
		for (QuickNote note : store.getNotes()) {
			all.addAll(note.getTags());
		}
		return new ArrayList<String>(all);
	}

	/** Every tag with its count as check boxes: several can be picked and the list stays open; Clear empties the filter */
	private void showTagMenu(Component under, int x) {
		tagMenu.removeAll();
		JPanel list = new JPanel();
		list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
		JButton clear = new JButton("Clear filter");
		clear.setAlignmentX(Component.LEFT_ALIGNMENT);
		clear.addActionListener(e -> {
			activeTags.clear();
			tagMenu.setVisible(false);
			rebuild(null);
		});
		list.add(clear);
		for (Map.Entry<String, Integer> entry : tagStrip.counts.entrySet()) {
			String tag = entry.getKey();
			JCheckBox box = new JCheckBox("#" + tag + "  " + entry.getValue(), activeTags.contains(tag));
			box.setAlignmentX(Component.LEFT_ALIGNMENT);
			box.addActionListener(e -> {
				if (!activeTags.remove(tag)) {
					activeTags.add(tag);
				}
				rebuild(null);
			});
			list.add(box);
		}
		Dimension size = list.getPreferredSize();
		JScrollPane tagScroll = new JScrollPane(list);
		tagScroll.setBorder(BorderFactory.createEmptyBorder());
		tagScroll.setPreferredSize(new Dimension(Math.max(180, size.width + 24), Math.min(size.height, 260) + 2));
		tagMenu.add(tagScroll);
		tagMenu.show(under, x, under.getHeight());
	}

	private static String describeDate(QuickNote note) {
		LocalDate day = note.getCreated().toLocalDate();
		LocalDate today = LocalDate.now();
		String time = TIME.format(note.getCreated());
		if (day.equals(today)) {
			return "today " + time;
		}
		if (day.equals(today.minusDays(1))) {
			return "yesterday " + time;
		}
		return (day.getYear() == today.getYear() ? DAY : DAY_YEAR).format(note.getCreated()) + " " + time;
	}

	private static void bindKey(JComponent comp, int key, int modifiers, String name, Runnable action) {
		int condition = comp instanceof QuickNotesPanel ? WHEN_ANCESTOR_OF_FOCUSED_COMPONENT : WHEN_FOCUSED;
		comp.getInputMap(condition).put(KeyStroke.getKeyStroke(key, modifiers), name);
		comp.getActionMap().put(name, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				action.run();
			}
		});
	}

	///////////////////////////////// COMPONENTS /////////////////////////////////

	/** One note: round checkbox, wrapping editable text, date and a delete button shown on hover */
	private final class NoteCard extends JPanel {
		private static final long serialVersionUID = 1L;

		private final QuickNote note;
		private final JCheckBox check = new JCheckBox();
		private final JTextArea area = new JTextArea();
		private final JLabel dateLbl = new JLabel();
		private final FlatButton tagBtn = new FlatButton("#", false);
		private final FlatButton deleteBtn = new FlatButton("\u00D7", true);
		private final NoteTagLine tagLine;
		private final JComboBox<String> tagPick = new JComboBox<String>();
		private boolean filling;
		private boolean hover;

		NoteCard(QuickNote note, String[] terms) {
			this.note = note;
			tagLine = new NoteTagLine(note);
			tagLine.setVisible(!note.getTags().isEmpty());
			installTagPicker();
			setOpaque(false);
			setLayout(new MigLayout("insets 8 8 7 6, fillx, gap 6 2", "[][grow,fill][]", "[top][]"));

			check.setSelected(note.isDone());
			check.setOpaque(false);
			check.setFocusable(false);
			check.setIcon(new CheckIcon(false, false));
			check.setRolloverIcon(new CheckIcon(false, true));
			check.setSelectedIcon(new CheckIcon(true, false));
			check.setRolloverSelectedIcon(new CheckIcon(true, true));
			check.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
			check.setToolTipText(note.isDone() ? "Mark as to do (Ctrl+Enter)" : "Mark as done (Ctrl+Enter)");
			check.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			check.addActionListener(e -> toggleDone());

			Font base = UIManager.getFont("Label.font");
			Font font = base.deriveFont(base.getSize2D() + 1f);
			if (note.isDone()) {
				font = font.deriveFont(Collections.singletonMap(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON));
			}
			area.setFont(font);
			area.setText(note.getText());
			area.setLineWrap(true);
			area.setWrapStyleWord(true);
			CardPalette.flatten(area);
			area.setOpaque(false);
			area.setBorder(BorderFactory.createEmptyBorder());
			area.setForeground(note.isDone() ? palette.muted : palette.text);
			area.setCaretColor(palette.text);
			area.setSelectionColor(palette.selection);
			area.setSelectedTextColor(palette.text);
			// Tab moves to the next note instead of inserting a tab
			area.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
			area.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
			highlight(terms);
			installEditing();

			dateLbl.setText(describeDate(note));
			dateLbl.setFont(base.deriveFont(base.getSize2D() - 2f));
			dateLbl.setForeground(palette.muted);

			deleteBtn.setFont(base.deriveFont(base.getSize2D() + 5f));
			deleteBtn.setToolTipText("Delete note");
			deleteBtn.setVisible(false);
			tagBtn.setFont(base.deriveFont(base.getSize2D() + 5f));
			tagBtn.setToolTipText("Add a tag");
			tagBtn.setVisible(false);
			tagBtn.addActionListener(e -> editTags());
			deleteBtn.addActionListener(e -> deleteNote(note));

			add(check, "aligny top");
			add(area, "growx, wmin 10");
			JPanel actions = new JPanel(new MigLayout("insets 0, gap 0", "[][]", "[]"));
			actions.setOpaque(false);
			actions.add(tagBtn, "hidemode 0");
			actions.add(deleteBtn, "hidemode 0");
			add(actions, "aligny top, hidemode 0, wrap");
			add(dateLbl, "skip 1, span 2, growx, wmin 0, wrap");
			add(tagLine, "skip 1, span 2, growx, wmin 0, hidemode 3, wrap");
			add(tagPick, "skip 1, span 2, growx, hidemode 3");

			MouseAdapter hoverTracker = new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					setHover(true);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					setHover(getMousePosition(true) != null);
				}
			};
			for (Component c : new Component[] { this, check, area, tagBtn, deleteBtn, dateLbl, tagLine, tagPick }) {
				c.addMouseListener(hoverTracker);
			}
		}

		private void setHover(boolean hover) {
			if (this.hover != hover) {
				this.hover = hover;
				tagBtn.setVisible(hover);
				deleteBtn.setVisible(hover);
				repaint();
			}
		}

		private void highlight(String[] terms) {
			Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(palette.highlight);
			String lower = note.getText().toLowerCase(Locale.ROOT);
			for (String term : terms) {
				for (int i = lower.indexOf(term); i >= 0; i = lower.indexOf(term, i + term.length())) {
					try {
						area.getHighlighter().addHighlight(i, i + term.length(), painter);
					} catch (BadLocationException e) {
						// text changed meanwhile, highlight skipped
					}
				}
			}
		}

		private void installEditing() {
			area.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
				note.setText(area.getText());
				area.getHighlighter().removeAllHighlights();
				store.changed();
				updateCounter();
			}));

			area.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					repaint();
				}

				@Override
				public void focusLost(FocusEvent e) {
					repaint();
					// an empty note left behind is removed
					if (note.isBlank() && store.contains(note)) {
						store.remove(note);
						cards.remove(note);
						listPanel.remove(NoteCard.this);
						listPanel.revalidate();
						listPanel.repaint();
						updateCounter();
					}
				}
			});

			bindKey(area, KeyEvent.VK_ENTER, 0, "qn-new-below", () -> {
				if (note.isDone()) {
					focusAddField();
				} else {
					rebuild(store.addAfter(note));
				}
			});
			area.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), DefaultEditorKit.insertBreakAction);
			bindKey(area, KeyEvent.VK_ENTER, ShortcutKeys.MENU_DOWN_MASK, "qn-toggle", this::toggleDone);
			bindKey(area, KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK, "qn-up", () -> moveBy(-1));
			bindKey(area, KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK, "qn-down", () -> moveBy(1));

			Action deletePrevChar = area.getActionMap().get(DefaultEditorKit.deletePrevCharAction);
			bindKey(area, KeyEvent.VK_BACK_SPACE, 0, "qn-backspace", () -> {
				if (area.getDocument().getLength() == 0) {
					deleteAndFocusPrevious();
				} else if (deletePrevChar != null) {
					deletePrevChar.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, null));
				}
			});
		}

		/** The # button: a combo right under the tags of this note, with the tags it does not have yet; picking one adds it */
		private void editTags() {
			if (tagPick.isVisible()) {
				hideTagPicker();
				return;
			}
			filling = true;
			tagPick.removeAllItems();
			for (String tag : knownTags()) {
				if (!note.getTags().contains(tag)) {
					tagPick.addItem(tag);
				}
			}
			tagPick.setSelectedItem(null);
			filling = false;
			if (tagPick.getItemCount() == 0) {
				return; // no other tag exists yet: create one with the + above
			}
			tagPick.setVisible(true);
			listPanel.revalidate();
			listPanel.repaint();
			SwingUtilities.invokeLater(() -> {
				tagPick.requestFocusInWindow();
				tagPick.showPopup();
			});
		}

		/** Picking a tag of the list adds it to the note and the combo goes away; Esc or a click outside hides it too */
		private void installTagPicker() {
			tagPick.setVisible(false);
			tagPick.setToolTipText("Choose a tag for this note");
			Font base = UIManager.getFont("Label.font");
			tagPick.setFont(base.deriveFont(base.getSize2D() - 1f));
			tagPick.addActionListener(e -> {
				Object picked = tagPick.getSelectedItem();
				if (filling || picked == null) {
					return;
				}
				List<String> tags = new ArrayList<String>(note.getTags());
				for (String tag : QuickNote.parseTags(picked.toString())) {
					if (!tags.contains(tag)) {
						tags.add(tag);
					}
				}
				note.setTags(tags);
				store.changed();
				rebuild(null);
			});
			tagPick.addPopupMenuListener(new PopupMenuListener() {
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					// nothing to do when the list opens
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					SwingUtilities.invokeLater(() -> {
						if (tagPick.getSelectedItem() == null) {
							hideTagPicker();
						}
					});
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
					SwingUtilities.invokeLater(() -> hideTagPicker());
				}
			});
			bindKey(tagPick, KeyEvent.VK_ESCAPE, 0, "qn-pick-esc", () -> hideTagPicker());
		}

		private void hideTagPicker() {
			if (tagPick.isVisible()) {
				tagPick.hidePopup();
				tagPick.setVisible(false);
				listPanel.revalidate();
				listPanel.repaint();
			}
		}

		private void toggleDone() {
			note.setDone(!note.isDone());
			store.changed();
			rebuild(null);
		}

		private void moveBy(int delta) {
			if (store.move(note, delta)) {
				rebuild(note);
			}
		}

		private void deleteAndFocusPrevious() {
			List<QuickNote> visible = new ArrayList<>();
			for (Component c : listPanel.getComponents()) {
				if (c instanceof NoteCard) {
					visible.add(((NoteCard) c).note);
				}
			}
			int index = visible.indexOf(note);
			deleteNote(note);
			if (index > 0) {
				rebuild(visible.get(index - 1));
			} else {
				rebuild(null);
				focusAddField();
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			CardPalette.antialias(g2);
			boolean editing = area.isFocusOwner();
			g2.setColor(note.isDone() ? palette.cardDone : (hover || editing ? palette.cardHover : palette.card));
			g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
			g2.setColor(editing ? palette.accent : palette.border);
			g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
			g2.dispose();
			super.paintComponent(g);
		}
	}

	/** A note deleted from the panel, kept for the undo of the footer */
	private static final class DeletedNote {
		final String workspaceId;
		final QuickNotesStore store;
		final QuickNote note;
		final int index;

		DeletedNote(String workspaceId, QuickNotesStore store, QuickNote note, int index) {
			this.workspaceId = workspaceId;
			this.store = store;
			this.note = note;
			this.index = index;
		}
	}

	/**
	 * Tag filter row: one fixed line that never wraps. "All", then the tags that fit (alphabetical; the last one may be cut
	 * with an ellipsis), then the arrow, or "+N" when N tags are hidden: the arrow opens the list of every tag. While a filter is on, the row keeps every tag that fits.
	 * Painting and mouse hits use the same layout, so a click always hits the chip that is drawn.
	 */
	private final class TagStrip extends JComponent {
		private static final long serialVersionUID = 1L;
		private static final String ARROW = "\u25BE";
		private static final int GAP = 6;

		private final Map<String, Integer> counts = new TreeMap<String, Integer>();
		private int total;

		/** One chip of the row: tag null = All, more = the arrow / "+N" button */
		private final class Slot {
			final String tag;
			final boolean more;
			final String label;
			final boolean selected;
			final String tip;
			final Rectangle bounds = new Rectangle();
			String shown;

			Slot(String tag, boolean more, String label, boolean selected, String tip) {
				this.tag = tag;
				this.more = more;
				this.label = label;
				this.shown = label;
				this.selected = selected;
				this.tip = tip;
			}
		}

		TagStrip() {
			setFocusable(false);
			setOpaque(false);
			Font base = UIManager.getFont("Label.font");
			setFont(base.deriveFont(base.getSize2D() - 1f));
			ToolTipManager.sharedInstance().registerComponent(this);
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					Slot slot = slotAt(e.getPoint());
					if (slot == null) {
						return;
					}
					if (slot.more) {
						showTagMenu(TagStrip.this, slot.bounds.x);
						return;
					}
					if (slot.tag == null) {
						activeTags.clear();
					} else if (!activeTags.remove(slot.tag)) {
						activeTags.add(slot.tag);
					}
					rebuild(null);
				}
			});
			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					setCursor(Cursor.getPredefinedCursor(slotAt(e.getPoint()) == null ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
				}
			});
		}

		void setTags(int total, Map<String, Integer> counts) {
			this.total = total;
			this.counts.clear();
			this.counts.putAll(counts);
			repaint();
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(0, chipHeight());
		}

		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			Slot slot = slotAt(e.getPoint());
			return slot == null ? null : slot.tip;
		}

		private int chipHeight() {
			return getFontMetrics(getFont()).getHeight() + 2 * CHIP_PAD_Y;
		}

		private int chipWidth(FontMetrics fm, String label) {
			return fm.stringWidth(label) + 2 * CHIP_PAD_X;
		}

		private Slot slotAt(Point p) {
			for (Slot slot : layout(getWidth())) {
				if (slot.bounds.contains(p)) {
					return slot;
				}
			}
			return null;
		}

		/** The chips for this width, with their bounds: All, the tags that fit, then the arrow or "+N" */
		private List<Slot> layout(int width) {
			FontMetrics fm = getFontMetrics(getFont());
			List<Slot> all = new ArrayList<Slot>();
			all.add(new Slot(null, false, "All  " + total, activeTags.isEmpty(), "Show every note"));
			for (Map.Entry<String, Integer> entry : counts.entrySet()) {
				all.add(new Slot(entry.getKey(), false, "#" + entry.getKey() + "  " + entry.getValue(), activeTags.contains(entry.getKey()),
						"Filter by #" + entry.getKey() + " (several chosen tags: a note with any of them is shown)"));
			}
			List<Slot> row = fit(fm, all, width - chipWidth(fm, ARROW) - GAP);
			if (row.size() < all.size()) {
				row = fit(fm, all, width - chipWidth(fm, "+" + all.size() + " " + ARROW) - GAP);
			}
			if (counts.isEmpty()) {
				return row;
			}
			boolean hiddenSelected = false;
			int hidden = counts.size();
			for (Slot slot : row) {
				if (slot.tag != null) {
					hidden--;
				}
			}
			for (Slot slot : all) {
				hiddenSelected |= slot.tag != null && slot.selected && !row.contains(slot);
			}
			String label = hidden > 0 ? "+" + hidden + " " + ARROW : ARROW;
			int x = row.isEmpty() ? 0 : row.get(row.size() - 1).bounds.x + row.get(row.size() - 1).bounds.width + GAP;
			Slot more = new Slot(null, true, label, hiddenSelected, "Show all the tags (" + counts.size() + ")");
			more.bounds.setBounds(x, 0, chipWidth(fm, label), chipHeight());
			row.add(more);
			return row;
		}

        /**
         * The chips that fit before the right edge, in alphabetical order. "All" and the chosen tags are always kept: their room
         * is reserved first, and the other tags fill the room that is left. So selecting a tag never hides the others, and
         * the chosen ones never go behind the arrow.
         */
        private List<Slot> fit(FontMetrics fm, List<Slot> slots, int right) {
            int n = slots.size();
            int[] natural = new int[n];
            int[] reservedAfter = new int[n + 1];
            for (int i = 0; i < n; i++) {
                natural[i] = chipWidth(fm, slots.get(i).label);
            }
            for (int i = n - 1; i >= 0; i--) {
                reservedAfter[i] = reservedAfter[i + 1] + (required(slots.get(i)) ? natural[i] + GAP : 0);
            }
            List<Slot> out = new ArrayList<Slot>();
            int used = 0;
            for (int i = 0; i < n; i++) {
                Slot slot = slots.get(i);
                boolean fits = used + natural[i] + GAP + reservedAfter[i + 1] <= right + GAP;
                if (!required(slot) && !fits) {
                    continue;
                }
                slot.shown = slot.label;
                slot.bounds.setBounds(used, 0, natural[i], chipHeight());
                out.add(slot);
                used += natural[i] + GAP;
            }
            return out;
        }

        private boolean required(Slot slot) {
            return slot.tag == null || slot.selected;
        }

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			CardPalette.antialias(g2);
			g2.setFont(getFont());
			FontMetrics fm = g2.getFontMetrics();
			if (counts.isEmpty()) {
				g2.setColor(palette.muted);
				g2.drawString("Tags: add one with the # button on a note", 0, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
			}
			for (Slot slot : layout(getWidth())) {
				Rectangle r = slot.bounds;
				g2.setColor(slot.selected ? palette.accent : palette.card);
				g2.fillRoundRect(r.x, r.y, r.width - 1, r.height - 1, r.height, r.height);
				if (!slot.selected) {
					g2.setColor(palette.border);
					g2.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, r.height, r.height);
				}
				g2.setColor(slot.selected ? palette.onAccent : palette.text);
				g2.drawString(slot.shown, r.x + CHIP_PAD_X, r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
			}
			g2.dispose();
		}
	}

	/**
	 * Tags of one note, on its row: one line of chips. The chips that do not fit are behind "+N", and "+N" shows them all,
	 * one per line. Hover on a chip turns it into "Remove" on a red background, back to the chip on mouse out; a click removes it.
	 */
	private final class NoteTagLine extends JComponent {
		private static final long serialVersionUID = 1L;
		private static final int GAP = 4;
		private static final int PAD_X = 7;
		private static final int PAD_Y = 2;
		private static final String REMOVE = "Remove";
		private final QuickNote note;
		private final Color removeColor = new Color(0xC9473F);
		private String hoveredTag;

		/** One chip: a tag (tag not null) or a control ("+N", "less") */
		private final class Chip {
			final String tag;
			final boolean control;
			final String label;
			String shown;
			final Rectangle bounds = new Rectangle();

			Chip(String tag, String label, boolean control) {
				this.tag = tag;
				this.label = label;
				this.shown = label;
				this.control = control;
			}
		}

		NoteTagLine(QuickNote note) {
			this.note = note;
			setOpaque(false);
			setFocusable(false);
			Font base = UIManager.getFont("Label.font");
			setFont(base.deriveFont(base.getSize2D() - 2f));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(MouseEvent e) {
					setHoveredTag(null);
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					Chip chip = slotAt(e.getPoint());
					if (chip == null) {
						return;
					}
					if (chip.control) {
						if (!expandedTags.remove(note)) {
							expandedTags.add(note);
						}
						rebuild(null);
					} else {
						removeTag(chip.tag);
					}
				}
			});
			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					Chip chip = slotAt(e.getPoint());
					setHoveredTag(chip == null || chip.control ? null : chip.tag);
					setCursor(Cursor.getPredefinedCursor(chip == null ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
				}
			});
		}

		private void setHoveredTag(String tag) {
			if (!java.util.Objects.equals(hoveredTag, tag)) {
				hoveredTag = tag;
				repaint();
			}
		}

		private void removeTag(String tag) {
			List<String> left = new ArrayList<String>(note.getTags());
			left.remove(tag);
			note.setTags(left);
			store.changed();
			rebuild(null);
		}

		private int chipHeight() {
			return getFontMetrics(getFont()).getHeight() + 2 * PAD_Y;
		}

		@Override
		public Dimension getPreferredSize() {
			int rows = expandedTags.contains(note) ? note.getTags().size() + 1 : 1;
			return new Dimension(0, rows * chipHeight() + (rows - 1) * GAP);
		}

		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		private Chip slotAt(Point p) {
			for (Chip chip : layout(getWidth())) {
				if (chip.bounds.contains(p)) {
					return chip;
				}
			}
			return null;
		}

		/** The chips for this width: one line, or one per line when expanded. Painting and clicks both use it */
		private List<Chip> layout(int width) {
			FontMetrics fm = getFontMetrics(getFont());
			int h = chipHeight();
			int removeW = fm.stringWidth(REMOVE) + 2 * PAD_X;
			List<Chip> out = new ArrayList<Chip>();
			if (expandedTags.contains(note)) {
				int y = 0;
				for (String tag : note.getTags()) {
					Chip chip = new Chip(tag, "#" + tag, false);
					int w = Math.min(Math.max(fm.stringWidth(chip.label) + 2 * PAD_X, removeW), width);
					chip.shown = shortLabel(fm, chip.label, w - 2 * PAD_X);
					chip.bounds.setBounds(0, y, w, h);
					out.add(chip);
					y += h + GAP;
				}
				Chip less = new Chip(null, "less", true);
				less.bounds.setBounds(0, y, fm.stringWidth(less.label) + 2 * PAD_X, h);
				out.add(less);
				return out;
			}
			List<Chip> all = new ArrayList<Chip>();
			for (String tag : note.getTags()) {
				all.add(new Chip(tag, "#" + tag, false));
			}
			List<Chip> row = place(fm, all, width, removeW);
			if (row.size() == all.size()) {
				return row;
			}
			// room for the "+N" control, sized for the largest count
			row = place(fm, all, width - (fm.stringWidth("+" + all.size()) + 2 * PAD_X) - GAP, removeW);
			int hidden = all.size() - row.size();
			Chip more = new Chip(null, "+" + hidden, true);
			int x = row.isEmpty() ? 0 : row.get(row.size() - 1).bounds.x + row.get(row.size() - 1).bounds.width + GAP;
			more.bounds.setBounds(x, 0, fm.stringWidth(more.label) + 2 * PAD_X, h);
			row.add(more);
			return row;
		}

		/** Whole chips from the left while they fit before the right edge */
		private List<Chip> place(FontMetrics fm, List<Chip> chips, int right, int removeW) {
			List<Chip> out = new ArrayList<Chip>();
			int x = 0;
			for (Chip chip : chips) {
				int w = Math.max(fm.stringWidth(chip.label) + 2 * PAD_X, removeW);
				if (x + w > right) {
					break;
				}
				chip.bounds.setBounds(x, 0, w, chipHeight());
				out.add(chip);
				x += w + GAP;
			}
			return out;
		}

		private String shortLabel(FontMetrics fm, String label, int maxW) {
			if (fm.stringWidth(label) <= maxW) {
				return label;
			}
			String s = label;
			while (s.length() > 1 && fm.stringWidth(s + "…") > maxW) {
				s = s.substring(0, s.length() - 1);
			}
			return s + "…";
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			CardPalette.antialias(g2);
			g2.setFont(getFont());
			FontMetrics fm = g2.getFontMetrics();
			for (Chip chip : layout(getWidth())) {
				Rectangle r = chip.bounds;
				boolean hot = chip.tag != null && chip.tag.equals(hoveredTag);
				g2.setColor(hot ? removeColor : palette.card);
				g2.fillRoundRect(r.x, r.y, r.width - 1, r.height - 1, r.height, r.height);
				if (!hot) {
					g2.setColor(palette.border);
					g2.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, r.height, r.height);
				}
				g2.setColor(hot ? Color.WHITE : palette.text);
				g2.drawString(hot ? REMOVE : chip.shown, r.x + PAD_X, r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
			}
			g2.dispose();
		}
	}

	/** Entry of the workspace combo */
	private static final class WorkspaceItem {
		final String id;
		final String label;

		WorkspaceItem(String id, String label) {
			this.id = id;
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	/** Borderless glyph button (close, delete) */
	private final class FlatButton extends JButton {
		private static final long serialVersionUID = 1L;

		private final boolean danger;

		FlatButton(String text, boolean danger) {
			super(text);
			this.danger = danger;
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
			setFocusable(false);
			setOpaque(false);
			setMargin(new java.awt.Insets(0, 4, 0, 4));
			setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			getModel().addChangeListener(e -> repaint());
		}

		@Override
		protected void paintComponent(Graphics g) {
			setForeground(getModel().isRollover() ? (danger ? palette.danger : palette.text) : palette.muted);
			super.paintComponent(g);
		}
	}

	/** Round Keep-style checkbox */
	private final class CheckIcon implements Icon {
		private static final int SIZE = 18;
		private final boolean checked;
		private final boolean rollover;

		CheckIcon(boolean checked, boolean rollover) {
			this.checked = checked;
			this.rollover = rollover;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g.create();
			CardPalette.antialias(g2);
			g2.translate(x, y);
			if (checked) {
				g2.setColor(palette.accent);
				g2.fillOval(1, 1, SIZE - 2, SIZE - 2);
				g2.setColor(palette.onAccent);
				g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				Path2D tick = new Path2D.Float();
				tick.moveTo(5, 9.5);
				tick.lineTo(8, 12.5);
				tick.lineTo(13, 6.5);
				g2.draw(tick);
			} else {
				g2.setColor(rollover ? palette.accent : palette.muted);
				g2.setStroke(new BasicStroke(1.6f));
				g2.drawOval(1, 1, SIZE - 3, SIZE - 3);
			}
			g2.dispose();
		}

		@Override
		public int getIconWidth() {
			return SIZE;
		}

		@Override
		public int getIconHeight() {
			return SIZE;
		}
	}

	/** Runs the same callback for every document change */
	private static final class SimpleDocumentListener implements DocumentListener {
		private final Runnable onChange;

		SimpleDocumentListener(Runnable onChange) {
			this.onChange = onChange;
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			onChange.run();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			onChange.run();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			onChange.run();
		}
	}
}
