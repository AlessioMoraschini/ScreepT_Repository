package gUI.utils.filedialog;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import java.awt.Color;


public class AdWaitDialog extends JDialog {

    private JOptionPane optionPane;
    private String msgErr = "An Error occurred :(";
    private String msgFinish = "Image Ready :)";
    private String msgWait = "I'm generating the Image... this could take up to 20-30 seconds for big Images, heavy stuff!";

    /**
     * Creates the reusable dialog.
     */
    public AdWaitDialog(String title, JFrame aFrame, boolean close, JProgressBar bar) {
        super(aFrame, true);
        
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
        }while(close);

    }

}
