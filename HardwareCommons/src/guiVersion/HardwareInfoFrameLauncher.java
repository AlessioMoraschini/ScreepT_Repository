package guiVersion;

import javax.swing.JFrame;

import org.apache.log4j.PropertyConfigurator;

import frameutils.utils.GuiUtilsExtended;
import resources.GeneralConfig;

public class HardwareInfoFrameLauncher {

	public static void main(String[] args) {
		
		// Initialize logger properties
		PropertyConfigurator.configure(GeneralConfig.LOG_PROPERTIES_PATH);
		System.setProperty("java.library.path", GeneralConfig.RESOURCES_DIR + "lib");
					
		HwInfoFrame frame = new HwInfoFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(250, 200, 1000, 750);
		GuiUtilsExtended.centerComponent(frame);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
		frame.setTitle("Hardware info - "+GeneralConfig.APPLICATION_NAME);
		
		// START CHECKING FOR EXTERNAL SIGNAL to close
		frame.startExternalSignalCloseFlagCheck();
	}
}
