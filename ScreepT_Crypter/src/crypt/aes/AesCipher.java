package crypt.aes;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JPasswordField;
import javax.swing.text.BadLocationException;

import utility.BitWorker;
import utility.ConversionUtils;
import utility.EncodingUtils;
import utility.EntrophyzerUtils;
import utility.hash.SafeHasher;
import utility.hash.SafeHasher.HashAlgorithm;
import utility.hash.SafeHasher.IterationsLevel;

public class AesCipher {
	
	private static final IterationsLevel iter = IterationsLevel.MED_LOW;
	private static final HashAlgorithm algo = HashAlgorithm.PBKDF2SHA512;
	
	public static final int limit = 16;

	//////////////////////
	//      CAMPI       //
	//////////////////////
	
	private EncodingUtils encodingUtils;

	//valore aggiornato con i pad
	private char[] password;
	
	//valore originale chiave inserita
	private char[] displayedKey;

	private IvParameterSpec ivParameterSpec;
	private SecretKeySpec secretKeySpec;
	
	private Cipher cipher;
	
	//////////////////////
	//    COSTRUTTORI   //  
	//////////////////////
	
	public AesCipher(char[] password) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException {
		
		encodingUtils = new EncodingUtils();
		displayedKey=password;
		this.password= new char[limit];
		int i = 0;
		
		// Pad with zeros
		while(i < limit) {
		    	if(i<password.length) {
		    		this.password[i] = password[i];
		    	}else {
		    		this.password[i] = '0';
		    	}
		    	i++;
		}
		
		// initialize salt, initializationVector and keySpec, both hashed and normal versions
		// (String source encryption isn't hashed, while byte source version it's hashed and salted)
		initIvAndParameterSpec(this.password);
	    cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	}
	
	private void initIvAndParameterSpec(char[] psw) {
		
		// Prepare bytes from various sources with omogeneus diffusion from source characters
		byte[] bytePsw = charKeyToByteArrayV2(psw);
//		byte[] bytePsw = HandleString.extractSameLengthByteArray(password); // Old version - less secure
		
		// Normal specs
		ivParameterSpec = new IvParameterSpec(bytePsw);
		secretKeySpec = new SecretKeySpec(Arrays.copyOf(bytePsw, limit), "AES");
		
		Arrays.fill(bytePsw, (byte)0);
	}
	
	//////////////////////
	//      METODI      //
	//////////////////////
	
	/**
	 * Questo metodo si occupa di codificare tramite AES-128 una stringa passata come parametro,
	 * (la converte in byte[], converte con "Cipher", dopodich� riconverte in string).
	 * 
	 * @param sourceString
	 * @return encryptedString
	 * 
	 * */
	public String encrypt(String toBeEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException,
	        InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
	    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
	    byte[] encrypted = cipher.doFinal(toBeEncrypt.getBytes(EncodingUtils.stringEncoding));
	    return encodingUtils.encodeBase64(encrypted);
	}
	
	public String encryptLines(String toBeEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException,
	    InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		String[] sourceLines = toBeEncrypt.split("\n");
		byte[] encrypted;
		StringBuilder builder = new StringBuilder(10000);
		for(int i=0; i<sourceLines.length; i++) {
			encrypted = cipher.doFinal(sourceLines[i].getBytes(EncodingUtils.stringEncoding));
			if (i == sourceLines.length - 1) {
				builder.append(encodingUtils.encodeBase64(encrypted));
			} else {
				builder.append(encodingUtils.encodeBase64(encrypted)).append("\n");
			}
		}
		return builder.toString();
	}
	
