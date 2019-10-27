package gui.utils;


import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
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
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import resources.SoundsConfigurator;

public class GuiUtils {
	
	// INItializer logger creation		
	static Logger logger = Logger.getLogger(GuiUtils.class); 
	
	public static final Cursor CURSOR_WAIT = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	public static final Cursor CURSOR_DEFAULT = Cursor.getDefaultCursor();
	public static final Cursor CURSOR_TEXT = new Cursor(Cursor.TEXT_CURSOR);
	public static final Cursor CURSOR_HAND = new Cursor(Cursor.HAND_CURSOR);
	public static final Cursor CURSOR_CROSS = new Cursor(Cursor.CROSSHAIR_CURSOR);
	
	public static final Dimension DEF_CUSTOM_CURSOR_SIZE = new Dimension(8,8);
	
	public static JOptionHelper dialogHelper = new JOptionHelper(null);

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
	
	public static <E, T> void refreshCombo(final JComboBox target, final Collection<E> values) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				target.removeAllItems();
				Object[] array = values.toArray();
				DefaultComboBoxModel model = new DefaultComboBoxModel<>(array);
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
	
	public static void setupTooltips(int delay, int timeout, Font font) {
		// Tooltip General settings
		ToolTipManager.sharedInstance().setInitialDelay(delay);
		ToolTipManager.sharedInstance().setDismissDelay(timeout);
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
		changeFrameStateLaterUnchecked(frame, toFront, finalState);
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
	public static void changeFrameStateLaterUnchecked(JFrame frame, JFrame toFront, int finalState) {
		
		if(frame == null || toFront == null) {return;}
		
		toFront.setLocationRelativeTo(frame);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame.setExtendedState(finalState);
				if(toFront != null) {
					toFront.toFront();
				}
			}
		});
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
