package dialogs;

import org.junit.Before;
import org.junit.Test;

import gui.commons.dialogutils.JOptionHelperExtended;
import test.util.utils.TestUtils;

public class TestDialogs {

	JOptionHelperExtended helper;
	
	@Before
	public void init() {
		helper = new JOptionHelperExtended(null);
	}
	
	@Test
	public void testErrorDial() {
		while (true) {
			Error e = new Error();
			//		helper.errorScroll("ERROR DETECTED!", "Error DETECTED", e);
			Object res = helper.errorScroll("ERROR DETECTED!", "Error DETECTED", e, new Object[] { "Go", "stay" }, JOptionHelperExtended.YES_OR_NO);
			TestUtils.printl("Dialog closed: Test is exiting. CHOICE: " + res);
		}
	}
}
