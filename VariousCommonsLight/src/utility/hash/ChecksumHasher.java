package utility.hash;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import utility.PropertiesManagerLight;
import utility.SafeLogger;

public class ChecksumHasher {

	public static SafeLogger logger = new SafeLogger(ChecksumHasher.class);
	
	public static boolean equalsFilesChecksum(File A, File B, HashingFunction algorithm) throws Throwable {
		
		String hashA = ChecksumHasher.getFileHash(A, algorithm, true);
		String hashB = ChecksumHasher.getFileHash(B, algorithm, true);
		
		return hashA.equals(hashB);
	}

	public static boolean checkFileHash(String filePath, String checksum, HashingFunction algorithm) throws Throwable {
		
		if(filePath == null || checksum == null) return false;
		
		MessageDigest messageDigest = MessageDigest.getInstance(algorithm.algorithmCode);
		messageDigest.update(Files.readAllBytes(Paths.get(filePath)));
		byte[] digest = messageDigest.digest();
		String myChecksum = DatatypeConverter.printHexBinary(digest).toUpperCase();
		
		return myChecksum.equalsIgnoreCase(checksum) || checksum.contains(myChecksum);
	}

	public static boolean checkFileHash(File file, String checksum, HashingFunction algorithm) throws Throwable {
		
		if(file == null || checksum == null) return false;
		
		MessageDigest messageDigest = MessageDigest.getInstance(algorithm.algorithmCode);
		messageDigest.update(Files.readAllBytes(file.toPath()));
		byte[] digest = messageDigest.digest();
		String myChecksum = DatatypeConverter.printHexBinary(digest).toUpperCase();
		
		return myChecksum.equalsIgnoreCase(checksum) || checksum.contains(myChecksum);
	}

	public static String getFileHash(File file, HashingFunction algorithm, boolean uppercase) throws Throwable {
		
		if(file == null) return "";
	
		logger.info("Generating hash for file: " + file.getAbsolutePath() + " with algorithm:"+algorithm);
		
		MessageDigest messageDigest = MessageDigest.getInstance(algorithm.algorithmCode);
		messageDigest.update(Files.readAllBytes(file.toPath()));
		byte[] digest = messageDigest.digest();
		
		String result;
		try {
			// NB: hex is not case sensitive!
			result = DatatypeConverter.printHexBinary(digest);
			result = (uppercase) ? result.toUpperCase() : result.toLowerCase();
		} catch (Error e) {
			result = "";
		}
		
		logger.info("Generated hash for file: " + file.getAbsolutePath() + ", HASHED:"+result);
		
		return result;
	}

	public static String getFolderHash(File directory, HashingFunction algorithm, boolean uppercase) throws Throwable {
		if(directory == null) {
			return "";
		} else if (directory.isFile()) {
			return getFileHash(directory, algorithm, uppercase);
		} else {
			String hashAccumulator = "";
			for (File curr : directory.listFiles()) {
				if (curr.isDirectory()) {
					hashAccumulator = new String(ChecksumHasher.xorBytes(hashAccumulator.getBytes("UTF-8"), getFolderHash(curr, algorithm, uppercase).getBytes("UTF-8")));
				} else {
					hashAccumulator = new String(ChecksumHasher.xorBytes(hashAccumulator.getBytes("UTF-8"), getFileHash(curr, algorithm, uppercase).getBytes("UTF-8")));
				}
			}
			return hashAccumulator;
		}
	}

	// HASHING //
	public static String getFileHash(String filePath, HashingFunction algorithm) throws Throwable {
		
		if(filePath == null) return "";
		
		System.out.println("Generating hash for file: " + filePath + " with algorithm:"+algorithm);
	         
	    MessageDigest messageDigest = MessageDigest.getInstance(algorithm.algorithmCode);
	    messageDigest.update(Files.readAllBytes(Paths.get(filePath)));
	    byte[] digest = messageDigest.digest();
	    
	    String result = DatatypeConverter.printHexBinary(digest).toUpperCase();
	    System.out.println("Generated hash for file: " + filePath + ", HASHED:"+result);
	    
	    return result;
	}

	public static byte[] xorBytes(byte[] first, byte[] second) {
		
		// Select longest as first
		byte[] A = first.length >= second.length ? first : second;
		byte[] B = A.length == first.length ? second : first;
		byte[] xorResult = new byte[A.length];
	
		int i = 0;
		for (byte firstiByte : A) {
			xorResult[i] = (i < B.length) ? (byte) (firstiByte ^ B[i]) : firstiByte;
			i++;
		}
		
		return xorResult;
	}

	//Add salt
	public static byte[] getSalt() throws NoSuchAlgorithmException
	{
	    SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
	    byte[] salt = new byte[16];
	    sr.nextBytes(salt);
	    return salt;
	}

	// HASHING //
	public static boolean checkHashesFromProperties(List<File> filesToCheck, PropertiesManagerLight propsManifest, HashingFunction hashingFunction) throws Throwable {
		if(filesToCheck == null || filesToCheck.isEmpty()) {
			return true;
		}
		
		boolean checked = true;
		String checksum = "";
		boolean tempResult = false;
		
		for(File current : filesToCheck) {
			checksum = propsManifest.getProperty(current.getName());
			if (checksum != null && !"".equals(checksum.trim())) {
				tempResult = checkFileHash(current, checksum, hashingFunction);
				checked = tempResult == false ? false : checked;
			}
		}
		
		return checked;
	}

	public static boolean checkHashFromProperties(File toCheck, PropertiesManagerLight propsManifest, HashingFunction hashingFunction) throws Throwable {
		if(propsManifest == null || toCheck == null || !toCheck.exists()) {
			return false;
		} else {
			String checksum = propsManifest.getProperty(toCheck.getName());
			if (checksum != null && !"".equals(checksum.trim())) {
				// If checksum defined then check current file for it
				return checkFileHash(toCheck, checksum, hashingFunction);
			} else {
				// If reference checksum not defined continue
				return true;
			}
		}
	}
	
}
