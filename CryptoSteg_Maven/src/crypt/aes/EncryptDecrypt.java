package crypt.aes;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
			
			ivParameterSpec = new IvParameterSpec(password.getBytes("UTF-8"));
		    secretKeySpec = new SecretKeySpec(password.getBytes("UTF-8"), "AES");
		    cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		}
		
		//////////////////////
		//      METODI      //
		//////////////////////
		
		/**
		 * Questo metodo si occupa di codificare tramite AES una stringa passata come parametro,
		 * (la converte in byte[], converte con "Cipher", dopodichè riconverte in string).
		 * 
		 * @param sourceString
		 * @return encryptedString
		 * */
		public String encrypt(String toBeEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException,
		        InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		    byte[] encrypted = cipher.doFinal(toBeEncrypt.getBytes());
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
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] encrypted = cipher.doFinal(toBeEncrypt);
		return encrypted;
		}
		
		/**
		 * Questo metodo si occupa di decodificare tramite AES una stringa passata come parametro,
		 * (la converte in byte[], converte con "Cipher", dopodichè riconverte in string).
		 * 
		 * @param sourceString
		 * @return encryptedString
		 * */
		public String decrypt(String encrypted) throws InvalidAlgorithmParameterException, InvalidKeyException,
		        BadPaddingException, IllegalBlockSizeException {
		    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		    byte[] decryptedBytes = cipher.doFinal(Base64.decodeBase64(encrypted));
		    return new String(decryptedBytes);
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
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
			byte[] decryptedBytes = cipher.doFinal(encrypted);
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
	
		public void setPassword(String password) {
			this.password = password;
		}
	
		public String getDisplayedKey() {
			return displayedKey;
		}
	
		public void setDisplayedKey(String displayedKey) {
			this.displayedKey = displayedKey;
		}
		
}