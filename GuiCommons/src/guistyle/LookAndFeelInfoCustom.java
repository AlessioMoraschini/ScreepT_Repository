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
	
	public boolean isJGoodies() {
		return this.getName().toLowerCase().contains("plastic");
	}
	
	public boolean isMetal() {
		return this.getName().toLowerCase().contains("metal");
	}
	
	public boolean isNimbus() {
		return this.getName().toLowerCase().contains("nimbus");
	}
}
