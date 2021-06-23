package com.renturapp.scansist;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by wayne on 30/09/16.
 * This is used to download data from movesist.com
 */

class DownloadRackDataTask extends AsyncTask<String, Void, String> {
/*public AsyncResponse delegate = null;//Call back interface
    public FTPFileUploadTask(AsyncResponse asyncResponse) {
        delegate = asyncResponse;//Assigning call back interface through constructor
    }*/

  @Override
  protected String doInBackground(String... params) {

    URL website;
    StringBuilder response = null;
    try {
      website = new URL(params[0]);

      HttpURLConnection connection = (HttpURLConnection) website.openConnection();
      connection.setRequestProperty("charset", "utf-8");
      BufferedReader in = new BufferedReader(
          new InputStreamReader(
              connection.getInputStream()));
      response = new StringBuilder();
      String inputLine;

      while ((inputLine = in.readLine()) != null)
        response.append(inputLine);
      in.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (response != null) {
      return response.toString();
    } else {
      return "";
    }
  }

  @Override
  protected void onPostExecute(String result) {
    //delegate.processFinish(result);
    MyAsyncBus.getInstance().post(new DownloadRackDataTaskResultEvent(result));
  }
}
