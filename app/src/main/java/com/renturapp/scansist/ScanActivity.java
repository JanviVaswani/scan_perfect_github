package com.renturapp.scansist;

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
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;

import android.widget.Spinner;
import android.widget.TextView;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.renturapp.scansist.MainActivity.urlExtension;

public class ScanActivity extends Activity implements
        DecoratedBarcodeView.TorchListener {

  //public MenuItem mFlash;
  //public MenuItem mBarCode;
  private Utility u;

  private static final String TAG = ScanActivity.class.getSimpleName();
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

  private String mRack,mManifestDate,mDirection,mStatus;

  private BarcodeCallback callback = new BarcodeCallback() {
    @Override
    public void barcodeResult(BarcodeResult result) {


      if (inValidManifestDate()) {
        u.messageBox(context, 3, false);
        beepManager.playBeepSoundAndVibrate();
        onPause();
        return;
      }

      if (result.getText() == null) {
        // Prevent nulls
        return;
      }

      if (result.getText().equals(lastText)) {
        // Prevent duplicate scans
        String stringCount = Integer.toString(scanCount);

        if (stringCount.equals("0") || stringCount.length()>1 && stringCount.substring(stringCount.length() - 1).equals("0")) {
          if (stringCount.equals("0")) {
            u.displayMessage(context,"ScanSist™\nBarcode [" + result.getText() + "]\nAlready Scanned!");
          }
          beepManager.playBeepSoundAndVibrate();
        }
        scanCount++;
        updateScanInfo(scanCount);
      } else {
        lastText = result.getText();
        if (lastText.length() == 15) {
          Spinner cs = (Spinner)findViewById(R.id.spnClause);
          cs.getSelectedView().setEnabled(true);
          cs.setEnabled(true);
          cs.setAlpha(1f);
          setNextFinish(u.isOnline());
          //hh 12hour format - HH 24 hr format
          SimpleDateFormat scan_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
          String scanDateTime = scan_sdf.format(new Date());

           if (u.scanAdapter.getCount() == 0) {
            beepManager.playBeepSoundAndVibrate();
            saveScan(scanDateTime);
           } else {
            //has it already been scaned!
            boolean alreadyScanned = false;
            for (Scan s : u.scans) {
              if (s.scanBarCode.equals(lastText)) {
                alreadyScanned = true;
                setSpnClause(s.clauseID);
                barcodeView.setStatusText(result.getText());
                break;
              }
            }
            if (alreadyScanned) {
              //title.setTextColor(Color.RED);
              scanCount = 0;
              updateScanInfo(scanCount);
              u.displayMessage(context,"ScanSist™\nBarcode [" + result.getText() + "]\nAlready Scanned!");
              return;
            } else {
              beepManager.playBeepSoundAndVibrate();
              //title.setTextColor(Color.BLACK);
              setSpnClause(0);
              saveScan(scanDateTime);
            }
          }
        } else {
          beepManager.playBeepSoundAndVibrate();
          u.displayMessage(context, "ScanSist™\nBarcode [" + result.getText() + "]\nInvalid!");
        }
        barcodeView.setStatusText(result.getText());
        //Added preview of scanned barcodeicon
        //ImageView imageView = (ImageView) findViewById(R.id.barcodePreview);
        //imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));
      }
    }
    @Override
    public void possibleResultPoints(List<ResultPoint> resultPoints) {
    }
  };

  private void saveScan(String scanDateTime) {

    u.scans.add(new Scan(u.scans.size()+1, 0, "    ", lastText, scanDateTime));
    u.scanAdapter.notifyDataSetChanged();
    u.saveScans();
    scanCount = 0;
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
  private void setSpnClause(int clauseID) {
    for (int i = 0;i<=u.clauseAdapter.getCount();i++) {
      Clause c = (Clause)spnClause.getItemAtPosition(i);
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

    context = ScanActivity.this;

    MyAsyncBus.getInstance().register(this);

    u = (Utility)getApplicationContext();  //mA = (MainActivity)context;

    btnNext = (Button)findViewById(R.id.btnNext);
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

    if (rackID!=-1) {
      String rackDescription = intent.getStringExtra("rackDescription");
      ((TextView) findViewById(R.id.lblScanRack)).setText(rackDescription);

    } else {
      rackID = sharedPref.getInt("rackID", 0);
      ((TextView) findViewById(R.id.lblScanRack)).setText("Rack: " + rackID);
    }
    mRack = String.format(Locale.UK, "%02d",rackID);

    String scanDateTime = intent.getStringExtra("scanDateTime");

    if (!scanDateTime.isEmpty()) {
      setScanDate(scanDateTime);
    } else {
      scanDateTime = sharedPref.getString("scanDateTime", "NothingFound");
      if (!scanDateTime.equals("NothingFound")){
        setScanDate(scanDateTime);
      }
    }
    mManifestDate = scanDateTime.substring(0,10);
    /* End of setup             */

    setNextFinish(u.HasScans(sharedPref) && u.isOnline() );

    spnClause= (Spinner) findViewById(R.id.spnClause);
    spnClause.setVisibility(View.VISIBLE);
    spnClause.setAdapter(u.clauseAdapter);
    initClause = true;//stop initialisation firing
    spnClause.setOnItemSelectedListener(
            new AdapterView.OnItemSelectedListener() {
              @Override
              public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //setBtnNextEnable();
                if (!initClause) {
                  Clause c = (Clause) u.clauseAdapter.getItem(position);
                  for (Scan s : u.scans) {
                    if (s.scanBarCode.equals(lastText) && s.clauseID != c.clauseID) {
                      s.clauseID = c.clauseID;
                      s.clauseCode = c.clauseCode;
                      u.scanAdapter.notifyDataSetChanged();
                      u.displayMessage(context, "ScanSist™\nBarcode [" + lastText + "]\nClaused - " + c.clauseDescription);
                      break;
                    }
                  }
                } else {
                  initClause = false;
                }
              }
              @Override
              public void onNothingSelected(AdapterView<?> arg0) {

              }

            });
    spnClause.setEnabled(false);
    spnClause.setEnabled(false);
    spnClause.setAlpha(0.5f);
    barcodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_scanner);
    //Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
    //barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
    barcodeView.decodeContinuous(callback);

    beepManager = new BeepManager(this);

    barcodeView.setTorchListener(this);
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
  public void uploadScans(){
    //
    u.displayMessage(context, "ScanSist™ Upload Activated\nPlease Wait.");
    String s = u.saveScans();


    //hh 12hour format - HH 24 hr format
    SimpleDateFormat scan_sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.UK);
    String uploadDateTime = scan_sdf.format(new Date());



    String depotNumber   = PreferenceManager.getDefaultSharedPreferences(context).getString("DepotNumber", "NothingFound");
    String scanSistCode =  String.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getInt("ScanSistCode", 0));

    String url      = "www.movesist" + urlExtension;
    String username = "clients";
    String userpass = "wdrcv227qt";
    String d = "/demo/users/scansist/Upload_" + depotNumber + "_" + uploadDateTime +"-001.xml";
    if (!depotNumber.equals("099")) {
      url      = "www.hazchemonline.com";
      username = "haz" + depotNumber;
      userpass = "haz" + depotNumber;
      d = "Upload_" + depotNumber + "_" + uploadDateTime +"-001.xml";
    }
      new ScanFTPFileUploadTask().execute(url,
        username,
        userpass,
        s,
        d,
        mDirection,
        mStatus,
        mRack,
        mManifestDate,
        depotNumber,
        scanSistCode);

  }
  @Subscribe
  public void onAsyncScanFTPFileUploadTaskResultEvent(ScanFTPFileUploadTaskResultEvent event) {
    View vb = findViewById(R.id.barcode_scanner);
    if (!event.getResult()) {
      u.displayMessage(context, "Upload Failed.");

      resume(vb);
      //onBackPressed();
      //return;Warning:(217, 13) 'return' is unnecessary as the last statement in a 'void' method
    } else {
      u.displayMessage(context, "Upload Successfully Completed.");
      // Clear shared preferences
      SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("ScanSist", 0);
      SharedPreferences.Editor editor = sharedPref.edit();
      editor.clear();   //its clear all data.
      editor.apply();  //Don't forgot to commit  SharedPreferences.
      u.scans.clear();
      u.scanAdapter.notifyDataSetChanged();
      resume(vb);
      onBackPressed();
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

  @Override
  protected void onResume() {
    super.onResume();
    barcodeView.resume();
    try {
      registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
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

  public void triggerScan(View view) {
    barcodeView.decodeSingle(callback);
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
  private Boolean inValidManifestDate() {

    /* Compare Dates of scans*/
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
      Date date = sdf.parse(mManifestDate);
      Date today = new Date();
      int bstOffSet = 0;
      if (today.toString().contains("BST") || today.toString().contains("GMT+01:00") ) {
        bstOffSet= 36000000;
      }
      int difference=
        ((int)(((date.getTime()+bstOffSet)/(24*60*60*1000))
          -(int)(today.getTime()/(24*60*60*1000))));

      if (difference<0) {
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
    switch(v.getId()) {
      case R.id.btnPrevious:
        onBackPressed();
        break;
      case R.id.btnNext:
        if (inValidManifestDate()) {
          u.messageBox(context,3,false);
          beepManager.playBeepSoundAndVibrate();
        } else {
          u.messageBox(context, 1, false);
        }
        break;
      case R.id.btnCancel:
        u.messageBox(context,2,false);
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
        nextScreen = new Intent(ScanActivity.this, SettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //nextScreen.putExtras(bundle);
        startActivity(nextScreen);
        //finish();
        break;
      case R.id.action_about:
        nextScreen = new Intent(ScanActivity.this, AboutActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //nextScreen.putExtras(bundle);
        startActivity(nextScreen);
        //finish();
        break;
      case R.id.action_licence:
        nextScreen = new Intent(ScanActivity.this, LicenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
    u.saveScans();
    Bundle bundle = new Bundle();
    bundle.putBoolean("onBackPressed",true);
    // Put your own code here which you want to run on back button click.
    Intent previousScreen = new Intent(ScanActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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