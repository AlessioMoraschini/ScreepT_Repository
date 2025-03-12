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
package network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.JProgressBar;

import org.apache.commons.net.ftp.FTPClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import network.om.FtpSpeedTestOptions;
import os.commons.om.OSinfo;
import resources.GeneralConfig;
import various.common.light.network.http.HttpHelper;
import various.common.light.network.utils.IPaddressConverter;
import various.common.light.utility.log.SafeLogger;
import various.common.light.utility.manipulation.ConversionUtils;
import various.common.light.utility.primitive.DynaBoolean;
import various.common.light.utility.string.StringWorker;

public class NetworkUtils {

////////////////////////  FIELDS  ////////////////////////
	
	// KeyGenPanel logger
	static SafeLogger logger = new SafeLogger(NetworkUtils.class); 
	
	private static Vector<URI> pubIPServices;
	
	private static int timeout; // in milliseconds
	
	private Vector<NetworkAdapter> networkAdapters;
	
	public Enumeration<NetworkInterface> networkInterfaces;
//	
////////////////////////  CONSTRUCTORS  ////////////////////////
	
	public NetworkUtils() throws IOException {
		
		//LogManager.getRootLogger().setLevel(Level.ERROR);
		retrieveAvailableAdapters();
	}
	
////////////////////////  PRIVATE METHODS  ////////////////////////
	
	private void init() throws SocketException {
		networkAdapters = new Vector<NetworkAdapter>();
		pubIPServices = getURLSservicesPUBIP();
		networkInterfaces = NetworkInterface.getNetworkInterfaces();
		timeout = GeneralConfig.NETWORK_UTILS_TIMEOUT;
	}
	
	private static Vector<URI> getURLSservicesPUBIP(){
		Vector<URI> listWS = new Vector<URI>();
		String[] propertiesVal = GeneralConfig.getPubIpProvList();
		try {
			for(String address : propertiesVal) {
				listWS.add(new URI(StringWorker.trimToEmpty(address)));
			}
		} catch (URISyntaxException e) {
			logger.error("Error in URI format", e);
		}
		
		return listWS;
	}
	
////////////////////////  PUBLIC METHODS  ////////////////////////

	public static RequestConfig configureCustomRequest(RequestConfig.Builder builder) {
		
		RequestConfig requestConfig = builder.build();
		return requestConfig;
		
	}
	
	private void retrieveAvailableAdapters() throws IOException {
		
		init();
		
		while(networkInterfaces.hasMoreElements()) {
			NetworkInterface currentInterface = networkInterfaces.nextElement();
			Enumeration<InetAddress> currentSubInterface = currentInterface.getInetAddresses();
			
			while(currentSubInterface.hasMoreElements() && currentInterface.isUp()&& !currentInterface.isLoopback()) {
				InetAddress currentInet = (InetAddress)currentSubInterface.nextElement();
				NetworkAdapter currentAdapter = new NetworkAdapter();	
				
				currentAdapter.setNetworkInterface(currentInterface);
				currentAdapter.setLocalIP(currentInet.getAddress());
				currentAdapter.setLocalMAC(currentInterface.getHardwareAddress());
				currentAdapter.setName(currentInterface.getDisplayName());
				currentAdapter.setMTU(currentInterface.getMTU());
				currentAdapter.setHostName(currentInet.getHostName());
				currentAdapter.setInetAddr(currentInet);

				networkAdapters.add(currentAdapter);
			}
			
		}
	}
	
	public String retrieveAdapterPublicIP(NetworkAdapter currentAdapter) throws IOException {
		if (currentAdapter.getLocalIP()!=null) {
			try {
				currentAdapter.setRemoteIP(retrievePublicIP(currentAdapter.getInetAddr()));
			} catch (IOException e) {
				logger.error("Unreachable network using adapter", e);
				currentAdapter.remIP = "Unreachable Service";
			}
		}
		return currentAdapter.remIP;
	}
	
	public static int retrieveLatency(NetworkInterface netInf) throws UnknownHostException, IOException {
		int times = 5;
		long start = System.nanoTime();
		for (int i = 0; i < times; i++) {
			InetAddress.getByName(GeneralConfig.testAddress).isReachable(netInf, 0, timeout);
		}		
		long end = System.nanoTime();
		int avg_ms = (int)((end-start)/(1000000*times));
		return avg_ms;
	}
	
