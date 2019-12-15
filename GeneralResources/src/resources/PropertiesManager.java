package resources;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
	
	public SimpleDateFormat GENERIC_FORMATTER = new SimpleDateFormat("yyyy/MM/dd - hh:mm:ss");
	
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

		PropertiesConfiguration config = new PropertiesConfiguration();
		layoutApacheCommons = new PropertiesConfigurationLayout(config);
		
		if (!prepareDefObj()) {
			dynamicFileBindingsDefSource = null;
		}
	}
	
	public PropertiesManager(Properties bindingsFileDefinitionDef) {
		dynamicFileBindingsDef = bindingsFileDefinitionDef;
		dynamicFileBindingsDefSource = new File("");

		PropertiesConfiguration config = new PropertiesConfiguration();
		layoutApacheCommons = new PropertiesConfigurationLayout(config);
	}
	
	/**
	 * Avoid null values reading from properties
	 * @return
	 */
	public String getProperty(String key) {
		logger.debug("Loading property: " + key + " From properties file: " + dynamicFileBindingsDefSource);
		String res = "";
		if(dynamicFileBindingsDef != null) {
			res = dynamicFileBindingsDef.getProperty(key);
			res = (res != null)? res : "";
		}
		
		logger.debug("Loaded property: " + res);
		return res;
	}

	/**
	 * Avoid null values reading from properties
	 * @return
	 */
	public String getProperty(String key, String defaultVal) {
		String res = "";
		if(dynamicFileBindingsDef != null) {
			res = dynamicFileBindingsDef.getProperty(key);
			res = (res != null)? res : defaultVal;
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
					logger.error("Cannot prepare properties file mapping !!");
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
			dynamicFileBindingsDef.setProperty(propertyName, value);
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
		dynamicFileBindingsDef.setProperty(propertyName, value);
		dynamicFileBindingsDef.put(propertyName, value);
		java.io.OutputStream out = null;
		try {
			logger.info("Saving property in " + dynamicFileBindingsDefSource + "\n\n[" + propertyName + " : " + value);
			out = new FileOutputStream(dynamicFileBindingsDefSource);
			dynamicFileBindingsDef.store(out, "# Updated on " + GENERIC_FORMATTER.format(new Date()));
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
	
	public boolean saveAllProperties() {
		java.io.OutputStream out = null;
		try {
			logger.info("Saving properties in " + dynamicFileBindingsDefSource + "\n\n");
			out = new FileOutputStream(dynamicFileBindingsDefSource);
			dynamicFileBindingsDef.store(out, "# Updated on " + GENERIC_FORMATTER.format(new Date()));
        	logger.info("Saved!");
        	return true;
		} catch (IOException e1) {
			logger.error(e1);
			return false;
		} finally {
			try {
				out.flush();
				out.close();
			} catch (IOException e1) {
				logger.error(e1);
			}
		}
	}
	
	public boolean removeProperty(String key){
		FileOutputStream out = null;
		
		try {
			out = new FileOutputStream(dynamicFileBindingsDefSource);
			dynamicFileBindingsDef.remove(key);
			dynamicFileBindingsDef.store(out, null);
		} catch (IOException e) {
			return false;
		} finally {
			try {
				out.close();
			} catch (Exception e2) {
			}
		}
		return true;
	}
	
	public int getIntVarFromProps(String key, int defaultVal) {
		String val = dynamicFileBindingsDef.getProperty(key);
		try {
			return Integer.valueOf(val);
		}catch(Exception e){
			return defaultVal;
		}
	}
	
	public ArrayList<String> getStringList(String key, String separator){
		ArrayList<String> list = new ArrayList<>();
		
		String rawList = dynamicFileBindingsDef.getProperty(key);
		
		if(rawList == null || "".equals(rawList)) {
			return list;
		}
		
		String[] splitted = rawList.split(separator);
		
		list.addAll(Arrays.asList(splitted));
		
		return list;
	}
	
	public static String listToString(ArrayList<String> stringList, String separator) {
		String concatenation = "";
		// concatenate each element with given separator
		for(String currentString : stringList) {
			concatenation += currentString + separator;
		}
		if(concatenation.length() > 0) {
			concatenation = concatenation.substring(0, concatenation.length() - separator.length());
		}
		return concatenation;
	}
	
	public ArrayList<File> getFileList(String key, String separator, boolean excludeNonExisting){
		ArrayList<String> rawList = getStringList(key, separator);
		ArrayList<File> fileList = new ArrayList<File>();
		
		if(rawList == null || rawList.isEmpty()) {
			return fileList;
		}
		
		for(String current : rawList) {
			File currentFile = new File(current);
			if(!excludeNonExisting || currentFile.exists()) {
				fileList.add(currentFile);
			}
		}
		
		return fileList;
	}

	public boolean getBooleanVarFromProps(String key, boolean defaultVal) {
		String val = dynamicFileBindingsDef.getProperty(key);
		try {
			return Boolean.valueOf(val);
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
	 * Read color from property string in form "R-G-B" with integer values
	 **/
	public Color getColorVarFromProps(String key, Color defaultVal) {
		String val = dynamicFileBindingsDef.getProperty(key);
		try {
			String[] rgb = val.split("-");
	    	if(rgb.length != 3) {
	    		return new Color(0,0,0);
	    	}
	    	int red = Integer.parseInt(rgb[0]);
	    	int green = Integer.parseInt(rgb[1]);
	    	int blue = Integer.parseInt(rgb[2]);
	    	
	    	return new Color(red, green, blue);
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
