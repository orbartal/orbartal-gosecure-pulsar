package orbartal.gosecure.pulsar;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

	private static final String PATH_TO_FILES = getPathToTestFiles();
	private static final String CONFIG_FILE = "test.config";
	
	private static String getPathToTestFiles() {
		String path = "src/test/resources";
		File file = new File(path);
		return file.getAbsolutePath();
	}
	

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
	
	public String getPathToSmallFile() {
		return getPath("test1.words.s1");
	}
	
	public String getPathToMediumFile() {
		return getPath("test1.words.m1");
	}
	
	public String getPathToLargeFile() {
		return getPath("test1.words.l1");
	}
	
	public String getPathToHugeFile() {
		return getPath("test1.words.h1");
	}
	
	public String getPath(String key) {
		String filename = properties.get(key).toString();
		return PATH_TO_FILES + File.separator + filename;
	}

}
