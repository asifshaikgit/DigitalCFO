package controllers.externalInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import play.mvc.Http;
import play.mvc.Http.Request;
import javax.transaction.Transactional;
import controllers.StaticController;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import play.db.jpa.JPAApi;
import play.Application;

public class KaizalaAuthorization extends StaticController {
	public static JPAApi jpaApi;
	public static EntityManager entityManager;
	public static Application application;
	private Request request;

	@Inject
	public KaizalaAuthorization(JPAApi jpaApi, Application application) {
		super(application);
		this.jpaApi = jpaApi;
		entityManager = EntityManagerProvider.getEntityManager();
		this.application = application;
	}

	@Transactional
	public void parsePurchaseOrderData() throws Exception {
	}

	public static void main(String[] args) {
		KaizalaAuthorization ka = new KaizalaAuthorization(jpaApi, application);
		String str = ka.getAccessToken();
	}

	public String getAccessToken() {
		String accessToken = "";
		try {
			URL url = new URL("https://api.kaiza.la/v1/accessToken");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("applicationId",
					"BFD261DD3FB6587957F1D4AA8F62F6E09FCE1145923253A92362ECE15D3BD008");
			conn.setRequestProperty("applicationSecret", "Q8UI3MCJ8Q");
			conn.setRequestProperty(
					"refreshToken",
					"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cm46bWljcm9zb2Z0OmNyZWRlbnRpYWxzIjoie1wicGhvbmVOdW1iZXJcIjpcIis5MTk5NzAxNzc2ODhcIixcImNJZFwiOlwiXCIsXCJ0ZXN0U2VuZGVyXCI6XCJmYWxzZVwiLFwiYXBwTmFtZVwiOlwiY29tLm1pY3Jvc29mdC5tb2JpbGUua2FpemFsYWFwaVwiLFwiYXBwbGljYXRpb25JZFwiOlwiQkZEMjYxREQzRkI2NTg3OTU3RjFENEFBOEY2MkY2RTA5RkNFMTE0NTkyMzI1M0E5MjM2MkVDRTE1RDNCRDAwOFwiLFwicGVybWlzc2lvbnNcIjpcIjguNFwiLFwiYXBwbGljYXRpb25UeXBlXCI6LTEsXCJ0b2tlblZhbGlkRnJvbVwiOjE1MDA1NjA5ODQwNTMsXCJkYXRhXCI6XCJ7XFxcIkdyb3VwSWRcXFwiOlxcXCJmYzhhMDM1NC00M2RkLTRlYWUtYTJiNy1lNTllZDkyYzQ5MTNcXFwiLFxcXCJBcHBOYW1lXFxcIjpcXFwiSURPU1BPXFxcIn1cIn0iLCJ1aWQiOiJNb2JpbGVBcHBzU2VydmljZTozYzNhOTAwYy02N2ZmLTQ5ZjgtYThiZC0yMTdmNzI5ZjJhOGIiLCJ2ZXIiOiIyIiwibmJmIjoxNTAwNTYwOTg0LCJleHAiOjE1MzIwOTY5ODQsImlhdCI6MTUwMDU2MDk4NCwiaXNzIjoidXJuOm1pY3Jvc29mdDp3aW5kb3dzLWF6dXJlOnp1bW8iLCJhdWQiOiJ1cm46bWljcm9zb2Z0OndpbmRvd3MtYXp1cmU6enVtbyJ9.Kf_o4pbo-_lWGdB8HMz9-CF-fb1nfAv9WegM4-U69CQ");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode() + conn.getResponseMessage());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			StringBuilder sb = new StringBuilder();
			while ((output = br.readLine()) != null) {
				sb.append(output + "\n");

			}
			JSONObject jObject = new JSONObject(sb.toString()); // json
			accessToken = jObject.getString("accessToken"); // get the name from
															// data.
			conn.disconnect();
			// "accessToken":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cm46bWljcm9zb2Z0OmNyZWRlbnRpYWxzIjoie1wicGhvbmVOdW1iZXJcIjpcIis5MTk5NzAxNzc2ODhcIixcImNJZFwiOlwiXCIsXCJ0ZXN0U2VuZGVyXCI6XCJmYWxzZVwiLFwiYXBwTmFtZVwiOlwiY29tLm1pY3Jvc29mdC5tb2JpbGUua2FpemFsYWFwaVwiLFwiYXBwbGljYXRpb25JZFwiOlwiQkZEMjYxREQzRkI2NTg3OTU3RjFENEFBOEY2MkY2RTA5RkNFMTE0NTkyMzI1M0E5MjM2MkVDRTE1RDNCRDAwOFwiLFwicGVybWlzc2lvbnNcIjpcIjIuMzA6My4xNDo0LjI6Ni4yMjo1LjQ6OS4yOjE1LjMwXCIsXCJhcHBsaWNhdGlvblR5cGVcIjotMSxcImRhdGFcIjpcIntcXFwiR3JvdXBJZFxcXCI6XFxcImZjOGEwMzU0LTQzZGQtNGVhZS1hMmI3LWU1OWVkOTJjNDkxM1xcXCIsXFxcIkFwcE5hbWVcXFwiOlxcXCJJRE9TUE9cXFwifVwifSIsInVpZCI6Ik1vYmlsZUFwcHNTZXJ2aWNlOjNjM2E5MDBjLTY3ZmYtNDlmOC1hOGJkLTIxN2Y3MjlmMmE4YiIsInZlciI6IjIiLCJuYmYiOjE1MDA0NDE4MDYsImV4cCI6MTUwMDUyODIwNiwiaWF0IjoxNTAwNDQxODA2LCJpc3MiOiJ1cm46bWljcm9zb2Z0OndpbmRvd3MtYXp1cmU6enVtbyIsImF1ZCI6InVybjptaWNyb3NvZnQ6d2luZG93cy1henVyZTp6dW1vIn0.pqkmqD1HgNZLNB9WdiQDnStFapBwYs3UIjhHhDKx8mE","endpointUrl":"https://inc-000.KaizalaMessaging.osi.office.net","accessTokenExpiry":1500528206420,"scope":"groupinfo.add,groupinfo.get,groupinfo.remove,groupinfo.update,groupmember.add,groupmember.get,groupmember.remove,groupmessage.add,action.add,action.get,action.update,profile.get,media.add,subscription.add,subscription.get,subscription.remove,subscription.update"}*/
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return accessToken;
	}

