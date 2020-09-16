package mousehacker;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.Toolkit;
import java.util.Random;

public class GhostMouse {

	public void run() throws AWTException, InterruptedException {
		Robot robot = new Robot();
		Random rand = new Random();
		for(int i = 0; i < 100; i++) {
			int x = rand.nextInt(getScreenSize().width);
			int y = rand.nextInt(getScreenSize().height);
			robot.mouseMove(x, y);
			Thread.sleep(100);
		}
	}
	
	public static Dimension getScreenSize() {
		try {
			return Toolkit.getDefaultToolkit().getScreenSize();
		} catch (Throwable e) {
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			int width = gd.getDisplayMode().getWidth();
			int height = gd.getDisplayMode().getHeight();
			return new Dimension(width, height);
		}
	}
	
	public static void main(String[] args) {
		try {
			new GhostMouse().run();
		} catch (AWTException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
