package junit;

import java.io.IOException;
import java.util.Arrays;

import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.DirUsage;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.junit.Test;

import oshi.SystemInfo;
import oshi.jna.platform.windows.PowrProf.SystemBatteryState;
import uTILS.local.FileStuff;

public class HwTest {

	private static final String dir = "TEST_FILES";
	private Sigar sigar;
	private Cpu cpu;
	private Cpu[] cpus;
	private CpuInfo[] cpuInfos;
	private DirUsage dirUsage;
	private Mem memory; 
	private FileSystem[] FSList;
	private SystemBatteryState battery;
	private SystemInfo sysINFO;
	
	@Test
	public void testHwInfo() throws IOException, SigarException {
		
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

		// CPU INFO
		titlePrint(" CPU INFO ");
		int i = 1;
		print("Vendor: " + cpuInfos[0].getVendor());
		print("Model: " + cpuInfos[0].getModel());
		print("N of Cores: " + cpuInfos[0].getTotalCores());
		print("N of sockets: " + cpuInfos[0].getTotalSockets());
		print("Cores x socket: " + cpuInfos[0].getCoresPerSocket());
		for(CpuInfo info :cpuInfos) {
			titlePrint(" CPU "+i+" INFO ");
		
			print("Mhz: " + info.getMhz());
			print("Cache size: " + info.getCacheSize());
			
			i++;
		}
		
		// oshi cpu
		titlePrint(" OSHI CPU INFO ");
		print(sysINFO.getHardware().getProcessor().toString());
		
		// DIRECTORY STATS
		titlePrint(" /"+ dir +" ");
		
		print("N of files: " + dirUsage.getFiles());
		print("N of dirs contained: " + dirUsage.getSubdirs());
		print("Disk usage: " + FileStuff.coolFileSize(dirUsage.getDiskUsage()));
		
		// RAM INFO
//		int ii=0;
//		while (i<20) {
			memory = sigar.getMem();
			titlePrint(" Memory Info ");
			print("Used: " + FileStuff.coolFileSize(memory.getActualUsed()));
			print("Free: " + FileStuff.coolFileSize(memory.getActualFree()));
			print("% Used: " + Math.round(memory.getUsedPercent()));
			print("% Free: " + Math.round(memory.getFreePercent()));
			print("Ram: " + memory.getRam());
//			ii++;
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				print("!! Memory analisys INTERRUPTED !!");
			} 
//		}

			
		// FILE SYSTEM INFO
		titlePrint(" File System Info ");
		for(FileSystem FS : FSList) {
			separatorPrint();
			print(FS.toString());
			print("Name: " + FS.getDevName());
			print("Dir: " + FS.getDirName());
			print("Flags: " + FS.getFlags());
			print("Options: " + FS.getOptions());
			print("Sys-Type: " + FS.getSysTypeName());
			print("Type: " + FS.getTypeName());
		}
		separatorPrint();
		
	}
	
	private void init() throws SigarException {
		sigar = new Sigar();
		cpu = sigar.getCpu();
		cpus = sigar.getCpuList();
		cpuInfos = sigar.getCpuInfoList();
		dirUsage = sigar.getDirUsage(dir);
		memory = sigar.getMem();
		FSList = sigar.getFileSystemList();
		sysINFO = new SystemInfo();
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
