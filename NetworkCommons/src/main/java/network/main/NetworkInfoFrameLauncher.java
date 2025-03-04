/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package network.main;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import gui.commons.frameutils.utils.GuiUtilsExtended;
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
		GuiUtilsExtended.centerComponent(frame);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
		frame.setTitle("Network info - "+GeneralConfig.APPLICATION_NAME);
	}

}
