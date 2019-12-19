package util;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

public class JOptionHelperLight {
	
	// MESSAGE TYPES
	public static final int ERR = JOptionPane.ERROR_MESSAGE;
	public static final int WARN = JOptionPane.WARNING_MESSAGE;
	public static final int INFO = JOptionPane.INFORMATION_MESSAGE;
	public static final int QUESTION = JOptionPane.QUESTION_MESSAGE;
	public static final int PLAIN = JOptionPane.PLAIN_MESSAGE;
	
	public static final int OK = JOptionPane.OK_OPTION;
	public static final int NO = JOptionPane.NO_OPTION;
	public static final int CANC = JOptionPane.CANCEL_OPTION;
	
	// QUESTION UTILS
	public static final int YES_OR_NO = JOptionPane.YES_NO_OPTION;
	public static final int YES_OR_NO_CANC = JOptionPane.YES_NO_CANCEL_OPTION;
	
	public static final Object[] defaultOptions = new Object[]{"Yes","No"};
	
	// LAZY ICON INITIALIZERS
	public static ImageIcon ICON_ERR_REF;
	public static ImageIcon ICON_WARN_REF;
	public static ImageIcon ICON_INFO_REF;
	public static ImageIcon ICON_QUESTION_REF;
	
	public Dimension IF_SCROLLABLE_DEF_DIMENSION = new Dimension( 500, 500 );
	
	public static Font DEFAULT_FONT_DIMENSION = new Font("SansSerif", Font.PLAIN, 17);
	
	public boolean enabledAdvices = true;
	
	// Parent component to center and refer to
	Component parentComponent;
	
	public JOptionHelperLight (Component parentComponent) {
		this.parentComponent = (parentComponent != null) ? parentComponent : new JFrame();
	}
	
	public JOptionHelperLight (Component parentComponent, boolean enableAdvices) {
		this(parentComponent);
		this.enabledAdvices = enableAdvices;
	}
	

	/**
	 * Shows a customizable message dialog that remains alwaysOnTop. Returns true if ok is selected, false in other cases
	 * msgType can be error, info, warn, exc...
	 */
	public boolean showInfoYNDialogOnTop(String msg, String title, int msgType) {
		
        final JOptionPane optionPane = new JOptionPane(
        		getLabelStyledText(msg), 
        		msgType,
        		YES_OR_NO, 
        		new ImageIcon(),
                defaultOptions,
                defaultOptions[0]);
        
		JDialog dialog = optionPane.createDialog(title);
		dialog.setAlwaysOnTop(true);
		dialog.setLocationRelativeTo(parentComponent);
		dialog.enableInputMethods(true);
		dialog.toFront();
		
		try {
			dialog.setVisible(true);
			dialog.dispose();
		} catch (Exception e) {
		}
		
		final Object value = optionPane.getValue();
		return (value != null && value.equals(defaultOptions[0]));
	}
	
	/**
	 * Shows an error message
	 * @param msg nullable, but should be not null and not empty to be clear to user
	 * @param title nullable
	 */
	public void error(String msg, String title) {
		if (enabledAdvices) {
			JOptionPane.showMessageDialog(parentComponent, getLabelStyledText(msg), title, ERR, ICON_ERR_REF);
		}
	}
	

	/**
	 * Shows a warning message
	 * @param msg nullable, but should be not null and not empty to be clear to user
	 * @param title nullable
	 */
	public void warn( String msg,  String title) {
		if (enabledAdvices) {
			JOptionPane.showMessageDialog(parentComponent, getLabelStyledText(msg), title, WARN, ICON_WARN_REF);
		}
	}
	

	/**
	 * Shows a INFO message
	 * @param msg nullable, but should be not null and not empty to be clear to user
	 * @param title nullable
	 */
	public void info(String msg, String title) {
		if (enabledAdvices) {
			JOptionPane.showMessageDialog(parentComponent, getLabelStyledText(msg), title, INFO, ICON_INFO_REF);
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
			result = JOptionPane.showConfirmDialog(parentComponent, getLabelStyledText(msg), title, YES_OR_NO, QUESTION, ICON_QUESTION_REF);
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
			result = JOptionPane.showConfirmDialog(parentComponent, getLabelStyledText(msg), title, YES_OR_NO_CANC, QUESTION, ICON_QUESTION_REF);
		}
		
		return result;
	}
	
	public String askForString(String msg, String title) {
		String result = "";
		
		if (enabledAdvices) {
			JOptionPane panel = new JOptionPane();
			panel.setLocation(parentComponent.getLocation());
			panel.setMessageType(INFO);
			result = panel.showInputDialog(parentComponent, getLabelStyledText(msg), "new");
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
			JOptionPane panel = new JOptionPane();
			panel.setInitialSelectionValue("new");
			panel.setLocation(parentComponent.getLocation());
			result = panel.showInputDialog(parentComponent, getLabelStyledText(msg), title, INFO);
		}
		
		return result;
	}
	
	/**
	 * Shows a customizable message dialog that remains alwaysOnTop. Returns true if ok is selected, false in other cases
	 * msgType can be error, info, warn, exc...
	 */
	public String askForStringUnsafeOnTop(String msg, String title, String initialValue, Object options[]) {
		
		final JOptionPane pane = new JOptionPane(
				msg, JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null,
				options, initialValue);
		pane.setWantsInput(true);
		pane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		pane.selectInitialValue();
		final JDialog dialog = pane.createDialog(null, title);
		dialog.setAlwaysOnTop(true);
		dialog.setLocationRelativeTo(parentComponent);
		dialog.toFront();
		dialog.setVisible(true);
		dialog.dispose();
		final Object value = pane.getInputValue();
		return (value == JOptionPane.UNINITIALIZED_VALUE) ? null : (String) value;
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
			JOptionPane panel = new JOptionPane();
			panel.setMessageType(INFO);
			panel.setLocation(parentComponent.getLocation());
			
			result = panel.showInputDialog(parentComponent, getLabelStyledText(msg), defaultVal);
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
	
	private JLabel getLabelStyledText(String msg) {
		JLabel label = new JLabel(msg, SwingConstants.CENTER);
		label.setOpaque(false);
		label.setFont(DEFAULT_FONT_DIMENSION);
		label.setVisible(true);
		label.revalidate();
		label.repaint();
		
		return label;
	}
	
	
}
