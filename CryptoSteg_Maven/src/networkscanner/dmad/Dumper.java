package networkscanner.dmad;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPSClient;


public class Dumper {
	
	// campi 
	
	public String hostFTP;
	public String usr;
	public String psw;
	public int Port;
	//public FTPClient ftpClient;
	public FTPSClient ftps;
	public String protocol = "TLS";
		
	//costruttori
	 
	public Dumper(String host,String user, String password,int PORT) {
		this.hostFTP=host;
		this.usr=user;
		this.psw=password;
		this.Port=PORT;
	}
	
	//metodi
	
	public synchronized static boolean CheckFTP(String server,String usr,String inskey,int port) {
		//FTPClient ftpClient = new FTPClient();
		FTPSClient ftps = new FTPSClient();
		boolean res=false;
		try {
			ftps.connect(server);
            res= ftps.login(usr, inskey);   
            }catch(Exception e){
			return false;
		}
		try {
            if (ftps.isConnected()) {
                ftps.logout();
                ftps.disconnect();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
		return res;
		
	}
	
	// metodo per il salvataggio da server su cartella locale
	public boolean saveLocalFile(String FTPdir,String RemoteName, String localOutputPath) {
		
		ftps=new FTPSClient(protocol);
        try {
            ftps.connect(this.hostFTP, this.Port);
            ftps.login(this.usr, this.psw);
            ftps.enterLocalPassiveMode();
            ftps.setFileType(FTP.BINARY_FILE_TYPE);
            //testa esistenza cartella specificata in CONFIG.ini)
            ftps.changeWorkingDirectory(FTPdir);
            int returnCodeA = ftps.getReplyCode();
            if (returnCodeA == 550) {//se la directory specificata 
            	return false;            
            }        
            File LocalFile = new File(localOutputPath); 
            OutputStream outputStream = new FileOutputStream(LocalFile);    
            //System.out.println("\n### Inizio download file: \n"+FTPdir+"/"+RemoteFile+" ###");
            boolean done = ftps.retrieveFile(RemoteName, outputStream);   
            outputStream.close();
            return done;
            
        } catch (IOException ex) {
             return false;
        } finally {
            try {
                if (ftps.isConnected()) {
                    ftps.logout();
                    ftps.disconnect();
                }
            } catch (IOException ex) {
                
            }
        }		
     
	}
	//metodo che ritorna un array contenente i file rilevati nella directory FTPdir
	public String[] GetRemoteFiles(String FTPdir){
		String [] alldumps;
		ftps=new FTPSClient(protocol);
        try {
            ftps.connect(this.hostFTP, this.Port);
            ftps.login(this.usr, this.psw);
            ftps.enterLocalPassiveMode();
            ftps.setFileType(FTP.BINARY_FILE_TYPE);
            //testa esistenza cartella specificata in CONFIG.ini)
            ftps.changeWorkingDirectory(FTPdir);
            int returnCodeA = ftps.getReplyCode();
            if (returnCodeA == 550) {//se la directory specificata in ini non esiste
            	return null;            
            }        
            alldumps = ftps.listNames(FTPdir);
            
            return alldumps;
        } catch (IOException ex) {
             return null;
        } finally {
            try {
                if (ftps.isConnected()) {
                    ftps.logout();
                    ftps.disconnect();
                }
            } catch (IOException ex) {
                return null;
            }
        }		
	}	
}
