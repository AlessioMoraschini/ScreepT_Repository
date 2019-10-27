package gui.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import resources.SoundsConfigurator;

public class Sounds {

	private static float volume = SoundsConfigurator.DEFAULT_EFFECTS_VOLUME;
	private static AudioInputStream openStream = null;

	public static void playSound(String pathToResource, Float volum){
		if(volum != null) {
			setVolume(volum);
		}else {
			setVolume(SoundsConfigurator.DEFAULT_EFFECTS_VOLUME);
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
		
		Sounds.volume = volume;
	}
	
}
