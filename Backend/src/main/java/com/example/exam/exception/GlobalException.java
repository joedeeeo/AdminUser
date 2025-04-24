package com.example.exam.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GlobalException extends RuntimeException{/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer code;
	private String message;
	
}
