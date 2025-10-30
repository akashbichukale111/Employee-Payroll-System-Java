import java.sql.*;
import java.util.Scanner;

public class EmployeePayrollSystem {

    // Database Connection Details - Change if necessary
    private static final String DB_URL = "jdbc:mysql://localhost:3306/employee_db";
    private static final String DB_USER = "root";   
    private static final String DB_PASS = "";       

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection connection = null;

        try {
            // 1. Load the Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 2. Establish Database Connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("✅ Database Connection Successful!");

            // Admin Login
            if (adminLogin(connection, scanner)) {
                System.out.println("\n--- Employee Payroll System ---");
                
                boolean running = true;
                while(running) {
                    System.out.println("\nSelect Option:");
                    System.out.println("1. Add New Employee");
                    System.out.println("2. View All Employees");
                    System.out.println("3. Exit");
                    System.out.print("Enter choice (1-3): ");
                    
                    if(scanner.hasNextInt()) {
                        int choice = scanner.nextInt();
                        scanner.nextLine(); // Consume newline
                        
                        switch(choice) {
                            case 1:
                                addNewEmployee(connection, scanner);
                                break;
                            case 2:
                                viewAllEmployees(connection);
                                break;
                            case 3:
                                running = false;
                                System.out.println("Thank you for using the Payroll System!");
                                break;
                            default:
                                System.out.println("Invalid choice. Please try again.");
                        }
                    } else {
                        System.out.println("Invalid input. Please enter a number.");
                        scanner.nextLine(); // Clear the buffer
                    }
                }
            } else {
                System.out.println("❌ Invalid Admin Credentials. Exiting...");
            }

        } catch (ClassNotFoundException e) {
            System.err.println("❌ Error: MySQL JDBC Driver not found. Please ensure the .jar file is in the classpath.");
        } catch (SQLException e) {
            System.err.println("❌ Database Connection Error: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
                scanner.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    // --- Admin Login Function ---
    private static boolean adminLogin(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n--- Admin Login ---");
        System.out.print("Username: ");
        String user = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setString(2, pass);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Returns TRUE if record is found
            }
        }
    }

    // --- Add New Employee Function ---
    private static void addNewEmployee(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n--- Add New Employee ---");
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Department: ");
        String dept = scanner.nextLine();
        System.out.print("Enter Basic Salary: ");
        
        double basicSalary = 0;
        if(scanner.hasNextDouble()) {
            basicSalary = scanner.nextDouble();
            scanner.nextLine(); // Consume newline
        } else {
            System.out.println("Invalid salary input. Returning to main menu.");
            scanner.nextLine(); // Clear the buffer
            return;
        }

        // Simple Salary Calculation: Gross Salary = Basic Salary + 20% Bonus
        double grossSalary = basicSalary * 1.20;

        String sql = "INSERT INTO employees (name, department, basic_salary, gross_salary) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, dept);
            pstmt.setDouble(3, basicSalary);
            pstmt.setDouble(4, grossSalary);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.printf("✅ Employee added successfully! Gross Salary: %.2f%n", grossSalary);
            }
        }
    }

    // --- View All Employees Function ---
    private static void viewAllEmployees(Connection conn) throws SQLException {
        System.out.println("\n--- All Employees ---");
        String sql = "SELECT * FROM employees";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.printf("%-5s %-20s %-15s %-10s%n", "ID", "Name", "Department", "Gross Salary");
            System.out.println("-----------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-5d %-20s %-15s %.2f%n",
                    rs.getInt("employee_id"),
                    rs.getString("name"),
                    rs.getString("department"),
                    rs.getDouble("gross_salary"));
            }
        }
    }
}