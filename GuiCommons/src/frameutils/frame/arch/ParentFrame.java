package frameutils.frame.arch;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

public class ParentFrame extends JFrame{
	private static final long serialVersionUID = 7079644907872152737L;
	public static Logger logger = Logger.getLogger(ParentFrame.class);
	
	public AtomicBoolean isActive = new AtomicBoolean(true);
	
	public ParentFrame() {
		isActive.set(true);
	}
}
