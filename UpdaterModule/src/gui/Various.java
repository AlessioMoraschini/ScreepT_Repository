package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Toolkit;
import java.io.File;
import java.text.DecimalFormat;

import javax.swing.JFrame;

public class Various {
	
	public static Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	public static Dimension getScreenSize(Component frame) {
		DisplayMode mode = frame.getGraphicsConfiguration().getDevice().getDisplayMode();
		return new Dimension(mode.getWidth(), mode.getHeight());
	}
	
	public static Dimension getScreenSizePerc(int factorPerc, JFrame frame) {
		Dimension screen = getScreenSize(frame);
		int w = (screen.width*factorPerc)/100;
		int h = (screen.height*factorPerc)/100;
		return new Dimension(w, h);
	}
	
	public static Dimension getScreenSize(int marginLeftRight) {
		Dimension screen = getScreenSize();
		return new Dimension(screen.width-(marginLeftRight+marginLeftRight), screen.height);
	}
	
	public static void centerComponent(Component targetComponent, int percentSize) {
		float percent = (float)percentSize/100;
		Dimension screenSize = (targetComponent instanceof JFrame)? getScreenSize((JFrame)targetComponent): getScreenSize();
		int W = (int)(screenSize.width * percent);
		int H = (int)(screenSize.height * percent);
		targetComponent.setSize(W, H);
		targetComponent.setBounds(screenSize.width/2-W/2, screenSize.height/2-H/2, W, H);
	}
	
	public static void centerComponent(Component targetFrame, int W, int H) {
		Dimension screenSize = (targetFrame instanceof JFrame)? getScreenSize((JFrame)targetFrame): getScreenSize();
		targetFrame.setSize(W, H);
		targetFrame.setBounds(screenSize.width/2-W/2, screenSize.height/2-H/2, W, H);
	}
	
	public static void centerComponent(Component targetFrame) {
		if(targetFrame == null) {
			return;
		}
		Dimension screenSize = (targetFrame instanceof JFrame)? getScreenSize((JFrame)targetFrame): getScreenSize();
		Dimension frameSize = null;
		frameSize = targetFrame.getSize();
		int W = frameSize.width;
		int H = frameSize.height;
		
		targetFrame.setBounds(screenSize.width/2-W/2, screenSize.height/2-H/2, W, H);
	}
	
	public static void moveFrame(Component element, int xDiff, int yDiff) {
		int totX = (element.getX() + xDiff);
		int totY = (element.getY() + yDiff);
		int newX = (totX < 0)? 0 : totX;
		int newY = (totY < 0)? 0 : totY;
		Dimension screenSize = (element instanceof JFrame)? getScreenSize((JFrame)element): getScreenSize();
		int maxDeltaX = screenSize.width - newX - element.getWidth();
		int maxDeltaY = screenSize.height - newY - element.getHeight();
		
		newX = (maxDeltaX < 0)? screenSize.width - element.getWidth() : newX;
		newY = (maxDeltaY < 0)? screenSize.height - element.getHeight() : newY;
		
		element.setBounds(newX, newY, element.getWidth(), element.getHeight());
	}
	
	/**
	 *  return a string containing the size of the file formatted correctly according to his size
	 * 
	 * @return cool formatted string representing the file size with correct measure unit
	 */
	public static String coolFileSize(File file){
		long size = file.length();
		if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB", "ExaB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.##").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	/**
	 *  return a string containing the size formatted correctly according to his size
	 * @param size is the size in bytes to represent in cool way
	 * @return cool formatted string representing the file size with correct measure unit
	 */
	public static String coolFileSize(Long size){
		if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB", "ExaB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.##").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	/**
	 *  return a string containing the size formatted correctly in bits according to his size
	 * @param size is the size in bytes to represent in cool way
	 * @return cool formatted string representing the file size in bits with correct measure unit
	 */
	public static String coolFileSizeBits(File file){
		long size = file.length();
		if(size <= 0) return "0";
	    final String[] units = new String[] { "b", "kb", "Mb", "Gb", "Tb", "Exab" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.##").format(size*8/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	/**
	 *  return a string containing the size formatted correctly in bits according to his size
	 * @param size is the size in bytes to represent in cool way
	 * @return cool formatted string representing the file size in bits with correct measure unit
	 */
	public static String coolFileSizeBits(Long size){
		if(size <= 0) return "0";
	    final String[] units = new String[] { "b", "kb", "Mb", "Gb", "Tb", "Exab" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.##").format(size*8/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}