	public static int retrieveLatencyV2(InetAddress localAddress) throws Exception {
		try {
            // Indirizzo dell'host di test
            URI uri = new URI(GeneralConfig.testAddress);

            RequestConfig requestConfig = RequestConfig.custom()
                .setLocalAddress(localAddress)
                .build();

            // Crea il client HTTP con la configurazione
            CloseableHttpClient httpClient = HttpClients.createSystem();

            // Crea la richiesta HTTP GET
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setConfig(requestConfig);

            // Cattura il tempo di inizio
            long startTime = System.nanoTime();

            // Esegui la richiesta e cattura la risposta
            HttpResponse response = httpClient.execute(httpGet);

            // Cattura il tempo di fine
            long endTime = System.nanoTime();

            // Calcola la latenza in millisecondi
            int latency = (int)(endTime - startTime) / 1000000;

            // Stampa la latenza
            logger.debug("Latency: " + latency + " ms");

            // Chiudi il client HTTP
            httpClient.close();

            return latency;
        } catch (UnknownHostException e) {
            logger.error("Error: unknown address", e);
            throw e;
        } catch (IOException e) {
        	logger.error("IO Error during http request", e);
        	throw e;
        } catch (Exception e) {
        	logger.error("Unknown Error", e);
        	throw e;
        }
	}
	
	public static String retrieveUpSpeed(InetAddress localAddress, FtpSpeedTestOptions testOptions, JProgressBar progress) throws SocketException, IOException, InterruptedException {

		//if(progress != null) progress.setIndeterminate(true);
		//updatePb(progress, 30);
		//
		//byte[] randomFile = new byte[testOptions.upSize];
		//byte fill = (byte) 56;
		//Arrays.fill(randomFile, fill);
		//ByteArrayInputStream randomStream = new ByteArrayInputStream(randomFile);
        //
		//FTPClient ftpClient = ftpUPinit(localAddress);
		//
		//// start upload
		//long start = System.currentTimeMillis();		
		//ftpClient.storeUniqueFile(randomStream);
		//long end = System.currentTimeMillis();
		//
		//ftpClient.logout();
		//randomStream.close();
		//
		//// calculate speed
		//int upSpeed = (int)(randomFile.length/((end-start)/(1000F)));
		//String UP_Speed = ConversionUtils.coolFileSize((long)upSpeed)+"/s  ["+ConversionUtils.coolFileSizeBits((long)upSpeed)+"ps]";
		//
		//if(progress != null) progress.setIndeterminate(false);
		//updatePb(progress, 100);

		return "UNAVAILABLE";
	}
	
	public static String retrieveDownSpeed(InetAddress localAddress, FtpSpeedTestOptions testOptions, JProgressBar progress) throws IOException {

		if(progress != null) progress.setIndeterminate(true);
		updatePb(progress, 5);
		
		FTPClient ftpClient = ftpDOWNinit(localAddress);
		byte[] downloadedBuffer = new byte[testOptions.downSize/10];
		InputStream downStream = ftpClient.retrieveFileStream(testOptions.downFileName);
		// start download
		int read = 1;
		int i=5;
		long start = System.currentTimeMillis();	
		while (read>0) {
			read = downStream.read(downloadedBuffer);
			updatePb(progress, i);
			i+=10;
		}
		long end = System.currentTimeMillis();
		
		ftpClient.logout();
		downStream.close();
		
		int downSpeed = (int)(testOptions.downSize/((end-start)/(1000F)));
		String DOWN_Speed = ConversionUtils.coolFileSize((long)downSpeed)+"/s  ["+ConversionUtils.coolFileSizeBits((long)downSpeed)+"ps]";
		
		if(progress != null) progress.setIndeterminate(false);
		updatePb(progress, 100);
		
		return DOWN_Speed;
	}
	
