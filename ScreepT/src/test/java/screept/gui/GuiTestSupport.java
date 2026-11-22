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
package screept.gui;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.ini4j.Wini;

import gui.commons.dialogutils.JOptionHelperExtended;
import initializer.configs.impl.INItializer;
import maingUI.MainGUI;
import maingUI.MainGuiParent;
import resources.GeneralConfig;

/**
 * Shared plumbing of the GUI tests: a real configuration bound to a temporary ini (never the user's one), the static
 * state of the application window faked without building it, dialogs that never appear, and EDT/reflection helpers.
 * Nothing here ever shows a window.
 */
public final class GuiTestSupport {

	private GuiTestSupport() {
	}

	// ------------------------------------------------------------------ dialogs

	/** What a dialog was asked to show */
	public static final class Shown {
		public final int type;
		public final String title;
		public final String message;

		Shown(int type, String title, Object message) {
			this.type = type;
			this.title = title;
			this.message = String.valueOf(message);
		}

		@Override
		public String toString() {
			return type + "|" + title + "|" + message;
		}
	}

	/**
	 * Replaces every dialog of the application: notices are recorded, questions are answered from scripted queues.
	 * A question or input with no scripted answer fails the test (it would block on a real modal dialog).
	 */
	public static final class ScriptedDialogs extends JOptionHelperExtended {
		public final List<Shown> notices = new ArrayList<Shown>();
		public final List<String> questions = new ArrayList<String>();
		/** answers of yes/no/cancel questions: TRUE yes, FALSE no, null cancel */
		public final LinkedList<Boolean> answers = new LinkedList<Boolean>();
		/** answers of text input questions (null = cancelled) */
		public final LinkedList<String> inputs = new LinkedList<String>();

		public ScriptedDialogs(Component parent) {
			super(parent, true);
		}

		public void reset() {
			notices.clear();
			questions.clear();
			answers.clear();
			inputs.clear();
		}

		public int count(int type) {
			int n = 0;
			for (Shown s : notices) {
				if (s.type == type) {
					n++;
				}
			}
			return n;
		}

		public int errors() {
			return count(ERR);
		}

		public int warnings() {
			return count(WARN);
		}

		public int infos() {
			return count(INFO);
		}

		@Override
		protected void showMessageCommon(Object msg, String title, int msgType, Object[] choiceOptions, Object defaultOption) {
			notices.add(new Shown(msgType, title, msg));
		}

		@Override
		public Boolean showYNMessageCommon(Object msg, String title, int msgType, boolean allowNull, boolean warningType, boolean cancEnabled) {
			questions.add(title + "|" + msg);
			if (answers.isEmpty()) {
				throw new AssertionError("unexpected question: " + title + " | " + msg);
			}
			return answers.removeFirst();
		}

		@Override
		public Boolean showYNMessageCommonOld(Object msg, String title, int msgType, boolean allowNull, boolean warningType, boolean cancEnabled) {
			return showYNMessageCommon(msg, title, msgType, allowNull, warningType, cancEnabled);
		}

		@Override
		public String showYNMessageCommon(Object msg, String title, int msgType, boolean warningType, String[] optionsAvailable, int initialOptionIndex) {
			throw new AssertionError("unexpected multi-option question: " + title);
		}

		@Override
		public String askForStringNullable(Object msg, String title, String initialValue, Object[] options) {
			questions.add(title + "|" + msg);
			if (inputs.isEmpty()) {
				throw new AssertionError("unexpected input question: " + title + " | " + msg);
			}
			return inputs.removeFirst();
		}

		@Override
		public Choice<char[]> askForPasswordNullable(String msg, String title, int maxLength, Object[] options) {
			throw new AssertionError("unexpected password question: " + title);
		}

		@Override
		public Object errorScroll(String msg, String title, Throwable e, Object[] options, int msgOption) {
			notices.add(new Shown(ERR, title, msg));
			return null;
		}

		@Override
		public Object warnScroll(String msg, String title, Throwable e, Object[] options, int msgOption) {
			notices.add(new Shown(WARN, title, msg));
			return null;
		}
	}

	// ------------------------------------------------------------------ fixture

	/**
	 * The faked application state of a test class: a never-shown main frame, an application object built without its heavy
	 * constructor, and a configuration bound to a temporary ini. {@link #close()} puts everything back.
	 */
	public static final class Fixture {
		public final INItializer conf;
		public final ScriptedDialogs dialogs;
		public final File iniFile;
		public final File workspace;
		public final MainGUI app;

