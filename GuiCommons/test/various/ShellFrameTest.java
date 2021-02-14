package various;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import frameutils.frame.ShellFrame;
import gui.GuiUtils;
import resources.IconsPathConfigurator;

public class ShellFrameTest {

	public static void main(String[] args) {
		
		IconsPathConfigurator.ICON_PAUSE = "./test_resources/icons/pause.png";
		IconsPathConfigurator.ICON_PLAY = "./test_resources/icons/play.png";
		IconsPathConfigurator.ICON_STOP = "./test_resources/icons/Stop.png";
		
		
		ShellFrame shellFrame = ShellFrame.getInstance(null, true);
		shellFrame.setAlwaysOnTop(true);
		
		shellFrame.setMinimumSize(shellFrame.getMinimumDimension());
		shellFrame.setPreferredSize(shellFrame.getDefaultDimension());
		
		shellFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
//				}
				GuiUtils.centerComponent(shellFrame, shellFrame.getDefaultDimension());
				shellFrame.setLocationRelativeTo(null);
				shellFrame.setVisible(true);
				
				System.out.print("Hello this is a test text!");
			}
		});
	}
}
