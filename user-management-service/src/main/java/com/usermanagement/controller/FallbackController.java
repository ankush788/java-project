package com.usermanagement.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

// it for all  url which is invalid
@RestController
public class FallbackController {

    @RequestMapping(value = "/**", method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.PATCH,
            RequestMethod.OPTIONS,
            RequestMethod.HEAD
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    public void handleUnknownEndpoint() {
        throw new ResponseStatusException(NOT_FOUND, "API not exist");
    }
}
