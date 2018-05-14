package com.renturapp.scansist;

/**
 * Created by Wayne on 10/08/2015.
 */
class FTPFileUploadTaskResultEvent {
    private final Boolean result;

    public FTPFileUploadTaskResultEvent(Boolean result) {
        this.result = result;
    }

    public Boolean getResult() {
        return result;
    }
}
