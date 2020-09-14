package orbartal.gosecure.pulsar.pulsar;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PulsarMessageWriter {

	@Value("${pulsar.api.url.port}")
	private String serverUrl;

	private CloseableUtil closeableUtil = new CloseableUtil();

	public void write(String topic, String value) {
		PulsarClient client = null;
		Producer<String> producer = null;
		Consumer<String> consumer = null;
		try {
			client = PulsarClient.builder().serviceUrl(serverUrl).build();
			producer = client.newProducer(Schema.STRING).topic(topic).create();
			consumer = client.newConsumer(Schema.STRING).topic(topic).subscriptionName("SUBSCRIPTION_NAME").subscribe();
			producer.send(value);
			producer.flush();
		} catch (Exception e) {
			throw new RuntimeException();
		} finally {
			closeableUtil.closeQuietly(consumer);
			closeableUtil.closeQuietly(producer);
			closeableUtil.closeQuietly(client);
		}
	}

}
