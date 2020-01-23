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
package utils.dialog;

import java.awt.Dialog;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public interface ITextAreaDialogs {

	public RSyntaxTextArea getTargetTextArea();
	public void loadThemeIntoTarget(RSyntaxTextArea txtArea);
	public String getCurrentClipboard();
	public Dialog getSearchDialog();
	public Dialog getReplaceDialog();
}
