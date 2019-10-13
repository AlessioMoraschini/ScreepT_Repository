package resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.apache.log4j.Logger;

public class PropertiesManager {
	
	// PropertiesManager logger
	static Logger logger = Logger.getLogger(PropertiesManager.class); 
	
	public File dynamicFileBindingsDefSource;
	public Properties dynamicFileBindingsDef;
	public PropertiesConfigurationLayout layoutApacheCommons;
	
	/**
	 * To check if definition had success, check if field <b>dynamicFileBindingsDefSource</b> is null after object's creation:
	 * in that case something has gone wrong
	 * 
	 * @param bindingsFileDefinitionSource
	 */
	public PropertiesManager(String bindingsFileDefinitionSource) {
		dynamicFileBindingsDefSource = new File(bindingsFileDefinitionSource);
		dynamicFileBindingsDef = new Properties();

		PropertiesConfiguration config = new PropertiesConfiguration();
		layoutApacheCommons = new PropertiesConfigurationLayout(config);
		
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
	
	/**
	 * Use this to refresh properties
	 * @return
	 */
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
	
	/**
	 * Use this to avoid overwriting comments existing in dest file ;)
	 * 
	 * @param propertyName
	 * @param value
	 */
	public void savePropertiesLayoutApache(String propertyName, String value) {
		try {
			GeneralConfig.linkedMappings.setProperty(propertyName, value);
        	layoutApacheCommons.load(new InputStreamReader(new FileInputStream(dynamicFileBindingsDefSource)));
        	layoutApacheCommons.getConfiguration().setProperty(propertyName, value);
        	logger.info("Saving property in " + dynamicFileBindingsDefSource + "\n\n[" + propertyName + " : " + value);
        	layoutApacheCommons.save(new FileWriter(dynamicFileBindingsDefSource, false));
        	logger.info("Saved!");
		} catch (ConfigurationException | IOException e) {
			logger.error(e);
		}
	}
	
	public void saveProperty(String propertyName, String value) {
		GeneralConfig.linkedMappings.setProperty(propertyName, value);
		GeneralConfig.linkedMappings.put(propertyName, value);
		java.io.OutputStream out = null;
		try {
			logger.info("Saving property in " + dynamicFileBindingsDefSource + "\n\n[" + propertyName + " : " + value);
			out = new FileOutputStream(GeneralConfig.resourcesMappingDefPath);
			GeneralConfig.linkedMappings.store(out, "# Updated on "+GeneralConfig.GENERIC_FORMATTER.format(new Date()));
        	logger.info("Saved!");
		} catch (IOException e1) {
			logger.error(e1);
		} finally {
			try {
				out.flush();
				out.close();
			} catch (IOException e1) {
				logger.error(e1);
			}
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
