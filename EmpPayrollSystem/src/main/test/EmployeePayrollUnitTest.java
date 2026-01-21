package main.test;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import main.java.EmployeePayroll;
import main.java.EmployeePayrollDBService;
import main.java.EmployeePayrollService;
import main.java.PayrollDBException;

public class EmployeePayrollUnitTest {
	EmployeePayrollDBService dbService = null;

    @Test
    void testUpdateEmployeeSalary() throws PayrollDBException {
    	dbService=EmployeePayrollDBService.getInstance();
        double newSalary = 3000000.00;
        EmployeePayroll emp = dbService.updateEmployeeSalary("Peter", newSalary);
        assertEquals(newSalary, emp.getNetPay(), 0.0);
    }
    
    @Test
    public void givenEmployee_WhenRemoved_ShouldNotAppearInActiveList() throws PayrollDBException {

        EmployeePayrollService service = new EmployeePayrollService();

        service.removeEmployee("Rinit");

        List<EmployeePayroll> list = service.getEmployeePayrollData();

        boolean exists = list.stream()
                             .anyMatch(emp -> emp.getEmpName().equals("Rinit"));

        assertFalse(exists);
    }

}
