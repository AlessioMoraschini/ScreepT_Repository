package network.progress;

public interface UploadAction {
	public default void doAfterUploadProgress(int percent, String currentFile) {
		System.out.println("Uploading File: " + currentFile + "Current progress is " + percent);
	};
}
