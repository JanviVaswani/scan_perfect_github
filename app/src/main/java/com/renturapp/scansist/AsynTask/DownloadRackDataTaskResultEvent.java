package com.renturapp.scansist.AsynTask;

/**
 * Created by wayne on 30/09/16.
 * This is used to download data from movesist.com
 */

public class DownloadRackDataTaskResultEvent {

    private final String result;

    public DownloadRackDataTaskResultEvent(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

}
