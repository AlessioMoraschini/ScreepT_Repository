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
package om;

import utility.string.StringWorker.EOL;

public interface ICommonShortcuts {

	public boolean isCtrlDown();
	public boolean isShiftDown();
	public boolean isAltDown();
	
	public void revalidateUnsaved();
	
	/**
	 * default line terminator will be used
	 */
	public void saveAsShortcut();
	
	/**
	 * If line terminator is null then default one will be used
	 */
	public void saveAsShortcut(EOL lineTerminator);
}
