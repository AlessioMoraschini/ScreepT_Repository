package utils;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import gui.UpdateProgressPanelMin;
import testUtils.TestUtils;

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
	
	public static File downloadFile(URL url, String localPath, UpdateProgressPanelMin progressUpdater) throws Exception{
		int fileSize = getFileSize(url);
		out.println("Opening url stream...");
		BufferedInputStream in = null;
		FileOutputStream fileOutputStream = null;
		
		if(progressUpdater != null) {
			progressUpdater.getDialog().setTitle("Downloading update file: " + new File(url.getFile()).getName());
			progressUpdater.getDialog().setMinimumSize(new Dimension(650, 120));
			progressUpdater.getDialog().setVisible(true);
			progressUpdater.getPb().setIndeterminate(false);
			progressUpdater.getDialog().setPreferredSize(new Dimension(650, 120));
			Various.centerComponent(progressUpdater.getDialog());
		}
		
		in = new BufferedInputStream(url.openStream());
		fileOutputStream = new FileOutputStream(localPath);
	    byte dataBuffer[] = new byte[1024];
	    int bytesRead;
	    int cycles = 1;
	    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
	        fileOutputStream.write(dataBuffer, 0, bytesRead);
	        int percDownloaded = getPerc(1024*cycles, fileSize);
	        percDownloaded = (percDownloaded>99) ? 99 : percDownloaded;
	        progressUpdater.getDialog().setVisible(true);
	        updateProgress("Downloading " + new File(url.getFile()).getName() + " ("+percDownloaded+"%)...", percDownloaded, progressUpdater, false);
	        cycles ++;
	    }
		try {
			out.println("Closing url stream...");
			in.close();
			fileOutputStream.close();
			updateProgress("Download Completed!", 100, progressUpdater, false);
			progressUpdater.forceClose();
			out.println("Streams closed!");
		} catch (IOException e) {
			out.println("An error occurred while closing streams after download" + Arrays.deepToString(e.getStackTrace()));
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
	
	/**
	 * Unzip an archive into destination folder. If overwrite is true, then no new unique file will be created, 
	 * but the existing one with same name will be overridden!
	 * @param sourcePath
	 * @param destFolderPath
	 * @param ovewrwrite
	 * @return
	 * @throws Exception
	 */
	public static File unzipFile(String sourcePath, String destFolderPath, boolean ovewrwrite) throws Exception {
		
		UpdateProgressPanelMin progressUpdater = new UpdateProgressPanelMin(true);
		
		long startTime = System.currentTimeMillis();
		TestUtils.printl("FileStuff.unzipFile() -> Starting unzip:"
				+ "\n source: " + sourcePath
				+ "\n destPath: " + destFolderPath
				+ "\n overwrite: " + ovewrwrite);
		
		if(sourcePath == null || destFolderPath == null || !new File(sourcePath).exists()) {
			throw new Exception();
		}
		
		// Adapt name in case of overwrite
		File fileToZip = new File(sourcePath);
		int lastDotIndex = fileToZip.getName().lastIndexOf('.');
		String destPathFolderName = fileToZip.getName().substring(0,lastDotIndex);
		destFolderPath += File.separator + destPathFolderName;
		
		File destFolderOut = new File(destFolderPath);
		if(!destFolderOut.exists() || destFolderOut.isFile()) {
			destFolderOut.mkdirs();
			destFolderOut.mkdir();
		}
		
		try {
			progressUpdater.getGeneralBar().setIndeterminate(true);
			// Prepare for write
			byte[] buffer = new byte[1024];
			ZipInputStream zis = new ZipInputStream(new FileInputStream(sourcePath));
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				String newOutCurrentPath = destFolderPath + File.separator + zipEntry.getName();
				File out = (ovewrwrite)? new File(newOutCurrentPath) : Various.renameJavaObjFile(new File(newOutCurrentPath));
				if(out.exists()) {
					out.delete();
				}
				if(zipEntry.isDirectory()) {
					out.mkdir();
					zipEntry = zis.getNextEntry();
					continue;
				}
				
				if (progressUpdater == null) {
					progressUpdater = new UpdateProgressPanelMin();
					Various.centerComponent(progressUpdater.getDialog());
				}
				if(!progressUpdater.getDialog().isVisible()) {
					progressUpdater.getDialog().setVisible(true);
				}
				if(progressUpdater != null) {
					progressUpdater.getDialog().setTitle("Unzipping file: " + new File(sourcePath).getName());
					progressUpdater.getDialog().setMinimumSize(new Dimension(650, 120));
					progressUpdater.getDialog().setPreferredSize(new Dimension(650, 120));
				}
				
				progressUpdater.getGeneralBar().setIndeterminate(true);
				updateProgress(zipEntry.getName(), 50, progressUpdater, true);
				
				// For every entry in archive
			    FileOutputStream fos = new FileOutputStream(out);
			    int len;
			    int cycles = 1;
			    while ((len = zis.read(buffer)) > 0) {
			    	int percDownloaded = getPerc(1024*cycles, zipEntry.getSize());
			    	percDownloaded = (percDownloaded>99) ? 99 : percDownloaded;
			    	updateProgress(zipEntry.getName(), percDownloaded, progressUpdater, false);
			    	
			    	// For each byte of current zipped file in archive
			        fos.write(buffer, 0, len);
			        
			        cycles++;
			    }
			    
			    fos.close();
			    zipEntry = zis.getNextEntry();
			}
			
			if (progressUpdater != null) {
				progressUpdater.update("Unzip completed!", 99);
				Thread.sleep(800);
		    	progressUpdater.update(100, "Completed", true);
				progressUpdater.update("Completed", 100);
				progressUpdater.forceClose();
			}
			
			zis.closeEntry();
			zis.close();
			
			TestUtils.printl("FileStuff.unzipFile() -> COMPLETED!"
					+ "\n source: " + sourcePath
					+ "\n unzipped File: " + out
					+ "\n overwrite: " + ovewrwrite);
			
			TestUtils.printl("unzip file: elapsed" + String.valueOf(System.currentTimeMillis()-startTime) + " ms");
		} catch (Exception e) {
			
			TestUtils.printl(Arrays.deepToString(e.getStackTrace()));
		} finally {
			if (progressUpdater != null) {
				progressUpdater.update("Completed", 100);
				progressUpdater.forceClose();
			}
		}
        
        return destFolderOut;
	}

	/**
	 * Zip a file or folder in a new .zip archive, into destination folder. If overwrite is true, then no new unique file will be created, 
	 * but the existing one with same name will be overridden!
	 * 
	 * @param sourcePath
	 * @param destFolderPath
	 * @param ovewrwrite
	 * @return
	 * @throws Exception
	 * 
	 * <b>Fix this before to use it</b>
	 */
	@Deprecated
	public static File zipFile(String sourcePath, String destFolderPath, boolean ovewrwrite, UpdateProgressPanelMin progressUpdater) throws Exception {
		
		long startTime = System.currentTimeMillis();
		TestUtils.printl("FileStuff.zipFile() -> Starting zip:"
				+ "\n source: " + sourcePath
				+ "\n destPath: " + destFolderPath
				+ "\n overwrite: " + ovewrwrite);
		
		if(sourcePath == null || destFolderPath == null) {
			throw new NullPointerException();
		}
		
		if(progressUpdater != null) {
			progressUpdater.getDialog().setTitle("Zipping file: " + new File(sourcePath).getName());
			progressUpdater.getDialog().setMinimumSize(new Dimension(650, 120));
			progressUpdater.getDialog().setPreferredSize(new Dimension(650, 120));
			Various.centerComponent(progressUpdater.getDialog());
		}
		
		File destFolderOut = new File(destFolderPath);
		if(!destFolderOut.exists()) {
			destFolderOut.mkdirs();
		}
		
		File fileToZip = new File(sourcePath);
		destFolderPath += File.separator + fileToZip.getName();
		File out = (ovewrwrite)? new File(destFolderPath) : Various.renameJavaObjFile(new File(destFolderPath));
		
		FileOutputStream fos = new FileOutputStream(out);
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		int cycles = 1;
		while((length = fis.read(bytes)) >= 0) {
			int percDownloaded = getPerc(1024*cycles, zipEntry.getSize());
	    	updateProgress("Zipping " + zipEntry.getName(), percDownloaded, progressUpdater, false);
			zipOut.write(bytes, 0, length);
			cycles ++;
		}
		zipOut.close();
		fis.close();
		fos.close();
		
		TestUtils.printl("FileStuff.zipFile() -> COMPLETED!"
				+ "\n source: " + sourcePath
				+ "\n zipped File: " + out
				+ "\n overwrite: " + ovewrwrite);
		
		TestUtils.printl("zipFile -> Finished in " + String.valueOf(System.currentTimeMillis()-startTime) + " ms");
		
		return out;
	}
	
	public static boolean isUrlReachable(URL urlToTest) {
		try {
			return InetAddress.getByName(urlToTest.getHost()).isReachable(5000);
		} catch (IOException e) {
			return false;
		}
	}
	
	public static String keepOnlyDotsAndDigits(String source) {
		String dotsOnly = source.replaceAll("_", ".");
		dotsOnly = dotsOnly.replaceAll("-", "");
		// remove non dots chars
		return dotsOnly.replaceAll("[a-zA-Z!#$%&'*+=?^`{|}+]|[_]\\-", "");
	}
	
	/**
	 * Dotted version format compare - if version not matches format
	 * <Strong>IllegalArgumentException</Strong> is thrown
	 * 
	 * @param version_A
	 * @param version_B
	 * @return -1 if A less than B, 1 if A more than B, 0 if equals
	 */
	public static int compareVersions(String version_A, String version_B) throws IllegalArgumentException {

		if (version_A == null || version_B == null) {
			throw new IllegalArgumentException("Version can not be null");
		}
		if (!version_A.matches("[0-9]+(\\.[0-9]+)*") || !version_B.matches("[0-9]+(\\.[0-9]+)*")) {
			throw new IllegalArgumentException("Invalid version format");
		}

		String[] AParts = version_A.split("\\.");
		String[] BParts = version_B.split("\\.");
		int length = Math.max(AParts.length, BParts.length);
		for (int i = 0; i < length; i++) {
			int ACurrPart = i < AParts.length ? Integer.parseInt(AParts[i]) : 0;
			int BCurrPart = i < BParts.length ? Integer.parseInt(BParts[i]) : 0;
			if (ACurrPart < BCurrPart) {
				return -1;
			}
			if (ACurrPart > BCurrPart) {
				return 1;
			}
		}
		return 0;
	}
	
	/**
	 * check if a given version (which can contain letters or underscores) respect
	 * min and max values : if less than max and more(or equal) to min then is OK.
	 * @param version in format n.x.y.z. eccc....
	 * @return
	 */
	public static boolean checkVersionBetweenRange(String version, String max, String min) {
		
		// remove non dots chars and transform undescores to dots
		String noLettersDotsOnly = keepOnlyDotsAndDigits(version);
		
		boolean lessThanMax;
		boolean moreThanMin;
		try {
			lessThanMax = compareVersions(noLettersDotsOnly, max) < 0;
			moreThanMin = compareVersions(noLettersDotsOnly, min) >= 0;
		} catch (IllegalArgumentException e) {
			TestUtils.printl("Error comparing version: illegal format of ["+version+"]" + Arrays.deepToString(e.getStackTrace()));
			return false;
		}
		
		return (lessThanMax && moreThanMin);
	}
	
	public static boolean checkNewVersion(String newVersion, String currentVersion) {
		
		// remove non dots chars and transform undescores to dots
		String noLettersDotsOnlyNew = keepOnlyDotsAndDigits(newVersion);
		String noLettersDotsOnlyCurr = keepOnlyDotsAndDigits(currentVersion);
		
		boolean newAvailable = false;
		try {
			newAvailable = compareVersions(noLettersDotsOnlyCurr, noLettersDotsOnlyNew) < 0;
		} catch (IllegalArgumentException e) {
			TestUtils.printl("Error comparing version: illegal format of A["+noLettersDotsOnlyCurr+"]" 
					+ " or B["+noLettersDotsOnlyNew+"]" + Arrays.deepToString(e.getStackTrace()));
			return false;
		}
		
		return (newAvailable);
	}
	
	public static int getPerc(int val, long tot) {
		float perc = ((float)val / (float)tot) * 100;
		return (int)perc;
	}
	
	public static void updateProgress(String text, int progress, UpdateProgressPanelMin progressUpdater, boolean useGeneralBar) {
		if(progressUpdater != null) {
			if(progressUpdater.secondBarEnabled) {
				progressUpdater.update(progress, text, useGeneralBar);
			}else {
				progressUpdater.update(text, progress);
			}
		}
	}

}
