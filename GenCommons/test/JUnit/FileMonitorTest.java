package JUnit;
import java.io.File;
import java.util.ArrayList;

import org.junit.*;

import amfileutils.filechangewatcher.ChangeDTO;
import amfileutils.filechangewatcher.FileChangeMonitor;
import testUtils.TestUtils;

public class FileMonitorTest {
	
	public static final String rootDirPath = "C:\\Users\\xx\\Desktop\\Crypter Test\\monitorTest";
	public static final File file1MonitoredPath = new File(rootDirPath+"\\AAAAAAAA.txt");
	public static final File folder1MonitoredPath = new File(rootDirPath+"\\folder");
	public static final int limit = 600; // 10 minutes
	
	@Test
	public void testFileChangeMonitor(){
		File rootDir = new File(rootDirPath);
		Assert.assertTrue(rootDir.exists());
		TestUtils.printl(rootDir.getAbsolutePath());
		ArrayList<File> monitored = new ArrayList<File>();
		monitored.add(file1MonitoredPath);
		monitored.add(folder1MonitoredPath);
		FileChangeMonitor fileMonitor = new FileChangeMonitor(rootDir, monitored);
		
		int i = 0;
		try{
			while(i < limit){
				TestUtils.print("\n\n ########################  NEW CHECK: ###############################\n\n");
				ChangeDTO changes = fileMonitor.checkForEverithing();
				for(File removed : changes.removedFiles){
					TestUtils.printl("FILE REMOVED:" + removed.getAbsolutePath());
				}
				for(File added : changes.addedFiles){
					TestUtils.printl("FILE ADDED:" + added.getAbsolutePath());
				}
				for(File changed : changes.changedFiles){
					TestUtils.printl("FILE CHANGED:" + changed.getAbsolutePath());
				}
				Thread.sleep(1000);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		TestUtils.printl("\n\n TIME ELAPSED : CLOSING TEST");
	}
}
