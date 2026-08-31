package com.project.roomloop.error;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.jsonwebtoken.security.SignatureException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handlerUsernameNotFound(UsernameNotFoundException ex){
        ApiError apiError = new ApiError("Username not FOund with this Username : "+ ex.getMessage(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(apiError, apiError.getStatusCode());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handlerUAuthenticationException(AuthenticationException ex){
        ApiError apiError = new ApiError("Authentication Failed : "+ ex.getMessage(), HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(apiError, apiError.getStatusCode());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handlerAccessDeniedException(AccessDeniedException ex){
        ApiError apiError = new ApiError("You dont have Acessd to This API : Insufficenit Permissions  : "+ ex.getMessage(), HttpStatus.FORBIDDEN);
        return new ResponseEntity<>(apiError, apiError.getStatusCode());
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiError> handlerJwtException(JwtException ex){
        ApiError apiError = new ApiError("Some Problem withs the JWT INvalid : "+ ex.getMessage(), HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(apiError, apiError.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handlerAllGenericExcetion(Exception ex){
        ApiError apiError = new ApiError("AN Unexpected Error  : "+ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(apiError,apiError.getStatusCode());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiError> handlerExpiredJwtException(ExpiredJwtException ex){
        ApiError apiError = new ApiError("You JWT TOken is Expire Login Again usig ID PASS : "+ex.getMessage(), HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(apiError,apiError.getStatusCode());
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ApiError> handleSignatureException(SignatureException ex){
        ApiError apiError = new ApiError("JWT signature does not match locally computed signature : "+ex.getMessage(), HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(apiError,apiError.getStatusCode());
    }

    @ExceptionHandler(ActiveMembershipException.class)
    public ResponseEntity<ApiError> handleActiveMembershipException(ActiveMembershipException ex){
        ApiError apiError = new ApiError("is Active Connection in User and Room : "+ex.getMessage(), HttpStatus.CONFLICT);
        return new ResponseEntity<>(apiError,apiError.getStatusCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiError apiError = new ApiError("Ther eis issur in the passing valid data : "+message,HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(apiError, apiError.getStatusCode());
    }
}
