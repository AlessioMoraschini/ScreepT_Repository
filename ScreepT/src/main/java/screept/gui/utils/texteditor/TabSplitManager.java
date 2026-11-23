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
package screept.gui.utils.texteditor;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import gui.commons.tabbedpaneutils.CustomTabbedPaneUI_v1;
import gui.commons.tabs.TabImplementor;
import gui.commons.tabs.TabImplementors;
import gui.texteditor.commons.om.TabTextEditorObject;
import gui.texteditor.commons.tabbededitor.CustomTabbedEditor;
import screept.gui.panels.TextEditor;

/**
 * Holds the tabs of the file editor and, when the user drags a tab to the right side of the content, a second panel
 * (another {@link CustomTabbedEditor}) that shows beside the first one. While the tab is dragged the area where it
 * would land is lit. The panel with the focus is the one the rest of the editor works on
 * ({@link TextEditor#getCurrentTabEditor()}). The second panel closes when its last tab goes away.
 * <p>
 * A moved tab is reopened in the other panel (a text tab keeps its unsaved text and caret, a viewer tab is reopened
 * on its file) and closed in the first: the undo history of the text does not travel with it.
 */
public class TabSplitManager {

	private static final Logger logger = Logger.getLogger(TabSplitManager.class);

	/** Share of the width, from the right, where a dragged tab opens the second panel */
	private static final int RIGHT_ZONE_PERCENT = 40;

	private final TextEditor textEditor;
	private final CustomTabbedEditor primary;
	private final JPanel tabsArea = new JPanel(new BorderLayout());
	private final Overlay overlay = new Overlay();
	private CustomTabbedEditor secondary;
	private JSplitPane split;
	private AWTEventListener pressListener;

	public TabSplitManager(TextEditor textEditor, CustomTabbedEditor primary) {
		this.textEditor = textEditor;
		this.primary = primary;
		tabsArea.setOpaque(false);
		tabsArea.setBorder(BorderFactory.createEmptyBorder());
		tabsArea.add(primary.getTabsComponent(), BorderLayout.CENTER);
	}

	/** What goes in the window in place of the tabs of the first editor */
	public JComponent getComponent() {
		return tabsArea;
	}

	public boolean hasSplit() {
		return secondary != null;
	}

	// ------------------------------------------------------------------ drag and drop

	private static Rectangle screenBounds(Component c) {
		return c != null && c.isShowing() ? new Rectangle(c.getLocationOnScreen(), c.getSize()) : new Rectangle();
	}

	/** Where a dragged tab would land if released at the given point, null if nowhere (the normal reorder applies) */
	private Target targetFor(CustomTabbedEditor source, int index, Point screenPoint) {
		if (index < 0 || index >= source.loadedList.getSize()) {
			return null;
		}
		TabTextEditorObject tab = source.loadedList.getList().get(index);
		if (tab.isEmbeddedViewer() && tab.viewer.isModified()) {
			return null; // a viewer with edits not saved stays where it is
		}
		if (tab.isEncrypted()) {
			return null; // reopening would read the encrypted bytes as text (and a save would write them back in plain form)
		}
		Rectangle full = screenBounds(tabsArea);
		if (!full.contains(screenPoint)) {
			return null;
		}
		if (secondary == null) {
			// one panel: the right part of the content opens the second one (only if something stays behind)
			if (source.loadedList.getSize() <= 1) {
				return null;
			}
			Rectangle content = source.getContentScreenBounds();
			int zoneX = full.x + full.width * (100 - RIGHT_ZONE_PERCENT) / 100;
			if (content == null || !content.contains(screenPoint) || screenPoint.x < zoneX) {
				return null;
			}
			return new Target(null, new Rectangle(zoneX, full.y, full.x + full.width - zoneX, full.height));
		}
		// two panels: the other one, whole
		for (CustomTabbedEditor editor : textEditor.tabbedEditorsList) {
			if (editor == source) {
				continue;
			}
			Rectangle holder = screenBounds(editor.getTabsComponent());
			if (holder.contains(screenPoint)) {
				if (source == primary && source.loadedList.getSize() <= 1) {
					return null; // the first panel never stays without tabs
				}
				return new Target(editor, holder);
			}
		}
		return null;
	}

	public boolean onDrag(CustomTabbedEditor source, int index, Point screenPoint) {
		Target target = targetFor(source, index, screenPoint);
		if (target == null) {
			overlay.hideOverlay();
			return false;
		}
		overlay.showOver(target.screenRect);
		return true;
	}

