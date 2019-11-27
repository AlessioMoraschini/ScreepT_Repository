package various.test;

import javax.swing.SwingUtilities;

import org.junit.Before;
import org.junit.Test;

import gui.PluginManagerGUI;
import updater.PluginDTO;
import updater.PluginManager;

public class TestPlugins {

	static PluginManager manager;
	static PluginManagerGUI managerGUI;
	
	@Before
	public void init() {
		manager = new PluginManager();
	}
	
	@Test
	public void testDownloadPLuginsList() throws Throwable {
		
		manager.discoverLatestPlugins();
		
		SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	managerGUI = new PluginManagerGUI(null, manager);
	        	managerGUI.setVisible(true);
	        }
	    });
		
		for(PluginDTO i : manager.getAllFromCache(false)) {
			System.out.println(i + "\n\n");
		}
		
	}
	
	public static void main(String[] args) throws Throwable {
		manager = new PluginManager();
		manager.discoverLatestPlugins();
		
		SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	managerGUI = new PluginManagerGUI(null, manager);
	        	managerGUI.setVisible(true);
	        }
	    });
		
		for(PluginDTO i : manager.getAllFromCache(false)) {
			System.out.println(i + "\n\n");
		}
	}
}
