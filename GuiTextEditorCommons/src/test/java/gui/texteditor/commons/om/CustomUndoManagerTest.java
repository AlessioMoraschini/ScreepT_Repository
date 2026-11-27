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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.junit.Test;

/** Hooks around undo/redo (a text area is not a window: nothing is shown) and the editor history entries */
public class CustomUndoManagerTest {

	@Test
	public void hooksRunAroundUndoAndRedoInOrder() {
		RSyntaxTextArea area = new RSyntaxTextArea();
		final List<String> events = new ArrayList<String>();
		CustomUndoManager manager = new CustomUndoManager(area, () -> events.add("afterRedo"), () -> events.add("afterUndo"));
		manager.setBeforeUndo(() -> events.add("beforeUndo"));
		manager.setBeforeRedo(() -> events.add("beforeRedo"));
		area.getDocument().addUndoableEditListener(manager);

		area.append("hello");
		assertEquals("hello", area.getText());
		manager.undo();
		assertEquals("", area.getText());
		manager.redo();
		assertEquals("hello", area.getText());
		assertEquals(java.util.Arrays.asList("beforeUndo", "afterUndo", "beforeRedo", "afterRedo"), events);
	}

	@Test
	public void commonActionsApplyToBothDirections() {
		RSyntaxTextArea area = new RSyntaxTextArea();
		final int[] calls = { 0, 0 };
		CustomUndoManager manager = new CustomUndoManager(area, null, null);
		manager.setCommonBeforeAction(() -> calls[0]++);
		manager.setCommonAfterAction(() -> calls[1]++);
		area.getDocument().addUndoableEditListener(manager);
		area.append("x");
		manager.undo();
		manager.redo();
		assertEquals(2, calls[0]);
		assertEquals(2, calls[1]);
	}

	@Test
	public void nullHooksBecomeNoOps() {
		RSyntaxTextArea area = new RSyntaxTextArea();
		CustomUndoManager manager = new CustomUndoManager(area, null, null);
		manager.setAfterRedo(null);
		manager.setAfterUndo(null);
		manager.setBeforeRedo(null);
		manager.setBeforeUndo(null);
		assertNotNull(manager.getAfterRedo());
		assertNotNull(manager.getAfterUndo());
		assertNotNull(manager.getBeforeRedo());
		assertNotNull(manager.getBeforeUndo());
		manager.getAfterRedo().run();
		manager.getBeforeUndo().run();
		area.getDocument().addUndoableEditListener(manager);
		area.append("x");
		manager.undo();
		manager.redo();
		assertEquals("x", area.getText());
	}

	@Test
	public void historyNodeEqualsNeedsTheSameCaretAndFileOrPane() {
		File file = new File("a.txt");
		RTextScrollPane pane = new RTextScrollPane(new RSyntaxTextArea());
		HistoryNode node = new HistoryNode(pane, file, 5);
		assertTrue(node.equals(new HistoryNode(null, new File("a.txt"), 5)));
		assertTrue(node.equals(new HistoryNode(pane, null, 5)));
		assertFalse(node.equals(new HistoryNode(pane, file, 6)));
		assertFalse(node.equals(new HistoryNode(new RTextScrollPane(new RSyntaxTextArea()), new File("b.txt"), 5)));
		assertFalse(node.equals(null));
		assertFalse(node.equals("a.txt"));
		assertSame(pane, node.getScrollPane());
		assertEquals(file, node.getLoadedFile());
		assertEquals(5, node.getCaretPosition());
		node.setCaretPosition(9);
		node.setLoadedFile(null);
		node.setScrollPane(null);
		assertEquals(9, node.getCaretPosition());
		assertTrue(node.toString().contains("caretPosition=9"));
	}

	@Test
	public void historyNodeEqualsItself() {
		HistoryNode empty = new HistoryNode(null, null, 0);
		assertTrue(empty.equals(empty));
	}
}
