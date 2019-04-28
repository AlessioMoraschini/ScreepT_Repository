package resources;

public class SoundsConfigurator {

	public static String SOUNDS_FOLDER = "Resources_Scrypt/sounds/";
	
	public static float DEFAULT_EFFECTS_VOLUME = 0.45f;
	
	public static final String CLICK_1 = SOUNDS_FOLDER + "Click1.wav";
	public static final String CLICK_2 = SOUNDS_FOLDER + "Click2.wav";
	public static final String CLICK_DOUBLE = SOUNDS_FOLDER + "clickDouble.wav";
	public static final String ERROR = SOUNDS_FOLDER + "Error.wav";
	public static final String SMASHING = SOUNDS_FOLDER + "Smashing.wav";
	public static final String SOUND_CLOCK = SOUNDS_FOLDER + "clock.wav";
	public static final String SOUND_PLING = SOUNDS_FOLDER + "Pling.wav";
	public static final String SOUND_POOL_SHOT = SOUNDS_FOLDER + "PoolShot.wav";
	
	public static String getSOUNDS_FOLDER() {
		return SOUNDS_FOLDER;
	}
	public static void setSOUNDS_FOLDER(String sOUNDS_FOLDER) {
		SOUNDS_FOLDER = sOUNDS_FOLDER;
	}
}
