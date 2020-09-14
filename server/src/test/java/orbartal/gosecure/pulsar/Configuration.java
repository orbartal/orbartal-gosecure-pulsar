package orbartal.gosecure.pulsar;

import java.io.InputStream;
import java.util.Properties;

public class Configuration {

	private static final String CONFIG_FILE = "test.config";

	private Properties properties;
	private static Configuration instance = new Configuration();

	public static Configuration get() {
		return instance;
	}

	private Configuration() {
		loadProperties();
	}

	private void loadProperties() {
		ClassLoader classLoader = Configuration.class.getClassLoader();
		try (InputStream input = classLoader.getResourceAsStream(CONFIG_FILE)) {
			if (input == null) {
				throw new RuntimeException("Error, unable to find config.properties");
			}
			this.properties = new Properties();
			this.properties.load(input);
		} catch (Exception ex) {
			throw new RuntimeException("Error, unable to load config.properties");
		}
	}

	public String getServerBaseUrl() {
		return properties.get("server.base.url").toString();
	}

	public String getPulsarBrokerApiUrl() {
		return properties.get("pulsar.api.url.port").toString();
	}
	
	public String getPulsarServeRestApiUrl() {
		return properties.get("pulsar.api.rest").toString();
	}

}
