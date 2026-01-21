package main.java;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeePayrollDBService {

    private static final String JDBC_URL =
            "jdbc:mysql://localhost:3306/employeepayrollsystem?useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    
    private static EmployeePayrollDBService instance; // Singleton instance
    private Connection connection;
    private PreparedStatement retrieveByNameStmt; // Cached PreparedStatement
    private PreparedStatement retrieveByDateRangeStmt;

    // Private constructor for Singleton
    private EmployeePayrollDBService() throws PayrollDBException {
        try {
            this.connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
            // Prepare and cache PreparedStatement
            this.retrieveByNameStmt = connection.prepareStatement(
                    "SELECT e.empId, e.empName, e.gender, e.startDate, p.netPay " +
                    "FROM employee e " +
                    "JOIN payroll p ON e.empId = p.empId " +
                    "WHERE e.empName = ?"
            );
            
            // Cached PreparedStatement for date range
            this.retrieveByDateRangeStmt = connection.prepareStatement(
                "SELECT e.empId, e.empName, e.gender, e.startDate, p.netPay " +
                "FROM employee e JOIN payroll p ON e.empId = p.empId " +
                "WHERE e.startDate BETWEEN ? AND ?"
            );

        } catch (SQLException e) {
            throw new PayrollDBException("Failed to initialize DB connection or prepare statement", e);
        }
    }

    // Public method to get Singleton instance
    public static EmployeePayrollDBService getInstance() throws PayrollDBException {
        if (instance == null) {
            instance = new EmployeePayrollDBService();
        }
        return instance;
    }
    
    // Do NOT close the PreparedStatement inside the method
    public List<EmployeePayroll> getEmployeesByDateRange(LocalDate start, LocalDate end) throws PayrollDBException {
        List<EmployeePayroll> employeeList = new ArrayList<>();
        try {
            retrieveByDateRangeStmt.setDate(1, Date.valueOf(start));
            retrieveByDateRangeStmt.setDate(2, Date.valueOf(end));

            // Only ResultSet is try-with-resources
            try (ResultSet rs = retrieveByDateRangeStmt.executeQuery()) {
                while (rs.next()) {
                    int empId = rs.getInt("empId");
                    String name = rs.getString("empName");
                    String gender = rs.getString("gender");

                    Date startDateSQL = rs.getDate("startDate");
                    LocalDate startDate = (startDateSQL != null) ? startDateSQL.toLocalDate() : null;

                    double netPay = rs.getDouble("netPay");

                    employeeList.add(new EmployeePayroll(empId, name, gender, startDate, netPay));
                }
            }

        } catch (SQLException e) {
            throw new PayrollDBException("Error retrieving employees by date range", e);
        }
        return employeeList;
    }

    // Retrieve employee payroll by name
    public List<EmployeePayroll> getEmployeePayrollByName(String empName) throws PayrollDBException {
        List<EmployeePayroll> payrollList = new ArrayList<>();
        try {
            retrieveByNameStmt.setString(1, empName);
            try (ResultSet rs = retrieveByNameStmt.executeQuery()) { // Reuse ResultSet
                while (rs.next()) {
                    int empId = rs.getInt("empId");
                    String name = rs.getString("empName");
                    String gender = rs.getString("gender");
                    Date startDateSQL = rs.getDate("startDate");
                    LocalDate startDate = (startDateSQL != null) ? startDateSQL.toLocalDate() : null;
                    double netPay = rs.getDouble("netPay");

                    payrollList.add(new EmployeePayroll(empId, name, gender, startDate, netPay));
                }
            }
        } catch (SQLException e) {
            throw new PayrollDBException("Error retrieving payroll data for " + empName, e);
        }
        return payrollList;
    }

    // Close method: call only at program exit
    public void close() {
        try {
            if (retrieveByNameStmt != null) retrieveByNameStmt.close();
            if (retrieveByDateRangeStmt != null) retrieveByDateRangeStmt.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<EmployeePayroll> readEmployeePayrollData() throws PayrollDBException {
        List<EmployeePayroll> payrollList = new ArrayList<>();
        String sql = "SELECT e.empId, e.empName, e.gender, e.startDate, p.netPay " +
                     "FROM employee e " +
                     "JOIN payroll p ON e.empId = p.empId";

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                int empId = rs.getInt("empId");
                String empName = rs.getString("empName");
                String gender = rs.getString("gender");

                Date startDateSQL = rs.getDate("startDate");
                LocalDate startDate = (startDateSQL != null) ? startDateSQL.toLocalDate() : null;

                double netPay = rs.getDouble("netPay");

                payrollList.add(new EmployeePayroll(empId, empName, gender, startDate, netPay));
            }
        } catch (Exception e) {
            throw new PayrollDBException("Failed to retrieve employee payroll data", e);
        }
        return payrollList;
    }

    // Update salary for a given employee and sync object
    public EmployeePayroll updateEmployeeSalary(String empName, double newSalary) throws PayrollDBException {
        String updateSQL = "UPDATE payroll p " +
                "JOIN employee e ON e.empId = p.empId " +
                "SET p.netPay = ? " +
                "WHERE e.empName = ?";

        String selectSQL = "SELECT e.empId, e.empName, e.gender, e.startDate, p.netPay " +
                "FROM employee e " +
                "JOIN payroll p ON e.empId = p.empId " +
                "WHERE e.empName = ?";

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {

            // 1️⃣ Update DB salary
            try (PreparedStatement updateStmt = connection.prepareStatement(updateSQL)) {
                updateStmt.setDouble(1, newSalary);
                updateStmt.setString(2, empName);
                int rowsUpdated = updateStmt.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new PayrollDBException("No employee found with name: " + empName);
                }
            }

            // 2️⃣ Retrieve updated employee data
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
                selectStmt.setString(1, empName);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        int empId = rs.getInt("empId");
                        String name = rs.getString("empName");
                        String gender = rs.getString("gender");
                        Date startDateSQL = rs.getDate("startDate");
                        LocalDate startDate = (startDateSQL != null) ? startDateSQL.toLocalDate() : null;
                        double netPay = rs.getDouble("netPay");

                        return new EmployeePayroll(empId, name, gender, startDate, netPay);
                    } else {
                        throw new PayrollDBException("Failed to retrieve updated data for employee: " + empName);
                    }
                }
            }

        } catch (Exception e) {
            throw new PayrollDBException("Error updating employee salary for " + empName, e);
        }
    }
}
