package com.renturapp.scansist.AsynTask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.renturapp.scansist.CustomHttpClient;
import com.renturapp.scansist.Model.ScanGoodsModel;
import com.renturapp.scansist.MyAsyncBus;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wayne on 15/10/16.
 * This is used to download data from movesist.com
 */

public class ScanGoodsInLocationTask extends AsyncTask<ScanGoodsModel, Void, String> {

    Context context;

    public ScanGoodsInLocationTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(ScanGoodsModel... params) {

        String scangoodsuploadurl = "https://www.movesist.uk/data/scangoods/";
        //List<ScanGoodsModel> goodslist = params[0];
        ScanGoodsModel scangoodout=params[0];
        String result = "";

        //for (ScanGoodsModel scangoodout : goodslist) {
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("CompanyID", (scangoodout.companyID).toString()));
            nameValuePairs.add(new BasicNameValuePair("CCGoodIDs", (scangoodout.CCGoodIDs)));
            nameValuePairs.add(new BasicNameValuePair("CCGoodStatus", (scangoodout.cCGoodStatus).toString()));
            nameValuePairs.add(new BasicNameValuePair("CCGoodRackID", (scangoodout.cCGoodRackID).toString()));

            Log.i("prm", params[0].toString());

            try {
                StringBuilder strResponse = null;
                CustomHttpClient httpclient = new CustomHttpClient();
                HttpPut httpput = new HttpPut(scangoodsuploadurl);
                httpput.setHeader("Content-type", "application/x-www-form-urlencoded");
                httpput.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httpput);
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                Log.e("pass 1", "connection success ");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                is));
                strResponse = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null)
                    strResponse.append(inputLine);
                in.close();

                result = strResponse != null ? strResponse.toString() : null;
                Log.i("rsps", result);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("err", e.toString());
                result = "Update Error";// + e.toString();
            }
        //}
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.i("dwe", "done without error" + result);
        MyAsyncBus.getInstance().post(new ScanGoodsInLocationTaskResultEvent(result));
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params) {
            if (first)
                first = false;
            else
                result.append("&");

            try {
                result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}