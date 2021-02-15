package frameutils.frame.panels;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import frameutils.frame.om.ConsoleConcatenationMode;
import frameutils.frame.om.ConsoleConcatenationModeWindows;
import frameutils.frame.panels.arch.ParentPanel;
import frameutils.frame.panels.common.LoadNewSavePanel;
import frameutils.frame.panels.common.PlayStopPausePanel;
import gui.GuiUtils;
import gui.RXTextUtilities;
import net.miginfocom.swing.MigLayout;
import om.OSinfo;
import resources.IconsPathConfigurator;
import utility.string.StringWorker.EOL;

import javax.swing.JSplitPane;

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

	private PlayStopPausePanel playStopPausePanel;
	private LoadNewSavePanel loadNewSavePanel;
	public JButton clcButton;
	public JLabel labelConcatenationMode;
	public JComboBox<String> comboConcatenationMode;
	public JTextArea commandTextArea;
	private JTextArea outputTextArea;
	
	private boolean aoutoScrollEnabled;
	
	private Process process;
	
	private JFrame parentFrame;
	

	public ShellReadWritePanel() {
		this(1000, null);
	}
	
	public ShellReadWritePanel(int visibleLines, JFrame parentFrame) {
		maxLines = visibleLines;
		aoutoScrollEnabled = true;
		this.parentFrame = parentFrame;

		super.init();
	}
	
	@Override
	public void initGui() {
		setLayout(new MigLayout(
				"fill, insets 1, hidemode 2", 
				"[60px][60px][25px][160px:160px:160px][50px][50px,grow][20px]", 
				"[35px:35px][50px,grow]"));
		
		// HEADER
		playStopPausePanel = new PlayStopPausePanel(playAction(), stopAction(), pauseAction());
		playStopPausePanel.setPauseVisible(false);
		playStopPausePanel.setOpaque(true);
		add(playStopPausePanel, "cell 0 0 1 1,grow");

		// TODO: add save/saveAs/load actions
		loadNewSavePanel = new LoadNewSavePanel(null, null, null, null);
		loadNewSavePanel.setNewVisible(false);
		loadNewSavePanel.setOpaque(true);
		add(loadNewSavePanel, "cell 1 0 1 1,grow");
		
		clcButton = getButton("", null, null, IconsPathConfigurator.ICON_TEXT_CLEAR, true);
		add(clcButton, "cell 2 0 1 1,grow");
		
		labelConcatenationMode = new JLabel("Lines concatenation mode:");
		add(labelConcatenationMode, "cell 3 0 1 1,alignx right");
		
		comboConcatenationMode = new JComboBox<String>(getComboModes());
		add(comboConcatenationMode, "cell 4 0 1 1, alignx right");
		// TODO: add currentFilePath textField
		
		if(parentFrame != null) {
			add(getPinToTopButton(parentFrame), "cell 6 0 1 1, alignx right, grow");
		}
		
		// SPLIT PANE
		splitPane = new JSplitPane();
		splitPane.setDividerLocation(140);
		splitPane.setContinuousLayout(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, "cell 0 1 7 1,grow");

		
		// TOP SCROLL PANEL
		scrollPaneTop = getScrollPane(true, true);
		scrollPaneTop.setPreferredSize(new Dimension(1920, 1080));
		splitPane.setTopComponent(scrollPaneTop);
		
		commandTextArea = getTextArea(Color.DARK_GRAY, Color.WHITE, Color.WHITE, ParentPanel.DEFAULT_TEXT_AREA_FONT);
		scrollPaneTop.setViewportView(commandTextArea);
		
		
		// BOTTOM SCROLL PANEL
		scrollPaneBottom = getScrollPane(true, true);
		scrollPaneBottom.setPreferredSize(new Dimension(1920, 1080));
		splitPane.setBottomComponent(scrollPaneBottom);
		
		outputTextArea = getTextArea(Color.DARK_GRAY, Color.WHITE, Color.WHITE, ParentPanel.DEFAULT_TEXT_AREA_FONT);
		outputTextArea.setEditable(false);
		outputTextArea.setText("I'm ready to launch your commands ;)");
		scrollPaneBottom.setViewportView(outputTextArea);
		
	}
	
	@Override
	public void addHandlers() {
		clcButton.addActionListener((e) -> {
			outputTextArea.setText("");
		});
	}
	
	private Runnable stopAction() {
		return () -> {
			GuiUtils.launchThreadSafeSwing(() -> {
				try {
					if(process != null)
						stopProcess(process);
					
				} catch (Exception e) {
					logger.error("Error stopping command", e);
					dialogHelper.error("Can't stop command!");
				}
			});
		};
	}
	
	private Runnable playAction() {
		return () -> {
			GuiUtils.launchThreadSafeSwing(() -> {
				try {
					String command = RXTextUtilities.getSelectedOrBlockText(commandTextArea, true, (String)comboConcatenationMode.getSelectedItem());
					process = launchCommand(command);
					outputTextArea.append("\n\n ##############################################\n\n >> Command Launched: " + command + "\n\n");
					readProcessOutput(process.getInputStream(), false);
					readProcessOutput(process.getErrorStream(), true);
					process.waitFor();
					
				} catch (BadLocationException | IOException | InterruptedException e) {
					logger.error("Error launching command", e);
					dialogHelper.error("Error launching command!");
				
				} finally {
					playStopPausePanel.initButtons();
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
	
	private void readProcessOutput(InputStream inputStream, boolean errorStream) throws UnsupportedEncodingException {
//		Charset charset = Charset.forName(new OSinfo().isWindows() && Charset.isSupported("UTF-16LE") 
//				? "UTF-16" 
//				: "UTF-8");
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
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
	
	private String[] getComboModes() {
		if(new OSinfo().isWindows())
			return ConsoleConcatenationModeWindows.values(false);
		else 
			return ConsoleConcatenationMode.values(false);
	}
}
