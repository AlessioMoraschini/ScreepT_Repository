import java.io.File;
import java.util.ArrayList;

import org.junit.Test;

import updater.PluginDTO;
import updater.PluginManager;

public class TestPlugins {

	@Test
	public void testDownloadPLuginsList() throws Throwable {
		
		PluginManager manager = new PluginManager();
		ArrayList<PluginDTO> list = manager.discoverLatestPlugins();
		
		for(PluginDTO i : list) {
			System.out.println(i + "\n\n");
		}
		
		System.out.println(manager.getFileAsStringListFromFolder(
				new File("PLUGINS/JnetPcap")));
	}
}