	public String getGroupId(String accessToken, String groupName,
			String assignToPhNo, String poGeneratorPhNo) {
		// accessToken="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cm46bWljcm9zb2Z0OmNyZWRlbnRpYWxzIjoie1wicGhvbmVOdW1iZXJcIjpcIis5MTk5NzAxNzc2ODhcIixcImNJZFwiOlwiXCIsXCJ0ZXN0U2VuZGVyXCI6XCJmYWxzZVwiLFwiYXBwTmFtZVwiOlwiY29tLm1pY3Jvc29mdC5tb2JpbGUua2FpemFsYWFwaVwiLFwiYXBwbGljYXRpb25JZFwiOlwiQkZEMjYxREQzRkI2NTg3OTU3RjFENEFBOEY2MkY2RTA5RkNFMTE0NTkyMzI1M0E5MjM2MkVDRTE1RDNCRDAwOFwiLFwicGVybWlzc2lvbnNcIjpcIjIuMzA6My4xNDo0LjI6Ni4yMjo1LjQ6OS4yOjE1LjMwXCIsXCJhcHBsaWNhdGlvblR5cGVcIjotMSxcImRhdGFcIjpcIntcXFwiR3JvdXBJZFxcXCI6XFxcImZjOGEwMzU0LTQzZGQtNGVhZS1hMmI3LWU1OWVkOTJjNDkxM1xcXCIsXFxcIkFwcE5hbWVcXFwiOlxcXCJJRE9TUE9cXFwifVwifSIsInVpZCI6Ik1vYmlsZUFwcHNTZXJ2aWNlOjNjM2E5MDBjLTY3ZmYtNDlmOC1hOGJkLTIxN2Y3MjlmMmE4YiIsInZlciI6IjIiLCJuYmYiOjE1MDA0NDE4MDYsImV4cCI6MTUwMDUyODIwNiwiaWF0IjoxNTAwNDQxODA2LCJpc3MiOiJ1cm46bWljcm9zb2Z0OndpbmRvd3MtYXp1cmU6enVtbyIsImF1ZCI6InVybjptaWNyb3NvZnQ6d2luZG93cy1henVyZTp6dW1vIn0.pqkmqD1HgNZLNB9WdiQDnStFapBwYs3UIjhHhDKx8mE";
		String groupId = "";
		try {
			// String json = "{\"mobileNumber\":\"+919970177688\"}";
			// String strBodyJSON =
			// "{name:\"ManaliGroup2\", welcomeMessage:\"Welcome to group\", members:
			// [\"+919970177688\", \"+919930302208\"]}";
			// String strBodyJSON =
			// "{name:\"IDOS_Kaizala_Groups\", welcomeMessage:\"Welcome to group\", members:
			// ["+"\"+91"+assignToPhNo+
			// "\",\"+91" + poGeneratorPhNo+"\" ]}";
			String strBodyJSON = "{name:\"" + groupName
					+ "\", welcomeMessage:\"Welcome to group\", members: ["
					+ "\"" + assignToPhNo + "\",\"" + poGeneratorPhNo + "\" ]}";
			URL url = new URL("https://api.kaiza.la/v1/groups/");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type",
					"application/json; charset=UTF-8");
			// con.setRequestProperty("applicationId","BFD261DD3FB6587957F1D4AA8F62F6E09FCE1145923253A92362ECE15D3BD008");
			con.setRequestProperty("accessToken", accessToken);

			OutputStreamWriter os = new OutputStreamWriter(
					con.getOutputStream()); // Need OutputStreamWriter only,
											// with only OutputStream not
											// working gives BadRequest 400
											// error
			os.write(strBodyJSON);
			os.flush();
			os.close();

			// read the response
			StringBuilder sb = new StringBuilder();
			int HttpResult = con.getResponseCode();
			if (HttpResult == HttpURLConnection.HTTP_OK) {
				BufferedReader buffer = new BufferedReader(
						new InputStreamReader(con.getInputStream(), "utf-8"));
				String line = null;
				while ((line = buffer.readLine()) != null) {
					sb.append(line + "\n");
				}
				buffer.close();
			} else {
				// System.out.println(con.getResponseMessage());
			}
			JSONObject jObject = new JSONObject(sb.toString()); // json
			groupId = jObject.getString("groupId"); // get the name from data.
			// {"groupId":"095fb106-2103-4fc9-8067-98ff2771609f","groupName":"ManaliGroup1","membersAdded":true,"isAddedAsSubGroup":true}
			con.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return groupId;
	}