	public boolean onDrop(final CustomTabbedEditor source, final int index, Point screenPoint) {
		final Target target = targetFor(source, index, screenPoint);
		overlay.hideOverlay();
		if (target == null) {
			return false;
		}
		// after the release event: the header being dragged is going to disappear
		SwingUtilities.invokeLater(() -> {
			try {
				CustomTabbedEditor destination = target.editor != null ? target.editor : openSecondPanel();
				if (destination != null) {
					moveTab(source, index, destination);
				}
			} catch (Exception e) {
				logger.error("Cannot move the tab to the other panel", e);
			}
		});
		return true;
	}

	public void onDragEnd() {
		overlay.hideOverlay();
	}

	// ------------------------------------------------------------------ panels

	/** At start: opens the second panel with the files it had when the application was closed (nothing if the list is empty) */
	public void restoreSecondPanel(java.util.List<String> paths, java.util.List<String> viewerEntries) {
		if (paths == null || paths.isEmpty() || secondary != null) {
			return;
		}
		CustomTabbedEditor editor = null;
		for (String path : paths) {
			File file = new File(path);
			if (!file.isFile()) {
				continue;
			}
			if (editor == null) {
				editor = openSecondPanel();
			}
			setCurrent(editor);
			editor.alreadyLoading = true;
			try {
				if (!editor.restoreViewerTab(file, viewerEntries)) {
					editor.addTab(file);
				}
			} catch (Exception e) {
				logger.warn("Cannot restore " + file + " in the second panel", e);
			} finally {
				editor.alreadyLoading = false;
			}
		}
		if (editor != null && editor.loadedList.getSize() == 0) {
			closeSecondPanel();
		}
		setCurrent(primary);
		primary.tabRefreshAfterUpdate();
	}

	private CustomTabbedEditor openSecondPanel() {
		if (secondary != null) {
			return secondary;
		}
		CustomTabbedEditor editor = new CustomTabbedEditor(textEditor, textEditor.currentConfiguration, new TabbedEditorOwnerScreepT(textEditor));
		// same order of the first panel (style, then scroll layout), so the tab strip sits at the same height
		editor.mainTabList.setBackground(primary.mainTabList.getBackground());
		editor.applyStyleToTabs();
		editor.mainTabList.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
		textEditor.tabbedEditorsList.add(editor);
		secondary = editor;

		tabsArea.removeAll();
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, primary.getTabsComponent(), editor.getTabsComponent());
		split.setResizeWeight(0.5);
		split.setContinuousLayout(true);
		split.setBorder(BorderFactory.createEmptyBorder());
		split.setBackground(primary.mainTabList.getBackground());
		tabsArea.add(split, BorderLayout.CENTER);
		tabsArea.revalidate();
		SwingUtilities.invokeLater(() -> {
			if (split != null) {
				split.setDividerLocation(0.5);
			}
		});

