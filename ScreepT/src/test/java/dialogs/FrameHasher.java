package dialogs;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Test;

import gui.commons.frameutils.frame.HashCheckerFrame;

public class FrameHasher {

	@Test
	public void TestHashFrame() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				HashCheckerFrame hashCheckerFrame = new HashCheckerFrame(new File(""), new JFrame(), null);
				hashCheckerFrame.setVisible(true);
				hashCheckerFrame.setBounds(100, 100, 800, 420);
				hashCheckerFrame.setAlwaysOnTop(true);
			}
		});
	}
}
