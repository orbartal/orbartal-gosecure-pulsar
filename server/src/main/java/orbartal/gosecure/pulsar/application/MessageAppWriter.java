package orbartal.gosecure.pulsar.application;

import org.springframework.stereotype.Component;

import orbartal.gosecure.pulsar.api.rest.model.PulsarMessageDto;
import orbartal.gosecure.pulsar.pulsar.PulsarMessageWriter;

@Component
public class MessageAppWriter {

	private PulsarMessageWriter pulsarMessageWriter;

	public MessageAppWriter(PulsarMessageWriter pulsarMessageWriter) {
		this.pulsarMessageWriter = pulsarMessageWriter;
	}

	public void write(PulsarMessageDto message) {
		if (message==null || message.getTopic()== null || message.getValue()==null) {
			throw new RuntimeException();
		}
		pulsarMessageWriter.write(message.getTopic(), message.getValue());
	}

}
