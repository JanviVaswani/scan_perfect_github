package com.renturapp.scansist;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Wayne on 08/08/2015.
 * Registering PODSist
 */
class RegisterScanSistTask extends AsyncTask<String, Void, Boolean> {

    @Override
    protected Boolean doInBackground(String... params) {

        Boolean result;

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("Serial", params[0]));
        nameValuePairs.add(new BasicNameValuePair("Tel", params[1]));
        nameValuePairs.add(new BasicNameValuePair("Network", params[2]));
        nameValuePairs.add(new BasicNameValuePair("Name", params[3]));
        nameValuePairs.add(new BasicNameValuePair("SimOp", params[4]));
        nameValuePairs.add(new BasicNameValuePair("SimOpName", params[5]));
        nameValuePairs.add(new BasicNameValuePair("CellLocation", params[6]));
        nameValuePairs.add(new BasicNameValuePair("DeviceId", params[7]));
        nameValuePairs.add(new BasicNameValuePair("AndroidId", params[8]));
        nameValuePairs.add(new BasicNameValuePair("FileName", params[9]));
        nameValuePairs.add(new BasicNameValuePair("RegDateTime", params[10]));
        nameValuePairs.add(new BasicNameValuePair("ScanSistIsDeleted", params[11]));
        nameValuePairs.add(new BasicNameValuePair("CompanyID", params[12]));
        nameValuePairs.add(new BasicNameValuePair("postType", params[13]));

        try {
            HttpClient httpclient = new CustomHttpClient();
            HttpPost httppost = new HttpPost(params[14]);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            Log.e("pass 1", "connection success ");
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Fail 1", e.toString());
            result = false;
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        MyAsyncBus.getInstance().post(new RegisterScanSistTaskResultEvent(result));
    }
}

