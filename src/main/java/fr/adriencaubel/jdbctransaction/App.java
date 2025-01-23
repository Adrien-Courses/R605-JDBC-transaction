package fr.adriencaubel.jdbctransaction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class App {

	private static final double MONTANT_TRANSFERT = 100;
	
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
	
	private static void effectuerTransfert(Connection connection) throws SQLException {
		// AUTO COMMIT FALSE
		connection.setAutoCommit(false);
		
		try {
			// Récupérer le montant du compte source
			String fromQuery = "SELECT balance FROM account WHERE name = ?";
			PreparedStatement getBalanceStmt = connection.prepareStatement(fromQuery);
			getBalanceStmt.setString(1, "Alice");
			ResultSet resultSet = getBalanceStmt.executeQuery();
			Double fromBalance = 0.0;
			if(resultSet.next()) {
				fromBalance = resultSet.getDouble("balance");
			}
			
			// Vérifier le montant du compte source
			if(fromBalance < MONTANT_TRANSFERT) {
                throw new SQLException("Insufficient funds in source account");
			}
			
			// Effectuer le transfert
			String updateBalanceQuery = "UPDATE account SET balance = ? WHERE name = ?";
			
            PreparedStatement updateBalanceStmt = connection.prepareStatement(updateBalanceQuery);
			Double newFromBalance = fromBalance - MONTANT_TRANSFERT; // mettre à jour la source
			updateBalanceStmt.setDouble(1, newFromBalance);
			updateBalanceStmt.setString(2, "Alice");
			updateBalanceStmt.execute();
			
			getBalanceStmt.setString(1, "Bob"); // récupérer le montant du destinataire
			resultSet = getBalanceStmt.executeQuery();
			Double toBalance = 0.0;
			if(resultSet.next()) {
				toBalance = resultSet.getDouble("balance");
			}
			
			// Simuler exception
		    // if (true) {
		    //     throw new SQLException("Simulated exception during the transfer.");
		    // }
			
			Double newToBalance = toBalance + MONTANT_TRANSFERT;
			updateBalanceStmt.setDouble(1, newToBalance); // mettre à jour la destination
			updateBalanceStmt.setString(2, "Bob");
			updateBalanceStmt.execute();
			
			// COMMIT
			connection.commit();
			System.out.println("Transfert réalisé");
		} catch (SQLException e) {
			connection.rollback();
			e.printStackTrace();
		} finally {
	        // Toujours remettre l'auto-commit à true
	        connection.setAutoCommit(true);
	    }
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
