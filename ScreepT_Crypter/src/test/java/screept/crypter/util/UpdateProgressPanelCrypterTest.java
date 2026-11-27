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
