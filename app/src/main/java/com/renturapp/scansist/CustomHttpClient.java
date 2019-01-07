package com.renturapp.scansist;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

public class CustomHttpClient extends DefaultHttpClient {

  public CustomHttpClient() {
    super();
    SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
    socketFactory.setHostnameVerifier(new CustomHostnameVerifier());
    Scheme scheme = (new Scheme("https", socketFactory, 443));
    getConnectionManager().getSchemeRegistry().register(scheme);
  }
}
