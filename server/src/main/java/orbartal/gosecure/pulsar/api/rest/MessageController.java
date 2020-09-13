package orbartal.gosecure.pulsar.api.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import orbartal.gosecure.pulsar.api.rest.model.PulsarMessageDto;
import orbartal.gosecure.pulsar.application.MessageAppWriter;

@RestController
@RequestMapping("/v1/message")
public class MessageController {

	private MessageAppWriter messageAppWriter;

	public MessageController(MessageAppWriter messageAppWriter) {
		this.messageAppWriter = messageAppWriter;
	}

	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Void> create(@RequestBody PulsarMessageDto message) {
		messageAppWriter.write(message);
		return ResponseEntity.ok().build();     
	}

}
