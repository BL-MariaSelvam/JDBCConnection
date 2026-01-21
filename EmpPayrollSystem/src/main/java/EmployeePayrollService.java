package main.java;

import java.util.List;

public class EmployeePayrollService {

    private EmployeePayrollDBService dbService;

    public EmployeePayrollService() {
        this.dbService = new EmployeePayrollDBService();
    }

    public List<EmployeePayroll> getEmployeePayrollData()
            throws PayrollDBException {
        return dbService.readEmployeePayrollData();
    }
}

