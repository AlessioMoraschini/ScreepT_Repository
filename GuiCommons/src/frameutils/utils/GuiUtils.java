package frameutils.utils;


import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import codeformatter.CustomCodeFormatter;
import dialogutils.JOptionHelperExtended;
import media.ImageGeneralWorker;
import media.Sounds;
import om.files.FileNamed;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import resources.SoundsConfigurator;
import variousutils.HandleString;

public class GuiUtils {
	
	// INItializer logger creation		
	static Logger logger = Logger.getLogger(GuiUtils.class); 
	
	public static final Cursor CURSOR_WAIT = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	public static final Cursor CURSOR_DEFAULT = Cursor.getDefaultCursor();
	public static final Cursor CURSOR_TEXT = new Cursor(Cursor.TEXT_CURSOR);
	public static final Cursor CURSOR_HAND = new Cursor(Cursor.HAND_CURSOR);
	public static final Cursor CURSOR_CROSS = new Cursor(Cursor.CROSSHAIR_CURSOR);
	
	public static final Dimension DEF_CUSTOM_CURSOR_SIZE = new Dimension(8,8);
	public static Cursor CURSOR_COLOR_PICK;
	
	public static JOptionHelperExtended dialogHelper = new JOptionHelperExtended(null);
	public static CustomCodeFormatter codeFormatter;
	
	public static void init() {
		codeFormatter = new CustomCodeFormatter(GeneralConfig.JS_LIB_FOLDER);
		CURSOR_COLOR_PICK = getCustomCursor(IconsPathConfigurator.ICON_COLOR_SAMPLER, DEF_CUSTOM_CURSOR_SIZE);
	}
	
