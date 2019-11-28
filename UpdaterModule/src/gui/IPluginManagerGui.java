package gui;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import updater.PluginDTO;
import updater.PluginManager;

public interface IPluginManagerGui {
	
	public PluginManager getPluginManager();
	public PluginDTO getPluginDTO(String nameWithoutExtension);
	public AtomicBoolean isInstalling();
	public void setInstalling(boolean installing);
	public JFrame getFrame();
	public JFrame getParentFrame();
	public void refreshTable(boolean useCache) throws Throwable;
	public void refreshGuiLibsAfterChange();
}
