package com.example.exam.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GlobalResponder {
	
	private Integer code;
	private String message;

}
