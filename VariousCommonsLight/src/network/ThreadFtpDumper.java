package network;

public class ThreadFtpDumper implements Runnable{

	public FtpDumper dumper;
	public String hostFTP;
	public String usr;
	public String psw;
	public int Port;
	public String FILEin; //path file input da copiare
	public String FTPout;  //nome nuovo file output su FTP
	public String folderFTPPath; //path della cartella in cui salvare
	
	public ThreadFtpDumper(String host,String user, String password,int PORT,String Inpath, String outN, String PathFD) {
		hostFTP=host;
		usr=user;
		psw=password;
		Port=PORT;
		FILEin=Inpath;
		FTPout=outN;
		folderFTPPath=PathFD;
	}
	
	public synchronized void run() {
		
		dumper=new FtpDumper(hostFTP, usr, psw, Port);
		synchronized (dumper) {
			dumper.uploadFile(FILEin, FTPout, folderFTPPath );
		}
		
	}
}
