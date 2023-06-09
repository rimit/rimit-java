package core;


import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import utilities.Request;
import utilities.Response;

import static utilities.Configs.*;

@Path("/transaction")
public class TransactionStatus {

	@POST
	@Path("/status")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> txnStatus(Map<String, Object> request) {
		return txnStatusId(request, null);
	}
	
	@SuppressWarnings({ "unchecked" })
	@POST
	@Path("/status/{tenant_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> txnStatusId(Map<String, Object> request, @PathParam("tenant_id") String tenant_id) {

		System.out.println("------------------");
		System.out.println("REQUEST : txnStatus");
		System.out.println("------------------");

		Gson gson = new Gson();
		Date date = new Date();

		try {

			/*  */
			/* ASSIGN ENCRYPTION_KEY, API_KEY & API_ID OF ENTITY */
			String ENCRYPTION_KEY = "";
			String AUTH_API_ID = "";
			String AUTH_API_KEY = "";
			/*  */

			/*  */
			/* ASSIGNING DATA RECIVED IN THE REQUEST */
			JsonObject REQUEST_DATA = gson.fromJson(gson.toJson(request), JsonElement.class).getAsJsonObject();
			/*  */

			/*  */
			/* ASSIGNING DATA RECIVED IN THE REQUEST */

			String TRANSACTION_TYPE = REQUEST_DATA.get("type").getAsString();
			String TRANSACTION_NATURE = REQUEST_DATA.get("nature").getAsString();
			String TRANSACTION_NUMBER = REQUEST_DATA.get("no").getAsString();
			String TRANSACTION_URN = REQUEST_DATA.get("urn").getAsString();
			String TRANSACTION_AMOUNT = REQUEST_DATA.get("amount").getAsString();
			String TRANSACTION_REF = REQUEST_DATA.get("reference").getAsString();
			/*  */

			// TXN_STATUS REQUEST URL
			String TXN_STATUS_URL = BASE_URL + "/transaction/statusCheck";

			Map<String, Object> TXN_STATUS_HEAD = new HashMap<String, Object>();
			TXN_STATUS_HEAD.put("api", "status");
			TXN_STATUS_HEAD.put("apiVersion", "V1");
			TXN_STATUS_HEAD.put("timeStamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(date));

			Map<String, Object> AUTH_MAP = new HashMap<String, Object>();
			AUTH_MAP.put("API_ID", AUTH_API_ID);
			AUTH_MAP.put("API_KEY", AUTH_API_KEY);
			TXN_STATUS_HEAD.put("auth", AUTH_MAP);

			Map<String, Object> TXN_STATUS_DATA = new HashMap<String, Object>();
			TXN_STATUS_DATA.put("txn_number", TRANSACTION_NUMBER);
			TXN_STATUS_DATA.put("txn_urn", TRANSACTION_URN);
			TXN_STATUS_DATA.put("txn_reference", TRANSACTION_REF);
			TXN_STATUS_DATA.put("txn_amount", TRANSACTION_AMOUNT);
			TXN_STATUS_DATA.put("txn_type", TRANSACTION_TYPE);
			TXN_STATUS_DATA.put("txn_nature", TRANSACTION_NATURE);


			// TXN_STATUS_RESULT MUST BE EMPTY
			Map<String, Object> TXN_STATUS_RESULT = new HashMap<String, Object>();

			JsonObject TXN_STATUS = Request.confirmRequest(gson.toJson(TXN_STATUS_HEAD),
					gson.toJson(TXN_STATUS_RESULT), gson.toJson(TXN_STATUS_DATA), TXN_STATUS_URL, ENCRYPTION_KEY);


			System.out.println("*****************");
			System.out.println("TXN_STATUS - RESPONSE");
			System.out.println(gson.toJson(TXN_STATUS));
			System.out.println("*****************");

			/*  */
			/*  */

			/* MANAGE RECEIVED RESPONSE */
			/*  */

			/*  */
			/*  */
			Response response = new Response();
			JsonObject content = TXN_STATUS.getAsJsonObject("content");
			return new ObjectMapper().readValue(response.
					error(gson.toJson(TXN_STATUS.getAsJsonObject().get("head")), gson.toJson(content.getAsJsonObject("result")), gson.toJson(content.getAsJsonObject("data"))),HashMap.class);

		} catch (Exception e) {
			System.out.println(e.toString());
			return null;
		}
	}
}
