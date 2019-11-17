package utils;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import gui.UpdateProgressPanelMin;
import gui.Various;
import static java.lang.System.*;

public class DownloadUtils {
	
	/**
	 * Retrieve the size in bits of a file located at given remote URL
	 * @param url
	 * @return
	 */
	public static int getFileSize(URL url) {
	    URLConnection conn = null;
	    try {
	    	out.println("Opening connection to url "+url+" to retrieve file size...");
	        conn = url.openConnection();
	        if(conn instanceof HttpURLConnection) {
	            ((HttpURLConnection)conn).setRequestMethod("HEAD");
	        }
	        conn.getInputStream();
	        out.println("=> retrieving file size");
	        int size = conn.getContentLength();
	        out.println("=> Found size: " + size);
	        return size;
	    } catch (IOException e) {
	    	out.println("An error occurred: " + Arrays.deepToString(e.getStackTrace()));
	        throw new RuntimeException(e);
	    } finally {
	        if(conn instanceof HttpURLConnection) {
	            ((HttpURLConnection)conn).disconnect();
	        }
	    }
	    
	}
	
	public static File downloadFile(URL url, String localPath, UpdateProgressPanelMin progressUpdater) {
		int fileSize = getFileSize(url);
		out.println("Opening url stream...");
		BufferedInputStream in = null;
		FileOutputStream fileOutputStream = null;
		
		if(progressUpdater != null) {
			progressUpdater.getDialog().setTitle(new File(url.getFile()).getName());
			progressUpdater.getDialog().setMinimumSize(new Dimension(650, 120));
			progressUpdater.getDialog().setPreferredSize(new Dimension(650, 120));
			Various.centerComponent(progressUpdater.getDialog());
		}
		
		try{
			in = new BufferedInputStream(url.openStream());
			fileOutputStream = new FileOutputStream(localPath);
		    byte dataBuffer[] = new byte[1024];
		    int bytesRead;
		    int cycles = 1;
		    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
		        fileOutputStream.write(dataBuffer, 0, bytesRead);
		        int percDownloaded = getPerc(1024*cycles, fileSize);
		        updateProgress("Downloading " + new File(url.getFile()).getName() + " ("+percDownloaded+"%)...", percDownloaded, progressUpdater);
		        cycles ++;
		    }
		} catch (IOException e) {
			out.println("ERROR DOWNLAODING FILE: " + url + "\n\n" + Arrays.deepToString(e.getStackTrace()));
		} finally {
			try {
				out.println("Closing url stream...");
				updateProgress("Download Completed!", 100, progressUpdater);
				in.close();
				fileOutputStream.close();
				out.println("Streams closed!");
			} catch (IOException e) {
				out.println("An error occurred while closing streams after download" + Arrays.deepToString(e.getStackTrace()));
			}
		}
		
		return new File(localPath);
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
				out.println("Cannot close stream, an error occurred" + Arrays.deepToString(e2.getStackTrace()));
			}
		}
		
		return lines;
	}
	
	public static int getPerc(int val, int tot) {
		float perc = ((float)val / (float)tot) * 100;
		return (int)perc;
	}
	
	public static void updateProgress(String text, int progress, UpdateProgressPanelMin progressUpdater) {
		if(progressUpdater != null) {
			progressUpdater.update(text, progress);
		}
	}

}
