package com.renturapp.scansist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScanFinish extends Activity {

  //public MenuItem mFlash;
  //public MenuItem mBarCode;
  private Utility u;

  private static final String TAG = ScanFinish.class.getSimpleName();


  private Boolean initClause;
  private Spinner spnClause;
  private Button btnNext;
  private int scanCount;
  //private MainActivity mA;
  private Context context;

  private String mTrunk,mManifestDate,mDirection,mStatus;

  private void setNextFinish(Boolean enabled){
    if(enabled) {
      btnNext.setClickable(true);
      btnNext.setAlpha(1f);
    } else {
      btnNext.setClickable(false);
      btnNext.setAlpha(0.5f);
    }
  }
  private void setSpnClause(int clauseID) {
    for (int i = 0;i<=u.clauseAdapter.getCount();i++) {
      Clause c = (Clause)spnClause.getItemAtPosition(i);
      if (c != null && clauseID == c.clauseID) {
        spnClause.setSelection(i);
        break;
      }
    }
  }
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_scan);

    context = ScanFinish.this;

    MyAsyncBus.getInstance().register(this);

    u = (Utility)getApplicationContext();  //mA = (MainActivity)context;

    btnNext = (Button)findViewById(R.id.btnNext);
    btnNext.setText("Finish");
    //lblScan = (TextView)findViewById(R.id.lblScan);
    scanCount = 0;
    // Create object of SharedPreferences.
    SharedPreferences sharedPref= getApplicationContext().getSharedPreferences("ScanSist",MODE_PRIVATE);

    /* From intent or Shared Preferences*/
    Intent intent = getIntent();
    // getting attached intent data
    Boolean previousPressed  = intent.getBooleanExtra("onBackPressed",false);
    /* Called when the activity is first created. */

    Integer status = intent.getIntExtra("status",-1);
    if (status!=-1) {
      setTitleCode(status);
    } else {
      status = sharedPref.getInt("status", -1);
      if (status != -1) {
        setTitleCode(status);
      }
    }

    int trunkNumber = intent.getIntExtra("trunkNumber", -1);
    if (trunkNumber!=-1) {
      ((TextView) findViewById(R.id.lblScanTrunk)).setText("Trunk: " + Integer.toString(trunkNumber));

    } else {
      trunkNumber = sharedPref.getInt("trunkNumber", 0);
      ((TextView) findViewById(R.id.lblScanTrunk)).setText("Trunk: " + Integer.toString(trunkNumber));
    }
    mTrunk = String.format("%02d",trunkNumber);

    String scanDateTime = intent.getStringExtra("scanDateTime");

    if (!scanDateTime.isEmpty()) {
      setScanDate(scanDateTime);
    } else {
      scanDateTime = sharedPref.getString("scanDateTime", "NothingFound");
      if (scanDateTime != "NothingFound"){
        setScanDate(scanDateTime);
      }
    }
    mManifestDate = scanDateTime.substring(0,10);
    /* End of setup             */

    String scansJson = sharedPref.getString("scans", "NothingFound");

    if (scansJson != "NothingFound") {
      u.setupScans(scansJson);
      setNextFinish(true);
    } else {
      setNextFinish(false);
    }
    ListView lv = (ListView)findViewById(R.id.scanList);

    lv.setAdapter(u.scanAdapter);

  }
  private void setTitleCode(int status){
    TextView t = (TextView)findViewById(R.id.lblTitle);

    switch (status) {

      case 0:
        t.setText(getString(R.string.from_hub));
        mDirection = getString(R.string.from_hub_direction);
        mStatus = getString(R.string.from_hub_status);
        break;
      case 1:
        t.setText(getString(R.string.to_hub));
        mDirection = getString(R.string.to_hub_direction);
        mStatus = getString(R.string.to_hub_status);
        break;
      case 2:
        t.setText(getString(R.string.onto_delivery));
        mDirection = getString(R.string.onto_delivery_direction);
        mStatus = getString(R.string.onto_delivery_status);
        break;
      default:
        t.setText(getString(R.string.from_hub));
        mDirection = getString(R.string.from_hub_direction);
        mStatus = getString(R.string.from_hub_status);
        break;
    }

  }
  public void uploadScans(){
    //
    u.displayMessage(context, "ScanSist™ Upload Activated\nPlease Wait.");
    String s = u.saveScans();


    //hh 12hour format - HH 24 hr format
    SimpleDateFormat scan_sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.UK);
    String uploadDateTime = scan_sdf.format(new Date());

    String d = "/demo/users/scansist/Upload_099" + uploadDateTime +"001.xml";

    new ScanFTPFileUploadTask().execute("www.movesist.com",
            "clients",
            "wdrcv227qt",
            s,
            d,
            mDirection,
            mStatus,
            mTrunk,
            mManifestDate);

  }
  @Subscribe
  public void onAsyncScanFTPFileUploadTaskResultEvent(ScanFTPFileUploadTaskResultEvent event) {

    if (!event.getResult()) {
      u.displayMessage(context, "Upload Failed.");
      //onBackPressed();
      //return;Warning:(217, 13) 'return' is unnecessary as the last statement in a 'void' method
    } else {
      u.displayMessage(context, "Upload Successfully Completed.");
    }
  }
  private void setScanDate(String d) {
    try{
      SimpleDateFormat scan_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
      Date date = scan_sdf.parse(d);
      String myFormat = "dd/MM/yy"; //In which you need put here
      SimpleDateFormat sdf_date = new SimpleDateFormat(myFormat, Locale.UK);
      ((TextView)findViewById(R.id.lblScanDate)).setText(sdf_date.format(date));
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  // very important when rotating !!! http://stackoverflow.com/questions/456211/activity-restart-on-rotation-android
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
  }

  /**
   * Check if the device's camera has a Flashlight.
   * @return true if there is Flashlight, otherwise false.
   */
  public void onWizardButtonClicked(View v) {
    // Check which radio button was clicked
    switch(v.getId()) {
      case R.id.btnPrevious:
        onBackPressed();
        break;
      case R.id.btnNext:
        u.messageBox(context,true,false);
        break;
      case R.id.btnCancel:
        u.messageBox(context,false,false);
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
    u.changeMenuItemState(u.getBarCode(),false,false,false);
    u.changeMenuItemState(u.getFlash(),false,false,false);

    return true;
  }
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    u.hideKeyboard(context);
    MenuItem h = u.getHome();
    MenuItem f = u.getFlash();
    MenuItem b = u.getBarCode();

    switch (item.getItemId()) {
      case R.id.action_settings:
        // User chose the "Settings" item, show the app settings UI...
        return true;
      case R.id.action_about:
        // User chose the "Settings" item, show the app settings UI...
        return true;
      case R.id.action_home:
        // User choose the "Setup home Option" action, go to home screen
        //changeMenuItemState(h, false, true, false);
        //Intent homeScreen = new Intent(ScanActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //startActivity(homeScreen);
        //finish();

        onBackPressed();
        return true;
      case R.id.action_barcode:

        //Not visible

        return true;
      case R.id.action_flash:

        //Not visible

        return true;
      default:
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onBackPressed() {
    u.saveScans();
    Bundle bundle = new Bundle();
    bundle.putBoolean("onBackPressed",true);
    // Put your own code here which you want to run on back button click.
    Intent previousScreen = new Intent(ScanFinish.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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