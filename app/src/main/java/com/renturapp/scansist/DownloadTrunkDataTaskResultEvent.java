package com.renturapp.scansist;

/**
 * Created by wayne on 30/09/16.
 * This is used to download data from movesist.com
 */

class DownloadTrunkDataTaskResultEvent {

  private final String result;

  public DownloadTrunkDataTaskResultEvent(String result) {
    this.result = result;
  }

  public String getResult() {
    return result;
  }

}
