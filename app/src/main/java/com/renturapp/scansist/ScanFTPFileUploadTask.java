package com.renturapp.scansist;

import android.os.AsyncTask;

import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Wayne on 08/08/2015.
 * Default Template
 */
class ScanFTPFileUploadTask extends AsyncTask<String, Void, Boolean> {
  //0  - Domain (Url)
  //1  - Username
  //2  - Password
  //3  - JSon Scans String
  //4  - Filename
  //5  - Direction
  //6  - Status
  //7  - Trunk
  //8  - ManifestDate
  //9  - DepotNumber
  //10 - ScanSistCode
    @Override
    protected Boolean doInBackground(String... params) {
        Boolean result = false;
        FTPClient con = new FTPClient();

        try {

            con.connect(InetAddress.getByName(params[0]));

            if (con.login(params[1], params[2])) {
                con.enterLocalPassiveMode();
                //Array Data

                JSONArray sa = new JSONArray(params[3]);
                StringBuilder data = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                    + "<Uploads xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n"
                    + " <Statuses>\n");
                for (int i=0; i<sa.length();i++){
                    JSONObject s = sa.getJSONObject(i);
                    data.append("  <Status>\n");

                    if(s.getInt("clauseID")==0) {
                        data.append("   <Barcode>").append(s.getString("scanBarCode")).append("</Barcode>\n");
                        data.append("   <StatusCode>").append(params[6]).append("</StatusCode>\n");
                        data.append("   <StatusDate>").append(s.getString("scanDateTime")).append("</StatusDate>\n");
                        data.append("   <User>").append(params[9]).append("</User>\n");
                        data.append("   <Device>").append(params[9]).append("-").append(params[10]).append("</Device>\n");
                        data.append("   <Note/>\n");
                        data.append("   <Manifest>\n");
                        data.append("    <ManifestNo></ManifestNo>\n");
                        data.append("    <Depot>").append(params[9]).append("</Depot>\n");
                        data.append("    <Trunk>").append(params[7]).append("</Trunk>\n");
                        data.append("    <ManifestDate>").append(params[8]).append("</ManifestDate>\n");
                        data.append("    <Direction>").append(params[5]).append("</Direction>\n");
                        data.append("    <Downloaded></Downloaded>\n");
                        data.append("   </Manifest>\n");
                    } else {
                        data.append("   <Barcode>").append(s.getString("scanBarCode")).append("</Barcode>\n");
                        data.append("   <StatusCode>").append(s.getString("clauseCode")).append("</StatusCode>\n");
                        data.append("   <StatusDate>").append(s.getString("scanDateTime")).append("</StatusDate>\n");
                        data.append("   <User>").append(params[9]).append("</User>\n");
                        data.append("   <Device>").append(params[9]).append("-").append(params[10]).append("</Device>\n");
                        data.append("   <Note/>\n");
                    }
                    data.append("  </Status>\n");
                }

                data.append(" </Statuses>\n");
                data.append("</Uploads>\n");
                ByteArrayInputStream in = new ByteArrayInputStream(data.toString().getBytes());
                result = con.storeFile(params[4], in);
                in.close();
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
        //delegate.processFinish(result);
        MyAsyncBus.getInstance().post(new ScanFTPFileUploadTaskResultEvent(result));
    }
}