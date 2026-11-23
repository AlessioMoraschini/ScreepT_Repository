package start;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import gui.texteditor.commons.om.TabTextEditorObject;
import maingUI.MainGUI;
import maingUI.MainGuiGeneralAppStarter;
import maingUI.MainGuiManager;
import maingUI.MainGuiParent;
import resources.GeneralConfig;
import test.util.gui.AutoDialogCloser;
import test.util.gui.UiSweep;

/**
 * Entry point of the child JVM of {@link AppStartTest}: starts ScreepT through its real main method, uses the main
 * window like a user (opens the text editor, types in a new tab, opens the crypter), then exits through the real
 * exit routine. Every observation is written to the report file given as first argument, step after step, so the
 * parent test sees how far the application went even if it hangs or dies.
 */
public final class AppStartProbe {

	private static final Properties REPORT = new Properties();
	private static File reportFile;

	private AppStartProbe() {
	}

	private static synchronized void note(String key, Object value) {
		REPORT.setProperty(key, String.valueOf(value));
		try (OutputStream out = new FileOutputStream(reportFile)) {
			REPORT.store(out, "AppStartProbe");
		} catch (Exception e) {
			// the parent will notice the missing key
		}
	}

	private static <T> T onEdt(Callable<T> job) throws Exception {
		final Object[] box = new Object[2];
		SwingUtilities.invokeAndWait(() -> {
			try {
				box[0] = job.call();
			} catch (Throwable t) {
				box[1] = t;
			}
		});
		if (box[1] != null) {
			throw new Exception((Throwable) box[1]);
		}
		@SuppressWarnings("unchecked")
		T result = (T) box[0];
		return result;
	}

