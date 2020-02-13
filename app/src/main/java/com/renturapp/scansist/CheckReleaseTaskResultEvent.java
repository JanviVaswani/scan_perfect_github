package com.renturapp.scansist;

/**
 * Created by Wayne on 10/08/2015.
 * Default template
 */
class CheckReleaseTaskResultEvent {
    private final String result;

    public CheckReleaseTaskResultEvent(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }
}