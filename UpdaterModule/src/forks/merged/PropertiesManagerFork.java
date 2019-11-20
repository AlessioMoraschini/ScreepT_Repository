package forks.merged;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesManagerFork {
	
	public File dynamicFileBindingsDefSource;
	public Properties dynamicFileBindingsDef;
	
	/**
	 * To check if definition had success, check if field <b>dynamicFileBindingsDefSource</b> is null after object's creation:
	 * in that case something has gone wrong
	 * 
	 * @param bindingsFileDefinitionSource
	 */
	public PropertiesManagerFork(String bindingsFileDefinitionSource) {
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
	public PropertiesManagerFork(File bindingsFileDefinitionSource) {
		dynamicFileBindingsDefSource = bindingsFileDefinitionSource;
		dynamicFileBindingsDef = new Properties();
		
		if (!prepareDefObj()) {
			dynamicFileBindingsDefSource = null;
		}
	}
	
	public PropertiesManagerFork(Properties bindingsFileDefinitionDef) {
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
					return false;
				}
			} 
        	
		}else {
			return false;
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
		String source = dynamicFileBindingsDef.getProperty(key);
		
		if (source == null || source.equals("")) {return defaultDim;};
		
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
    	String stringFont = dynamicFileBindingsDef.getProperty(key);
    	
    	if(stringFont == null || stringFont.equals("")) return defaultFont;
    	
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
