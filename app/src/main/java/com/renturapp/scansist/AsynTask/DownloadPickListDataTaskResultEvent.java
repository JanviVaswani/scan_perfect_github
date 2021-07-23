package com.renturapp.scansist.AsynTask;

/**
 * Created by wayne on 30/09/16.
 * This is used to download data from movesist.com
 */

public class DownloadPickListDataTaskResultEvent {

    private final String result;

    public DownloadPickListDataTaskResultEvent(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

}
