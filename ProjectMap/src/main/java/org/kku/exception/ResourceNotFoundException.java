package org.kku.exception;

/**
 * Custom Exception สำหรับเมื่อไม่พบทรัพยากร
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
