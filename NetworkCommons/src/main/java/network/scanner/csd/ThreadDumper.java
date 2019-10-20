package network.scanner.csd;

import java.io.FileReader;

import org.ini4j.Ini;

public class ThreadDumper implements Runnable{

	public Dumper dumper;
	public String hostFTP;
	public String usr;
	public String psw;
	public int Port;
	public String FILEin; //path file input da copiare
	public String FTPout;  //nome nuovo file output su FTP
	public String folderFTPPath; //path della cartella in cui salvare
	
	public ThreadDumper(String host,String user, String password,int PORT,String Inpath, String outN, String PathFD) {
		hostFTP=host;
		usr=user;
		psw=password;
		Port=PORT;
		FILEin=Inpath;
		FTPout=outN;
		folderFTPPath=PathFD;
	}
	
	public synchronized void run() {
		Ini INI=null;
		try {
			INI= new Ini(new FileReader("CONFIG.ini"));
		}catch (Exception e) {
			System.out.println("Errore nell'apertura di CONFIG.ini");
		}
		
		dumper=new Dumper(hostFTP, usr, psw, Port);
		synchronized (dumper) {
			dumper.SaveRemoteFile(FILEin, FTPout, folderFTPPath,INI );
		}
		
	}
}
