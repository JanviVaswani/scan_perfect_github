package com.renturapp.scansist.AsynTask;

import android.os.AsyncTask;

import com.renturapp.scansist.MyAsyncBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by wayne on 15/10/16.
 * This is used to download data from movesist.com
 */

public class ScanRackGoodsAsyncTask extends AsyncTask<String, String, String> {
    HttpURLConnection conn;
    URL url = null;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String jsonUrl =strings[0];
            String jsonGetString;

            URL url = new URL(jsonUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setInstanceFollowRedirects(true);
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer stringBuffer = new StringBuffer();
            while ((jsonGetString = bufferedReader.readLine()) != null){
                stringBuffer.append(jsonGetString + "\n");
            }

            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();
            return stringBuffer.toString().trim();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        MyAsyncBus.getInstance().post(new ScanRackGoodsAsyncTaskResultEvent(result));
    }
}