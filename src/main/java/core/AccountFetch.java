package core;



import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import utilities.CommonCodes;
import utilities.Configs;
import utilities.Crypto;
import utilities.Request;
import utilities.Response;
import static utilities.CommonCodes.*;

@Path("/account")
public class AccountFetch {

	@POST
	@Path("/fetch")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> accountFetch(Map<String, Object> request)
			throws JsonMappingException, JsonProcessingException {

		return accountFetchId(request, null);
	}

	@SuppressWarnings({"unchecked", "unused"})
	@POST
	@Path("/fetch/{tenant_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> accountFetchId(Map<String, Object> request, @PathParam("tenant_id") String tenant_id)
			throws JsonMappingException, JsonProcessingException {

		System.out.println("------------------");
		System.out.println("REQUEST : accountFetch");
		System.out.println("------------------");

		Gson gson = new GsonBuilder().registerTypeAdapter(Double.class, new JsonSerializer<Double>() {

			@Override
			public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
				if (src == src.longValue())
					return new JsonPrimitive(src.longValue());
				return new JsonPrimitive(src);
			}
		}).create();

		Date date = new Date();

		Map<String, Object> head = new HashMap<String, Object>();
		head.put("api", "accountFetch");
		head.put("apiVersion", "V1");
		head.put("timeStamp", new SimpleDateFormat("YYYY-MM-dd hh:mm:ss a").format(date));

		Response response = new Response();

		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> data = new HashMap<String, Object>();


		try {
			String TENANT_ID = "";
			if (Configs.IS_MULTY_TENANT_PLATFORM.equals("YES")) {
				if (Configs.MULTY_TENANT_MODE.equals("QUERY")) {
					if (tenant_id != null)
						TENANT_ID = tenant_id;
				} else if (Configs.MULTY_TENANT_MODE.equals("PARAMS")) {
					if (tenant_id != null)
						TENANT_ID = tenant_id;
				}
			}

			/*  */
			/* ASSIGN ENCRYPTION_KEY OF ENTITY */
			String ENCRYPTION_KEY = "";
			/*  */

			// ASSIGNING DATA RECIVED IN THE REQUEST
			JsonObject REQUEST_DATA = gson.fromJson(gson.toJson(request), JsonElement.class).getAsJsonObject()
					.getAsJsonObject("encrypted_data");

			// DECRYPTING DATA RECEIVED
			JsonObject DECRYPTED_DATA = Crypto.decryptRimitData(REQUEST_DATA, ENCRYPTION_KEY);

			// ERROR RESPONSE IF DECRYPTION FAILED
			if (DECRYPTED_DATA == null) {
				result.put("code", RESULT_CODE_DECRYPTION_FAILED);
				result.put("status", STATUS_ERROR);
				result.put("message", RESULT_MESSAGE_E2008);

				head.put("HTTP_CODE", HTTP_CODE_BAD_REQUEST);
				data = null;
				return new ObjectMapper().readValue(
						response.error(gson.toJson(head), gson.toJson(result), gson.toJson(data)), HashMap.class);
			}

			String USER_MOBILE = DECRYPTED_DATA.getAsJsonObject("content").getAsJsonObject("data").get("mobile")
					.getAsString();
			String USER_CC = DECRYPTED_DATA.getAsJsonObject("content").getAsJsonObject("data").get("country_code")
					.getAsString();
			String DOB = DECRYPTED_DATA.getAsJsonObject("content").getAsJsonObject("data").get("dob").getAsString();

			/*  */
			/*  */
			/* VERIFY THE USER */
			/*
			 * MANAGE SCOPE FOR ERRORS (Refer -
			 * https://doc.rimit.co/account/account-fetch#response-code)
			 */
			/*  */
			/*  */

			/*  */
			/* EG FOR FAILED RESPONSE :FIND USER, IF NOT FOUND, SEND RESPONSE AS FAILED */
			boolean FIND_USER = true;
			if (!FIND_USER) {
				result.put("code", RESULT_CODE_MOBILE_NUMBER_NOT_FOUND);
				result.put("status", STATUS_FAILED);
				result.put("message", RESULT_MESSAGE_E2014);

				head.put("HTTP_CODE", HTTP_CODE_SUCCESS);
				data = null;

				return new ObjectMapper().readValue(
						response.success(gson.toJson(head), gson.toJson(result), gson.toJson(data), ENCRYPTION_KEY),
						HashMap.class);
			}
			/*  */


			Map<String, Object> USER_DATA = new HashMap<String, Object>();
			USER_DATA.put("mobile", USER_MOBILE);
			USER_DATA.put("country_code", USER_CC);

			// IF SUCCESSFUL, CALL addAccount
			addAccount(USER_DATA);

			result.put("code", RESULT_CODE_SUCCESS);
			result.put("status", STATUS_SUCCESS);
			result.put("message", RESULT_MESSAGE_E1001);

			head.put("HTTP_CODE", HTTP_CODE_SUCCESS);
			data = null;
			return new ObjectMapper().readValue(
					response.success(gson.toJson(head), gson.toJson(result), gson.toJson(data), ENCRYPTION_KEY),
					HashMap.class);

		} catch (Exception e) {
			result.put("code", RESULT_CODE_SERVICE_NOT_AVAILABLE);
			result.put("status", STATUS_ERROR);
			result.put("message", RESULT_MESSAGE_E2003);

			head.put("HTTP_CODE", HTTP_CODE_SERVICE_UNAVAILABLE);
			data = null;
			return new ObjectMapper().readValue(
					response.error(gson.toJson(result), gson.toJson(head), gson.toJson(data)), HashMap.class);
		}
	}

