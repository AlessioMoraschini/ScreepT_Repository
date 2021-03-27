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
package screept.gui.utils.filedialog;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;


public class AdWaitDialog extends JDialog {
	private static final long serialVersionUID = 8586470397692817574L;
	
	AtomicBoolean closedOp = new AtomicBoolean(false);
    private JOptionPane optionPane;
    private String msgErr = "An Error occurred :(";
    private String msgFinish = "Image Ready :)";
    private String msgWait = "I'm generating the Image... this could take up to 20-30 seconds for big Images, heavy stuff!";

    /**
     * Creates the reusable dialog.
     */
    @SuppressWarnings("static-access")
	public AdWaitDialog(String title, JFrame aFrame, JProgressBar bar) {
        super(aFrame, true);
        
        optionPane = new JOptionPane();
        setBounds(400, 200, 400, 150);
        setTitle(title);
//        //Handle window closing disable initially.
//        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
//        int i = 0;
        // catch every value if user commit an action

        optionPane.setBackground(Color.LIGHT_GRAY);

//        if(optionPane.showMessageDialog(this, msgWait))
        
        //Make this dialog display it.
        setContentPane(optionPane);
        
        do {
        	optionPane.add(bar);
        	optionPane.showConfirmDialog(aFrame, msgWait);
        }while(!closedOp.get());
    }

    public static void unclosableInfoPane(String msg) {
    	final JOptionPane optionPane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
    	final JDialog dialog = new JDialog();
    	dialog.setTitle(msg);
    	dialog.setModal(true);

    	dialog.setContentPane(optionPane);

    	dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    	dialog.pack();

    	dialog.setVisible(true);
    }
    
    public static void showInfoPaneWithTimer(String msg) {
    	final JOptionPane optionPane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
    	final JDialog dialog = new JDialog();
    	dialog.setTitle(msg);
    	dialog.setModal(true);

    	dialog.setContentPane(optionPane);

    	dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    	dialog.pack();

    	//create timer to dispose of dialog after 5 seconds
    	Timer timer = new Timer(5000, new AbstractAction() {
			private static final long serialVersionUID = -6274357138634122453L;

			@Override
    	    public void actionPerformed(ActionEvent ae) {
    	        dialog.dispose();
    	    }
    	});
    	timer.setRepeats(false);//the timer should only go off once

    	//start timer to close JDialog as dialog modal we must start the timer before its visible
    	timer.start();

    	dialog.setVisible(true);
    }
	public boolean isClosedOp() {
		return closedOp.get();
	}

	public void setClosedOp(boolean closedOp) {
		this.closedOp.set(closedOp);
	}

	public String getMsgErr() {
		return msgErr;
	}

	public void setMsgErr(String msgErr) {
		this.msgErr = msgErr;
	}

	public String getMsgFinish() {
		return msgFinish;
	}

	public void setMsgFinish(String msgFinish) {
		this.msgFinish = msgFinish;
	}

	public String getMsgWait() {
		return msgWait;
	}

	public void setMsgWait(String msgWait) {
		this.msgWait = msgWait;
	}

    
}