	/**
	 * Use this to disable or enable default system shortcuts for a given key, identified by its keyCode
	 * @param keyCode
	 */
	public static void setKeySystemActionEnabled(int keyCode, boolean enable) {
		
		KeyEventDispatcher customKeyDispatcher = getKeyDispatcherFromCode(keyCode);
		
		if(enable) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(customKeyDispatcher);
		}else {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(customKeyDispatcher);
		}
		
	}
	
	public static KeyEventDispatcher getKeyDispatcherFromCode(int keyCode) {
		return new KeyEventDispatcher() {
		    @Override
		    public boolean dispatchKeyEvent(KeyEvent e) {
		        return e.getKeyCode() == keyCode;
		    }
		};
    }
	
	public static boolean isKeyCodeValidCode(int keyCodeToCheck) {
		String unknown = Toolkit.getProperty("AWT.unknown", "Unknown");
        return !KeyEvent.getKeyText(keyCodeToCheck).equals(unknown + " keyCode: 0x" + Integer.toString(keyCodeToCheck, 16));
	}
	
	public static int getFirstIndexMatchFromStringCombo(JComboBox<String> comboBox, String match) {
		if(comboBox == null || match == null) {
			return -1;
		}
		for(int i = 0; i < comboBox.getItemCount(); i++) {
			if(comboBox.getItemAt(i).equals(match)) {
				return i;
			}
		}
		
		return -1;
	}

	public static int getFirstIndexMatchFromFileNamedCombo(JComboBox<FileNamed> comboBox, String match) {
		if(comboBox == null || match == null) {
			return -1;
		}
		for(int i = 0; i < comboBox.getItemCount(); i++) {
			if(comboBox.getItemAt(i).getFileName().equals(match)) {
				return i;
			}
		}
		
		return -1;
	}
	
	public static Frame getTopFrame() {
		Frame[] frames = Frame.getFrames();
		for (int i = 0; i < frames.length; i++) {
			if (frames[i].getFocusOwner() != null) {
				return frames[i];
			}
		}
		if (frames.length > 0) {
			return frames[0];
		}
		return null;
	}

	public static Cursor getCustomCursor(String imgPath, Dimension size) {
		Dimension correctSize = size;
		if(correctSize == null) {
			correctSize = DEF_CUSTOM_CURSOR_SIZE;
		}
		
		Cursor result = CURSOR_DEFAULT;
		try {
			Point hotSpot = new Point(0, 0);
	        BufferedImage cursorImg = ImageIO.read(new File(IconsPathConfigurator.ICON_COLOR_SAMPLER));
	        cursorImg = ImageGeneralWorker.scaleImage(cursorImg, BufferedImage.TYPE_4BYTE_ABGR_PRE, correctSize.width, correctSize.height);
			result = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, hotSpot, imgPath);
		} catch (Exception e) {
			logger.error(e);
		}
		
		return result;
	}
	
	/**
	 * Retrieve selected text from JTextaea if something is selected, else retrieve all text
	 * @param source
	 * @return
	 */
	public static String getSelectedOrAllText(JTextArea source) {
		String selection = source.getSelectedText();
		String allText = source.getText();
		String result = (selection == null)? allText : selection;
		
		return result;
	}
	
	public static void formatTextLater(JTextArea sourceArea, String extension, int tabSize) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				sourceArea.setCursor(GuiUtils.CURSOR_WAIT);
				formatText(sourceArea, extension, tabSize);
				sourceArea.setCursor(GuiUtils.CURSOR_TEXT);
			}
		});
	}
	
	public static void formatText(JTextArea sourceArea, String extension, int tabSize) {
		Dimension originalDialogDim = dialogHelper.IF_SCROLLABLE_DEF_DIMENSION;
		dialogHelper.setIF_SCROLLABLE_DEF_DIMENSION(new Dimension(500,230));
		boolean error = false;
		int selStart = sourceArea.getSelectionStart(); 
		int selEnd = sourceArea.getSelectionEnd();
		String source = GuiUtils.getSelectedOrAllText(sourceArea);
		String old = new String(source);
		String formatted = "";
		boolean suppported = false;

		try {
			// XML format handler (if xml formatter works then is a xml recognized file)
			if (extension.toLowerCase().endsWith("xml")) {
				suppported = true;
				formatted = CustomCodeFormatter.xmlPrettyFormat(source, tabSize);
				source = CustomCodeFormatter.xmlPrettyFormat(source, tabSize);
				if (source.equals(old)) {
					// already formatted
					source = HandleString.removeLines(source);
				}
			// JS/JSON -> use javascript function call with custom library to prettify JS string
			}else if (extension.toLowerCase().endsWith("js") || extension.toLowerCase().endsWith("json")) {
				suppported = true;
				formatted = codeFormatter.jsFormatJSLIB(source, 5);
				formatted = replaceEachLineMatchesFromLeft(true, formatted, "    ", "\t");
				source = formatted;
				
			// HTML -> use javascript function call with custom library to prettify html string
			}else if (extension.toLowerCase().endsWith("html")) {
				suppported = true;
				formatted = codeFormatter.htmlFormatJSLIB(source, 5);
				formatted = replaceEachLineMatchesFromLeft(true, formatted, "    ", "\t");
				source = formatted;
			
			// CSS -> use javascript function call with custom library to prettify css string
			}else if (extension.toLowerCase().endsWith("css")) {
				suppported = true;
				formatted = codeFormatter.cssFormatJSLIB(source, 5);
				formatted = replaceEachLineMatchesFromLeft(true, formatted, "    ", "\t");
				source = formatted;
				
			// JAVA -> use google library to prettify java string
			} else if (extension.toLowerCase().endsWith("java")) {
			suppported = false;
//			formatted = CustomCodeFormatter.googleJavaCodeFormat(source);
//			source = formatted;
		}
		} catch (Exception e) {
			manageFormatterException(e, extension);
			error = true;
		}

		// apply formatted to textArea
		if (!error && !source.equals(old)) {
			if (selStart == selEnd) {
				sourceArea.setText(source);
			}else {
				sourceArea.replaceSelection(source);
			}
		}else {
			String unsupportedAdvice = "";
			if(!suppported) {
				unsupportedAdvice = extension + " is not yet supported... maybe coming soon? ;)";
			}
			
			if (!suppported || error) {
				Sounds.playSound(SoundsConfigurator.ERROR, null);
				logger.warn("Format not supported or an error occurred :-> skipping text replace in text area. " + unsupportedAdvice);
				dialogHelper.warn(encapsulateInHtml("<b>Cannot format with current settings</b></br>" + unsupportedAdvice), "Cannot Format text");
			}
		}
		
		dialogHelper.setIF_SCROLLABLE_DEF_DIMENSION(originalDialogDim);
	}
	
	public static String encapsulateInHtml(String original) {
		return "<html>" + original + "</html>";
	}
	
	public static String replaceEachLineMatchesFromLeft(boolean startsWith, String source, String match, String replacement) {
		String[] lines = source.split("\n");
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (String line : lines) {
			if(i > 0 && i< lines.length) {
				builder.append("\n");
			}
			if (!startsWith || line.startsWith(match)) {
				builder.append(line.replaceAll(match, replacement));			}else {
				builder.append(line);
			}
			i++;
		}
		
		return builder.toString();
	}
	
	private static void manageFormatterException(Exception e, String extension) {
		Sounds.playSound(SoundsConfigurator.ERROR, null);
		String exception = Arrays.deepToString(e.getStackTrace());
		logger.error("error formatting "+ extension +" string !", e);
		dialogHelper.errorScroll("Error reading " + extension + " format: " + exception, "format Error");
	}
	
	public static void launchThreadSafeSwing(Runnable threadAction) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try {
					new Thread(threadAction).start();
				} catch (Exception e) {
					logger.error("GuiUtils.launchThreadSafeSwing(threadAction) => Error executing threadAction! ", e);
				}
			}
		});
	}
	
	public static void labelUpdateWithTimeout(String message, JTextComponent target, int timeout) {
		
		if(message == null || target == null) {
			return;
		}
		
		launchThreadSafeSwing(new Runnable() {
			@Override
			public void run() {
				try {
					target.setText(message);
					Thread.sleep(timeout);
					target.setText("");
				} catch (InterruptedException e) {
					logger.error("Exception happened!", e);
				}
			}
		});
	}
	
	public static <E> void refreshCombo(final JComboBox<E> target, final Collection<E> values) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				target.removeAllItems();
				Object[] array = values.toArray();
				DefaultComboBoxModel<E> model = new DefaultComboBoxModel(array);
				target.setModel(model);
				target.revalidate();
				target.repaint();
			}
		});
	}
	
	public static Window getCurrentActiveWindow() {
		Window activeWindow = javax.swing.FocusManager.getCurrentManager().getActiveWindow();
		return activeWindow;
	}
	
	/**
	 * Calculates current GUI width / screen width ratio (1 => full screen).
	 * 
	 * Returns 0 in case of error, else the ratio guiWidth/screenWidth
	 */
	public static float getGuiToScreenWidthRatio(Window mainGui) {
		if (mainGui == null) { return 0;};
		
		int width = (mainGui.getWidth() != 0)? mainGui.getWidth() : 1;
		
		return (float)(width/getScreenSize().width);
	}

	/**
	 * Calculates current GUI heigth / screen height ratio (1 => full screen).
	 * 
	 * Returns 0 in case of error, else the ratio guiHeigth/screenHeigth
	 */
	public static float getGuiToScreenHeitghtRatio(JComponent mainGui) {
		if (mainGui == null) { return 0;};
		
		int height = (mainGui.getHeight() != 0)? mainGui.getHeight() : 1;
		
		return (float)(height/getScreenSize().height);
	}
	
	/**
	 * Refresh caret selection in textArea 
	 */
	public static void syncCaretPosition(JTextArea target, int start, Integer end) {
		try {
			
			if (end != start) {
				target.setSelectionStart(start);
				target.setSelectionEnd(end);
			}else {
				target.setCaretPosition(0);
				target.setCaretPosition(start);
			}
			
		} catch (Exception e) {
			logger.warn("ERROR setting caret position into textArea miniMap");
		}
	}
	
	public static void setupTooltips(int delay, int timeout, int reshowDelay, Font font) {
		// Tooltip General settings
		ToolTipManager.sharedInstance().setInitialDelay(delay);
		ToolTipManager.sharedInstance().setDismissDelay(timeout);
		ToolTipManager.sharedInstance().setReshowDelay(reshowDelay);
		UIManager.put("ToolTip.font", font);
	}
	
	public static void scrollJScrollPane(JScrollPane scrollPane, boolean bottom) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				JScrollBar vertical = scrollPane.getVerticalScrollBar();
				int value = (bottom)? vertical.getMaximum() : vertical.getMinimum();
				vertical.setValue(value);
			}
		});
	}
	
	public static void makeTransparentJButton(JButton targetButton) {
		targetButton.setOpaque(false);
		targetButton.setContentAreaFilled(false);
		targetButton.setBorderPainted(false);
	}
	
	public static void makeOpaqueJButton(JButton targetButton) {
		targetButton.setOpaque(true);
		targetButton.setContentAreaFilled(true);
		targetButton.setBorderPainted(true);
	}
	
	public static void makeTransparentJToggleButton(JToggleButton targetButton) {
		targetButton.setOpaque(false);
		targetButton.setContentAreaFilled(false);
		targetButton.setBorderPainted(false);
	}
	
	public static void makeOpaqueJToggleButton(JToggleButton targetButton) {
		targetButton.setOpaque(true);
		targetButton.setContentAreaFilled(true);
		targetButton.setBorderPainted(true);
	}
	
	/**
	 * Use JFrame states as state to modify a frame (icon, maximized, etc,...) with invokeLater built in.
	 * Prevent maximize if before was not maxified
	 * 
	 * <b>if toFront is not null it will be take to front last</b>
	 */
	public static void changeFrameStateLater(JFrame frame, JFrame toFront, int finalState) {
		if((frame.getExtendedState() != JFrame.MAXIMIZED_BOTH) && finalState == JFrame.MAXIMIZED_BOTH ) {
			return; // if was not max then skip
		}
		extendFramesAndMoveToTop(frame, toFront, finalState);
	}
	
	/**
	 * Use JFrame states as state to modify a frame (icon, maximized, etc,...) with invokeLater built in.
	 * Prevent maximize if before was not maxified
	 * 
	 * <b>if toFront is not null it will be take to front last</b>
	 */
	public static void changeFrameStateLater(JFrame frame, JDialog toFront, int finalState) {
		if((frame.getExtendedState() != JFrame.MAXIMIZED_BOTH) && finalState == JFrame.MAXIMIZED_BOTH ) {
			return; // if was not max then skip
		}
		changeFrameStateLaterUnchecked(frame, toFront, finalState);
	}
	
	/**
	 * Use JFrame states as state to modify a frame (icon, maximized, etc,...) with invokeLater built in.
	 * Prevent maximize if before was not maxified
	 * 
	 * <b>if toFront is not null it will be take to front last</b>
	 */
	public static void extendFramesAndMoveToTop(JFrame frame, JFrame toFront, int finalState) {
		
		if(frame == null || toFront == null) {return;}
		
		toFront.setLocationRelativeTo(frame);
		frame.setExtendedState(finalState);
		toFront.setExtendedState(finalState);
		toFront.toFront();
	}
	
	/**
	 * Use JFrame states as state to modify a frame (icon, maximized, etc,...) with invokeLater built in.
	 * Prevent maximize if before was not maxified
	 * 
	 * <b>if toFront is not null it will be take to front last</b>
	 */
	public static void changeFrameStateLaterUnchecked(JFrame frame, JDialog toFront, int finalState) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame.setExtendedState(finalState);
				if(toFront != null) {
					toFront.toFront();
				}
			}
		});
	}
	
	public static boolean removeKeyListeners(Component target) {
		try {
			for (KeyListener key : target.getKeyListeners()) {
				target.removeKeyListener(key);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean setFrameIcon(Image icon, JFrame frameToSet) {
		if(icon != null) {
			frameToSet.setIconImage(icon);
			return true;
		}else {
			return false;
		}
	}
	
	public static boolean setFrameIcon(ImageIcon icon, JFrame frameToSet) {
		if(icon != null) {
			frameToSet.setIconImage(icon.getImage());
			return true;
		}else {
			return false;
		}
	}
	
	public static void setFrameIcon(String icon, JFrame frameToSet) {
		ImageIcon imgIcon = new ImageIcon(icon);
		frameToSet.setIconImage(imgIcon.getImage());
	}
	
	public static void setFrameIcon(String icon, Dialog frameToSet) {
		ImageIcon imgIcon = new ImageIcon(icon);
		frameToSet.setIconImage(imgIcon.getImage());
	}
	
	/**
	 * this method makes the current frame that's invoking this method to move on same monitor as the given frame
	 * @param frame his monitor will be the target for the current window
	 * @return the found graphicsDevice obj of target frame
	 */
	public static GraphicsDevice showOnSameScreen(JFrame son, Window frame) {
		GraphicsDevice result = frame.getGraphicsConfiguration().getDevice();
		son.setLocationRelativeTo(frame);
//		if (frame != null) {
//			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//			GraphicsDevice[] gd = ge.getScreenDevices();
//			final GraphicsDevice frameDevice = frame.getGraphicsConfiguration().getDevice();
//			for (GraphicsDevice graphicsDevice : gd) {
//				if (frameDevice.equals(graphicsDevice)
//						|| frameDevice.getIDstring().equals(graphicsDevice.getIDstring())) {
//					final Rectangle monitorBounds = graphicsDevice.getDefaultConfiguration().getBounds();
//					final int x = monitorBounds.x + (monitorBounds.width - frame.getWidth()) / 2;
//					final int y = monitorBounds.y + (monitorBounds.height - frame.getHeight()) / 2;
//					frame.setLocation(x, y);
//					result = graphicsDevice;
//					break;
//				}
//			} 
//		}
		return result;
	}
	
	public static Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}
	
	public static void maximizeFrameSize(JFrame targetFrame) {
	    Dimension screenSize = getScreenSize();
	    targetFrame.setSize(screenSize.width, screenSize.height);
	}
	
	public static Dimension makeFullScreen(Component element) {
//		Dimension screen = getScreenSize(element);
		Dimension returnDim = new Dimension(element.getWidth(), element.getHeight());
		if(element instanceof JFrame) {
			((JFrame) element).setUndecorated(true);
		}
//		element.setMinimumSize(screen);
		return returnDim;
	}
	
	public static Dimension getScreenSize(Component frame) {
		DisplayMode mode = frame.getGraphicsConfiguration().getDevice().getDisplayMode();
		return new Dimension(mode.getWidth(), mode.getHeight());
	}
	
	public static Dimension getScreenSizePerc(int factorPerc, JFrame frame) {
		Dimension screen = getScreenSize(frame);
		int w = (screen.width*factorPerc)/100;
		int h = (screen.height*factorPerc)/100;
		return new Dimension(w, h);
	}
	
	public static Dimension getScreenSize(int marginLeftRight) {
		Dimension screen = getScreenSize();
		return new Dimension(screen.width-(marginLeftRight+marginLeftRight), screen.height);
	}
	
	public static void centerComponent(Component targetComponent, int percentSize) {
		float percent = (float)percentSize/100;
		Dimension screenSize = (targetComponent instanceof JFrame)? getScreenSize((JFrame)targetComponent): getScreenSize();
		int W = (int)(screenSize.width * percent);
		int H = (int)(screenSize.height * percent);
		targetComponent.setSize(W, H);
		targetComponent.setBounds(screenSize.width/2-W/2, screenSize.height/2-H/2, W, H);
	}
	
	public static void centerComponent(Component targetFrame, int W, int H) {
		Dimension screenSize = (targetFrame instanceof JFrame)? getScreenSize((JFrame)targetFrame): getScreenSize();
		targetFrame.setSize(W, H);
		targetFrame.setBounds(screenSize.width/2-W/2, screenSize.height/2-H/2, W, H);
	}

	public static void centerComponent(Component targetFrame, Dimension dimension) {
		centerComponent(targetFrame, dimension.width, dimension.height);
	}
	
	public static void centerComponent(Component targetFrame) {
		if(targetFrame == null) {
			return;
		}
		Dimension screenSize = (targetFrame instanceof JFrame)? getScreenSize((JFrame)targetFrame): getScreenSize();
		Dimension frameSize = null;
		frameSize = targetFrame.getSize();
		int W = frameSize.width;
		int H = frameSize.height;
		
		targetFrame.setBounds(screenSize.width/2-W/2, screenSize.height/2-H/2, W, H);
	}
	
	public static void centerFrame(JFrame targetFrame, int percentSize) {
		float percent = (float)percentSize/100;
		Dimension screenSize = getScreenSize(targetFrame);
		int W = (int)(screenSize.width * percent);
		int H = (int)(screenSize.height * percent);
		targetFrame.setSize(W, H);
		targetFrame.setBounds(screenSize.width/2-W/2, screenSize.height/2-H/2, W, H);
	}
	
	public static void centerFrame(JFrame targetFrame, int W, int H) {
		Dimension screenSize = getScreenSize(targetFrame);
		targetFrame.setSize(W, H);
		targetFrame.setBounds(screenSize.width/2-W/2, screenSize.height/2-H/2, W, H);
	}
	
	public static void moveFrame(Component element, int xDiff, int yDiff) {
		int totX = (element.getX() + xDiff);
		int totY = (element.getY() + yDiff);
		int newX = (totX < 0)? 0 : totX;
		int newY = (totY < 0)? 0 : totY;
		Dimension screenSize = (element instanceof JFrame)? getScreenSize((JFrame)element): getScreenSize();
		int maxDeltaX = screenSize.width - newX - element.getWidth();
		int maxDeltaY = screenSize.height - newY - element.getHeight();
		
		newX = (maxDeltaX < 0)? screenSize.width - element.getWidth() : newX;
		newY = (maxDeltaY < 0)? screenSize.height - element.getHeight() : newY;
		
		element.setBounds(newX, newY, element.getWidth(), element.getHeight());
	}
	
	public static void openInFileSystem(File destination) {
		if (destination != null && destination.exists()) {
			try {
				if (destination.isDirectory()) {
					Desktop.getDesktop().open(destination);
				}else {
					Desktop.getDesktop().open(destination.getParentFile());
				}
				logger.debug(destination.getAbsolutePath()+" Opened in system explorer");

			} catch (IOException e1) {
				logger.error("Error opening folder in system explorer !", e1);
			} 
		}	
	}
	
	public static void setCursor(JFrame frame, Cursor cursor) {
        if (frame != null) {
            RootPaneContainer root = (RootPaneContainer) frame.getRootPane().getTopLevelAncestor();
            root.getGlassPane().setCursor(cursor);
            root.getGlassPane().setVisible(true);
        }
    }
	
	public static void setCursor( JRootPane frame, Cursor cursor) {
        if (frame != null) {
            RootPaneContainer root = (RootPaneContainer) frame.getTopLevelAncestor();
            root.getGlassPane().setCursor(cursor);
            root.getGlassPane().setVisible(true);
        }
    }
	
	public static void setWaitCursor(JFrame frame) {
        if (frame != null) {
            RootPaneContainer root = (RootPaneContainer) frame.getRootPane().getTopLevelAncestor();
            root.getGlassPane().setCursor(CURSOR_WAIT);
            root.getGlassPane().setVisible(true);
        }
    }
	
	public static void setWaitCursor(JRootPane frame) {
		if (frame != null) {
			RootPaneContainer root = (RootPaneContainer) frame.getTopLevelAncestor();
			root.getGlassPane().setCursor(CURSOR_WAIT);
			root.getGlassPane().setVisible(true);
		}
	}

    public static void setDefaultCursor(JFrame frame) {
        if (frame != null) {
            RootPaneContainer root = (RootPaneContainer) frame.getRootPane().getTopLevelAncestor();
            root.getGlassPane().setCursor(CURSOR_DEFAULT);
        }
    }
    
    public static void setDefaultCursor(JRootPane frame) {
        if (frame != null) {
            RootPaneContainer root = (RootPaneContainer) frame.getTopLevelAncestor();
            root.getGlassPane().setCursor(CURSOR_DEFAULT);
        }
    }
    
    public static void selectAllText(JTextComponent field) {
    	field.requestFocus();
    	field.setSelectionStart(0);
    	field.setSelectionEnd(field.getText().length());
    }
    
	public static String getTxtAreaCurrentLine(JTextArea txtArea) throws BadLocationException {
		
		int offsetFromStart = txtArea.getCaretPosition();
		int currLine = txtArea.getLineOfOffset(offsetFromStart);
		int startOffset = txtArea.getLineStartOffset(currLine);
		int endOffset = txtArea.getLineEndOffset(currLine);
		
		int length = endOffset - startOffset;
		
		return txtArea.getText(startOffset, length);
	}
	
	public static void setJspinnerColors(JSpinner spinner, Color foreground, Color background) {
		JComponent editor = spinner.getEditor();
		int n = editor.getComponentCount();
		for (int i = 0; i < n; i++) {
			Component c = editor.getComponent(i);
			if (c instanceof JTextField) {
				c.setForeground(foreground);
				c.setBackground(background);
			}
		}
	}
	
	/**
	 * Create a copy of the current line denoted by current caret position, and insert it before or after the current line
	 * 
	 * If more lines are selected, then all of them will be duplicated
	 * 
	 * @param textArea
	 * @param upDirection
	 * @return
	 * @throws BadLocationException
	 */
	public static boolean duplicateTextAreaCurrentLine(JTextArea textArea, boolean upDirection) throws BadLocationException {
		
		int offsetFromStart = (textArea.getSelectionStart() <= textArea.getSelectionEnd())? textArea.getSelectionStart() : textArea.getSelectionEnd();
		int selectionEnd = (textArea.getSelectionStart() <= textArea.getSelectionEnd())? textArea.getSelectionEnd() : textArea.getSelectionStart();
		
		int currLine = textArea.getLineOfOffset(offsetFromStart);
		int startOffset = textArea.getLineStartOffset(currLine);
		int endOffset = textArea.getLineEndOffset(currLine);

		int selectionEndLine = textArea.getLineOfOffset(selectionEnd);
		int selectionEndLStartOffs = textArea.getLineStartOffset(selectionEndLine);
		int selectionEndLEndOffs = textArea.getLineEndOffset(selectionEndLine);
		
		int lineLength = endOffset - startOffset;
		
		// IF MORE LINES SELECTED COPY ALL SELECTED TEXT
		if((selectionEnd - offsetFromStart) > lineLength) {
			String insertText = textArea.getSelectedText();
			
			if(!upDirection) {
				// in this case insert at the end
				return insertLineIntoTextArea(selectionEndLEndOffs, insertText, textArea);
			}else {
				// in this case insert at start
				return insertLineIntoTextArea(selectionEndLStartOffs, insertText, textArea);
			}
			
		}else {
			// ELSE
			String insertText = textArea.getText(startOffset, lineLength);
			if(!upDirection) {
				// in this case insert at the end
				return insertLineIntoTextArea(endOffset, insertText, textArea);
			}else {
				// in this case insert at start
				return insertLineIntoTextArea(startOffset, insertText, textArea);
			}
		}
	}
	
	/**
	 * Insert a the given string at the given caret position offset.
	 * 
	 * @param caretPosStart the caret start position
	 * @param newLine the string to insert
	 * @param textArea the target textarea to insert into
	 * 
	 * @return true if line has been inserted, false if not
	 */
	public static boolean insertLineIntoTextArea(int caretPosStart, String newLine, JTextArea textArea) {
		try {
			textArea.getDocument().insertString(caretPosStart, newLine, null);
			return true;
		} catch (Exception e) {
			logger.error("Cannot insert line at caret n°: " + caretPosStart + "TextArea length is: " + textArea.getText().length());
			return false;
		}
	}
	
}
