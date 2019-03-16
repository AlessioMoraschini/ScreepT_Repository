package launcherutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class FileUtils {
	
	
	public static boolean writeStringToFile(String source, File outFile, boolean overwrite) throws IOException {
		if(outFile.exists()) {
			if (overwrite) {
				boolean deleted = outFile.delete();
				boolean recreated = outFile.createNewFile();
				if(!recreated || !deleted) {
					return false;
				}
			}else {
				return false;
			}
		}else {
			boolean recreated = outFile.createNewFile();
			if(!recreated) {
				return false;
			}
		}
		
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outFile), System.getProperty("file.encoding"));
		PrintWriter out = new PrintWriter(outputStreamWriter);
		out.write(source);
		
		out.close();
		outputStreamWriter.close();
		
		return true;
	}
}
