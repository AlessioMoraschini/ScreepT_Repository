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
package gui.texteditor.commons.utils.panels.arch;

import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import gui.texteditor.commons.utils.dialog.ITextAreaDialogs;

public class TogglePanelTextArea extends JPanel implements IParentPanel{
	private static final long serialVersionUID = 5510587599931974469L;

	// ParentPanel logger creation
	public static Logger logger = Logger.getLogger(TogglePanelTextArea.class);
	
	public ITextAreaDialogs dialogManager;
	public JScrollPane currentScrollPane;
	
	/**
	 * This method syncronize current secondary panel textarea with the main one retrieved with 
	 * @throws IOException 
	 */
	@Override
	public void sync() {
		// DEFAULT warning message if not overridden
		logger.warn("This method is not yet implemented");
	}

	/**
	 * This method reinitialize the panel based on data contained in file identified by target key.
	 * You can refer to generalConfig to know which kind of panel you can call and their relative files
	 * @throws Exception 
	 */
	@Override 
	public void reload(String target) throws Exception{
		// DEFAULT warning message if not overridden
		logger.warn("This method is not yet implemented");
	}
	
	/**
	 * This method reinitialize the panel based on data contained in file identified by target key.
	 * You can refer to generalConfig to know which kind of panel you can call and their relative files
	 * 
	 * @throws IOException 
	 */
	@Override
	public void doAction(String target) throws IOException{
		// DEFAULT warning message if not overridden
		logger.warn("This method is not yet implemented");
	}

}
