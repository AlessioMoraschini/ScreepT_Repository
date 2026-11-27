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
package screept.launcher.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import os.commons.utils.SysUtils;

/** Linux desktop integration: only text and temporary folders, nothing of the user is touched */
public class DesktopIntegrationTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final String SETTINGS = "{\n \"launcher-animation-effect\": {\"type\": \"combobox\", \"value\": 3},\n"
			+ " \"pinned-apps\": {\n  \"type\": \"generic\",\n  \"default\": [\n   \"nemo.desktop\"\n  ],\n  \"value\": [\n   \"firefox.desktop\"\n  ]\n }\n}\n";

	@Test
	public void theEntryStartsTheScriptEvenWithSpacesAndSpecialCharacters() {
		String entry = DesktopIntegration.desktopEntry("/home/a b/ScreepT/ScreepT.sh", "/home/a b/icon.png");
		assertTrue(entry.contains("\nExec=\"/home/a b/ScreepT/ScreepT.sh\" %F\n"));
		assertTrue(entry.contains("\nPath=/home/a b/ScreepT\n"));
		assertTrue(entry.contains("MimeType=text/plain;"));
		assertEquals("\"/x/\\\\$y 100%%.sh\"", DesktopIntegration.execArgument("/x/$y 100%.sh"));
	}

	@Test
	public void pinnedOnceInTheListOfTheValueOnly() {
		String changed = DesktopIntegration.addPinned(SETTINGS, "screept.desktop");
		assertTrue(changed.contains("\"firefox.desktop\", \"screept.desktop\""));
		assertTrue("the default list stays", changed.contains("\"nemo.desktop\"\n  ],"));
		assertEquals(changed, DesktopIntegration.addPinned(changed, "screept.desktop"));
		assertTrue(DesktopIntegration.addPinned("{\"pinned-apps\": {\"value\": []}}", "screept.desktop").contains("[\"screept.desktop\"]"));
		assertNull(DesktopIntegration.addPinned("{\"other\": {\"value\": []}}", "screept.desktop"));
		assertNull("no value of its own: the next object is not touched",
				DesktopIntegration.addPinned("{\"pinned-apps\": {\"default\": []}, \"x\": {\"value\": []}}", "screept.desktop"));
	}

	@Test
	public void cinnamonSettingsAreChangedWithABackup() throws Exception {
		File folder = tmp.newFolder(".config", "cinnamon", "spices", "grouped-window-list@cinnamon.org");
		File settings = new File(folder, "2.json");
		Files.write(settings.toPath(), SETTINGS.getBytes(StandardCharsets.UTF_8));
		DesktopIntegration.pinToCinnamon(tmp.getRoot());
		assertTrue(new String(Files.readAllBytes(settings.toPath()), StandardCharsets.UTF_8).contains("screept.desktop"));
		assertEquals(SETTINGS, new String(Files.readAllBytes(new File(folder, "2.json.before-screept").toPath()), StandardCharsets.UTF_8));
	}

	@Test
	public void gnomeFavorites() {
		assertEquals("['a.desktop', 'screept.desktop']", DesktopIntegration.addFavorite("['a.desktop']\n", "screept.desktop"));
		assertEquals("['screept.desktop']", DesktopIntegration.addFavorite("@as []", "screept.desktop"));
		assertEquals("['screept.desktop']", DesktopIntegration.addFavorite("['screept.desktop']", "screept.desktop"));
		assertNull(DesktopIntegration.addFavorite(null, "screept.desktop"));
		assertNull(DesktopIntegration.addFavorite("No such schema", "screept.desktop"));
	}

	@Test
	public void fileNamesCannotRunCommandsFromTheStartScript() {
		assertEquals("'/tmp/$(rm -rf x) `y`'", SysUtils.quoteFileArgument("/tmp/$(rm -rf x) `y`", false));
		assertEquals("'/tmp/it'\\''s'", SysUtils.quoteFileArgument("/tmp/it's", false));
		assertEquals("\"C:\\a b\"", SysUtils.quoteFileArgument("C:\\a b", true));
	}

	@Test
	public void theLinuxPipelineIsChosenOnlyWhenTheUserDidNot() {
		assertEquals(" -Dsun.java2d.xrender=false", CommandsAssembler.linuxGraphicsArgs(true, "-Xfoo"));
		assertEquals("", CommandsAssembler.linuxGraphicsArgs(false, "-Xfoo"));
		assertEquals("", CommandsAssembler.linuxGraphicsArgs(true, "-Dsun.java2d.opengl=true"));
		assertEquals("", CommandsAssembler.linuxGraphicsArgs(true, "-Dsun.java2d.xrender=true"));
	}
}
