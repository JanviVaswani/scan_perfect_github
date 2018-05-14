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
    //0 - Domain (Url)
    //1 - Username
    //2 - Password
    //3 - JSon Scans String
    //4 - Filename
    //5 - Direction
    //6 - Status
    //7 - Trunk
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
                String data = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        +"<Uploads xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n"
                        +" <Statuses>\n";
                for (int i=0; i<sa.length();i++){
                    JSONObject s = sa.getJSONObject(i);
                    data+=" <Status>\n";

                    if(s.getInt("clauseID")==0) {
                        data+="  <Barcode>" + s.getString("scanBarCode")+"</Barcode>\n";
                        data+="  <StatusCode>" + params[6]  + "</StatusCode>\n";
                        data+="  <StatusDate>" + s.getString("scanDateTime") + "</StatusDate>\n";
                        data+="  <User>" + "099" + "</User>\n";
                        data+="  <Device>" + "099-1" + "</Device>\n";
                        data+="  <Note/>\n";
                        data+="  <Manifest>\n";
                        data+="   <ManifestNo></ManifestNo>\n";
                        data+="   <Depot>099</Depot>\n";
                        data+="   <Trunk>" + params[7] + "</Trunk>\n";
                        data+="   <ManifestDate>" + params[8] + "</ManifestDate>\n";
                        data+="   <Direction>" + params[5] + "</Direction>\n";
                        data+="   <Downloaded></Downloaded>\n";
                        data+="  </Manifest>\n";
                    } else {
                        data+="  <Barcode>"    + s.getString("scanBarCode")  + "</Barcode>\n";
                        data+="  <StatusCode>" + s.getString("clauseCode")   + "</StatusCode>\n";
                        data+="  <StatusDate>" + s.getString("scanDateTime") + "</StatusDate>\n";
                        data+="  <User>" + "099" + "</User>\n";
                        data+="  <Device>" + "099-1" + "</Device>\n";
                        data+="  <Note/>\n";
                    }
                    /*scans.add(new Scan(
                            s.getInt("scanID"),
                            s.getInt("clauseID"),
                            s.getString("clauseCode"),
                            s.getString("scanBarCode"),
                            s.getString("scanDateTime")));*/

                    data+="  </Status>\n";
                }

                data+=" </Statuses>\n";
                data+="</Uploads>\n";
                ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes());
                result = con.storeFile(params[4], in);
                in.close();
                if (result) {
                    result = con.sendSiteCommand("chmod 604 " + params[4]);
                }
                // if (result)
                // System.out.println("upload result: " + result);
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