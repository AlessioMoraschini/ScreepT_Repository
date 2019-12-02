package network.om;

import resources.GeneralConfig;

public enum FtpSpeedTestOptions {	SHORT_TEST(GeneralConfig.DOWN_SHORT_FILE, GeneralConfig.UP_SHORT_FILE, GeneralConfig.DOWN_SHORT_FILE_SIZE_MB, GeneralConfig.UP_SHORT_FILE_SIZE_KB),
									MED_TEST(GeneralConfig.DOWN_MED_FILE, GeneralConfig.UP_MED_FILE, GeneralConfig.DOWN_MED_FILE_SIZE_MB, GeneralConfig.UP_MED_FILE_SIZE_KB),
								  	LONG_TEST(GeneralConfig.DOWN_BIG_FILE, GeneralConfig.UP_BIG_FILE, GeneralConfig.DOWN_BIG_FILE_SIZE_MB, GeneralConfig.UP_BIG_FILE_SIZE_KB);

	public static final int MB_multiplier = 1024*1024; // 1 MB
	public static final int KB_multiplier = 1024; // 1 KB
	public static final String HOST_FTP = GeneralConfig.FTP_HOST;
	public static final String USER_FTP = GeneralConfig.FTP_USER;
	public static final String PSW_FTP = GeneralConfig.FTP_PSW; 
	public static final String UP_FOLDER = GeneralConfig.FTP_UPLOAD_FOLDER;
	
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
