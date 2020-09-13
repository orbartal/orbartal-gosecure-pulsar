package orbartal.gosecure.pulsar.pulsar;

import java.io.Closeable;
import java.io.IOException;

public class CloseableUtil {
	
	public void closeQuietly(Closeable input) {
		if (input==null) {
			return;
		}
		try {
			input.close();
		} catch (IOException e) {
			throw new RuntimeException();
		}
		
	}

}
