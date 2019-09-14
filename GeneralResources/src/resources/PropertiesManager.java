package resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class PropertiesManager {
	
	public File dynamicFileBindingsDefSource;
	public Properties dynamicFileBindingsDef;
	
	/**
	 * To check if definition had success, check if field <b>dynamicFileBindingsDefSource</b> is null after object's creation:
	 * in that case something has gone wrong
	 * 
	 * @param bindingsFileDefinitionSource
	 */
	public PropertiesManager(String bindingsFileDefinitionSource) {
		dynamicFileBindingsDefSource = new File(bindingsFileDefinitionSource);
		dynamicFileBindingsDef = new Properties();
		
		if (!prepareDefObj()) {
			dynamicFileBindingsDefSource = null;
		}
	}
	
	/**
	 * To check if definition had success, check if field <b>dynamicFileBindingsDefSource</b> is null after object's creation:
	 * in that case something has gone wrong
	 * 
	 * @param bindingsFileDefinitionSource
	 */
	public PropertiesManager(File bindingsFileDefinitionSource) {
		dynamicFileBindingsDefSource = bindingsFileDefinitionSource;
		dynamicFileBindingsDef = new Properties();
		
		if (!prepareDefObj()) {
			dynamicFileBindingsDefSource = null;
		}
	}
	
	public PropertiesManager(Properties bindingsFileDefinitionDef) {
		dynamicFileBindingsDef = bindingsFileDefinitionDef;
		dynamicFileBindingsDefSource = new File("");
	}
	
	public boolean prepareDefObj() {

        if (dynamicFileBindingsDefSource != null) {
			
        	synchronized (dynamicFileBindingsDefSource) {
				try (InputStream input = new FileInputStream(dynamicFileBindingsDefSource)) {

					dynamicFileBindingsDef.load(input);
					return true;

				} catch (IOException ex) {
					return false;
				}
			} 
        	
		}else {
			return false;
		}
    }

	public File getDynamicFileBindingsDefSource() {
		return dynamicFileBindingsDefSource;
	}

	public void setDynamicFileBindingsDefSource(File dynamicFileBindingsDefSource) {
		this.dynamicFileBindingsDefSource = dynamicFileBindingsDefSource;
	}

	public Properties getDynamicFileBindingsDef() {
		return dynamicFileBindingsDef;
	}

	public void setDynamicFileBindingsDef(Properties dynamicFileBindingsDef) {
		this.dynamicFileBindingsDef = dynamicFileBindingsDef;
	}
	
	
}
