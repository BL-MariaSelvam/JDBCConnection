package main.java;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeePayrollDBService {

    private static final String JDBC_URL =
            "jdbc:mysql://localhost:3306/employeepayrollsystem?useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public List<EmployeePayroll> readEmployeePayrollData()
            throws PayrollDBException {

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

                LocalDate startDate = rs.getDate("startDate") != null
                        ? rs.getDate("startDate").toLocalDate()
                        : null;

                double netPay = rs.getDouble("netPay");

                payrollList.add(
                        new EmployeePayroll(empId, empName, gender, startDate, netPay)
                );
            }

        } catch (Exception e) {
            throw new PayrollDBException("Failed to retrieve employee payroll data", e);
        }

        return payrollList;
    }
}

