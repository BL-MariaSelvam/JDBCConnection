package main.java;

import java.time.LocalDate;

public class EmployeePayroll {

    private int empId;
    private String empName;
    private String gender;
    private LocalDate startDate;
    private double netPay;

    // Old constructor (backward compatibility)
    public EmployeePayroll(int empId, String empName, double netPay) {
        this(empId, empName, null, null, netPay);
    }

    // New constructor
    public EmployeePayroll(int empId, String empName,
                           String gender, LocalDate startDate,
                           double netPay) {
        this.empId = empId;
        this.empName = empName;
        this.gender = gender;
        this.startDate = startDate;
        this.netPay = netPay;
    }

    @Override
    public String toString() {
        return "EmployeePayroll{" +
                "empId=" + empId +
                ", empName='" + empName + '\'' +
                ", gender='" + gender + '\'' +
                ", startDate=" + startDate +
                ", netPay=" + netPay +
                '}';
    }
}

