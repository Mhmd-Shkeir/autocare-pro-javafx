	package application;
	
	import java.sql.Connection;
	import java.sql.DriverManager;
	import java.sql.SQLException;
	
	public class DatabaseConnection {
	    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=Car_Maintenance;user=User;password=123;trustServerCertificate=true";
	
	    public static Connection connect() {
	        Connection conn = null;
	        try {
	            conn = DriverManager.getConnection(URL);
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
	        return conn;
	    }
	}
	
