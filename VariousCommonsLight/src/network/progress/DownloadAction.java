package network.progress;

public interface DownloadAction {
	public default void doAfterDownloadProgress(int percent, String currentFile) {
		System.out.println("Downloading File: " + currentFile + "Current progress is " + percent);
	};
}
