package com.renturapp.scansist;

/**
 * Created by Wayne on 10/08/2015.
 * Default template
 */
class RegisterScanSistTaskResultEvent {
    private final Boolean result;

    public RegisterScanSistTaskResultEvent(Boolean result) {
        this.result = result;
    }

    public Boolean getResult() {
        return result;
    }
}
