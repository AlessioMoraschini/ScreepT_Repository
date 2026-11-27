/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.gui.utils;

import various.common.light.gui.UpdateProgressPanel;

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
