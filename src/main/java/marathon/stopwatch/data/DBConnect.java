package marathon.stopwatch.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class DBConnect {
	Connection conn = null;
	
	//	JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost:3306/sannat";
	
	//	Database Credentials
	static final String USER = "sannat";
	static final String PASS = "marathon";
	
	public void connect() throws ClassNotFoundException, SQLException{
		Class.forName(JDBC_DRIVER);
		
		System.out.println("Connecting to the database...");
		conn = DriverManager.getConnection(DB_URL, USER, PASS);
	}
	
	public void insertTime(int stopwatch_auto_id, String time) {
		try{
			String insertStat = "INSERT INTO runner_time (rt_stopwatch_auto_id, rt_time) VALUES (?, ?)";
			PreparedStatement prepstat = conn.prepareStatement(insertStat);
			
			prepstat.setInt(1, stopwatch_auto_id);
			prepstat.setString(2, time);
			
			prepstat.executeUpdate();
			
			System.out.println("ADDED SUCCESSFULLY: " + stopwatch_auto_id + ", " + time);
		}catch (SQLException sqle){
			System.out.println("ERROR IN ADDING: " + stopwatch_auto_id + ", " + time + "TAKE NOTICE");
		}
	}
	
	public void closeConnection() throws SQLException{
		conn.close();
	}
}
