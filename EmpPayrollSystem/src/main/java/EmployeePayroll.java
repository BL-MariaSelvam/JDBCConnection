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

    public int getEmpId() {
		return empId;
	}

	public void setEmpId(int empId) {
		this.empId = empId;
	}

	public String getEmpName() {
		return empName;
	}

	public void setEmpName(String empName) {
		this.empName = empName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public double getNetPay() {
		return netPay;
	}

	public void setNetPay(double netPay) {
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