	public String getGroupMembers(String accessToken, String groupId,
			String assignToPhNo, String poGeneratorPhNo) {
		String groupMemebers = "";
		try {
			URL url = new URL("https://api.kaiza.la/v1/groups/" + groupId
					+ "/members");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type",
					"application/json; charset=UTF-8");
			con.setRequestProperty("accessToken", accessToken);

			// read the response
			StringBuilder sb = new StringBuilder();
			int HttpResult = con.getResponseCode();
			if (HttpResult == HttpURLConnection.HTTP_OK) {
				BufferedReader buffer = new BufferedReader(
						new InputStreamReader(con.getInputStream(), "utf-8"));
				String line = null;
				while ((line = buffer.readLine()) != null) {
					sb.append(line + "\n");
				}
				buffer.close();
			} /*
				 * else {
				 * System.out.println(con.getResponseMessage());
				 * }
				 */
			JSONObject jObject = new JSONObject(sb.toString()); // json
			JSONArray members = jObject.getJSONArray("members"); // get the name
																	// from
																	// data.
			String assignedTo = "";
			String vendorId = "";

			for (int i = 0, size = members.length(); i < size; i++) {
				JSONObject jObjassignedTo = members.getJSONObject(i);
				// String role=jObjassignedTo.getString("role");
				String phNo = jObjassignedTo.getString("mobileNumber");
				// phNo = phNo.replaceFirst("+91", "");
				if (phNo.equalsIgnoreCase(assignToPhNo)) { // role.equalsIgnoreCase("Member")
															// &&
					assignedTo = jObjassignedTo.getString("id");
				} else if (phNo.equalsIgnoreCase(poGeneratorPhNo)) {
					vendorId = jObjassignedTo.getString("id");
				}
			}
			groupMemebers = assignedTo + ";" + vendorId;
			/*
			 * { "members": [ { "id": "3c3a900c-67ff-49f8-a8bd-217f729f2a8b",
			 * "role": "Admin", "mobileNumber": "+919970177688", "name":
			 * "Shardul M", "isProvisioned": true }, { "id":
			 * "537e8f1e-8e58-45af-a97b-d96e8e1d1881", "role": "Member",
			 * "mobileNumber": "+919108506090", "name": "", "isProvisioned":
			 * false } ] }
			 */
			con.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return groupMemebers;
	}

