package com.example.practiceApps.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    record ErrorResponse(int status, String message, long timestamp){}

    @ExceptionHandler(PositionNotFound.class)
    public ResponseEntity<ErrorResponse> handlePositionNotFound(PositionNotFound ex){
        return new ResponseEntity<>((new ErrorResponse(HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                System.currentTimeMillis())),HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotEnoughQtyToSell.class)
    public ResponseEntity<ErrorResponse> handleNotEnoughQtyToSell(NotEnoughQtyToSell ex){
        return new ResponseEntity<>((new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getMessage(),
                System.currentTimeMillis())),HttpStatus.UNPROCESSABLE_ENTITY); //422 or 409 Conflict
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEverythingElse(Exception ex){
        return new ResponseEntity<>((new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                System.currentTimeMillis())),HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
