package utilities;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Hashing {

	 public static String hashData(String data, String salt) {
	        try {
	            int iterations = 2048;
	            int keyLength = 32 * 8; // 32 bytes * 8 bits

	            char[] dataChars = data.toCharArray();
	            byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);

	            PBEKeySpec spec = new PBEKeySpec(dataChars, saltBytes, iterations, keyLength);

	            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
	            byte[] hashedBytes = keyFactory.generateSecret(spec).getEncoded();

	            StringBuilder stringBuilder = new StringBuilder();
	            for (byte b : hashedBytes) {
	                stringBuilder.append(String.format("%02x", b));
	            }

	            return stringBuilder.toString();
	        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
	            e.printStackTrace();
	            return null;
	        }
	    }

	
	public static boolean hashVerify(String data, String hash, String salt) {
		String hashedData = hashData(data, salt);
		if(hashedData!=null && hashedData.equals(hash))
			return true;
		else
			return false;
	}
}
