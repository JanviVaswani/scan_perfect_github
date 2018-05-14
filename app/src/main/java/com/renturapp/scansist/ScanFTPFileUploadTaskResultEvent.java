package com.renturapp.scansist;

/**
 * Created by Wayne on 10/08/2015.
 */
class ScanFTPFileUploadTaskResultEvent {
    private final Boolean result;

    public ScanFTPFileUploadTaskResultEvent(Boolean result) {
        this.result = result;
    }

    public Boolean getResult() {
        return result;
    }
}
