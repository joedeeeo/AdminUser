package com.example.exam.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(GlobalException.class)
	@ResponseStatus(code = HttpStatus.LOCKED)
	public GlobalResponder handleGlobalException(GlobalException e)
	{
		return new GlobalResponder(e.getCode(), e.getMessage());
	}

}
