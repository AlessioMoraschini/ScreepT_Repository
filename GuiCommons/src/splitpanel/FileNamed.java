package splitpanel;

import java.io.File;

public class FileNamed{
	
	public final File mFile;

	public FileNamed(final File file) {
		mFile = file;
	}
	
	public boolean isDirectory() {
        return mFile.isDirectory();
    }
	
	public FileNamed[] listFiles() {
        final File[] files = mFile.listFiles();
        if (files == null) return null;
        if (files.length < 1) return new FileNamed[0];

        final FileNamed[] ret = new FileNamed[files.length];
        for (int i = 0; i < ret.length; i++) {
            final File f = files[i];
            ret[i] = new FileNamed(f);
        }
        return ret;
    }

    public File getFile() {
        return mFile;
    }

    @Override 
    public String toString() {
        return mFile.getName();
    }
}
