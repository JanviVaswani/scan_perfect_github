package com.renturapp.scansist;

import android.os.AsyncTask;

import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by Wayne on 08/08/2015.
 * Default Licence check
 */
class CheckLicenceTask extends AsyncTask<String, Void, Boolean> {

    //public AsyncResponse delegate = null;//Call back interface
    //public CheckLicenceTask(AsyncResponse asyncResponse) {
        //delegate = asyncResponse;//Assigning call back interface through constructor
    //}
    @Override
    protected Boolean doInBackground(String... urls) {
        Boolean result = false;
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con =
                    (HttpURLConnection) new URL(urls[0]).openConnection();
          //http://stackoverflow.com/questions/17638398/androids-httpurlconnection-throws-eofexception-on-head-requests
            con.setRequestProperty( "Accept-Encoding", "" );
            con.setRequestMethod("HEAD");
            result = (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {e.printStackTrace();
            e.printStackTrace();
        }
        return result;
    }
    @Override
    protected void onPostExecute(Boolean result) {
        //delegate.processFinish(result);
        MyAsyncBus.getInstance().post(new CheckLicenceTaskResultEvent(result));
    }
}

