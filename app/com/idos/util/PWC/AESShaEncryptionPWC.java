package com.idos.util.PWC;

	
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AESShaEncryptionPWC {
		public static void main(String args[])
		{
			String key = "LZxqpewNsRpRrfOt";

	        String clean = "gstr1_file";
	        
	        String encryptedString;
	        String decryptedString;
			try {
				encryptedString = Encrypt(clean,key);
				System.out.println("encryptedString " + encryptedString);
				decryptedString = Decrypt(encryptedString,key);
				System.out.println("decryptedString " + decryptedString);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		
		public static String Decrypt(String text, String key) throws Exception{
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] keyBytes= new byte[16]; //changing 16 to 32
			//byte[] keyBytes= new byte[32];
			byte[] b= key.getBytes("UTF-8");
			int len= b.length;
			if (len > keyBytes.length) len = keyBytes.length;
			System.arraycopy(b, 0, keyBytes, 0, len);
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
			cipher.init(Cipher.DECRYPT_MODE,keySpec,ivSpec);

			//BASE64Decoder decoder = new BASE64Decoder();
			//byte [] results = cipher.doFinal(Base64.getDecoder().decode(text));
			byte [] results = cipher.doFinal(Base64.decodeBase64(text));
			return new String(results,"UTF-8");
		}

		public static String Encrypt(String text, String key) throws Exception {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] keyBytes= new byte[16]; //changing 16 to 32
			//byte[] keyBytes= new byte[32];
			byte[] b= key.getBytes("UTF-8");
			int len= b.length;
			if (len > keyBytes.length) len = keyBytes.length;
			System.arraycopy(b, 0, keyBytes, 0, len);
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
			cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivSpec);

			byte[] results = cipher.doFinal(text.getBytes("UTF-8"));
			//BASE64Encoder encoder = new BASE64Encoder();
			//return Base64.getEncoder().encodeToString(results);
			return Base64.encodeBase64String(results);
		}				
}



