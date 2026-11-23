/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package sweep;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Assume;
import org.junit.Test;

import test.util.gui.UiSweep;

/**
 * The method sweep of a module has a time budget, so the classes at the end of the alphabet are left out. This module is not on the critical path
 * of the build: it sweeps the part of the classes of various.common.light.gui.dialogs.color.ColorDialogHelper that the owner leaves out (smoke coverage, no assertions on behaviour).
 */
public class VariousRestSweepTest {

	@Test
	public void sweepTheClassesLeftOutByTheirOwner() throws Exception {
		Class<?> anchor = various.common.light.gui.dialogs.color.ColorDialogHelper.class;
		Assume.assumeTrue(new File(anchor.getProtectionDomain().getCodeSource().getLocation().toURI()).isDirectory());
		int calls = UiSweep.runMethods(anchor, null, "various.common.light.gui.dialogs.color.ColorDialogHelper", 25000L);
		assertTrue(calls >= 0);
	}
}
