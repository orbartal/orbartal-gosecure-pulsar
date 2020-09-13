
package orbartal.gosecure.pulsar;

import org.apache.http.HttpStatus;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import orbartal.gosecure.pulsar.pulsar.CloseableUtil;

public class MessageControllerTest {

	private static final String APPLICATION_JSON_VALUE = "application/json";
	private static final String API_PATH = "/v1/message/create";

	private CloseableUtil closeableUtil = new CloseableUtil();

	public MessageControllerTest() {
		RestAssured.baseURI = Configuration.get().getServerBaseUrl();
	}

	@Test(timeout = 10000)
	public void testOneMessageResponse() throws JSONException {
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

	@Test(timeout = 10000)
	public void testOneMessagePulsar() throws JSONException {
		String value = "value_1";
		String topic = "topic_1";

		JSONObject requestParams = new JSONObject();
		requestParams.put("value", value); 
		requestParams.put("topic", topic);

		RestAssured.given()
				.contentType(APPLICATION_JSON_VALUE)
				.body(requestParams.toString())
				.request(Method.POST, API_PATH);
		
		String pulsarApi = Configuration.get().getPulsarServeApiUrl();
		PulsarClient client = null;
		Consumer<String> consumer = null;
		try {
			client = PulsarClient.builder().serviceUrl(pulsarApi).build();
			consumer = client.newConsumer(Schema.STRING).topic(topic).subscriptionName("subscriptionName1").subscribe();
			 Message<String> msg = consumer.receive();
	         consumer.acknowledge(msg);
	         String actual = msg.getValue();
	 		 Assert.assertEquals(value, actual);
		} catch (Exception e) {
			Assert.fail();
		} finally {
			closeableUtil.closeQuietly(consumer);
			closeableUtil.closeQuietly(client);
		}
	}

}
