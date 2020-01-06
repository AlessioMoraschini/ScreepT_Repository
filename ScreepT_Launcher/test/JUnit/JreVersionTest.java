package JUnit;

import org.junit.Test;

import utils.TestUtils;

public class JreVersionTest {

	@Test
	public void testVersion() {
		TestUtils.print(System.getProperty("java.home"));
	}
}
