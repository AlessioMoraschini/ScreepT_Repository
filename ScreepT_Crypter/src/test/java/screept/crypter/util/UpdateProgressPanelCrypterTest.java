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
package screept.crypter.util;

import static org.junit.Assert.assertFalse;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.junit.Assume;
import org.junit.Test;

public class UpdateProgressPanelCrypterTest {

	/** The screen thread waits for the lock of update() while the work thread that holds it closes the window */
	@Test
	public void closingFromAWorkThreadNeverWaitsForTheScreenThread() throws Exception {
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		UpdateProgressPanelCrypter panel = new UpdateProgressPanelCrypter(false);
		panel.getDialog().addNotify(); // a displayable window: only then dispose() has something to wait for
		CountDownLatch screenThreadBusy = new CountDownLatch(1);
		Thread worker = new Thread(() -> {
			synchronized (panel) {
				SwingUtilities.invokeLater(() -> {
					screenThreadBusy.countDown();
					panel.forceClose();
				});
				try {
					screenThreadBusy.await();
					Thread.sleep(200); // the screen thread is now waiting for the lock
				} catch (InterruptedException e) {
					return;
				}
				panel.forceClose();
			}
		});
		worker.setDaemon(true);
		worker.start();
		worker.join(TimeUnit.SECONDS.toMillis(10));
		assertFalse("deadlock between the work thread and the screen thread", worker.isAlive());
	}
}
