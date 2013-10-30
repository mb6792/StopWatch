package marathon.stopwatch.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Logger {
	
	static FileWriter byteFileWriter;
	static FileWriter eventFileWriter;

	public static void initialiseLoggers(String byteFilePath, String eventFilePath) throws IOException{
		byteFileWriter = new FileWriter(byteFilePath);
		eventFileWriter = new FileWriter(eventFilePath);
	}
	
	public static void logByte(byte b){
		try{
			byteFileWriter.write(b);
			byteFileWriter.flush();
		}catch(Exception e){}
	}
	
	public static void logEvent(String s){
		try{
			s = new Date().getTime() + ": " + s;
			eventFileWriter.write(s + "\n");
			eventFileWriter.flush();
		}catch(Exception e){}
	}
	
	public static void close(){
		try{
			byteFileWriter.close();
			eventFileWriter.close();
		}catch(Exception e){}
	}
	
	@Override
	public void finalize(){
		close();
	}
}
