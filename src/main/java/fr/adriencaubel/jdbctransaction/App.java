package fr.adriencaubel.jdbctransaction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class App {

	public static void main(String[] args) {
		String url = "jdbc:mysql://localhost:3313/jdbc-transaction";
        String user = "root";
        String password = "password";
		
        try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection connection = DriverManager.getConnection(url, user, password);
			createTable(connection);
			initializeAccounts(connection);
			effectuerTransfert(connection);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
        
	}
	
	private static void effectuerTransfert(Connection connection) {
		/* TODO */
	}
	
	
    private static void createTable(Connection connection) throws SQLException {
        String createTableQuery = """
                CREATE TABLE IF NOT EXISTS account (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(50),
                    balance DOUBLE
                );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableQuery);
        }
    }
    
    private static void initializeAccounts(Connection connection) throws SQLException {
        String insertQuery = "INSERT INTO account (name, balance) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setString(1, "Alice");
            pstmt.setDouble(2, 1000.00);
            pstmt.executeUpdate();

            pstmt.setString(1, "Bob");
            pstmt.setDouble(2, 500.00);
            pstmt.executeUpdate();
        }
    }
}
