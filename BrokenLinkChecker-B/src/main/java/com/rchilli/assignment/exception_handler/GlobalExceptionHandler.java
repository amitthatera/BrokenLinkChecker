package com.rchilli.assignment.exception_handler;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.rchilli.assignment.dto.ExceptionResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MalformedURLException.class)
	public ResponseEntity<ExceptionResponse> handleMalformedUrlException(MalformedURLException e){
		return new ResponseEntity<ExceptionResponse>(new ExceptionResponse(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ExceptionResponse> handleIllegalArgumentException(IllegalArgumentException e){
		return new ResponseEntity<ExceptionResponse>(new ExceptionResponse(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(SocketTimeoutException.class)
	public ResponseEntity<ExceptionResponse> handleSocketTimeoutException(SocketTimeoutException e){
		return new ResponseEntity<ExceptionResponse>(new ExceptionResponse(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
	}
}
