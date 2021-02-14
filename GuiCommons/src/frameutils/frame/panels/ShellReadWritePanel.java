package frameutils.frame.panels;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import frameutils.frame.panels.arch.ParentPanel;
import gui.GuiUtils;
import gui.RXTextUtilities;
import net.miginfocom.swing.MigLayout;
import om.OSinfo;
import utility.string.StringWorker.EOL;

import javax.swing.JSplitPane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a console that prints out every output from System.out/System.err streams
 * @author Alessio Moraschini
 *
 */
public class ShellReadWritePanel extends ParentPanel {
	private static final long serialVersionUID = -351031239684511014L;

	public int maxLines;
	
	private JScrollPane scrollPaneBottom;
	private JScrollPane scrollPaneTop;
	
	private JSplitPane splitPane;

	private PlayStopPausePanel headButtonsPanel;
	private JTextArea commandTextArea;
	private JTextArea outputTextArea;
	
	private boolean aoutoScrollEnabled;
	
	private Process process;
	

	public ShellReadWritePanel() {
		this(1000);
	}
	
	public ShellReadWritePanel(int visibleLines) {
		maxLines = visibleLines;
		aoutoScrollEnabled = true;
		super.init();
	}
	
	
	public void initGui() {
		setLayout(new MigLayout("fill, insets 1, hidemode 2", "[60px][50px,grow][50px,grow][50px,grow]", "[35px:35px][50px,grow]"));
		
		// HEADER
		headButtonsPanel = new PlayStopPausePanel(playAction(), stopAction(), pauseAction());
		headButtonsPanel.setPauseVisible(false);
		headButtonsPanel.setVisible(true);
		headButtonsPanel.setOpaque(true);
		add(headButtonsPanel, "cell 0 0 1 1,grow");
		
		// SPLIT PANE
		splitPane = new JSplitPane();
		splitPane.setDividerLocation(140);
		splitPane.setContinuousLayout(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, "cell 0 1 4 1,grow");

		
		// TOP SCROLL PANEL
		scrollPaneTop = getScrollPane(true, true);
		scrollPaneTop.setPreferredSize(new Dimension(1920, 1080));
		splitPane.setTopComponent(scrollPaneTop);
		
		commandTextArea = new JTextArea();
		commandTextArea.setMargin(new Insets(4, 4, 4, 4));
		commandTextArea.setFont(ParentPanel.DEFAULT_TEXT_AREA_FONT);
		commandTextArea.setForeground(Color.WHITE);
		commandTextArea.setBackground(Color.DARK_GRAY);
		commandTextArea.setCaretColor(Color.WHITE);
		scrollPaneTop.setViewportView(commandTextArea);
		
		
		// BOTTOM SCROLL PANEL
		scrollPaneBottom = getScrollPane(true, true);
		scrollPaneBottom.setPreferredSize(new Dimension(1920, 1080));
		splitPane.setBottomComponent(scrollPaneBottom);
		
		outputTextArea = new JTextArea();
		outputTextArea.setFont(ParentPanel.DEFAULT_TEXT_AREA_FONT);
		outputTextArea.setForeground(Color.LIGHT_GRAY);
		outputTextArea.setBackground(Color.DARK_GRAY);
		outputTextArea.setMargin(new Insets(4, 4, 4, 4));
		outputTextArea.setEditable(false);
		outputTextArea.setFont(ParentPanel.DEFAULT_TEXT_AREA_FONT);
		outputTextArea.setText("I'm ready to launch your commands ;)");
		scrollPaneBottom.setViewportView(outputTextArea);
	}
	
	public void addHandlers() {
		// For now no component listener are needed
	}
	
	private Runnable stopAction() {
		return () -> {
			GuiUtils.launchThreadSafeSwing(() -> {
				try {
					if(process != null)
						stopProcess(process);
					
				} catch (Exception e) {
					logger.error("Error launching command", e);
					dialogHelper.error("Can't stop command!");
				}
			});
		};
	}
	
	private Runnable playAction() {
		return () -> {
			GuiUtils.launchThreadSafeSwing(() -> {
				try {
					String command = RXTextUtilities.getSelectedOrBlockText(commandTextArea, true, " && ");
					process = launchCommand(command);
					outputTextArea.append("\n\n ##############################################\n\n >> Command Launched: " + command + "\n\n");
					readProcessOutput(process.getInputStream(), false);
					readProcessOutput(process.getErrorStream(), true);
					process.waitFor();
					headButtonsPanel.initButtons();
					
				} catch (BadLocationException | IOException | InterruptedException e) {
					logger.error("Error launching command", e);
					dialogHelper.error("Error launching command!");
				}
			});
		};
	}
	
	private Runnable pauseAction() {
		return () -> {};
	}
	
	private Process launchCommand(String command) throws IOException {
		List<String> commands = new ArrayList<>();
		if(new OSinfo().isWindows()) {
			commands.add("cmd.exe");
			commands.add("/C");
		}
		commands.add(command);
		
		ProcessBuilder builder = new ProcessBuilder(commands);
		return builder.start();
	}

	private void stopProcess(Process process) throws InterruptedException {
		if(process != null)
			process.destroyForcibly().waitFor();
	}
	
	private void readProcessOutput(InputStream inputStream, boolean errorStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				outputTextArea.append(errorStream ? ("ERROR: " + line) : line);
				outputTextArea.append(EOL.defaultEol.eol);
				if(aoutoScrollEnabled)
					outputTextArea.setCaretPosition(outputTextArea.getDocument().getLength()-1);
			}
			reader.close();
		} catch (IOException e) {
			logger.error("Error reading process output:\n\n", e);
			outputTextArea.append("!! Error reading process output:\n\n");
		}
	}
	
}
