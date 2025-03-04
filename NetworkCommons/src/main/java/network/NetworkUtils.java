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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
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
import resources.GeneralConfig;
import various.common.light.network.http.HttpHelper;
import various.common.light.network.utils.IPaddressConverter;
import various.common.light.utility.log.SafeLogger;
import various.common.light.utility.manipulation.ConversionUtils;
import various.common.light.utility.string.StringWorker;

public class NetworkUtils {

////////////////////////  FIELDS  ////////////////////////
	
	// KeyGenPanel logger
	static SafeLogger logger = new SafeLogger(NetworkUtils.class); 
	
	//private static final Random RANDOM_GENERATOR = new Random();

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
			
			while(currentSubInterface.hasMoreElements() /**&& currentInterface.isUp()**/ && !currentInterface.isVirtual() && !currentInterface.isLoopback()) {
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
		if (currentAdapter.getLocalIP()!=null && currentAdapter.getInetAddr().isReachable(1000)) {
			try {
				currentAdapter.setRemoteIP(retrievePublicIP(currentAdapter.getInetAddr()));
			} catch (IOException e) {
				logger.error("Unreachable network using adapter");
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
	
	public static String retrieveUpSpeed(InetAddress localAddress, FtpSpeedTestOptions testOptions, JProgressBar progress) throws SocketException, IOException, InterruptedException {

		if(!localAddress.isReachable(2000)) {
			return "Given local address is not reachable. Check connection and retry";
		}
		
		if(progress != null) progress.setIndeterminate(true);
		updatePb(progress, 30);
		
		byte[] randomFile = new byte[testOptions.upSize];
		byte fill = (byte) 56;
		Arrays.fill(randomFile, fill);
		//RANDOM_GENERATOR.nextBytes(randomFile);
		ByteArrayInputStream randomStream = new ByteArrayInputStream(randomFile);

		FTPClient ftpClient = ftpUPinit(localAddress);
		
		// start upload
		long start = System.currentTimeMillis();		
		ftpClient.storeUniqueFile(randomStream);
		long end = System.currentTimeMillis();
		
		ftpClient.logout();
		randomStream.close();
		
		// calculate speed
		int upSpeed = (int)(randomFile.length/((end-start)/(1000F)));
		String UP_Speed = ConversionUtils.coolFileSize((long)upSpeed)+"/s  ["+ConversionUtils.coolFileSizeBits((long)upSpeed)+"ps]";
		
		if(progress != null) progress.setIndeterminate(false);
		updatePb(progress, 100);

		return UP_Speed;
	}
	
	public static String retrieveDownSpeed(InetAddress localAddress, FtpSpeedTestOptions testOptions, JProgressBar progress) throws IOException {

		if(!localAddress.isReachable(2000)) {
			return "Given local address is not reachable. Check connection and retry";
		}
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
	
	private static void updatePb(JProgressBar pb, int i) {
		if(pb != null) {
			pb.setValue(i);
		}
	}
	
	private static byte[] retrievePublicIP(InetAddress localAddress) throws IOException {
		
		if(!localAddress.isReachable(2000)) {
			logger.warn("Adapter unreachable");
			return new byte[0];
		}
		
		boolean retrieved = false;
		String responseString = null;
		
		RequestConfig.Builder builder = RequestConfig.custom();
		builder.setLocalAddress(localAddress);
		builder.setConnectTimeout(timeout);
		
		RequestConfig localAddressConfig = configureCustomRequest(builder);
		
		// Create an instance of HttpClient.
		CloseableHttpClient httpclient = HttpClients.createDefault();

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
