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
package tabbededitor;

import java.io.File;

public interface IChangeMonManager {

	public void checkChanges();
	public void updateCurrMonitor(File file);
	public void checkAndAdviceIfCurrentChanged(boolean forceCheck, boolean ask) throws Exception;
	public boolean isAlreadyChecking();
	public boolean setAlreadyChecking(boolean alreadyChecking);
	public Runnable getRunnableChanges();
	public void cleanFromHistory(File target);
	public void removePendingFiredChanges(File target);
}
