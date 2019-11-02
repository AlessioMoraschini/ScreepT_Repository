import org.junit.Before;
import org.junit.Test;

import resources.PropertiesManager;

public class TestProps {

	public static final String testProps = "TestFiles/resourcesFilePathsMappings.properties";
	public PropertiesManager manager;
	
	@Before
	public void init() {
		manager = new PropertiesManager(testProps);
	}
	
	@Test
	public void TestPropertiesApacheSave() {
		manager.savePropertiesLayoutApache("MAX_N_OF_ERR_DIALOG_OPENED", "5");
	}
}