	public static String retrieveDownSpeedV2(DynaBoolean stopProcess, InetAddress localAddress, FtpSpeedTestOptions testOptions, JProgressBar progressBar) throws Exception {

		if(progressBar != null) progressBar.setIndeterminate(true);
		updatePb(progressBar, 5);
		int downSpeed = 0;

        try {
            
         // Indirizzo dell'host di test
            URI uri = new URI(testOptions.downFileName);

            RequestConfig requestConfig = RequestConfig.custom()
                .setLocalAddress(localAddress)
                .build();

            // Crea il client HTTP con la configurazione
            CloseableHttpClient httpClient = HttpClients.createSystem();

            // Crea la richiesta HTTP GET
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setConfig(requestConfig);

            long startTime = System.nanoTime();
            // Esegui la richiesta e cattura la risposta
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if(progressBar != null) progressBar.setIndeterminate(false);

            if (entity != null) {
                BufferedInputStream inputStream = new BufferedInputStream(entity.getContent());
	            byte[] data = new byte[1048576];// 1MB of buffer
	            int mbyteContent = 0;
	            long totalMBytesRead = 0;
	            while ((mbyteContent = inputStream.read(data, 0, 1048576)) != -1) {
	                totalMBytesRead += mbyteContent;
	                updatePb(progressBar, (int)(((float)totalMBytesRead)/testOptions.downSize * 100));
	                if(stopProcess.isTrue()) {
	                	break;
	                }
	            }

	            if(stopProcess.isTrue()) {
	            	throw new Exception("Stopped by user");
                }
	            
	            inputStream.close();
            }
            long endTime = System.nanoTime();
            downSpeed = (int)(testOptions.downSize/((endTime-startTime)/(1000000000F)));

        } catch (Exception e) {
            throw e;
        } finally {
        	if(progressBar != null) progressBar.setIndeterminate(false);
    		updatePb(progressBar, 100);
        }
		
		return ConversionUtils.coolFileSize((long)downSpeed)+"/s  ["+ConversionUtils.coolFileSizeBits((long)downSpeed)+"ps]";
	}
	
	private static void updatePb(JProgressBar pb, int i) {
		if(pb != null) {
			pb.setValue(i);
		}
	}
	
	private static byte[] retrievePublicIP(InetAddress localAddress) throws IOException {
		
		boolean retrieved = false;
		String responseString = null;
		
		RequestConfig.Builder builder = RequestConfig.custom();
		builder.setLocalAddress(localAddress);
		builder.setConnectTimeout(timeout);
		
		RequestConfig localAddressConfig = configureCustomRequest(builder);
		
		// Create an instance of HttpClient.
		CloseableHttpClient httpclient = HttpClients.createSystem();

	    while (!retrieved) {
			// Create a method instance.
			HttpGet request = new HttpGet(pubIPServices.iterator().next());
			// set config to request before to make it
			request.setConfig(localAddressConfig);
			
			HttpResponse response;
			
			try {
				response = httpclient.execute(request);
				HttpEntity entity = response.getEntity();
				try {
					responseString = EntityUtils.toString(entity, "UTF-8");
					System.out.println("=> "+responseString);
				} catch (ParseException e) {
					logger.error("Error parsing IP", e);
				}
				
				if(response.getStatusLine().getStatusCode()==HttpStatus.SC_OK) 
					return IPaddressConverter.asBytes(responseString);
				
			} catch (ClientProtocolException e) {
				logger.error("Protocol error calling webService", e);
			}
		}
	    
	    return IPaddressConverter.asBytes(responseString);
	}
	
	@SuppressWarnings("restriction")
	public static List<String> getDnsServers() {
		try {
			return sun.net.dns.ResolverConfiguration.open().nameservers();
		} catch (Throwable e) {
			logger.error("Cannot retrieve DNS servers", e);
			return new ArrayList<String>();
		}
	}
	
	public static List<String> getDnsServersUsingOSCommands(InetAddress localInterfaceAddress) {
		try {
			if(new OSinfo().isWindows()) {
				return getDnsServersForInterfaceWin(localInterfaceAddress);
			} else {
				return getDnsServersForInterfaceLinux(localInterfaceAddress);
			}
		} catch (Throwable e) {
			logger.error("Cannot retrieve DNS servers", e);
			return new ArrayList<String>();
		}
	}
	
	public static List<String> getDnsServersForInterfaceLinux(InetAddress localInterfaceAddress) throws Exception{
		List<String> dnsServers = new ArrayList<>();
		try {
            BufferedReader reader = new BufferedReader(new FileReader("/etc/resolv.conf"));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("nameserver")) {
                    String[] parts = line.split(" ");
                    if (parts.length > 1) {
                        dnsServers.add(parts[1]);
                    }
                }
            }

