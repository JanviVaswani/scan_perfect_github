package com.renturapp.scansist;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Created by Wayne on 08/08/2015.
 * Default Licence check
 */
class CheckReleaseTask extends AsyncTask<String, Void, String> {

    //public AsyncResponse delegate = null;//Call back interface
    //public CheckLicenceTask(AsyncResponse asyncResponse) {
    //delegate = asyncResponse;//Assigning call back interface through constructor
    @Override
    protected String doInBackground(String... urls) {
        String result = "";
        Boolean responseOK = false;
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            HttpsURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpsURLConnection con =
              (HttpsURLConnection) new URL(urls[0]).openConnection();
            //http://stackoverflow.com/questions/17638398/androids-httpurlconnection-throws-eofexception-on-head-requests
            con.setRequestProperty( "Accept-Encoding", "" );
            //con.setRequestMethod("HEAD");
            responseOK = (con.getResponseCode() == HttpsURLConnection.HTTP_OK);

            if (responseOK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                String i = sb.toString();
                result = i.substring(i.indexOf("<title>")+7,i.indexOf("</title>"));
                //if (v.equals(urls[1])) {
                //    result = true;
                //}
            }
        } catch (Exception e) {e.printStackTrace();
            e.printStackTrace();
        }
        return result;
    }
    @Override
    protected void onPostExecute(String result) {
        //delegate.processFinish(result);
        MyAsyncBus.getInstance().post(new CheckReleaseTaskResultEvent(result));
    }
}
