package frameutils.frame.panels;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.apache.commons.io.FileUtils;

import files.FileVarious;
import files.FileWorker;
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
import utility.string.StringWorker;
import utility.string.StringWorker.EOL;
import utils.SysUtils;

import javax.swing.JSplitPane;

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
	public JCheckBox checkBoxConcatenation;
	public JCheckBox checkBoxAutoscroll;
	public JLabel labelConcatenationMode;
	public JLabel lblCurrentFile;
	public JComboBox<String> comboConcatenationMode;
	public JTextArea commandTextArea;
	private JTextArea outputTextArea;
	
	private boolean fileUnsaved;
	public File loadedFile;
	
	private Process process;
	
	private JFrame parentFrame;
	

	public ShellReadWritePanel() {
		this(1000, null);
	}
	
	public ShellReadWritePanel(int visibleLines, JFrame parentFrame) {
		maxLines = visibleLines;
		fileUnsaved = false;
		this.parentFrame = parentFrame;
		
		super.init();
	}
	
	@Override
	public void initGui() {
		setLayout(new MigLayout(
				"fill, insets 1, hidemode 2", 
				"[60px][60px][25px][120px][110px:110px:110px][50px][70px:70px][10px][60px, grow][20px]", 
				"[35px:35px][50px,grow]"));
		
		// HEADER
		playStopPausePanel = new PlayStopPausePanel(playAction(), stopAction(), pauseAction());
		playStopPausePanel.setPauseVisible(false);
		playStopPausePanel.setOpaque(true);
		add(playStopPausePanel, "cell 0 0 1 1,grow");

		loadNewSavePanel = new LoadNewSavePanel(loadAction(null), null, saveAction(null), saveAsAction());
		loadNewSavePanel.setNewVisible(false);
		loadNewSavePanel.setOpaque(true);
		add(loadNewSavePanel, "cell 1 0 1 1,grow");
		
		clcButton = getButton("", null, null, IconsPathConfigurator.ICON_TEXT_CLEAR, true);
		add(clcButton, "cell 2 0 1 1,grow");
		
		checkBoxAutoscroll = new JCheckBox("Autoscroll");
		checkBoxAutoscroll.setCursor(GuiUtils.CURSOR_HAND);
		add(checkBoxAutoscroll, "cell 3 0 1 1,grow");
		
		checkBoxConcatenation = new JCheckBox("Concatenate lines");
		checkBoxConcatenation.setCursor(GuiUtils.CURSOR_HAND);
		add(checkBoxConcatenation, "cell 4 0 1 1,grow");
		
		labelConcatenationMode = new JLabel("Lines concatenation mode:");
		add(labelConcatenationMode, "cell 5 0 1 1,alignx right");
		
		comboConcatenationMode = new JComboBox<String>(getComboModes());
		add(comboConcatenationMode, "cell 6 0 1 1, alignx right");

		lblCurrentFile = new JLabel("[No file loaded]");
		lblCurrentFile.setForeground(Color.DARK_GRAY);
		add(lblCurrentFile, "cell 8 0 1 1, alignx right");
		
		if(parentFrame != null) {
			add(getPinToTopButton(parentFrame), "cell 9 0 1 1, alignx right, grow");
		}
		
		// SPLIT PANE
		splitPane = new JSplitPane();
		splitPane.setDividerLocation(140);
		splitPane.setContinuousLayout(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, "cell 0 1 10 1,grow");

		
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
		
		checkBoxConcatenation.addActionListener((e)->{
			comboConcatenationMode.setEnabled(checkBoxConcatenation.isSelected());
		});
		GuiUtils.clickAndTriggerCheckbox(checkBoxConcatenation, true);

		GuiUtils.clickAndTriggerCheckbox(checkBoxAutoscroll, false);
		
		commandTextArea.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				onDocumentModification();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				onDocumentModification();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onDocumentModification();
			}
		});
	}
	
	private void onDocumentModification() {
		fileUnsaved = true;
		String fileName = lblCurrentFile.getText();
		if(!fileName.endsWith("(*)")) 
			lblCurrentFile.setText(fileName + "(*)");
	}

	private void onDocumentReset() {
		fileUnsaved = false;
		String fileName = lblCurrentFile.getText();
		if(fileName.endsWith("(*)")) 
			lblCurrentFile.setText(fileName.substring(0, fileName.length() - 3));
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
				String batFile = null;
				try {
					boolean concatenateSelectedLines = checkBoxConcatenation.isSelected();
					String command = concatenateSelectedLines 
							? RXTextUtilities.getSelectedOrBlockText(commandTextArea, true, (String)comboConcatenationMode.getSelectedItem())
							: RXTextUtilities.getSelectedOrBlockText(commandTextArea, true, EOL.LF.eol);
					
					if(concatenateSelectedLines) {
						outputTextArea.append("\n\n ##############################################\n\n "
								+ " >> Command Launched: \n\n" + command + "\n\n");
						process = launchCommand(command);
						readProcessOutput(process.getInputStream(), false);
						readProcessOutput(process.getErrorStream(), true);
						process.waitFor();
					} else {
						batFile = writeInitBatShAndGetPath(command + "\n exit");
						outputTextArea.append("\n\n ##############################################\n\n "
								+ " >> Temp file launched: \n\n" + batFile + "\n\n");
						process = SysUtils.launchBatchOrBashSimple(batFile);
						readProcessOutput(process.getInputStream(), false);
						readProcessOutput(process.getErrorStream(), true);
						process.waitFor();
					}
					
				} catch (BadLocationException | IOException | InterruptedException e) {
					logger.error("Error launching command", e);
					dialogHelper.error("Error launching command!");
				
				} finally {
					if(batFile != null)
					FileUtils.deleteQuietly(new File(batFile));
					playStopPausePanel.initButtons();
				}
			});
		};
	}
	
	private Runnable pauseAction() {
		return () -> {};
	}
	
	public Runnable loadAction(final File forceFile) {
		return () -> {
			
			File source = forceFile == null ? fileChooser.fileRead(null) : forceFile;
			
			if(askSave())
				loadNewSavePanel.getSaveButton().doClick();
			
			FileWorker.loadFileTxtArea(source, commandTextArea, lblCurrentFile, null, EOL.LF);
			loadedFile = source;
			lblCurrentFile.setToolTipText(loadedFile.getAbsolutePath());
			fileUnsaved = false;
		};
	}
	public Runnable saveAction(final File forceFile) {
		return () -> {
			if(saveFile(false))
				onDocumentReset();
		};
	}
	public Runnable saveAsAction() {
		return () -> {
			if(saveFile(true))
				onDocumentReset();
		};
	}
	
	
	private boolean saveFile(boolean saveAs) {
		boolean done = false;
		try {
			File selectedFile = saveAs || loadedFile == null || !loadedFile.isFile() || !loadedFile.exists() 
					? fileChooser.fileWrite(null, (loadedFile != null ? loadedFile.getName() : "ShellFile.txt"), FileVarious.getParent(loadedFile), null)
					: loadedFile;

			if (selectedFile != null) {
				
				String outString = StringWorker.normalizeStringToEol(commandTextArea.getText(), EOL.LF);
				
				if (selectedFile.exists()) {
					if (dialogHelper.yesOrNo("File " + selectedFile.getName()
							+ " exists, overwrite it?", "Overwrite?")) {
						done = FileWorker.writeStringToFile(outString, selectedFile, true);
					} 
				} else {
					done = selectedFile.createNewFile();
					PrintWriter out = new PrintWriter(selectedFile.getAbsolutePath());
					out.write(outString);
					out.close();
				}
				
				if (done) {
					String msgOk = selectedFile.getName() + " succesfully saved!";
					if(saveAs)
						dialogHelper.info(msgOk, "File Saved!");

					if (loadedFile == null) 
						loadedFile = selectedFile;
					
					lblCurrentFile.setText(loadedFile.getName());
					lblCurrentFile.setToolTipText(loadedFile.getCanonicalPath());
				
				} else {
					dialogHelper.error("Error while saving text to file");
				}
			}
			
		} catch (IOException e1) {
			dialogHelper.error("error Saving the selected file :(");
			logger.error("Exception happened!", e1);
		}
		
		return done;
	}
	
	private boolean askSave() {
		return dialogHelper.askYorNPrecond("Unsaved file", "Do you want to save current file before to load?", this, fileUnsaved);
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
	
	private static String writeInitBatShAndGetPath(String commandList) throws IOException {
		
		String initFilePath = (new OSinfo().isWindows())? "TEMP_SHELL_EXECUTABLE.bat" : "TEMP_SHELL_EXECUTABLE.sh";
		logger.info("Creating batch/bash file : " + initFilePath);
		
		// write commands to build bash/batch starter file
		boolean written = FileWorker.writeStringToFile(commandList, new File(initFilePath), false);
		
		String msg = (written)? "Write completed correctly :)" : "Write not succeded for some error :(";
		logger.info(msg);
		
		return initFilePath;
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
				if(checkBoxAutoscroll.isSelected())
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
