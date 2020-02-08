package gui;

import java.awt.Color;

import org.junit.Test;

import gui.selection.ColorDialogHelper;
import utils.TestUtils;
import gui.selection.ColorChooserGrid.UserChoice;

public class ColorChooserTest {

	@Test
	public void testGridChooser() {
		GUIStyelSettings.setDarkNimbus(1);
		UserChoice userChoice = ColorDialogHelper.askForColorGrid(Color.YELLOW);
		
		TestUtils.printl("Selection: " + userChoice.name() + " : selection color:" + userChoice.getChosenColor());
	}
}
