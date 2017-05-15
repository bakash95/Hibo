package com.journaldev.spring.aspect;

public class EmployeeXMLConfigAspect {

	public void employeeAroundAdvice(){
		System.out.println("EmployeeXMLConfigAspect:: Before invoking getName() method");
		System.out.println("EmployeeXMLConfigAspect:: After invoking getName() method. Return value");
	}
}