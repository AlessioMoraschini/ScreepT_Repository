package networkscanner.csd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.ftp.FTPClient;
import org.ini4j.Ini;


public class Dumper {
	
	// campi 
	
	public String hostFTP;
	public String usr;
	public String psw;
	public int Port;
	public FTPClient ftpClient;
	public FTPSClient ftps;
	public String protocol = "SSL";
		
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
            System.out.println("\n**** ERRORE DISCONNESSIONE FTPS ****\n");
        }
		return res;
		
	}
	
	
	/**@author Alessio Moraschini
     * 
     *  Questo metodo serve per salvare un file "filename" nella
     *  cartella folderpathname presente nel server ftp, usando i dati specificati
     *  alla creazione dell'oggetto Dumper(host,usr,pssw,port)
     *  Il file viene salvato con il nome specificato in "ftpname"
     * 
     * @return 0 se tutto � andato bene
     * @return 1 se c'� stato un errore
     */  
	public synchronized int SaveRemoteFile(String filename, String ftpname, String folderftpPath, Ini IniFile) {
		String FTPdir=IniFile.get("FTP_LOGIN", "PathFTP");
		//ftpClient = new FTPClient();
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
            	System.out.println("Check della directory:  "+ FTPdir +"\n");
                ftps.makeDirectory(FTPdir);
            }
            ftps.changeWorkingDirectory(FTPdir);
            
            //carico il file usando inputstream         
            File LocalFile = new File(filename); 
            String RemoteFile = ftpname;
            InputStream inputStream = new FileInputStream(LocalFile);    
            System.out.println("\n### Inizio caricamento file "+FTPdir+"/"+RemoteFile+" ###");
            boolean done = ftps.storeFile(RemoteFile, inputStream);
            inputStream.close();
            if (done) {
                System.out.printf(" -> File caricato correttamente!\n");
            }  
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
            return 1;
        } finally {
            try {
                if (ftps.isConnected()) {
                    ftps.logout();
                    ftps.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("\n**** ERRORE DISCONNESSIONE FTPS ****\n");
                return 1;
            }
        }
		return 0;		
	}	
}