		// the panel the user presses (tabs, text, viewers) is the one the editor works on
		pressListener = ev -> {
			if (ev.getID() == MouseEvent.MOUSE_PRESSED && ev.getSource() instanceof Component) {
				Component source = (Component) ev.getSource();
				for (CustomTabbedEditor candidate : textEditor.tabbedEditorsList) {
					if (SwingUtilities.isDescendingFrom(source, candidate.getTabsComponent())) {
						activate(candidate);
						return;
					}
				}
			}
		};
		Toolkit.getDefaultToolkit().addAWTEventListener(pressListener, AWTEvent.MOUSE_EVENT_MASK);
		return editor;
	}

	private void closeSecondPanel() {
		CustomTabbedEditor editor = secondary;
		if (editor == null) {
			return;
		}
		if (pressListener != null) {
			Toolkit.getDefaultToolkit().removeAWTEventListener(pressListener);
			pressListener = null;
		}
		for (TabTextEditorObject tab : new java.util.ArrayList<TabTextEditorObject>(editor.loadedList.getList())) {
			try {
				tab.releaseViewer();
				tab.deleteTempFile();
			} catch (Exception e) {
				logger.warn("Cannot release a tab of the closed panel", e);
			}
		}
		try {
			editor.changeMonManager.finalize();
		} catch (Throwable e) {
			logger.warn("Cannot stop the file monitor of the closed panel", e);
		}
		textEditor.tabbedEditorsList.remove(editor);
		secondary = null;
		split = null;
		textEditor.currentTabbedEditorSelected = 0;
		tabsArea.removeAll();
		tabsArea.add(primary.getTabsComponent(), BorderLayout.CENTER);
		tabsArea.revalidate();
		tabsArea.repaint();
	}

	/** Makes the editor the one that receives the commands of the window (the tab bar of the other one stays as it is) */
	private void activate(final CustomTabbedEditor editor) {
		int index = textEditor.tabbedEditorsList.indexOf(editor);
		if (index < 0 || index == textEditor.currentTabbedEditorSelected) {
			return;
		}
		textEditor.currentTabbedEditorSelected = index;
		// the toolbar, the footer, the tree... follow the tab in front of the new panel
		SwingUtilities.invokeLater(() -> {
			if (textEditor.tabbedEditorsList.contains(editor) && editor.loadedTabObj != null) {
				editor.tabRefreshAfterUpdate();
			}
		});
	}

	/** The second panel lost its last tab: it is not needed any more */
	public void onLastTabClosed(CustomTabbedEditor editor) {
		if (editor == secondary) {
			closeSecondPanel();
			primary.tabRefreshAfterUpdate();
		}
	}

	// ------------------------------------------------------------------ the move

	private void setCurrent(CustomTabbedEditor editor) {
		int index = textEditor.tabbedEditorsList.indexOf(editor);
		if (index >= 0) {
			textEditor.currentTabbedEditorSelected = index;
		}
	}

	/** Opens the content of the tab in the destination: viewers on their file, text with what is not on disk and the caret */
	private boolean reopen(TabTextEditorObject tab, CustomTabbedEditor destination) {
		File file = tab.loadedFile;
		if (tab.isEmbeddedViewer()) {
			TabImplementor implementor = TabImplementors.byId(tab.viewerId);
			return implementor != null && file != null && destination.openInViewerTab(file, implementor);
		}
		String text = tab.textArea.getText();
		int caret = tab.textArea.getCaretPosition();
		boolean fileOnDisk = file != null && file.exists();
		TabTextEditorObject copy = destination.addTab(fileOnDisk ? file : null);
		if (copy == null) {
			return false;
		}
		if (tab.unsaved || !fileOnDisk) {
			// what is not on disk travels with the tab
			copy.textArea.setText(text);
			copy.unsaved = tab.unsaved || !text.isEmpty();
			destination.updateSavedLabelNTab(destination.geIndexOfTabObj(copy));
		}
		try {
			copy.textArea.setCaretPosition(Math.min(caret, copy.textArea.getDocument().getLength()));
		} catch (Exception e) {
			logger.debug("Cannot restore the caret in the moved tab", e);
		}
		return true;
	}

	private void moveTab(CustomTabbedEditor source, int index, CustomTabbedEditor destination) {
		TabTextEditorObject tab = source.loadedList.getList().get(index);
		boolean wasLast = source.loadedList.getSize() == 1;

		// the new tab is created while the destination is the current editor, so the window follows it
		setCurrent(destination);
		// loading text must not be taken for a user edit: it would mark (and rename) the tab that was selected before
		destination.alreadyLoading = true;
		boolean created;
		try {
			created = reopen(tab, destination);
		} finally {
			destination.alreadyLoading = false;
		}
		if (!created) {
			setCurrent(source);
			return;
		}

		// the source tab is closed without questions: its content now lives in the other panel
		tab.unsaved = false;
		setCurrent(source);
		if (wasLast && source == secondary) {
			closeSecondPanel();
		} else {
			source.removeTab(index);
		}
		setCurrent(destination);
		destination.tabRefreshAfterUpdate();
	}

	// ------------------------------------------------------------------ helpers

	private static final class Target {
		final CustomTabbedEditor editor; // null = the second panel, to be opened
		final Rectangle screenRect;

		Target(CustomTabbedEditor editor, Rectangle screenRect) {
			this.editor = editor;
			this.screenRect = screenRect;
		}
	}

	/** Translucent rectangle over the area where the dragged tab would appear */
	private final class Overlay extends JComponent {
		private static final long serialVersionUID = 1L;

		Overlay() {
			setOpaque(false);
		}

		@Override
		public boolean contains(int x, int y) {
			return false; // never takes the mouse from the tab being dragged
		}

		void showOver(Rectangle screenRect) {
			javax.swing.JRootPane root = textEditor.getRootPane();
			if (root == null) {
				return;
			}
			JLayeredPane layers = root.getLayeredPane();
			if (getParent() != layers) {
				layers.add(this, JLayeredPane.DRAG_LAYER);
			}
			Point p = screenRect.getLocation();
			SwingUtilities.convertPointFromScreen(p, layers);
			Rectangle bounds = new Rectangle(p.x, p.y, screenRect.width, screenRect.height);
			if (!bounds.equals(getBounds())) {
				setBounds(bounds);
			}
			setVisible(true);
			repaint();
		}

		void hideOverlay() {
			java.awt.Container parent = getParent();
			if (parent != null) {
				Rectangle old = getBounds();
				parent.remove(this);
				parent.repaint(old.x, old.y, old.width, old.height);
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			try {
				Color accent = CustomTabbedPaneUI_v1.accentColor();
				g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 64));
				g2.fillRect(0, 0, getWidth(), getHeight());
				g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 210));
				g2.setStroke(new BasicStroke(3f));
				g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
			} finally {
				g2.dispose();
			}
		}
	}
}
