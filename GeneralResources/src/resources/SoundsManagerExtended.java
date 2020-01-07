package resources;

import sounds.SoundsManager;

public class SoundsManagerExtended extends SoundsManager {
	
	static {
		volume = SoundsConfigurator.DEFAULT_EFFECTS_VOLUME;
	}

	public static void playSound(String pathToResource, Float volum){
		if(volum != null) {
			SoundsManager.playSound(pathToResource, volum);
		}else {
			SoundsManager.playSound(pathToResource, SoundsConfigurator.DEFAULT_EFFECTS_VOLUME);
		}
	}
}
