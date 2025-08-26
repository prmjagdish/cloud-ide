package com.cloud_ide.file_service.exception;

public class FileServiceException extends RuntimeException {

    public FileServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileServiceException(String message) {
        super(message);
    }
}