	public String sendPurchaseOrderToKaizala(String accessToken, String groupId,
			String strBodyJSON) {
		String actionId = "";
		try {
			// accessToken="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cm46bWljcm9zb2Z0OmNyZWRlbnRpYWxzIjoie1wicGhvbmVOdW1iZXJcIjpcIis5MTk5NzAxNzc2ODhcIixcImNJZFwiOlwiXCIsXCJ0ZXN0U2VuZGVyXCI6XCJmYWxzZVwiLFwiYXBwTmFtZVwiOlwiY29tLm1pY3Jvc29mdC5tb2JpbGUua2FpemFsYWFwaVwiLFwiYXBwbGljYXRpb25JZFwiOlwiQkZEMjYxREQzRkI2NTg3OTU3RjFENEFBOEY2MkY2RTA5RkNFMTE0NTkyMzI1M0E5MjM2MkVDRTE1RDNCRDAwOFwiLFwicGVybWlzc2lvbnNcIjpcIjIuMzA6My4xNDo0LjI6Ni4yMjo1LjQ6OS4yOjE1LjMwXCIsXCJhcHBsaWNhdGlvblR5cGVcIjotMSxcImRhdGFcIjpcIntcXFwiR3JvdXBJZFxcXCI6XFxcImZjOGEwMzU0LTQzZGQtNGVhZS1hMmI3LWU1OWVkOTJjNDkxM1xcXCIsXFxcIkFwcE5hbWVcXFwiOlxcXCJJRE9TUE9cXFwifVwifSIsInVpZCI6Ik1vYmlsZUFwcHNTZXJ2aWNlOjNjM2E5MDBjLTY3ZmYtNDlmOC1hOGJkLTIxN2Y3MjlmMmE4YiIsInZlciI6IjIiLCJuYmYiOjE1MDA0NDE4MDYsImV4cCI6MTUwMDUyODIwNiwiaWF0IjoxNTAwNDQxODA2LCJpc3MiOiJ1cm46bWljcm9zb2Z0OndpbmRvd3MtYXp1cmU6enVtbyIsImF1ZCI6InVybjptaWNyb3NvZnQ6d2luZG93cy1henVyZTp6dW1vIn0.pqkmqD1HgNZLNB9WdiQDnStFapBwYs3UIjhHhDKx8mE";
			// String json = "{\"mobileNumber\":\"+919970177688\"}";
			// String strBodyJSON =
			// "{id:\"com.microsoft.kaizala.miniapps.idostest\", actionBody:{\"title\":
			// \"purchase order #EF50034\", properties:[{\"Name\":\"PL\", \"Value\":\"
			// [{'PN':'Tomato', 'PQ':'50', 'PPPU':'30', 'PF':'123675', 'PU':'Kgs'}]\",
			// \"Type\":6}, { \"Name\":\"SA\", \"Type\":0, \"Value\":\"Microsoft India R&D
			// pvt. ltd.\" }, { \"Name\":\"SS\", \"Type\":0, \"Value\":\"Telangana\" }, {
			// \"Name\":\"aTo\", \"Type\":0,
			// \"Value\":\"c1e74ab7-c2ed-4f63-912e-b9f016a17983\" }, { \"Name\":\"VID\",
			// \"Type\":0, \"Value\":\"c1e74ab7-c2ed-4f63-912e-b9f016a17983\" }, {
			// \"Name\":\"POD\", \"Type\":3, \"Value\":\"1500357780666\" }, {
			// \"Name\":\"state\", \"Type\":1, \"Value\":\"0\" } ] } }";
			URL url = new URL("https://api.kaiza.la/v1/groups/" + groupId
					+ "/actions");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type",
					"application/json; charset=UTF-8");
			con.setRequestProperty("applicationId",
					"BFD261DD3FB6587957F1D4AA8F62F6E09FCE1145923253A92362ECE15D3BD008");
			con.setRequestProperty("accessToken", accessToken);

			OutputStreamWriter os = new OutputStreamWriter(
					con.getOutputStream());
			os.write(strBodyJSON);
			os.flush();
			os.close();

			// display what returns the POST request
			StringBuilder sb = new StringBuilder();
			int HttpResult = con.getResponseCode();
			if (HttpResult == HttpURLConnection.HTTP_OK) {
				BufferedReader buffer = new BufferedReader(
						new InputStreamReader(con.getInputStream(), "utf-8"));
				String line = null;
				while ((line = buffer.readLine()) != null) {
					sb.append(line + "\n");
				}
				buffer.close();
				// { "referenceId": "9a9a5a77-33dc-44df-bc61-595a41b91b43",
				// "actionId": "08bf42e6-37f5-4faa-b9a8-456e7edbb449"}
			} /*
				 * else {
				 * System.out.println(con.getResponseMessage());
				 * }
				 */
			JSONObject jObject = new JSONObject(sb.toString()); // json
			actionId = jObject.getString("actionId");
			con.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return actionId;
	}

