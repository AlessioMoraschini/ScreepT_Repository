package gui.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.lang3.StringUtils;

import resources.IconsPathConfigurator;

public class JOptionHelper {
	
	// MESSAGE TYPES
	public static final int ERR = JOptionPane.ERROR_MESSAGE;
	public static final int WARN = JOptionPane.WARNING_MESSAGE;
	public static final int INFO = JOptionPane.INFORMATION_MESSAGE;
	public static final int QUESTION = JOptionPane.QUESTION_MESSAGE;
	public static final int PLAIN = JOptionPane.PLAIN_MESSAGE;
	
	// QUESTION UTILS
	public static final int YES_OR_NO = JOptionPane.YES_NO_OPTION;
	public static final int YES_OR_NO_CANC = JOptionPane.YES_NO_CANCEL_OPTION;
	
	// LAZY ICON INITIALIZERS
	public static final ImageIcon ICON_ERR_REF = new ImageIcon(IconsPathConfigurator.ICON_BIG_GEN_ERROR);
	public static final ImageIcon ICON_WARN_REF = new ImageIcon(IconsPathConfigurator.ICON_BIG_GEN_WARNING);
	public static final ImageIcon ICON_INFO_REF = new ImageIcon(IconsPathConfigurator.ICON_GEN_INFO);
	public static final ImageIcon ICON_QUESTION_REF = new ImageIcon(IconsPathConfigurator.ICON_GEN_QUESTION);
	
	public Dimension IF_SCROLLABLE_DEF_DIMENSION = new Dimension( 500, 500 );
	
	public boolean enabledAdvices = true;
	
	// Parent component to center and refer to
	Component parentComponent;
	
	public JOptionHelper (Component parentComponent) {
		this.parentComponent = parentComponent;
	}
	
	public JOptionHelper (Component parentComponent, boolean enableAdvices) {
		this.parentComponent = parentComponent;
		this.enabledAdvices = enableAdvices;
	}
	
	private JScrollPane getScrollableDialogPanel(String preloadedMsg) {
		preloadedMsg = (preloadedMsg == null)? "" : preloadedMsg;
		
		JTextArea textArea = new JTextArea("Insert your Text here");
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(textArea);
		scrollPane.setVisible(true);
		textArea.setLineWrap(true);  
		textArea.setWrapStyleWord(true); 
		textArea.setMargin(new Insets(6,6,6,6));
		scrollPane.setPreferredSize(IF_SCROLLABLE_DEF_DIMENSION);
		
		textArea.setText(preloadedMsg);
		GuiUtils.scrollJScrollPane(scrollPane, false);
		textArea.requestFocus();
		textArea.setCaretPosition(0);
		
		return scrollPane;
	}

	/**
	 * Shows an error message
	 * @param msg nullable, but should be not null and not empty to be clear to user
	 * @param title nullable
	 */
	public void error(String msg, String title) {
		if (enabledAdvices) {
			title = (!StringUtils.isEmpty(title)) ? title : "Error!!";
			msg = (!StringUtils.isEmpty(msg)) ? msg : "Error message not specified";
			JOptionPane.showMessageDialog(parentComponent, msg, title, ERR, ICON_ERR_REF);
		}
	}
	
	/**
	 * Shows an error message within a scrollable panel, useful to show long messages
	 * 
	 * @param msg nullable, but should be not null and not empty to be clear to user
	 * @param title nullable
	 */
	public boolean errorScroll(String msg, String title) {
		if (enabledAdvices) {
			msg = (!StringUtils.isEmpty(msg)) ? msg : "Error message not specified";
			JScrollPane scroller = getScrollableDialogPanel(msg);
			int resp = JOptionPane.showConfirmDialog(parentComponent, scroller, title, ERR);
			if(resp == JOptionPane.OK_OPTION) return true;
		}
		return false;
	}

	/**
	 * Shows an error message within a scrollable panel, useful to show long messages
	 * This version pretty prints an exception/error (throwable)
	 * 
	 * @param msg nullable, but should be not null and not empty to be clear to user
	 * @param title nullable
	 */
	public void errorScroll(String msg, String title, Throwable e) {
		if (enabledAdvices) {
			msg = (!StringUtils.isEmpty(msg)) ? msg : "Error message not specified";
			String exception = Arrays.deepToString(e.getStackTrace());
			String newMsg = msg+"\n\n"+exception;
			JScrollPane scroller = getScrollableDialogPanel(newMsg);
			JOptionPane.showMessageDialog(parentComponent, scroller, title, ERR);
		}
	}

	/**
	 * Shows a warning message
	 * @param msg nullable, but should be not null and not empty to be clear to user
	 * @param title nullable
	 */
	public void warn(String msg, String title) {
		if (enabledAdvices) {
			title = (!StringUtils.isEmpty(title)) ? title : "Warning!";
			msg = (!StringUtils.isEmpty(msg)) ? msg : "Warning message not specified";
			JOptionPane.showMessageDialog(parentComponent, msg, title, WARN, ICON_WARN_REF);
		}
	}
	
	/**
	 * Shows a warning message within a scrollable panel, useful to show long messages
	 * 
	 * @param msg nullable, but should be not null and not empty to be clear to user
	 * @param title nullable
	 */
	public void warnScroll(String msg, String title) {
		if (enabledAdvices) {
			msg = (!StringUtils.isEmpty(msg)) ? msg : "Warning message not specified";
			JScrollPane scroller = getScrollableDialogPanel(msg);
			JOptionPane.showMessageDialog(parentComponent, scroller, title, WARN);
		}
	}

