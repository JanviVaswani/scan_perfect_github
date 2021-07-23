package com.renturapp.scansist;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by wayne on 30/09/16.
 * This is used to download data from movesist.com
 */

class DownloadDataTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {

        URL website;
        StringBuilder response = null;
        try {

            HttpClient httpclient = new CustomHttpClient();
            HttpGet httpget = new HttpGet(params[0]);

            HttpResponse httpResponse = httpclient.execute(httpget);
            HttpEntity entity = httpResponse.getEntity();
            InputStream is = entity.getContent();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            is));
            response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response != null ? response.toString() : null;

    }

    @Override
    protected void onPostExecute(String result) {
        MyAsyncBus.getInstance().post(new DownloadDataTaskResultEvent(result));
    }
}