	/**
	 * Questo metodo si occupa di codificare tramite AES un array di byte passato come parametro. 
	 * A differenza del metodo precedente ritorna anche un valore in formato byte[] 
	 * 
	 * @param sourceByte[]
	 * @return encryptedByte[]
	 * @throws BadLocationException 
	 * */
	public byte[] encrypt(byte[] toBeEncrypt, boolean hashed, boolean shuffleResult) throws NoSuchPaddingException, NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, BadLocationException {
		
		// generate random initialization vector
		byte[] iv = generateIV(16);
		
		// STORE MODE INFO into first 2 bits of IV : {IV_BIT(0)= [hashed] ; IV_BIT(1)= [shuffle]}
		// Save in the first bit information about hashed (0) or not hashed(1)
		iv[0] = BitWorker.setBitAtPosition(iv[0], 0, hashed);
//		System.out.println("hashed: " + hashed);
		
		// Use the second bit to store information about shuffle mode on(1)/off(0)
		iv[0] = BitWorker.setBitAtPosition(iv[0], 1, shuffleResult);
//		System.out.println("shuffleResult: " + shuffleResult);
		
		ivParameterSpec = new IvParameterSpec(iv);
		
		// Configure cipher
		if (hashed) {
			// Use iv as salt to hash password
			cipher.init(Cipher.ENCRYPT_MODE, getSaltedKeySpec(iv), ivParameterSpec);
		} else {
			// Use normal iv without hashing password
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		}
		
		byte[] encrypted = cipher.doFinal(toBeEncrypt);
		
		if(shuffleResult) {
			encrypted = shuffle(iv, encrypted);
		}
		
		// insert iv at the start of encrypted msg: RESULT = IV + ENCRYPTED
	    byte[] result = new byte[iv.length + encrypted.length];
	    System.arraycopy(iv, 0, result, 0, iv.length);
	    System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
		
		return result;
	}
	
