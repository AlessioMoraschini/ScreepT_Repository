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
package gui.texteditor.commons.tabbededitor;

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
	public void finalize() throws Throwable;
}
