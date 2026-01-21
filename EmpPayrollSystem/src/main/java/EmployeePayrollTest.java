package main.java;

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
    }
}

