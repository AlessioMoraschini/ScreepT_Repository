package uTILS.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Vector;

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
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class NetworkUtils {

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(NetworkUtils.class); 
	
	private static Vector<URI> pubIPServices;
	
	private static int timeout = 10000; // in milliseconds
	
	private Vector<NetworkAdapter> adapters = new Vector<NetworkAdapter>();
	
	// CONSTRUCTOR
	public NetworkUtils() throws IOException {
		LogManager.getRootLogger().setLevel(Level.ERROR);
		
		pubIPServices = getURLSservicesPUBIP();
		Enumeration<NetworkInterface> networkAdapters;
		networkAdapters = NetworkInterface.getNetworkInterfaces();
		
		while(networkAdapters.hasMoreElements()) {
			NetworkInterface currentInterface = networkAdapters.nextElement();
			Enumeration<InetAddress> currentSubInterface = currentInterface.getInetAddresses();
			
			while(currentSubInterface.hasMoreElements() && currentInterface.isUp()) {
				InetAddress currentInet = (InetAddress)currentSubInterface.nextElement();
				NetworkAdapter currentAdapter = new NetworkAdapter();	
				
				currentAdapter.setLocalIP(currentInet.getAddress());
				currentAdapter.setLocalMAC(currentInterface.getHardwareAddress());
				
				if (currentAdapter.getLocalIP()!=null && currentInet.isReachable(1000)) {
					try {
						currentAdapter.setRemoteIP(retrievePublicIP(currentInet));
					} catch (IOException e) {
						logger.error("Unreachable network using adapter");
					}
				}
				adapters.add(currentAdapter);
			}
			
		}
		System.out.println("END");
	}
	
	// TODO => sposta in metodi ad hoc chiamabili singolarmente e svuota costruttore
	
	// METHODS
	
	public static Vector<URI> getURLSservicesPUBIP(){
		Vector<URI> listWS = new Vector<URI>();
		
		try {
			listWS.add(new URI("http://checkip.amazonaws.com"));
			listWS.add(new URI("http://www.icanhazip.com"));
			listWS.add(new URI("http://www.ipinfo.io/ip"));
		} catch (URISyntaxException e) {
			logger.error("Error in URI format", e);
		}
		
		return listWS;
	}

	public static byte[] retrievePublicIP(InetAddress localAddress) throws IOException {
		boolean retrieved = false;
		String responseString = null;
		
		RequestConfig.Builder builder = RequestConfig.custom();
		builder.setLocalAddress(localAddress);
		builder.setConnectTimeout(timeout);
		RequestConfig requestConfig = builder.build();
		
		// Create an instance of HttpClient.
		CloseableHttpClient httpclient = HttpClients.createDefault();

	    while (!retrieved) {
			// Create a method instance.
			HttpGet request = new HttpGet(pubIPServices.iterator().next());
			// set config to request before to make it
			request.setConfig(requestConfig);
			
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
}
