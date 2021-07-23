package com.renturapp.scansist.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.renturapp.scansist.AboutActivity;
import com.renturapp.scansist.AsynTask.ScanRackLocationAsyncTask;
import com.renturapp.scansist.AsynTask.ScanRackLocationAsyncTaskResultEvent;
import com.renturapp.scansist.Clause;
import com.renturapp.scansist.LicenceActivity;
import com.renturapp.scansist.MainActivity;
import com.renturapp.scansist.MyAsyncBus;
import com.renturapp.scansist.R;
import com.renturapp.scansist.Model.Scan;
import com.renturapp.scansist.Model.ScanModel;
import com.renturapp.scansist.SettingsActivity;
import com.renturapp.scansist.Utility;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScanRackLocationRadioOneActivity extends Activity implements
        DecoratedBarcodeView.TorchListener {

    private Utility u;

    private static final String TAG = ScanRackLocationRadioOneActivity.class.getSimpleName();
    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;
    private TextView lblScan;

    private Boolean initClause;
    private Spinner spnClause;
    private Button btnNext;
    private int scanCount;
    //private MainActivity mA;
    private Context context;

    private String mRack, mManifestDate, mDirection, mStatus;
    int globalStatus;
    //Edited Code
    List<ScanModel> globalData = Collections.emptyList();

    String getDatefromBundle;

    private static final int DELAY = 2000; // 2 second

    private BarcodeCallback callback = new BarcodeCallback() {

        private long lastTimestamp = 0;

        @Override
        public void barcodeResult(BarcodeResult result) {
            if (System.currentTimeMillis() - lastTimestamp < DELAY) {
                return;
            }

            if (result.getText() == null) {
                // Prevent nulls
                return;
            }

            //Validate Barcode
            boolean barcodeexists = false;
            int _rackId = 0;
            String _rackDescription = "";
            String _rackBarcode = "";

            int _picklistId = 0;
            String _picklistDescription = "";

            if (globalStatus == 0) {
                if (result.getText().startsWith("01")) {
                    for (ScanModel single : globalData) {
                        if (single.rackBarcode.equals(result.getText())) {
                            barcodeexists = true;
                            _rackId = single.rackID;
                            _rackDescription = single.rackDescription;
                        }
                    }

                    if (barcodeexists) {
                        //Toast.makeText(ScanRackLocationRadioOneActivity.this, "Valid Rack Location Barcode", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ScanRackLocationRadioOneActivity.this, ScanGoodsForRackLocationRadioOneActivity.class);
                        intent.putExtra("rackID", _rackId);
                        intent.putExtra("rackDescription", _rackDescription);
                        intent.putExtra("scanDateTime", getDatefromBundle);
                        startActivity(intent);
                        finish();
                    } else {
                        // Too soon after the last barcode - ignore.
                        Toast.makeText(ScanRackLocationRadioOneActivity.this, "Invalid Rack Location", Toast.LENGTH_SHORT).show();
                        beepManager.playBeepSoundAndVibrate();
                        lastTimestamp = System.currentTimeMillis();
                        return;
                    }
                } else {
                    Toast.makeText(ScanRackLocationRadioOneActivity.this, "Not a Rack Barcode", Toast.LENGTH_SHORT).show();
                    beepManager.playBeepSoundAndVibrate();
                    lastTimestamp = System.currentTimeMillis();
                    return;
                }
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    private void saveScan(String scanDateTime) {
        u.scans.add(new Scan(u.scans.size() + 1, 0, "    ", lastText, scanDateTime));
        u.scanAdapter.notifyDataSetChanged();
        u.saveScans();
        scanCount = 0;
        updateScanInfo(scanCount);
        u.sortScans();
    }

    private void setNextFinish(Boolean enabled) {
        if (enabled) {
            btnNext.setClickable(true);
            btnNext.setAlpha(1f);
        } else {
            btnNext.setClickable(false);
            btnNext.setAlpha(0.5f);
        }
    }

    private void setSpnClause(int clauseID) {
        for (int i = 0; i <= u.clauseAdapter.getCount(); i++) {
            Clause c = (Clause) spnClause.getItemAtPosition(i);
            if (c != null && clauseID == c.clauseID) {
                spnClause.setSelection(i);
                break;
            }
        }
    }

    private void updateScanInfo(int s) {
        lblScan.setText(s + " Scans");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_scan);

        context = ScanRackLocationRadioOneActivity.this;

        //Edited Code
        //To get Date From Bundle
        Bundle bundleGetDate = getIntent().getExtras();
        getDatefromBundle = bundleGetDate.getString("scanDateTime");

        MyAsyncBus.getInstance().register(this);

        u = (Utility) getApplicationContext();  //mA = (MainActivity)context;

        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setText("Finish");
        lblScan = (TextView) findViewById(R.id.lblScan);
        scanCount = 0;
        // Create object of SharedPreferences.
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);

        /* From intent or Shared Preferences*/
        Intent intent = getIntent();
        // getting attached intent data
        Boolean previousPressed = intent.getBooleanExtra("onBackPressed", false);
        /* Called when the activity is first created. */

        int status = intent.getIntExtra("status", -1);
        if (status != -1) {
            setTitleCode(status);
        } else {
            status = sharedPref.getInt("status", -1);
            if (status != -1) {
                setTitleCode(status);
            }
        }

        globalStatus = status;
        String fetchbarcodesurl = "";

        if (status == 0) {
            fetchbarcodesurl = "https://movesist.uk/data/racks?CompanyID=" + u.RegCompanyId + "&getType=0";

            ScanRackLocationAsyncTask scanAsyncTask = new ScanRackLocationAsyncTask();
            scanAsyncTask.execute(fetchbarcodesurl);

        }

        int rackID = intent.getIntExtra("rackID", -1);

        if (rackID != -1 && rackID != 0) {
            String rackDescription = intent.getStringExtra("rackDescription");
            ((TextView) findViewById(R.id.lblScanRack)).setText(rackDescription);

        } else {
            rackID = sharedPref.getInt("rackID", 0);
            ((TextView) findViewById(R.id.lblScanRack)).setText("Scan RackList");
        }
        mRack = String.format(Locale.UK, "%02d", rackID);

        String scanDateTime = intent.getStringExtra("scanDateTime");

        if (!scanDateTime.isEmpty()) {
            //setScanDate(scanDateTime);
        } else {
            scanDateTime = sharedPref.getString("scanDateTime", "NothingFound");
            if (!scanDateTime.equals("NothingFound")) {
                //setScanDate(scanDateTime);
            }
        }
        mManifestDate = scanDateTime.substring(0, 10);
        /* End of setup             */

        setNextFinish(u.HasScans(sharedPref) && u.isOnline());

        initClause = true;//stop initialisation firing

        barcodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_scanner);

        barcodeView.decodeContinuous(callback);

        beepManager = new BeepManager(this);
        beepManager.setVibrateEnabled(true);

        barcodeView.setTorchListener(this);
    }

    //Async Result for Rack Locations
    @Subscribe
    public void onAsyncScanRackLocationResultEvent(ScanRackLocationAsyncTaskResultEvent event) {
        List<ScanModel> data = new ArrayList<>();
        try {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);
            int status = sharedPref.getInt("status", -1);
            JSONArray jArray = new JSONArray(event.getResult());

            // Extract data from json and store into ArrayList as class objects
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject json_data = jArray.getJSONObject(i);

                ScanModel scanModel = new ScanModel();

                scanModel.rackBarcode = json_data.getString("RackBarcode");
                scanModel.rackID = json_data.getInt("RackID");
                scanModel.rackDescription = json_data.getString("RackDescription");

                data.add(scanModel);
            }
            globalData = data;
        } catch (JSONException e) {
            Toast.makeText(ScanRackLocationRadioOneActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void setTitleCode(int status) {
        TextView t = (TextView) findViewById(R.id.lblTitle);

        switch (status) {

            case 0:
                t.setText(getString(R.string.into_location));
                mDirection = getString(R.string.into_location_direction);
                mStatus = getString(R.string.into_location_status);
                break;
            case 1:
                t.setText(getString(R.string.check_rack_goods));
                mDirection = getString(R.string.check_rack_goods_direction);
                mStatus = getString(R.string.check_rack_goods_status);
                break;
            case 2:
                t.setText(getString(R.string.check_goods));
                mDirection = getString(R.string.check_goods_direction);
                mStatus = getString(R.string.check_goods_status);
                break;
            case 3:
                t.setText(getString(R.string.goods_out));
                mDirection = getString(R.string.goods_out_direction);
                mStatus = getString(R.string._goods_out_location_status);
                break;
            default:
                t.setText(getString(R.string.into_location));
                mDirection = getString(R.string.into_location_direction);
                mStatus = getString(R.string.into_location_status);
                break;
        }
    }



    // very important when rotating !!! http://stackoverflow.com/questions/456211/activity-restart-on-rotation-android
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
        try {
            registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
        try {
            unregisterReceiver(networkStateReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);
                setNextFinish(u.HasScans(sharedPref));
            } else {
                setNextFinish(false);
            }
        }
    };

    public void pause(View view) {
        barcodeView.pause();
    }

    public void resume(View view) {
        barcodeView.resume();
    }

    public void triggerScan(View view) {
        barcodeView.decodeSingle(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    /**
     * Check if the device's camera has a Flashlight.
     *
     * @return true if there is Flashlight, otherwise false.
     */
    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    @Override
    public void onTorchOff() {
        MenuItem f = u.getFlash();
        f.setIcon(R.drawable.flashoff);
        f.setTitle(getString(R.string.turn_on_flashlight));
    }

    @Override
    public void onTorchOn() {
        MenuItem f = u.getFlash();
        f.setIcon(R.drawable.flashon);
        f.setTitle(R.string.turn_off_flashlight);
    }

    private Boolean inValidManifestDate() {

        /* Compare Dates of scans*/
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
            Date date = sdf.parse(mManifestDate);
            Date today = new Date();
            int bstOffSet = 0;
            if (today.toString().contains("BST") || today.toString().contains("GMT+01:00") || today.toString().contains("GMT+05:30")) {
                bstOffSet = 36000000;
            }
            int difference =
                    ((int) (((date.getTime() + bstOffSet) / (24 * 60 * 60 * 1000))
                            - (int) (today.getTime() / (24 * 60 * 60 * 1000))));

            if (difference < 0) {
                return true;
            } else {
                return false;

            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void onWizardButtonClicked(View v) {
        // Check which radio button was clicked
        View vb = findViewById(R.id.barcode_scanner);
        pause(vb);
        switch (v.getId()) {
            case R.id.btnPrevious:
                onBackPressed();
                break;
            case R.id.btnNext:
                u.messageBox(context, 1, false);
                break;
            case R.id.btnCancel:
                u.messageBox(context, 2, false);
                break;
            default:
                throw new RuntimeException("Unknow button ID");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        u.setHome(menu.findItem(R.id.action_home));
        u.setBarCode(menu.findItem(R.id.action_barcode));
        u.setFlash(menu.findItem(R.id.action_flash));

        u.changeMenuItemState(u.getHome(), true, true, true);
        u.changeMenuItemState(u.getBarCode(), true, true, true);
        u.changeMenuItemState(u.getFlash(), true, hasFlash(), true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        u.hideKeyboard(context);
        MenuItem h = u.getHome();
        MenuItem f = u.getFlash();
        MenuItem b = u.getBarCode();

        //FragmentTransaction ft;
        int id = item.getItemId();
        Intent nextScreen;
        switch (id) {
            case R.id.action_settings:
                nextScreen = new Intent(ScanRackLocationRadioOneActivity.this, SettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //nextScreen.putExtras(bundle);
                startActivity(nextScreen);
                //finish();
                break;
            case R.id.action_about:
                nextScreen = new Intent(ScanRackLocationRadioOneActivity.this, AboutActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //nextScreen.putExtras(bundle);
                startActivity(nextScreen);
                //finish();
                break;
            case R.id.action_licence:
                nextScreen = new Intent(ScanRackLocationRadioOneActivity.this, LicenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //nextScreen.putExtras(bundle);
                startActivity(nextScreen);
                //finish();
                break;
            case R.id.action_home:
                // User choose the "Setup home Option" action, go to home screen
                //changeMenuItemState(h, false, true, false);
                //Intent homeScreen = new Intent(ScanActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //startActivity(homeScreen);
                //finish();
                onBackPressed();
                return true;
            case R.id.action_barcode:

                View v = findViewById(R.id.barcode_scanner);
                if (b.getTitle().equals(getString(R.string.scan_resume))) {
                    b.setTitle(R.string.scan_pause);
                    u.changeMenuItemState(b, true, true, true);
                    resume(v);
                } else {
                    b.setTitle(R.string.scan_resume);
                    u.changeMenuItemState(b, true, true, false);
                    pause(v);
                }
                //return true;
                break;
            case R.id.action_flash:

                if (f.getTitle().equals(getString(R.string.turn_on_flashlight))) {
                    barcodeView.setTorchOn();
                } else {
                    barcodeView.setTorchOff();
                }
                //return true;
                break;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Clear scans
        u.scans.clear();
        u.scanAdapter.notifyDataSetChanged();

        Bundle bundle = new Bundle();
        bundle.putBoolean("onBackPressed", true);
        Intent previousScreen = new Intent(ScanRackLocationRadioOneActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        previousScreen.putExtras(bundle);
        startActivity(previousScreen);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        MyAsyncBus.getInstance().unregister(this);
        super.onDestroy();
    }

//    public class ScanAsyncTask extends AsyncTask<String, String, String> {
//        HttpURLConnection conn;
//        URL url = null;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//            try {
//                String jsonUrl = strings[0];
//                String jsonGetString;
//
//                URL url = new URL(jsonUrl);
//                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//                httpURLConnection.setInstanceFollowRedirects(true);
//                InputStream inputStream = httpURLConnection.getInputStream();
//                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//
//                StringBuffer stringBuffer = new StringBuffer();
//                while ((jsonGetString = bufferedReader.readLine()) != null) {
//                    stringBuffer.append(jsonGetString + "\n");
//                }
//
//                bufferedReader.close();
//                inputStream.close();
//                httpURLConnection.disconnect();
//                return stringBuffer.toString().trim();
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            //this method will be running on UI thread
//
//            List<ScanModel> data = new ArrayList<>();
//            try {
//                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);
//                int status = sharedPref.getInt("status", -1);
//                JSONArray jArray = new JSONArray(result);
//
//                // Extract data from json and store into ArrayList as class objects
//                for (int i = 0; i < jArray.length(); i++) {
//                    JSONObject json_data = jArray.getJSONObject(i);
//
//                    ScanModel scanModel = new ScanModel();
//
//                    if (status == 3) {
//                        scanModel.pickListBarcode = json_data.getString("PickListBarcode");
//                        scanModel.pickListID = json_data.getInt("PickListID");
//                        scanModel.pickListDescription = json_data.getString("PickListDescription");
//                    } else {
//                        scanModel.rackBarcode = json_data.getString("RackBarcode");
//                        scanModel.rackID = json_data.getInt("RackID");
//                        scanModel.rackDescription = json_data.getString("RackDescription");
//                    }
//                    data.add(scanModel);
//                }
//                globalData = data;
//            } catch (JSONException e) {
//                Toast.makeText(ScanRackLocationRadioOneActivity.this, e.toString(), Toast.LENGTH_LONG).show();
//            }
//        }
//    }
}