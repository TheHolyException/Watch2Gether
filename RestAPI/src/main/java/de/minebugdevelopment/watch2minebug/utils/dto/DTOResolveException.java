package de.minebugdevelopment.watch2minebug.utils.dto;

public class DTOResolveException extends RuntimeException {

    DTOResolveException(String message) {
        super(message);
    }

    DTOResolveException(Throwable throwable) {
        super(throwable);
    }

}
