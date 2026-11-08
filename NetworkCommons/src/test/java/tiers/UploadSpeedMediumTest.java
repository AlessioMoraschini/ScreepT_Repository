package tiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JProgressBar;

import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import network.NetworkUtils;
import network.om.FtpSpeedTestOptions;

/** MEDIUM tier: the upload speed test against local servers (nothing leaves the PC) */
public class UploadSpeedMediumTest {

	private static HttpServer server(final int status, final AtomicLong received) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/up", exchange -> {
			InputStream in = exchange.getRequestBody();
			byte[] buffer = new byte[65536];
			int n;
			while ((n = in.read(buffer)) >= 0) {
				received.addAndGet(n);
			}
			exchange.sendResponseHeaders(status, -1);
			exchange.close();
		});
		server.start();
		return server;
	}

	private static String url(HttpServer server) {
		return "http://127.0.0.1:" + server.getAddress().getPort() + "/up";
	}

	@Test
	public void sendsTheRandomBytesAndReportsASpeed() throws Exception {
		AtomicLong received = new AtomicLong();
		HttpServer ok = server(200, received);
		try {
			JProgressBar progress = new JProgressBar();
			String speed = NetworkUtils.retrieveUpSpeedV2(() -> false, InetAddress.getByName("127.0.0.1"), FtpSpeedTestOptions.SHORT_TEST, progress, url(ok));
			assertTrue(speed, speed.contains("/s"));
			assertTrue("payload of at least 1 MB", received.get() >= 1024 * 1024);
			assertEquals(100, progress.getValue());
		} finally {
			ok.stop(0);
		}
	}

	@Test
	public void movesToTheNextServerWhenTheFirstOneFails() throws Exception {
		AtomicLong received = new AtomicLong();
		HttpServer broken = server(500, new AtomicLong());
		HttpServer ok = server(200, received);
		try {
			String speed = NetworkUtils.retrieveUpSpeedV2(() -> false, InetAddress.getByName("127.0.0.1"), FtpSpeedTestOptions.SHORT_TEST, new JProgressBar(), url(broken), url(ok));
			assertTrue(speed.contains("/s"));
			assertTrue(received.get() > 0);
		} finally {
			broken.stop(0);
			ok.stop(0);
		}
	}

	@Test
	public void saysSoWhenNoServerWorks() throws Exception {
		HttpServer broken = server(500, new AtomicLong());
		try {
			NetworkUtils.retrieveUpSpeedV2(() -> false, InetAddress.getByName("127.0.0.1"), FtpSpeedTestOptions.SHORT_TEST, new JProgressBar(), url(broken));
			fail("an error was expected");
		} catch (IOException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().startsWith("Upload failed"));
		} finally {
			broken.stop(0);
		}
	}
}
