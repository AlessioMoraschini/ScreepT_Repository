package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
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
			while(line!=null){
				line = StringUtils.trim(line);
				if(line.length()>0 && line.substring(0,1).equals("-")){
					line = line.substring(1, line.length());
				}
				line = StringUtils.trim(line);
				provider_temp.addCompletion(new BasicCompletion(provider_temp,line));
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
			
			ac.install(code_text_area);
			
			logger.info("Grammar installed: "+filename);
		
		} catch (IOException e) {
			logger.error("Error reading: "+filename, e);
		}								
	}//end of installer 
}
