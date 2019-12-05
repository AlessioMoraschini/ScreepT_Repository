package guistyle;

import javax.swing.UIManager.LookAndFeelInfo;

public class LookAndFeelInfoCustom extends LookAndFeelInfo{

	public LookAndFeelInfoCustom(String name, String className) {
		super(name, className);
	}
	
	@Override
	public String toString() {
		return super.getName();
	}
}
