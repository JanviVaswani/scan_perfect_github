package com.renturapp.scansist;

import android.os.AsyncTask;

import org.apache.commons.net.ftp.FTPClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Wayne on 08/08/2015.
 * Default Template
 */
class FTPFileUploadTask extends AsyncTask<String, Void, Boolean> {

    @Override
    protected Boolean doInBackground(String... params) {
        Boolean result = false;
        FTPClient con = new FTPClient();

        try {
            con.connect(InetAddress.getByName(params[0]));
            if (con.login(params[1], params[2])) {
                con.enterLocalPassiveMode();
                String data = params[3];
                ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes());
                result = con.storeFile(params[4], in);
                in.close();
                if (result) {
                    result = con.sendSiteCommand("chmod 604 " + params[4]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            con.logout();
            con.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        MyAsyncBus.getInstance().post(new FTPFileUploadTaskResultEvent(result));
    }
}

