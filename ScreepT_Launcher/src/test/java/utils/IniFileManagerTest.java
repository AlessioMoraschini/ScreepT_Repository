package utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import screept.launcher.utils.IniFileManager;

/**
 * The launcher configuration decides how ScreepT is started (memory, JVM range, console, hidden launcher). What it reads
 * from the ini, what it migrates once, and what it must never do twice or break on.
 */
public class IniFileManagerTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private String savedMin;
	private String savedMax;
	private String savedFull;
	private boolean savedOnce;

	@Before
	public void remember() {
		savedMin = IniFileManager.MIN_JRE_VERSION;
		savedMax = IniFileManager.MAX_JRE_VERSION;
		savedFull = IniFileManager.MAX_JRE_VERSION_FULL_COMPATIBILITY;
		savedOnce = IniFileManager.showLauncherOnce;
		IniFileManager.showLauncherOnce = false;
	}

	@After
	public void restore() {
		IniFileManager.MIN_JRE_VERSION = savedMin;
		IniFileManager.MAX_JRE_VERSION = savedMax;
		IniFileManager.MAX_JRE_VERSION_FULL_COMPATIBILITY = savedFull;
		IniFileManager.showLauncherOnce = savedOnce;
	}

	private File ini(String content) throws Exception {
		File f = new File(tmp.getRoot(), "Launcher_Configuration.ini");
		Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
		return f;
	}

	private static String text(File f) throws Exception {
		return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
	}

	@Test
	public void valuesOfTheIniAreRead() throws Exception {
		File f = ini("[JVM_OPTIONS]\nminJvmVersion=11\nmaxJvmVersion=30\npreferredJvm=17\nmanualPathJvm= C:/jdk \ngcArgs=-XX:+UseZGC\n"
				+ "[APPLICATION_OPTIONS]\nscaleGuiPerc=125,5\nviewCmdConsole=true\njarToLaunch=ScreepT_0.9.91.jar\nautoShowInstallOpt=false\n"
				+ "flagHideNStartDirect=true\nlogLevelSetting=DEBUG\n");
		IniFileManager m = new IniFileManager(f.getAbsolutePath());
		m.initFromINIFile();
		assertEquals("11", IniFileManager.MIN_JRE_VERSION);
		assertEquals("a maximum above what ScreepT supports is kept", "30", IniFileManager.MAX_JRE_VERSION);
		assertEquals("17", m.preferredJvm);
		assertEquals("C:/jdk", m.manualPathJvm);
		assertEquals("-XX:+UseZGC", m.JVM_GC_OPTIONS);
		assertEquals("a decimal comma becomes a point", "125.5", m.GUI_SCALE_ARG_VALUE);
		assertTrue(m.viewShellAfterStart);
		assertEquals("ScreepT_0.9.91.jar", m.MAIN_JAR_FILE);
		assertFalse(m.showInstallOptions);
		assertTrue(m.hideNStartDirect);
		assertTrue("hidden at start", m.isHiddenForThisStart());
		assertEquals(org.apache.log4j.Level.DEBUG, m.logLevel);
	}

	@Test
	public void aMaximumBelowTheSupportedOneIsRaised() throws Exception {
		File f = ini("[JVM_OPTIONS]\nmaxJvmVersion=17\nmaxJvmVersionFullCompatibility=9\n[APPLICATION_OPTIONS]\nscaleGuiPerc=100\n");
		new IniFileManager(f.getAbsolutePath()).initFromINIFile();
		assertEquals(IniFileManager.SUPPORTED_MAX_JRE_VERSION, IniFileManager.MAX_JRE_VERSION);
		assertEquals(IniFileManager.SUPPORTED_MAX_JRE_VERSION, IniFileManager.MAX_JRE_VERSION_FULL_COMPATIBILITY);
	}

	@Test
	public void missingKeysKeepTheDefaultsAndNeverStopTheLauncher() throws Exception {
		File f = ini("[JVM_OPTIONS]\n[APPLICATION_OPTIONS]\n");
		IniFileManager m = new IniFileManager(f.getAbsolutePath());
		m.initFromINIFile();
		assertEquals("1", m.GUI_SCALE_ARG_VALUE);
		assertEquals("MainAppToLaunch.jar", m.MAIN_JAR_FILE);
		assertTrue(m.showInstallOptions);
		assertFalse(m.hideNStartDirect);
		assertFalse(m.viewShellAfterStart);
		assertFalse(m.isHiddenForThisStart());
	}

	@Test
	public void unreadableIniKeepsTheDefaults() {
		IniFileManager m = new IniFileManager(new File(tmp.getRoot(), "absent.ini").getAbsolutePath());
		m.initFromINIFile();
		assertEquals(org.apache.log4j.Level.INFO, m.logLevel);
		assertTrue(m.JVM_MEMORY_OPTIONS.contains("-Xmx" + IniFileManager.DEFAULT_MAX_MEMORY_MB + "m"));
	}

	@Test
	public void memoryAndStackAreBroughtToTheDefaultsOnceAndThenLeftToTheUser() throws Exception {
		File f = ini("[JVM_OPTIONS]\nmemoryArgs=-Xmx64g -Xms4g\nstackSizeArgs=10\n[APPLICATION_OPTIONS]\nscaleGuiPerc=100\n");
		IniFileManager first = new IniFileManager(f.getAbsolutePath());
		first.initFromINIFile();
		assertTrue(first.JVM_MEMORY_OPTIONS, first.JVM_MEMORY_OPTIONS.matches("-Xmx\\d+m -Xms128m"));
		long max = Long.parseLong(first.JVM_MEMORY_OPTIONS.replaceAll("^-Xmx(\\d+)m.*$", "$1"));
		assertTrue("never more than the default maximum: " + max, max <= IniFileManager.DEFAULT_MAX_MEMORY_MB);
		assertEquals(" -Xss6m", first.JVM_STACK_SIZE);
		String saved = text(f);
		assertTrue(saved, saved.contains("memoryDefaultMigrated"));
		assertTrue(saved, saved.contains("jvmDefaultsMigrated"));

		// the user raises the memory afterwards: it is respected from now on
		assertTrue(first.writeValueToIniConfiguration(IniFileManager.INI_JVM_OPT, "memoryArgs", "-Xmx4096m -Xms512m"));
		assertTrue(first.writeValueToIniConfiguration(IniFileManager.INI_JVM_OPT, "stackSizeArgs", "12"));
		IniFileManager second = new IniFileManager(f.getAbsolutePath());
		second.initFromINIFile();
		assertEquals("-Xmx4096m -Xms512m", second.JVM_MEMORY_OPTIONS);
		assertEquals(" -Xss12m", second.JVM_STACK_SIZE);
	}

	@Test
	public void showLauncherOnceIsConsumedByTheFirstRead() throws Exception {
		File f = ini("[JVM_OPTIONS]\n[APPLICATION_OPTIONS]\nflagHideNStartDirect=true\nflagShowLauncherOnce=true\n");
		IniFileManager m = new IniFileManager(f.getAbsolutePath());
		m.initFromINIFile();
		assertTrue(IniFileManager.showLauncherOnce);
		assertFalse("hidden launcher is shown this once", m.isHiddenForThisStart());
		assertTrue("and the flag is cleared in the file for the next start", text(f).replaceAll("\\s", "").contains("flagShowLauncherOnce=false"));
	}

	@Test
	public void valuesCanBeWrittenAndReadBack() throws Exception {
		File f = ini("[JVM_OPTIONS]\n[APPLICATION_OPTIONS]\nscaleGuiPerc=100\n");
		IniFileManager m = new IniFileManager(f.getAbsolutePath());
		m.initFromINIFile();
		assertTrue(m.writeValueToIniConfiguration(IniFileManager.INI_APP_OPT, "jarToLaunch", "New.jar"));
		IniFileManager again = new IniFileManager(f.getAbsolutePath());
		again.initFromINIFile();
		assertEquals("New.jar", again.MAIN_JAR_FILE);
	}
}
