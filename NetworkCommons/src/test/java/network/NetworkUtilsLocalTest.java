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
package network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.URI;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JProgressBar;

import org.apache.http.client.config.RequestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import network.om.FtpSpeedTestOptions;
import resources.GeneralConfig;

/**
 * Speed, latency and public IP functions of {@link NetworkUtils} against servers on 127.0.0.1 only.
 */
public class NetworkUtilsLocalTest {

	private static final int BODY_BYTES = 3 * 1024 * 1024;

	private HttpServer server;
	private ServerSocket dropper;
	private String savedTestAddress;
	private String savedUpUrl;
	private String savedUpFallback;
	private int savedTimeout;
	private String savedDownName;
	private int savedDownSize;

	@Before
	public void remember() {
		savedTestAddress = GeneralConfig.testAddress;
		savedUpUrl = GeneralConfig.UP_URL;
		savedUpFallback = GeneralConfig.UP_URL_FALLBACK;
		savedTimeout = NetworkUtils.getTimeout();
		savedDownName = FtpSpeedTestOptions.SHORT_TEST.downFileName;
		savedDownSize = FtpSpeedTestOptions.SHORT_TEST.downSize;
	}

	@After
	public void restore() {
		GeneralConfig.testAddress = savedTestAddress;
		GeneralConfig.UP_URL = savedUpUrl;
		GeneralConfig.UP_URL_FALLBACK = savedUpFallback;
		NetworkUtils.setTimeout(savedTimeout);
		FtpSpeedTestOptions.SHORT_TEST.downFileName = savedDownName;
		FtpSpeedTestOptions.SHORT_TEST.downSize = savedDownSize;
		if (dropper != null) {
			try {
				dropper.close();
			} catch (IOException e) {
				// ignored
			}
			dropper = null;
		}
		if (server != null) {
			server.stop(0);
			server = null;
		}
	}

	private static InetAddress loopback() throws IOException {
		return InetAddress.getByName("127.0.0.1");
	}

