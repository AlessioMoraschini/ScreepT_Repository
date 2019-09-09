package enums;

import resources.GeneralConfig;

public enum Themes {DARK(GeneralConfig.RESOURCES_DIR + "themes/dark.xml"),
					DEFAULT(GeneralConfig.RESOURCES_DIR + "themes/default.xml"),
					DEFAULT_V2(GeneralConfig.RESOURCES_DIR + "themes/default-alt.xml"),
					ECLIPSE(GeneralConfig.RESOURCES_DIR + "themes/eclipse.xml"),
					IDEA(GeneralConfig.RESOURCES_DIR + "themes/idea.xml"),
					MONOKAI(GeneralConfig.RESOURCES_DIR + "themes/monokai.xml"),
					THEMEVS(GeneralConfig.RESOURCES_DIR + "themes/vs.xml"),
					NONE("");
	
	public String defaultPath;
	
	private Themes(String path) {
		this.defaultPath = path;
	}

}
