package updates;

import java.io.IOException;

import org.junit.Test;

import utils.SysUtils;

public class JarLauncherTest {

	public static String batTestToLaunch = "C:\\Users\\xx\\Desktop\\SCREEPT_LATEST_V1\\ScreepT.exe";
//	public static String batTestToLaunch = "C:\\Users\\xx\\Desktop\\Documenti&Utili\\Informatica varie\\Puppynoid_Build\\V5.0\\Puppynoid.bat";
	
	@Test
	public void testAdminJarLaunch() throws IOException, InterruptedException {
//		SysUtils.launchJarAsAdmin(jarTestToLaunch, "cmd.exe java -jar " + jarTestToLaunch);
		SysUtils.launchExeAsAdmin(batTestToLaunch, batTestToLaunch);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		SysUtils.launchExeAsAdmin(batTestToLaunch, batTestToLaunch);
		
	}
}
