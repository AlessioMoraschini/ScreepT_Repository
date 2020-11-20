package junit.GUI;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSFileStore;
import runtime.RuntimeUtil;
import utility.manipulation.ConversionUtils;

public class HwTest {

//	private static final String dir = "TEST_FILES";
	private CentralProcessor cpu;
	private GlobalMemory memory; 
//	private SystemBatteryState battery;
	private SystemInfo sysINFO;
	
	@Test
	public void testHwInfo() throws IOException {
		
		init();
		
		// SYSTEM INFO
		titlePrint(" OS Info ");
		print("OS name: " + System.getProperty("os.name"));
		print("OS version: " + System.getProperty("os.version"));
		print("OS architecture: " + System.getProperty("os.arch"));
		print("Java version: " + System.getProperty("java.version"));

		print(sysINFO.getHardware().getDisplays()[0].getEdid());
		print("Monitors: " + sysINFO.getHardware().getDisplays()[0]);
		//print("Sound cards: " + Arrays.toString(sysINFO.getHardware().getSoundCards()));
		print("USB vendor: " + sysINFO.getHardware().getUsbDevices(false)[0].getVendor());
		print("MOBO: " + sysINFO.getHardware().getComputerSystem().getBaseboard().getManufacturer());
		print("Firmware: " + sysINFO.getHardware().getComputerSystem().getFirmware().getDescription());
		print("HD model: " + sysINFO.getHardware().getDiskStores()[0].getModel());
		print("USB : " + Arrays.toString(sysINFO.getHardware().getUsbDevices(false)));
		print("HD READ speed: " + sysINFO.getHardware().getDiskStores()[0].getReadBytes());
		print("HD WRITE speed: " + sysINFO.getHardware().getDiskStores()[0].getWriteBytes());

		// oshi cpu
		titlePrint(" CPU INFO ");
		print("Vendor: " + cpu.getVendor());
		print("Identifier: " + cpu.getIdentifier());
		print("Model: " + cpu.getModel());
		print("Name: " + cpu.getName());
		print("Load: " + ConversionUtils.getPercentString(cpu.getSystemCpuLoad()));
		print("Family: " + cpu.getFamily());
		print("N of logical cores: " + cpu.getLogicalProcessorCount());
		print("N of Cores: " + cpu.getPhysicalProcessorCount());
		print("N of sockets: " + cpu.getPhysicalPackageCount());
		print("Frequency: " + ConversionUtils.coolFrequency(cpu.getVendorFreq()));
		
		titlePrint(" OSHI CPU INFO ");
		print(sysINFO.getHardware().getProcessor().toString());
		
		print("Current process - memory Used: " + RuntimeUtil.getUsedMemoryString());
		print("Current process - memory available: " + RuntimeUtil.getFreeMemoryString());
		print("Current process - Percentage of memory max: " + RuntimeUtil.getMaxMemoryString());
		print("Current process - Percentage of memory used: " + RuntimeUtil.getPercentageUsedFormatted());
		print("Current process - Percentage of memory used on System: " + RuntimeUtil.getPercMemoryOnTotalSystem(memory.getTotal()));

		print("Current process - Percentage of CPU used: " + RuntimeUtil.getCurrentProcCpuUsageString());
		
		// RAM INFO
//		int ii=0;
//		while (i<20) {
		long memoryTot = memory.getTotal();
		long memoryFree = memory.getAvailable();
		long memoryUsed = memoryTot - memoryFree;
				
		
		titlePrint(" RAM Memory Info ");
		print("Total: " + ConversionUtils.coolFileSize(memoryTot));
		print("Used: " + ConversionUtils.coolFileSize(memoryUsed));
		print("Free: " + ConversionUtils.coolFileSize(memoryFree));
		print("% Used: " + ConversionUtils.getPercent(memoryUsed, memoryTot));
		print("% Free: " + ConversionUtils.getPercent(memoryFree, memoryTot));
//			ii++;
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			print("!! Memory analisys INTERRUPTED !!");
		} 
//		}

			
		// FILE SYSTEM INFO
		titlePrint(" File System Info ");
		for(OSFileStore FS : sysINFO.getOperatingSystem().getFileSystem().getFileStores()) {
			separatorPrint();
			print(FS.toString());
			print("Name: " + FS.getName());
			print("Dir: " + FS.getMount());
			print("Volume: " + FS.getVolume());
			print("Total space: " + FS.getTotalSpace());
			print("Usable space: " + FS.getUsableSpace());
			print("Logical volume: " + FS.getLogicalVolume());
			print("Type: " + FS.getType());
		}
		separatorPrint();
		
		// Network with OSHI
		NetworkIF[] interfaces = sysINFO.getHardware().getNetworkIFs();
		for(NetworkIF newtInterface : interfaces) {
			print(newtInterface.getDisplayName());
		}
		
	}
	
	private void init() {
		sysINFO = new SystemInfo();
		cpu = sysINFO.getHardware().getProcessor();
		memory = sysINFO.getHardware().getMemory();
	}
	// lazy printer
	private void print(Object src) {
		System.out.println(src);
	}
	
	private void separatorPrint() {
		print("\n   ###################################################\n");
	}
	
	private void titlePrint(String title) {
		int n = 60;
		int l = title.length()/2;
		String out="\n";
		for(int i = 0; i<n; i++) out+=("#");
		out+="\n";
		for(int i = 0; i<(n/2)-l; i++) out+=("#");
		out+=title;
		for(int i = 0; i<(n/2)-l; i++) out+=("#");
		out+="\n";
		for(int i = 0; i<n; i++) out+=("#");
		print(out+"\n");
	}
	
}
