package sounds;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundsManager {
	
	public static final float DEFAULT_VOLUME = 0.8f;
	public static float volume = DEFAULT_VOLUME;

	// Preset static volumes
	public static final float MAX = 1.0f;
	public static final float HIGH = 0.8f;
	public static final float MED_HIGH = 0.6f;
	public static final float MED = 0.5f;
	public static final float MED_LOW = 0.4f;
	public static final float LOW = 0.3f;
	public static final float MIN = 0.2f;
	public static final float MUTE = 0f;

	public static AudioInputStream openStream = null;
	
	public static void playSound(String pathToResource, Float volum){
		if(volum != null) {
			setVolume(volum);
		}else {
			setVolume(DEFAULT_VOLUME);
		}
		File audioFile = new File(pathToResource);
		if(audioFile == null || !audioFile.exists()) {
			return;
		}
		
		AudioInputStream inputStream = null;
		try {
			Clip clip = AudioSystem.getClip();
	        inputStream = AudioSystem.getAudioInputStream(audioFile);
	        clip.open(inputStream);
	        applyVolume(clip);
	        clip.start();
	        inputStream.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			openStream = inputStream;
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
		
		if(openStream != null) {
			try {
				openStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void applyVolume(Clip clip) {
        FloatControl volumeManager = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float range = volumeManager.getMaximum() - volumeManager.getMinimum();
        float calculatedVolume = volumeManager.getMinimum() + (range * volume);
        volumeManager.setValue(calculatedVolume);
	}

	public static float getVolume() {
		return volume;
	}

	public static void setVolume(float volume) {
		
		volume = volume* (1.0f + volume);
		
		volume = (volume > 1)? 1.0f : volume;
		volume = (volume < 0)? 0.0f : volume;
		
		SoundsManager.volume = volume;
	}
}
