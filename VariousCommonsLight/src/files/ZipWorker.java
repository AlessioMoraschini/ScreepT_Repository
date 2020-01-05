package files;

import static java.lang.System.out;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import gui.UpdateProgressPanel;
import network.utils.DownloadUtils;

public class ZipWorker {

	public static File zipFolderToFile(String dir2zip, File outputFile, boolean overwrite, boolean excludeParent) {
		
		if(dir2zip != null){
			File dirSrc = new File(dir2zip);
			if(dirSrc.exists() && dirSrc.isDirectory()) {
				try {
				
					//create a ZipOutputStream to zip the data to 
					if(!overwrite && outputFile.exists()) {
						outputFile = Various.renameJavaObjFile(outputFile);
						outputFile.createNewFile();
					}
					
				    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile)); 
				    zipFolder(zos, dirSrc, null, new UpdateProgressPanel(true), true, excludeParent);
				    
				    //close the stream 
				    zos.close();
				    return outputFile;
				    
				}catch(Exception e){
					return null;
				}
			}
		} 
		
		return null;
	}

	public static File zipFolder(String dir2zip, File outputDir, boolean overwrite, boolean excludeParent) {
		
		
		if(dir2zip != null) {
			
			File dirSrc = new File(dir2zip);
			if (dirSrc.exists() && dirSrc.isDirectory()) {
				File outputFile = new File(outputDir.getAbsolutePath() + File.separator + dirSrc.getName() + ".zip");
				return zipFolderToFile(dir2zip, outputFile, overwrite, excludeParent);
			} 
		}
		
		return null;
	}
	
	/**
	 * Internal usage method.
	 */
	private static void zipFolder(
			ZipOutputStream zos,
			File fileToZip,
			String parentDirectoryName,
			UpdateProgressPanel progressUpdater,
			boolean autocloseProgbar,
			boolean excludeParent) throws Exception {
		
	    if (fileToZip == null || !fileToZip.exists()) {
	        return;
	    }
	
	    progressUpdater = (progressUpdater == null)? new UpdateProgressPanel(true) : progressUpdater;
	    FileInputStream fis = null;
	    
	    try {
	    	
	    	if (parentDirectoryName == null) {
				progressUpdater.getDialog().setTitle("Zipping folder: " + fileToZip.getName());
				progressUpdater.getDialog().setMinimumSize(new Dimension(650, 120));
				progressUpdater.getDialog().setPreferredSize(new Dimension(650, 120));
				progressUpdater.getGeneralBar().setIndeterminate(true);
				progressUpdater.getGeneralBar().setValue(50);
				progressUpdater.getGeneralBar().setStringPainted(false);
			}
	    	
			String zipEntryName = fileToZip.getName();
			if (parentDirectoryName!=null && !parentDirectoryName.isEmpty() && !excludeParent) {
			    zipEntryName = parentDirectoryName + "/" + fileToZip.getName();
			}
	
			if (fileToZip.isDirectory()) {
			    System.out.println("+" + zipEntryName);
			    for (File file : fileToZip.listFiles()) {
			    	boolean dontUseParentForSons = excludeParent && parentDirectoryName == null;
			    	zipFolder(zos, file, zipEntryName, progressUpdater, false, dontUseParentForSons);
			    }
			} else {
			    System.out.println("   " + zipEntryName);
			    byte[] buffer = new byte[1024];
			    fis = new FileInputStream(fileToZip);
			    zos.putNextEntry(new ZipEntry(zipEntryName));
			    
			    int fileSize = (int)fileToZip.length();
			    int length;
			    
			    int i = 0;
			    while ((length = fis.read(buffer)) > 0) {
			    	
			    	int percGenZipped = (int)((float)i*100*buffer.length/(float)fileSize);
			    	percGenZipped = percGenZipped >= 100 ? 99 : percGenZipped;
			    	DownloadUtils.updateProgress("Zipping " + fileToZip, percGenZipped, progressUpdater, false);
			        zos.write(buffer, 0, length);
			        
			        i++;
			    }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (autocloseProgbar) {
				progressUpdater.forceClose();
			} else {
				progressUpdater.getDialog().setVisible(true);
			}
			zos.closeEntry();
			if (fis != null) {
				fis.close();
			}
		}
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
	public static File zipFile(String sourcePath, String destFolderPath, boolean ovewrwrite, UpdateProgressPanel progressUpdater) throws Exception {
		
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
			int percDownloaded = DownloadUtils.getPerc(1024*cycles, zipEntry.getSize());
	    	DownloadUtils.updateProgress("Zipping " + zipEntry.getName(), percDownloaded, progressUpdater, false);
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

	/**
	 * Unzip an archive into destination folder. If overwrite is true, then no new unique file will be created, 
	 * but the existing one with same name will be overridden!
	 * 
	 * Use version From downloadUtils
	 * 
	 * @param sourcePath
	 * @param destFolderPath
	 * @param ovewrwrite
	 * @return
	 * @throws Exception
	 */
	public static File unzipFile(String sourcePath, String destFolderPath, boolean ovewrwrite) throws Exception {
		
		UpdateProgressPanel progressUpdater = new UpdateProgressPanel(true);
		
		long startTime = System.currentTimeMillis();
		DownloadUtils.logger.info("FileStuff.unzipFile() -> Starting unzip:"
				+ "\n sourcePath: " + sourcePath
				+ "\n destFolderPath: " + destFolderPath
				+ "\n overwrite: " + ovewrwrite);
		
		if(sourcePath == null || destFolderPath == null || !new File(sourcePath).exists()) {
			DownloadUtils.logger.error("Null input data, cannot proceed.");
			throw new Exception();
		}
		
		// Adapt name in case of overwrite
		File fileToZip = new File(sourcePath);
		int lastDotIndex = fileToZip.getName().lastIndexOf('.');
		String destPathFolderName = fileToZip.getName().substring(0,lastDotIndex);
		String separator = destFolderPath.endsWith("/") || destFolderPath.endsWith(File.separator) ? 
				"" : File.separator;
		destFolderPath += separator + destPathFolderName;
		
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
				DownloadUtils.logger.debug("Unzipping current element: " + zipEntry.getName());
				
				String newOutCurrentPath = destFolderPath + File.separator + zipEntry.getName();
				File out = (ovewrwrite)? new File(newOutCurrentPath) : Various.renameJavaObjFile(new File(newOutCurrentPath));
				if(out.exists()) {
					DownloadUtils.logger.debug("Current output existing: will be deleted! " + out);
					boolean deleted = out.delete();
					DownloadUtils.logger.debug(out + " delete result: " + deleted);
				} else {
					DownloadUtils.logger.debug("Current output does not exist: creating all parent directories needed! " + out.getParentFile());
					out.getParentFile().mkdirs();
				}
				if(zipEntry.isDirectory()) {
					DownloadUtils.logger.debug("Current element is directory, creating and continuing with next! " + zipEntry.getName());
					out.mkdir();
					zipEntry = zis.getNextEntry();
					continue;
				}
				
				if (progressUpdater == null) {
					progressUpdater = new UpdateProgressPanel();
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
				DownloadUtils.updateProgress(zipEntry.getName(), 50, progressUpdater, true);
				
				// For every entry in archive
				DownloadUtils.logger.debug("Creating output stream for current output node: " + out);
			    FileOutputStream fos = new FileOutputStream(out);
			    DownloadUtils.logger.debug("Output stream created! starting unzip...");
			    int len;
			    int cycles = 1;
			    while ((len = zis.read(buffer)) > 0) {
			    	int percDownloaded = DownloadUtils.getPerc(1024*cycles, zipEntry.getSize());
			    	percDownloaded = (percDownloaded>99) ? 99 : percDownloaded;
			    	
			    	DownloadUtils.ifStoppedByUserThrowException(progressUpdater);
			    	DownloadUtils.updateProgress(zipEntry.getName(), percDownloaded, progressUpdater, false);
			    	
			    	// For each byte of current zipped file in archive
			        fos.write(buffer, 0, len);
			        
			        cycles++;
			    }
			    
			    fos.close();
			    DownloadUtils.logger.debug("Current element unzipped! " + zipEntry.getName());
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
			
			DownloadUtils.logger.info("FileStuff.unzipFile() -> COMPLETED!"
					+ "\n source: " + sourcePath
					+ "\n unzipped File: " + out
					+ "\n overwrite: " + ovewrwrite);
			
			DownloadUtils.logger.info("unzip file: elapsed" + String.valueOf(System.currentTimeMillis()-startTime) + " ms");
		} catch (Exception e) {
			
			DownloadUtils.logger.error("An error occurred while unzipping data", e);
			throw e;
			
		} finally {
			if (progressUpdater != null) {
				progressUpdater.update("Completed", 100);
				progressUpdater.forceClose();
			}
		}
	    
	    return destFolderOut;
	}

}
