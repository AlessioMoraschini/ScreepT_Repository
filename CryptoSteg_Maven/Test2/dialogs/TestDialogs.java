package dialogs;

import org.junit.Before;
import org.junit.Test;

import dialogutils.JOptionHelper;
import utils.TestUtils;

public class TestDialogs {

	JOptionHelper helper;
	
	@Before
	public void init() {
		helper = new JOptionHelper(null);
	}
	
	@Test
	public void testErrorDial() {
		while (true) {
			Error e = new Error();
			//		helper.errorScroll("ERROR DETECTED!", "Error DETECTED", e);
			Object res = helper.errorScroll("ERROR DETECTED!", "Error DETECTED", e, new Object[] { "Go", "stay" }, JOptionHelper.YES_OR_NO);
			TestUtils.printl("Dialog closed: Test is exiting. CHOICE: " + res);
		}
	}
}
