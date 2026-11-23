package start;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

/**
 * The real start of ScreepT: its own main method, in a separate JVM, on a copy of the runtime files in a temporary
 * folder and with a temporary user home. Nothing of the user (configuration, signal file of a running ScreepT, session)
 * is read or written, and the child is always killed, whatever happens. The child ({@link AppStartProbe}) uses the
 * main window like a user: text editor from the menu, a new tab with text, crypter, then the real exit routine.
 * What is checked: the application gets up, the windows and the managers exist, the text typed is the text read back,
 * the exit is clean (exit code 0) and the signal file for the plugins is removed on exit.
 */
public class AppStartTest {

	private static final long CHILD_TIMEOUT_SECONDS = 150;

	/** Runtime files of the application, relative to the module folder: the working directory of a real start. */
	private static final String[] RUNTIME = {
			"Resources_ScreepT", "resourcesFilePathsMappings.properties", "LAUNCHER", "ROOT_DIR_APP_FILES", "UTILS_BUILD",
			"PLUGINS", "UPDATES", "LOG", "EXTERNAL_PLUGINS", "Installed_plugins.properties", "EULA_ScreepT_License.txt",
			"CHANGELOG.md", "README.md", "manifest.properties", "buildNumber.properties" };

	private static void copyTree(final Path from, final Path to) throws IOException {
		Files.walkFileTree(from, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Files.createDirectories(to.resolve(from.relativize(dir).toString()));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, to.resolve(from.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static String tail(File log) {
		try {
			List<String> lines = Files.readAllLines(log.toPath(), StandardCharsets.UTF_8);
			return String.join("\n", lines.subList(Math.max(0, lines.size() - 40), lines.size()));
		} catch (Exception e) {
			return "(no child log: " + e + ")";
		}
	}

	/** The coverage agent of this JVM, if any, so that what the child executes counts too. */
	private static List<String> agentArguments() {
		List<String> out = new ArrayList<>();
		for (String a : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
			if (a.startsWith("-javaagent:") && a.contains("jacoco")) {
				out.add(a);
			}
		}
		return out;
	}

	@Test
	public void applicationStartsWorksAndExitsCleanly() throws Exception {
		File module = new File(System.getProperty("user.dir"));
		File base = new File(module, "target/appstart");
		deleteTree(base);
		File work = new File(base, "work");
		File home = new File(base, "home");
		assertTrue(work.mkdirs() && home.mkdirs());

		for (String name : RUNTIME) {
			Path src = new File(module, name).toPath();
			if (Files.isDirectory(src)) {
				copyTree(src, new File(work, name).toPath());
			} else if (Files.isRegularFile(src)) {
				Files.copy(src, new File(work, name).toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		}
		// no network from a test: the web addresses of ScreepT (update and plugin manifests) point to a local server that
		// answers 404 and notes what was asked, so the "server unreachable / nothing to download" paths run for real
		final List<String> requested = new CopyOnWriteArrayList<>();
		HttpServer fakeWeb = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		fakeWeb.createContext("/", ex -> {
			requested.add(ex.getRequestURI().getPath());
			ex.sendResponseHeaders(404, -1);
			ex.close();
		});
		fakeWeb.start();
		File mapping = new File(work, "resourcesFilePathsMappings.properties");
		String mappingText = new String(Files.readAllBytes(mapping.toPath()), StandardCharsets.ISO_8859_1);
		assertTrue("the mapping file points to the real web site", mappingText.contains("www.am-design-development.com"));
		Files.write(mapping.toPath(), mappingText.replace("https\\://www.am-design-development.com", "http\\://127.0.0.1:" + fakeWeb.getAddress().getPort())
				.getBytes(StandardCharsets.ISO_8859_1));
		// the license is accepted in the copy, a real start would stop at the EULA window
		File license = new File(work, "Resources_ScreepT/license.properties");
		Files.write(license.toPath(), "LICENSE_ACCEPTED=true\n".getBytes(StandardCharsets.UTF_8));

		File report = new File(base, "report.properties");
		File childLog = new File(base, "child.log");

		List<String> cmd = new ArrayList<>();
		cmd.add(new File(System.getProperty("java.home"), "bin/java").getPath());
		cmd.addAll(agentArguments());
		cmd.addAll(Arrays.asList("-Xmx768m", "-Dfile.encoding=UTF-8", "-Duser.home=" + home.getAbsolutePath(),
				"-Dscreept.nativeFileDialog=false", "-Dtest.autodialog.pollMs=40", "-Dtest.autodialog.graceMs=80", "-Djava.util.prefs.PreferencesFactory=test.util.gui.MemoryPreferences",
				"-Djava.net.useSystemProxies=false", "-cp", System.getProperty("java.class.path"), AppStartProbe.class.getName(),
				report.getAbsolutePath()));

		ProcessBuilder pb = new ProcessBuilder(cmd).directory(work).redirectErrorStream(true).redirectOutput(childLog);
		long begin = System.currentTimeMillis();
		Process child = pb.start();
		int exit;
		try {
			if (!child.waitFor(CHILD_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
				throw new AssertionError("ScreepT did not finish within " + CHILD_TIMEOUT_SECONDS + "s. Report: " + readReport(report)
						+ "\n--- child log (tail) ---\n" + tail(childLog));
			}
			exit = child.exitValue();
		} finally {
			if (child.isAlive()) {
				child.destroyForcibly();
				child.waitFor(20, TimeUnit.SECONDS);
			}
			fakeWeb.stop(0);
		}
		Properties r = readReport(report);
		Properties shown = new Properties();
		shown.putAll(r);
		shown.remove("tourDialogs");
		String diagnostics = "report=" + shown + "\n--- child log (tail) ---\n" + tail(childLog);
		System.out.println("ScreepT start test: child finished in " + (System.currentTimeMillis() - begin) + " ms, startMillis=" + r.getProperty("startMillis"));

		assertFalse("the application reported an error. " + diagnostics, r.containsKey("error"));
		assertEquals(diagnostics, "true", r.getProperty("mainAppRunning"));
		assertEquals(diagnostics, "true", r.getProperty("configLoaded"));
		assertEquals(diagnostics, "true", r.getProperty("updatesManager"));
		assertEquals(diagnostics, "true", r.getProperty("pluginManager"));
		assertTrue("the main window has its menu bar. " + diagnostics, Integer.parseInt(r.getProperty("menuCount", "0")) >= 3);
		assertTrue(diagnostics, Integer.parseInt(r.getProperty("frameWidth", "0")) > 400);

		// isolation: the child lived in the temporary home, never in the one of the user
		assertTrue(diagnostics, new File(r.getProperty("userHome", "")).equals(home));
		assertTrue("signal file for the plugins is written at start. " + diagnostics, "true".equals(r.getProperty("signalFileWhileRunning")));
		assertTrue(diagnostics, r.getProperty("signalFile", "").startsWith(home.getAbsolutePath()));

		// the user path: text editor from the menu, a new tab, the text typed is the text read back
		assertEquals(diagnostics, "true", r.getProperty("textEditorShowing"));
		assertEquals(diagnostics, "true", r.getProperty("newTabCreated"));
		assertEquals(diagnostics, "alpha|beta|gamma", r.getProperty("typedText"));
		assertTrue(diagnostics, Integer.parseInt(r.getProperty("tabCount", "0")) >= 1);
		assertEquals(diagnostics, "true", r.getProperty("crypterShowing"));

		// the tour of the whole menu bar: every entry answers, none hangs, none throws, none ends in the error dialogs of the application
		assertTrue("menu entries clicked. " + diagnostics, Integer.parseInt(r.getProperty("tourClicked", "0")) >= 40);
		assertEquals("entries that never came back. " + diagnostics, "[]", r.getProperty("tourStuck"));
		assertEquals("entries that threw. " + diagnostics, "", r.getProperty("tourFailed", ""));
		String dialogs = r.getProperty("tourDialogs", "");
		assertFalse("an error dialog of the application appeared during the tour: " + dialogs,
				dialogs.contains("An Exception Occurred") || dialogs.contains("Critical Error"));

		// the application asked its own web space (and nothing else) for updates, and survived the 404
		assertTrue("update manifest requested. requests=" + requested + "\n" + diagnostics, requested.contains("/ScreepT/Updates/manifest.properties"));
		for (String path : requested) {
			assertTrue("only ScreepT web space is asked: " + path, path.startsWith("/ScreepT/"));
		}

		// exit: clean code, signal file removed (the plugins stop on it)
		assertEquals(diagnostics, "closing", r.getProperty("step"));
		assertEquals(diagnostics, 0, exit);
		assertFalse("signal file removed on exit. " + diagnostics, new File(r.getProperty("signalFile")).exists());
	}

	private static Properties readReport(File report) {
		Properties p = new Properties();
		try (InputStream in = Files.newInputStream(report.toPath())) {
			p.load(in);
		} catch (Exception e) {
			p.setProperty("reportUnreadable", String.valueOf(e));
		}
		return p;
	}

	private static void deleteTree(File dir) throws IOException {
		if (!dir.exists()) {
			return;
		}
		Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.deleteIfExists(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException {
				Files.deleteIfExists(d);
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