	public boolean addAccount(Map<String, Object> userData) {
		System.out.println("------------------");
		System.out.println("REQUEST : AddAccount");
		System.out.println("------------------");
		Gson gson = new Gson();

		try {
			/*  */
			/* ASSIGN ENCRYPTION_KEY, API_KEY & API_ID OF ENTITY */
			String ENCRYPTION_KEY = "";
			String AUTH_API_ID = "";
			String AUTH_API_KEY = "";
			/*  */

			// ADD_ACCOUNT REQUEST URL
			String ADD_ACCOUNT_URL = Configs.BASE_URL + "/account/add";

			Map<String, Object> ADD_ACCOUNT_HEAD = new HashMap<>();
			ADD_ACCOUNT_HEAD.put("api", "accountAdd");
			ADD_ACCOUNT_HEAD.put("apiVersion", "V1");
			ADD_ACCOUNT_HEAD.put("timeStamp", new SimpleDateFormat("YYYY-MM-dd hh:mm:ss a").format(new Date()));
			Map<String, Object> auth = new HashMap<>();
			auth.put("API_ID", AUTH_API_ID);
			auth.put("API_KEY", AUTH_API_KEY);
			ADD_ACCOUNT_HEAD.put("auth", auth);

			/*  */
			/* ASSIGN USER DATA BASED ON REQUEST DATA ON accountFetch */
			Map<String, Object> USER_DATA = new HashMap<>();
			USER_DATA.put("mobile", userData.get("mobile"));
			USER_DATA.put("country_code", userData.get("country_code"));
			/*  */

			/*  */
			/* READ ALL ACCOUNTS OF THE USER IN ACCOUNTS DATA */
			Map<String, Object> ACCOUNTS_DATA = new HashMap<>();
			ACCOUNTS_DATA.put("account_name", "");
			ACCOUNTS_DATA.put("account_number", "");
			ACCOUNTS_DATA.put("branch_code", "");
			ACCOUNTS_DATA.put("branch_name", "");
			ACCOUNTS_DATA.put("account_type", "");
			ACCOUNTS_DATA.put("account_class", "");
			ACCOUNTS_DATA.put("txn_amount_limit", "");
			ACCOUNTS_DATA.put("account_status", "");
			ACCOUNTS_DATA.put("account_opening_date", "");

			ACCOUNTS_DATA.put("is_debit_allowed", true);
			ACCOUNTS_DATA.put("is_credit_allowed", true);
			ACCOUNTS_DATA.put("is_cash_debit_allowed", true);
			ACCOUNTS_DATA.put("is_cash_credit_allowed", true);
			ACCOUNTS_DATA.put("auth_salt", "");
			/*  */

			/*  */
			/* ASSIGN DATA RECEIVED FROM ACCOUNTS_DATA MAP */
			Map<String, Object> ACCOUNT = new HashMap<>();
			ACCOUNT.put("account_name", ACCOUNTS_DATA.get("account_name"));
			ACCOUNT.put("account_number", ACCOUNTS_DATA.get("account_number"));
			ACCOUNT.put("branch_code", ACCOUNTS_DATA.get("branch_code"));
			ACCOUNT.put("branch_name", ACCOUNTS_DATA.get("branch_name"));
			ACCOUNT.put("account_type", ACCOUNTS_DATA.get("account_type"));
			ACCOUNT.put("account_class", ACCOUNTS_DATA.get("account_class"));
			ACCOUNT.put("account_status", ACCOUNTS_DATA.get("account_status"));
			ACCOUNT.put("account_opening_date", ACCOUNTS_DATA.get("account_opening_date"));
			ACCOUNT.put("account_currency", ACCOUNTS_DATA.get("account_currency"));
			ACCOUNT.put("account_daily_limit", ACCOUNTS_DATA.get("account_daily_limit"));
			ACCOUNT.put("is_debit_allowed", ACCOUNTS_DATA.get("is_debit_allowed"));
			ACCOUNT.put("is_credit_allowed", ACCOUNTS_DATA.get("is_credit_allowed"));
			ACCOUNT.put("is_cash_debit_allowed", ACCOUNTS_DATA.get("is_cash_debit_allowed"));
			ACCOUNT.put("is_cash_credit_allowed", ACCOUNTS_DATA.get("is_cash_credit_allowed"));
			ACCOUNT.put("auth_salt", ACCOUNTS_DATA.get("auth_salt"));

			List<Map<String, Object>> USER_ACCOUNTS = new ArrayList<>();
			USER_ACCOUNTS.add(ACCOUNT);
			/*  */

			Map<String, Object> ADD_ACCOUNTS_DATA = new HashMap<>();
			ADD_ACCOUNTS_DATA.put("user", USER_DATA);
			ADD_ACCOUNTS_DATA.put("accounts", USER_ACCOUNTS);

			// IF THE ALL ACCOUNTS READ SUCCESSFULLY
			Map<String, Object> ADD_ACCOUNT_RESULT = new HashMap<>();
			ADD_ACCOUNT_RESULT.put("code", CommonCodes.RESULT_CODE_SUCCESS);
			ADD_ACCOUNT_RESULT.put("status", CommonCodes.STATUS_SUCCESS);
			ADD_ACCOUNT_RESULT.put("message", CommonCodes.RESULT_MESSAGE_E1001);

			JsonObject ADD_ACCOUNT_CONFIRM = Request.confirmRequest(gson.toJson(ADD_ACCOUNT_HEAD),
					gson.toJson(ADD_ACCOUNT_RESULT), gson.toJson(ADD_ACCOUNTS_DATA), ADD_ACCOUNT_URL, ENCRYPTION_KEY);

			
			System.out.println("*****************");
			System.out.println("ADD_ACCOUNT_CONFIRM - RESPONSE");
			System.out.println(ADD_ACCOUNT_CONFIRM);
			System.out.println("*****************");

			/*  */
			/*  */

			/* MANAGE RECEIVED RESPONSE */
			/*  */

			/*  */
			/*  */
			return true;
			// res.status(200).send(ADD_ACCOUNT_CONFIRM);
		} catch (Exception error) {
			error.printStackTrace();
			System.out.println(error);
		}
		return false;
		
	}

}