	private String startServer(final String path, final int status, final byte[] body, final AtomicLong received) throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext(path, exchange -> {
			InputStream in = exchange.getRequestBody();
			byte[] buffer = new byte[65536];
			int n;
			while ((n = in.read(buffer)) >= 0) {
				if (received != null) {
					received.addAndGet(n);
				}
			}
			if (body == null) {
				exchange.sendResponseHeaders(status, -1);
			} else {
				exchange.sendResponseHeaders(status, body.length);
				exchange.getResponseBody().write(body);
			}
			exchange.close();
		});
		server.start();
		return "http://127.0.0.1:" + server.getAddress().getPort() + path;
	}

	/** A local server that hangs up on every connection: fails at once (a refused connection takes 2 s on Windows) */
	private String closedPortUrl() throws IOException {
		final ServerSocket s = new ServerSocket(0, 5, loopback());
		dropper = s;
		Thread t = new Thread(() -> {
			try {
				while (true) {
					s.accept().close();
				}
			} catch (IOException e) {
				// server closed by the test
			}
		}, "dropping-server");
		t.setDaemon(true);
		t.start();
		return "http://127.0.0.1:" + s.getLocalPort() + "/x";
	}

	// ------------------------------------------------------------------ download

	@Test
	public void downloadSpeedReadsTheWholeBody() throws Exception {
		String url = startServer("/down", 200, new byte[BODY_BYTES], null);
		FtpSpeedTestOptions.SHORT_TEST.downFileName = url;
		FtpSpeedTestOptions.SHORT_TEST.downSize = BODY_BYTES;
		JProgressBar bar = new JProgressBar();

		String speed = NetworkUtils.retrieveDownSpeedV2(() -> false, loopback(), FtpSpeedTestOptions.SHORT_TEST, bar);

		assertTrue(speed, speed.contains("/s"));
		assertTrue(speed, speed.contains("ps]"));
		assertEquals(100, bar.getValue());
		assertEquals(false, bar.isIndeterminate());
	}

	@Test
	public void downloadCanBeStoppedByTheUser() throws Exception {
		String url = startServer("/down", 200, new byte[BODY_BYTES], null);
		FtpSpeedTestOptions.SHORT_TEST.downFileName = url;
		FtpSpeedTestOptions.SHORT_TEST.downSize = BODY_BYTES;
		JProgressBar bar = new JProgressBar();
		try {
			NetworkUtils.retrieveDownSpeedV2(() -> true, loopback(), FtpSpeedTestOptions.SHORT_TEST, bar);
			fail("the stop request must interrupt the test");
		} catch (Exception e) {
			assertEquals("Stopped by user", e.getMessage());
		}
		// the bar goes back to a finished state also on failure
		assertEquals(100, bar.getValue());
		assertEquals(false, bar.isIndeterminate());
	}

	@Test
	public void downloadFromAnUnreachableServerFails() throws Exception {
		FtpSpeedTestOptions.SHORT_TEST.downFileName = closedPortUrl();
		try {
			NetworkUtils.retrieveDownSpeedV2(() -> false, loopback(), FtpSpeedTestOptions.SHORT_TEST, null);
			fail("failed connection expected");
		} catch (IOException expected) {
			assertNotNull(expected.getMessage());
		}
	}

	// ------------------------------------------------------------------ upload through the configured servers

	@Test
	public void uploadUsesTheConfiguredServerAndSkipsBlankOnes() throws Exception {
		AtomicLong received = new AtomicLong();
		GeneralConfig.UP_URL = "  ";
		GeneralConfig.UP_URL_FALLBACK = startServer("/up", 200, null, received);

		String speed = NetworkUtils.retrieveUpSpeedV2(() -> false, loopback(), FtpSpeedTestOptions.SHORT_TEST, null);

		assertTrue(speed, speed.contains("/s"));
		assertTrue(received.get() >= 1024 * 1024);
	}

	@Test
	public void uploadWithoutStopSupportAlsoWorks() throws Exception {
		AtomicLong received = new AtomicLong();
		GeneralConfig.UP_URL = startServer("/up", 200, null, received);
		GeneralConfig.UP_URL_FALLBACK = "";

		String speed = NetworkUtils.retrieveUpSpeed(loopback(), FtpSpeedTestOptions.SHORT_TEST, new JProgressBar());

		assertTrue(speed, speed.contains("/s"));
		assertTrue(received.get() > 0);
	}

	@Test
	public void uploadWithNoServerAtAllExplainsWhy() throws Exception {
		try {
			NetworkUtils.retrieveUpSpeedV2(() -> false, loopback(), FtpSpeedTestOptions.SHORT_TEST, null, "", "   ", null);
			fail("an error was expected");
		} catch (IOException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("No upload server"));
		}
	}

	@Test
	public void uploadStoppedByTheUserDoesNotTryOtherServers() throws Exception {
		AtomicLong received = new AtomicLong();
		String url = startServer("/up", 200, null, received);
		try {
			NetworkUtils.retrieveUpSpeedV2(() -> true, loopback(), FtpSpeedTestOptions.SHORT_TEST, null, url, url);
			fail("the stop request must be raised");
		} catch (IOException expected) {
			IOException e = expected;
			// only the 1 KB warm-up request arrived: the timed payload was never sent
			assertTrue("received " + received.get(), received.get() <= 1024);
		}
	}

	// ------------------------------------------------------------------ latency

	@Test
	public void latencyOverHttpIsMeasuredInMilliseconds() throws Exception {
		GeneralConfig.testAddress = startServer("/ping", 200, new byte[16], null);
		int latency = NetworkUtils.retrieveLatencyV2(loopback());
		assertTrue("latency " + latency, latency >= 0 && latency < 5000);
	}

	@Test
	public void latencyOverHttpFailsWhenTheServerIsDown() throws Exception {
		GeneralConfig.testAddress = closedPortUrl();
		try {
			NetworkUtils.retrieveLatencyV2(loopback());
			fail("failed connection expected");
		} catch (IOException expected) {
			assertNotNull(expected.getMessage());
		}
	}

	@Test
	public void latencyOverReachabilityOnTheLoopbackInterface() throws Exception {
		NetworkInterface lo = NetworkInterface.getByInetAddress(loopback());
		org.junit.Assume.assumeNotNull(lo);
		GeneralConfig.testAddress = "127.0.0.1";
		NetworkUtils.setTimeout(500);
		int avg = NetworkUtils.retrieveLatency(lo);
		assertTrue("average " + avg, avg >= 0 && avg <= 500);
	}

	// ------------------------------------------------------------------ public IP

	@Test
	public void publicIpIsReadFromTheFirstProviderThroughTheAdapter() throws Exception {
		NetworkUtils utils = new NetworkUtils();
		String url = startServer("/ip", 200, "203.0.113.7".getBytes("UTF-8"), null);
		NetworkUtils.getPubIPServices().clear();
		NetworkUtils.getPubIPServices().add(new URI(url));

		NetworkAdapter adapter = new NetworkAdapter();
		adapter.setLocalIP(loopback().getAddress());
		adapter.setInetAddr(loopback());

		assertEquals("203.0.113.7", utils.retrieveAdapterPublicIP(adapter));
		assertEquals("203.0.113.7", adapter.remIP);
		assertEquals(4, adapter.getRemoteIP().length);
	}

	@Test
	public void unreachableProviderIsReportedAsAMessageNotAnException() throws Exception {
		NetworkUtils utils = new NetworkUtils();
		NetworkUtils.getPubIPServices().clear();
		NetworkUtils.getPubIPServices().add(new URI(closedPortUrl()));
		NetworkUtils.setTimeout(500);

		NetworkAdapter adapter = new NetworkAdapter();
		adapter.setLocalIP(loopback().getAddress());
		adapter.setInetAddr(loopback());

		String shown = utils.retrieveAdapterPublicIP(adapter);
		assertNotNull(shown);
		assertEquals(shown, adapter.remIP);
	}

	@Test
	public void adapterWithoutLocalAddressIsLeftAlone() throws Exception {
		NetworkUtils utils = new NetworkUtils();
		NetworkAdapter adapter = new NetworkAdapter();
		assertEquals(null, utils.retrieveAdapterPublicIP(adapter));
	}

	// ------------------------------------------------------------------ adapters and settings

	@Test
	public void adapterListsAreConsistent() throws Exception {
		NetworkUtils utils = new NetworkUtils();
		Vector<NetworkAdapter> all = utils.getAdapters();
		assertNotNull(all);
		assertEquals(all, utils.getAdapters(true));
		Vector<NetworkAdapter> real = utils.getAdapters(false);
		// physical adapters only, or all of them when no physical one exists
		assertTrue(real.size() <= all.size());
		assertTrue(all.isEmpty() || !real.isEmpty());
		for (NetworkAdapter a : real) {
			assertTrue(all.contains(a));
			assertNotNull(a.getNetworkInterface());
		}
		assertTrue(NetworkUtils.getPubIPServices().size() > 0);
	}

	@Test
	public void timeoutIsStoredAsGiven() {
		NetworkUtils.setTimeout(1234);
		assertEquals(1234, NetworkUtils.getTimeout());
	}

	@Test
	public void customRequestConfigKeepsTheBuilderValues() {
		RequestConfig config = NetworkUtils.configureCustomRequest(RequestConfig.custom().setConnectTimeout(777).setSocketTimeout(888));
		assertEquals(777, config.getConnectTimeout());
		assertEquals(888, config.getSocketTimeout());
	}

	@Test
	public void dnsServersAreAlwaysAList() throws Exception {
		List<String> dns = NetworkUtils.getDnsServers();
		assertNotNull(dns);
		for (String server : dns) {
			assertTrue(server, !server.trim().isEmpty());
		}
	}

	@Test
	public void resolvConfIsOnlyReadWhereItExists() throws Exception {
		if (new File("/etc/resolv.conf").exists()) {
			assertNotNull(NetworkUtils.getDnsServersForInterfaceLinux(loopback()));
		} else {
			try {
				NetworkUtils.getDnsServersForInterfaceLinux(loopback());
				fail("missing resolv.conf must be reported");
			} catch (IOException expected) {
				assertNotNull(expected.getMessage());
			}
		}
	}
}
