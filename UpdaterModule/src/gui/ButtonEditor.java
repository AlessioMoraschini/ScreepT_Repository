package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import exception.ProgressBarInterruptedException;
import forks.merged.Various;
import updater.PluginDTO;
import updater.PluginManager;

class ButtonEditor extends DefaultCellEditor {
	private static final long serialVersionUID = 2322648608181904669L;

	protected JButton button;
    private String label;
    private boolean isPushed;
    
    private IPluginManagerGui pluginManagerInterface;

    public ButtonEditor(IPluginManagerGui pluginManagerInterface) {
        super(new JCheckBox());
        this.button = new JButton();
        this.button.setForeground(Color.BLACK);
        this.button.setBackground(Color.LIGHT_GRAY);
        this.button.setOpaque(false);
        this.pluginManagerInterface = pluginManagerInterface;
        this.button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				 fireEditingStopped();
			}
		});
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
    	
    	String pluginName = (String)table.getValueAt(row, 0);
    	Various.launchThreadSafeSwing(getActionOnClick(pluginName));
    	PluginDTO clickedButtonPlugin = pluginManagerInterface.getPluginDTO(pluginName);

    	String installStr = clickedButtonPlugin.installationCompleted ? "Uninstall " : "Install ";
    	label = installStr + pluginName;
        button.setText(label);
        isPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (isPushed) {
        	
        }
        isPushed = false;
        return label;
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }
    
    public Runnable getActionOnClick(String pluginName) {
    	return new Runnable() {
			public void run() {
				if (!pluginManagerInterface.isInstalling().get()) {
					pluginManagerInterface.setInstalling(true);
					
					PluginDTO clickedButtonPlugin = pluginManagerInterface.getPluginDTO(pluginName);
					
					boolean installed = clickedButtonPlugin.installationCompleted;
					PluginManager pluginManager = pluginManagerInterface.getPluginManager();
					JFrame thisPanel = pluginManagerInterface.getFrame();

					try {
						if (installed) {
								
							PluginDTO refreshedPlugin = pluginManager.retrieveFromCache(clickedButtonPlugin.getName());

							boolean uninstalled = pluginManager.removePlugin(refreshedPlugin);
							if (uninstalled) {
								String message = "Plugin uninstalled correctly ;)";
								JOptionPane.showMessageDialog(thisPanel, message, "Plugin uninstalled", JOptionPane.INFORMATION_MESSAGE);
							} else {
								String message = "An error occurred during plugin removal!";
								JOptionPane.showMessageDialog(thisPanel, message, "An error occurred", JOptionPane.ERROR_MESSAGE);
							}

						} else {
							try {
								PluginDTO refreshedPlugin = pluginManager.retrieveFromCache(clickedButtonPlugin.getName());

								PluginDTO downloaded = pluginManager.downloadPlugin(refreshedPlugin);
								pluginManager.installPlugin(downloaded, true);
								String message = "Installation completed! Plugin will be available after restart.";
								JOptionPane.showMessageDialog(thisPanel, message, "Plugin installed", JOptionPane.INFORMATION_MESSAGE);

							} catch (Throwable e) {
								if (e instanceof ProgressBarInterruptedException) {
									String message = "Installation process killed by user.";
									JOptionPane.showMessageDialog(thisPanel, message, "Interrupted by user", JOptionPane.INFORMATION_MESSAGE);
								} else {
									String message = "An error occurred during installation";
									JOptionPane.showMessageDialog(thisPanel, message, "An error occurred", JOptionPane.ERROR_MESSAGE);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							pluginManagerInterface.refreshTable(true);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
					
					pluginManagerInterface.setInstalling(false);
				} else {
					JOptionPane.showMessageDialog(pluginManagerInterface.getFrame(), "Must wait current installation to finish before to start another one!", "Already installing", JOptionPane.WARNING_MESSAGE);
				}
			}
		};
    }
}