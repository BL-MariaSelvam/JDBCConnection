package main.java;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

public class JDBCConnection {
	public static void main(String args[]) {
		String jdbcDiver="jdbc:mysql://localhost:3306/employeepayrollsystem?useSSL=false";
		String username="root";
		String password="root";
		Connection con;
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println("Driver Loaded");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Connecting to drivers");
		try {
			con=DriverManager.getConnection(jdbcDiver,username,password);
			System.out.println("Connection successful");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		listDrivers();
		
		

	}

	private static void listDrivers() {
		Enumeration<Driver> driverList=DriverManager.getDrivers();
		while(driverList.hasMoreElements()) {
			Driver dive=(Driver) driverList.nextElement();
			System.out.println(" "+dive.getClass().getName());
		}
		
	}

}
