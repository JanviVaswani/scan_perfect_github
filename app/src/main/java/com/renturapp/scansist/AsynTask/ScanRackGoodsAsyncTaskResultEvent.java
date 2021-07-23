package com.renturapp.scansist.AsynTask;

/**
 * Created by Wayne on 10/08/2015.
 */
public class ScanRackGoodsAsyncTaskResultEvent {
    private final String result;

    public ScanRackGoodsAsyncTaskResultEvent(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }
}
