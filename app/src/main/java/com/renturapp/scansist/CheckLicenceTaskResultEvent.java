package com.renturapp.scansist;

/**
 * Created by Wayne on 10/08/2015.
 * Default template
 */
class CheckLicenceTaskResultEvent {
    private final Boolean result;

    public CheckLicenceTaskResultEvent(Boolean result) {
        this.result = result;
    }

    public Boolean getResult() {
        return result;
    }
}
