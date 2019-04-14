package JUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import dependencycheck.LibChecker;

public class LibCheckerTest {
	
	boolean loggerEnabled = true;
	
	@Test
	@Ignore
	public void TestClass() {
		LibChecker.setLoggerEnabled(loggerEnabled);
		
		assertFalse(LibChecker.checkClassInstantiation("fakeClass", null));
		assertTrue(LibChecker.checkClassExisting("org.junit.Test"));
	}
	
	@Test
	public void TestClassInstantioation() {
		LibChecker.setLoggerEnabled(loggerEnabled);
		String[] args = {""};
		assertTrue(LibChecker.checkClassInstantiation("java.lang.String", args));
//		assertTrue(LibChecker.checkClassInstantiation("org.junit.Assert", new Boolean(true)));
	}
	
	@Test
	@Ignore
	public void TestMethod() {
		LibChecker.setLoggerEnabled(loggerEnabled);
		assertTrue(LibChecker.checkMethod("java.lang.String", "equals", Object.class));
	}
}
