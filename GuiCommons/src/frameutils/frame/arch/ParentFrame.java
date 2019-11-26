package frameutils.frame.arch;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import dialogutils.GenericFileChooserDialog;
import dialogutils.JOptionHelper;

public class ParentFrame extends JFrame{
	private static final long serialVersionUID = 7079644907872152737L;
	public static Logger logger = Logger.getLogger(ParentFrame.class);
	
	public AtomicBoolean isActive = new AtomicBoolean(true);

	public JOptionHelper dialogHelper;
	public GenericFileChooserDialog fileChooser;
	
	public ParentFrame() {
		isActive.set(true);
		
		this.dialogHelper = new JOptionHelper(this);
		this.fileChooser = new GenericFileChooserDialog(null);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
}
