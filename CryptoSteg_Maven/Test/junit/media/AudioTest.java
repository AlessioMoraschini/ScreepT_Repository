package junit.media;

import org.junit.Test;

import media.Sounds;
import resources.SoundsConfigurator;
import testUtils.Printer;

public class AudioTest {
	
	@Test
	public void TestAudioClip() throws InterruptedException {
		for (int i = 0; i < 5; i++) {
			Printer.printl("Playing: " + SoundsConfigurator.CLICK_2);
			Sounds.setVolume(0.15f*i);
			Sounds.playSound(SoundsConfigurator.CLICK_2);
			Thread.sleep(300);
		}
	}
}
