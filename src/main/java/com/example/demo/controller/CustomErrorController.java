package com.example.demo.controller;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

public class CustomErrorController implements ErrorController {


    public String handleError(HttpServletRequest request) {
        Object errorStatus = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (errorStatus != null) {
            Integer statusCode = Integer.valueOf(errorStatus.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "err-response-400";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "err-response-500";
            }
        }
        return "error";
    }

}