	/**
	 * Questo metodo si occupa di decodificare tramite AES una stringa passata come parametro,
	 * (la converte in byte[], converte con "Cipher", dopodich� riconverte in string).
	 * 
	 * @param sourceString
	 * @return encryptedString
	 * @throws UnsupportedEncodingException 
	 * */
	public String decrypt(String encrypted) throws InvalidAlgorithmParameterException, InvalidKeyException,
	        BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
		
	    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
	    byte[] decryptedBytes = cipher.doFinal(encodingUtils.decodeBase64bytes(encrypted));
	    return new String(decryptedBytes, EncodingUtils.stringEncoding);
	}
	
	public String decryptLines(String encrypted) throws InvalidAlgorithmParameterException, InvalidKeyException,
	    	BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
		
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		String[] sourceLines = encrypted.split("\n");
		byte[] decrypted;
		StringBuilder builder = new StringBuilder(10000);
		for(int i=0; i<sourceLines.length; i++) {
			decrypted = cipher.doFinal(encodingUtils.decodeBase64bytes(sourceLines[i]));
			if (i == sourceLines.length - 1) {
				builder.append(new String(decrypted, EncodingUtils.stringEncoding));
			} else {
				builder.append(new String(decrypted, EncodingUtils.stringEncoding)).append("\n");
			}
		}
		return builder.toString();
	}
	
	
	/**
	 * Questo metodo si occupa di decodificare tramite AES un array di byte passato come parametro. 
	 * A differenza del metodo precedente ritorna anche un valore in formato byte[]
	 * 
	 * @param sourceByte[]
	 * @return encryptedByte[]
	 * @throws BadLocationException 
	 * */
	public byte[] decrypt(byte[] encrypted) throws InvalidAlgorithmParameterException, InvalidKeyException,
			BadPaddingException, IllegalBlockSizeException, BadLocationException {
		
		// retrieve IV and cripted message: 
		byte[] iv = Arrays.copyOfRange(encrypted, 0, 16);
	    byte[] ciphertext = Arrays.copyOfRange(encrypted, iv.length, encrypted.length);			
	    ivParameterSpec = new IvParameterSpec(iv);
	    
	    // { HASHED = [IV_BIT(0)] ; SHUFFLE = [IV_BIT(1)] }
	    boolean hashed = BitWorker.getBitAtPosition(iv[0], 0);
//	    System.out.println("hashed: " + hashed);
	    
	    boolean shuffledResult = BitWorker.getBitAtPosition(iv[0], 1);
//	    System.out.println("shuffledResult: " + shuffledResult);
	    
	    if(shuffledResult) {
			encrypted = shuffleBack(iv, ciphertext);
		}
	    
	    // Configure cipher
		if (hashed) {
			// use iv as salt to hash password
			cipher.init(Cipher.DECRYPT_MODE, getSaltedKeySpec(iv), ivParameterSpec);
		} else {
			// use normal password without hashing it
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		}
		
		byte[] decryptedBytes = cipher.doFinal(encrypted);
		return decryptedBytes;
	}
	
	public byte[] shuffle(byte[] iv, byte[] source) {
		int keyReorderLength = password.length + iv.length;
		byte[] arrayReorderingKey = new byte[keyReorderLength];
		System.arraycopy(EntrophyzerUtils.convertCharsToBytes(password, password.length), 0, arrayReorderingKey, 0, password.length);
		System.arraycopy(iv, 0, arrayReorderingKey, password.length, iv.length);
		
		return EntrophyzerUtils.orderArrayByKey(source, EntrophyzerUtils.byteArrayToInt(arrayReorderingKey));
	}

	public byte[] shuffleBack(byte[] iv, byte[] shuffled) {
		int keyReorderLength = password.length + iv.length;
		byte[] arrayReorderingKey = new byte[keyReorderLength];
		System.arraycopy(EntrophyzerUtils.convertCharsToBytes(password, password.length), 0, arrayReorderingKey, 0, password.length);
		System.arraycopy(iv, 0, arrayReorderingKey, password.length, iv.length);
		
		return EntrophyzerUtils.reorderBackArrayByKey(shuffled, EntrophyzerUtils.byteArrayToInt(arrayReorderingKey));
	}
	
	// used with JTextField to check password strenght
	public static void check(JPasswordField aesKey) {
		
		if(aesKey == null) {
			return;
		}
			
        if(aesKey.getPassword().length<16) {     	
        	aesKey.setBackground(Color.orange);					    
        	aesKey.setToolTipText("You could reach 16 chars to have best secured password ;)");
		}
		if(aesKey.getPassword().length>16) {
			aesKey.setBackground(new Color(227, 223, 113)); 	
			aesKey.setToolTipText("More that 16 chars are overkill, only first 16 are actually used :)");
		}
	    if(aesKey.getPassword().length==0) {
        	aesKey.setBackground(new Color(209, 73, 73));
			aesKey.setToolTipText("Password is empty...Fill password text-field!");
		}
		if(aesKey.getPassword().length==16) {
        	aesKey.setBackground(new Color(107, 219, 105));
			aesKey.setToolTipText("WOW! 16 char is perfect! :D");
		}	
	}
	
	private static byte[] generateIV(int length) {
		SecureRandom sRand = new SecureRandom();
		byte[] iv = new byte[length];
		sRand.nextBytes(iv);
		return iv;
	}
	
	private SecretKeySpec getSaltedKeySpec(byte[] salt) {

		SecretKeySpec hashedSecretKeySpec = null;
		byte[] bytePsw = new byte[0];
		try {
			bytePsw = charKeyToByteArrayV2(this.password);
			bytePsw = SafeHasher.hashPswWithGivenSalt(SafeHasher.byteArrayToChar(bytePsw), salt, iter, algo);
			hashedSecretKeySpec = new SecretKeySpec(Arrays.copyOf(bytePsw, limit), "AES");
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			hashedSecretKeySpec = secretKeySpec;
			e.printStackTrace();
		} finally {
			Arrays.fill(bytePsw, Byte.MIN_VALUE);
		}
		
		return hashedSecretKeySpec;
	}
	
	public byte[] charKeyToByteArray() {
		CharBuffer chBuff = CharBuffer.wrap(this.password);
		ByteBuffer byBuff = Charset.forName("UTF-8").encode(chBuff);
		byte[] bytes = Arrays.copyOfRange(byBuff.array(), 0, limit);
		Arrays.fill(byBuff.array(), (byte)0); // clear array data
		
		return bytes;
	}
	
	/**
	 * to improve - used for multi-bytes charset that generates much more than 16 bytes to have best diffusion through source chars
	 */
	public byte[] charKeyToByteArrayV2(char[] key) {
		CharBuffer chBuff = CharBuffer.wrap(key);
		ByteBuffer byBuff = Charset.forName("UTF-8").encode(chBuff);
		byte[] fullBytes = byBuff.array();
		byte[] bytes = new byte[limit];
		int zeros = nOfZeros(fullBytes);
		float multiplicator = (fullBytes.length-zeros)/((float)bytes.length);
		
		for(int i = 0; i < limit; i++) {
			// a char is 2 bytes, so take a byte every two to avoid depending on only first limit/2 chars
			// use % to avoid outOfBounds in case of limit exceed
			int nextIndex = (int)(i*multiplicator) % (fullBytes.length-zeros);
			bytes[i] = fullBytes[nextIndex];
		}
		Arrays.fill(byBuff.array(), (byte)0); // clear array data
		Arrays.fill(fullBytes, (byte)0); // clear array data
		
		return bytes;
	}
	
	public static byte[] xorBytes(byte[] first, byte[] second) {
		byte[] xorResult = new byte[first.length];

		int i = 0;
		for (byte firstiByte : first) {
			xorResult[i] = (i < second.length) ? (byte) (firstiByte ^ second[i++]) : firstiByte;
		}
		
		return xorResult;
	}
	
	public int nOfZeros(byte[] src) {
		int n = 0;
		for(byte by : src) {
			if(by == 0) {
				n++;
			}else {
				n = 0; // only consecutive final zeros counted
			}
		}
		return n;
	}
	
	/**
	 * this function erase all sensitive data stored when initializing this obj
	 * remember that you'll have to reinitialize before to use it again.
	 */
	public void clearSensitiveData() {
		Arrays.fill(this.displayedKey, '0');
		Arrays.fill(this.password, '0');
		byte[] tempEraser = new byte[limit]; 
		Arrays.fill(tempEraser, Byte.MIN_VALUE);
		
		ivParameterSpec = new IvParameterSpec(tempEraser);
	    secretKeySpec = new SecretKeySpec(Arrays.copyOf(tempEraser, 16), "AES");
	}
	
	//////////////////////
	// GETTERS & SETTERS /
	//////////////////////

	public IvParameterSpec getIvParameterSpec() {
		return ivParameterSpec;
	}
	
	public void setIvParameterSpec(IvParameterSpec ivParameterSpec) {
		this.ivParameterSpec = ivParameterSpec;
	}

	public SecretKeySpec getSecretKeySpec() {
		return secretKeySpec;
	}
	
	public void setSecretKeySpec(SecretKeySpec secretKeySpec) {
		this.secretKeySpec = secretKeySpec;
	}

	public Cipher getCipher() {
		return cipher;
	}
	
	public void setCipher(Cipher cipher) {
		this.cipher = cipher;
	}

	public char[] getPassword() {
		return password;
	}

	public char[] getDisplayedKey() {
		return displayedKey;
	}

	public void setDisplayedKey(char[] displayedKey) {
		this.displayedKey = displayedKey;
	}

	/**
	 * Password mst be more than 10 characters long, containing both symbols, uppercase, lowercase, symbols, specials, and digits.
	 * @param password
	 * @return
	 */
	public static String isPasswordSafeNoSpecials(char[] password, int minLengthIncluded) {
		if(password.length <= minLengthIncluded || 
				!ConversionUtils.containsOneOfMatchesChar(password, ConversionUtils.numberCharacters) ||
				!ConversionUtils.containsOneOfMatchesChar(password, ConversionUtils.baseUppercase) ||
				!ConversionUtils.containsOneOfMatchesChar(password, ConversionUtils.baseLowercase)) {
			return "<html><u>Your password is not safe enough</u>, it should be more than " + minLengthIncluded + 
					" charachters, containing uppercase, lowercase, and digits."
					+ "<br>(To have the best secured password, symbols and special characters should be used too!)"
					+ "<br><br><b>Are you sure to use this password?</b></html>";
		} else {
			return "";
		}
	}

	/**
	 * Password mst be more than 10 characters long, containing both symbols, uppercase, lowercase, symbols, specials, and digits.
	 * @param password
	 * @return
	 */
	public static String isPasswordSafeBestSecurity(char[] password, int minLengthIncluded) {
		if(password.length <= minLengthIncluded || 
				!ConversionUtils.containsOneOfMatchesChar(password, ConversionUtils.symbolCharacters) ||
				!ConversionUtils.containsOneOfMatchesChar(password, ConversionUtils.specialCharacters) ||
				!ConversionUtils.containsOneOfMatchesChar(password, ConversionUtils.numberCharacters) ||
				!ConversionUtils.containsOneOfMatchesChar(password, ConversionUtils.baseUppercase) ||
				!ConversionUtils.containsOneOfMatchesChar(password, ConversionUtils.baseLowercase)) {
			return "<html><u>Your password is not safe enough</u>, it should be more than " + minLengthIncluded + 
					" charachters, containing uppercase, lowercase, symbols, special characters, and digits."
					+ "<br>("+ ConversionUtils.specialString + ConversionUtils.symbolString +")"
					+ "<br><br><b>Are you sure to use this password?</b></html>";
		} else {
			return "";
		}
	}
		
}