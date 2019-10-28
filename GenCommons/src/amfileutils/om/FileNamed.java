package amfileutils.om;

import java.io.File;
import java.io.IOException;

public class FileNamed{
	
	public final File mFile;

	public FileNamed(final String filePath) {
		mFile = new File(filePath);
	}

	public FileNamed(final File file) {
		mFile = file;
	}
	
	public boolean isDirectory() {
        return mFile != null && mFile.isDirectory();
    }
	
	public FileNamed[] listFiles() {
        if (mFile != null) {
			final File[] files = mFile.listFiles();
			if (files == null)
				return null;
			if (files.length < 1)
				return new FileNamed[0];
			final FileNamed[] ret = new FileNamed[files.length];
			for (int i = 0; i < ret.length; i++) {
				final File f = files[i];
				ret[i] = new FileNamed(f);
			}
			return ret;
		}else {
			return new FileNamed[0];
		}
    }

    public File getFile() {
        return mFile;
    }
    
    public String getFileName() {
        return (mFile != null)? mFile.getName() : "null_file";
    }
    
    public String getFileAbsPath() {
        return (mFile != null)? mFile.getAbsolutePath() : "null_file";
    }
    
    public String getCanonicalAbsPath() throws IOException {
        return (mFile != null)? mFile.getCanonicalPath() : "null_file";
    }

    @Override 
    public String toString() {
        return (mFile != null)? mFile.getName() : "null_file";
    }
}
