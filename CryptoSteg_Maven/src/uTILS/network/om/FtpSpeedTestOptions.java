package uTILS.network.om;

public enum FtpSpeedTestOptions {	SHORT_TEST("1MB.zip", "1MB upload", 1, 250),
									MED_TEST("5MB.zip", "5MB upload", 5, 1000),
								  	LONG_TEST("20MB.zip", "20MB upload", 20, 4000);

	public static final int MB_multiplier = 1024*1024; // 1 MB
	public static final int KB_multiplier = 1024; // 1 KB
	public static final String HOST_FTP = "speedtest.tele2.net" ;
	public static final String USER_FTP = "anonymous";
	public static final String PSW_FTP = "anonymous"; 
	public static final String UP_FOLDER = "upload";
	
	public String downFileName;
	public String upFileName;
	public int downSize;
	public int upSize;
	
	private FtpSpeedTestOptions(String downFileN, String upFileN, int downSize, int upSize) {
		this.downFileName = downFileN;
		this.upFileName = upFileN;
		this.downSize = downSize * MB_multiplier;
		this.upSize = upSize * KB_multiplier;
	}
}
