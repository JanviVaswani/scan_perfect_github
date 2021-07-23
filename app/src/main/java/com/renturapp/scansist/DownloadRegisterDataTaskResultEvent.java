package com.renturapp.scansist;

class DownloadRegisterDataTaskResultEvent {

    private final String result;

    public DownloadRegisterDataTaskResultEvent(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }
}
