package com.renturapp.scansist;

/**
 * Created by wayne on 30/09/16.
 * This is used to download data from movesist.com
 */

class DownloadPickListDataTaskResultEvent {

  private final String result;

  public DownloadPickListDataTaskResultEvent(String result) {
    this.result = result;
  }

  public String getResult() {
    return result;
  }

}
