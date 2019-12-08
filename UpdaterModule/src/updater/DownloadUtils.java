package updater;

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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.DatatypeConverter;

import exception.ProgressBarInterruptedException;
import forks.merged.PropertiesManagerFork;
import forks.merged.Various;
import gui.UpdateProgressPanelMin;

import static java.lang.System.out;

public class DownloadUtils {
	
	public static int CONNECTION_TIMEOUT_MS = 5000;
	
	/**
	 * Retrieve the size in bits of a file located at given remote URL
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
				conn.setConnectTimeout(CONNECTION_TIMEOUT_MS);
				if(conn instanceof HttpURLConnection) {
				    ((HttpURLConnection)conn).setRequestMethod("HEAD");
				}
				conn.getInputStream();
			} catch (Exception e) {
				throw new TimeoutException("Server unreachable!");
			}
	        out.println("=> retrieving file size");
	        int size = conn.getContentLength();
	        out.println("=> Found size: " + Various.coolFileSize((long)size));
	        return size;
	    } catch (Exception e) {
	    	out.println("An error occurred: " + Arrays.deepToString(e.getStackTrace()));
	        throw e;
	    } finally {
	        if(conn instanceof HttpURLConnection) {
	            ((HttpURLConnection)conn).disconnect();
	        }
	    }
	    
	}
	
	public static File downloadFile(boolean silent, URL url, String localPath, UpdateProgressPanelMin progressUpdater) throws Exception, ProgressBarInterruptedException{
		int fileSize = getFileSize(url);
		out.println("Opening url stream...");
		BufferedInputStream in = null;
		FileOutputStream fileOutputStream = null;
		
		try {
			if(progressUpdater != null && !silent) {
				progressUpdater.getDialog().setTitle("Downloading update file: " + new File(url.getFile()).getName());
				progressUpdater.getDialog().setMinimumSize(new Dimension(650, 120));
				progressUpdater.getDialog().setVisible(true);
				progressUpdater.getPb().setIndeterminate(false);
				progressUpdater.getDialog().setPreferredSize(new Dimension(650, 120));
				Various.centerComponent(progressUpdater.getDialog());
				progressUpdater.setClosable(true);
			}
			
			in = new BufferedInputStream(url.openStream());
			fileOutputStream = new FileOutputStream(localPath);
			int bufferSIze = 1024;
			byte dataBuffer[] = new byte[bufferSIze];
			int bytesRead;
			int cycles = 1;
			
			while ((bytesRead = in.read(dataBuffer, 0, bufferSIze)) != -1) {
			    fileOutputStream.write(dataBuffer, 0, bytesRead);
			    int percDownloaded = getPerc(bufferSIze*cycles, fileSize);
			    percDownloaded = (percDownloaded>99) ? 99 : percDownloaded;
			    ifStoppedByUserThrowException(progressUpdater);
			    String msg = "Downloading " + new File(url.getFile()).getName() + " ("+getPercSizesString(bufferSIze*cycles, fileSize)+")";
				updateProgress(msg, percDownloaded, progressUpdater, false);
			    cycles ++;
			}
			
			
		} catch (Exception e) {
			if (e instanceof ProgressBarInterruptedException) {
				throw e;
			}
		} finally {
			
			try {
				out.println("Closing url stream...");
				in.close();
				fileOutputStream.close();
				updateProgress("Download Completed!", 100, progressUpdater, false);
				out.println("Streams closed!");
			} catch (IOException e) {
				out.println("An error occurred while closing streams after download" + Arrays.deepToString(e.getStackTrace()));
			}
			
			if (progressUpdater != null) {
				progressUpdater.forceClose();
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
		out.println("FileStuff.unzipFile() -> Starting unzip:"
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
					progressUpdater.setClosable(true);
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
			    	
			    	ifStoppedByUserThrowException(progressUpdater);
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
			
			out.println("FileStuff.unzipFile() -> COMPLETED!"
					+ "\n source: " + sourcePath
					+ "\n unzipped File: " + out
					+ "\n overwrite: " + ovewrwrite);
			
			out.println("unzip file: elapsed" + String.valueOf(System.currentTimeMillis()-startTime) + " ms");
		} catch (Exception e) {
			
			out.println(Arrays.deepToString(e.getStackTrace()));
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
		out.println("FileStuff.zipFile() -> Starting zip:"
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
		
		System.out.println("FileStuff.zipFile() -> COMPLETED!"
				+ "\n source: " + sourcePath
				+ "\n zipped File: " + out
				+ "\n overwrite: " + ovewrwrite);
		
		System.out.println("zipFile -> Finished in " + String.valueOf(System.currentTimeMillis()-startTime) + " ms");
		
		return out;
	}
	
	public static File zipFolder(String dir2zip, File outputDir, boolean overwrite) {
		
		if(dir2zip != null){
			File dirSrc = new File(dir2zip);
			if(dirSrc.exists() && dirSrc.isDirectory()) try {
				
				//create a ZipOutputStream to zip the data to 
				File output = new File(outputDir.getAbsolutePath() + File.separator + dirSrc.getName() + ".zip");
				if(!overwrite && output.exists()) {
					output = Various.renameJavaObjFile(output);
					output.createNewFile();
				}
				
			    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output)); 
			    zipFolder(dir2zip, zos, new UpdateProgressPanelMin(true), true); 
			    
			    //close the stream 
			    zos.close();
			    return output;
			}catch(Exception e){
				return null;
			}
		} 
		
		return null;
	}
	
	public static boolean zipFolder(String dir2zip, ZipOutputStream zos, UpdateProgressPanelMin progressUpdater, boolean autocloseProgbar) throws Exception{
		// create a new File object based on the directory we
		boolean result = true;
		
		progressUpdater = (progressUpdater == null)? new UpdateProgressPanelMin(true) : progressUpdater;
		
		try {
			progressUpdater.getDialog().setTitle("Zipping directory: " + new File(dir2zip).getName());
			progressUpdater.getDialog().setMinimumSize(new Dimension(650, 120));
			progressUpdater.getDialog().setPreferredSize(new Dimension(650, 120));
			progressUpdater.getGeneralBar().setIndeterminate(false);
			Various.centerComponent(progressUpdater.getDialog());
			
			File zipDir = new File(dir2zip);
			// get a listing of the directory content
			String[] dirList = zipDir.list();
			byte[] readBuffer = new byte[2156];
			int bytesIn = 0;
			// loop through dirList, and zip the files
			for (int i = 0; i < dirList.length; i++) {
				int percGenZipped = (int)((float)i*100/(float)dirList.length);
		    	updateProgress("Zipping " + dirList[i], percGenZipped, progressUpdater, true);
		    	progressUpdater.getPb().setIndeterminate(true);
		    	
				File f = new File(zipDir, dirList[i]);
				if (f.isDirectory()) {
					// if the File object is a directory, call this
					// function again to add its content recursively
					String filePath = f.getPath();
					boolean tempResult = zipFolder(filePath, zos, progressUpdater, false);
					result = (tempResult == false)? false : result;
					
					// loop again
					continue;
				}
				// if we reached here, the File object f was not
				// create a FileInputStream on top of f
				FileInputStream fis = new FileInputStream(f);
				ZipEntry anEntry = new ZipEntry(f.getPath());
				// place the zip entry in the ZipOutputStream object
				zos.putNextEntry(anEntry);
				
				// now write the content of the file to the ZipOutputStream
				while ((bytesIn = fis.read(readBuffer)) != -1) {
					
					updateProgress("Zipping " + dirList[i], percGenZipped, progressUpdater, true);
					
					zos.write(readBuffer, 0, bytesIn);
				}
				// close the Stream
				fis.close();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (autocloseProgbar) {
				progressUpdater.forceClose();
			} else {
				progressUpdater.getDialog().setVisible(true);
			}
		}
		
		return result;
	}
	
	public static boolean isUrlReachable(URL urlToTest) {
		try {
			return InetAddress.getByName(urlToTest.getHost()).isReachable(5000);
		} catch (IOException e) {
			return false;
		}
	}
	
	// HASHING //
	public static String getFileHash(String filePath, HashingFunction algorithm) throws Throwable {
		
		if(filePath == null) return "";
		
		System.out.println("Generating hash for file: " + filePath + " with algorithm:"+algorithm);
	         
	    MessageDigest messageDigest = MessageDigest.getInstance(algorithm.algorithmCode);
	    messageDigest.update(Files.readAllBytes(Paths.get(filePath)));
	    byte[] digest = messageDigest.digest();
	    
	    String result = DatatypeConverter.printHexBinary(digest).toUpperCase();
	    System.out.println("Generated hash for file: " + filePath + ", HASHED:"+result);
	    
	    return result;
	}

	public static String getFileHash(File file, HashingFunction algorithm, boolean uppercase) throws Throwable {
		
		if(file == null) return "";

		System.out.println("Generating hash for file: " + file.getAbsolutePath() + " with algorithm:"+algorithm);
		
		MessageDigest messageDigest = MessageDigest.getInstance(algorithm.algorithmCode);
		messageDigest.update(Files.readAllBytes(file.toPath()));
		byte[] digest = messageDigest.digest();
		
		String result;
		try {
			// NB: hex is not case sensitive!
			result = DatatypeConverter.printHexBinary(digest);
			result = (uppercase) ? result.toUpperCase() : result.toLowerCase();
		} catch (Error e) {
			result = "";
		}
		
		System.out.println("Generated hash for file: " + file.getAbsolutePath() + ", HASHED:"+result);
		
		return result;
	}
	
	public static String getFolderHash(File directory, HashingFunction algorithm, boolean uppercase) throws Throwable {
		if(directory == null) {
			return "";
		} else if (directory.isFile()) {
			return getFileHash(directory, algorithm, uppercase);
		} else {
			String hashAccumulator = "";
			for (File curr : directory.listFiles()) {
				if (curr.isDirectory()) {
					hashAccumulator = new String(xorBytes(hashAccumulator.getBytes("UTF-8"), getFolderHash(curr, algorithm, uppercase).getBytes("UTF-8")));
				} else {
					hashAccumulator = new String(xorBytes(hashAccumulator.getBytes("UTF-8"), getFileHash(curr, algorithm, uppercase).getBytes("UTF-8")));
				}
			}
			return hashAccumulator;
		}
	}
	
	public static byte[] xorBytes(byte[] first, byte[] second) {
		
		// Select longest as first
		byte[] A = first.length >= second.length ? first : second;
		byte[] B = A.length == first.length ? second : first;
		byte[] xorResult = new byte[A.length];

		int i = 0;
		for (byte firstiByte : A) {
			xorResult[i] = (i < B.length) ? (byte) (firstiByte ^ B[i]) : firstiByte;
			i++;
		}
		
		return xorResult;
	}
	
	//Add salt
    public static byte[] getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

	// HASHING //
    public static boolean checkHashesFromProperties(List<File> filesToCheck, PropertiesManagerFork propsManifest, HashingFunction hashingFunction) throws Throwable {
    	if(filesToCheck == null || filesToCheck.isEmpty()) {
    		return true;
    	}
    	
    	boolean checked = true;
    	String checksum = "";
    	boolean tempResult = false;
    	
    	for(File current : filesToCheck) {
    		checksum = propsManifest.getProperty(current.getName());
    		if (checksum != null && !"".equals(checksum.trim())) {
				tempResult = checkFileHash(current, checksum, hashingFunction);
				checked = tempResult == false ? false : checked;
			}
    	}
    	
    	return checked;
    }
    
    public static boolean checkHashFromProperties(File toCheck, PropertiesManagerFork propsManifest, HashingFunction hashingFunction) throws Throwable {
		if(propsManifest == null || toCheck == null || !toCheck.exists()) {
			return false;
		} else {
			String checksum = propsManifest.getProperty(toCheck.getName());
			if (checksum != null && !"".equals(checksum.trim())) {
				// If checksum defined then check current file for it
				return DownloadUtils.checkFileHash(toCheck, checksum, hashingFunction);
			} else {
				// If reference checksum not defined continue
				return true;
			}
		}
	}
    
	public static boolean checkFileHash(String filePath, String checksum, HashingFunction algorithm) throws Throwable {
		
		if(filePath == null || checksum == null) return false;
		
		MessageDigest messageDigest = MessageDigest.getInstance(algorithm.algorithmCode);
		messageDigest.update(Files.readAllBytes(Paths.get(filePath)));
		byte[] digest = messageDigest.digest();
		String myChecksum = DatatypeConverter.printHexBinary(digest).toUpperCase();
		
		return myChecksum.equalsIgnoreCase(checksum) || checksum.contains(myChecksum);
	}

	public static boolean checkFileHash(File file, String checksum, HashingFunction algorithm) throws Throwable {
		
		if(file == null || checksum == null) return false;
		
		MessageDigest messageDigest = MessageDigest.getInstance(algorithm.algorithmCode);
		messageDigest.update(Files.readAllBytes(file.toPath()));
		byte[] digest = messageDigest.digest();
		String myChecksum = DatatypeConverter.printHexBinary(digest).toUpperCase();
		
		return myChecksum.equalsIgnoreCase(checksum) || checksum.contains(myChecksum);
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
		
		String cleanVersA = keepOnlyDotsAndDigits(version_A);
		String cleanVersB = keepOnlyDotsAndDigits(version_B);
		
		if (!cleanVersA.matches("[0-9]+(\\.[0-9]+)*") || !cleanVersB.matches("[0-9]+(\\.[0-9]+)*")) {
			throw new IllegalArgumentException("Invalid version format");
		}

		String[] AParts = cleanVersA.split("\\.");
		String[] BParts = cleanVersB.split("\\.");
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
			out.println("Error comparing version: illegal format of ["+version+"]" + Arrays.deepToString(e.getStackTrace()));
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
			out.println("Error comparing version: illegal format of A["+noLettersDotsOnlyCurr+"]" 
					+ " or B["+noLettersDotsOnlyNew+"]" + Arrays.deepToString(e.getStackTrace()));
			return false;
		}
		
		return (newAvailable);
	}
	
	public static int getPerc(int val, long tot) {
		double perc = ((double)val / (double)tot) * 100;
		return (int)perc;
	}

	public static int getPerc(long val, long tot) {
		float perc = ((float)val / (float)tot) * 100;
		return (int)perc;
	}

	public static String getPercSizesString(int val, long tot) {
		float perc = ((float)val / (float)tot) * 100;
		
		String totalStr = Various.coolFileSize(tot);
		String downloadedStr = Various.coolFileSize((long)(tot * perc) / 100);
		
		return downloadedStr + "/" + totalStr;
	}
	
	public static void updateProgress(String text, int progress, UpdateProgressPanelMin progressUpdater, boolean useGeneralBar) throws ProgressBarInterruptedException {
		if(progressUpdater != null) {
			if(progressUpdater.secondBarEnabled) {
				progressUpdater.update(progress, text, useGeneralBar);
			}else {
				progressUpdater.update(text, progress);
			}
		}
	}
	
	public static void ifStoppedByUserThrowException(UpdateProgressPanelMin progressMonitor) throws ProgressBarInterruptedException {
		if(progressMonitor != null && progressMonitor.closable.get() && progressMonitor.stopProcess.get()) {
			progressMonitor.forceCloseNExceptionSignal();
		}
	}

}
