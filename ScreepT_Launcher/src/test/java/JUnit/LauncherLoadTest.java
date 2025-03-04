package JUnit;

import org.junit.Test;

import screept.launcher.gui.utils.LauncherLoaderFrame;

public class LauncherLoadTest {
	
	@Test
	public void testProgressBar(){
		LauncherLoaderFrame loaderFrame = new LauncherLoaderFrame("Loading Test", "LAUNCHER/launcher_icon.jpg", 200);
		for (int i = 0; i <= 100; i++) {
			loaderFrame.update(String.valueOf(loaderFrame.loader.getProgress()), i);
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				System.out.println("INTERRUPTED!");
			}
		}
	}	
}
