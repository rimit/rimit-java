package utilities;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class Crypto {
	static Gson gson = new GsonBuilder().registerTypeAdapter(Double.class, new JsonSerializer<Double>() {

		@Override
		public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
			if (src == src.longValue())
				return new JsonPrimitive(src.longValue());
			return new JsonPrimitive(src);
		}
	}).create();

	public static String encryptRimitData(String data, final String key) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

			String iv = generateRandomHexToken(8);

			System.out.println("---------------------");
			System.out.println("*** ENCRYPT - KEY *** " + key);
			System.out.println("*** ENCRYPT - IV *** " + iv);
			System.out.println("*** ENCRYPT - DATA *** " + data);
			System.out.println("---------------------");

			IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());

			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);

			String encrypted = Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes("UTF-8")));

			String salt = iv + iv;
			String hash = Hashing.hashData(data, salt);

			JsonObject encryptedData = new JsonObject();
			encryptedData.addProperty("cipher_text", encrypted);
			encryptedData.addProperty("iv", iv);
			encryptedData.addProperty("hash", hash);

			System.out.println("*** ENCRYPTED DATA ***");
			System.out.println(gson.toJson(encryptedData));
			System.out.println("---------------------");

			return gson.toJson(encryptedData);

		} catch (Exception e) {
			System.out.println(e.toString());
			return null;
		}
	}

	@SuppressWarnings("unused")
	public static JsonObject decryptRimitData(final JsonObject data, final String key) {
		try {

			IvParameterSpec iv = new IvParameterSpec(data.get("iv").getAsString().getBytes("UTF-8"));

			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			System.out.println("---------------------");
			System.out.println("*** DECRYPT - KEY *** " + key);
			System.out.println("*** DECRYPT - IV *** " + data.get("iv").getAsString());
			System.out.println("*** DECRYPT - DATA *** " + gson.toJson(data));
			System.out.println("---------------------");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			String encrypted = data.get("cipher_text").getAsString();

			JsonObject decryptedObject = null;

			byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));
			String decryptedString = new String(decryptedBytes);

			JsonElement decryptedElement = gson.fromJson(decryptedString, JsonElement.class);
			decryptedObject = decryptedElement.getAsJsonObject();

			System.out.println("*** DECRYPTED DATA ***");
			System.out.println(gson.toJson(decryptedObject));
			System.out.println("---------------------");

			String salt = data.get("iv").getAsString() + data.get("iv").getAsString();
			String hash = data.get("hash").getAsString();
			boolean validHash = Hashing.hashVerify(decryptedString, hash, salt);
			if (!validHash) {
				System.out.println("Invalid Hash");
				return null;
			}

			return decryptedObject;

		} catch (Exception e) {
			System.out.println("Error " + e.toString());
			return null;
		}

	}

	public static String generateRandomHexToken(int byteLength) {
		SecureRandom secureRandom = new SecureRandom();
		byte[] token = new byte[byteLength];
		String iv = "";
		do {
			secureRandom.nextBytes(token);
			iv = new BigInteger(1, token).toString(16);
		} while (iv.length() != 16);
		return iv; // Hexadecimal encoding
	}

}
