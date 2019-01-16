package com.renturapp.scansist;

/**
 * Created by Wayne on 10/08/2015.
 * Default template
 */
class CheckReleaseTaskResultEvent {
    private final Long result;

    public CheckReleaseTaskResultEvent(Long result) {
        this.result = result;
    }

    public Long getResult() {
        return result;
    }
}
