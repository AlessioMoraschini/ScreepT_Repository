package junit.netscanner;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import org.ini4j.Ini;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;

import networkscanner.csd.CaptureCycle;

public class ProvaSplit {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("Benvenuto in csNd, creato da Alessio Moraschini"
				+ " per ottimizzare gli scan della tua rete. \n"
				+ "Guarda la guida prima di iniziare. \n"
				+ "E...fanne buon uso! ;)\n \n ");	
		
		List<PcapIf> alldevs = new ArrayList<PcapIf>();  
		StringBuilder errbuf = new StringBuilder();  

		//ottengo una lista dei device trovati sulla VM
		int r = Pcap.findAllDevs(alldevs, errbuf);  
		//se viene riscontrato un qualche problema il programma si arresta 
		//e ritorna il messaggio di errore salvato nel buffer degli errori
		if (r == Pcap.NOT_OK || alldevs.isEmpty()) {  
			System.err.printf("Impossibile leggere la lista di dispositivi: Errore \n %s", errbuf.toString());  
        return;
        }  

		System.out.println("Dispositivi di rete trovati:");  
		int i = 0;  
		for (PcapIf device : alldevs) {  
			String description =  (device.getDescription() != null) ? device.getDescription()
					: "[Nessuna descrizione disponibile]";  
			System.out.printf("#%d: %s [%s]\n", i++, device.getName(), description);  
			}  

		System.out.println("\n scegliere un dispositivo (digitare: 0 - "+(alldevs.size()-1)+") \n");
		Scanner input = new Scanner(System.in);
		int devAscolto = Integer.parseInt(input.nextLine());
		PcapIf device = alldevs.get(devAscolto); // seleziono il device scelto
		System.out.printf("\n scelto "+device.getName()+"come predefinito",  
				(device.getDescription() != null) ? device.getDescription()  
						: device.getName());  
		Ini inifile= new Ini();
		try {
			
			Ini conFile = new Ini(new FileReader("CONFIG.ini"));
			inifile=conFile;
			System.out.println(" (Ini path Caricato)");
		}catch(Exception e) {System.out.println("Problemi con la lettura del file ini"
			+e.getMessage());
		}	
		
		String iniPORTS=inifile.get("SPLIT_OPTION", "Port");
		//ottengo le porte indicate nel CONFIG.ini
		StringTokenizer tokenizer = new StringTokenizer(iniPORTS, ",");
		int PortNumber=tokenizer.countTokens();		
		int [] ports=new int[PortNumber];
		System.out.println("\n"+"rilevate "+PortNumber+" porte da scansionare: \n");
		for(int j=0;j<PortNumber;j++) {
			ports[j]=Integer.parseInt(tokenizer.nextToken());
			System.out.println("porta "+(j+1)+": "+ports[j]);
		}		
		
		input.close();
		//creo un'istanza di ftpDumper per il salvataggio file su ftp
		// definendo host, username e password	
		Vector<PcapPacket> packetBlacklist=new Vector<PcapPacket>();				
		//ora posso avviare un ciclo di cattura della durata di MinutidiCattura
		//qui inizier� il while(true) del ciclo cattura-split-dump
		//specifico il path in cui salvare il dump completo da passare
		//allo splitter, che si occupa di separarlo per porta
		String tempDumpPath="Dump";
		CaptureCycle ciclo = new CaptureCycle(device, errbuf, inifile,tempDumpPath);
		String saved=ciclo.Split(tempDumpPath+"/"+"24-02-2018 @ 15.37.14 noFilter"+".cap", 62874,packetBlacklist);
		System.out.println("\n**********************************************************");
		System.out.println("\n*                    Fine dello split!                   *");
		System.out.println("\n**********************************************************");
		System.out.println("\nfile splittato e salvato in: "+saved);
		System.out.println(ciclo.Fcontrol+"\n\nMAC DEVICE: " + ciclo.local_MAC);
		System.out.println("\nFlussi attivi in blacklist:\n");
		Tcp tcp=new Tcp();
		Ip4 ip4=new Ip4();
		for(PcapPacket p:packetBlacklist) {
			String PiPs=new String();
			String PiPd=new String();
			try {
				PiPs=InetAddress.getByAddress(p.getHeader(ip4).source()).toString();
				PiPd=InetAddress.getByAddress(p.getHeader(ip4).destination()).toString();
			}catch(Exception e) {}
			System.out.println("Flusso "+packetBlacklist.indexOf(p)
					+ ":   "+p.getHeader(tcp).destination()+" - "+p.getHeader(tcp).source()
					+" - "+PiPs+" - "+PiPd);
		}
	}

}
