package com.renturapp.scansist;

import android.os.AsyncTask;

import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by Wayne on 08/08/2015.
 * Default Licence check
 */
class CheckReleaseTask extends AsyncTask<String, Void, Long> {

    @Override
    protected Long doInBackground(String... urls) {
        long result = -1;
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con =
                    (HttpURLConnection) new URL(urls[0]).openConnection();
          //http://stackoverflow.com/questions/17638398/androids-httpurlconnection-throws-eofexception-on-head-requests
            con.setRequestProperty( "Accept-Encoding", "" );
            con.setRequestMethod("HEAD");
            result = (con.getResponseCode() == HttpURLConnection.HTTP_OK?con.getLastModified():-1);
        } catch (Exception e) {e.printStackTrace();
            e.printStackTrace();
        }
        return result;
    }
    @Override
    protected void onPostExecute(Long result) {
        //delegate.processFinish(result);
        MyAsyncBus.getInstance().post(new CheckReleaseTaskResultEvent(result));
    }
}

