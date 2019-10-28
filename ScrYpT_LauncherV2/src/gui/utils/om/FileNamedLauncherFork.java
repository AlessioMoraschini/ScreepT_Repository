package gui.utils.om;

import java.io.File;
import java.io.IOException;

public class FileNamedLauncherFork{
	
	public final File mFile;

	public FileNamedLauncherFork(final String filePath) {
		mFile = new File(filePath);
	}

	public FileNamedLauncherFork(final File file) {
		mFile = file;
	}
	
	public boolean isDirectory() {
        return mFile.isDirectory();
    }
	
	public FileNamedLauncherFork[] listFiles() {
        final File[] files = mFile.listFiles();
        if (files == null) return null;
        if (files.length < 1) return new FileNamedLauncherFork[0];

        final FileNamedLauncherFork[] ret = new FileNamedLauncherFork[files.length];
        for (int i = 0; i < ret.length; i++) {
            final File f = files[i];
            ret[i] = new FileNamedLauncherFork(f);
        }
        return ret;
    }

    public File getFile() {
        return mFile;
    }
    
    public String getFileName() {
        return mFile.getName();
    }
    
    public String getFileAbsPath() {
        return mFile.getAbsolutePath();
    }
    
    public String getCanonicalAbsPath() throws IOException {
        return mFile.getCanonicalPath();
    }

    @Override 
    public String toString() {
        return mFile.getName();
    }
}
