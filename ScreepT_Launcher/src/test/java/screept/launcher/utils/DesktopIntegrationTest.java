/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.launcher.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
	public void removingIsTheInverseOfPinning() {
		String pinned = DesktopIntegration.addPinned(SETTINGS, "screept.desktop");
		assertEquals(SETTINGS, DesktopIntegration.removePinned(pinned, "screept.desktop"));
		assertEquals(SETTINGS, DesktopIntegration.removePinned(SETTINGS, "screept.desktop"));
		assertEquals("{\"pinned-apps\": {\"value\": []}}",
				DesktopIntegration.removePinned("{\"pinned-apps\": {\"value\": [\"screept.desktop\"]}}", "screept.desktop"));
		assertEquals("{\"pinned-apps\": {\"value\": [\"b\"]}}",
				DesktopIntegration.removePinned("{\"pinned-apps\": {\"value\": [\"screept.desktop\", \"b\"]}}", "screept.desktop"));
		assertNull(DesktopIntegration.removePinned("{}", "screept.desktop"));

		assertEquals("['a.desktop', 'b.desktop']",
				DesktopIntegration.removeFavorite("['a.desktop', 'screept.desktop', 'b.desktop']\n", "screept.desktop"));
		assertEquals("[]", DesktopIntegration.removeFavorite("['screept.desktop']", "screept.desktop"));
		assertEquals("@as []", DesktopIntegration.removeFavorite("@as []\n", "screept.desktop"));
		assertNull(DesktopIntegration.removeFavorite("No such schema", "screept.desktop"));
	}

	@Test
	public void cinnamonPinIsReadAndRemoved() throws Exception {
		File folder = tmp.newFolder(".config", "cinnamon", "spices", "grouped-window-list@cinnamon.org");
		Files.write(new File(folder, "2.json").toPath(), SETTINGS.getBytes(StandardCharsets.UTF_8));
		assertFalse(DesktopIntegration.isPinnedInCinnamon(tmp.getRoot()));
		DesktopIntegration.pinToCinnamon(tmp.getRoot());
		assertTrue(DesktopIntegration.isPinnedInCinnamon(tmp.getRoot()));
		DesktopIntegration.unpinFromCinnamon(tmp.getRoot());
		assertFalse(DesktopIntegration.isPinnedInCinnamon(tmp.getRoot()));
		assertEquals(SETTINGS, new String(Files.readAllBytes(new File(folder, "2.json").toPath()), StandardCharsets.UTF_8));
	}

	@Test
	public void onlyTheEntriesOfThisInstallationAreActive() throws Exception {
		File entry = tmp.newFile("screept.desktop");
		Files.write(entry.toPath(), DesktopIntegration.desktopEntry("/opt/a/ScreepT.sh", "/i.png").getBytes(StandardCharsets.UTF_8));
		assertTrue(DesktopIntegration.startsWith(entry, "Exec=\"/opt/a/ScreepT.sh\" %F"));
		assertFalse(DesktopIntegration.startsWith(entry, "Exec=\"/opt/b/ScreepT.sh\" %F"));
		assertFalse(DesktopIntegration.startsWith(new File(tmp.getRoot(), "missing"), "Exec=\"/opt/a/ScreepT.sh\" %F"));

		File actions = tmp.newFolder("nemo", "actions");
		Files.write(new File(actions, "screept.nemo_action").toPath(),
				DesktopIntegration.nemoAction("/opt/a b/ScreepT.sh").getBytes(StandardCharsets.UTF_8));
		assertTrue(DesktopIntegration.isOpenWithActive(tmp.getRoot(), "/opt/a b/ScreepT.sh"));
		assertFalse(DesktopIntegration.isOpenWithActive(tmp.getRoot(), "/opt/b/ScreepT.sh"));

		String plain = DesktopIntegration.withoutFileTypes(DesktopIntegration.desktopEntry("/opt/a/ScreepT.sh", "/i.png"));
		assertFalse(plain.contains("MimeType"));
		assertTrue(plain.contains("StartupWMClass="));
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
