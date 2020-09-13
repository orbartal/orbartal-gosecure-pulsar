
package orbartal.gosecure.pulsar;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;

public class MessageControllerTest {

	private static final String APPLICATION_JSON_VALUE = "application/json";
	private static final String API_PATH = "/v1/message/create";

	public MessageControllerTest() {
		RestAssured.baseURI = Configuration.get().getServerBaseUrl();
	}

	@Test
	public void testOneMessage() throws JSONException {
		JSONObject requestParams = new JSONObject();
		requestParams.put("value", "value_1"); 
		requestParams.put("topic", "topic_1");
		
		Response response = RestAssured.given()
				.contentType(APPLICATION_JSON_VALUE)
				.body(requestParams.toString())
				.request(Method.POST, API_PATH);
		
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		
	}

}