	/**
	 * Shows a INFO message
	 * @param msg nullable, but should be not null and not empty to be clear to user
	 * @param title nullable
	 */
	public void info(String msg, String title) {
		if (enabledAdvices) {
			title = (!StringUtils.isEmpty(title)) ? title : "Info";
			msg = (!StringUtils.isEmpty(msg)) ? msg : "Information message not specified";
			JOptionPane.showMessageDialog(parentComponent, msg, title, INFO, ICON_INFO_REF);
		}
	}
	
	/**
	 * Shows a information message within a scrollable panel, useful to show long messages
	 * 
	 * @param msg nullable, but should be not null and not empty to be clear to user
	 * @param title nullable
	 */
	public void infoScroll(String msg, String title) {
		if (enabledAdvices) {
			msg = (!StringUtils.isEmpty(msg)) ? msg : "Information message not specified";
			JScrollPane scroller = getScrollableDialogPanel(msg);
			JOptionPane.showMessageDialog(parentComponent, scroller, title, INFO);
		}
	}
	
	/**
	 * Shows a Question message
	 * @param msg nullable, but should be not null and not empty to be clear to user
	 * @param title nullable
	 */
	public void question(String msg, String title) {
		if (enabledAdvices) {
			title = (!StringUtils.isEmpty(title)) ? title : "Info";
			msg = (!StringUtils.isEmpty(msg)) ? msg : "Information message not specified";
			JOptionPane.showMessageDialog(parentComponent, msg, title, QUESTION, ICON_QUESTION_REF);
		}
	}
	
	/**
	 * Shows a plain message
	 * @param msg nullable, but should be not null and not empty to be clear to user
	 * @param title nullable
	 */
	public void plainMsg(String msg, String title) {
		if (enabledAdvices) {
			title = (!StringUtils.isEmpty(title)) ? title : "Plain";
			msg = (!StringUtils.isEmpty(msg)) ? msg : "Information message not specified";
			JOptionPane.showMessageDialog(parentComponent, msg, title, PLAIN);
		}
	}

	/**
	 * Shows a question and return boolean choice
	 * @param msg nullable, but should be not null and not empty to be clear to user
	 * @param title nullable
	 */
	public boolean yesOrNo(String msg, String title) {
		int result = JOptionPane.NO_OPTION;
		if (enabledAdvices) {
			title = (!StringUtils.isEmpty(title)) ? title : "Plain";
			msg = (!StringUtils.isEmpty(msg)) ? msg : "Information message not specified";
			result = JOptionPane.showConfirmDialog(parentComponent, msg, title, YES_OR_NO, QUESTION, ICON_QUESTION_REF);
		}
		
		return result == JOptionPane.OK_OPTION;
	}

	/**
	 * Shows a question and return int choice
	 * @param msg nullable, but should be not null and not empty to be clear to user
	 * @param title nullable
	 */
	public int yesNoOrCanc(String msg, String title) {
		int result = JOptionPane.NO_OPTION;
		if (enabledAdvices) {
			title = (!StringUtils.isEmpty(title)) ? title : "Plain";
			msg = (!StringUtils.isEmpty(msg)) ? msg : "Information message not specified";
			result = JOptionPane.showConfirmDialog(parentComponent, msg, title, YES_OR_NO_CANC, QUESTION, ICON_QUESTION_REF);
		}
		
		return result;
	}
	
	public String askForString(String msg, String title) {
		String result = "";
		
		if (enabledAdvices) {
			title = (!StringUtils.isEmpty(title)) ? title : "Plain";
			msg = (!StringUtils.isEmpty(msg)) ? msg : "Information message not specified";
			JOptionPane panel = new JOptionPane();
			panel.setMessageType(INFO);
			
			result = panel.showInputDialog(parentComponent, msg, "new");
			result = (result == null)? "" : result;
		}
		
		return result;
	}
	

	/**
	 * Returns null if cancel is pressed
	 * @param msg
	 * @param title
	 * @return
	 */
	public String askForStringUnsafe(String msg, String title) {
		String result = "";
		
		if (enabledAdvices) {
			title = (!StringUtils.isEmpty(title)) ? title : "Plain";
			msg = (!StringUtils.isEmpty(msg)) ? msg : "Information message not specified";
			JOptionPane panel = new JOptionPane();
			panel.setInitialSelectionValue("new");
			result = panel.showInputDialog(parentComponent, msg, title, INFO);
		}
		
		return result;
	}

	/**
	 * Returns null if cancel is pressed
	 * @param msg
	 * @param title
	 * @return
	 */
	public String askForStringUnsafe(String msg, String title, String defaultVal) {
		String result = "";
		
		if (enabledAdvices) {
			title = (!StringUtils.isEmpty(title)) ? title : "Plain";
			msg = (!StringUtils.isEmpty(msg)) ? msg : "Information message not specified";
			JOptionPane panel = new JOptionPane();
			panel.setMessageType(INFO);
			
			result = panel.showInputDialog(parentComponent, msg, defaultVal);
		}
		
		return result;
	}
	
	public void setAdvicesEnabled(boolean enabled) {
		enabledAdvices = enabled;
	}
	public Dimension getIF_SCROLLABLE_DEF_DIMENSION() {
		return IF_SCROLLABLE_DEF_DIMENSION;
	}

	public void setIF_SCROLLABLE_DEF_DIMENSION(Dimension iF_SCROLLABLE_DEF_DIMENSION) {
		IF_SCROLLABLE_DEF_DIMENSION = iF_SCROLLABLE_DEF_DIMENSION;
	}
	
	
}
