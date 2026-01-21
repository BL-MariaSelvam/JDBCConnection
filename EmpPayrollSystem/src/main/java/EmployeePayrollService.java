package main.java;

import java.util.List;

public class EmployeePayrollService {

    private EmployeePayrollDBService dbService;

    // Constructor declares throws
    public EmployeePayrollService() throws PayrollDBException {
        this.dbService = EmployeePayrollDBService.getInstance();
    }

    public List<EmployeePayroll> getEmployeePayrollData() throws PayrollDBException {
        return dbService.readEmployeePayrollData();
    }
}
