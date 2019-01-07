package gUI.utils.filedialog;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import java.awt.Color;
import java.util.concurrent.atomic.AtomicBoolean;


public class AdWaitDialog extends JDialog {

	AtomicBoolean closedOp = new AtomicBoolean(false);
    private JOptionPane optionPane;
    private String msgErr = "An Error occurred :(";
    private String msgFinish = "Image Ready :)";
    private String msgWait = "I'm generating the Image... this could take up to 20-30 seconds for big Images, heavy stuff!";

    /**
     * Creates the reusable dialog.
     */
    public AdWaitDialog(String title, JFrame aFrame, JProgressBar bar) {
        super(aFrame, true);
        
        optionPane = new JOptionPane();
        setBounds(400, 200, 400, 150);
        setTitle(title);
//        //Handle window closing disable initially.
//        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        int i = 0;
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
