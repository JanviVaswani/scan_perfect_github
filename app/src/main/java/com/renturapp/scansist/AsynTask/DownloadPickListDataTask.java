package com.renturapp.scansist.AsynTask;

import android.os.AsyncTask;

import com.renturapp.scansist.CustomHttpClient;
import com.renturapp.scansist.AsynTask.DownloadPickListDataTaskResultEvent;
import com.renturapp.scansist.MyAsyncBus;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.InputStream;

/**
 * Created by wayne on 30/09/16.
 * This is used to download data from movesist.com
 */

public class DownloadPickListDataTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        StringBuilder response = null;
        String inputLine;

        HttpClient httpClient = new CustomHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpGet httpGet = new HttpGet(params[0]);

        try {
            HttpResponse httpresponse = httpClient.execute(httpGet, localContext);
            HttpEntity entity = httpresponse.getEntity();

            InputStream is = entity.getContent();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            response = new StringBuilder();

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();

        } catch (Exception e) {
            return e.getLocalizedMessage();
        }

        if (response != null) {
            return response.toString();
        } else {
            return "";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        MyAsyncBus.getInstance().post(new DownloadPickListDataTaskResultEvent(result));
    }
}
