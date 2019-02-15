package gUI.utils.progressDialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;
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

	public final static int PanelWidth = 400;
	public final static int PanelHeight = 100;
	
	private boolean forceStop = false;
	
	// progressBars
    private volatile JProgressBar pb;
    private volatile JProgressBar GeneralBar;
    
    private JDialog dialog;
    private JLabel label = new JLabel("  I'm workin' !");
    
    private INItializer config;
    
    private GridBagConstraints gbc_pb;
    private GridBagConstraints gbc_genb;
    
    // CONSTRUCTORS
    
    public TestUpdateProgress() {
		
		initGraphics(1);
		
		initSpecBar();
		
		new Thread(new Runnable() {

            public void run() {

                try {
					doInBackground(pb);
				} catch (Exception e) {
					logger.error("Catched exception while monitoring progress!",e);
		            dialog.dispose();
				}
                
        	}
        }).start();
    }

	public TestUpdateProgress(INItializer currentConf) {
    	
		config = currentConf;
		
		initGraphics(1);
		
		initSpecBar();
		
		new Thread(new Runnable() {

            public void run() {

                try {
					doInBackground(pb);
				} catch (Exception e) {
					logger.error("Catched exception while monitoring progress!",e);
		            dialog.dispose();
				}
                
        	}
        }).start();
        
    }
	
	public TestUpdateProgress(INItializer currentConf, boolean secondBar) {
    	
		config = currentConf;

		initGraphics(2);
		
		initSpecBar();
		
		initGeneralBar();
		
        if(secondBar) {
        	initGeneralBar();
        }
        
        new Thread(new Runnable() {

            public void run() {

                try {
					doInBackground(GeneralBar);
				} catch (Exception e) {
					logger.error("Catched exception while monitoring progress!",e);
		            dialog.dispose();
				}
                
        	}
        }).start();
    }
	
	private synchronized void initGeneralBar() {
		
		//TODO set progress bar
        GeneralBar = new JProgressBar();       
        GeneralBar.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);

        GeneralBar.setStringPainted(true);
        
        dialog.getContentPane().add(GeneralBar, gbc_pb);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
		
	}
	
	private synchronized void initSpecBar() {
		
        // set progress bar
        pb = new JProgressBar();       
        pb.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);

		pb.setStringPainted(true);
       
        dialog.getContentPane().add(pb, gbc_pb);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
		
	}
	
	private synchronized void initGraphics(int nYrows) {
		
		if (config!=null) {
			try {
				// set preferred style from current configuration
				UIManager.setLookAndFeel(config.getGuiOpt().getPreferredStyle());
				UIManager.put("nimbusOrange", Color.GREEN);

			} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
					| IllegalAccessException ex) {
				logger.error("Error setting current look and feel! :(");
			} 
		}
		
		dialog = new JDialog();
        dialog.setMinimumSize(new Dimension(PanelWidth, PanelHeight));
        dialog.setTitle("Processing");
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0};
        gridBagLayout.columnWidths = new int[]{0, 10, 100, 0, 10};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0};
        gridBagLayout.rowHeights = new int[]{60, 40, 5};
        dialog.getContentPane().setLayout(gridBagLayout);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setAlwaysOnTop(true);
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.gridwidth = 3;
        gbc_label.insets = new Insets(0, 0, 5, 0);
        gbc_label.gridx = 1;
        gbc_label.gridy = 0;
        dialog.getContentPane().add(label, gbc_label);
        
        gbc_pb = new GridBagConstraints();
        gbc_pb.insets = new Insets(0, 0, 5, 5);
        gbc_pb.fill = GridBagConstraints.BOTH;
        gbc_pb.gridx = 2;
        gbc_pb.gridy = nYrows;
        
        if (nYrows>1) {
			gbc_genb = new GridBagConstraints();
			gbc_genb.insets = new Insets(0, 0, 5, 5);
			gbc_genb.fill = GridBagConstraints.BOTH;
			gbc_genb.gridx = 2;
			gbc_genb.gridy = nYrows - 1;
		}
	}

    protected void doneCheck(JProgressBar bar) {
        if (bar.getValue()>=100) {
            dialog.dispose();
        }
    }

    protected void doInBackground(JProgressBar bar) throws Exception {
        while(pb.getValue()<100 && !forceStop) {
            Thread.sleep(150);
            doneCheck(bar);
        }
    }         
    
    public void forceClose() {
    	forceStop = true;
    }

	public JProgressBar getPb() {
		return pb;
	}

	public void setPb(JProgressBar pb) {
		this.pb = pb;
	}
		
    public JProgressBar getGeneralBar() {
		return GeneralBar;
	}

	public void setGeneralBar(JProgressBar generalBar) {
		GeneralBar = generalBar;
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
