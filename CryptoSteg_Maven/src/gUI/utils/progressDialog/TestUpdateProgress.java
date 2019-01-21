package gUI.utils.progressDialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

import gUI.utils.tree.TreeFileMouseListenerDictionary;
import iNIT.INItializer;

public class TestUpdateProgress {
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TestUpdateProgress.class); 

    private volatile JProgressBar pb;
    private JDialog dialog;
    private JLabel label = new JLabel("  I'm workin' !");

	public TestUpdateProgress(INItializer currentConf) {
    	
        try {
        	// set preferred style from current configuration
            UIManager.setLookAndFeel(currentConf.getGuiOpt().getPreferredStyle());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        	logger.error("Error setting current look and feel! :(");
        }
		
        dialog = new JDialog();
        dialog.setMinimumSize(new Dimension(250, 100));
        dialog.setTitle("Processing");
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0};
        gridBagLayout.columnWidths = new int[]{0, 10, 100, 0, 10};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0};
        gridBagLayout.rowHeights = new int[]{60, 40, 5};
        dialog.getContentPane().setLayout(gridBagLayout);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setAlwaysOnTop(true);
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(2, 2, 2, 2);
//        gbc.weightx = 1;
//        gbc.gridy = 0;
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.gridwidth = 3;
        gbc_label.insets = new Insets(0, 0, 5, 0);
        gbc_label.gridx = 1;
        gbc_label.gridy = 0;
        dialog.getContentPane().add(label, gbc_label);
        
        // set progress bar
        pb = new JProgressBar();       
        UIManager.put("nimbusOrange", Color.GREEN);
        pb.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);

//        // bar color
//        UIManager.put("ProgressBar.background", Color.gray);
//        UIManager.put("ProgressBar.foreground", Color.GREEN);
//        // text color
//        UIManager.put("ProgressBar.selectionBackground", Color.darkGray);
//        UIManager.put("ProgressBar.selectionForeground", Color.BLACK);
		pb.setStringPainted(true);
        
        GridBagConstraints gbc_pb = new GridBagConstraints();
        gbc_pb.insets = new Insets(0, 0, 5, 5);
        gbc_pb.fill = GridBagConstraints.BOTH;
        gbc_pb.gridx = 2;
        gbc_pb.gridy = 1;
        dialog.getContentPane().add(pb, gbc_pb);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
		
        new Thread(new Runnable() {

            public void run() {

                try {
					doInBackground();
				} catch (Exception e) {

					e.printStackTrace();
				}
                
        	}
        }).start();
    }

    protected void doneCheck() {
        if (pb.getValue()>=100) {
            dialog.dispose();
        }
    }

    protected void doInBackground() throws Exception {
        while(pb.getValue()<=101) {
            Thread.sleep(150);
            doneCheck();
        }
    }         

	public JProgressBar getPb() {
		return pb;
	}

	public void setPb(JProgressBar pb) {
		this.pb = pb;
	}
		
    public JDialog getDialog() {
		return dialog;
	}

	public void setDialog(JDialog dialog) {
		this.dialog = dialog;
	}

	public JLabel getLabel() {
		return label;
	}

	public void setLabel(JLabel label) {
		this.label = label;
	}
	
}