	private static boolean waitFor(Callable<Boolean> condition, long millis) throws Exception {
		long end = System.currentTimeMillis() + millis;
		while (System.currentTimeMillis() < end) {
			try {
				if (condition.call()) {
					return true;
				}
			} catch (Exception e) {
				// not ready yet
			}
			Thread.sleep(100);
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
		reportFile = new File(args[0]);
		note("step", "started");
		AutoDialogCloser.start();

		long t0 = System.currentTimeMillis();
		try {
			MainGuiGeneralAppStarter.main(new String[0]);
		} catch (Throwable t) {
			note("error", "main threw " + t);
			System.exit(3);
		}
		note("startMillis", System.currentTimeMillis() - t0);

		if (!waitFor(() -> MainGuiParent.mainFrameScreept != null && MainGuiParent.mainFrameScreept.isShowing(), 60_000)) {
			note("error", "the main window never became visible");
			System.exit(4);
		}
		final JFrame frame = MainGuiParent.mainFrameScreept;
		note("step", "window-visible");
		note("menuCount", onEdt(() -> frame.getJMenuBar() == null ? 0 : frame.getJMenuBar().getMenuCount()));
		note("frameWidth", frame.getWidth());
		note("mainAppRunning", MainGuiParent.mainAppRunning);
		note("appVersion", GeneralConfig.APP_VERSION);
		note("configLoaded", MainGuiParent.currentConfiguration != null);
		note("updatesManager", MainGuiGeneralAppStarter.updatesManager != null);
		note("pluginManager", MainGuiGeneralAppStarter.pluginManager != null);
		note("signalFileWhileRunning", GeneralConfig.SCREEPT_ACTIVE_SIGNAL_FILE.exists());
		note("signalFile", GeneralConfig.SCREEPT_ACTIVE_SIGNAL_FILE.getAbsolutePath());
		note("userHome", System.getProperty("user.home"));

		// text editor: the way a user gets there is the menu
		onEdt(() -> {
			MainGUI.mainAPP.getMntmTextEditor().doClick();
			return null;
		});
		boolean editorUp = waitFor(() -> MainGuiParent.textEditorPanel != null && MainGuiParent.textEditorPanel.isShowing(), 30_000);
		note("textEditorShowing", editorUp);
		if (editorUp) {
			final TabTextEditorObject tab = onEdt(() -> MainGuiParent.textEditorPanel.getCurrentTabEditor().addTab(null));
			note("newTabCreated", tab != null);
			if (tab != null) {
				onEdt(() -> {
					tab.textArea.setText("alpha\nbeta\ngamma");
					return null;
				});
				note("typedText", onEdt(() -> tab.textArea.getText()).replace("\r", "").replace("\n", "|"));
				note("tabCount", onEdt(() -> MainGuiParent.textEditorPanel.getCurrentTabEditor().mainTabList.getTabCount()));
				// nothing unsaved when the application is closed: the exit routine would ask what to do with the text
				onEdt(() -> {
					tab.textArea.setText("");
					return null;
				});
			}
		}

		// crypter panel
		onEdt(() -> {
			MainGUI.mainAPP.getMntmCrypter().doClick();
			return null;
		});
		note("crypterShowing", waitFor(() -> MainGuiParent.crypterPanel != null && MainGuiParent.crypterPanel.isShowing(), 30_000));

		// every entry of the menu bar, one after the other, like a user trying the whole application: what could hurt
		// (exit, program start, network, registry) is blocked by the guard, windows are closed after each entry
		SecurityManager before = UiSweep.installGuard();
		try {
			tourMenus(frame);
			tourPanels(frame);
		} finally {
			UiSweep.removeGuard(before);
		}

		note("step", "closing");
		MainGuiManager.closeApplication(0);
		// closeApplication ends with System.exit: reaching this line means the exit routine gave up
		note("error", "closeApplication returned without exiting");
		System.exit(5);
	}

	/** Entries that end or restart the application, or install something: not part of the tour. */
	private static final Pattern SKIP_ITEM = Pattern.compile("(?i).*(exit|quit|restart|kill|install|reboot|shutdown|logoff|license|context menu|visit website|temp folder).*");

	private static void collect(JMenu menu, String path, List<JMenuItem> items, List<String> labels) {
		for (java.awt.Component c : menu.getMenuComponents()) {
			if (c instanceof JMenu) {
				collect((JMenu) c, path + ((JMenu) c).getText() + " > ", items, labels);
			} else if (c instanceof JMenuItem) {
				items.add((JMenuItem) c);
				labels.add(path + ((JMenuItem) c).getText());
			}
		}
	}

	private static void closeOtherWindows(final JFrame keep) {
		SwingUtilities.invokeLater(() -> {
			for (Window w : Window.getWindows()) {
				if (w != keep && w.isShowing()) {
					w.dispose();
				}
			}
		});
	}

	/** One click on the EDT, on a helper thread so that a hang is detected; returns what the click threw, or null. */
	private static Throwable click(final JMenuItem item, String label, List<String> stuck) throws InterruptedException {
		final Throwable[] error = new Throwable[1];
		Thread clicker = new Thread(() -> {
			try {
				SwingUtilities.invokeAndWait(() -> item.doClick(0));
			} catch (Throwable t) {
				error[0] = t;
			}
		}, "tour-click");
		clicker.setDaemon(true);
		clicker.start();
		clicker.join(6_000);
		if (clicker.isAlive()) {
			stuck.add(label);
			clicker.interrupt();
		}
		return error[0];
	}

	/** Every button, tab, combo, tree and field of the real panels with the real state, one panel after the other. */
	private static void tourPanels(final JFrame frame) throws Exception {
		final java.awt.Component[] panels = new java.awt.Component[4];
		onEdt(() -> {
			MainGUI.mainAPP.getMntmTextEditor().doClick();
			panels[0] = MainGuiParent.textEditorPanel;
			MainGUI.mainAPP.getMntmCrypter().doClick();
			panels[1] = MainGuiParent.crypterPanel;
			panels[2] = MainGuiParent.configManagerPanel;
			panels[3] = frame.getContentPane();
			return null;
		});
		long[] millis = { 6_000, 4_000, 3_000, 6_000 };
		int touched = 0;
		for (int i = 0; i < panels.length; i++) {
			final java.awt.Component panel = panels[i];
			if (panel == null) {
				continue;
			}
			final long ms = millis[i];
			Thread t = new Thread(() -> {
				try {
					SwingUtilities.invokeAndWait(() -> UiSweep.interactFor(panel, ms));
				} catch (Throwable ignored) {
					// a panel that cannot be driven is reported by the missing coverage, not by a failure
				}
			}, "tour-panel");
			t.setDaemon(true);
			t.start();
			t.join(ms + 15_000);
			touched++;
			closeOtherWindows(frame);
			Thread.sleep(200);
		}
		note("panelsTouched", touched);
	}

	private static void tourMenus(final JFrame frame) throws Exception {
		final List<JMenuItem> items = new ArrayList<>();
		final List<String> labels = new ArrayList<>();
		onEdt(() -> {
			JMenuBar bar = frame.getJMenuBar();
			for (int i = 0; i < bar.getMenuCount(); i++) {
				if (bar.getMenu(i) != null) {
					collect(bar.getMenu(i), bar.getMenu(i).getText() + " > ", items, labels);
				}
			}
			return null;
		});
		note("tourEntries", items.size());
		int clicked = 0;
		int skipped = 0;
		List<String> stuck = new ArrayList<>();
		StringBuilder failed = new StringBuilder();
		StringBuilder slow = new StringBuilder();
		for (int i = 0; i < items.size(); i++) {
			final JMenuItem item = items.get(i);
			final String label = labels.get(i);
			if (SKIP_ITEM.matcher(label).matches() || !onEdt(() -> item.isEnabled() && item.getActionListeners().length > 0)) {
				skipped++;
				continue;
			}
			long startedAt = System.currentTimeMillis();
			// a click can meet a window that is still being closed by the previous one: one more try before calling it a failure
			Throwable error = click(item, label, stuck);
			if (error != null && stuck.isEmpty()) {
				closeOtherWindows(frame);
				Thread.sleep(300);
				error = click(item, label, stuck);
			}
			if (error != null) {
				Throwable root = error;
				while (root.getCause() != null) {
					root = root.getCause();
				}
				StackTraceElement top = root.getStackTrace().length > 0 ? root.getStackTrace()[0] : null;
				failed.append(label).append(" => ").append(root).append(" at ").append(top).append(" ; ");
			}
			clicked++;
			long took = System.currentTimeMillis() - startedAt;
			if (took > 1200) {
				slow.append(label).append(" ").append(took).append("ms ; ");
			}
			Thread.sleep(30);
			closeOtherWindows(frame);
		}
		note("tourClicked", clicked);
		note("tourSkipped", skipped);
		note("tourStuck", stuck);
		note("tourFailed", failed);
		note("tourSlow", slow);
		note("tourDialogs", AutoDialogCloser.closedDialogs());
	}
}
