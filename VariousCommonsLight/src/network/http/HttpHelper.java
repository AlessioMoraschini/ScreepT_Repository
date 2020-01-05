package network.http;

import static java.lang.System.out;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import files.Various;
import network.utils.DownloadUtils;
import utility.SafeLogger;

public class HttpHelper {
	
	public static SafeLogger logger = new SafeLogger(HttpHelper.class);
	
	public static boolean is404(URL url, HttpRequestMetod method) throws MalformedURLException, IOException {
		return 404 == (getResponseCode(url, method));
	}

	public static boolean is404(String urlString, HttpRequestMetod method) throws MalformedURLException, IOException {
		return 404 == (getResponseCode(urlString, method));
	}

	public static int getResponseCode(String urlString, HttpRequestMetod method) throws MalformedURLException, IOException {
	    URL url = new URL(urlString); 
	    HttpURLConnection httpUrlConnection =  (HttpURLConnection)url.openConnection(); 
	    httpUrlConnection.setRequestMethod(method.name()); 
	    httpUrlConnection.connect(); 
	    return httpUrlConnection.getResponseCode();
	}

	public static int getResponseCode(URL url, HttpRequestMetod method) throws MalformedURLException, IOException {
		HttpURLConnection httpUrlConnection =  (HttpURLConnection)url.openConnection(); 
		httpUrlConnection.setRequestMethod(method.name()); 
		httpUrlConnection.connect(); 
		return httpUrlConnection.getResponseCode();
	}
	
	public enum HttpRequestMetod {
		GET,
		POST, 
		HEAD,
		OPTIONS, 
		PUT, 
		DELETE, 
		TRACE; 
	}

	public static boolean isUrlReachable(URL urlToTest) {
		try {
			return InetAddress.getByName(urlToTest.getHost()).isReachable(5000);
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Retrieve a list of String lines from the given text file
	 */
	public static List<String> getLinesFromUrlFile(URL url) {
		ArrayList<String> lines = new ArrayList<>(); 
		InputStream inStream = null;
		Scanner scanner = null;
		
		try {
			inStream = url.openStream();
			scanner = new Scanner(inStream);
			while (scanner.useDelimiter("\\A").hasNext()) {
				String text = scanner.useDelimiter("\\A").next();
				lines.add(text);
			}
		} catch (IOException e) {
			out.println(Arrays.deepToString(e.getStackTrace()));
		} finally {
			try {
				scanner.close();
				inStream.close();
			} catch (Exception e2) {
				logger.error("Cannot close stream, an error occurred", e2);
			}
		}
		
		return lines;
	}

	/**
	 * Retrieve the size in bytes of a file located at given remote URL
	 * @param url
	 * @return
	 * @throws Exception 
	 */
	public static int getFileSize(URL url) throws Exception {
	    URLConnection conn = null;
	    try {
	    	out.println("Opening connection to url "+url+" to retrieve file size...");
	        try {
				conn = url.openConnection();
				conn.setConnectTimeout(DownloadUtils.CONNECTION_TIMEOUT_MS);
				if(conn instanceof HttpURLConnection) {
				    ((HttpURLConnection)conn).setRequestMethod("HEAD");
				}
				conn.getInputStream();
			} catch (Exception e) {
				throw new TimeoutException("Server unreachable!");
			}
	        logger.info("=> retrieving file size");
	        int size = conn.getContentLength();
	        logger.info("=> Found size: " + Various.coolFileSize((long)size));
	        return size;
	    } catch (Exception e) {
	    	logger.error("An error occurred while retrieving file size: ", e);
	        throw e;
	    } finally {
	        if(conn instanceof HttpURLConnection) {
	            ((HttpURLConnection)conn).disconnect();
	        }
	    }
	}
}