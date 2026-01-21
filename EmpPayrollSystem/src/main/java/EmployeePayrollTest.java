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
            dbService.getSalaryStatisticsByGender();
            
            // Create new employee object
            EmployeePayroll newEmp = new EmployeePayroll(0, "Terissa", "F", LocalDate.of(2023, 1, 15), 5000000.00);

            // Add employee to DB and get updated object with empId
            EmployeePayroll addedEmp = dbService.addEmployee(newEmp);

            System.out.println("Added Employee Payroll: " + addedEmp);

            // Optional: Retrieve from DB to verify
            dbService.getEmployeePayrollByName("Terissa")
                     .forEach(System.out::println);

            
        } catch (PayrollDBException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close resources at the very end
            if (dbService != null) dbService.close();
        }

    }
}
