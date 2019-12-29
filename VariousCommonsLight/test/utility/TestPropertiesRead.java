package utility;

import org.junit.BeforeClass;
import org.junit.Test;

import gui.JOptionHelperLight;
import testconfig.TestConstants;

public class TestPropertiesRead {
	
	private static final String sourceFilePropertiesPath = TestConstants.sourceFolderPath + "manifest.properties";
	private static PropertiesManagerLight propertiesManager;
	
	@BeforeClass
	public static void init() {
		propertiesManager = new PropertiesManagerLight(sourceFilePropertiesPath);
	}

	@Test
	public void testPropertiesRead() {
		String key = "updatesZippedUrl";
		while(!key.isEmpty()) {
			
			String value = propertiesManager.getProperty(key, "NULL");
			new JOptionHelperLight(null).info("KEY read value is: [" + value + "]", "READ from " + key);
			key = new JOptionHelperLight(null).askForString("Key for value read", "Insert property key");
		}
	}
}
