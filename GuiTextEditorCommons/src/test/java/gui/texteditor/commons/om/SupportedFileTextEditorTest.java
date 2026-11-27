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
package gui.texteditor.commons.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** The table of the file types known by the editor */
public class SupportedFileTextEditorTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void wellKnownNamesWithoutExtension() {
		assertSame(SupportedFileTextEditor.HOSTS, SupportedFileTextEditor.fromKnownFileName(new File("C:/Windows/System32/drivers/etc/hosts")));
		assertSame(SupportedFileTextEditor.HOSTS, SupportedFileTextEditor.fromKnownFileName(new File("HOSTS")));
		assertSame(SupportedFileTextEditor.DOCKER, SupportedFileTextEditor.fromKnownFileName(new File("proj/Dockerfile")));
		assertNull(SupportedFileTextEditor.fromKnownFileName(new File("hosts.txt")));
		assertNull(SupportedFileTextEditor.fromKnownFileName(new File("notes")));
		assertNull(SupportedFileTextEditor.fromKnownFileName(null));
	}

	@Test
	public void supportedMeansExistingWithAKnownExtensionOrName() throws IOException {
		assertTrue(SupportedFileTextEditor.isSupported(tmp.newFile("A.JAVA")));
		assertTrue(SupportedFileTextEditor.isSupported(tmp.newFile("b.json")));
		assertTrue(SupportedFileTextEditor.isSupported(tmp.newFile(".htaccess")));
		assertTrue(SupportedFileTextEditor.isSupported(tmp.newFile("Dockerfile")));
		assertTrue(SupportedFileTextEditor.isSupported(tmp.newFile("hosts")));
		assertFalse(SupportedFileTextEditor.isSupported(tmp.newFile("c.exe")));
		assertFalse(SupportedFileTextEditor.isSupported(tmp.newFile("noextension")));
		assertFalse(SupportedFileTextEditor.isSupported(new File(tmp.getRoot(), "missing.java")));
		assertFalse(SupportedFileTextEditor.isSupported(null));
	}

	@Test
	public void everyTypeHasAnExtensionASyntaxAndAnIcon() {
		for (SupportedFileTextEditor type : SupportedFileTextEditor.values()) {
			assertTrue(type.name(), type.extension.startsWith("."));
			assertTrue(type.name(), type.syntax != null && !type.syntax.isEmpty());
			assertNotNull(type.name(), type.iconPath);
			assertTrue(type.coolDescription().startsWith(type.name()));
			assertTrue(type.coolDescription().endsWith("(" + type.extension + ")"));
		}
	}

	@Test
	public void listsHaveOneEntryPerType() {
		int n = SupportedFileTextEditor.values().length;
		assertEquals(n, SupportedFileTextEditor.getSupportedExtensions().size());
		assertEquals(n, SupportedFileTextEditor.getSupportedExtensionsWithDots().size());
		assertEquals(n, SupportedFileTextEditor.getSupportedSyntaxList().size());
		assertEquals(n, SupportedFileTextEditor.getMultipleFileFilter().length);
		assertEquals(2, SupportedFileTextEditor.getMultipleFileFilter(SupportedFileTextEditor.JAVA, SupportedFileTextEditor.XML).length);
		assertTrue(SupportedFileTextEditor.getSupportedExtensions().contains("java"));
	}

	@Test
	public void relatedFilterDescribesTheType() {
		assertNotNull(SupportedFileTextEditor.JAVA.getRelatedFilter());
		assertEquals("JAVA (.java)", SupportedFileTextEditor.JAVA.coolDescription());
	}
}
