package crypt.aes;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JTextField;

import org.apache.commons.codec.binary.Base64;
	
	public class EncryptDecrypt {
	
		//////////////////////
		//      CAMPI       //
		//////////////////////
		
		//private static final String SECRET_KEY_1 = "ssdkF$HUy2A#D%kd";
		//private static final String SECRET_KEY_2 = "weJiSEvR5yAC5ftB";
		private String password;//valore aggiornato con i pad
		private String displayedKey;//valore originale chiave inserita
		private IvParameterSpec ivParameterSpec;
		private SecretKeySpec secretKeySpec;
		private Cipher cipher;
		
		private SecureRandom sRand;
		
		//////////////////////
		//    COSTRUTTORI   //  
		//////////////////////
		
		public EncryptDecrypt() {
			
		}
		
		public EncryptDecrypt(String password) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException {
			displayedKey=password;
			while(password.length() !=16) {
			    if(password.length()<16) {
			    	password=password.concat("0");	    	
			    }
			    else if(password.length()>16) {
			    	password=password.substring(0, 16);   	  
			    }
			    this.password=password;
			}
			
			if(password.length()==16) {
				this.password=password;
			}
			// CHANGE UTF-8 TO ISO8859.1 AND EXTRACT 16 BYTES ONLY -> NOW KEY LENGTH IS ALWAYS 16 BYTES EVEN WITH CYRILLIC OR OTHER ENCODINGS
//			byte[] bytePsw = password.getBytes("UTF-8");
			byte[] bytePsw = password.getBytes("ISO-8859-1");
			ivParameterSpec = new IvParameterSpec(bytePsw);
		    secretKeySpec = new SecretKeySpec(Arrays.copyOf(bytePsw, 16), "AES");
		    cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
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
		public static void check(JTextField aesKey) {
			String actkey=aesKey.getText();	
	        if(actkey.length()<16) {     	
	        	aesKey.setBackground(Color.orange);					    
	        	aesKey.setToolTipText("You could reach 16 chars to have best secured password ;)");
			}
			if(actkey.length()>16) {
				aesKey.setBackground(Color.yellow); 	
				aesKey.setToolTipText("More that 16 chars are overkill, only first 16 are actually used :)");
			}
		    if(actkey.length()==0) {
	        	aesKey.setBackground(Color.red);
				aesKey.setToolTipText("Password is empty...Fill password text-field!");
			}
			if(actkey.length()==16) {
	        	aesKey.setBackground(Color.green);
				aesKey.setToolTipText("WOW! 16 char is perfect! :D");
			}	
		}
		
		private byte[] generateIV() {
			sRand = new SecureRandom();
			byte[] iv = new byte[16];
			sRand.nextBytes(iv);
			return iv;
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
	
		public String getPassword() {
			return password;
		}
	
		public String getDisplayedKey() {
			return displayedKey;
		}
	
		public void setDisplayedKey(String displayedKey) {
			this.displayedKey = displayedKey;
		}
		
}