package network.scanner.csd;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapDumper;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Vector;
import java.util.regex.Pattern;

import org.ini4j.*;

public class CaptureCycle {

	// CAMPI DELLA CLASSE
	
	String local_ip;
	public String Fcontrol; //stringa contenente l'espressione regolare di filtro inserita nel file ini
	public int FLAG_Fcontrol;  // valore intero reappresentante filtro flusso ON/OFF
	public String local_MAC;  // contiene l'indirizzo MAC del device di cattura
	public String dumPath;  // contiene il path relativo a cui viene salvato il .cap
	public PcapIf dEV;     // contiene il device per la cattura
	public int minuti;    // contiene i minuti di scansione richiesti
    public int maxpLenght;  // contiene la massima dimensione del pacchetti
    public int timeOut;   // contiene il timeout per lo svuotamento del buffer
    public int npack;    // contatore pacchetti del ciclo in corso
    public int flag;     // contiene il flag per la modalit� promiscua
    public String deviceName;  // contiene il nome del device di cattura specificato
    public StringBuilder errBuffer; // contiene il buffer degli errori relativo a Pcap
    
    // COSTRUTTORI DELLA CLASSE
      
    public CaptureCycle(PcapIf dev, StringBuilder buff,Ini inifile, String path) {
    	
    	try {
			this.local_MAC = Arrays.toString(dev.getHardwareAddress());
		}catch(Exception e) {}
		//TEST System.out.println("MAC device: "+ local_MAC);
    	try {
    		this.local_ip="/"+InetAddress.getLocalHost().getHostAddress();
    	}catch(Exception e) {}
    	//inizializazione espressione controllo/controllo flusso
    	Fcontrol=inifile.get("SPLIT_OPTION", "FlowControl");
    	if(Fcontrol.equals("1")) {
    		this.FLAG_Fcontrol=1;
    	}else if(Fcontrol.equals("0")){
    		this.FLAG_Fcontrol=0;
    	}else {
    		this.FLAG_Fcontrol=2;
    	}
    	this.deviceName = dev.getName();
    	this.errBuffer = buff;
    	this.dumPath=path;
    	//leggo modalit� di scan da file .ini
    	int prom=Integer.parseInt(inifile.get("INFO", "Promisc"));
    	if(prom!=Pcap.MODE_PROMISCUOUS)//se il valore � diverso da 1
    		this.flag = Pcap.MODE_NON_PROMISCUOUS;
    	if(prom==Pcap.MODE_PROMISCUOUS)//se settato a 1
    		this.flag = Pcap.MODE_PROMISCUOUS;
      	//leggo la durata del ciclo di scan da file .ini
    	try {
    		this.minuti=Integer.parseInt(inifile.get("INFO", "Cycle")); 
    	}catch(Exception e) {
    		this.minuti=10;   //se valore non settato setto 10 minuti come predefinito
     	}   	
    	// gli altri valori vengono settati ai predefiniti
    	this.timeOut = 1000; //1000 millisecondi=1 secondo massimo ritardo di dispatch jmemory
    	this.maxpLenght = 64 * 1024;//pacchetto intero (64 KByte)
    }
    
       // METODI DELLA CLASSE
    
