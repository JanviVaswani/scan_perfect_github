package com.renturapp.scansist;

import com.squareup.otto.Bus;

/**
 * Created by Wayne on 10/08/2015.
 * Default template
 */
public class MyAsyncBus {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }
}
