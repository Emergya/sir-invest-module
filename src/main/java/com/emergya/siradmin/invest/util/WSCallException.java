package com.emergya.siradmin.invest.util;

import org.springframework.http.HttpStatus;

public class WSCallException extends RuntimeException {

	private static final long serialVersionUID = 6825887152567891013L;
	
	private HttpStatus statusCode;
    private String body;

    public WSCallException(String msg) {
        super(msg);
        // TODO Auto-generated constructor stub
    }

    public WSCallException(HttpStatus statusCode, String body, String msg) {
        super(msg);
        this.statusCode = statusCode;
        this.body = body;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
