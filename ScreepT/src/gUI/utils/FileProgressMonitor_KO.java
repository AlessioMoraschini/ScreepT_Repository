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
package gUI.utils;

import gui.UpdateProgressPanel;

public class FileProgressMonitor_KO {
	
	// this is the tread that update the progress;
	public static Thread moveUpdater;
	
	// flag used to exit loop
	public static volatile boolean finished = false;
	
	// progress GUI bar
	public static UpdateProgressPanel progUpdater;
	
//////////////////////////////
		// METHODS //
		/////////////

//	not working
//	/**
//	 * this method is useful when knowing the final size to reach, assuming that it's increasing until 100%
//	 * @param file the file to monitor
//	 * @param finalSize the wanted size to set as 100%
//	 */
//	public static void monitorProgress(File file, Long finalSize) {
//		
//		progUpdater = new TestUpdateProgress();
//		
//		moveUpdater = new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				
//				progUpdater.getPb().setValue(0);
//				progUpdater.getLabel().setText("Workin on "+file.getName() +"...");
//				
//				while(!file.exists()) {
//					
//				}
//				
//				while(!finished) {
//					int percentage = (int)((float)file.length()/(float) finalSize) * 100;
//					progUpdater.getPb().setValue(percentage);
//					try {
//						Thread.sleep(20);
//					} catch (InterruptedException e) {
//					}
//				}
//			}
//		});
//		
//		// start update progress
//		moveUpdater.start();
//	}
}
