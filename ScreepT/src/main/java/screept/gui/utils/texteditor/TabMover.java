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
package screept.gui.utils.texteditor;

import java.awt.Component;
import java.util.Collections;

import javax.swing.JTabbedPane;

import gui.texteditor.commons.tabbededitor.CustomTabbedEditor;

@Deprecated
public class TabMover {
	
	@SuppressWarnings("unused")
	private static volatile boolean movingTab = false;
	JTabbedPane tabReferenceDraggable;
	CustomTabbedEditor currrentTabbedEditor;
	
	public TabMover(CustomTabbedEditor tabRef) {
		currrentTabbedEditor = tabRef;
		tabReferenceDraggable = currrentTabbedEditor.mainTabList;
	}
	
	/**
	 * Use this to move a tab left or right by one position
	 * @param leftDirection
	 * @deprecated
	 */
	public void moveCurrentTab(boolean leftDirection) {
		movingTab = true;
		int currentIndexSelected = tabReferenceDraggable.getSelectedIndex();
		int newIndex;
		if(leftDirection) {
			// LEFT
			newIndex = currentIndexSelected-1;
			if(isExtremeLeftOrRightCurrentTab(leftDirection)) {
				newIndex = tabReferenceDraggable.getTabCount()-1;
			}
		}else {
			// RIGHT
			newIndex = currentIndexSelected+1;
			if(isExtremeLeftOrRightCurrentTab(leftDirection)) {
				newIndex = 0;
			}
		}
		
		switchTwoTabs(currentIndexSelected, newIndex);
		movingTab = false;
		tabReferenceDraggable.setSelectedIndex(newIndex);
	}
	
	private boolean switchTwoTabs(int indexA, int indexB) {
		int maxIndex = currrentTabbedEditor.mainTabList.getTabCount()-1;
		boolean error1 = indexA > maxIndex || indexB > maxIndex;
		boolean error2 = indexA < 0 || indexB < 0;
		if(error1 || error2) {
			return false;
		}
		
		moveTabs(indexA, indexB);
		
		return true;	
	}
	
	private void moveTabs(int indexA, int indexB) {
		
		
		Component compA = tabReferenceDraggable.getComponentAt(indexA);
		Component tabHeadA = tabReferenceDraggable.getTabComponentAt(indexA);
        String titleA = tabReferenceDraggable.getTitleAt(indexA);
        tabReferenceDraggable.setTabComponentAt(indexA, tabReferenceDraggable.getTabComponentAt(indexB));
        tabReferenceDraggable.insertTab(tabReferenceDraggable.getTitleAt(indexB), null, tabReferenceDraggable.getComponentAt(indexB), null, indexA);
        tabReferenceDraggable.setTabComponentAt(indexB, tabHeadA);
        tabReferenceDraggable.insertTab(titleA, null, compA, null, indexB);
        Collections.swap(currrentTabbedEditor.loadedList.getList(), indexA, indexB);
        
	}
	
	private boolean isExtremeLeftOrRightCurrentTab(boolean leftDirection) {
				
		int nTabs = tabReferenceDraggable.getTabCount();
		int currentTabIndex = tabReferenceDraggable.getSelectedIndex();
		
		if (leftDirection) {
			if ((currentTabIndex == 0)) {
				return true;
			} else {
				return false;
			} 
		}else {
			if ((currentTabIndex == nTabs - 1)) {
				return true;
			} else {
				return false;
			} 
		}
	}

}
