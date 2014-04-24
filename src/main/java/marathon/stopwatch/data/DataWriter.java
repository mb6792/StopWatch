package marathon.stopwatch.data;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

public class DataWriter {

	static FileWriter sharedFile;
	static FileWriter localFile;
	static int timeCounter = 1;
	static DBConnect dbcon = null;
	
	public static void initialiseWriters(String byteFilePath, String eventFilePath) throws IOException{
		sharedFile = new FileWriter(byteFilePath);
		localFile = new FileWriter(eventFilePath);
		timeCounter = 1;
		dbcon = new DBConnect();
		try {
			dbcon.connect();
		} catch (ClassNotFoundException e) {
			System.out.println("CLASS NOT FOUND EXCEPTION!! " + e);
		} catch (SQLException e) {
			System.out.println("SQL EXCEPTION!!! " + e);
		}
	}
	
	public static void storeData(final String s){	
		sharedFileWrite(timeCounter, s);
		localFileWrite(timeCounter, s);
		stdoutWrite(timeCounter, s);
		dbcon.insertTime(timeCounter, s);
		timeCounter++;		
	}
	
	public static void sharedFileWrite(int counter, String s){
		try{
			sharedFile.write(counter + " | " + s + "\n");
			sharedFile.flush();
		}catch(Exception e){
			System.out.println("Failed to write to shared file: " + s);
		}
	}
	
	public static void localFileWrite(int counter, String s){
		try{
			localFile.write(counter + " | " + s + "\n");
			localFile.flush();
		}catch(Exception e){
			System.out.println("Failed to write to local file: " + s);
		}
	}
	
	public static void stdoutWrite(int counter ,String s){
			System.out.println("\n" + counter + " | " + s);
	}
	
	public static void close(){
		try{
			sharedFile.close();
			localFile.close();
			dbcon.closeConnection();
		}catch(Exception e){}
	}
	
	@Override
	public void finalize(){
		close();
	}
}
