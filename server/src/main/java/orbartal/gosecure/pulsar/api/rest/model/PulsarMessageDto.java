package orbartal.gosecure.pulsar.api.rest.model;

public class PulsarMessageDto {

	private String topic;
	private String value;

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
