package networkscanner.dmad;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapDumper;
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


public class CaptureCycle {

	// CAMPI DELLA CLASSE
	
	public String local_ip;
	public String Fcontrol; //stringa contenente l'espressione regolare di filtro inserita nel file ini
	public int FLAG_Fcontrol;  // valore intero reappresentante filtro flusso ON/OFF
	public String srcPath;    //contiene il path della cartella contenente i dump di origine
	public String dumPath;  // contiene il path relativo in cui viene salvato il .cap
    public StringBuilder errBuffer; // contiene il buffer degli errori relativo a Pcap
    boolean flow;
    boolean exp;
    // COSTRUTTORI DELLA CLASSE
      
    public CaptureCycle(StringBuilder buff,String sourcePath, String outPath,String ip,boolean flow_contr,boolean expr,String filter) {
    	this.srcPath=sourcePath;
    	flow=flow_contr;
    	exp=expr;
    	this.Fcontrol=filter;    	
    	this.local_ip="/"+ip;
    	if(flow_contr) {
    		this.FLAG_Fcontrol=1;
    	}else if(expr){
    		this.FLAG_Fcontrol=2;
    	}else {
    		this.FLAG_Fcontrol=0;
    	}
    	
    	this.errBuffer = buff;
    	this.dumPath=outPath;
    }
    