	public void sendResponse(String accessToken, String groupId,
			String actionId, Boolean success) {
		try {

			URL url = new URL("https://api.kaiza.la/v1/groups/" + groupId
					+ "/actions/" + actionId + "/responses ");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type",
					"application/json; charset=UTF-8");
			con.setRequestProperty("applicationId",
					"BFD261DD3FB6587957F1D4AA8F62F6E09FCE1145923253A92362ECE15D3BD008");
			con.setRequestProperty("accessToken", accessToken);
			OutputStreamWriter os = new OutputStreamWriter(
					con.getOutputStream());
			os.write(generateERPUploadResponseBody(success));
			os.flush();
			os.close();

			// display what returns the POST request
			StringBuilder sb = new StringBuilder();
			int HttpResult = con.getResponseCode();
			if (HttpResult == HttpURLConnection.HTTP_OK) {
				UpdateSMD(accessToken, groupId, actionId, true);
				BufferedReader buffer = new BufferedReader(
						new InputStreamReader(con.getInputStream(), "utf-8"));
				String line = null;
				while ((line = buffer.readLine()) != null) {
					sb.append(line + "\n");
				}
				buffer.close();

				// { "referenceId": "9a9a5a77-33dc-44df-bc61-595a41b91b43",
				// "actionId": "08bf42e6-37f5-4faa-b9a8-456e7edbb449"}
			}
			con.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	String generateERPUploadResponseBody(boolean success) {
		String message = "";

		try {
			JSONObject obj = new JSONObject();
			obj.put("actiontype", "survey");
			JSONArray list = new JSONArray();
			list.put("-");
			list.put("-");
			list.put("-");
			list.put("-");
			list.put("-");
			list.put("-");
			if (success) {
				list.put("ERP Upload Success");
			} else {
				list.put("ERP Upload Failure");
			}
			list.put("-");
			list.put("-");

			JSONObject answers = new JSONObject();
			answers.put("Answers", list);
			obj.put("actionBody", answers);
			message = obj.toString();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return message;

	}

	public void UpdateSMD(String accessToken, String groupId, String actionId,
			Boolean success) {
		try {
			URL url = new URL("https://api.kaiza.la/v1/groups/" + groupId
					+ "/actions/" + actionId + "/properties");

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("PUT");
			con.setRequestProperty("Content-Type",
					"application/json; charset=UTF-8");
			con.setRequestProperty("applicationId",
					"BFD261DD3FB6587957F1D4AA8F62F6E09FCE1145923253A92362ECE15D3BD008");
			con.setRequestProperty("accessToken", accessToken);
			OutputStreamWriter os = new OutputStreamWriter(
					con.getOutputStream());
			os.write(updateSMDRequestBody(groupId, success));
			os.flush();
			os.close();

			// display what returns the POST request
			StringBuilder sb = new StringBuilder();
			int HttpResult = con.getResponseCode();
			if (HttpResult == HttpURLConnection.HTTP_OK) {
				BufferedReader buffer = new BufferedReader(
						new InputStreamReader(con.getInputStream(), "utf-8"));
				String line = null;
				while ((line = buffer.readLine()) != null) {
					sb.append(line + "\n");
				}
				buffer.close();

				// { "referenceId": "9a9a5a77-33dc-44df-bc61-595a41b91b43",
				// "actionId": "08bf42e6-37f5-4faa-b9a8-456e7edbb449"}
			}
			con.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	String updateSMDRequestBody(String groupId, boolean success) {
		String message = "";

		try {
			JSONObject obj = new JSONObject();
			obj.put("version", -1);
			obj.put("notificationTargetGroup", groupId);
			JSONArray actionProperties = new JSONArray();
			JSONObject stateProperty = new JSONObject();
			stateProperty.put("propertyType", "Custom");
			stateProperty.put("name", "state");
			stateProperty.put("type", 1);
			stateProperty.put("value", "3");
			actionProperties.put(stateProperty);

			JSONObject approvalProperty = new JSONObject();
			approvalProperty.put("propertyType", "Custom");
			approvalProperty.put("name", "approval");
			approvalProperty.put("type", 1);
			if (success)
				approvalProperty.put("value", "3");
			else
				approvalProperty.put("value", "2");
			actionProperties.put(approvalProperty);
			obj.put("actionProperties", actionProperties);
			message = obj.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return message;
	}

}