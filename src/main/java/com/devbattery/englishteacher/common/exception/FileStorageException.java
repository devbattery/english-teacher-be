package com.devbattery.englishteacher.common.exception;

public class FileStorageException extends CustomException {

    public FileStorageException() {
        super(ErrorCode.FILE_STORAGE_ERROR);
    }

}
