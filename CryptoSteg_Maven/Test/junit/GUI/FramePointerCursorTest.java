package junit.GUI;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import frameutils.utils.GuiUtils;
import media.ImageGeneralWorker;
import resources.IconsPathConfigurator;

public class FramePointerCursorTest extends JFrame{

	FramePointerCursorTest() throws IOException {
        final int x1 = 100, y1 = 100, x2 = 300, y2 = 300;

        BufferedImage cursorImg = ImageIO.read(new File(IconsPathConfigurator.ICON_COLOR_SAMPLER));
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        Cursor defaultCursor = new Cursor(Cursor.HAND_CURSOR);

        addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if (x > x1 && x < x2 && y > y1 && y < y2)
                    setCursor(blankCursor);
                else
                    setCursor(defaultCursor);
            }
        });

        setSize(new Dimension(400, 400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        GuiUtils.centerComponent(this);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
			try {
				new FramePointerCursorTest();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
    }
}
