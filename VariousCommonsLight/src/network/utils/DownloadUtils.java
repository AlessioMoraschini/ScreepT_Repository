package network.utils;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import utility.ConversionUtils;
import utility.SafeLogger;
import gui.GuiUtils;
import gui.UpdateProgressPanel;
import network.http.HttpHelper;
import om.exception.ProgressBarInterruptedException;

import static java.lang.System.out;

public class DownloadUtils {
	
	public static SafeLogger logger = new SafeLogger(DownloadUtils.class);
	
	public static int CONNECTION_TIMEOUT_MS = 5000;
	
	public static File downloadFile(boolean silent, URL url, String localPath, UpdateProgressPanel progressUpdater) throws Exception, ProgressBarInterruptedException{
		int fileSize = HttpHelper.getFileSize(url);
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
				GuiUtils.centerComponent(progressUpdater.getDialog());
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
	
	private static String keepOnlyDotsAndDigits(String source) {
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
	private static int compareVersions(String version_A, String version_B) throws IllegalArgumentException {

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
	
	public static boolean compareNewVersion(String newVersion, String currentVersion) {
		
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
		
		String totalStr = ConversionUtils.coolFileSize(tot);
		String downloadedStr = ConversionUtils.coolFileSize((long)(tot * perc) / 100);
		
		return downloadedStr + "/" + totalStr;
	}
	
	public static void updateProgress(String text, int progress, UpdateProgressPanel progressUpdater, boolean useGeneralBar) throws ProgressBarInterruptedException {
		if(progressUpdater != null) {
			if(progressUpdater.secondBarEnabled) {
				progressUpdater.update(progress, text, useGeneralBar);
			}else {
				progressUpdater.update(text, progress);
			}
		}
	}
	
	public static void ifStoppedByUserThrowException(UpdateProgressPanel progressMonitor) throws ProgressBarInterruptedException {
		if(progressMonitor != null && progressMonitor.closable.get() && progressMonitor.stopProcess.get()) {
			progressMonitor.forceCloseNExceptionSignal();
		}
	}

}
