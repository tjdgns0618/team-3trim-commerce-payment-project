package com.example.team3trimcommercepaymentproject.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Void> handleBusinessException(BusinessException e) {
		ErrorCode code = e.getErrorCode();
		return ResponseEntity.status(code.getStatus()).build();
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Void> handleValidation(MethodArgumentNotValidException e) {
		return ResponseEntity.badRequest().build();
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Void> handleException(Exception e) {
		log.error("서버 오류 발생", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}
}