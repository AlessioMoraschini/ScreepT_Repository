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
package utils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;


/**
 * The Class AutoComplete. This class provides autocomplete functionality for writing  plantuml codes easily 
 * 
 */
public class AutoComplete {

	static Logger logger = Logger.getLogger(AutoComplete.class);
	
	public RSyntaxTextArea code_text_area;   /**<text area for writing code */
	public CompletionProvider provider;   /**<provides autocompletiton */
	public AutoCompletion ac;   /**< autocompletiion engine */

	/**
	 * Instantiates a new auto complete.
	 *
	 * @param textarea the textarea
	 */
	public  AutoComplete(RSyntaxTextArea textarea) {

		code_text_area=textarea;
		
	}//end of method

	/**
	 * it install grammar rules from given file for autocomplete  
	 *
	 * @param filename the filename for installing grammer rules
	 */
	 public void installGrammar(String filename){

		DefaultCompletionProvider provider_temp = new DefaultCompletionProvider();
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(new File(filename));
		} catch (FileNotFoundException e) {
			logger.error("could not read: "+filename, e);
			try {
				fileReader.close();
			}catch(Exception e1) {
			}
			return;
		}
		
		BufferedReader br = new BufferedReader(fileReader);
		try {
			String line=br.readLine();
			String description = "";
			while(line!=null){
				line = line.trim();
				
				if(line.length()>0 && line.substring(0,1).equals("-")){
					line = line.substring(1, line.length());
				}
				line = line.trim();
				try {
					description = line.split(";;;")[1];
					line = line.split(";;;")[0];
				} catch (Exception e) {
					description = "";
				}
				
				if(!"".equals(line)) {
					provider_temp.addCompletion(new BasicCompletion(provider_temp,line, null, description));
				}
				line =br.readLine();
			}
		
			br.close();
			provider= provider_temp;
			ac = new AutoCompletion(provider);
			ac.setAutoActivationEnabled(true);
			ac.setAutoActivationDelay(0);
			ac.setParameterAssistanceEnabled(true);
			ac.setAutoCompleteEnabled(true);
			ac.setAutoCompleteSingleChoices(true);
			ac.setParameterAssistanceEnabled(true);
			ac.setDescriptionWindowSize(400,200);
			ac.setShowDescWindow(true);
			ac.setDescriptionWindowColor(new Color(252, 240, 162));
			
			ac.install(code_text_area);
			
			logger.info("Grammar installed: "+filename);
		
		} catch (IOException e) {
			logger.error("Error reading: "+filename, e);
		}								
	}//end of installer 
}
