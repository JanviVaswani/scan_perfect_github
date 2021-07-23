package com.renturapp.scansist.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;

import android.view.Gravity;
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
import com.renturapp.scansist.AsynTask.ScanRackGoodsAsyncTask;
import com.renturapp.scansist.AsynTask.ScanRackGoodsAsyncTaskResultEvent;

import com.renturapp.scansist.LicenceActivity;
import com.renturapp.scansist.MainActivity;
import com.renturapp.scansist.MyAsyncBus;
import com.renturapp.scansist.R;
import com.renturapp.scansist.Model.Scan;
import com.renturapp.scansist.AsynTask.ScanGoodsInLocationTask;
import com.renturapp.scansist.AsynTask.ScanGoodsInLocationTaskResultEvent;
import com.renturapp.scansist.Model.ScanGoodsModel;
import com.renturapp.scansist.SettingsActivity;
import com.renturapp.scansist.Utility;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ScanGoodsForRackLocationRadioOneActivity extends Activity implements
        DecoratedBarcodeView.TorchListener {

    private Utility u;

    private static final String TAG = ScanGoodsForRackLocationRadioOneActivity.class.getSimpleName();
    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;
    private TextView lblScan;

    private Boolean initClause;
    private Spinner spnClause;
    private Button btnNext,btnPrev,btnCancel;
    private int scanCount = 0;
    private Context context;

    private String mRack,mManifestDate,mDirection,mStatus;

    List<ScanGoodsModel> globalDataGoods = Collections.emptyList();

    int globalRackID1Variable;

    List<ScanGoodsModel> insertGoods = new ArrayList<>();
    ScanGoodsModel scangoodsoutObj;

    private static final int DELAY = 3000; // 3 second
    ProgressDialog progressDialog;

    private BarcodeCallback callback = new BarcodeCallback() {
        private long lastTimestamp = 0;
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(System.currentTimeMillis() - lastTimestamp < DELAY) {
                return;
            }

            if (result.getText() == null) {
                // Prevent nulls
                return;
            }

            boolean barcodeexists=false;
            int goodsrackid=0;

            if(result.getText().startsWith("00") && result.getText().length()==15){
                //To check if good is already scanned in any other location
                for(ScanGoodsModel gooditem:globalDataGoods){
                    if(gooditem.cCGoodBarcode.equals(result.getText())){
                        barcodeexists=true;
                        goodsrackid=gooditem.cCGoodRackID;
                    }
                }

                boolean updatescangoods=false;

                if(barcodeexists){
                    if(globalRackID1Variable == goodsrackid)
                    {
                        updatescangoods=true;
                    }
                    else{
                        updatescangoods=false;
                    }

                    if (result.getText().equals(lastText)){
                        beepManager.playBeepSoundAndVibrate();
                        Toast.makeText(ScanGoodsForRackLocationRadioOneActivity.this, "Barcode Already Exists", Toast.LENGTH_SHORT).show();
                    }

                    lastTimestamp = System.currentTimeMillis();
                }
                else{
                    updatescangoods=true;
                }

                if(!updatescangoods){
                    barcodeView.setStatusText(result.getText());
                    Toast.makeText(ScanGoodsForRackLocationRadioOneActivity.this, "The Goods have been scanned into another location", Toast.LENGTH_SHORT).show();
                    beepManager.playBeepSoundAndVibrate();
                    lastTimestamp = System.currentTimeMillis();
                }
                else{
                    //Set Goods Object to Scan In
                    scangoodsoutObj=new ScanGoodsModel();
                    scangoodsoutObj.companyID= Integer.parseInt(u.RegCompanyId);
                    scangoodsoutObj.cCGoodID=Integer.parseInt(result.getText().substring(2,12));
                    scangoodsoutObj.cCGoodRackID=globalRackID1Variable;
                    scangoodsoutObj.cCGoodStatus="1";

                    //Set Scan Count
                    if (result.getText().equals(lastText)) {
                        beepManager.playBeepSoundAndVibrate();
                        Toast.makeText(ScanGoodsForRackLocationRadioOneActivity.this, "Barcode Already Exists", Toast.LENGTH_SHORT).show();
                        lastTimestamp = System.currentTimeMillis();
                    }
                    else {
                        lastText = result.getText();
                        setNextFinish(u.isOnline());
                        SimpleDateFormat scan_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
                        String scanDateTime = scan_sdf.format(new Date());

                        if (u.scanAdapter.getCount() == 0) {
                            saveScan(scanDateTime);
                            insertGoods.add(scangoodsoutObj);
                        }
                        else {
                            //has it already been scaned!
                            boolean alreadyScanned = false;
                            for (Scan s : u.scans) {
                                if (s.scanBarCode.equals(lastText)) {
                                    alreadyScanned = true;
                                    //barcodeView.setStatusText(result.getText());
                                    break;
                                }
                            }

                            if (alreadyScanned) {
                                Toast.makeText(ScanGoodsForRackLocationRadioOneActivity.this, "Barcode Already Exists", Toast.LENGTH_SHORT).show();
                                lastTimestamp = System.currentTimeMillis();
                            }
                            else {
                                saveScan(scanDateTime);
                                insertGoods.add(scangoodsoutObj);
                            }
                        }
                        barcodeView.setStatusText(result.getText());
                    }
                }
            }
            else{
                // Too soon after the last barcode - ignore.
                Toast.makeText(ScanGoodsForRackLocationRadioOneActivity.this, "Not a Goods Barcode", Toast.LENGTH_SHORT).show();
                beepManager.playBeepSoundAndVibrate();
                lastTimestamp = System.currentTimeMillis();
            }
            barcodeView.setStatusText(result.getText());
            scanCount++;
            updateScanInfo(scanCount);
            lastTimestamp = System.currentTimeMillis();
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    private void saveScan(String scanDateTime) {
        u.scans.add(new Scan(u.scans.size()+1, 0, "    ", lastText, scanDateTime));
        u.scanAdapter.notifyDataSetChanged();
        u.saveScans();
        updateScanInfo(scanCount);
        u.sortScans();
    }

    private void setNextFinish(Boolean enabled){
        if(enabled) {
            btnNext.setClickable(true);
            btnNext.setAlpha(1f);
        } else {
            btnNext.setClickable(false);
            btnNext.setAlpha(0.5f);
        }
    }

    private void updateScanInfo(int s) {
        lblScan.setText(s + " Scans");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_scan_goods_for_rack_location_radio_one);

        context = ScanGoodsForRackLocationRadioOneActivity.this;

        String jsonUrl ="https://movesist.uk/data/ccgoods/?getType=6&CompanyID="+u.RegCompanyId;

        ScanRackGoodsAsyncTask scanAsyncTask  = new ScanRackGoodsAsyncTask();
        scanAsyncTask.execute(jsonUrl);

        MyAsyncBus.getInstance().register(this);

        u = (Utility)getApplicationContext();  //mA = (MainActivity)context;

        btnNext = (Button)findViewById(R.id.btnNext);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnPrev = (Button)findViewById(R.id.btnPrevious);
        btnNext.setText("Finish");
        lblScan = (TextView)findViewById(R.id.lblScan);
        scanCount = 0;

        // Create object of SharedPreferences.
        SharedPreferences sharedPref= getApplicationContext().getSharedPreferences("ScanSist",MODE_PRIVATE);

        /* From intent or Shared Preferences*/
        Intent intent = getIntent();
        // getting attached intent data
        Boolean previousPressed  = intent.getBooleanExtra("onBackPressed",false);
        /* Called when the activity is first created. */

        int status = intent.getIntExtra("status",-1);
        if (status!=-1) {
            setTitleCode(status);
        } else {
            status = sharedPref.getInt("status", -1);
            if (status != -1) {
                setTitleCode(status);
            }
        }

        int rackID = intent.getIntExtra("rackID", -1);

        if (rackID!=-1)
            if (rackID!=-1) {
                String rackDescription = intent.getStringExtra("rackDescription");
                ((TextView) findViewById(R.id.lblScanRack)).setText(rackDescription);
            } else {
                rackID = sharedPref.getInt("rackID", 0);
                ((TextView) findViewById(R.id.lblScanRack)).setText("Rack: " + rackID);
            }
        mRack = String.format(Locale.UK, "%02d",rackID);

        //Get Date from ScanRackLocationRadioOneActivity
        String scanDateTime = intent.getStringExtra("scanDateTime");

        //Get Rack ID from ScanRackLocationRadioOneActivity
        int rackID1 = intent.getIntExtra("rackID", 0);
        globalRackID1Variable = rackID1;

        if (!scanDateTime.isEmpty()) {
        } else {
            scanDateTime = sharedPref.getString("scanDateTime", "NothingFound");
            if (!scanDateTime.equals("NothingFound")){
            }
        }
        mManifestDate = scanDateTime.substring(0,10);
        /* End of setup             */

        setNextFinish(u.HasScans(sharedPref) && u.isOnline() );

        initClause = true;//stop initialisation firing
        barcodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(callback);

        beepManager = new BeepManager(this);
        beepManager.setVibrateEnabled(true);

        barcodeView.setTorchListener(this);
    }

    //Async Result for Rack Goods
    @Subscribe
    public void onAsyncScanRackGoodsResultEvent(ScanRackGoodsAsyncTaskResultEvent event) {
        //this method will be running on UI thread
        List<ScanGoodsModel> data=new ArrayList<>();
        try {
            JSONArray jArray = new JSONArray(event.getResult());

            // Extract data from json and store into ArrayList as class objects
            for(int i = 0; i < jArray.length(); i++){
                JSONObject json_data = jArray.getJSONObject(i);

                ScanGoodsModel scanGoodsModel = new ScanGoodsModel();

                scanGoodsModel.cCGoodID= json_data.getInt("CCGoodID");
                scanGoodsModel.rackDescription= json_data.getString("RackDescription");
                scanGoodsModel.cCGoodBarcode= json_data.getString("CCGoodBarcode");
                scanGoodsModel.companyID= json_data.getInt("CompanyID");
                scanGoodsModel.cCGoodRackID= json_data.getInt("CCGoodRackID");
                scanGoodsModel.cCGoodStatus= json_data.getString("CCGoodStatus");

                data.add(scanGoodsModel);
            }
            globalDataGoods = data;
        } catch (JSONException e) {
            Toast.makeText(ScanGoodsForRackLocationRadioOneActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void onWizardButtonClicked(View v) {
        // Check which radio button was clicked
        View vb = findViewById(R.id.barcode_scanner);
        pause(vb);
        switch(v.getId()) {
            case R.id.btnPrevious:
                onBackPressed();
                break;
            case R.id.btnNext:
                //messageBox(context,3,false);
                messageBox(context, 1, false);
                break;
            case R.id.btnCancel:
                messageBox(context,2,false);
                break;
            default:
                throw new RuntimeException("Unknow button ID");
        }
    }

    //On finish button click
    public void messageBox(Context ctx, int finish, Boolean timeout) {
        final Context c = ctx;
        final int f = finish;
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        StringBuilder message;
        if (f == 1) {
            builder.setTitle(R.string.confirm_finish);
        } else if (f == 2){
            builder.setTitle((R.string.confirm_cancel));
        } else {
            builder.setTitle((R.string.confirm_clear));
        }
        if (!u.scans.isEmpty()) {
            if (f == 1) {
                int d = 0;
                for (Scan s : u.scans) {
                    if (s.clauseID > 0) {
                        d++;
                    }
                }
                if (d > 0) {
                    message = new StringBuilder("Upload " + u.scanAdapter.getCount() + " Scanned Job" + (u.scanAdapter.getCount() > 1 ? "s" : "") + "\n"
                            + d + " Damaged:\n\n");
                } else {
                    message = new StringBuilder("Upload " + u.scanAdapter.getCount() + " Scanned Job" + (u.scanAdapter.getCount() > 1 ? "s" : "") + ":\n\n");
                }

                for (Scan s : u.scans) {
                    message.append(s.scanBarCode).append(" ").append(s.clauseCode).append(" ").append("\n");
                }
            } else {
                if (f == 3) {
                    message = new StringBuilder("Cancel the ScanSist™ App?\n\nClear Previous " + u.scanAdapter.getCount() + " Scanned Job" + (u.scanAdapter.getCount() > 1 ? "s" : "") + ":\n\n");
                } else {
                    message = new StringBuilder("Cancel the ScanSist™ App?\n\nClear " + u.scanAdapter.getCount() + " Scanned Job" + (u.scanAdapter.getCount() > 1 ? "s" : "") + ":\n\n");
                }
                for (Scan s : u.scans) {
                    if (s.clauseID > 0) {
                        message.append(s.scanBarCode).append(" ").append(s.clauseCode).append(" ").append("\n");
                    } else {
                        message.append(s.scanBarCode).append("               ").append("\n");
                    }
                }

            }

            //builder.setView(setText(c, message.toString(),f));
            builder.setMessage(message.toString());
        } else {
            if (f == 1) {
                //builder.setView(setText(c, getString(R.string.confirm_message_finish),f));
                builder.setMessage(R.string.confirm_message_finish);
            } else if (f == 2) {
                //builder.setView(setText(c, getString(R.string.confirm_message_cancel),f));
                builder.setMessage(R.string.confirm_message_cancel);
            } else {
                //builder.setView(setText(c, getString(R.string.confirm_message_clear),f));
                builder.setMessage(R.string.confirm_message_clear);
            }
        }

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (f == 1) {
                    dialog.dismiss();
                    barcodeView.setVisibility(View.INVISIBLE);
                    ((TextView) findViewById(R.id.lblScanRack)).setVisibility(View.INVISIBLE);
                    btnPrev.setEnabled(false);
                    btnNext.setEnabled(false);
                    btnCancel.setEnabled(false);
                    ((ScanGoodsForRackLocationRadioOneActivity) c).uploadScans();
                } else {
                    // Clear shared preferences
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.clear();   //its clear all data.
                    editor.apply();  //Don't forgot to commit  SharedPreferences.
                    dialog.dismiss();

                    final Timer t = new Timer();
                    t.schedule(new TimerTask() {
                        public void run() {
                            ((Activity) c).finish();
                            android.os.Process.killProcess(android.os.Process.myPid());
                            t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
                        }
                    }, 100); // after 2 second (or 2000 miliseconds), the task will be active.
                }
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                try {
                    View vb = ((ScanGoodsForRackLocationRadioOneActivity) c).findViewById(R.id.barcode_scanner);
                    ((ScanGoodsForRackLocationRadioOneActivity) c).resume(vb);
                    if (f ==3) {
                        ((ScanGoodsForRackLocationRadioOneActivity) c).onResume();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        Button n = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        n.setGravity(Gravity.LEFT);
        n.setWidth(150);

        if (timeout) {
            final Timer t = new Timer();
            t.schedule(new TimerTask() {
                public void run() {
                    dialog.dismiss(); // when the task active then close the dialog
                    t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
                }
            }, 3000); // after 2 second (or 2000 miliseconds), the task will be active.
        }
    }

    private void setTitleCode(int status){
        TextView t = (TextView)findViewById(R.id.lblTitle);

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
            default:
                t.setText(getString(R.string.into_location));
                mDirection = getString(R.string.into_location_direction);
                mStatus = getString(R.string.into_location_status);
                break;
        }

    }

    //***To upload Scanned Goods Out***
    public void uploadScans(){
        //Start Custom Progress Dialog
        progressDialog = new ProgressDialog(ScanGoodsForRackLocationRadioOneActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progess_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        //u.displayMessage(context, "ScanSist™ Upload Activated\nPlease Wait.");
        new ScanGoodsInLocationTask(ScanGoodsForRackLocationRadioOneActivity.this).execute(insertGoods);
    }

    @Subscribe
    public void onAsyncScanGoodsInLocationTaskResultEvent(ScanGoodsInLocationTaskResultEvent event) {
        View vb = findViewById(R.id.barcode_scanner);

        //Dismiss Custom Progress Dialog
        progressDialog.dismiss();

        if (event.getResult() =="Update Error") {
            u.displayMessage(context, "Upload Failed.");
            resume(vb);
        } else {
            //Toast.makeText(ScanGoodsForRackLocationRadioOFneActivity.this,"Goods Added", Toast.LENGTH_LONG).show();
            u.displayMessage(context, "Upload Successfully Completed");

            // Clear shared preferences
            SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("ScanSist", 0);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();   //its clear all data.
            editor.apply();  //Don't forgot to commit  SharedPreferences.
            u.scans.clear();
            u.scanAdapter.notifyDataSetChanged();
            //resume(vb);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 3s = 3000ms
                    onBackPressed();
                }
            }, 3000);
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
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
        try {
            unregisterReceiver(networkStateReceiver);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver networkStateReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
                SharedPreferences sharedPref= getApplicationContext().getSharedPreferences("ScanSist",MODE_PRIVATE);
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


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    /**
     * Check if the device's camera has a Flashlight.
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        u.setHome(menu.findItem(R.id.action_home));
        u.setBarCode(menu.findItem(R.id.action_barcode));
        u.setFlash(menu.findItem(R.id.action_flash));

        u.changeMenuItemState(u.getHome(),true,true,true);
        u.changeMenuItemState(u.getBarCode(),true,true,true);
        u.changeMenuItemState(u.getFlash(),true,hasFlash(),true);

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
                nextScreen = new Intent(ScanGoodsForRackLocationRadioOneActivity.this, SettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(nextScreen);
                break;
            case R.id.action_about:
                nextScreen = new Intent(ScanGoodsForRackLocationRadioOneActivity.this, AboutActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(nextScreen);
                break;
            case R.id.action_licence:
                nextScreen = new Intent(ScanGoodsForRackLocationRadioOneActivity.this, LicenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(nextScreen);
                break;
            case R.id.action_home:
                onBackPressed();
                return true;
            case R.id.action_barcode:

                View v = findViewById(R.id.barcode_scanner);
                if (b.getTitle().equals(getString(R.string.scan_resume))) {
                    b.setTitle(R.string.scan_pause);
                    u.changeMenuItemState(b,true,true ,true);
                    resume(v);
                } else {
                    b.setTitle(R.string.scan_resume);
                    u.changeMenuItemState(b,true,true ,false);
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
        bundle.putBoolean("onBackPressed",true);

        Intent previousScreen = new Intent(ScanGoodsForRackLocationRadioOneActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
}