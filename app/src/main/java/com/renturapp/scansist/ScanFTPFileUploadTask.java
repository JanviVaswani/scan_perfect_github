package com.renturapp.scansist;

import android.os.AsyncTask;

/*import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;*/

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wayne on 08/08/2015.
 * Default Template
 */
public class ScanFTPFileUploadTask extends AsyncTask<String, Void, Boolean> {
    //0  - Domain (Url)
    //1  - Username
    //2  - Password
    //3  - JSon Scans String
    //4  - Filename
    //5  - Direction
    //6  - Status
    //7  - Rack
    //8  - ManifestDate
    //9  - DepotNumber
    //10 - ScanSistCode

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();

            pairs.add(new BasicNameValuePair("CompanyID", "42"));
            pairs.add(new BasicNameValuePair("CCGoodID", "1864"));
            pairs.add(new BasicNameValuePair("CCGoodStatus", "1"));
            pairs.add(new BasicNameValuePair("putType", "3"));
            pairs.add(new BasicNameValuePair("CCGoodRackID", "37"));

            HttpClient client = new DefaultHttpClient();
            HttpPut put = new HttpPut(params[0]);
            put.setEntity(new UrlEncodedFormEntity(pairs));
            HttpResponse response = client.execute(put);
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();

            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            StringBuilder strresponse = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                strresponse.append(inputLine);
            in.close();
            Log.e("Put Response Data", strresponse.toString());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        //delegate.processFinish(result);
        MyAsyncBus.getInstance().post(new ScanFTPFileUploadTaskResultEvent(result));
    }
}