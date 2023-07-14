package com.example.demo.util.exception;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * bipin : 2/6/22 : Created the class
 */

@ControllerAdvice
public class CustomControllerAdvice {

    @ExceptionHandler(CustomErrorException.class)
    public ResponseEntity<ErrorResponse> handleCustomErrorExceptions(Exception e){
        CustomErrorException customErrorException = (CustomErrorException) e;

        HttpStatus status = customErrorException.getStatus();

        // converts stack trace to String
        StringWriter stringWriter = new StringWriter();
        customErrorException.printStackTrace(new PrintWriter(stringWriter));
        String stackTrace = stringWriter.toString();

        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        customErrorException.getMessage(),
                        //stackTrace,
                        customErrorException.getData()
                ),
                status
        );
    }

    //BadCredentialsException.class
    @ExceptionHandler({NullPointerException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNullPointerExceptions(Exception e) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        e.getMessage()
                        //Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")),
                ),
                status
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleExceptions(Exception e) {

        //Since exceptions due to internal server errors, 500 is thrown
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        e.printStackTrace();
        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        e.getMessage()
                        //Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"))
                ),
                status
        );
    }

    @ExceptionHandler({NoHandlerFoundException.class, EntityNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNoHandlers(Exception e) {
        // When unknown url is found, exception is raised instead of default whitelabel error page
        // can be tuned by : spring.mvc.throw-exception-if-no-handler-found=true
        HttpStatus status = HttpStatus.NOT_FOUND;
        e.printStackTrace();
        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        e.getMessage()
                        //Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"))
                ),
                status
        );
    }

    @ExceptionHandler(value = {EntityExistsException.class, BadCredentialsException.class,
            MismatchedInputException.class, HttpMessageNotReadableException.class, HttpMediaTypeNotSupportedException.class })
    public ResponseEntity<ErrorResponse> invalidInput(Exception ex) {
        ErrorResponse response = new ErrorResponse();
        //response.setCode(HttpStatus.BAD_REQUEST.value());
        //response.setMessage(ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        ex.getMessage()
                        //Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"))
                ),
                status
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) ->{

            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        ex.getMessage(),
                        errors
                        //Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"))
                ),
                status
        );
    }
}
