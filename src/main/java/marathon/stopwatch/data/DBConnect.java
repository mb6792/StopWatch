package marathon.stopwatch.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


public class DBConnect {
	Connection conn = null;
	Connection conn_local = null;
	
	ArrayList<String[]> remote_queue = new ArrayList<String[]>();
	
	boolean connection_reset = false;
	
	//	JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	
	// REMOTE DATABASE
	static final String DB_URL = "jdbc:mysql://mark-bonnici.com:3306/marathon2014";
	static final String USER = "gozomarathon";
	static final String PASS = "gm2014!";
	
	// LOCAL DATABASE
	static final String DB_URL_LOCAL = "jdbc:mysql://localhost:3306/gm2014";
	static final String USER_LOCAL = "gm2014";
	static final String PASS_LOCAL = "gm2014!";
	
	public void connect() throws ClassNotFoundException, SQLException{
		Class.forName(JDBC_DRIVER);
		
		System.out.println("Connecting to the Remote Database...");
		conn = DriverManager.getConnection(DB_URL, USER, PASS);
		
		System.out.println("Connecting to the Local Database...");
		conn_local = DriverManager.getConnection(DB_URL_LOCAL, USER_LOCAL, PASS_LOCAL);
	}
	
	public void insertTime(final int stopwatch_auto_id, final String time) {
		new Thread(){
			public synchronized void run(){
				if(connection_reset == true){
					processRemoteQueue();
					connection_reset = false;
				}
				insertTime_local(stopwatch_auto_id, time);
				insertTime_remote(stopwatch_auto_id, time);
			}
		}.start();
	}
	
	public void insertTime_local(int stopwatch_auto_id, String time){
		String insertStat = "INSERT INTO marathon2014 (rt_stopwatch_auto_id, rt_time, rt_timestamp) VALUES (?, ?, ?)";
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date date = new Date();
		
		try{
			PreparedStatement prepstat_local = conn_local.prepareStatement(insertStat);
			prepstat_local.setInt(1, stopwatch_auto_id);
			prepstat_local.setString(2, time);
			prepstat_local.setString(3, sdf.format(date));
			prepstat_local.executeUpdate();
			
			System.out.println("LOCAL: ADDED SUCCESSFULLY: " + stopwatch_auto_id + ", " + time);
		}catch (SQLException sqle){
			// sqle.printStackTrace();
			System.out.println("LOCAL: ERROR IN ADDING: " + stopwatch_auto_id + ", " + time + "TAKE NOTICE");
		}
	}
	
	public void insertTime_remote(int stopwatch_auto_id, String time){
		String insertStat = "INSERT INTO marathon2014 (rt_stopwatch_auto_id, rt_time, rt_timestamp) VALUES (?, ?, ?)";
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date date = new Date();
		
		try{
			PreparedStatement prepstat = conn.prepareStatement(insertStat);
			prepstat.setInt(1, stopwatch_auto_id);
			prepstat.setString(2, time);
			prepstat.setString(3, sdf.format(date));
			prepstat.executeUpdate();
			
			System.out.println("REMOTE: ADDED SUCCESSFULLY: " + stopwatch_auto_id + ", " + time);
		}catch(SQLException sqle){
			// sqle.printStackTrace();
			
			try {
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				connection_reset = true;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("Reconnection Failure");
			}
			
			
			String[] current_error = new String[2];
			current_error[0] = "" + stopwatch_auto_id;
			current_error[1] = time;
			remote_queue.add(current_error);
			
			Iterator<String[]> it = remote_queue.iterator();
			while(it.hasNext()){
			    String[] error = it.next();
			    System.out.println( error[0] + " # " + error[1]);
			}
			
			System.out.println("REMOTE: ERROR IN ADDING: " + stopwatch_auto_id + ", " + time + "TAKE NOTICE");
		}
	}
	
	public void processRemoteQueue(){	
		Iterator<String[]> it = remote_queue.iterator();
			
		while(it.hasNext()){
		    String[] error = it.next();
		    insertTime_remote(Integer.parseInt(error[0]), error[1]);	    
		}
			
		remote_queue.clear();	
	}
	
	public void closeConnection() throws SQLException{
		conn_local.close();
		conn.close();
	}
}
