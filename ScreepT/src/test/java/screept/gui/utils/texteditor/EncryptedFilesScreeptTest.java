package screept.gui.utils.texteditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import screept.crypter.engine.CryptoEngine;
import screept.crypter.engine.CryptoException;
import screept.crypter.engine.CryptoKey;
import screept.crypter.engine.CryptoProfile;

/**
 * Encrypted files in the editor: recognising them, and the wait window that appears while the key is derived (about a second):
 * it must appear on the interface thread, close by itself when the work ends and hand back the result or the failure.
 */
public class EncryptedFilesScreeptTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static int visibleWindows() {
		int n = 0;
		for (Window w : Window.getWindows()) {
			if (w.isShowing()) {
				n++;
			}
		}
		return n;
	}

	@Test
	public void onlyFilesInTheCrypterFormatAreRecognised() throws Exception {
		EncryptedFilesScreepT support = new EncryptedFilesScreepT(null, () -> false);
		File plain = tmp.newFile("plain.txt");
		Files.write(plain.toPath(), "just text, long enough to be bigger than the header".getBytes(StandardCharsets.UTF_8));
		assertFalse(support.isEncrypted(plain));
		assertFalse("empty file", support.isEncrypted(tmp.newFile("empty.txt")));
		assertFalse("missing file", support.isEncrypted(new File(tmp.getRoot(), "missing.bin")));

		File secret = tmp.newFile("secret.enc");
		Files.write(secret.toPath(), CryptoEngine.encrypt("hello".getBytes(StandardCharsets.UTF_8), CryptoKey.of("password-123".toCharArray()), CryptoProfile.standard()));
		assertTrue(support.isEncrypted(secret));
	}

	@Test
	public void sessionRestoreNeverAsksForAPassword() throws Exception {
		File secret = tmp.newFile("restore.enc");
		Files.write(secret.toPath(), CryptoEngine.encrypt("hello".getBytes(StandardCharsets.UTF_8), CryptoKey.of("password-123".toCharArray()), CryptoProfile.standard()));
		assertEquals("the file just stays closed", null, new EncryptedFilesScreepT(null, () -> true).open(secret));
	}

	@Test
	public void waitWindowShowsWhileTheWorkRunsAndClosesWithItsResult() throws Exception {
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		final JFrame frame = new JFrame("owner");
		final int before = visibleWindows();
		final AtomicReference<Integer> shownDuringWork = new AtomicReference<Integer>(-1);
		final AtomicReference<String> result = new AtomicReference<String>();
		try {
			SwingUtilities.invokeAndWait(() -> {
				try {
					result.set(EncryptedFilesScreepT.withWaitCursor(frame, () -> {
						// measured early: the test run's dialog closer (AutoDialogCloser) answers any modal window after ~300 ms
						Thread.sleep(150);
						shownDuringWork.set(visibleWindows());
						return "derived";
					}));
				} catch (CryptoException e) {
					fail("unexpected " + e);
				}
			});
		} finally {
			frame.dispose();
		}
		assertEquals("derived", result.get());
		assertTrue("the wait window was visible during the work (" + shownDuringWork.get() + " vs " + before + ")", shownDuringWork.get() > before);
		assertEquals("and gone afterwards", before, visibleWindows());
	}

	@Test
	public void failuresOfTheWorkReachTheCallerAndStillCloseTheWindow() throws Exception {
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		final int before = visibleWindows();
		final AtomicReference<Throwable> caught = new AtomicReference<Throwable>();
		SwingUtilities.invokeAndWait(() -> {
			try {
				EncryptedFilesScreepT.<String>withWaitCursor(null, () -> {
					throw new CryptoException("wrong password");
				});
			} catch (Throwable t) {
				caught.set(t);
			}
		});
		assertTrue(String.valueOf(caught.get()), caught.get() instanceof CryptoException);
		assertEquals(before, visibleWindows());
	}

	@Test
	public void outsideTheInterfaceThreadTheWorkJustRuns() throws Exception {
		assertEquals("plain", EncryptedFilesScreepT.withWaitCursor(null, () -> "plain"));
	}
}
