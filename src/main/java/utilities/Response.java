package utilities;

import java.lang.reflect.Type;
import java.util.HashMap;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class Response {

	Gson gson = new GsonBuilder().registerTypeAdapter(Double.class, new JsonSerializer<Double>() {

		@Override
		public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
			if (src == src.longValue())
				return new JsonPrimitive(src.longValue());
			return new JsonPrimitive(src);
		}
	}).create();

	public String success(String head, String result, String data, String key) {
		Map<String, Object> content = new HashMap<String, Object>();
		content.put("result", gson.fromJson(result, HashMap.class));
		content.put("data", gson.fromJson(data, HashMap.class));

		Map<String, Object> encryptData = new HashMap<String, Object>();
		encryptData.put("content", content);

		final String datas = gson.toJson(encryptData);
		final String encrypted = Crypto.encryptRimitData(datas, key);

		Map<String, Object> statusResponse = new HashMap<String, Object>();
		statusResponse.put("encrypted_data", gson.fromJson(encrypted, HashMap.class));
		statusResponse.put("head", gson.fromJson(head, HashMap.class));

		return gson.toJson(statusResponse);
	}

	public String error(String head, String result, String data) {
		Map<String, Object> content = new HashMap<String, Object>();
		content.put("data", gson.fromJson(data, HashMap.class));
		content.put("result", gson.fromJson(result, HashMap.class));

		Map<String, Object> statusResponse = new HashMap<String, Object>();
		statusResponse.put("content", content);
		statusResponse.put("head", gson.fromJson(head, HashMap.class));

		return gson.toJson(statusResponse);
	}

}