		private final JFrame frame;
		private final MainGUI oldApp;
		private final JFrame oldFrame;
		private final INItializer oldConf;
		private final JOptionHelperExtended oldDialogHelper;
		private final boolean oldRunning;
		private final LookAndFeel oldLaf;
		private final String oldDefaultIni;
		private final String oldBackupIni;
		private final String oldIniFolder;
		private final String oldLauncherIni;
		public final File launcherIni;

		private Fixture(File dir) throws Exception {
			// fonts, sizes and paths of the application: normally filled at start-up, the panels read them
			if (GeneralConfig.lazyInitializedGlobalReference == null) {
				GeneralConfig.lazyInitializedGlobalReference = new GeneralConfig();
			}
			oldApp = MainGUI.mainAPP;
			oldFrame = MainGuiParent.mainFrameScreept;
			oldConf = MainGUI.currentConfiguration;
			oldDialogHelper = MainGUI.dialogHelper;
			oldRunning = MainGUI.mainAppRunning;
			oldLaf = UIManager.getLookAndFeel();
			oldDefaultIni = GeneralConfig.DEFAULT_INI;
			oldBackupIni = GeneralConfig.BACKUP_INI;
			oldIniFolder = GeneralConfig.DEFAULT_INI_FOLDER;
			oldLauncherIni = GeneralConfig.DEFAULT_LAUNCHER_INI_PATH;

			workspace = new File(dir, "workspace");
			workspace.mkdirs();
			File iniFolder = new File(dir, "INI_FILES");
			iniFolder.mkdirs();
			File backup = new File(dir, "BACKUP_CONFIG.ini");
			// the shipped template, copied: it is only read, then the session keys are emptied so no user file or tab can be reopened
			Files.copy(new File(INItializer.DEFAULT_INI_PATH).toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Wini seed = new Wini(backup);
			String ed = INItializer.TEXT_EDITOR_OPT;
			for (String key : new String[] { "loadedFilesAtStart", "pinnedFilesAtStart", "secondPanelFilesAtStart", "viewerTabsAtStart",
					"loadedTempFilesAtStart", "lastOpenedShellFiles", "lastOpenedFiles", "lastSelectedWorkspaces",
					"openedFileListCaretPosition", "selectedFileAtStart", "lastTagsUsed", "lastSearchTerms", "lastReplaceTerms" }) {
				seed.put(ed, key, "");
			}
			seed.put(ed, "defaultWorspaceLocation", workspace.getAbsolutePath());
			seed.put(ed, "firstTimeOpened", "false");
			seed.put(INItializer.GEN_OPT, "userName", "tester"); // an empty name would open the "who are you" dialog
			seed.store();

			iniFile = new File(dir, "CONFIG.ini");
			Files.copy(backup.toPath(), iniFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			// every write of the application goes to the temporary files, never to the real ones
			GeneralConfig.DEFAULT_INI = new File(dir, "DEFAULT_CONFIG.ini").getAbsolutePath();
			GeneralConfig.BACKUP_INI = backup.getAbsolutePath();
			GeneralConfig.DEFAULT_INI_FOLDER = iniFolder.getAbsolutePath() + File.separator;
			// the launcher ini (options window writes it): a temporary one with the one key the window handles
			launcherIni = new File(dir, "Launcher_Configuration.ini");
			Wini launcher = new Wini();
			launcher.put("APPLICATION_OPTIONS", "flagHideNStartDirect", "false");
			launcher.store(launcherIni);
			GeneralConfig.DEFAULT_LAUNCHER_INI_PATH = launcherIni.getAbsolutePath();

			Field u = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			u.setAccessible(true);
			// MainGUI is the application window: its constructor builds the whole application, the panels only need its frame
			app = (MainGUI) ((sun.misc.Unsafe) u.get(null)).allocateInstance(MainGUI.class);
			frame = new JFrame("test (never shown)");
			MainGuiParent.mainFrameScreept = frame;
			MainGUI.mainAPP = app;
			app.windowCreator = new screept.general.utils.WindowCreator(app); // only a holder: the panels ask it for sub-panels

			conf = new INItializer();
			conf.setIniFile(new Wini(iniFile));
			conf.setIniReadFile(new Wini(iniFile)); // Wini, as the application: a plain Ini would eat the backslashes of Windows paths
			conf.readConfigIniFile();
			MainGUI.currentConfiguration = conf;

			dialogs = new ScriptedDialogs(frame);
			MainGUI.dialogHelper = dialogs;
		}

		public JFrame frame() {
			return frame;
		}

		public void close() throws Exception {
			MainGUI.mainAppRunning = false; // stops the watcher threads some panels start
			edt(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					frame.dispose();
					return null;
				}
			});
			MainGUI.mainAPP = oldApp;
			MainGuiParent.mainFrameScreept = oldFrame;
			MainGUI.currentConfiguration = oldConf;
			MainGUI.dialogHelper = oldDialogHelper;
			MainGUI.mainAppRunning = oldRunning;
			GeneralConfig.DEFAULT_INI = oldDefaultIni;
			GeneralConfig.BACKUP_INI = oldBackupIni;
			GeneralConfig.DEFAULT_INI_FOLDER = oldIniFolder;
			GeneralConfig.DEFAULT_LAUNCHER_INI_PATH = oldLauncherIni;
			final LookAndFeel laf = oldLaf;
			edt(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					if (laf != null && UIManager.getLookAndFeel() != laf) {
						try {
							UIManager.setLookAndFeel(laf);
						} catch (Exception ignored) {
							// the look and feel is only cosmetic
						}
					}
					return null;
				}
			});
		}
	}

	/** Builds the fixture on the EDT; temporary files live in dir */
	public static Fixture open(final File dir) throws Exception {
		return edt(new Callable<Fixture>() {
			@Override
			public Fixture call() throws Exception {
				return new Fixture(dir);
			}
		});
	}

	// ------------------------------------------------------------------ EDT errors

	/**
	 * An event queue that keeps the exceptions thrown by invokeLater() code (the EDT would only print them), so a test can
	 * check that an action did not fail silently.
	 */
	public static final class EdtErrors extends EventQueue {
		public final List<Throwable> errors = Collections.synchronizedList(new ArrayList<Throwable>());

		@Override
		protected void dispatchEvent(AWTEvent event) {
			try {
				super.dispatchEvent(event);
			} catch (Throwable t) {
				errors.add(t);
			}
		}

		public static EdtErrors plug() {
			EdtErrors queue = new EdtErrors();
			Toolkit.getDefaultToolkit().getSystemEventQueue().push(queue);
			return queue;
		}

		public void unplug() {
			pop();
		}
	}

	// ------------------------------------------------------------------ EDT

	public static <T> T edt(final Callable<T> job) throws Exception {
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

	public static void edt(final Runnable job) throws Exception {
		edt(new Callable<Void>() {
			@Override
			public Void call() {
				job.run();
				return null;
			}
		});
	}

	/** Waits for everything already queued on the EDT (invokeLater calls included, one level deep) */
	public static void flushEdt() throws Exception {
		for (int i = 0; i < 3; i++) {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
				}
			});
		}
	}

	// ------------------------------------------------------------------ reflection

	private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
		for (Class<?> c = type; c != null; c = c.getSuperclass()) {
			try {
				Field f = c.getDeclaredField(name);
				f.setAccessible(true);
				return f;
			} catch (NoSuchFieldException next) {
				// look in the parent class
			}
		}
		throw new NoSuchFieldException(type.getName() + "." + name);
	}

	public static Object get(Object target, String name) throws Exception {
		return findField(target.getClass(), name).get(target);
	}

	public static void set(Object target, String name, Object value) throws Exception {
		findField(target.getClass(), name).set(target, value);
	}

	/** Calls a (possibly private) method by name and argument count, on the EDT */
	public static Object call(final Object target, final String name, final Object... args) throws Exception {
		Method found = null;
		for (Class<?> c = target.getClass(); c != null && found == null; c = c.getSuperclass()) {
			for (Method m : c.getDeclaredMethods()) {
				if (m.getName().equals(name) && m.getParameterTypes().length == args.length) {
					found = m;
					break;
				}
			}
		}
		if (found == null) {
			throw new NoSuchMethodException(target.getClass().getName() + "." + name);
		}
		final Method method = found;
		method.setAccessible(true);
		return edt(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				try {
					return method.invoke(target, args);
				} catch (InvocationTargetException e) {
					Throwable cause = e.getCause();
					if (cause instanceof Exception) {
						throw (Exception) cause;
					}
					throw new AssertionError(cause);
				}
			}
		});
	}
}
