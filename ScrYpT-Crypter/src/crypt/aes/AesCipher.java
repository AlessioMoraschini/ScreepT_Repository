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
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JPasswordField;

import org.apache.commons.codec.binary.Base64;

public class AesCipher {

	//////////////////////
	//      CAMPI       //
	//////////////////////
	
	//private static final String SECRET_KEY_1 = "ssdkF$HUy2A#D%kd";
	//private static final String SECRET_KEY_2 = "weJiSEvR5yAC5ftB";
	public static final int limit = 16;
	private char[] password;//valore aggiornato con i pad
	private char[] displayedKey;//valore originale chiave inserita
	private IvParameterSpec ivParameterSpec;
	private SecretKeySpec secretKeySpec;
	private Cipher cipher;
	
	private SecureRandom sRand;
	
	//////////////////////
	//    COSTRUTTORI   //  
	//////////////////////
	
	public AesCipher() {
		
	}
	
	public AesCipher(char[] password) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException {
		
		displayedKey=password;
		this.password= new char[limit];
		int i = 0;
		while(i < limit) {
		    	if(i<password.length) {
		    		this.password[i] = password[i];
		    	}else {
		    		this.password[i] = '0';
		    	}
		    	i++;
		}
		byte[] bytePsw = charKeyToByteArrayV2();
//			byte[] bytePsw = HandleString.extractSameLengthByteArray(password);
		ivParameterSpec = new IvParameterSpec(bytePsw);
	    secretKeySpec = new SecretKeySpec(Arrays.copyOf(bytePsw, 16), "AES");
	    cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	    Arrays.fill(bytePsw, (byte)0); // clear sensitive temp data
	}
	
	//////////////////////
	//      METODI      //
	//////////////////////
	
	/**
	 * Questo metodo si occupa di codificare tramite AES una stringa passata come parametro,
	 * (la converte in byte[], converte con "Cipher", dopodich� riconverte in string).
	 * 
	 * @param sourceString
	 * @return encryptedString
	 * 
	 * */
	public String encrypt(String toBeEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException,
	        InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
	    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
	    byte[] encrypted = cipher.doFinal(toBeEncrypt.getBytes(System.getProperty("file.encoding")));
	    return Base64.encodeBase64String(encrypted);
	}
	
	public String encryptLines(String toBeEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException,
	    InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		String[] sourceLines = toBeEncrypt.split("\n");
		byte[] encrypted;
		StringBuilder builder = new StringBuilder(10000);
		for(int i=0; i<sourceLines.length; i++) {
			encrypted = cipher.doFinal(sourceLines[i].getBytes(System.getProperty("file.encoding")));
			builder.append(Base64.encodeBase64String(encrypted)).append("\n");
		}
		return builder.toString();
	}
	
	/**
	 * Questo metodo si occupa di codificare tramite AES un array di byte passato come parametro. 
	 * A differenza del metodo precedente ritorna anche un valore in formato byte[] 
	 * 
	 * @param sourceByte[]
	 * @return encryptedByte[]
	 * */
	public byte[] encrypt(byte[] toBeEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException,
	InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		
		// set random initialization vector
		byte[] iv = generateIV();
		ivParameterSpec = new IvParameterSpec(iv);
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] encrypted = cipher.doFinal(toBeEncrypt);
		
		// insert iv at the start of encrypted msg
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
	    byte[] decryptedBytes = cipher.doFinal(Base64.decodeBase64(encrypted.getBytes()));
	    return new String(decryptedBytes,System.getProperty("file.encoding"));
	}
	
	public String decryptLines(String encrypted) throws InvalidAlgorithmParameterException, InvalidKeyException,
	    	BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
		
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		String[] sourceLines = encrypted.split("\n");
		byte[] decrypted;
		StringBuilder builder = new StringBuilder(10000);
		for(int i=0; i<sourceLines.length; i++) {
			decrypted = cipher.doFinal(Base64.decodeBase64(sourceLines[i].getBytes()));
			builder.append(new String(decrypted,System.getProperty("file.encoding"))).append("\n");
		}
		return builder.toString();
	}
	
	
	/**
	 * Questo metodo si occupa di decodificare tramite AES un array di byte passato come parametro. 
	 * A differenza del metodo precedente ritorna anche un valore in formato byte[]
	 * 
	 * @param sourceByte[]
	 * @return encryptedByte[]
	 * */
	public byte[] decrypt(byte[] encrypted) throws InvalidAlgorithmParameterException, InvalidKeyException,
    BadPaddingException, IllegalBlockSizeException {
		
		// retrieve IV and cripted message
		byte[] iv = Arrays.copyOfRange(encrypted, 0, 16);
	    byte[] ciphertext = Arrays.copyOfRange(encrypted, iv.length, encrypted.length);			
	    ivParameterSpec = new IvParameterSpec(iv);
	    
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] decryptedBytes = cipher.doFinal(ciphertext);
		return decryptedBytes;
	}
	
	// used with JTextField to check password strenght
	public static void check(JPasswordField aesKey) {
			
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
	
	/**
	 * Still to test
	 * @param psw
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	byte[] hashPsw(char[] psw) throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		KeySpec spec = new PBEKeySpec(psw, salt, 65536, 128);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		return factory.generateSecret(spec).getEncoded();
	}
	
	private byte[] generateIV() {
		sRand = new SecureRandom();
		byte[] iv = new byte[16];
		sRand.nextBytes(iv);
		return iv;
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
	public byte[] charKeyToByteArrayV2() {
		CharBuffer chBuff = CharBuffer.wrap(this.password);
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
		Arrays.fill(tempEraser, (byte)0);
		
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
		
}