package utils.config;

import java.io.IOException;

public class GeneralConfig extends AbstractConfig {
	
	public static final String KEY_BASE_VOLUME = "BASE_VOLUME";
	public static final String KEY_EFFECTS_VOLUME = "EFFECTS_VOLUME";
	public static final String KEY_DIFFICULTY = "DIFFICULTY";
	public static final String KEY_BASE_MUTED = "KEY_BASE_MUTED";

	public GeneralConfig(String propertiesPath) throws IOException {
		super(propertiesPath);
	}

}
