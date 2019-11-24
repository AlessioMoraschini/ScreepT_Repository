import java.util.ArrayList;

import org.junit.Test;

import updater.PluginDTO;
import updater.PluginManager;

public class TestPlugins {

	@Test
	public void testDownloadPLuginsList() {
		
		ArrayList<PluginDTO> list = new PluginManager().getLatestPlugins();
		
		for(PluginDTO i : list) {
			System.out.println(i + "\n\n");
		}
	}
}