    /**@author Alessio Moraschini
     * 
     *  Questo metodo pu� essere usato per catturare pacchetti
     *  fino a che sono passati i minuti indicati nel campo this.minuti
     *  I pacchetti vengono salvati in un unico dump, specificato nel campo 
     *  this.dumpPath. 
     * 
     * @return 0 se tutto � andato bene
     * @return 1 se c'� stato un errore
     * @throws IOException 
     */    
    public String startCycle() throws IOException {
    	this.npack=0;
    	Pcap pcap = Pcap.openLive(this.deviceName, this.maxpLenght, this.flag, this.timeOut, this.errBuffer);
    	if (pcap == null) {  
    	    System.err.printf("Errore nell'apertura del device di cattura: \n \n" 
    	         	+ this.errBuffer.toString()+"\n \n");
    	    	   return (this.dumPath+"/preDump.cap");       
    	}  
    	
    	//variabile che indica quando chiudere la cattura (se true deve uscire dal ciclo)
    	boolean timeToStop=false;
    	String ofile = this.dumPath+"/preDump.cap";  // output file che conterr�
    	PcapDumper dumper = pcap.dumpOpen(ofile); // il dump intero da splittare
    	                                             	
    	//variabile in cui viene salvato il tempo in nanosecondi(inizio scan)
    	long Init= System.nanoTime();
    	    	   	
    	// inizio un loop che cattura 1 pacchetto per volta
    	// e continuo fino a che sono passati this.minuti dall'assegnamento
    	// di nanoTime() a Init;
    	int cont=0;
    	while(!timeToStop) {
    		pcap.loop(1, dumper);// ad ogni iterazione del while scrivo 
            // un pacchetto sul dump, e cos� via fino alla fine del ciclo 
    		cont++;
    		this.npack=cont;
	    	if((System.nanoTime()-Init)/1000000000>this.minuti*60) {
	    		System.out.println("CICLO FINITO (trascorsi "+
	    	            (System.nanoTime()-Init)/1000000000+" secondi dall'inizio del ciclo)");
	    		timeToStop=true;
	    		
	     	}
	    	if(!timeToStop) {
	    		System.out.println("Pacchetto in transito rilevato: (durata ciclo richiesta:" + this.minuti*60 +
	    				" - secondi trascorsi: "+(System.nanoTime()-Init)/1000000000+") -> Continua a catturare!");
		    	System.out.println("Pacchetto numero "+cont);
	    		
	    	}
	    	
    	}//fine del ciclo di ricezione e dump pacchetti
    	 // ora scrivo il dump    	             
    	dumper.close(); // Won't be able to delete without explicit close     	  
    	//chiudo il pcap generato inizialmente per la cattura
    	pcap.close();    	
        return ofile;
    }
    
