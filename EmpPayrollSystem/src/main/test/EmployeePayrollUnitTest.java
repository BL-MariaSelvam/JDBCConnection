package main.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import main.java.EmployeePayroll;
import main.java.EmployeePayrollDBService;
import main.java.PayrollDBException;

public class EmployeePayrollUnitTest {
	EmployeePayrollDBService dbService = new EmployeePayrollDBService();

    @Test
    void testUpdateEmployeeSalary() throws PayrollDBException {
        double newSalary = 3000000.00;
        EmployeePayroll emp = dbService.updateEmployeeSalary("Peter", newSalary);
        assertEquals(newSalary, emp.getNetPay(), 0.0);
    }
}
