package com.bbva.webdav.drools;

import org.apache.commons.httpclient.HttpMethod;

public class GuvnorRepositoryException extends Exception {

    private static final long serialVersionUID = 1L;
    private String message = null;
    
    @Override
    public String getMessage() {
        return message != null ? message : super.getMessage();
    }

    public GuvnorRepositoryException() {
        super();
    }

    public GuvnorRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public GuvnorRepositoryException(String message) {
        super(message);
    }

    public GuvnorRepositoryException(Throwable cause) {
        super(cause);
    }

    public GuvnorRepositoryException(HttpMethod method, int expectedStatus) {
        super();
        message = method.getStatusCode() + ":" + method.getStatusText();
    }

}
