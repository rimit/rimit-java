package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class Request {

	public static JsonObject confirmRequest(String head, String result, String data, String uri, String key) throws IOException {

		Gson gson = new GsonBuilder().registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
			@Override
			public JsonElement serialize(Double src, java.lang.reflect.Type typeOfSrc,
					JsonSerializationContext context) {
				if (src == src.longValue())
					return new JsonPrimitive(src.longValue());
				return new JsonPrimitive(src);
			}
		}).create();

		Map<String, Object> content = new HashMap<String, Object>();
		content.put("result", gson.fromJson(result, HashMap.class));
		content.put("data", gson.fromJson(data, HashMap.class));

		Map<String, Object> encryptData = new HashMap<String, Object>();
		encryptData.put("content", content);
		
		final String stringData = gson.toJson(encryptData);

		System.out.println("---------------------");
		System.out.println("DATA TO BE ENCRYPTED");
		System.out.println(encryptData);
		System.out.println("---------------------");

		final String encrypted = Crypto.encryptRimitData(stringData, key);

		Map<String, Object> encryptDetail = new HashMap<>();
		encryptDetail.put("head", gson.fromJson(head, HashMap.class));
		encryptDetail.put("encrypted_data", gson.fromJson(encrypted, HashMap.class));

		String requestBody = gson.toJson(encryptDetail);

		URL url = new URL(uri);
		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		http.setRequestMethod("POST");
		http.setDoOutput(true);
		http.setRequestProperty("Content-Type", "application/json");

		byte[] out = requestBody.getBytes(StandardCharsets.UTF_8);
		OutputStream stream = http.getOutputStream();
		stream.write(out);

		BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line).append("\n");
		}
		br.close();

		http.disconnect();

		JsonElement responseElement = gson.fromJson(sb.toString(), JsonElement.class);

		JsonObject response = responseElement.getAsJsonObject();
		
		int httpCode=response.getAsJsonObject("head").get("HTTP_CODE").getAsInt();
		if (httpCode ==(CommonCodes.HTTP_CODE_BAD_REQUEST) || httpCode==(CommonCodes.HTTP_CODE_UNAUTHORIZED) || httpCode==(CommonCodes.HTTP_CODE_SERVICE_UNAVAILABLE)) {
			System.out.println("---------------------");
			System.out.println("DECRYPTED FAILED");
			System.out.println(response);
			System.out.println("---------------------");
			return response;
		}

		System.out.println("---------------------");
		System.out.println("DATA TO BE DECRYPTED");
		System.out.println(response);
		System.out.println("---------------------");
		
		JsonObject decrypted = Crypto.decryptRimitData(response.getAsJsonObject("encrypted_data"), key);
		
		JsonObject responseData = new JsonObject();
		responseData.add("head", response.getAsJsonObject("head"));
		responseData.add("content", decrypted.getAsJsonObject("content"));

		return responseData;
	}
}
