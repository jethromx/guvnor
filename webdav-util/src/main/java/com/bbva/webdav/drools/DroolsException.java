package com.bbva.webdav.drools;

public class DroolsException extends Exception {

    private static final long serialVersionUID = 1L;

    public DroolsException() {
        super();
    }

    public DroolsException(String message, Throwable cause) {
        super(message, cause);
    }

    public DroolsException(String message) {
        super(message);
    }

    public DroolsException(Throwable cause) {
        super(cause);
    }

}
