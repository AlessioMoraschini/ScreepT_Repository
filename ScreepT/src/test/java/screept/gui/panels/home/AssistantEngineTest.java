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
package screept.gui.panels.home;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import resources.GeneralConfig;
import screept.gui.panels.home.AssistantEngine.Reply;

/** The home page assistant: built-in commands and answers taken from the guide and the changelog that ship with the application. */
public class AssistantEngineTest {

	private static final File GUIDE = new File("ROOT_DIR_APP_FILES/Readme/ReadMe.html");
	private static String savedGuidePath;
	private static HomeTerminalSession session;

	@BeforeClass
	public static void useTheGuideOfTheSources() {
		Assume.assumeTrue(GUIDE.isFile() && new File("CHANGELOG.md").isFile());
		savedGuidePath = GeneralConfig.SCREEPT_TUTORIAL_HTML_PATH;
		GeneralConfig.SCREEPT_TUTORIAL_HTML_PATH = GUIDE.getPath();
		session = HomeTerminalSession.get();
	}

	@AfterClass
	public static void restore() {
		if (GUIDE.isFile()) {
			GeneralConfig.SCREEPT_TUTORIAL_HTML_PATH = savedGuidePath;
		}
	}

	private static String text(Reply r) {
		StringBuilder sb = new StringBuilder();
		for (Reply.Entry e : r.entries) {
			sb.append(e.kind).append(": ").append(e.text).append('\n');
		}
		return sb.toString();
	}

	private static Reply ask(String q) {
		return AssistantEngine.reply(q, session);
	}

	@Test
	public void builtInCommandsAnswerInEnglishAndItalian() {
		assertTrue(text(ask("help")).contains("What can I do?"));
		assertEquals(text(ask("help")), text(ask("Aiuto!")));
		assertTrue(ask("clear").clear && ask("cls").clear && ask("pulisci").clear);
		assertTrue(text(ask("about")).contains("ScreepT assistant"));
		assertEquals(text(ask("about")), text(ask("chi sei")));
		assertTrue(text(ask("whoami")).contains("excellent taste"));
		assertTrue(text(ask("sudo anything")).contains("sudoers"));
		assertTrue(text(ask("rm -rf /")).contains("never delete"));
		assertTrue(text(ask("quit")).contains("no escape"));
		assertTrue(text(ask("Thanks")).contains("Anytime"));
		assertFalse(text(ask("ciao")).isEmpty());
		assertFalse(text(ask("date")).isEmpty());
		assertTrue(text(ask("version")).startsWith("OUTPUT: "));
	}

	@Test
	public void changelogIsSummarisedByNews() {
		String news = text(ask("news"));
		assertTrue(news, news.contains("Latest version: "));
		assertTrue(news, news.contains("- New:") || news.contains("- Improved:") || news.contains("- Fixed:"));
		assertEquals(news, text(ask("what's new")));
		assertEquals(news, text(ask("novità")));
	}

	@Test
	public void shortcutsAreListedAndFiltered() {
		String all = text(ask("shortcuts"));
		assertTrue(all, all.contains("Keyboard shortcuts") && all.contains("EXIT APPLICATION"));
		String save = text(ask("shortcuts save"));
		assertTrue(save, save.contains("Shortcuts for \"save\"") && save.contains("SAVE FILE"));
		assertFalse(save, save.contains("EXIT APPLICATION"));
		assertTrue(text(ask("scorciatoie save")).contains("SAVE FILE"));
	}

	@Test
	public void guideTopicsAndEasterEggs() {
		String topics = text(ask("topics"));
		assertTrue(topics, topics.contains("Sections of the guide") && topics.contains("Easter eggs"));
		assertEquals(topics, text(ask("indice")));
		String eggs = text(ask("eggs"));
		assertTrue(eggs, eggs.contains("snake") && eggs.contains("puppynoid"));
	}

	@Test
	public void eggsStartTheirGameWhenTheHomePageOffersIt() {
		for (String egg : new String[] { "snake", "puppynoid", "cats", "poem", "matrix", "bugs", "cipher", "typing", "trivia", "fortune", "glitch" }) {
			Reply r = ask(egg);
			assertEquals(egg, 1, r.entries.size());
			// no home page here: the assistant says so instead of failing; with one it would start the game
			assertTrue(egg + ": " + text(r), r.uiAction != null || text(r).contains("not available"));
		}
	}

	@Test
	public void questionsAreAnsweredFromTheGuide() {
		Reply r = ask("how do I encrypt a file");
		String t = text(r);
		assertTrue(t, t.contains("SYSTEM:") && t.contains("TITLE:") && t.contains("SOURCE:"));
		assertTrue(t, t.toLowerCase().contains("encrypt") || t.toLowerCase().contains("crypter"));
		// the same question in Italian reaches the same section
		String it = text(ask("come si cifra un file"));
		assertTrue(it, it.contains("TITLE:"));
		assertTrue(text(ask("where is the crypter")).contains("TITLE:"));
	}

	@Test
	public void nonsenseIsNotUnderstood() {
		String t = text(ask("qwertyuiop zxcvbnm"));
		assertFalse(t, t.contains("Pulling it from the manual"));
		assertTrue(t, t.length() > 10);
	}

	@Test
	public void knowledgeSearchFindsSectionsAndTheLatestVersion() {
		AssistantKnowledge k = AssistantKnowledge.get();
		k.ensureLoaded();
		AssistantKnowledge.Answer a = k.search("log search big files");
		assertNotNull(a);
		assertTrue(a.found);
		assertFalse(a.lines.isEmpty());
		assertFalse(k.search("qwertyuiop zxcvbnm").found);
		List<?> topics = k.topics();
		assertTrue(topics.size() > 5);
		assertFalse(k.shortcuts("save", 3).isEmpty());
		assertTrue(k.shortcuts("zzzzzzzz", 3).isEmpty());
		assertEquals("zzzzzzzzz", AssistantKnowledge.englishFor("zzzzzzzzz"));
		assertFalse(AssistantKnowledge.stems("encrypting the files").isEmpty());
		assertTrue(AssistantKnowledge.isStopWord("the"));
		assertEquals("novita", AssistantKnowledge.normalize("Novità"));
		assertFalse(k.latestVersion().isEmpty());
		assertFalse(k.latestChanges().isEmpty());
		assertNotNull(k.latestHighlight());
	}
}
