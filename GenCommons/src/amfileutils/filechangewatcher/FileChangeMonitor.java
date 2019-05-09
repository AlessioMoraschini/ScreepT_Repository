package amfileutils.filechangewatcher;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileChangeMonitor {

	// FIELDS // 
	private boolean monitorAllContents = false;
	public WatchService watchService;
	public WatchKey watchKeyAll;
	public ArrayList<File> monitoredFileList;
	public File rootMonitorDir;
	
	// CONSTRUCTORS //
	
	public FileChangeMonitor(File rootMonitorFolder, ArrayList<File> monitoredInitialFiles){
		rootMonitorDir = rootMonitorFolder;
		if(monitoredInitialFiles != null){
//			monitoredFileList = monitoredInitialFiles;
			monitoredFileList = new ArrayList<File>();
			// test for folder nested and add files contained to monitor list
			monitoredFileList.addAll(monitoredInitialFiles);
			for(File subFile : monitoredInitialFiles) {
				if(subFile.isDirectory()) {
					addFolderContent(subFile);
				}
			}
		}else{
			monitorAllContents = true;
			monitoredFileList = new ArrayList<File>();
			addFolderContent(rootMonitorDir);
		}
		initialize();
	}
	
	// METHODS //
	
	public void initialize(){
		final Path rootPath = FileSystems.getDefault().getPath(rootMonitorDir.getAbsolutePath());
		try {
			watchService = FileSystems.getDefault().newWatchService();
		    watchKeyAll = rootPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	}
	
	public void addFolderContent(File folder){
		for(File current : folder.listFiles()){
			if(current.isFile()){
				monitoredFileList.add(current);
			}else if(current.isDirectory()){
				addFolderContent(current);
			}
		}
	}
	
	public ChangeDTO checkForEverithing() throws InterruptedException{
		ChangeDTO changedsResponse = new ChangeDTO();
		boolean fileAlreadyAdded = false;
		boolean justAdded = false;
        for (WatchEvent<?> event : watchKeyAll.pollEvents()) {
            
        	WatchEvent<Path> eventPath = (WatchEvent<Path>) event;
        	Path dir = (Path)watchKeyAll.watchable();
            
        	//we only register "ENTRY_MODIFY" so the context is always a Path.
        	final Path changed = dir.resolve(eventPath.context());
        	
            Iterator<File> iteratorMonitored = monitoredFileList.iterator();
            List<File> thingsToBeAddToMonitored = new ArrayList<File>();
            		
            while(iteratorMonitored.hasNext()) {
            	File currentFileInMonitored = iteratorMonitored.next();
            	File currentChange = new File(changed.toUri());
            	
				if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY && !justAdded && currentFileInMonitored.toPath().endsWith(currentChange.getAbsolutePath())) {
					changedsResponse.changedFiles.add(currentChange);
				} else if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE  && !fileAlreadyAdded) {
					changedsResponse.addedFiles.add(currentChange);
					justAdded = true;
					if(monitorAllContents){
						thingsToBeAddToMonitored.add(currentChange);
					}
					fileAlreadyAdded = true;
				} else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE && currentFileInMonitored.toPath().endsWith(currentChange.getAbsolutePath())) {
					changedsResponse.removedFiles.add(currentChange);
					if(monitoredFileList.contains(currentChange)){
						iteratorMonitored.remove();
					}
				} 
			}
            fileAlreadyAdded = false;
            
            monitoredFileList.addAll(thingsToBeAddToMonitored);
        }
        // reset the key
        boolean valid = watchKeyAll.reset();
        if (!valid) {
        	// notify error
        }
        
        return changedsResponse;
	}
	
	public boolean addFileToMonitorList(File fileToAdd){
		if(!monitoredFileList.contains(fileToAdd)){
			monitoredFileList.add(fileToAdd);
			return true;
		}
		else return false;
	}
	
	public void removeFileFromList(int index){
		monitoredFileList.remove(index);
	}
	
	public void removeFileFromList(File fileToRemove){
		monitoredFileList.remove(fileToRemove);
	}
}
