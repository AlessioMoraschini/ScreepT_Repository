package resources;

import java.awt.Dimension;
import java.awt.Font;
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
import org.apache.commons.lang.StringUtils;
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
	 * Avoid null values reading from properties
	 * @return
	 */
	public String getProperty(String key) {
		String res = "";
		if(dynamicFileBindingsDef != null) {
			res = dynamicFileBindingsDef.getProperty(key);
			res = (res != null)? res : "";
		}
		
		return res;
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
	
	public int getIntVarFromProps(String key, int defaultVal) {
		String val = dynamicFileBindingsDef.getProperty(key);
		try {
			return Integer.valueOf(val);
		}catch(Exception e){
			return defaultVal;
		}
	}

	public float getFloatVarFromProps(String key, float defaultVal) {
		String val = dynamicFileBindingsDef.getProperty(key);
		try {
			return Float.valueOf(val);
		}catch(Exception e){
			return defaultVal;
		}
	}
	
	/**
	 * Parse dimension from property string in the form of "int-int", identified by his String key in properties
	 */
	public Dimension getDimensionFromString(String separator, String key, Dimension defaultDim) {
		int width;
		int height;
		String source = StringUtils.trimToEmpty(dynamicFileBindingsDef.getProperty(key));
		try {
			String[] splitted = source.split(separator);
			width = Integer.parseInt(splitted[0]);
			height = Integer.parseInt(splitted[1]);
			return new Dimension(width, height);
		} catch (Exception e) {
			return defaultDim;
		}
		
	}
	
    /**
     * parse font from a representing string
     */
    public Font getFontFromString(String key, Font defaultFont) {
    	String stringFont = StringUtils.trimToEmpty(dynamicFileBindingsDef.getProperty(key));
    	if("".equals(StringUtils.trimToEmpty(stringFont))) return defaultFont;
    	
    	String[] font = stringFont.split("-");
    	String type;
    	int style;
    	int size;
		try {
			if (font.length != 3) {
				return defaultFont;
			}
			type = font[0];
			style = Integer.parseInt(font[1]);
			size = Integer.parseInt(font[2]);
		} catch (Exception e) {
			return defaultFont;
		}
    	
    	return new Font(type, style, size);
    }
    
	// GETTERS AND SETTERS //	
	
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
