
package orbartal.gosecure.pulsar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.common.policies.data.TopicStats;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import orbartal.gosecure.pulsar.pulsar.CloseableUtil;

public class MessageControllerTest {

	private static final String SUBSCRIPTION_NAME = "SUBSCRIPTION_NAME";
	private static final String APPLICATION_JSON_VALUE = "application/json";
	private static final String API_PATH = "/v1/message/create";
	private static final  String PULSAR_NAMESPACE_1  = "public/default";

	private static final int MESSAGE_LENGTH = 8;

	private CloseableUtil closeableUtil = new CloseableUtil();
	private String pulsarRestApi;

	public MessageControllerTest() {
		RestAssured.baseURI = Configuration.get().getServerBaseUrl();
		pulsarRestApi = Configuration.get().getPulsarServeRestApiUrl();
	}

	@Test
	public void testDeleteAllMessages() throws Exception {
		PulsarAdmin admin = PulsarAdmin.builder().serviceHttpUrl(pulsarRestApi).build();
		List<String> topics = admin.topics().getList(PULSAR_NAMESPACE_1);
		for (String topic : topics) {
			String last = getTopicFromFullName(topic);
			admin.topics().delete(last);
		}
	}

	@Test
	public void testGetDataOnTopics() throws Exception {
		PulsarAdmin admin = PulsarAdmin.builder().serviceHttpUrl(pulsarRestApi).build();
		List<String> topics = admin.topics().getList(PULSAR_NAMESPACE_1);
		for (String topic : topics) {
			TopicStats state = admin.topics().getStats(topic);
			JSONObject requestParams = new JSONObject();
			requestParams.put("storageSize", state.storageSize); 
			requestParams.put("msgInCounter", state.msgInCounter); 
			requestParams.put("msgOutCounter", state.msgOutCounter);
			System.out.println(topic + " = " +requestParams);
		}
	}