            reader.close();

        } catch (IOException e) {
            throw e;
        }
		
		return dnsServers;
	}
	
	public static List<String> getDnsServersForInterfaceWin(InetAddress localInterfaceAddress) throws Exception{
		String targetIpAddress = localInterfaceAddress.getHostAddress(); // Indirizzo IP di destinazione

		List<String> dnsServers = new ArrayList<>();
        try {
            // Esegui il comando ipconfig /all
            Process process = Runtime.getRuntime().exec("ipconfig /all");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            boolean isCorrectAdapter = false;

            while ((line = reader.readLine()) != null) {
                // Verifica se la riga contiene l'indirizzo IP di destinazione
                if (line.contains(targetIpAddress)) {
                    isCorrectAdapter = true;
                } else {
                    isCorrectAdapter = false;
                }

                // Se siamo nell'adapter corretto e la linea contiene "Server DNS"
                if (isCorrectAdapter) {
                	while (!(line.trim().startsWith("Server DNS") || line.trim().startsWith("DNS Servers")) && line != null) {
                		line = reader.readLine();
                    }
                    while ((line = reader.readLine()) != null && line.trim().matches("\\S+")) {
                        dnsServers.add(line.trim());
                    }
                    break;
                }
            }
            
            reader.close();
            process.waitFor();
        } catch (Exception e) {
            throw e;
        }

        return dnsServers;
	}
	
	public static String getIpNetstatGateway() throws Exception {
		
		Process result = Runtime.getRuntime().exec("netstat -rn");

        BufferedReader output = new BufferedReader(new InputStreamReader(result.getInputStream()));
        String thisLine = output.readLine();
        
        ArrayList<String> ips = new ArrayList<>();
        int columnGateway = 0;

        boolean ipLineFound = false;
		while (thisLine != null) {
			thisLine = output.readLine();
			
			if(ipLineFound) {
				
				String[] elements = thisLine.split(" ");
				for(String element : elements) {
					String ip = IPaddressConverter.extractIpFromString(element).trim();
					if(!ip.isEmpty()) {
						ips.add(ip);
					}
				}
				
				break;
			}
			
			if (thisLine.contains("Gateway")) {
				String[] headStrings = thisLine.split("    ");
				for(String elem : headStrings) {
					if(!elem.trim().isEmpty() && !elem.trim().contains("Gateway")) {
						columnGateway++;
					} else if(elem.trim().contains("Gateway")) {
						break;
					}
				}
				ipLineFound = true;
			}
		}
		
		return ips.get(columnGateway);
	}
	
	private static FTPClient ftpDOWNinit(InetAddress localAddress) throws IOException {
		// force passive mode to use specified adapter
		FTPClient ftpClient = new FTPClient();
		ftpClient.setProxy(HttpHelper.getProxy());
		ftpClient.enterLocalPassiveMode();
		ftpClient.setPassiveLocalIPAddress(localAddress);
		ftpClient.connect(FtpSpeedTestOptions.HOST_FTP);
		ftpClient.login(FtpSpeedTestOptions.USER_FTP, FtpSpeedTestOptions.PSW_FTP);
		return ftpClient;
	}
	
	private static FTPClient ftpUPinit(InetAddress localAddress) throws IOException {
		// force passive mode to use specified adapter
		FTPClient ftpClient = new FTPClient();
		ftpClient.setProxy(HttpHelper.getProxy());
		ftpClient.enterLocalPassiveMode();
		ftpClient.setPassiveLocalIPAddress(localAddress);
		ftpClient.connect(FtpSpeedTestOptions.HOST_FTP);
		ftpClient.login(FtpSpeedTestOptions.USER_FTP, FtpSpeedTestOptions.PSW_FTP);
		ftpClient.changeWorkingDirectory(FtpSpeedTestOptions.UP_FOLDER);
		return ftpClient;
	}

////////////////////////  GETTERS AND SETTERS  ////////////////////////
	
	public static int getTimeout() {
		return timeout;
	}

	public static void setTimeout(int timeout) {
		NetworkUtils.timeout = timeout;
	}

	public static Vector<URI> getPubIPServices() {
		return pubIPServices;
	}

	public Vector<NetworkAdapter> getAdapters() {
		return networkAdapters;
	}

}
