package com.emergya.siradmin.invest.util;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

public class WSResponseErrorHandler implements ResponseErrorHandler {

    private ResponseErrorHandler myErrorHandler = new DefaultResponseErrorHandler();

    public boolean hasError(ClientHttpResponse response) throws IOException {
        return myErrorHandler.hasError(response);
    }

    public void handleError(ClientHttpResponse response) throws IOException {
        String body = IOUtils.toString(response.getBody());
        WSCallException exception = new WSCallException(response.getStatusCode(), body, body);
        throw exception;
    }

}