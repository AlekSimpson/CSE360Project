package officeAutomation;

import java.math.BigInteger;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SecurityHandler {
	
	private static SecurityHandler singleInstance = null;
	
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
		// TODO: int paddingAmount = (arr.length * 2) - hexValue.length();

		return hexValue;

		// TODO: this is to remove any ambiguity around the size of the hex value
		//String paddedHex = String.format("%" + paddingAmount + "d", 0) + hexValue;
		//return ((paddingAmount > 0) ? paddedHex : hexValue);
	}
}
