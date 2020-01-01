package JUnit;

import org.junit.Test;

import launcherutils.JvmHelper;
import utils.TestUtils;

public class CompareVersionTest {

	private static final String versionA = "jdk1.8.0_162";
	private static final String versionB = "jdk-8.0.232.09-hotspot";
	private static final String versionC = "asd1.7.0.232.09-hotspot";
	
	private static final String MAX_JVM = "12.0.0";
	private static final String MIN_JVM = "1.8.0";
	
	@Test
	public void testComparation() {
		
		JvmHelper jvmHelper = new JvmHelper(MAX_JVM, MIN_JVM);
		boolean checkedA = jvmHelper.checkSingleJavaVersionOK(versionA, MAX_JVM, MIN_JVM);
		boolean checkedB = jvmHelper.checkSingleJavaVersionOK(versionB, MAX_JVM, MIN_JVM);
		boolean checkedC = jvmHelper.checkSingleJavaVersionOK(versionC, MAX_JVM, MIN_JVM);
		
		TestUtils.printl("Check A result: " + checkedA);
		TestUtils.printl("Check B result: " + checkedB);
		TestUtils.printl("Check C result: " + checkedC);
	}
}
