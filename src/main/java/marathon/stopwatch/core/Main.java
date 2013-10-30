package marathon.stopwatch.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import marathon.stopwatch.serial.Reader;
import marathon.stopwatch.data.DataWriter;
import marathon.stopwatch.logging.Logger;


public class Main {

	private static String COM_PORT = "COM_PORT";
	private static String BAUD_RATE = "BAUD_RATE";
	private static String BYTE_LOGGER_PATH = "BYTE_LOGGER_PATH";
	private static String EVENT_LOGGER_PATH = "EVENT_LOGGER_PATH";
	private static String TIME_FILE_1 = "TIME_FILE_1";
	private static String TIME_FILE_2 = "TIME_FILE_2";
	
		
	public static void main(String[] args) throws Exception{
		
		System.out.println("STARTING!");
		
		loadConfiguration();
		Logger.initialiseLoggers(BYTE_LOGGER_PATH, EVENT_LOGGER_PATH);
		DataWriter.initialiseWriters(TIME_FILE_1, TIME_FILE_2);
				
		Reader reader = new Reader();
		reader.getAvailablePorts();
		reader.open(COM_PORT, BAUD_RATE);
		reader.read();
	}
	
	private static void loadConfiguration() throws Exception {
		
		Properties prop = new Properties();
		
		File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String path;
		
		if(jarFile.isFile()) {
			path = jarFile.getParent();
		} else {
			path = jarFile.getPath();
			
			if(path.endsWith("bin") || path.endsWith("bin\\"))
				path = jarFile.getPath().replace("\\bin","");
		}
		
		path = path.replaceAll("%20", " ");
		
		File propertiesFile = new File(path, "config.properties");
		
		prop.load(new FileInputStream(propertiesFile));
		
		COM_PORT = prop.getProperty(COM_PORT);
		BAUD_RATE = prop.getProperty(BAUD_RATE);
		BYTE_LOGGER_PATH = prop.getProperty(BYTE_LOGGER_PATH);
		EVENT_LOGGER_PATH = prop.getProperty(EVENT_LOGGER_PATH);
		TIME_FILE_1 = prop.getProperty(TIME_FILE_1);
		TIME_FILE_2 = prop.getProperty(TIME_FILE_2);

	}
}
