package core;

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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import utilities.Crypto;
import utilities.Response;
import static utilities.Configs.*;
import static utilities.CommonCodes.*;

@Path("/account")
public class AccountStatement {

	@POST
	@Path("/statement")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> accountStatement(Map<String, Object> request)
			throws JsonMappingException, JsonProcessingException {

		return accountStatementId(request, null);
	}

	@SuppressWarnings({ "unchecked", "unused" })
	@POST
	@Path("/statement/{tenant_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> accountStatementId(Map<String, Object> request, @PathParam("tenant_id") String tenant_id)
			throws JsonMappingException, JsonProcessingException {

		System.out.println("------------------");
		System.out.println("REQUEST : accountStatement");
		System.out.println("------------------");

		Gson gson = new Gson();

		Date date = new Date();

		Map<String, Object> head = new HashMap<String, Object>();
		head.put("api", "accountStatement");
		head.put("apiVersion", "V1");
		head.put("timeStamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(date));

		Response response = new Response();

		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> data = new HashMap<String, Object>();

		List<Map<String, Object>> TRANSACTION_DATA = new ArrayList<Map<String, Object>>();

		try {

			// ASSIGNING TENANT_ID IF THE PLATFORM IS MULTY TENANT
			String TENANT_ID = "";
			if (IS_MULTY_TENANT_PLATFORM.equals("YES")) {
				if (MULTY_TENANT_MODE.equals("QUERY")) {
					TENANT_ID = tenant_id;
				} else if (MULTY_TENANT_MODE.equals("PARAMS")) {
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

			String USER_MOBILE = "", USER_CC = "", ACC_NO = "", ACC_BRANCH = "", START_DATE = "", END_DATE = "";
			//

			USER_MOBILE = DECRYPTED_DATA.getAsJsonObject("content").getAsJsonObject("data").get("mobile").getAsString();
			USER_CC = DECRYPTED_DATA.getAsJsonObject("content").getAsJsonObject("data").get("country_code")
					.getAsString();
			ACC_NO = DECRYPTED_DATA.getAsJsonObject("content").getAsJsonObject("data").get("account_number")
					.getAsString();
			ACC_BRANCH = DECRYPTED_DATA.getAsJsonObject("content").getAsJsonObject("data").get("branch_code")
					.getAsString();

			START_DATE = DECRYPTED_DATA.getAsJsonObject("content").getAsJsonObject("data").get("start_date")
					.getAsString();
			END_DATE = DECRYPTED_DATA.getAsJsonObject("content").getAsJsonObject("data").get("end_date").getAsString();

			/*  */
			/*  */
			/* VERIFY THE USER */
			/*
			 * MANAGE SCOPE FOR ERRORS (Refer -
			 * https://doc.rimit.co/account/account-statement#response-code)
			 */
			/*  */
			/*  */

			/*  */
			/*
			 * EG FOR FAILED RESPONSE : FIND USER ACCOUNT, IF NOT FOUND, SEND RESPONSE AS
			 * FAILED
			 */
			boolean FIND_ACCOUNT = true;
			if (!FIND_ACCOUNT) {
				result.put("code", RESULT_CODE_INVALID_ACCOUNT);
				result.put("status", STATUS_FAILED);
				result.put("message", RESULT_MESSAGE_E2021);

				head.put("HTTP_CODE", HTTP_CODE_SUCCESS);
				data = null;

				return new ObjectMapper().readValue(
						response.error(gson.toJson(head), gson.toJson(result), gson.toJson(data)), HashMap.class);
			}

			/*  */

			/*  */
			/* FIND THE ACCOUNT BALANCE AND ASSIGN. KEEP 0 IF NO BALANCE FOUND */
			String ACC_BALANCE = "0";
			/*  */

			List<Map<String, Object>> ACCOUNT_TRANSACTION = new ArrayList<Map<String, Object>>();

			/*  */
			/*
			 * FIND ALL TRANSACTIONS BETWEEN START_DATE & END_DATE IN THE RESPECTIVE ACCOUNT
			 */
			Map<String, Object> accountTransaction1 = new HashMap<String, Object>();
			accountTransaction1.put("txn_id", "");
			accountTransaction1.put("date", "");
			accountTransaction1.put("time", "");
			accountTransaction1.put("debit_amount", "");
			accountTransaction1.put("credit_amount", "");
			accountTransaction1.put("balance", "");
			accountTransaction1.put("description", "");

			ACCOUNT_TRANSACTION.add(accountTransaction1);

			Map<String, Object> accountTransaction2 = new HashMap<String, Object>();
			accountTransaction2.put("txn_id", "");
			accountTransaction2.put("date", "");
			accountTransaction2.put("time", "");
			accountTransaction2.put("debit_amount", "");
			accountTransaction2.put("credit_amount", "");
			accountTransaction2.put("balance", "");
			accountTransaction2.put("description", "");

			ACCOUNT_TRANSACTION.add(accountTransaction2);

			/*  */

			/*  */
			/* ASSIGN DATA RECEIVED FROM ACCOUNT_TRANSACTION ARRAY */
			if (ACCOUNT_TRANSACTION.size() > 0) {
				for (Map<String, Object> transaction : ACCOUNT_TRANSACTION) {

					Map<String, Object> txnData = new HashMap<String, Object>();

					txnData.put("txn_id", transaction.get("txn_id"));
					txnData.put("date", transaction.get("date"));
					txnData.put("time", transaction.get("time"));
					txnData.put("debit_amount", transaction.get("debit_amount"));
					txnData.put("credit_amount", transaction.get("credit_amount"));
					txnData.put("balance", transaction.get("balance"));
					txnData.put("description", transaction.get("description"));

					TRANSACTION_DATA.add(txnData);
				}
			}
			/*  */
			String TRANSACTION_COUNT = "" + ACCOUNT_TRANSACTION.size();

			Map<String, Object> USER_ACCOUNT = new HashMap<String, Object>();
			USER_ACCOUNT.put("account_number", ACC_NO);
			USER_ACCOUNT.put("account_branch", ACC_BRANCH);
			USER_ACCOUNT.put("account_balance", ACC_BALANCE);
			USER_ACCOUNT.put("start_date", START_DATE);
			USER_ACCOUNT.put("end_date", END_DATE);
			USER_ACCOUNT.put("transactions_count", TRANSACTION_COUNT);

			result.put("code", RESULT_CODE_SUCCESS);
			result.put("status", STATUS_SUCCESS);
			result.put("message", RESULT_MESSAGE_E1001);
			head.put("HTTP_CODE", HTTP_CODE_SUCCESS);

			data.put("account", USER_ACCOUNT);
			data.put("transactions", TRANSACTION_DATA);

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
}
