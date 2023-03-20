package logger;

import org.junit.Test;

import test.util.utils.TestUtils;
import various.common.light.utility.log.SafeLogger;

public class SafeLoggerTest {
	
	@Test
	public void testLoggerOk() {
		
		SafeLogger logger = new SafeLogger(this.getClass());
		
		TestUtils.printHeader("#", 50, "TEST_OK");
		
		logger.error("ERROR_TEST");
		logger.error("ERROR_TEST", new Exception("Test exception"));
		
		logger.warn("WARN_TEST");
		logger.warn("WARN_TEST", new Exception("Test exception"));
		
		logger.info("INFO");
		logger.debug("DEBUG");
		
	}
}
