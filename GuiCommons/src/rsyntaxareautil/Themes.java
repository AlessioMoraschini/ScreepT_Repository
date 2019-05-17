package rsyntaxareautil;

import resources.IconsPathConfigurator;

public enum Themes {DARK(IconsPathConfigurator.RESOURCES_DIR + "themes/dark.xml"),
					DEFAULT(IconsPathConfigurator.RESOURCES_DIR + "themes/default.xml"),
					DEFAULT2(IconsPathConfigurator.RESOURCES_DIR + "themes/default-alt.xml"),
					ELIPSE(IconsPathConfigurator.RESOURCES_DIR + "themes/eclipse.xml"),
					IDEA(IconsPathConfigurator.RESOURCES_DIR + "themes/idea.xml"),
					MONOKAI(IconsPathConfigurator.RESOURCES_DIR + "themes/monokai.xml"),
					THEMEVS(IconsPathConfigurator.RESOURCES_DIR + "themes/vs.xml"),
					NONE("");
	
	public String defaultPath;
	
	private Themes(String path) {
		this.defaultPath = path;
	}

}
