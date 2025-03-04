/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
//package network.scanner.csd;
//
//import org.ini4j.Ini;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.FileReader;
//import java.net.InetAddress;
//import java.nio.channels.FileChannel;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Scanner;
//import java.util.StringTokenizer;
//import java.util.Vector;
//
//import org.jnetpcap.Pcap;  
//import org.jnetpcap.PcapIf;
//import org.jnetpcap.packet.PcapPacket;
//import org.jnetpcap.protocol.network.Ip4;
//import org.jnetpcap.protocol.tcpip.Tcp;
//
//import network.FtpDumper;
//import network.ThreadFtpDumper;
//
//public class Main {
//
//	public static void main(String[] args) {
//		charAnimationPrinter ConsoleAnimated=new charAnimationPrinter();
//		String out_msg=  "*  Benvenuto in csNd, creato da Alessio Moraschini per ottimizzare gli scan della tua rete.  *\n"
//				        +"*  Consulta la guida prima di iniziare...e fanne buon uso! ;)                                *\n";
//	    System.out.println("\n\n**********************************************************************************************");
//		ConsoleAnimated.Print(out_msg, 30);
//		System.out.println("**********************************************************************************************\n");
//		List<PcapIf> alldevs = new ArrayList<PcapIf>();  
//		StringBuilder errbuf = new StringBuilder();  
//
//		//ottengo una lista dei device trovati sulla VM
//		int r = Pcap.findAllDevs(alldevs, errbuf);  
//		//se viene riscontrato un qualche problema il programma si arresta 
//		//e ritorna il messaggio di errore salvato nel buffer degli errori
//		if (r == Pcap.NOT_OK || alldevs.isEmpty()) {  
//			System.err.printf("Impossibile leggere la lista di dispositivi: Errore \n %s", errbuf.toString());  
//        return;
//        }  
//		System.out.println("Dispositivi di rete trovati:\n");
//			
//		int i = 0;  
//		for (PcapIf device : alldevs) {  
//			String description =  (device.getDescription() != null) ? device.getDescription()
//					: "[Nessuna descrizione disponibile]";  
//			System.out.printf("#%d: %s [%s]\n", i++, device.getName(), description);  
//			
//			}  
//        int devAscolto=-1;
//        Scanner input = new Scanner(System.in);
//        while (devAscolto<0||devAscolto>(alldevs.size()-1)) {
//        	try {
//        		System.out.println("\n-> Scegliere un dispositivo: (Digitare il numero corrispondente compreso tra 0 e "+(alldevs.size()-1)+") \n");
//        		devAscolto = Integer.parseInt(input.nextLine());
//        		if (devAscolto<0||devAscolto>alldevs.size()-1) {
//            		System.out.println("\n*** Dispositivo non in lista! ***");
//            	}
//        	}catch(Exception e) {System.out.println("\n*** Digitare un Numero! ***");}
//        	
//        }
//		
//		PcapIf device = alldevs.get(devAscolto); // seleziono il device scelto
//		
//		System.out.println("\n-> Scelto: "+device.getDescription()+"["+device.getName()+"] come Device di ascolto. \n(Puoi cambiarlo riavviando csNd)");
//		
//		//LETTURA CONFIG.ini
//		Ini inifile= new Ini();
//		boolean continua=false;
//		while(!continua) {
//			try {			
//				Ini conFile = new Ini(new FileReader("CONFIG.ini"));
//				inifile=conFile;
//				System.out.println("\n (CONFIG.ini caricato correttamente)\n");
//				continua=true;
//			}catch(Exception e) {System.out.println("\n### Problemi con la lettura del file CONFIG.ini ###\n"
//				+e.getMessage()+" premere invio per riprovare.");
//			}	
//		}
//		
//		String parentFTPdir=inifile.get("FTP_LOGIN", "PathFTP");
//		String host=inifile.get("FTP_LOGIN", "HostFTP");
//		String usr=inifile.get("FTP_LOGIN", "UsrFTP");
//		String hasKEY=inifile.get("FTP_LOGIN", "KeyFTP");
//		String iniPORTS=inifile.get("SPLIT_OPTION", "Port");
//		int splitDumpOnly=Integer.parseInt(inifile.get("FTP_OPTION", "DumpOnlySplit"));
//		int PortFTP=Integer.parseInt(inifile.get("FTP_LOGIN", "PortFTP"));
//		int DumpYN=Integer.parseInt(inifile.get("FTP_OPTION", "DumpFTP"));
//		String threadFLAG=inifile.get("FTP_OPTION", "Threaded_version_Beta");
//		int FLAG_th=0;//flag per attivare disattivare il salvataggio thread TFP
//		if(threadFLAG.equals("1")) {
//			FLAG_th=1;
//			out_msg="\n \n********** Modalit� salvataggio thread FTP attiva ***********\n \n";
//			ConsoleAnimated.Print(out_msg, 20);
//		}else {
//			out_msg="\n \n********** Modalit� salvataggio FTP sequenziale attiva ***********\n \n";
//			ConsoleAnimated.Print(out_msg, 20);
//		}
//
//		try{
//			Thread.sleep(300);
//		}catch(Exception e) {}
//		
//		long iniDurata_cattura;		
//		//Test VALORI _> System.out.println(parentFTPdir+hasKEY+host+usr+iniPORTS+splitDumpOnly+PortFTP+DumpYN);
//		//ottengo le porte indicate nel CONFIG.ini
//		StringTokenizer tokenizer = new StringTokenizer(iniPORTS, ",");
//		int PortNumber=tokenizer.countTokens();		
//		int [] ports=new int[PortNumber];
//		System.out.println("\n"+"rilevate "+PortNumber+" porte da scansionare: \n");
//		for(int j=0;j<PortNumber;j++) {
//			ports[j]=Integer.parseInt(tokenizer.nextToken());
//			System.out.println("porta "+(j+1)+": "+ports[j]);
//			try{
//				Thread.sleep(100);
//			}catch(Exception e) {}
//		}		
//		String ftpKey=new String();
//		//se non setto a 0 il flag salvo solo gli split in remoto(default)
//		if(splitDumpOnly!=0&&DumpYN==1) {
//			   System.out.println("\nI dump non filtrati saranno salvati solo in locale. \n"
//			   		+ "\n-> Per cambiare questa impostazione settare DumpOnlySplit=0 nel file CONFIG.ini");
//		}
//		//Se salvataggio non settato a 1, di default salvo solo in locale
//		if(DumpYN!=1){
//			System.out.println("\nInvio remoto disattivato\n\n-> Per cambiare questa impostazione settare DumpFTP=1"+
//		         " nel file CONFIG.ini\n");
//		}
//		//se il salvataggio ftp � settato da CONFIG
//		if(DumpYN==1){
//			
//			System.out.println("\nlettura path cartella di destinazione su "+host+" (specificata in CONFIG.ini):"+
//			         "\n\n######################\n"+
//					     "### "+parentFTPdir+" ###\n"+
//					     "######################\n");
//			boolean ask=false;//serve per chiedere chiave ftp in caso di errore nella psw in CONFIG.ini				
//			do 
//			{	  
//			   ftpKey=hasKEY;
//			   if(ask) {
//			      System.out.println("\nPer proseguire,individuare e risolvere il problema di connessione e "
//			      		+ "successivamente premere invio. \n\n->");
//			      input.nextLine();
//			    //se premi invio ricarico dati ftp
//			      try {			
//						Ini conFile = new Ini(new FileReader("CONFIG.ini"));
//						inifile=conFile;
//						System.out.println("\n (CONFIG.ini caricato correttamente)\n");
//		     		}catch(Exception e) {System.out.println("\n### Problemi con la lettura del file CONFIG.ini ###\n"
//						+e.getMessage());
//					}
//			      ftpKey=inifile.get("FTP_LOGIN", "KeyFTP");//chiave
//			      host=inifile.get("FTP_LOGIN", "HostFTP");//host
//				  usr=inifile.get("FTP_LOGIN", "UsrFTP");//user
//				  PortFTP=Integer.parseInt(inifile.get("FTP_LOGIN", "PortFTP"));//porta
//			   }//se ask settato o chiave nulla
//			   
//			   
//			 
//	   		   if(FtpDumper.checkFTP(host, usr, ftpKey,PortFTP)==false) {
//				   ask=true;
//				   System.out.println("Password inserita: "+ftpKey+"\n\nErrore nella conessione al server FTP: "
//				   		+host+ "\n\n(Se la password � corretta e la connessione di rete funzionante, Controlla CONFIG.ini)\n"
//				   				+ "Assicurati di non avere un firewall attivo che impedisca la comunicazione con il server\n"
//				   				+ "attraverso la porta "+PortFTP+" (valore specificato in CONFIG.ini)");
//			   }
//		       }while(FtpDumper.checkFTP(host, usr, ftpKey,PortFTP)==false);
//			   System.out.println("\n-> Dati corretti, connesso via ftps(SSL) a "+host+" :D \n \n");
//		}//end IF(DumpFTP==1)
//		//stampa a video durata cattura riconosciuta
//		try {
//			iniDurata_cattura=Long.parseLong(inifile.get("INFO", "CaptureUntil"));
//			System.out.println("CaptureUntil= "+iniDurata_cattura+" (CONFIG.ini) -> Dopo "+iniDurata_cattura
//					+" minuti dall'inizio dello scan csNd si chiuder� automaticamente.\n");
//		}catch(Exception e) {
//			iniDurata_cattura=60;//se input non corretto default 60 minuti
//			System.out.println("CaptureUntil non specificato correttamente in CONFIG.ini. Speficicare un numero intero."
//					            +"\nUtilizzo CaptureUntil= " +iniDurata_cattura+ " minuti di DEFAULT\n");
//		}
//		System.out.println("\n ****  PREMERE INVIO PER INIZIARE LA SCANSIONE  ****\n");
//	    input.nextLine(); //attesa input utente per iniziare lo scan
//		input.close();
//		//creo un'istanza di ftpDumper per il salvataggio file su ftp
//		// definendo host, username e password (non da problemi se non c'� connessione)
//		//siccome dumper inizializza solo i valori dei campi dell'istanza creata.
//		//il codice dell'accesso ftp � attivato solo all'invocazione dei metodi 
//		//check() e saveRemoteFile() )
//		FtpDumper ftpDumper=new FtpDumper(host, usr, ftpKey,PortFTP);
//		//inizializzo la blacklist che manterr� i pacchetti di riferimento
//		//che rappresentano ogni flusso tcp bloccato (richiesta in uscita o
//		//risposta in entrata)
//		Vector <PcapPacket> packetBlacklist = new Vector<PcapPacket>(); 		
//		//ora posso avviare un ciclo di cattura della durata di MinutidiCattura
//		//qui inizier� il while(true) del ciclo cattura-split-dump
//		//specifico il path in cui salvare il dump completo da passare
//		//allo splitter, che si occupa di separarlo per porta
//		String tempDumpPath="Dump/temp";		
//		CaptureCycle ciclo = new CaptureCycle(device, errbuf, inifile,tempDumpPath);
//		//ora inizio il ciclo di cattura pacchetti della durata minuti indicata nel
//		// file ini (se non modificato dati predefiniti). Inizio il ciclo di scansione	
//		// e vado avanti fino a che il programma non viene terminato
//		int contaCicli=0;
//		//inizializza Elapsed, usato per terminare il programma
//		long ELAPSED=System.nanoTime();//variabile per tener conto del tempo trascorso
//		long Limit=ELAPSED+(iniDurata_cattura*60*1000000000);//calcola valore limite di system.currenttime()
//	while(ELAPSED<Limit) { //inizio ciclo cattura e continua fino a tempo esaurito
//		 
//		System.out.println("In ascolto sul device selezionato ("+device.getName()+")...\n\n");
//		String sourcefile=tempDumpPath+"/preDump.cap";
//		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
//		Calendar cal = Calendar.getInstance();
//        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm.ss");
//        String time=sdf.format(cal.getTime());
//        String backupfile="Dump/Local_capture/NOfilter-"+date+"-"+time+".cap";
//		try{  //inizia lo scan e salva il path del dump ottenuto in sourcefile
//			//creo un file uguale al file .cap creato
//	    	//creo un file di backup(nome unico) per conservare il dump ed evitare
//	    	//di perdere i dati quando verr� sovrascritto "preDump.cap"
//	    	//nel prossimo ciclo. 
//	    	//ora creo il backup con nome unico dato da "NOfilter-dd-MM-yyyy-HH.mm.ss.cap"
//			sourcefile=ciclo.startCycle(); //inizia ciclo i-esimo di cattura e ottieni string del file out
//			System.out.println("\n**********************************************************");
//			System.out.println("\n*             Elaborazione Dump non filtrato             *");
//			System.out.println("\n**********************************************************\n");
//			File File_in = new File(sourcefile); 
//			File File_out= new File(backupfile);
//	    	FileInputStream fsin = new FileInputStream(File_in);
//	    	FileOutputStream fsout = new FileOutputStream(File_out);
//	    	FileChannel sourceChannel = fsin.getChannel();    
//	    	FileChannel destChannel = fsout.getChannel();
//	        destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());    
//	        sourceChannel.close();
//	        destChannel.close();
//	        fsin.close();
//	        fsout.close(); 
//	        System.out.println("\n Il file "+backupfile+" ha "+File_out.length()+" Bytes di dati.\n -> Salvato in \"Dump/Local_capture\""); 		
//		}catch(Exception e) {System.out.println(e.getMessage());};
//		//fine del ciclo di cattura	
//		
//		//FASE DI INVIO DEL DUMP NON FILTRATO (Se attivo dumpFTP e se disattiva l'opzione DumpOnlySplit) 
//		
//		//a partire dalla directory madre specificata da PathFTP, indicato nel file CONFIG
//		//si crea una nuova cartella per ogni giorno
//		
//        //se config.ini impostato per         
//        //creazione dei path specifici per questo dump con data e ora:
//        //path del file sorgente da inviare
//	    
//		//nome nuova cartella riferita a data e ora
//		//cartella usata per ex salvataggio in cartelle per ogni giorno 
//		//funzione eliminata- per reinserirla modificare il dumper
//		String folderftpath = parentFTPdir+"/"+date; 
//		
//		String ftpfilename=("NOfilter-"+date+"-"+time+".cap");
//		//nome nuovo file su ftp
//		//se salvataggio FTP attivato e salvataggio solo split disattivato
//		//allora salva su FTP il dump non filtrato prima dello split
//		if (splitDumpOnly==0&&DumpYN==1) {
//			
//			String FTPdir=inifile.get("FTP_LOGIN", "PathFTP");
//		    //salvo il dump su ftp (nome gi� calcolato quindi nomein==nomeout)
//			if(FLAG_th==1) {//se flag thread save attivo
//			
//				synchronized (backupfile) { //esegui questo blocco per un solo file con lo stesso nome alla volta
//					
//					ThreadFtpDumper dumper = new ThreadFtpDumper(host, usr, ftpKey,PortFTP, backupfile, ftpfilename, FTPdir);
//					//prova salvataggio con thread
//					new Thread(dumper).start();//avvia il thread
//				    //file dump non filtrato salvato su server ftp
//				}
//			}else { // Se thread save flag TCP disattivato
//				
//				ftpDumper.uploadFile(backupfile,ftpfilename,FTPdir);
//			}
//			
//	    }
//	
//		System.out.println("\n**********************************************************");
//		System.out.println("\n*                  Inizio dello split!                   *");
//		System.out.println("\n**********************************************************\n");
//		//ora inizia la fase di split
//		//ripeto il ciclo per ogni porta da scansionare
//		for(int z=0;z<ports.length;z++) {
//			//creo un file uguale al file .cap splittato
//	    	//creo un file di backup(nome unico) per conservare il dump ed evitare
//	    	//di perdere i dati quando verr� sovrascritto al successivo ciclo
//	    	//ora creo il backup con nome unico dato da port-PP-date-time.cap
//			String dumpSplit=ciclo.Split(tempDumpPath+"/preDump.cap", ports[z],packetBlacklist);
//			String ftPortoutname="Port-"+ports[z]+"-"+ date +"-"+ time +".cap";
//			String FTPsplitPath="Dump/Local_capture/"+ftPortoutname;
//			File splitFile = new File(dumpSplit); 
//			File backup= new File(FTPsplitPath);	    	
//	    	try {
//	    		FileChannel sourceChannel=null;
//		    	FileChannel destChannel=null;
//		    	FileInputStream fsin = new FileInputStream(splitFile);
//		    	FileOutputStream fsout = new FileOutputStream(backup);
//	            sourceChannel = fsin.getChannel();
//	            destChannel = fsout.getChannel();
//	            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
//	            sourceChannel.close();
//	            destChannel.close();
//	            fsin.close();
//	            fsout.close(); 
//	        }catch(Exception e) {} 
//	    	    	
//	    	System.out.println("\n Il file "+ftPortoutname+" ha "+backup.length()+" Bytes di dati.\n -> Salvato in \"Dump/Local_capture\"");              
//	    	if(DumpYN==1) {//se salvataggio ftp attivato salvo su server ftp
//	    		
//	    		String FTPdir=inifile.get("FTP_LOGIN", "PathFTP");
//	    		
//	    		if(FLAG_th==1) { // thread save attivo
//	    			synchronized (FTPsplitPath) { //esegui questo blocco per un solo thread alla volta
//						
//						ThreadFtpDumper dumperS=new ThreadFtpDumper(host, usr, ftpKey,PortFTP, FTPsplitPath, ftPortoutname, folderftpath);
//						//prova salvataggio con thread
//						new Thread(dumperS).start();//avvia il thread
//					    //file dump non filtrato salvato su server ftp
//					}
//				}else { // thread save disattivato
//					
//					ftpDumper.uploadFile(FTPsplitPath,ftPortoutname,FTPdir);
//				}
//	    		
//	    	}
//	      }	
//		System.out.println("\n**********************************************************");
//		System.out.println("\n*                    Fine dello split!                   *");
//		System.out.println("\n**********************************************************\n");
//		if(ciclo.FLAG_Fcontrol==1) {
//			System.out.println("\nFlussi attivi in blacklist:\n");
//			Tcp tcp=new Tcp();
//			Ip4 ip4=new Ip4();
//			for(PcapPacket p:packetBlacklist) {
//				String PiPs=new String();
//				String PiPd=new String();
//				try {
//					PiPs=InetAddress.getByAddress(p.getHeader(ip4).source()).toString();
//					PiPd=InetAddress.getByAddress(p.getHeader(ip4).destination()).toString();
//				}catch(Exception e) {}
//				System.out.println("\nFlusso "+packetBlacklist.indexOf(p)
//						+ ":   "+p.getHeader(tcp).destination()+" - "+p.getHeader(tcp).source()
//						+" - "+PiPs+" - "+PiPd);
//			}
//		}
//		ELAPSED=System.nanoTime();//aggiorna la variabile di controllo chiusura programmata software
//		                          //e poi torna al controllo iniziale del While
//		contaCicli++;		
//		if(ELAPSED<Limit) {
//			
//			System.out.print("Ciclo"+contaCicli+" ; trascorsi: "+(ELAPSED/1000000000) +" - Limite(iniziale+nanosecondi richiesti): "+(Limit/1000000000)+
//		             "\n-> Mancano "+ ((Limit/1000000000)-(ELAPSED/1000000000)) +" Secondi al termine.\n\n");
//		}				
//	}//uscita dal while di cattura
//	while(Thread.activeCount()>2) {// aspetta completamento FTP
//		try {
//			Thread.sleep(2000);  //aspetta 2 secondi
//			System.out.println("*** Attesa completamento salvataggio FTP ***...\n\n"
//					   + "-> Thread residui in elaborazione: "+(Thread.activeCount()-2));
//		    Thread.sleep(1000); // aspetta 1 secondo
//		} catch (InterruptedException e) {}
//	}
//	System.out.println((Thread.activeCount()-2)+" Thread in lavorazione...\n");
//	try {
//	for(int q=0;q<20;q++){
//		Thread.sleep(100);  //aspetta 0,1 secondi
//		System.out.printf(":) ");
//	    Thread.sleep(100); // aspetta 0,1 second1i	
//	}    
//	    System.out.println(":) \n\n-> csNd terminato!\n\n--------- GRAZIE PER AVER USATO QUESTO SOFTWARE ;) --------\n");
//	    Thread.sleep(300000); // aspetta 5 minuti e chiudi
//	} catch (InterruptedException e) {System.out.println("Exiting");}
//  }//End main
//}//End main Class
