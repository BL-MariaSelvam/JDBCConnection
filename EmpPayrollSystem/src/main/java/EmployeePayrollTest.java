package main.java;

import org.junit.jupiter.api.Test;

public class EmployeePayrollTest {

    public static void main(String[] args) {

       

        try {
        	 EmployeePayrollService service = new EmployeePayrollService();
            service.getEmployeePayrollData()
                   .forEach(System.out::println);

        } catch (PayrollDBException e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        try {
//        EmployeePayrollDBService dbService = new EmployeePayrollDBService().;
        EmployeePayrollDBService dbService = EmployeePayrollDBService.getInstance();

        // Retrieve Terisa payroll
        dbService.getEmployeePayrollByName("Maria")
                 .forEach(System.out::println);

        // Close resources at the end
        dbService.close();

    } catch (PayrollDBException e) {
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
    }
        try {
        	 EmployeePayrollDBService dbService = EmployeePayrollDBService.getInstance();

            EmployeePayroll updatedEmp = dbService.updateEmployeeSalary("Peter", 3000000.00);
            System.out.println("Updated Employee Payroll: " + updatedEmp);

        } catch (PayrollDBException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