       // METODI DELLA CLASSE
       
    
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
    public String Split(String sourceDumpName, int portFilter,Vector <PcapPacket> packetList){
		String splitted =this.dumPath+"/Port-"+portFilter+"-localDump.cap";
		String sourceDumpPath=this.srcPath+"/"+sourceDumpName;
		
	    final Pcap splitter = Pcap.openOffline(sourceDumpPath, this.errBuffer);
		if (splitter == null) {
			System.err.println(this.errBuffer); // Error is stored in errbuf if any
			return "Error-"+splitted;
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
						    if (local_ip.equals(packetsrcIP))
				    	    {
						    	//ricavo il quartetto che descrive il pacchetto corrente
						    	byte[] SRCip=ip4.source();
						    	byte[] DSTip=ip4.destination();
						    	int SRCp=tcp.source();
						    	int DSTp=tcp.destination();
						    	
						    	if (FLAG_Fcontrol==0)
						    	{  //se controllo di flusso disattivato						    		
									
									splitdumper.dump(pa.getCaptureHeader(), packet);  // salva il pacchetto nel dump
									
								}else if (FLAG_Fcontrol==1) {  //se controllo di flusso attivo

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
									}
									//fine scansione blacklist (se il pacchetto � associato a un flusso in black list setto inBlacklist=true)
									//inizia la fase di riconoscimento dei flussi, e blocco traffico generato in uscita 
									//dalla vulbox (e relativo traffico di risposta <=> stesso flusso tcp <=> stesso quartetto ID)
									//memorizzo i flussi da scartare quando riconosco un SYN(da questo ip),
									//oppure SYN-ACK. li rimuovo quando leggo un FIN
									if(tcp.flags_SYN()&&!tcp.flags_ACK()&&!inBlacklist) {
										 
										packetList.add(pa);
										
									//se rilevo che questo pc sta chiedendo di instaurare un flusso
		                            // tcp, invece di inviare il pacchetto lo salvo nel vector dei pacchetti che rappresentano 
					                //i quartetti da scartare(di riferimento)
									}
									//se pacchetto corrisponde a flusso in blacklist scartalo
									if(inBlacklist) {
									}
									//se invece sta mandando altro traffico && non � presente nella blacklist
									//allora salva il pacchetto 
									if(!inBlacklist && !tcp.flags_SYN()) {
										
										if(exp) {//se modalit� analisi espressione regolare
											   
											String payload=Arrays.toString(tcp.getPayload());
											String pay=new String(tcp.getPayload());
								    		if (Pattern.matches(Fcontrol, payload)||Pattern.matches(Fcontrol, pay)) 
								    		{
								    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
								    		}//altrimenti salva
								        }else {
								        	splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
								        }
									}
									
									//se FIN() � settato a true allora flusso tcp chiuso -> libera il valore dalla blacklist
									if(tcp.flags_FIN()&& inBlacklist) {
										
										packetList.remove(blacklistPnt);
									}
									
								} // fine if controllo di flusso attivo		
								
								else if(FLAG_Fcontrol==2) {//se modalit� analisi espressione regolare
									   
									String payload=Arrays.toString(tcp.getPayload());
									String pay=new String(tcp.getPayload());
						    		if (Pattern.matches(Fcontrol, payload)||Pattern.matches(Fcontrol, pay)) 
							    		{
							    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
							    		}else {
							    			//Console.append("\n\nCorrispondenza non trovata nel pacchetto Numero: "+packet.getFrameNumber());
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
						    if (local_ip.equals(packetdstIP))
				    	    {
						    	byte[] SRCip=ip4.source();
						    	byte[] DSTip=ip4.destination();
						    	int SRCp=tcp.source();
						    	int DSTp=tcp.destination();						    	
						    	if (FLAG_Fcontrol==0) {  //se filtro disattivato
									
						    	    splitdumper.dump(pa.getCaptureHeader(), packet);  //salva il pacchetto nel dump
									
								}else if (FLAG_Fcontrol==1) {  //se controllo di flusso attivo
													    		    				    		  
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
									//se rilevo che questo pc sta chiedendo di instaurare un flusso
		                            // tcp, invece di inviare il pacchetto lo salvo nel vector dei pacchetti che rappresentano 
					                //i quartetti da scartare(di riferimento)
									}
									//se pacchetto in blacklist scartalo								 
									if(inBlacklist) {
									}
									//se invece sta mandando altro traffico && non � presente nella blacklist
									//allora salva il pacchetto
									if(!inBlacklist && !tcp.flags_SYN() && !tcp.flags_FIN()) {
									
										if(exp) {//se modalit� analisi espressione regolare
											
											String payload=Arrays.toString(tcp.getPayload());
											String pay=new String(tcp.getPayload());
								    		if (Pattern.matches(Fcontrol, payload)||Pattern.matches(Fcontrol, pay)) 
								    		{
								    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
								    		}  	
								    	}else {
								    		splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
								    		
										}
									
									}
									//se FIN() � settato a true(e pacchetto in blacklist) allora flusso tcp chiuso -> libera il valore dalla blacklist
									if(tcp.flags_FIN()&& tcp.flags_ACK()&& inBlacklist) {
										
										packetList.remove(blacklistPnt);
									}
									
								}// fine if controllo di flusso attivo
								else if(FLAG_Fcontrol==2) {//se modalit� analisi espressione regolare
									
									String payload=Arrays.toString(tcp.getPayload());
									String pay=new String(tcp.getPayload());
						    		if (Pattern.matches(Fcontrol, payload)||Pattern.matches(Fcontrol, pay)) 
						    		{
						    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
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
						    if (local_ip.equals(InetAddress.getByAddress(ip4.source()).toString()))
				    	    {
						    	if(FLAG_Fcontrol==2) {//se modalit� analisi espressione regolare
						    		
						    		String payload=Arrays.toString(udp.getPayload());
									String pay=new String(udp.getPayload());
						    		if (Pattern.matches(Fcontrol, payload)||Pattern.matches(Fcontrol, pay)) 
						    		{
						    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
						    		} 		
								}else { //se modalit� 1 o 0
					    		    splitdumper.dump(pa.getCaptureHeader(), packet); //salva pacchetto nel dump
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
						    if (local_ip.equals(InetAddress.getByAddress(ip4.destination()).toString()))
				    	    {
						    	
						    	if(FLAG_Fcontrol==2) {//se modalit� analisi espressione regolare
									
						    		String payload=Arrays.toString(udp.getPayload());
									String pay=new String(udp.getPayload());
						    		if (Pattern.matches(Fcontrol, payload)||Pattern.matches(Fcontrol, pay))
						    		{
						    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
						    		}else {
						    		}  		
								}else {
									splitdumper.dump(pa.getCaptureHeader(), packet);  //salva pacchetto nel dump
								}
						    	
				    	    }//end if(ip=local_ip)				    	
				        }catch(Exception e) {}	
    				}//end if(port=portfilter)
				}//end PACCHETTO UDP
			}//end nextpacket()

		}, this.errBuffer);//end loop scansione file
		splitdumper.close();//chiusura dump
		splitter.close(); //chiusura pcap 	    
		return splitted;
		//nome del file	contenente lo split	
	}//end metodo
     
    
    
    public String Filter(String sourceDumpName,Vector <PcapPacket> packetList){
		String splitted =this.dumPath+"/FILTERED-localDump.cap";
		String sourceDumpPath=this.srcPath+"/"+sourceDumpName;
		
	    final Pcap splitter = Pcap.openOffline(sourceDumpPath, this.errBuffer);
		if (splitter == null) {
			System.err.println(this.errBuffer); // Error is stored in errbuf if any
			return "Error-"+splitted;
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
					
						packet.getHeader(ip4);
						packet.getHeader(eth);									
						String packetsrcIP=new String();
						try {
							packetsrcIP=InetAddress.getByAddress(ip4.source()).toString();
						}catch(Exception e) {}
													
						    //se inoltre indirizzo ip sorgente corrisponde a questo pc - > in uscita dalla porta di questo pc
						    if (local_ip.equals(packetsrcIP))
				    	    {
						    	//ricavo il quartetto che descrive il pacchetto corrente
						    	byte[] SRCip=ip4.source();
						    	byte[] DSTip=ip4.destination();
						    	int SRCp=tcp.source();
						    	int DSTp=tcp.destination();
						    	
						    	if (FLAG_Fcontrol==0)
						    	{  //se controllo di flusso disattivato						    		
									
									splitdumper.dump(pa.getCaptureHeader(), packet);  // salva il pacchetto nel dump
									
								}else if (FLAG_Fcontrol==1) {  //se controllo di flusso attivo

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
									}
									//fine scansione blacklist (se il pacchetto � associato a un flusso in black list setto inBlacklist=true)
									//inizia la fase di riconoscimento dei flussi, e blocco traffico generato in uscita 
									//dalla vulbox (e relativo traffico di risposta <=> stesso flusso tcp <=> stesso quartetto ID)
									//memorizzo i flussi da scartare quando riconosco un SYN(da questo ip),
									//oppure SYN-ACK. li rimuovo quando leggo un FIN
									if(tcp.flags_SYN()&&!tcp.flags_ACK()&&!inBlacklist) {
										 
										packetList.add(pa);
										
									//se rilevo che questo pc sta chiedendo di instaurare un flusso
		                            // tcp, invece di inviare il pacchetto lo salvo nel vector dei pacchetti che rappresentano 
					                //i quartetti da scartare(di riferimento)
									}
									//se pacchetto corrisponde a flusso in blacklist scartalo
									if(inBlacklist) {
									}
									//se invece sta mandando altro traffico && non � presente nella blacklist
									//allora salva il pacchetto 
									if(!inBlacklist && !tcp.flags_SYN()) {
										
										if(exp) {//se modalit� analisi espressione regolare
											   
											String payload=Arrays.toString(tcp.getPayload());
											String pay=new String(tcp.getPayload());
								    		if (Pattern.matches(Fcontrol, payload)||Pattern.matches(Fcontrol, pay)) 
								    		{
								    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
								    		}//altrimenti salva
								        }else {
								        	splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
								        }
									}
									
									//se FIN() � settato a true allora flusso tcp chiuso -> libera il valore dalla blacklist
									if(tcp.flags_FIN()&& inBlacklist) {
										
										packetList.remove(blacklistPnt);
									}
									
								} // fine if controllo di flusso attivo		
								
								else if(FLAG_Fcontrol==2) {//se modalit� analisi espressione regolare
									   
									String payload=Arrays.toString(tcp.getPayload());
									String pay=new String(tcp.getPayload());
						    		if (Pattern.matches(Fcontrol, payload)||Pattern.matches(Fcontrol, pay)) 
							    		{
							    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
							    		}else {
							    			//Console.append("\n\nCorrispondenza non trovata nel pacchetto Numero: "+packet.getFrameNumber());
							    		}
							    }//fine if controllo espressione regolare
				    	    } // fine if IPsrc=Local_IP				    												
					
					
					//caso in cui il traffico � in entrata dalla porta in analisi(devo anche controllare che il destinatario sia la vulnbox)
				
						packet.getHeader(ip4);
						String packetdstIP=new String();
						try {
							packetdstIP=InetAddress.getByAddress(ip4.destination()).toString();
						}catch(Exception e) {}													
						    //se inoltre indirizzo ip destinatario corrisponde a questo pc - > in ingresso dalla porta di questo pc
						    if (local_ip.equals(packetdstIP))
				    	    {
						    	byte[] SRCip=ip4.source();
						    	byte[] DSTip=ip4.destination();
						    	int SRCp=tcp.source();
						    	int DSTp=tcp.destination();						    	
						    	if (FLAG_Fcontrol==0) {  //se filtro disattivato
									
						    	    splitdumper.dump(pa.getCaptureHeader(), packet);  //salva il pacchetto nel dump
									
								}else if (FLAG_Fcontrol==1) {  //se controllo di flusso attivo
													    		    				    		  
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
									//se rilevo che questo pc sta chiedendo di instaurare un flusso
		                            // tcp, invece di inviare il pacchetto lo salvo nel vector dei pacchetti che rappresentano 
					                //i quartetti da scartare(di riferimento)
									}
									//se pacchetto in blacklist scartalo								 
									if(inBlacklist) {
									}
									//se invece sta mandando altro traffico && non � presente nella blacklist
									//allora salva il pacchetto
									if(!inBlacklist && !tcp.flags_SYN() && !tcp.flags_FIN()) {
									
										if(exp) {//se modalit� analisi espressione regolare
											
											String payload=Arrays.toString(tcp.getPayload());
											String pay=new String(tcp.getPayload());
								    		if (Pattern.matches(Fcontrol, payload)||Pattern.matches(Fcontrol, pay)) 
								    		{
								    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
								    		}  	
								    	}else {
								    		splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
								    		
										}
									
									}
									//se FIN() � settato a true(e pacchetto in blacklist) allora flusso tcp chiuso -> libera il valore dalla blacklist
									if(tcp.flags_FIN()&& tcp.flags_ACK()&& inBlacklist) {
										
										packetList.remove(blacklistPnt);
									}
									
								}// fine if controllo di flusso attivo
								else if(FLAG_Fcontrol==2) {//se modalit� analisi espressione regolare
									
									String payload=Arrays.toString(tcp.getPayload());
									String pay=new String(tcp.getPayload());
						    		if (Pattern.matches(Fcontrol, payload)||Pattern.matches(Fcontrol, pay)) 
						    		{
						    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
						    		}  	
						    	}// fine if exp regolare
				    	    }// fine if IPdst=Local_IP					    						
				}//fine if pacchetto con header TCP e IP4
				//PACCHETTO UDP
				if (packet.hasHeader(udp)&&packet.hasHeader(ip4)) {					
					packet.getHeader(udp);
				
						packet.getHeader(ip4);
						try {							
						    //se inoltre indirizzo ip sorgente corrisponde a questo pc - > in uscita dalla porta di questo pc
						    if (local_ip.equals(InetAddress.getByAddress(ip4.source()).toString()))
				    	    {
						    	if(FLAG_Fcontrol==2) {//se modalit� analisi espressione regolare
						    		
						    		String payload=Arrays.toString(udp.getPayload());
									String pay=new String(udp.getPayload());
						    		if (Pattern.matches(Fcontrol, payload)||Pattern.matches(Fcontrol, pay)) 
						    		{
						    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
						    		} 		
								}else { //se modalit� 1 o 0
					    		    splitdumper.dump(pa.getCaptureHeader(), packet); //salva pacchetto nel dump
					    		 }
				    		    
				    	    }				    	
				        }catch(Exception e) {}												
					
						packet.getHeader(ip4);
						try {							
						    //se inoltre indirizzo ip destinatario corrisponde a questo pc - > in ingresso dalla porta di questo pc
						    if (local_ip.equals(InetAddress.getByAddress(ip4.destination()).toString()))
				    	    {
						    	
						    	if(FLAG_Fcontrol==2) {//se modalit� analisi espressione regolare
									
						    		String payload=Arrays.toString(udp.getPayload());
									String pay=new String(udp.getPayload());
						    		if (Pattern.matches(Fcontrol, payload)||Pattern.matches(Fcontrol, pay))
						    		{
						    			splitdumper.dump(pa.getCaptureHeader(), packet); // salva pacchetto nel dump
						    		}else {
						    		}  		
								}else {
									splitdumper.dump(pa.getCaptureHeader(), packet);  //salva pacchetto nel dump
								}
						    	
				    	    }//end if(ip=local_ip)				    	
				        }catch(Exception e) {}	
				}//end PACCHETTO UDP
			}//end nextpacket()

		}, this.errBuffer);//end loop scansione file
		splitdumper.close();//chiusura dump
		splitter.close(); //chiusura pcap 	    
		return splitted;
		//nome del file	contenente lo split	
	}//end metodo
   
   
    
}
