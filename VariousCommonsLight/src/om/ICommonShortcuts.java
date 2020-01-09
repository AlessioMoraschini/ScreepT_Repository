package om;

public interface ICommonShortcuts {

	public boolean isCtrlDown();
	public boolean isShiftDown();
	public boolean isAltDown();
	
	public void revalidateUnsaved();
	
	public void saveAsShortcut();
}