    /**@author Alessio Moraschini
     * 
     *  Questo metodo serve per ricavare un file filtrato per porta 
     *  a partire da un dump precedente catturato con startCycle()
     *  Si comporta in maniera simile a Capture Cycle, ma questa volta
     *  leggendo da file .cap invece che device, non salva backup in locale 
     *  con nomi distinti, quindi ad ogni successivo avvio per una stessa porta
     *  il file portDump.cap viene sovrascritto. Deve perci� essere gestito
     *  dall'utilizzatore per far si che ci� non accada, salvando da qualche parte
     *  il valore temporaneo prima di procedere con il successivo scan di quella
     *  porta (Come per il dump non filtrato).
     *  
     * 
     * @return splitted 
     * rappresenta il percorso del file contenente lo split ottenuto
     * @throws IOException 
     * da gestire per intercettare eventuali problemi nell'IO
     */
    public String Split(String sourceDumpPath, int portFilter,Vector <PcapPacket> packetList){
		String splitted =this.dumPath+"/Port-"+portFilter+"-localDump.cap";
	    final Pcap splitter = Pcap.openOffline(sourceDumpPath, this.errBuffer);
		if (splitter == null) {
			System.err.println(this.errBuffer); // Error is stored in errbuf if any
			return "Error"+splitted;
		}//ora inizializzo il dumper per salvare i pacchetti in un nuovo .cap		
		PcapDumper splitdumper=splitter.dumpOpen(splitted);
		//inizio del loop di scan per portfilter su qeusto pc
		//(definisco in handler e in nextpacket algoritmo)
		splitter.loop(Pcap.DISPATCH_BUFFER_FULL, new JPacketHandler<StringBuilder>() {
            //inizializzo il tipo dei pacchetti di riferimento che user� per 
			//riconoscere se salvare il pacchetto in nextpacket()
			final Tcp tcp = new Tcp();
            final Ip4 ip4=new Ip4();
			final Udp udp = new Udp();
			final Ethernet eth=new Ethernet();
			
			public void nextPacket(JPacket packet, StringBuilder errbuf) {
				
				final PcapPacket pa = new PcapPacket(packet);
				
				if (packet.hasHeader(Tcp.ID)&&packet.hasHeader(ip4)&&packet.hasHeader(eth)) //Pacchetto con header tcp e header ipv4
				{					
					packet.getHeader(tcp);
					
					//caso in cui il pacchetto � in transito dalla porta richiesta
					if(tcp.source()==portFilter)  
					{
						packet.getHeader(ip4);
						packet.getHeader(eth);									
						String packetsrcIP=new String();
						try {
							packetsrcIP=InetAddress.getByAddress(ip4.source()).toString();
						}catch(Exception e) {}
													
						    //se inoltre indirizzo ip sorgente corrisponde a questo pc - > in uscita dalla porta di questo pc
						    if (local_MAC.equals(Arrays.toString(eth.source()))||local_ip.equals(packetsrcIP))
				    	    {
						    	//ricavo il quartetto che descrive il pacchetto corrente
						    	byte[] SRCip=ip4.source();
						    	byte[] DSTip=ip4.destination();
						    	int SRCp=tcp.source();
						    	int DSTp=tcp.destination();
						    	
						    	if (FLAG_Fcontrol==0)
						    	{  //se controllo di flusso disattivato						    		
									System.out.println("\nFlow control disattivato - Pacchetto TCP in uscita dalla porta "+portFilter+" \nSource IP: "+ local_ip);
					    		    System.out.println("frame #"+ packet.getFrameNumber());
					    		    System.out.println("tcp.dst_port="+ tcp.destination());
									System.out.println("tcp.src_port="+ tcp.source());
									System.out.println("tcp.ack="+ tcp.ack());
									splitdumper.dump(pa.getCaptureHeader(), packet);  // salva il pacchetto nel dump
									
								}else if (FLAG_Fcontrol==1) {  //se controllo di flusso attivo
									

					    		    System.out.println("\nFlow control attivo - Pacchetto TCP in uscita dalla porta "+portFilter+" \nSource IP: "+ local_ip);
					    		    System.out.println("frame #"+ packet.getFrameNumber());
					    		    System.out.println("tcp.dst_port="+ tcp.destination());
									System.out.println("tcp.src_port="+ tcp.source());
									System.out.println("tcp.ack="+ tcp.ack());
									//ora controllo se per ogni pacchetto in blacklist il quartetto destinatario-sorgente corrisponde
									//con quello del pacchetto corrente. Se corrisponde setta inBlacklist a true.
									boolean inBlacklist=false;
									PcapPacket blacklistPnt=null;
									for(PcapPacket iter:packetList) {
										if(iter.getHeader(tcp).source()==SRCp && Arrays.equals(iter.getHeader(ip4).source(), SRCip)) 
											if(iter.getHeader(tcp).destination()==DSTp && Arrays.equals(iter.getHeader(ip4).destination(), DSTip)) {
												inBlacklist=true;
												blacklistPnt=iter;
											}
												
										//controllo se dest corrisponde a source sia per IP che porta, per aggiungere anche le comunicazioni
										//in senso opposto
										if(iter.getHeader(tcp).source()==DSTp && Arrays.equals(iter.getHeader(ip4).source(), DSTip)) 
											if(iter.getHeader(tcp).destination()==SRCp && Arrays.equals(iter.getHeader(ip4).destination(), SRCip)) {
												inBlacklist=true;
												blacklistPnt=iter;
											}														
									}//fine scansione blacklist (se il pacchetto � associato a un flusso in black list setto inBlacklist=true)
									
									//inizia la fase di riconoscimento dei flussi, e blocco traffico generato in uscita 
									//dalla vulbox (e relativo traffico di risposta <=> stesso flusso tcp <=> stesso quartetto ID)
									//memorizzo i flussi da scartare quando riconosco un SYN(da questo ip),
									//oppure SYN-ACK. li rimuovo quando leggo un FIN
									if(tcp.flags_SYN()&&!tcp.flags_ACK()&&!inBlacklist) {
										 
										packetList.add(pa);
										System.out.println("rilevato traffico generato da "+ local_ip+"\n-> Pacchetto scartato e aggiunto in blacklist");
						            //se rilevo che questo pc sta chiedendo di instaurare un flusso
		                            // tcp, invece di inviare il pacchetto lo salvo nel vector dei pacchetti che rappresentano 
					                //i quartetti da scartare(di riferimento)
									}
									//se pacchetto corrisponde a flusso in blacklist scartalo
									if(inBlacklist) {
										System.out.println("Rilevato pacchetto in uscita in blacklist -> scartato");
									}
									//se invece sta mandando altro traffico && non � presente nella blacklist
									//allora salva il pacchetto 
									if(!inBlacklist && !tcp.flags_SYN()) {
										splitdumper.dump(pa.getCaptureHeader(), packet);  // salva il pacchetto nel dump
						    		    System.out.println("Pacchetto non correlato a traffico generato dalla vulnbox -> conservato");
									}
									
									//se FIN() � settato a true allora flusso tcp chiuso -> libera il valore dalla blacklist
									if(tcp.flags_FIN()&& inBlacklist) {
										
										packetList.remove(blacklistPnt);
										System.out.println("Connessione in blacklist chiusa, rimossa dalla blacklist per FIN=true");
									}
									
								} // fine if controllo di flusso attivo		
								
								else if(FLAG_Fcontrol==2) {//se modalit� analisi espressione regolare
									   
							   			String payload=new String(tcp.getPayload());
							    		if (Pattern.matches(Fcontrol, payload)) 
							    		{
							    			System.out.println("\n!!!! Corrispondenza trovata nella TCP payload del pacchetto Numero: "+packet.getFrameNumber()+" !!!!");
							    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
							    		}else {
							    			System.out.println("\nCorrispondenza non trovata nel pacchetto Numero: "+packet.getFrameNumber());
							    		}
								}//fine if controllo espressione regolare
				    	    } // fine if IPsrc=Local_IP				    												
					} //fine if TCP src=PortFilter
					
					//caso in cui il traffico � in entrata dalla porta in analisi(devo anche controllare che il destinatario sia la vulnbox)
					//controllo porta destinazione = a portfilter
					if(tcp.destination()==portFilter)  
					{
						packet.getHeader(ip4);
						String packetdstIP=new String();
						try {
							packetdstIP=InetAddress.getByAddress(ip4.destination()).toString();
						}catch(Exception e) {}
													
						    //se inoltre indirizzo ip destinatario corrisponde a questo pc - > in ingresso dalla porta di questo pc
						    if (local_MAC.equals(Arrays.toString(eth.destination()))||local_ip.equals(packetdstIP))
				    	    {
						    	byte[] SRCip=ip4.source();
						    	byte[] DSTip=ip4.destination();
						    	int SRCp=tcp.source();
						    	int DSTp=tcp.destination();
						    	
						    	if (FLAG_Fcontrol==0) {  //se controllo di flusso disattivato
									
									System.out.println("\nFlow control disattivato - Pacchetto TCP in entrata dalla porta "+portFilter+" \nSource IP: "+ local_ip);
					    		    System.out.println("frame #"+ packet.getFrameNumber());
					    		    System.out.println("tcp.dst_port="+ tcp.destination());
									System.out.println("tcp.src_port="+ tcp.source());
									System.out.println("tcp.ack="+ tcp.ack());
									splitdumper.dump(pa.getCaptureHeader(), packet);  //salva il pacchetto nel dump
									
								}else if (FLAG_Fcontrol==1) {  //se controllo di flusso attivo
								

					    		    System.out.println("\nFlow control attivo - Pacchetto TCP in entrata dalla porta "+portFilter+"\nDestination IP: "+ local_ip);
					    		    System.out.println("frame #"+ packet.getFrameNumber());
					    		    System.out.println("tcp.dst_port="+ tcp.destination());
									System.out.println("tcp.src_port="+ tcp.source());
									System.out.println("tcp.ack="+ tcp.ack());
					    		    //splitdumper.dump(pa.getCaptureHeader(), packet);
					    		    				    		  
									//ora controllo se per ogni pacchetto in blacklist il quartetto corrisponde con quello del
									//pacchetto corrente. Se corrisponde setta inBlacklist a true.
									boolean inBlacklist=false;
									PcapPacket blacklistPnt=null;
									for(PcapPacket iter:packetList) {
										if(iter.getHeader(tcp).source()==SRCp && Arrays.equals(iter.getHeader(ip4).source(), SRCip)) 
											if(iter.getHeader(tcp).destination()==DSTp && Arrays.equals(iter.getHeader(ip4).destination(), DSTip)) {
												inBlacklist=true;
												blacklistPnt=iter;
											}
												
										//controllo se dest corrisponde a source sia per IP che porta, per aggiungere anche le comunicazioni
										//in senso opposto
										if(iter.getHeader(tcp).source()==DSTp && Arrays.equals(iter.getHeader(ip4).source(), DSTip)) 
											if(iter.getHeader(tcp).destination()==SRCp && Arrays.equals(iter.getHeader(ip4).destination(), SRCip)) {
												inBlacklist=true;
												blacklistPnt=iter;
											}														
									}//fine del for di scansione blacklist (se il pacchetto � associato a un flusso in black list setto inBlacklist=true)
									  
									//inizia la fase di riconoscimento dei flussi, e blocco traffico in ingresso associato a una richiesta 
									//effettuata dalla vulbox (lo capisco vedendo risposta SYN,ACK, oppure se presente nella blacklist)
									if(tcp.flags_SYN()&&tcp.flags_ACK()&&!inBlacklist) {
										 
										packetList.add(pa);
										System.out.println("Rilevato traffico di risposta a "+ local_ip+"\n-> flusso scartato e aggiunto in blacklist");
						            //se rilevo che questo pc sta chiedendo di instaurare un flusso
		                            // tcp, invece di inviare il pacchetto lo salvo nel vector dei pacchetti che rappresentano 
					                //i quartetti da scartare(di riferimento)
									}
									//se pacchetto in blacklist scartalo								 
									if(inBlacklist) {
										System.out.println("Rilevato pacchetto in entrata in blacklist -> scartato");
									}
									//se invece sta mandando altro traffico && non � presente nella blacklist
									//allora salva il pacchetto
									if(!inBlacklist && !tcp.flags_SYN() && !tcp.flags_FIN()) {
										splitdumper.dump(pa.getCaptureHeader(), packet);    //salva il pacchetto nel dump
						    		    System.out.println("Pacchetto non di risposta a traffico generato dalla vulnbox -> conservato");
									}
									//se FIN() � settato a true(e pacchetto in blacklist) allora flusso tcp chiuso -> libera il valore dalla blacklist
									if(tcp.flags_FIN()&& tcp.flags_ACK()&& inBlacklist) {
										
										packetList.remove(blacklistPnt);
						    		    System.out.println("traffico relativo a flusso in blacklist, rimosso dalla blacklist per FIN=true");
									}
									
								}// fine if controllo di flusso attivo
								else if(FLAG_Fcontrol==2) {//se modalit� analisi espressione regolare
									
						   			String payload=new String(tcp.getPayload());
						    		if (Pattern.matches(Fcontrol, payload)) 
						    		{
						    			System.out.println("\n!!!! Corrispondenza trovata nella TCP payload del pacchetto Numero: "+packet.getFrameNumber()+"!!!!");
						    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
						    		}else {
						    			System.out.println("\nCorrispondenza non trovata nel pacchetto Numero: "+packet.getFrameNumber());
						    		}  		
								}// fine if exp regolare
				    	    }// fine if IPdst=Local_IP					    						
					}// fine if DstPort = portfilter	
				}//fine if pacchetto con header TCP e IP4
		
				//PACCHETTO UDP
				if (packet.hasHeader(udp)&&packet.hasHeader(ip4)) {					
					packet.getHeader(udp);
					//caso in cui il traffico � in uscita dalla porta in analisi (devo anche controllare che il source sia la vulnbox)
					if(udp.source()==portFilter)  
					{
						packet.getHeader(ip4);
						try {							
						    //se inoltre indirizzo ip sorgente corrisponde a questo pc - > in uscita dalla porta di questo pc
						    if (local_MAC.equals(Arrays.toString(eth.source()))||local_ip.equals(InetAddress.getByAddress(ip4.source()).toString()))
				    	    {
						    	if(FLAG_Fcontrol==2) {//se modalit� analisi espressione regolare
						    		
						    		String payload=new String(udp.getPayload());
						    		if (Pattern.matches(Fcontrol, payload)) 
						    		{
						    			System.out.println("\n!!!!Corrispondenza trovata nella UDP payload del pacchetto Numero: "+packet.getFrameNumber()+"!!!!");
						    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
						    		}else {
						    			System.out.println("\nCorrispondenza non trovata nel pacchetto Numero: "+packet.getFrameNumber());
						    		}  		
								}else { //se modalit� 1 o 0
									//System.out.println("frame #"+ packet.getFrameNumber());
					    		    splitdumper.dump(pa.getCaptureHeader(), packet); //salva pacchetto nel dump
					    		    //System.out.println(udp.getName()+" -> conservato");
								}
				    		    
				    	    }				    	
				        }catch(Exception e) {}												
					}					
					//caso in cui il traffico � in entrata dalla porta in analisi(devo anche controllare che il destinatario sia la vulnbox)
					if(udp.destination()==portFilter)  
					{
						packet.getHeader(ip4);
						try {							
						    //se inoltre indirizzo ip destinatario corrisponde a questo pc - > in ingresso dalla porta di questo pc
						    if (local_MAC.equals(Arrays.toString(eth.destination()))||local_ip.equals(InetAddress.getByAddress(ip4.destination()).toString()))
				    	    {
						    	
						    	if(FLAG_Fcontrol==2) {//se modalit� analisi espressione regolare
									
						    		String payload=new String(udp.getPayload());
						    		if (Pattern.matches(Fcontrol, payload)) 
						    		{
						    			System.out.println("\n!!!!Corrispondenza trovata nella UDP payload del pacchetto Numero: "+packet.getFrameNumber()+"!!!!");
						    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
						    		}else {
						    			System.out.println("\nCorrispondenza non trovata nel pacchetto Numero: "+packet.getFrameNumber());
						    		}  		
								}else {
									//System.out.println("\nPacchetto UDP in entrata dalla porta "+portFilter+"\nDestination IP: "+ local_ip);
							    	//System.out.println("frame #"+ packet.getFrameNumber());
					    		    splitdumper.dump(pa.getCaptureHeader(), packet);  //salva pacchetto nel dump
					    		    //System.out.println(udp.getName()+" -> conservato");
								}
						    	
				    	    }				    	
				        }catch(Exception e) {}	
    				}//end traffico udp ingresso
				}//end PACCHETTO UDP
			}//end nextpacket()

		}, this.errBuffer);//end loop scansione file
		splitdumper.close();//chiusura dump
		splitter.close(); //chiusura pcap 	    
		return splitted;
		//nome del file	contenente lo split	
	}//end metodo
     
    //metodo per testare il funzionamento del file .ini
    public void IniTest() {
	   System.out.println("Lettura minuti da file: "+this.minuti);
	   System.out.println("Lettura Promisc da file: "+this.flag); 	   
   }
   
    
}
