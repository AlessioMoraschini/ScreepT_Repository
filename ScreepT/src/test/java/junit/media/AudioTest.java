package junit.media;

import org.junit.Test;

import resources.SoundsConfigurator;
import resources.SoundsManagerExtended;
import test.util.utils.TestUtils;

public class AudioTest {
	
	@Test
	public void TestAudioClip() throws InterruptedException {
		SoundsConfigurator.init();
		for (int i = 0; i < 5; i++) {
			TestUtils.printl("Playing: " + SoundsConfigurator.CLICK_2);
			SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, 0.15f * i);
			Thread.sleep(100);
		}
	}
}
