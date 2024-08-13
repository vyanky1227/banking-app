package banking;

import java.sql.*;
import java.util.Scanner;

public class AccountManager {
    private Connection connection;
    private Scanner scanner;

    // Constructor to initialize connection and scanner
    public AccountManager() {
        try {
            // Establish MySQL connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/your_database_name", "your_username", "your_password");
        } catch (Exception e) {
            e.printStackTrace();
        }
        scanner = new Scanner(System.in);
    }

    public long open_account(String email) {
        if (!account_exist(email)) {
            String open_account_query = "INSERT INTO Accounts(account_number, full_name, email, balance, security_pin) VALUES(?, ?, ?, ?, ?)";
            scanner.nextLine();
            System.out.print("Enter Full Name: ");
            String full_name = scanner.nextLine();
            System.out.print("Enter Initial Amount: ");
            double balance = scanner.nextDouble();
            scanner.nextLine();
            System.out.print("Enter Security Pin: ");
            String security_pin = scanner.nextLine();
            
            try {
                long account_number = generateAccountNumber();
                try (PreparedStatement preparedStatement = connection.prepareStatement(open_account_query)) {
                    preparedStatement.setLong(1, account_number);
                    preparedStatement.setString(2, full_name);
                    preparedStatement.setString(3, email);
                    preparedStatement.setDouble(4, balance);
                    preparedStatement.setString(5, security_pin);
                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        return account_number;
                    } else {
                        throw new RuntimeException("Account Creation failed!!");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Error while creating account");
            }
        } else {
            throw new RuntimeException("Account Already Exists");
        }
    }

    public long getAccount_number(String email) {
        String query = "SELECT account_number FROM Accounts WHERE email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("account_number");
                } else {
                    throw new RuntimeException("Account Number Doesn't Exist!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while retrieving account number");
        }
    }

    private long generateAccountNumber() {
        String query = "SELECT account_number FROM Accounts ORDER BY account_number DESC LIMIT 1";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                long last_account_number = resultSet.getLong("account_number");
                return last_account_number + 1;
            } else {
                return 10000100; // Starting number if no accounts exist
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while generating account number");
        }
    }

    public boolean account_exist(String email) {
        String query = "SELECT 1 FROM Accounts WHERE email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while checking if account exists");
        }
    }

    // Close connection and scanner
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            if (scanner != null) {
                scanner.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