	@Test(timeout = 10000)
	public void testOneMessageResponse() throws Exception {
		Response response = sendMessage("value_1", "topic_1");
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test(timeout = 60*1000)
	public void testOneMessagePulsar() throws JSONException {
		String value = getRandomText();
		String topic = getRandomText();
		sendMessage(value, topic);
		PulsarClient client = null;
		Consumer<String> consumer = null;
		try {
			client = buildClient();
			consumer = buildConsumer(client, topic);
			Message<String> message = consumer.receive();
			consumer.acknowledge(message);
			String actual = message.getValue();
			Assert.assertEquals(value, actual);
		} catch (Exception e) {
			Assert.fail();
		} finally {
			closeableUtil.closeQuietly(consumer);
			closeableUtil.closeQuietly(client);
		}
	}

	@Test(timeout = 90 * 1000)
	public void testManyMessagesOneTopicPulsar() throws JSONException {
		int size = 10; //number of messages
		List<String> values = IntStream.range(0, size).boxed().map(i -> getRandomText()).sorted().collect(Collectors.toList());
		String topic = getRandomText();
		values.forEach(v -> sendMessage(v, topic));
		String pulsarApi = Configuration.get().getPulsarBrokerApiUrl();
		PulsarClient client = null;
		Consumer<String> consumer = null;
		try {
			List<String> actual = new ArrayList<>();
			client = PulsarClient.builder().serviceUrl(pulsarApi).build();
			consumer = buildConsumer(client, topic);
			for (int i = 0; i < size; i++) {
				Message<String> msg = consumer.receive();
				consumer.acknowledge(msg);
				String value = msg.getValue();
				actual.add(value);
			}
			Assert.assertEquals(values.size(), actual.size());
			Assert.assertEquals(values, actual.stream().sorted().collect(Collectors.toList()));
		} catch (Exception e) {
			Assert.fail();
		} finally {
			closeableUtil.closeQuietly(consumer);
			closeableUtil.closeQuietly(client);
		}
	}
	
	@Test(timeout = 90 * 1000)
	public void testPulsarManyTopicsWithUniqueConsumerPerTopic() throws JSONException {
		int size = 10; //number of messages
		List<String> topics = IntStream.range(0, size).boxed().map(i -> getRandomText()).sorted().collect(Collectors.toList());
		Map<String, String> valueByTopic = topics.stream().collect(Collectors.toMap(t->t, t->getRandomText()));
		valueByTopic.entrySet().forEach(p -> sendMessage(p.getValue(), p.getKey()));
		String pulsarApi = Configuration.get().getPulsarBrokerApiUrl();
		PulsarClient client = null;
		Consumer<String> consumer = null;
		try {
			Map<String, String> actual = new HashMap<>();
			client = PulsarClient.builder().serviceUrl(pulsarApi).build();
			for (String topic : valueByTopic.keySet()) {
				consumer = buildConsumer(client, topic);
				Message<String> msg = consumer.receive();
				consumer.acknowledge(msg);
				String value = msg.getValue();
				actual.put(topic, value);
			}
			Assert.assertEquals(valueByTopic.size(), actual.size());
			topics.forEach(t->Assert.assertEquals(valueByTopic.get(t), actual.get(t)));
		} catch (Exception e) {
			Assert.fail();
		} finally {
			closeableUtil.closeQuietly(consumer);
			closeableUtil.closeQuietly(client);
		}
	}

	@Test(timeout = 90 * 1000)
	public void testPulsarManyTopicsWithSameConsumerForAllTopics() throws JSONException {
		int size = 10; //number of messages
		List<String> topics = IntStream.range(0, size).boxed().map(i -> getRandomText()).sorted().collect(Collectors.toList());
		Map<String, String> valueByTopic = topics.stream().collect(Collectors.toMap(t->t, t->getRandomText()));
		valueByTopic.entrySet().forEach(p -> sendMessage(p.getValue(), p.getKey()));
		String pulsarApi = Configuration.get().getPulsarBrokerApiUrl();
		PulsarClient client = null;
		Consumer<String> consumer = null;
		try {
			Map<String, String> actual = new HashMap<>();
			client = PulsarClient.builder().serviceUrl(pulsarApi).build();
			consumer = buildConsumer(client, topics);
			for (int i =0 ; i< size; i++) {
				Message<String> msg = consumer.receive();
				consumer.acknowledge(msg);
				String value = msg.getValue();
				String topic = getTopicFromFullName(msg.getTopicName());
				actual.put(topic, value);
			}
			Assert.assertEquals(valueByTopic.size(), actual.size());
			topics.forEach(t->Assert.assertEquals(valueByTopic.get(t), actual.get(t)));
		} catch (Exception e) {
			Assert.fail();
		} finally {
			closeableUtil.closeQuietly(consumer);
			closeableUtil.closeQuietly(client);
		}
	}

	private Consumer<String> buildConsumer(PulsarClient client, String topic) throws PulsarClientException {
		return client.newConsumer(Schema.STRING)
			.topic(topic)
			.subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
			.subscriptionType(SubscriptionType.Exclusive)
			.subscriptionName(SUBSCRIPTION_NAME).subscribe();
	}
	
	private Consumer<String> buildConsumer(PulsarClient client, List<String> topics) throws PulsarClientException {
		return client.newConsumer(Schema.STRING)
			.topics(topics)
			.subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
			.subscriptionType(SubscriptionType.Exclusive)
			.subscriptionName(SUBSCRIPTION_NAME).subscribe();
	}
	
	private Response sendMessage(String value, String topic) {
		JSONObject requestParams = new JSONObject();
		try {
			requestParams.put("value", value);
			requestParams.put("topic", topic);
			return RestAssured.given()
					.contentType(APPLICATION_JSON_VALUE)
					.body(requestParams.toString())
					.request(Method.POST, API_PATH);
		} catch (JSONException e) {
			throw new RuntimeException();
		} 
	}
	
	private PulsarClient buildClient() throws PulsarClientException {
		return PulsarClient.builder().serviceUrl(Configuration.get().getPulsarBrokerApiUrl()).build();
	}

	private String getRandomText() {
		return getRandomText(MESSAGE_LENGTH);
	}

	private String getRandomText(int length) {
		return RandomStringUtils.randomAlphabetic(length).toLowerCase();
	}

	private String getTopicFromFullName(String topic) {
		String[] arr = topic.split("/");
		String last = arr[arr.length-1];
		return last;
	}

}
