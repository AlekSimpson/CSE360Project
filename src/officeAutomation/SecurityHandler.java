package officeAutomation;

import java.math.BigInteger;

import java.security.spec.KeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SecurityHandler {
	
	private static SecurityHandler singleInstance = null;
	private static final int KEY_LENGTH = 256;
	private static final int ITERATION_COUNT = 65536;
	
	private SecurityHandler() {
		super();
	}
	
	public static synchronized SecurityHandler getHandler() {
		if (singleInstance == null) {
			singleInstance = new SecurityHandler();
		}
		return singleInstance;
	}
	
	// this function will output the secure hash of an inputted string
	public AppResult<String> getPasswordHash(String password) throws InvalidKeySpecException {
		int iterations = 500; // high enough to make it annoying to brute force the password
		char[] passChars = password.toCharArray();
		PBEKeySpec spec = new PBEKeySpec(passChars, "a".getBytes(), iterations, 512);
		
		// perform the hash
		try {
			SecretKeyFactory seckeyFact = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hashedArray = seckeyFact.generateSecret(spec).getEncoded();
			
			StringBuilder buffer = new StringBuilder();
			buffer.append(convertToHex(hashedArray));

			return new AppResult<String>(buffer.toString(), null);
		} catch (NoSuchAlgorithmException e) {
			ApplicationError err = new ApplicationError("NoSuchAlgorithm", e.getMessage());
			return new AppResult<String>(null, err);
		}
	}
	
	private static String convertToHex(byte[] arr) throws NoSuchAlgorithmException {
		BigInteger bint = new BigInteger(1, arr);
		String hexValue = bint.toString(16);

		return hexValue;
	}

	public static String encrypt(String plainText, String secretKey, String salt) {
		try {
			SecureRandom secureRandom = new SecureRandom();
			byte[] iv = new byte[16];
			secureRandom.nextBytes(iv);
			IvParameterSpec ivspec = new IvParameterSpec(iv);

			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), ITERATION_COUNT, KEY_LENGTH);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivspec);

			byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));
			byte[] encryptedData = new byte[iv.length + cipherText.length];
			System.arraycopy(iv, 0, encryptedData, 0, iv.length);
			System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);

			return Base64.getEncoder().encodeToString(encryptedData);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String decrypt(String encryptedText, String secretKey, String salt) {
		try {
			byte[] encryptedData = Base64.getDecoder().decode(encryptedText);
			byte[] iv = new byte[16];
			System.arraycopy(encryptedData, 0, iv, 0, iv.length);
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), ITERATION_COUNT, KEY_LENGTH);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");
			
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);
			
			byte[] cipherText = new byte[encryptedData.length - 16];
			System.arraycopy(encryptedData, 16, cipherText, 0, cipherText.length);
			
			byte[] decryptedText = cipher.doFinal(cipherText);
			return new String(decryptedText, "UTF-8");
		} catch (Exception e) {
			// Handle the exception properly
			e.printStackTrace();
			return null;
		}
	}
}
