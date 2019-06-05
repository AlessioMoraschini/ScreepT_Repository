package enums;

import resources.IconsPathConfigurator;

public enum Themes {DARK(IconsPathConfigurator.RESOURCES_DIR + "themes/dark.xml"),
					DEFAULT(IconsPathConfigurator.RESOURCES_DIR + "themes/default.xml"),
					DEFAULT_V2(IconsPathConfigurator.RESOURCES_DIR + "themes/default-alt.xml"),
					ECLIPSE(IconsPathConfigurator.RESOURCES_DIR + "themes/eclipse.xml"),
					IDEA(IconsPathConfigurator.RESOURCES_DIR + "themes/idea.xml"),
					MONOKAI(IconsPathConfigurator.RESOURCES_DIR + "themes/monokai.xml"),
					THEMEVS(IconsPathConfigurator.RESOURCES_DIR + "themes/vs.xml"),
					NONE("");
	
	public String defaultPath;
	
	private Themes(String path) {
		this.defaultPath = path;
	}

}
