import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class fileBufferRead {

	private static final int BUFFER_SIZE = 1024*1024;
	
	public static void fileBufferedWorker(File srcFile) throws IOException {
		
		FileInputStream streamIN = new FileInputStream(srcFile);
		byte[] buffer = new byte[BUFFER_SIZE];
		
		// this integer will contain value -1 when file is ended 
		int readFlag = streamIN.read(buffer);
		
		while(readFlag > 0) {
			
			// do work with bufferBytes
			
			readFlag = streamIN.read(buffer);
		}
		
		streamIN.close();
		
	}
	
}
