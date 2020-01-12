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
package gUI.utils.tree.crypter.om;

import java.io.File;

public class CrypterDragFileModel {
	public File realFile;
	public String name;
	
	public CrypterDragFileModel(File source) {
		this.name = source.getName();
		this.realFile = source;
	}
	
	public String toString() {
		return realFile.getName();
	}
}
