/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
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
package gui.texteditor.commons.filesearch.cli;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import gui.commons.frameutils.frame.terminal.TerminalPanel;
import various.common.light.i18n.I18n;
import various.common.light.utility.log.SafeLogger;

/**
 * Search in files at full speed: the command line search runs in a terminal of ScreepT (its own JVM, no results tree),
 * with colours and its live output. Enter, when it ends, runs it again.
 */
public class SearchTerminalFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final SafeLogger logger = new SafeLogger(SearchTerminalFrame.class);

	private final TerminalPanel terminal = new TerminalPanel();
	private final String[] command;
	private final String directory;

	private SearchTerminalFrame(String text, String[] command, String directory) {
		super(I18n.t("fsearch.terminal_title") + " - " + text);
		this.command = command;
		this.directory = directory;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(terminal, BorderLayout.CENTER);
		setMinimumSize(new Dimension(500, 300));
		setSize(1100, 650);
		terminal.setRestartAction(this::run);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				terminal.dispose();
			}
		});
	}

	private void run() {
		try {
			terminal.startCommand(directory, command);
			terminal.focusTerminal();
		} catch (IOException e) {
			logger.error("Cannot start the search in the terminal", e);
		}
	}

	/**
	 * Opens a terminal window that runs the command line search with these arguments.
	 * @param cliArgs the arguments of {@link SearchCli} (text, options, folders)
	 */
	public static SearchTerminalFrame open(Component parent, String text, List<String> cliArgs) {
		List<String> cmd = new ArrayList<>();
		String javaHome = System.getProperty("java.home");
		File java = new File(javaHome, "bin" + File.separator + (File.separatorChar == '\\' ? "java.exe" : "java"));
		cmd.add(java.getAbsolutePath());
		cmd.add("-Dfile.encoding=UTF-8");
		cmd.add("-XX:+UseParallelGC");
		cmd.add("-cp");
		cmd.add(absoluteClassPath());
		cmd.add(SearchCli.class.getName());
		cmd.add("--color");
		cmd.add("--stats");
		cmd.addAll(cliArgs);

		SearchTerminalFrame frame = new SearchTerminalFrame(text, cmd.toArray(new String[0]), System.getProperty("user.dir"));
		frame.setLocationRelativeTo(parent);
		frame.setVisible(true);
		frame.run();
		return frame;
	}

	/** The class path of this JVM with absolute entries: the search runs in its own process, from another folder */
	static String absoluteClassPath() {
		String base = System.getProperty("user.dir");
		StringBuilder sb = new StringBuilder();
		for (String entry : System.getProperty("java.class.path", "").split(File.pathSeparator)) {
			if (entry.trim().isEmpty())
				continue;
			boolean wildcard = entry.endsWith("*");
			String path = wildcard ? entry.substring(0, entry.length() - 1) : entry;
			File f = new File(path.isEmpty() ? "." : path);
			String abs = (f.isAbsolute() ? f : new File(base, f.getPath())).toPath().toAbsolutePath().normalize().toString();
			if (wildcard)
				abs = abs.endsWith(File.separator) ? abs + "*" : abs + File.separator + "*";
			if (sb.length() > 0)
				sb.append(File.pathSeparator);
			sb.append(abs);
		}
		return sb.toString();
	}
}
