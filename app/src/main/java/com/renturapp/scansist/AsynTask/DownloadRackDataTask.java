package com.renturapp.scansist.AsynTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.renturapp.scansist.MyAsyncBus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by wayne on 30/09/16.
 * This is used to download data from movesist.com
 */

public class DownloadRackDataTask extends AsyncTask<String, Void, String> {
    ProgressDialog progressDialog;
    Context context;

    public DownloadRackDataTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressDialog = new ProgressDialog(context);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
    }

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
        MyAsyncBus.getInstance().post(new DownloadRackDataTaskResultEvent(result));
        progressDialog.dismiss();
    }
}
