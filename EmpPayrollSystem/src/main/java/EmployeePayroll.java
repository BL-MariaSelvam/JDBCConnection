package main.java;

import java.time.LocalDate;
import java.util.List;

public class EmployeePayroll {

    // ===== Employee =====
    private int empId;
    private String empName;
    private String gender;
    private LocalDate startDate;

    // ===== Address =====
    private String street;
    private String city;
    private String state;
    private String zip;

    // ===== Phone (Multi-valued) =====
    private List<String> phoneNumbers;

    // ===== Department (Many-to-Many) =====
    private String[] departments;

    // ===== Payroll =====
    private double basicPay;
    private double deductions;
    private double taxablePay;
    private double incomeTax;
    private double netPay;

    /* ------------------------------------------------
       BACKWARD COMPATIBILITY CONSTRUCTOR (UCs earlier)
       ------------------------------------------------ */
    public EmployeePayroll(int empId, String empName, double netPay) {
        this.empId = empId;
        this.empName = empName;
        this.netPay = netPay;
    }

    /* ------------------------------------------------
       CURRENT CONSTRUCTOR (Used by JDBC Reads)
       ------------------------------------------------ */
    public EmployeePayroll(int empId, String empName,
                           String gender, LocalDate startDate,
                           double netPay) {
        this.empId = empId;
        this.empName = empName;
        this.gender = gender;
        this.startDate = startDate;
        this.netPay = netPay;
    }

    /* ------------------------------------------------
       FULL ER-BASED CONSTRUCTOR (Insert Transaction)
       ------------------------------------------------ */
    public EmployeePayroll(String empName, String gender,
                           LocalDate startDate, double basicPay,
                           String street, String city, String state, String zip,
                           List<String> phoneNumbers,
                           String[] departments) {

        this.empName = empName;
        this.gender = gender;
        this.startDate = startDate;
        this.basicPay = basicPay;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.phoneNumbers = phoneNumbers;
        this.departments = departments;

        // Derived payroll fields
        this.deductions = basicPay * 0.20;
        this.taxablePay = basicPay - deductions;
        this.incomeTax = taxablePay * 0.10;
        this.netPay = basicPay - incomeTax;
    }

    // ===== Getters & Setters =====

    public int getEmpId() { return empId; }
    public void setEmpId(int empId) { this.empId = empId; }

    public String getEmpName() { return empName; }
    public void setEmpName(String empName) { this.empName = empName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZip() { return zip; }

    public List<String> getPhoneNumbers() { return phoneNumbers; }
    public String[] getDepartments() { return departments; }

    public double getBasicPay() { return basicPay; }
    public double getDeductions() { return deductions; }
    public double getTaxablePay() { return taxablePay; }
    public double getIncomeTax() { return incomeTax; }

    public double getNetPay() { return netPay; }
    public void setNetPay(double netPay) { this.netPay = netPay; }

    public void setStreet(String street) {
		this.street = street;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public void setPhoneNumbers(List<String> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public void setDepartments(String[] departments) {
		this.departments = departments;
	}

	public void setDeductions(double deductions) {
		this.deductions = deductions;
	}

	public void setTaxablePay(double taxablePay) {
		this.taxablePay = taxablePay;
	}

	public void setIncomeTax(double incomeTax) {
		this.incomeTax = incomeTax;
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
