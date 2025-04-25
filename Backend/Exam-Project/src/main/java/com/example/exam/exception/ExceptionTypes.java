package com.example.exam.exception;

import lombok.Getter;

@Getter
public enum ExceptionTypes {
	
	NO_SUCH_USER_EXISTS(1, "No Such User Exists"),
	INCORRECT_PASSWORD(2, "Password is Incorrect"),
	FORBIDDEN_ADMIN_DELETION(3, "Admin Can't Be Deleted");
	
	int statusCode;
	String message;
	
	private ExceptionTypes(int code, String msg)
	{
		this.statusCode = code;
		this.message = msg;
	}
}
