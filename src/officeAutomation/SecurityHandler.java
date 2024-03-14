package officeAutomation;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
	
	public AppResult<String> getPasswordHash(String password) throws InvalidKeySpecException {
		int iterations = 500; // high enough to make it annoying to brute force the password
		char[] passChars = password.toCharArray();
		AppResult<byte[]> saltResult = getSalt();
		byte[] salt;

		// check that result is valid
		if (saltResult.isErr()) {
			return new AppResult<String>(null, saltResult.orElse());
		}
		
		salt = saltResult.andThen();
		
		PBEKeySpec spec = new PBEKeySpec(passChars, salt, iterations, 512);
		
		// perform the hash
		try {
			SecretKeyFactory seckeyFact = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hashedArray = seckeyFact.generateSecret(spec).getEncoded();
			
			StringBuilder buffer = new StringBuilder();
			buffer.append(convertToHex(salt));
			buffer.append(convertToHex(hashedArray));

			return new AppResult<String>(buffer.toString(), null);
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			ApplicationError err = new ApplicationError("NoSuchAlgorithm", e.getMessage());
			return new AppResult<String>(null, err);
		}
	}
	
	private static String convertToHex(byte[] arr) throws NoSuchAlgorithmException {
		BigInteger bint = new BigInteger(1, arr);
		String hexValue = bint.toString(16);
		int paddingAmount = (arr.length * 2) - hexValue.length();

		// this is to remove any ambiguity around the size of the hex value
		String paddedHex = String.format("%0" + paddingAmount + "d", 0) + hexValue;
		return ((paddingAmount > 0) ? paddedHex : hexValue);
	}
	
	// for extra security we will provide the hash with some *salt*
	private static AppResult<byte[]> getSalt() {
		try {
			SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");		
			byte[] byteArray = new byte[16]; // this array will hold "the salt"
			rand.nextBytes(byteArray);
			return new AppResult<byte[]>(byteArray, null);
		}catch (Exception e) {
			System.out.println(e.getMessage());
			ApplicationError err = new ApplicationError("Salt Not Found", e.getMessage());
			return new AppResult<byte[]>(null, err);
		}
	}
}
