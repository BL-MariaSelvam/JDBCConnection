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
    
    public EmployeePayroll addEmployeeWithPayrollDetails(EmployeePayroll newEmp) throws PayrollDBException {
        String insertEmployeeSQL = "INSERT INTO employee (empName, gender, startDate) VALUES (?, ?, ?)";
        String insertPayrollDetailsSQL = "INSERT INTO payroll (empId, basicPay, deductions, taxablePay, incomeTax, netPay) VALUES (?, ?, ?, ?, ?, ?)";
        String insertPayrollSQL = "INSERT INTO payroll (empId, netPay) VALUES (?, ?)"; // For backward compatibility

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
            conn.setAutoCommit(false); // Start transaction

            int generatedEmpId = -1;

            // 1️⃣ Insert employee
            try (PreparedStatement empStmt = conn.prepareStatement(insertEmployeeSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                empStmt.setString(1, newEmp.getEmpName());
                empStmt.setString(2, newEmp.getGender());
                empStmt.setDate(3, newEmp.getStartDate() != null ? Date.valueOf(newEmp.getStartDate()) : null);

                int rows = empStmt.executeUpdate();
                if (rows == 0) throw new PayrollDBException("Failed to insert employee: " + newEmp.getEmpName());

                try (ResultSet rs = empStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedEmpId = rs.getInt(1);
                    } else {
                        throw new PayrollDBException("Failed to retrieve employee ID after insert");
                    }
                }
            }

            // 2️⃣ Calculate derived payroll fields
            double basicPay = newEmp.getNetPay(); // Assuming netPay passed in object is the basic salary
            double deductions = basicPay * 0.2;
            double taxablePay = basicPay - deductions;
            double incomeTax = taxablePay * 0.1;
            double netPay = basicPay - incomeTax;

            // 3️⃣ Insert into payroll_details
            try (PreparedStatement payrollDetailsStmt = conn.prepareStatement(insertPayrollDetailsSQL)) {
                payrollDetailsStmt.setInt(1, generatedEmpId);
                payrollDetailsStmt.setDouble(2, basicPay);
                payrollDetailsStmt.setDouble(3, deductions);
                payrollDetailsStmt.setDouble(4, taxablePay);
                payrollDetailsStmt.setDouble(5, incomeTax);
                payrollDetailsStmt.setDouble(6, netPay);

                int rows = payrollDetailsStmt.executeUpdate();
                if (rows == 0) throw new PayrollDBException("Failed to insert payroll details for employee: " + newEmp.getEmpName());
            }

            // 4️⃣ Insert into payroll table (backward compatibility)
            try (PreparedStatement payrollStmt = conn.prepareStatement(insertPayrollSQL)) {
                payrollStmt.setInt(1, generatedEmpId);
                payrollStmt.setDouble(2, netPay);

                int rows = payrollStmt.executeUpdate();
                if (rows == 0) throw new PayrollDBException("Failed to insert payroll for employee: " + newEmp.getEmpName());
            }

            conn.commit(); // ✅ commit transaction

            // 5️⃣ Return updated EmployeePayroll object with calculated netPay
            return new EmployeePayroll(generatedEmpId, newEmp.getEmpName(), newEmp.getGender(), newEmp.getStartDate(), netPay);

        } catch (SQLException e) {
            throw new PayrollDBException("Error adding employee with payroll details: " + newEmp.getEmpName(), e);
        }
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
    
    public EmployeePayroll addEmployee(EmployeePayroll newEmp) throws PayrollDBException {
        // Insert SQL for employee table
        String insertEmployeeSQL = "INSERT INTO employee (empName, gender, startDate) VALUES (?, ?, ?)";
        // Insert SQL for payroll table
        String insertPayrollSQL = "INSERT INTO payroll (empId, netPay) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
            conn.setAutoCommit(false); // Start transaction

            int generatedEmpId = -1;

            // 1️⃣ Insert into employee table
            try (PreparedStatement empStmt = conn.prepareStatement(insertEmployeeSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                empStmt.setString(1, newEmp.getEmpName());
                empStmt.setString(2, newEmp.getGender());
                empStmt.setDate(3, newEmp.getStartDate() != null ? Date.valueOf(newEmp.getStartDate()) : null);

                int rows = empStmt.executeUpdate();
                if (rows == 0) {
                    throw new PayrollDBException("Failed to insert employee: " + newEmp.getEmpName());
                }

                // Get generated empId
                try (ResultSet rs = empStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedEmpId = rs.getInt(1);
                    } else {
                        throw new PayrollDBException("Failed to retrieve employee ID after insert");
                    }
                }
            }

            // 2️⃣ Insert into payroll table
            try (PreparedStatement payrollStmt = conn.prepareStatement(insertPayrollSQL)) {
                payrollStmt.setInt(1, generatedEmpId);
                payrollStmt.setDouble(2, newEmp.getNetPay());

                int rows = payrollStmt.executeUpdate();
                if (rows == 0) {
                    throw new PayrollDBException("Failed to insert payroll for employee: " + newEmp.getEmpName());
                }
            }

            // 3️⃣ Commit transaction
            conn.commit();

            // 4️⃣ Return EmployeePayroll object with generated empId
            return new EmployeePayroll(generatedEmpId, newEmp.getEmpName(), newEmp.getGender(), newEmp.getStartDate(), newEmp.getNetPay());

        } catch (SQLException e) {
            throw new PayrollDBException("Error adding new employee: " + newEmp.getEmpName(), e);
        }
    }

    
    public EmployeePayroll addEmployeePayroll(EmployeePayroll emp)
            throws PayrollDBException {

        String insertEmployeeSQL =
                "INSERT INTO employee (empName, gender, startDate) VALUES (?, ?, ?)";

        String insertAddressSQL =
                "INSERT INTO address (empId, street, city, state, zip) VALUES (?, ?, ?, ?, ?)";

        String insertDeptSQL =
                "INSERT INTO employee_department (empId, deptId) VALUES (?, ?)";

        String insertPayrollSQL =
                "INSERT INTO payroll (empId, basicPay, deductions, taxablePay, incomeTax, netPay) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
            conn.setAutoCommit(false); // 🔴 START TRANSACTION

            int empId;

            // 1️⃣ Insert Employee
            try (PreparedStatement empStmt =
                         conn.prepareStatement(insertEmployeeSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {

                empStmt.setString(1, emp.getEmpName());
                empStmt.setString(2, emp.getGender());
                empStmt.setDate(3, Date.valueOf(emp.getStartDate()));
                empStmt.executeUpdate();

                ResultSet rs = empStmt.getGeneratedKeys();
                if (!rs.next()) {
                    throw new PayrollDBException("Employee ID generation failed");
                }
                empId = rs.getInt(1);
            }

            // 2️⃣ Insert Address
            try (PreparedStatement addrStmt = conn.prepareStatement(insertAddressSQL)) {
                addrStmt.setInt(1, empId);
                addrStmt.setString(2, emp.getStreet());
                addrStmt.setString(3, emp.getCity());
                addrStmt.setString(4, emp.getState());
                addrStmt.setString(5, emp.getZip());
                addrStmt.executeUpdate();
            }

            // 3️⃣ Insert Departments
            for (String deptName : emp.getDepartments()) {
                int deptId = getDepartmentId(deptName, conn);
                try (PreparedStatement deptStmt = conn.prepareStatement(insertDeptSQL)) {
                    deptStmt.setInt(1, empId);
                    deptStmt.setInt(2, deptId);
                    deptStmt.executeUpdate();
                }
            }

            // 4️⃣ Payroll Calculations
            double basicPay = emp.getBasicPay();
            double deductions = basicPay * 0.20;
            double taxablePay = basicPay - deductions;
            double incomeTax = taxablePay * 0.10;
            double netPay = basicPay - incomeTax;

            // 5️⃣ Insert Payroll
            try (PreparedStatement payrollStmt = conn.prepareStatement(insertPayrollSQL)) {
                payrollStmt.setInt(1, empId);
                payrollStmt.setDouble(2, basicPay);
                payrollStmt.setDouble(3, deductions);
                payrollStmt.setDouble(4, taxablePay);
                payrollStmt.setDouble(5, incomeTax);
                payrollStmt.setDouble(6, netPay);
                payrollStmt.executeUpdate();
            }

            conn.commit(); // ✅ TRANSACTION SUCCESS

            // 🔵 Update object ONLY after successful commit
            emp.setEmpId(empId);
            emp.setDeductions(deductions);
            emp.setTaxablePay(taxablePay);
            emp.setIncomeTax(incomeTax);
            emp.setNetPay(netPay);

            return emp;

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback(); // 🔁 ROLLBACK
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new PayrollDBException("Failed to add employee payroll", e);

        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean removeEmployeeFromPayroll(String empName)
            throws PayrollDBException {

        String sql = "UPDATE employee SET is_active = FALSE WHERE empName = ?";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, empName);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new PayrollDBException("Employee not found: " + empName);
            }
            return true;

        } catch (Exception e) {
            throw new PayrollDBException("Failed to remove employee: " + empName, e);
        }
    }

    private int getDepartmentId(String deptName, Connection conn)
            throws SQLException {

        String selectSQL = "SELECT deptId FROM department WHERE deptName = ?";
        String insertSQL = "INSERT INTO department (deptName) VALUES (?)";

        try (PreparedStatement stmt = conn.prepareStatement(selectSQL)) {
            stmt.setString(1, deptName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("deptId");
        }

        try (PreparedStatement stmt =
                     conn.prepareStatement(insertSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, deptName);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
    }


			public void getSalaryStatisticsByGender() throws PayrollDBException {
                String sql = "SELECT gender, " +
                             "SUM(netPay) AS totalSalary, " +
                             "AVG(netPay) AS averageSalary, " +
                             "MIN(netPay) AS minSalary, " +
                             "MAX(netPay) AS maxSalary, " +
                             "COUNT(*) AS employeeCount " +
                             "FROM employee e " +
                             "JOIN payroll p ON e.empId = p.empId " +
                             "GROUP BY gender";

                try (PreparedStatement stmt = connection.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    System.out.println("Gender-wise Salary Statistics:");
                    while (rs.next()) {
                        String gender = rs.getString("gender");
                        double sum = rs.getDouble("totalSalary");
                        double avg = rs.getDouble("averageSalary");
                        double min = rs.getDouble("minSalary");
                        double max = rs.getDouble("maxSalary");
                        int count = rs.getInt("employeeCount");

                        System.out.println("Gender: " + gender);
                        System.out.println("  Total Salary: " + sum);
                        System.out.println("  Average Salary: " + avg);
                        System.out.println("  Min Salary: " + min);
                        System.out.println("  Max Salary: " + max);
                        System.out.println("  Number of Employees: " + count);
                        System.out.println("------------------------------");
                    }

                } catch (SQLException e) {
                    throw new PayrollDBException("Error retrieving salary statistics by gender", e);
                }
            }

    }

