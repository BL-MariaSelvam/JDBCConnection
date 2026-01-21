package main.java;

import org.junit.jupiter.api.Test;

public class EmployeePayrollTest {

    public static void main(String[] args) {

        EmployeePayrollService service = new EmployeePayrollService();

        try {
            service.getEmployeePayrollData()
                   .forEach(System.out::println);

        } catch (PayrollDBException e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        EmployeePayrollDBService dbService = new EmployeePayrollDBService();
        try {
            EmployeePayroll updatedEmp = dbService.updateEmployeeSalary("Peter", 3000000.00);
            System.out.println("Updated Employee Payroll: " + updatedEmp);

        } catch (PayrollDBException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

