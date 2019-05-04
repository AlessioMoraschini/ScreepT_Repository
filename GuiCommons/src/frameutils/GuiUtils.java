package frameutils;


import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;

import org.apache.log4j.Logger;

public class GuiUtils {
	
	// INItializer logger creation		
	static Logger logger = Logger.getLogger(GuiUtils.class); 
	
	public static final Cursor CURSOR_WAIT = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	public static final Cursor CURSOR_DEFAULT = Cursor.getDefaultCursor();
	public static final Cursor CURSOR_TEXT = new Cursor(Cursor.TEXT_CURSOR);
	public static final Cursor CURSOR_HAND = new Cursor(Cursor.HAND_CURSOR);
	
	public static boolean setFrameIcon(Image icon, JFrame frameToSet) {
		if(icon != null) {
			frameToSet.setIconImage(icon);
			return true;
		}else {
			return false;
		}
	}
	
	public static void setFrameIcon(String icon, JFrame frameToSet) {
		ImageIcon imgIcon = new ImageIcon(icon);
		frameToSet.setIconImage(imgIcon.getImage());
	}
	
	public static Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}
	
	public static Dimension getScreenSize(int marginLeftRight) {
		Dimension screen = getScreenSize();
		return new Dimension(screen.width-(marginLeftRight+marginLeftRight), screen.height);
	}
	
	public static void centerComponent(Component targetComponent, int percentSize) {
		float percent = (float)percentSize/100;
		Dimension screenSize = getScreenSize();
		int W = (int)(screenSize.width * percent);
		int H = (int)(screenSize.height * percent);
		targetComponent.setSize(W, H);
		targetComponent.setLocation(screenSize.width/2-W/2, screenSize.height/2-H/2);
	}
	
	public static void centerComponent(Component targetFrame, int W, int H) {
		Dimension screenSize = getScreenSize();
		targetFrame.setSize(W, H);
		targetFrame.setLocation(screenSize.width/2-W/2, screenSize.height/2-H/2);
	}
	
	public static void centerFrame(JFrame targetFrame, int percentSize) {
		float percent = (float)percentSize/100;
		Dimension screenSize = getScreenSize();
		int W = (int)(screenSize.width * percent);
		int H = (int)(screenSize.height * percent);
		targetFrame.setSize(W, H);
		targetFrame.setLocation(screenSize.width/2-W/2, screenSize.height/2-H/2);
	}
	
	public static void centerFrame(JFrame targetFrame, int W, int H) {
		Dimension screenSize = getScreenSize();
		targetFrame.setSize(W, H);
		targetFrame.setLocation(screenSize.width/2-W/2, screenSize.height/2-H/2);
	}
	
	public static void moveFrame(Component element, int xDiff, int yDiff) {
		int newX = ((element.getX() + xDiff) < 0)? 0 : (element.getX() + xDiff);
		int newY = ((element.getY() + yDiff) < 0)? 0 : (element.getY() + yDiff);
		element.setBounds(new Rectangle(newX, newY, element.getWidth(), element.getHeight()));
	}
	
	public static void openInFileSystem(File destination) {
		if (destination != null && destination.exists()) {
			try {
				Desktop.getDesktop().open(destination);
				logger.debug(destination.getAbsolutePath()+" Opened in system explorer");

			} catch (IOException e1) {
				logger.error("Error opening folder in system explorer !", e1);
			} 
		}	
	}
	
	public static void setCursor(JFrame frame, Cursor cursor) {
        if (frame != null) {
            RootPaneContainer root = (RootPaneContainer) frame.getRootPane().getTopLevelAncestor();
            root.getGlassPane().setCursor(cursor);
            root.getGlassPane().setVisible(true);
        }
    }
	
	public static void setCursor( JRootPane frame, Cursor cursor) {
        if (frame != null) {
            RootPaneContainer root = (RootPaneContainer) frame.getTopLevelAncestor();
            root.getGlassPane().setCursor(cursor);
            root.getGlassPane().setVisible(true);
        }
    }
	
	public static void setWaitCursor(JFrame frame) {
        if (frame != null) {
            RootPaneContainer root = (RootPaneContainer) frame.getRootPane().getTopLevelAncestor();
            root.getGlassPane().setCursor(CURSOR_WAIT);
            root.getGlassPane().setVisible(true);
        }
    }
	
	public static void setWaitCursor(JRootPane frame) {
		if (frame != null) {
			RootPaneContainer root = (RootPaneContainer) frame.getTopLevelAncestor();
			root.getGlassPane().setCursor(CURSOR_WAIT);
			root.getGlassPane().setVisible(true);
		}
	}

    public static void setDefaultCursor(JFrame frame) {
        if (frame != null) {
            RootPaneContainer root = (RootPaneContainer) frame.getRootPane().getTopLevelAncestor();
            root.getGlassPane().setCursor(CURSOR_DEFAULT);
        }
    }
    
    public static void setDefaultCursor(JRootPane frame) {
        if (frame != null) {
            RootPaneContainer root = (RootPaneContainer) frame.getTopLevelAncestor();
            root.getGlassPane().setCursor(CURSOR_DEFAULT);
        }
    }
	
}
