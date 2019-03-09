package gUI.om;

import oshi.hardware.HWDiskStore;

public class HDinfo {
	
	private HWDiskStore disk;
	
	public HDinfo(HWDiskStore disk) {
		this.disk = disk;
	}
	
	public String toString() {
		int lengthMax = 18;
		String outName = (disk.getModel().length()>lengthMax)? disk.getModel().substring(0,lengthMax-1)+"...": disk.getModel() ;
		return outName;
	}

	public HWDiskStore getDisk() {
		return disk;
	}

	public void setDisk(HWDiskStore disk) {
		this.disk = disk;
	}
	
	
}
