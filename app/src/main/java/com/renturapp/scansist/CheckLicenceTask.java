package com.renturapp.scansist;

import android.os.AsyncTask;

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
class CheckLicenceTask extends AsyncTask<String, Void, Boolean> {

    //public AsyncResponse delegate = null;//Call back interface
    //public CheckLicenceTask(AsyncResponse asyncResponse) {
        //delegate = asyncResponse;//Assigning call back interface through constructor
    @Override
    protected Boolean doInBackground(String... urls) {
        Boolean result = false;
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
            con.setRequestMethod("HEAD");
            result = (con.getResponseCode() == HttpsURLConnection.HTTP_OK);
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

