package guiVersion;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import frameutils.utils.GuiUtils;
import resources.GeneralConfig;

public class NetworkInfoFrameLauncher {
	// NetworkInfoFrameLauncher logger
	static Logger logger = Logger.getLogger(NetworkInfoFrameLauncher.class); 
		
	public static void main(String[] args) {
		
		
		// Initialize logger properties
		PropertyConfigurator.configure(GeneralConfig.LOG_PROPERTIES_PATH);
		System.setProperty("java.library.path", GeneralConfig.RESOURCES_DIR + "lib");
					
		NetInfoFrame frame = new NetInfoFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(150, 150, 900, 650);
		GuiUtils.centerComponent(frame);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
		frame.setTitle("Network info - "+GeneralConfig.APPLICATION_NAME);
	}

}
