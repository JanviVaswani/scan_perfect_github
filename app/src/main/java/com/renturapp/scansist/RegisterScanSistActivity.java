package com.renturapp.scansist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.renturapp.scansist.MainActivity.androidId;
import static com.renturapp.scansist.MainActivity.deviceFilePath;
import static com.renturapp.scansist.MainActivity.deviceFullPath;
import static com.renturapp.scansist.MainActivity.deviceUu;
import static com.renturapp.scansist.MainActivity.mCompanyID;
import static com.renturapp.scansist.MainActivity.mcompany;
import static com.renturapp.scansist.MainActivity.regdatetime;
import static com.renturapp.scansist.MainActivity.urlExtension;

public class RegisterScanSistActivity extends Activity {

    private Context context;
    Button btnClose;
    private EditText depotNumber;
    private Utility u;
    private MainActivity mA;
    private int iCompanyID = 0;

    String currentVersion;
    String latestVersion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.scansist_register);

        context = RegisterScanSistActivity.this;
        MyAsyncBus.getInstance().register(this);
        u = (Utility) getApplicationContext();
        //mA = (MainActivity)context;

        btnClose = (Button) findViewById(R.id.btnRegisterScanSist);
        btnClose.setVisibility(View.INVISIBLE);

        depotNumber = (EditText) findViewById(R.id.txtDepotNumber);
        depotNumber.requestFocus();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        depotNumber.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    if (depotNumber.getText().length() == 5) {
                        Toast.makeText(context, " Registering ScanSist™ app to depot: " + depotNumber.getText(), Toast.LENGTH_SHORT).show();
                        String dn = depotNumber.getText().toString();
                        if (u.isOnline()) {
                            new DownloadRegisterDataTask().execute("https://www.movesist" + urlExtension + "/data/company/?DepotNumber=" + dn + "&getType=1");
                        }

                        return true;
                    } else {
                        Toast.makeText(context, "Depot (" + depotNumber.getText() + ") must be 5 characters!", Toast.LENGTH_SHORT).show();
                        depotNumber.requestFocus();
                        return false;
                    }
                }
                return false;
            }
        });
    }

    @Subscribe
    public void onDownloadRegisterDataTaskResultEvent(DownloadRegisterDataTaskResultEvent event) {

        String companyID;
        String depotNumber;
        String companyUrl;
        String companyName;

        if (event.getResult() != null) {
            SharedPreferences dp = PreferenceManager.getDefaultSharedPreferences(context);
            //now get the Editor
            SharedPreferences.Editor editor = dp.edit();
            try {

                JSONObject obj = new JSONObject(event.getResult());
                iCompanyID = obj.getInt("CompanyID");

                if (iCompanyID == 0) {
                    u.displayMessage(context, "Depot Registration Failed.");
                } else {
                    companyID = Integer.toString(obj.getInt("CompanyID"));
                    //Set up Company Id for Application
                    u.RegCompanyId = companyID;
                    u.showlicensedialog = true;

                    depotNumber = obj.getString("DepotNumber");
                    companyUrl = obj.getString("CompanyUrl");
                    companyName = obj.getString("CompanyName");

                    editor.putString("CompanyID", companyID);
                    editor.putString("DepotNumber", depotNumber);
                    editor.putString("CompanyUrl", companyUrl);
                    editor.putString("CompanyName", companyName);

                    //was added in 2.3, it commits without returning a boolean indicating success or failure
                    editor.apply();
                    u.displayMessage(context, "Depot Registration Information Saved.");
                    mCompanyID = companyID;
                    mcompany = companyUrl;

                    //Upload file
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE MMM dd yyyy, hh:mm:ss aa", Locale.UK);
                    String licencedatetime = sdf.format(new Date());
                    SimpleDateFormat reg_sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.UK);

                    regdatetime = reg_sdf.format(new Date());

                    deviceUu = "scansist_" + mcompany + "_" + androidId;
                    deviceFullPath = "https://www.movesist" + urlExtension + "/clients/" + mcompany + "/users/scansist/" + deviceUu + "_p.html";
                    deviceFilePath = "/" + mcompany + "/users/scansist/" + deviceUu + "_p.html";

                    u.readTelephoneDetails();

                    String simInfo;
                    simInfo = "Serial: " + u.tmSerial
                            + "\nTel: " + u.tmLineNumber
                            + "\nNetwork: " + u.tmNetworkOperator
                            + "\nName: " + u.tmNetworkOperatorName
                            + "\nSimOp: " + u.tmSimOperator
                            + "\nSimOpName: " + u.tmSimOperatorName
                            + "\nCellLocation: " + u.tmCellLocation
                            + "\nDeviceId: " + u.tmDevice
                            + "\nAndroidId: " + androidId
                            + "\nRegDateTime: " + licencedatetime
                            + "\nScanSistIsDeleted: false"
                            + "\nCompanyID: " + mCompanyID;
                    new FTPFileUploadTask().execute("www.movesist" + urlExtension,
                            "clients",
                            "wdrcv227qt",
                            simInfo,
                            deviceFilePath);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            //TODO
            // delaydialogueClose(true);
            u.displayMessage(context, "Depot Registration Failed.");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        MyAsyncBus.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //Must use default preference file!
        String deviceUuPref = PreferenceManager.getDefaultSharedPreferences(context).getString("RegKey", "NothingFound");
        if (deviceUuPref.equals("NothingFound")) {
            //Do we need to upload a file
            final ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setCustomTitle(u.setProgressTitle(context));
            progressDialog.setMessage(MainActivity.notRegistered);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();

            //delaydialogueClose(true);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 2s = 2000ms
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    //finish();
                }
            }, 2000);
        } else {
            if (iCompanyID != 0) {
                Intent mainActivity = new Intent(RegisterScanSistActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainActivity);
            }
        }
    }

    //App Upgrade
    class GetVersion extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            return null;
        }
    }

}