package gUI.utils.texteditor;

import java.awt.Component;
import java.util.Collections;

import gUI.panels.TextEditor;
import tabbededitor.CustomTabbedEditor;
import tabbedpaneutils.FileDragNDropManager;

@Deprecated
public class TabMover {
	
	private static volatile boolean movingTab = false;
	FileDragNDropManager tabReferenceDraggable;
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
        Collections.swap(currrentTabbedEditor.loadedList, indexA, indexB);
        
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
