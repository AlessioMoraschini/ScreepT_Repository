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

import java.io.File;

import org.fife.ui.rtextarea.RTextScrollPane;

public class HistoryNode {

	RTextScrollPane scrollPane;
	File loadedFile;
	int caretPosition;
	
	public HistoryNode(RTextScrollPane scrollPane, File loadedFile,	int caretPosition) {
		this.scrollPane = scrollPane;
		this.loadedFile = loadedFile;
		this.caretPosition = caretPosition;
	}

	public RTextScrollPane getScrollPane() {
		return scrollPane;
	}

	public void setScrollPane(RTextScrollPane scrollPane) {
		this.scrollPane = scrollPane;
	}

	public File getLoadedFile() {
		return loadedFile;
	}

	public void setLoadedFile(File loadedFile) {
		this.loadedFile = loadedFile;
	}

	public int getCaretPosition() {
		return caretPosition;
	}

	public void setCaretPosition(int caretPosition) {
		this.caretPosition = caretPosition;
	}

	@Override
	public boolean equals(Object otherNode) {
		
		if (otherNode == this) {
			return true;
		}
		if (otherNode != null && otherNode instanceof HistoryNode) {
			HistoryNode other = (HistoryNode) otherNode;
			boolean fileMatch = false;
			boolean scrollPaneMatch = false;
			boolean lineMatch = other.caretPosition == caretPosition;
			
			if(other.loadedFile != null) {
				fileMatch = other.loadedFile.equals(loadedFile);
			}
			if(other.scrollPane != null) {
				scrollPaneMatch = other.scrollPane.equals(scrollPane);
			}
			
			return lineMatch && (fileMatch || scrollPaneMatch);
		}else {
			return super.equals(otherNode);
		}
	}
	
	@Override
	public String toString() {
		return "HistoryNode [scrollPane=" + scrollPane + ", loadedFile=" + loadedFile + ", caretPosition=" + caretPosition + "]";
	}
	
}
