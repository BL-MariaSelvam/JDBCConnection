package main.java;

import java.time.LocalDate;
import java.util.List;

public class EmployeePayrollTest {

    public static void main(String[] args) {

        EmployeePayrollService service = null;
        EmployeePayrollDBService dbService = null;

        try {
            // 1️⃣ Read all employee payroll data
            service = new EmployeePayrollService();
            service.getEmployeePayrollData()
                   .forEach(System.out::println);

        } catch (PayrollDBException e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            // 2️⃣ Get singleton instance of DB service
            dbService = EmployeePayrollDBService.getInstance();

            // Retrieve payroll for specific employee
            dbService.getEmployeePayrollByName("Maria")
                     .forEach(System.out::println);

            // 3️⃣ Update salary for an employee
            EmployeePayroll updatedEmp = dbService.updateEmployeeSalary("Peter", 3000000.00);
            System.out.println("Updated Employee Payroll: " + updatedEmp);

            // 4️⃣ Retrieve employees in a specific date range
            LocalDate start = LocalDate.of(2023, 1, 1);
            LocalDate end = LocalDate.of(2023, 12, 31);
            List<EmployeePayroll> employees = dbService.getEmployeesByDateRange(start, end);

            employees.forEach(System.out::println);

        } catch (PayrollDBException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close resources at the very end
            if (dbService != null) dbService.close();
        }

    }
}
