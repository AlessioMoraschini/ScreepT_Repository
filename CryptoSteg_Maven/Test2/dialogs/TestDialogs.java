package dialogs;

import org.junit.Before;
import org.junit.Test;

import dialogutils.JOptionHelper;
import testUtils.TestUtils;

public class TestDialogs {

	JOptionHelper helper;
	
	@Before
	public void init() {
		helper = new JOptionHelper(null);
	}
	
	@Test
	public void testErrorDial() {
		Error e = new Error();
		helper.errorScroll("ERROR DETECTED!", "Error DETECTED", e);
		
		TestUtils.print("Dialog closed: Test is exiting");
	}
}
