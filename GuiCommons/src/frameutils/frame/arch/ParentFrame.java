package frameutils.frame.arch;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

public class ParentFrame extends JFrame{

	public AtomicBoolean isActive = new AtomicBoolean(true);
}
