package junit.media;

import org.junit.Test;

import media.SoundsExtended;
import resources.SoundsConfigurator;
import testUtils.TestUtils;

public class AudioTest {
	
	@Test
	public void TestAudioClip() throws InterruptedException {
		for (int i = 0; i < 5; i++) {
			TestUtils.printl("Playing: " + SoundsConfigurator.CLICK_2);
			SoundsExtended.setVolume(0.15f*i);
			SoundsExtended.playSound(SoundsConfigurator.CLICK_2, null);
			Thread.sleep(300);
		}
	}
}
