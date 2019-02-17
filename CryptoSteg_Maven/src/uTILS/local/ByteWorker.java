package uTILS.local;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ByteWorker {

	/**
	 * DEFAULT CONSTRUCTOR - not yet useful for any method
	 */
	public ByteWorker() {
		
	}
	
	// METHODS 
	
	/**
	 * this method is created to use standard-safe way to convert from string to array of bytes.
	 * @param srcString the source string
	 * @return the byte array equivalent to srcString
	 */
	public static byte[] strToByteArray(String srcString) {
		return srcString.getBytes();
	}
	
	/**
	 * this method is created to use standard-safe way to convert from string to array of bytes.
	 * @param srcString the source string
	 * @param charset the charset to use during conversion
	 * 
	 * @return the byte array equivalent to srcString
	 * 
	 * @throws UnsupportedEncodingException if the charset is not a valid one (use ListCharset class to get a list of available charsets) 
	 */
	public static byte[] strToByteArray(String srcString, String charset) throws UnsupportedEncodingException {
		return srcString.getBytes(charset);
		
	}
	
	/**
	 * this method is created to use standard-safe way to convert from string to arrayList of Byte.
	 * @param srcString the source string
	 * @return the Byte arrayList equivalent to srcString
	 */
	public static ArrayList<Byte> strToByteList(String srcString) {

		byte[] codedBytes = srcString.getBytes();
		ArrayList<Byte> byteList = new ArrayList<Byte>();

		// translate byte[] to ArrayList<Byte>
		for(byte currentByte : codedBytes) {
			byteList.add(currentByte);
		}
		
		return byteList;
		
	}
	
	/**
	 * this method is created to use standard-safe way to convert from byte array to arrayList of Byte.
	 * @param srcString the source array of bytes
	 * @return the ArrayList of Byte obj equivalent to src byte array
	 */
	public static ArrayList<Byte> byteArrToByteList(byte[] srcByteArray) {

		ArrayList<Byte> byteList = new ArrayList<Byte>();

		// translate byte[] to ArrayList<Byte>
		for(byte currentByte : srcByteArray) {
			byteList.add(currentByte);
		}
		
		return byteList;
		
	}
	
	/**
	 * this method is created to use standard-safe way to convert from byte array to arrayList of Byte.
	 * @param srcString the source List of Byte obj
	 * @return the byte array equivalent to srcByteList
	 */
	public static byte[] byteListToByteArray(ArrayList<Byte> srcByteList) {

		byte[] decodedBytes = new byte[srcByteList.size()];
		int i = 0;
		for(Byte currentByte : srcByteList) {
			decodedBytes[i] = currentByte;
			i++;
		}
		
		return decodedBytes;
		
	}
	
	
	
	// GETTERS AND SETTERS
	
}
