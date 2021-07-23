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
import com.renturapp.scansist.AsynTask.GetPicklistGoodsAsyncTask;
import com.renturapp.scansist.AsynTask.GetPicklistGoodsAsyncTaskResultEvent;
import com.renturapp.scansist.LicenceActivity;
import com.renturapp.scansist.MainActivity;
import com.renturapp.scansist.Model.ScanGoodsModel;
import com.renturapp.scansist.MyAsyncBus;
import com.renturapp.scansist.R;
import com.renturapp.scansist.Model.Scan;
import com.renturapp.scansist.AsynTask.ScanGoodsInLocationTask;
import com.renturapp.scansist.AsynTask.ScanGoodsInLocationTaskResultEvent;
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

public class ScanPickListGoodsActivity extends Activity implements
        DecoratedBarcodeView.TorchListener {

    private Utility u;

    private static final String TAG = ScanPickListGoodsActivity.class.getSimpleName();
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
    private int picklistId;
    private String picklistdesc;

    List<ScanGoodsModel> globalDataPicklistGoods = Collections.emptyList();
    ScanGoodsModel scangoodsoutObj=null;

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

            //Scan Picklist Goods and Validate Barcode

            if(result.getText().startsWith("00") && result.getText().length()==15) {

                for (ScanGoodsModel singlegood : globalDataPicklistGoods) {
                    if ((singlegood.cCGoodBarcode).equals(result.getText())) {
                        barcodeexists = true;
                        //Set an object of ScanGoodsModel for Scanned Goods Out Class
                        scangoodsoutObj = new ScanGoodsModel();
                        if (singlegood.isGoodScanned == "N") {
                            scangoodsoutObj.cCGoodID = singlegood.cCGoodID;
                            scangoodsoutObj.cCGoodDescription = singlegood.cCGoodDescription;
                            scangoodsoutObj.cCGoodBarcode = singlegood.cCGoodBarcode;
                            scangoodsoutObj.companyID = singlegood.companyID;
                            scangoodsoutObj.cCGoodRackID = singlegood.cCGoodRackID;
                            scangoodsoutObj.cCGoodStatus = singlegood.cCGoodStatus;
                            scangoodsoutObj.isGoodScanned = "S";
                            singlegood.isGoodScanned = "S";
                        }
                    }
                }

                u.scanpicklistgoods = globalDataPicklistGoods;

                if (!barcodeexists) {
                    // Too soon after the last barcode - ignore.
                    beepManager.playBeepSoundAndVibrate();
                    lastTimestamp = System.currentTimeMillis();
                    invalidBarcodeDialog(result.getText());

                } else {
                    if (result.getText().equals(lastText)) {
                        beepManager.playBeepSoundAndVibrate();
                        Toast.makeText(ScanPickListGoodsActivity.this, "Barcode Already Exists", Toast.LENGTH_SHORT).show();
                        lastTimestamp = System.currentTimeMillis();
                    } else {
                        lastText = result.getText();
                        setNextFinish(u.isOnline());
                        //hh 12hour format - HH 24 hr format
                        SimpleDateFormat scan_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
                        String scanDateTime = scan_sdf.format(new Date());


                        lastTimestamp = System.currentTimeMillis();

                        if (u.scanAdapter.getCount() == 0) {
                            saveScan(scanDateTime);

                            //Set an list of ScanGoodsModel for Scanned Goods Out Class
                            u.scannedgoodoutList.add(scangoodsoutObj);
                        } else {
                            //has it already been scaned!
                            boolean alreadyScanned = false;
                            for (Scan s : u.scans) {
                                if (s.scanBarCode.equals(lastText)) {
                                    alreadyScanned = true;
                                    barcodeView.setStatusText(result.getText());
                                    break;
                                }
                            }
                            if (alreadyScanned) {

                            } else {
                                saveScan(scanDateTime);
                                //Set an list of ScanGoodsModel for Scanned Goods Out Class
                                u.scannedgoodoutList.add(scangoodsoutObj);
                            }
                        }
                        barcodeView.setStatusText(result.getText());
                        lastText = result.getText();
                    }
                }
            }
            else{
                // Too soon after the last barcode - ignore.
                Toast.makeText(ScanPickListGoodsActivity.this, "Not a Goods Barcode", Toast.LENGTH_SHORT).show();
                beepManager.playBeepSoundAndVibrate();
                lastTimestamp = System.currentTimeMillis();
            }

            barcodeView.setStatusText(result.getText());
            scanCount++;
            updateScanInfo(scanCount);

            //Enable Finish Button if all goods are scanned
            allGoodsScanned();
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
        this.setContentView(R.layout.activity_scan_picklist_goods);

        context = ScanPickListGoodsActivity.this;

        MyAsyncBus.getInstance().register(this);

        u = (Utility)getApplicationContext();  //mA = (MainActivity)context;

        btnNext = (Button)findViewById(R.id.btnNext);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnPrev = (Button)findViewById(R.id.btnPrevious);

        btnNext.setText("Finish");
        btnNext.setEnabled(false);

        lblScan = (TextView)findViewById(R.id.lblScan);
        scanCount = 0;
        // Create object of SharedPreferences.
        SharedPreferences sharedPref= getApplicationContext().getSharedPreferences("ScanSist",MODE_PRIVATE);

        /* From intent or Shared Preferences*/
        Intent intent = getIntent();
        Boolean previousPressed  = intent.getBooleanExtra("onBackPressed",false);
        picklistId=intent.getIntExtra("picklistid",0);
        picklistdesc=intent.getStringExtra("picklistdesc");
        //Set picklist Description
        ((TextView) findViewById(R.id.lblScanRack)).setText(picklistdesc);

        //Load PickLists Goods
        if(u.scanpicklistgoods.size()>0){
            globalDataPicklistGoods=u.scanpicklistgoods;
        }
        else{
            GetPicklistGoodsAsyncTask scanAsyncTaskPickListGoods  = new GetPicklistGoodsAsyncTask();
            scanAsyncTaskPickListGoods.execute("https://movesist.uk/data/ccgoods/?getType=4&CompanyID=" + u.RegCompanyId + "&CCGoodPickListID="+picklistId);
        }

        //Enable Finish Button if all goods are scanned
        allGoodsScanned();
        String scanDateTime = "";

        if (!scanDateTime.isEmpty()) {
        } else {
            scanDateTime = sharedPref.getString("scanDateTime", "NothingFound");
            if (!scanDateTime.equals("NothingFound")){
            }
        }
        mManifestDate = scanDateTime.substring(0,10);
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
    public void onAsyncGetPicklistGoodsAsyncTaskResultEvent(GetPicklistGoodsAsyncTaskResultEvent event) {
        List<ScanGoodsModel> goodsdata=new ArrayList<>();
        try {
            JSONArray jArray = new JSONArray(event.getResult());

            // Extract data from json and store into ArrayList as class objects
            for(int i = 0; i < jArray.length(); i++){
                JSONObject json_data = jArray.getJSONObject(i);

                ScanGoodsModel goodsModel = new ScanGoodsModel();

                goodsModel.cCGoodID = json_data.getInt("CCGoodID"); //***NAME FROM API URL***
                goodsModel.cCGoodQuantity = json_data.getInt("CCGoodQuantity");
                goodsModel.buyerStreet = json_data.getString("BuyerStreet");
                goodsModel.sellerStreet= json_data.getString("SellerStreet");
                goodsModel.rackDescription=json_data.getString("RackDescription");
                goodsModel.cCGoodDescription=json_data.getString("CCGoodDescription");
                goodsModel.cCGoodQuantity=json_data.getInt("CCGoodQuantity");;
                goodsModel.cCGoodPackagingType=json_data.getString("CCGoodPackagingType");
                goodsModel.cCGoodGrossWeight=json_data.getDouble("CCGoodGrossWeight");
                goodsModel.cCGoodNetWeight=json_data.getDouble("CCGoodNetWeight");
                goodsModel.cCGoodLength=json_data.getInt("CCGoodLength");
                goodsModel.cCGoodHeight=json_data.getInt("CCGoodHeight");
                goodsModel.cCGoodWidth=json_data.getInt("CCGoodWidth");
                goodsModel.cCGoodValue=json_data.getInt("CCGoodValue");
                goodsModel.cCGoodBarcode=json_data.getString("CCGoodBarcode");

                goodsModel.companyID=json_data.getInt("CompanyID");
                goodsModel.cCGoodRackID=json_data.getInt("CCGoodRackID");
                goodsModel.cCGoodPickListID=json_data.getInt("CCGoodPickListID");
                //goodsModel.cCGoodStatus=json_data.getInt("CCGoodStatus");
                //goodsModel.cCGoodStatus=(json_data.getJSONObject("CCGoodStatus")==null)?0:json_data.getInt("CCGoodStatus");

                goodsdata.add(goodsModel);
            }
            globalDataPicklistGoods = goodsdata;
        } catch (JSONException e) {
            Toast.makeText(ScanPickListGoodsActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    //To check if all goods are scanned
    private  void allGoodsScanned(){
        int totalgoodscount=u.scanpicklistgoods.size();
        int scannedgoods=u.scans.size();

        if(scannedgoods==totalgoodscount){
            btnNext.setEnabled(true);
        }
    }

    //***To upload Scanned Goods Out***
    public void uploadScans(){
        //Start Custom Progress Dialog
        progressDialog = new ProgressDialog(ScanPickListGoodsActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progess_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        //u.displayMessage(context, "ScanSist™ Upload Activated\nPlease Wait.");
        new ScanGoodsInLocationTask(ScanPickListGoodsActivity.this).execute(u.scannedgoodoutList);
    }

    @Subscribe
    public void onAsyncScanGoodsInLocationTaskResultEvent(ScanGoodsInLocationTaskResultEvent event) {
        View vb = findViewById(R.id.barcode_scanner);

        //Dismiss Custom Progress Dialog
        progressDialog.dismiss();

        if (event.getResult()=="Update Error") {
            u.displayMessage(context, "Upload Failed");
            resume(vb);
        } else {

            u.displayMessage(context, "Upload Successfully Completed");

            //Clear Lists
            //u.scanpicklistgoods=Collections.EMPTY_LIST;
            u.scanpicklistgoods=new ArrayList<>();
            //u.scannedgoodoutList=Collections.EMPTY_LIST;
            u.scannedgoodoutList=new ArrayList<>();

            // Clear shared preferences
            SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("ScanSist", 0);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();   //its clear all data.
            editor.apply();  //Don't forgot to commit  SharedPreferences.
            u.scans.clear();
            u.scanAdapter.notifyDataSetChanged();

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 3s = 3000ms
                    onBackPressed();
                }
            }, 3000);
            //onBackPressed();
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

    public void onWizardButtonClicked(View v) {
        // Check which radio button was clicked
        View vb = findViewById(R.id.barcode_scanner);
        pause(vb);
        switch(v.getId()) {
            case R.id.btnPrevious:
                onBackPressed();
                break;
            case R.id.btnNext:
                messageBox(context, 1, false);
                break;
            case R.id.btnCancel:
                messageBox(context,2,false);
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
                nextScreen = new Intent(ScanPickListGoodsActivity.this, SettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(nextScreen);
                break;
            case R.id.action_about:
                nextScreen = new Intent(ScanPickListGoodsActivity.this, AboutActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(nextScreen);
                break;
            case R.id.action_licence:
                nextScreen = new Intent(ScanPickListGoodsActivity.this, LicenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(nextScreen);
                break;
            case R.id.action_home:
                // User choose the "Setup home Option" action, go to home screen
                //changeMenuItemState(h, false, true, false);
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
        u.saveScans();
        Bundle bundle = new Bundle();
        bundle.putBoolean("onBackPressed",true);

        if(u.scanpicklistgoods.isEmpty()){
            // Put your own code here which you want to run on back button click.
            Intent previousScreen = new Intent(ScanPickListGoodsActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            previousScreen.putExtras(bundle);
            startActivity(previousScreen);
            finish();
            super.onBackPressed();
        }else{
            bundle.putInt("status", 3);
            bundle.putInt("picklistID", picklistId);
            bundle.putString("picklistDescription", picklistdesc);

            Intent previousScreen = new Intent(ScanPickListGoodsActivity.this, GetCCGoodsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            previousScreen.putExtras(bundle);
            startActivity(previousScreen);
            finish();
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        MyAsyncBus.getInstance().unregister(this);
        super.onDestroy();
    }

    private void invalidBarcodeDialog(String barcode) {
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setMessage(barcode +" barcode Does Not Exist");
        dialog.setTitle("Invalid Barcode!");
        dialog.setPositiveButton("Confirm to Continue",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog=dialog.create();
        alertDialog.show();
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
                builder.setMessage(R.string.confirm_message_finish);
            } else if (f == 2) {
                builder.setMessage(R.string.confirm_message_cancel);
            } else {
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
                    ((ScanPickListGoodsActivity) c).uploadScans();
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
                    View vb = ((ScanPickListGoodsActivity) c).findViewById(R.id.barcode_scanner);
                    ((ScanPickListGoodsActivity) c).resume(vb);
                    if (f ==3) {
                        ((ScanPickListGoodsActivity) c).onResume();
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
}