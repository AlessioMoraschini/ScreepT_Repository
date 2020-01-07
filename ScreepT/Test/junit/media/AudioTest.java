package junit.media;

import org.junit.Test;

import resources.SoundsConfigurator;
import resources.SoundsManagerExtended;
import testUtils.TestUtils;

public class AudioTest {
	
	@Test
	public void TestAudioClip() throws InterruptedException {
		for (int i = 0; i < 5; i++) {
			TestUtils.printl("Playing: " + SoundsConfigurator.CLICK_2);
			SoundsManagerExtended.setVolume(0.15f*i);
			SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
			Thread.sleep(300);
		}
	}
}
