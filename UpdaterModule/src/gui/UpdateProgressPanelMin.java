package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import exception.ProgressBarInterruptedException;
import resources.GeneralConfig;
import utils.Various;

/**
 * This is a class that implements a customizable closable progress bar panel, with up to two progressBars.
 * Progress Panel can be closable or not (by default is closable), just call method 
 * @author xx
 *
 */
public class UpdateProgressPanelMin {
	
	private final static int LeftStart = 400;
	private final static int TopStart = 300;
	public final static int PanelWidth = 400;
	public final static int PanelHeight = 100;
	
	// progressBars
    private volatile JProgressBar pb;
    private volatile JProgressBar GeneralBar;
    public AtomicBoolean progBarEnabled = new AtomicBoolean(true);
    
    public AtomicBoolean closable = new AtomicBoolean();
    public AtomicBoolean stopProcess = new AtomicBoolean();
    
    private JDialog dialog;
    private JLabel label = new JLabel("  I'm workin' !");
    
    private GridBagConstraints gbc_pb;
    private GridBagConstraints gbc_genb;
    
    public boolean secondBarEnabled = false;
    
    // ####################### START CONSTRUCTORS - USE THE ONE WITH TWO PARAMS #################### //
    
    public UpdateProgressPanelMin() {
		
    	initGraphics(1);
    	
    	initSpecBar();
    	
    	dialog.setVisible(true);
    	dialog.repaint();
		
		new Thread(new Runnable() {

            public void run() {

                try {
					doInBackground(pb);
				} catch (Exception e) {
		            dialog.dispose();
				}
                
        	}
        }).start();
    }
    
    public UpdateProgressPanelMin(boolean secondBar) {
    	
		if (secondBar) {
			initGraphics(2);
			initGeneralBar();
			secondBarEnabled = true;
		}else {
			initGraphics(1);
			secondBarEnabled = false;
		}
		
		initSpecBar();
		
		dialog.setVisible(true);
		dialog.repaint();
		
        new Thread(new Runnable() {

            public void run() {

                try {
                	if (secondBar) {
                		doInBackground(GeneralBar);
                	}else {
                		doInBackground(pb);
                	}
                	
				} catch (Exception e) {
		            dialog.dispose();
				}
                
        	}
        }).start();
    }

	// ####################### END CONSTRUCTORS #################### //
	
	private synchronized void initGeneralBar() {
		
        GeneralBar = new JProgressBar();       
        GeneralBar.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);

        GeneralBar.setStringPainted(true);
        
        dialog.getContentPane().add(GeneralBar, gbc_genb);
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
		
		try {
			// set preferred style from current configuration
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			UIManager.put("nimbusOrange", Color.GREEN);

		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
				| IllegalAccessException ex) {
			System.out.println("Error setting current look and feel! :(");
		} 
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0};
		gridBagLayout.columnWidths = new int[]{0, 10, 100, 0, 10};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0};
		gridBagLayout.rowHeights = new int[]{40, 40, 25};
		
		dialog = new JDialog();
		dialog.setMinimumSize(new Dimension(PanelWidth, PanelHeight));
        dialog.setBounds(new Rectangle(LeftStart, TopStart, PanelWidth, PanelHeight));
        dialog.setTitle("Processing");
        dialog.getContentPane().setLayout(gridBagLayout);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setAlwaysOnTop(true);
        
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.gridwidth = 3;
        gbc_label.insets = new Insets(0, 0, 5, 0);
        gbc_label.gridx = 1;
        gbc_label.gridy = 0;
        dialog.getContentPane().add(label, gbc_label);
        label.setFont(GeneralConfig.BUTTON_DEFAULT_FONT.deriveFont(16));
       
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
        
        if(!progBarEnabled.get()) {
        	dialog.setVisible(false);
        }
        
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
	        @Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
	        	if(closable.get()) {
	        		stopProcess.set(true);
        			int choice = JOptionPane.showConfirmDialog(null, "Stop process?", "Stop process?", JOptionPane.QUESTION_MESSAGE);
        			if (choice == JOptionPane.YES_OPTION) {
        				stopProcess.set(true);
        			} else {
        				stopProcess.set(false);
        			}
	        	}else {
	        		stopProcess.set(false);
	        	}
	        }
        });
	}
	
	public void setUndefinedDuration() {
		pb.setIndeterminate(true);
		if(GeneralBar != null) {
			GeneralBar.setIndeterminate(true);
		}
	}

    protected void doneCheck(JProgressBar bar) {
        if (bar.getValue()>=100) {
            dialog.dispose();
        }
    }
    
    public void setClosable(boolean closable) {
    	this.closable.set(closable);
    }

    protected void doInBackground(JProgressBar bar) throws Exception {
        while(bar.getValue()<100 && !stopProcess.get()) {
        	
        	if(!progBarEnabled.get()) {
            	dialog.setVisible(false);
            }else if(!dialog.isVisible()){
            	dialog.setVisible(true);
            }
        	
        	pb.repaint();
        	GeneralBar.repaint();
        	doneCheck(bar);
        	
            Thread.sleep(150);
        } // End while
        
        forceClose();
    }        
    
    public void update(String label, int perc) {
    	if (getPb() != null) {
			this.getPb().setValue(perc);
		}
		this.getLabel().setText(label);
    	if (GeneralBar != null) {
			this.GeneralBar.setValue(perc);
		}
    }
    
    public void forceClose() {
    	stopProcess.set(true);
    	dialog.dispose();
    }

    /**
     * Use this and catch when you want to kill process and undo work when user clicked X to stop a process if it's stoppable.
     * @throws ProgressBarInterruptedException
     */
    public void forceCloseNExceptionSignal() throws ProgressBarInterruptedException {
    	stopProcess.set(true);
    	dialog.dispose();
    	throw new ProgressBarInterruptedException("Forcing process stop: user called forceCloseNExceptionSignal()");
    }
	
	/**
	 * 
	 * @param value integer progress from 1 to 100: if more or equal to 100 dialog will close
	 * @param msg can be null, will be display on dialog
	 * @throws ProgressBarInterruptedException 
	 */
	public synchronized void update( int value, String msg, boolean useGeneralBar) {
		
		if (!useGeneralBar) {
			this.pb.setValue(value);
			this.pb.setStringPainted(true);
			this.pb.setString(value + "%");
			if (msg != null) {
				this.label.setText(msg);
			} 
		}else if(GeneralBar != null){
			this.GeneralBar.setValue(value);
			this.GeneralBar.setStringPainted(true);
			this.GeneralBar.setString(value + "%");
			if (msg != null) {
				this.label.setText(msg);
			} 
		}
	}
	
	public void toFront() {
		dialog.setAlwaysOnTop(true);
	}
	
	public void move(int xDiff, int yDiff) {
		Various.moveFrame(dialog, xDiff, yDiff);
	}
	
	public void hide() {
		dialog.setVisible(false);
	}

	public void show() {
		dialog.setVisible(true);
	}
	
	// ############### GETTERS AND SETTERS ################### //
	
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

	public AtomicBoolean getProgBarEnabled() {
		return progBarEnabled;
	}

	public void setProgBarEnabled(boolean progBarEnabled) {
		this.progBarEnabled = new AtomicBoolean(progBarEnabled);
		dialog.setVisible(progBarEnabled);
	}
	
}
