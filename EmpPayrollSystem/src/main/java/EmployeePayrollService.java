package main.java;

import java.util.ArrayList;
import java.util.List;

public class EmployeePayrollService {

    private EmployeePayrollDBService dbService;
    private List<EmployeePayroll> employeePayrollList;

    public EmployeePayrollService() throws PayrollDBException {
        this.dbService = EmployeePayrollDBService.getInstance();
        this.employeePayrollList = new ArrayList<>();
        this.employeePayrollList = dbService.readEmployeePayrollData(); // only ACTIVE employees
    }

    // Read all active employees
    public List<EmployeePayroll> getEmployeePayrollData() throws PayrollDBException {
        employeePayrollList = dbService.readEmployeePayrollData();
        return employeePayrollList;
    }

    // Remove employee (Soft delete)
    public void removeEmployee(String empName) throws PayrollDBException {

        boolean removedFromDB = dbService.removeEmployeeFromPayroll(empName);

        if (removedFromDB) {
            employeePayrollList.removeIf(
                emp -> emp.getEmpName().equalsIgnoreCase(empName)
            );
        }
    }
}
