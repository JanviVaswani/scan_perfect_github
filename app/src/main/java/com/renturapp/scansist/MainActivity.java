package com.renturapp.scansist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static java.lang.Integer.parseInt;

public class MainActivity extends Activity {


    //Registration
  private ProgressDialog progressDialog;
  private Context context;

  public static String deviceUu;
  public static String deviceFilePath;
  public static String deviceFullPath;
  public static String androidId;
  public static String regdatetime;


  //private String deviceUu;
 // private String deviceFilePath;
  private String tmLineNumber;
  private String tmNetworkOperator;
  private String tmNetworkOperatorName;
  private String tmSimOperator;
  private String tmSimOperatorName;
  private String tmCellLocation;
  private String tmDevice;
  private String tmSerial;
  //private String androidId;
 // private String regdatetime;
  String licencedatetime;
  private String downloadtrunkdata = "";
  private TelephonyManager tm = null;
  private static boolean uploadregfile = false;
  public  static String mCompanyID = "2";
  public  static String mcompany = "demo";
  public  static String urlExtension = ".com";
  private static String liveVersion;
  private static String installedVersion;
  private static String releaseDownloadUrl = "https://www.movesist" + urlExtension + "/clients/" + mcompany + "/downloads/scansist/" + mcompany + "_scansist_";
  static boolean localData = false;
  private Utility u;
  private Calendar myCalendar;
  private TextView dateText;
  private TextView lblDateText;
  private DatePickerDialog.OnDateSetListener date;
  private Date mDate;
  private Spinner spnTrunk;
  private RadioGroup rBg;

  private JSONArray ta;

  private Button btnNext;
  public  static String notRegistered = "Not a registered user.\n\nPlease contact sales on 01788 523800 to register your application.";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MyAsyncBus.getInstance().register(this);
    setContentView(R.layout.activity_main);

    uploadregfile = false;

    context = MainActivity.this;
    u = (Utility) getApplicationContext();
    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    String companyID = PreferenceManager.getDefaultSharedPreferences(context).getString("CompanyID", "NothingFound");
    String company = PreferenceManager.getDefaultSharedPreferences(context).getString("CompanyUrl", "NothingFound");
    String depotNumber = PreferenceManager.getDefaultSharedPreferences(context).getString("DepotNumber", "NothingFound");
    String companyName = PreferenceManager.getDefaultSharedPreferences(context).getString("CompanyName", "NothingFound");

    if (companyID.equals("NothingFound") ||
      company.equals("NothingFound") ||
      depotNumber.equals("NothingFound") ||
      companyName.equals("NothingFound")) {

      Intent registerScanSist = new Intent(MainActivity.this, RegisterScanSistActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      //nextScreen.putExtras(bundle);
      startActivity(registerScanSist);
    } else {

      btnNext = (Button) findViewById(R.id.btnNext);
      btnNext.setClickable(false);
      btnNext.setAlpha(0.5f);

      rBg = (RadioGroup) findViewById(R.id.rBtnG);

      myCalendar = Calendar.getInstance();

      dateText = (TextView) findViewById(R.id.txtManifestDate);
      lblDateText =  (TextView) findViewById(R.id.lblManifestDate);
      dateText.setPaintFlags(dateText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
      dateText.setHintTextColor(getResources().getColor(R.color.blue));

      date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
          // TODO Auto-generated method stub
          myCalendar.set(Calendar.YEAR, year);
          myCalendar.set(Calendar.MONTH, monthOfYear);
          myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
          updateLabel();
        }

      };

      dateText.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          // TODO Auto-generated method stub
          new DatePickerDialog(context, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT, date, myCalendar
            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
            myCalendar.get(Calendar.DAY_OF_MONTH)).show();

          if (mDate != null) {
            myCalendar.setTime(mDate);
          }
          u.hideKeyboard(context);
        }
      });

      mCompanyID = companyID;
      mcompany   = company;

      uploadregfile = true;
      SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);
      Boolean hasScansToProcess = u.HasScans(sharedPref);

      if (!hasScansToProcess && !u.isOnline()) {
        u.displayMessage(context, "No Internet connection.\n\nLicence cannot be verified.");
        onBackPressed();
        return;
      } else {
        //New instance is set to null
        progressDialog = null;
        //Debug Mode no Licence check
        Boolean debugMode = false;
        if (debugMode) {
          u.displayMessage(context, "ScanSist™ - Trial Application - Please wait.");
        }
        /*
         *
         *
         *   RegisterScanSist ScanSist App
         *
         *
         *                          */
        Intent intent = getIntent();

        // getting attached intent data
        Boolean previousPressed = intent.getBooleanExtra("onBackPressed", false);
        if (!localData) {
          downloadtrunkdata = "https://www.movesist" + urlExtension + "/data/trunks/?CompanyID=" + mCompanyID + "&getType=7&AndroidId=" + androidId;
        } else {
          downloadtrunkdata = "http://192.168.0.5/data/trunks/?CompanyID=" + mCompanyID + "&getType=7&AndroidId=" + androidId;
        }
        /* Called when the activity is first created. */
        if (!previousPressed && !hasScansToProcess) { // && !Utility.isActivityBackground()) {

          deviceUu = "scansist_" + mcompany + "_" + androidId;
          deviceFullPath = "https://www.movesist" + urlExtension + "/clients/" + mcompany + "/users/scansist/" + deviceUu + "_p.html";
          deviceFilePath = "/" + mcompany + "/users/scansist/" + deviceUu + "_p.html";
          String scanSistCode =  String.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getInt("ScanSistCode", MODE_PRIVATE));

          if (!u.isOnline()) {
            u.displayMessage(context, "No Internet connection.\n\nLicence cannot be verified.");
            MainActivity.super.onBackPressed();
          } else {
            //Check for the occurrence of the file that is shown in the saved registration key adding a '_p' will ensure it runs once registered
            String deviceRegFullPath = "https://www.movesist" + urlExtension + "/clients/" + mcompany + "/users/scansist/" + deviceUu + "_p.html";
            progressDialog = new ProgressDialog(context);
            progressDialog.setCustomTitle(u.setProgressTitle(context));
            progressDialog.setMessage("Checking Licence - v" + getVersionName(context) + "\n\n" + companyName + "\n\nDepot " + depotNumber + " Scanner ID: " + scanSistCode + "\n\n         Please wait.");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
            new CheckLicenceTask().execute(deviceRegFullPath);
            uploadregfile = false;
          }
        } else {
          String trunksJson = sharedPref.getString("trunks", "NothingFound");
          try {
            ta = new JSONArray(trunksJson);
          } catch (JSONException e) {
            e.printStackTrace();
          }
          if (!trunksJson.equals("NothingFound") && !u.isOnline()) {
            if (u.trunkAdapter == null) {
              u.trunkAdapter = new ListTrunkAdapter(u);
            }
            u.setupTrunks(trunksJson);
            setupTrunkSpinner(sharedPref);
          } else {
            //May have just finished so reload
            //ToDo This occurs when we select the radio buttons so not needed
            //ToDo but would occur if the radio button is already selected!
            if (ta!=null && ta.length() == 1) {
              new DownloadTrunkDataTask().execute(downloadtrunkdata);
            }
          }
        }
      }
      /*
       *
       *
       *     End of ScanSist registration checking
       *
       *
       *                                              */


      // Create object of SharedPreferences.
      //SharedPreferences sharedPref= getApplicationContext().getSharedPreferences("ScanSist",MODE_PRIVATE);
      String scanDateTime = sharedPref.getString("scanDateTime", "NothingFound");

      if (!scanDateTime.equals("NothingFound")) {
        SimpleDateFormat scan_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
        try {
          mDate = scan_sdf.parse(scanDateTime);
          String myFormat = "dd/MM/yy"; //In which you need put here
          SimpleDateFormat sdf_scan = new SimpleDateFormat(myFormat, Locale.UK);
          dateText.setText(sdf_scan.format(mDate));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      int status = sharedPref.getInt("status", -1);

      if (status != -1) {
        setRadioButton(status,sharedPref);
      }
      if (u.clauses.isEmpty()) {
        u.setupClauses(status);
      }

      //String scansJson = sharedPref.getString("scans", "NothingFound");

      //if (!scansJson.equals("NothingFound")) {
      //  u.setupScans(scansJson);
      //}

      setBtnNextEnable(sharedPref);

    }
  }
 // very important when rotating !!! http://stackoverflow.com/questions/456211/activity-restart-on-rotation-android
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
  }

  private void updateLabel() {
    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);
    String myFormat = "dd/MM/yy"; //In which you need put here
    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);

    mDate = myCalendar.getTime();
    dateText.setText(sdf.format(mDate));

    setBtnNextEnable(sharedPref);
  }

  public void onRadioButtonClicked(View v) {
    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);
    int trunkHub = hubStatus();
    setBtnNextEnable(sharedPref);
    u.setupClauses(trunkHub);
    new DownloadTrunkDataTask().execute(downloadtrunkdata + "&TrunkType=" + trunkHub);

  }

  private void enableRbg (Boolean enabled) {
    for (int i = 0; i < rBg.getChildCount(); i++) {
      rBg.getChildAt(i).setEnabled(enabled);
    }
  }
  private void enableScanSetup(Boolean enabled) {
    if (enabled) {
      if (lblDateText!=null) {
        lblDateText.setAlpha(1f);
      }
      if (dateText!=null) {
        dateText.setClickable(true);
        dateText.setEnabled(true);
        dateText.setAlpha(1f);
      }
      if (spnTrunk!=null) {
        spnTrunk.setClickable(true);
        spnTrunk.setEnabled(true);
        spnTrunk.setAlpha(1f);
      }
    } else {
      if (lblDateText!=null) {
        lblDateText.setAlpha(0.5f);
      }
      if (dateText!=null) {
        dateText.setClickable(false);
        dateText.setEnabled(false);
        dateText.setAlpha(0.33f);
      }
      if (spnTrunk!=null) {
        spnTrunk.setClickable(false);
        spnTrunk.setEnabled(false);
        spnTrunk.setAlpha(0.5f);
      }

    }
  }
  public void onWizardButtonClicked(View v) {

    switch (v.getId()) {
      case R.id.btnPrevious:
        onBackPressed();
        break;
      case R.id.btnNext:
        // Check which radio button was clicked
        selectPage(hubStatus());
        break;
      case R.id.btnCancel:
        u.messageBox(context, 2, false);
        break;
      default:
        throw new RuntimeException("Unknow button ID");

    }
  }

  private void setupTrunkSpinner(SharedPreferences sharedPref) {

    final SharedPreferences sp = sharedPref;
    spnTrunk = (Spinner) findViewById(R.id.spnTrunk);
    spnTrunk.setVisibility(View.VISIBLE);
    spnTrunk.setAdapter(u.trunkAdapter);

    spnTrunk.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            setBtnNextEnable(sp);
          }

          @Override
          public void onNothingSelected(AdapterView<?> arg0) {
          }
        });
    int trunkNumber = sharedPref.getInt("trunkNumber", 0);
    //ToDo The download list may not be in sync with the stored list or may be length of 1 (the default)
    if (u.trunkAdapter.getCount()>1) {
      for (int i = 0; i <= u.trunkAdapter.getCount(); i++) {
        Trunk t = (Trunk) spnTrunk.getItemAtPosition(i);
        if (t != null && trunkNumber == t.trunkNumber) {
          spnTrunk.setSelection(i);
          break;
        }
      }
    }
  }

  private int hubStatus() {

    int status;
    //RadioGroup rBg = (RadioGroup) findViewById(R.id.rBtnG);
    switch (rBg.getCheckedRadioButtonId()) {

      case R.id.rBtnFromHub:
        status = 0;
        break;
      case R.id.rBtnToHub:
        status = 1;
        break;
      case R.id.rBtnOntoDelivery:
        status = 2;
        break;
      default:
        status = -1;
        break;
    }
    return status;

  }

  private void setBtnNextEnable(SharedPreferences sharedPref) {

    TextView id = (TextView) findViewById(R.id.ID);

    int trunkNumber = 0;
    if (id != null) {
      trunkNumber = parseInt(id.getText().toString());
    }

    if (dateText.length() > 0 && trunkNumber > 0 && mDate != null && rBg.getCheckedRadioButtonId() != -1) {
      btnNext.setClickable(true);
      btnNext.setAlpha(1f);

    } else {
      btnNext.setClickable(false);
      btnNext.setAlpha(0.5f);
    }
    if (u.HasScans(sharedPref)) {
      enableScanSetup(false);
      enableRbg(false);
    } else {
      enableScanSetup(true);
      enableRbg(true);
    }
  }

  private void setRadioButton(Integer s,SharedPreferences sharedPref) {

    switch (s) {
      case 0:
        RadioButton fh = (RadioButton) findViewById(R.id.rBtnFromHub);
        fh.setChecked(true);
        break;
      case 1:
        RadioButton th = (RadioButton) findViewById(R.id.rBtnToHub);
        th.setChecked(true);
        break;
      case 2:
        RadioButton d = (RadioButton) findViewById(R.id.rBtnOntoDelivery);
        d.setChecked(true);
        break;
    }
    if (u.isOnline()) {
      new DownloadTrunkDataTask().execute(downloadtrunkdata + "&TrunkType=" + s);
    }
    setBtnNextEnable(sharedPref);
  }

  private void selectPage(Integer status) {

    Bundle bundle = new Bundle();
    bundle.putInt("status", status);
    Trunk trunk = (Trunk) spnTrunk.getSelectedItem();
    bundle.putInt("trunkNumber", trunk.trunkNumber);
    bundle.putString("trunkDescription", trunk.trunkDescription);

    if (mDate != null) {
      //hh 12hour format - HH 24 hr format
      SimpleDateFormat scan_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
      String scanDateTime = scan_sdf.format(mDate);
      bundle.putString("scanDateTime", scanDateTime);
    }

    Intent nextScreen = new Intent(MainActivity.this, ScanActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    nextScreen.putExtras(bundle);
    startActivity(nextScreen);
    finish();

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_menu, menu);

    //home
    u.setHome(menu.findItem(R.id.action_home));
    u.changeMenuItemState(u.getHome(), false, true, false);

    u.setBarCode(menu.findItem(R.id.action_barcode));
    u.changeMenuItemState(u.getBarCode(), true, false, true);

    u.setFlash(menu.findItem(R.id.action_flash));
    u.changeMenuItemState(u.getFlash(), true, false, true);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    u.hideKeyboard(context);
    //FragmentTransaction ft;
    int id = item.getItemId();
    Intent nextScreen;
    switch (id) {
      case R.id.action_settings:
        nextScreen = new Intent(MainActivity.this, SettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //nextScreen.putExtras(bundle);
        startActivity(nextScreen);
        //finish();
        break;

      case R.id.action_about:
        nextScreen = new Intent(MainActivity.this, AboutActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //nextScreen.putExtras(bundle);
        startActivity(nextScreen);
        //finish();
        break;


      case R.id.action_licence:
        nextScreen = new Intent(MainActivity.this, LicenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //nextScreen.putExtras(bundle);
        startActivity(nextScreen);
        //finish();
        break;
        case R.id.action_home:
        // User choose the "Setup home Option" action, go to home screen
        u.changeMenuItemState(u.getHome(), false, true, false);
        u.changeMenuItemState(u.getBarCode(), false, false, false);
        onBackPressed();
        break;
        //return true;
      case R.id.action_barcode:
        //return true;
        break;
      case R.id.action_flash:
        //return true;
        break;
      default:
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        //return super.onOptionsItemSelected(item);
        break;

    }

    return super.onOptionsItemSelected(item);

  }

  @Override
  public void onBackPressed() {
    MainActivity.super.onBackPressed();
  }

  @Override
  protected void onDestroy() {

    // Always unregister when an object no longer should be on the bus.
    MyAsyncBus.getInstance().unregister(this);
    super.onDestroy();
    int status;
    if (spnTrunk != null) {

      Trunk trunk = (Trunk) spnTrunk.getSelectedItem();

      // Create object of SharedPreferences.
      SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);
      //now get Editor
      SharedPreferences.Editor editor = sharedPref.edit();

      if (mDate != null) {
        //hh 12hour format - HH 24 hr format
        SimpleDateFormat scan_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
        String scanDateTime = scan_sdf.format(mDate);
        editor.putString("scanDateTime", scanDateTime);
      }
      editor.putInt("trunkNumber", trunk.trunkNumber);
      status = hubStatus();
      editor.putInt("status", status);
      editor.apply();
    }
  }

  @Subscribe
  public void onCheckLicenceTaskResultEvent(CheckLicenceTaskResultEvent event) {
    String vn = getVersionName(context);
    installedVersion = vn.replace(".","_");
    if (!uploadregfile) {
      if (!event.getResult()) {
        u.displayMessage(context, "Not a registered user.\n\nPlease contact sales on 01788 523800 to register your application.");
        delaydialogueClose(true);
      } else {
        //licence ok so download data if required
        //already called in setRadioButton
        //new DownloadTrunkDataTask().execute(downloadtrunkdata);
        //delaydialogueClose(false);
        //new CheckReleaseTask().execute(releaseDownloadUrl);
        new CheckReleaseTask().execute(releaseDownloadUrl +"version.html",installedVersion);
      }
    } else {
      new CheckReleaseTask().execute(releaseDownloadUrl +"version.html",installedVersion);
      //delaydialogueClose(false);
      //stopScan();
      //TODO must load register intent first and once its complete then upload file
      //Intent registerScanSist = new Intent(MainActivity.this, RegisterScanSistActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      //nextScreen.putExtras(bundle);
      //startActivity(registerScanSist);
    }
  }
  @Subscribe
  public void onCheckReleaseTaskResultEvent(CheckReleaseTaskResultEvent event){

    liveVersion  = event.getResult();
    if (liveVersion.length()> 0 && !installedVersion.equals(liveVersion) ) {
      if (!uploadregfile) {
        delaydialogueClose(false);
      }
      triggerUpdate(context);
    } else {
      if (!uploadregfile) {
        delaydialogueClose(false);
      } else {
        Intent registerScanSist = new Intent(MainActivity.this, RegisterScanSistActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //nextScreen.putExtras(bundle);
        startActivity(registerScanSist);
      }
      //Download not available but still need trunks!
      //new DownloadTrunkDataTask().execute(downloadtrunkdata);
    }
  }

  @Subscribe
  public void onAsyncFTPFileUploadTaskResultEvent(FTPFileUploadTaskResultEvent event) {

    if (!event.getResult()) {
      delaydialogueClose(true);
      u.displayMessage(context, "Registration Failed.");
    } else {
      //u.readTelephoneDetails();
      new RegisterScanSistTask().execute(u.tmSerial
        , u.tmLineNumber
        , u.tmNetworkOperator
        , u.tmNetworkOperatorName
        , u.tmSimOperator
        , u.tmSimOperatorName
        , u.tmCellLocation
        , u.tmDevice
        , androidId
        , deviceUu + ".html"
        , regdatetime
        , "false"
        , mCompanyID
        , "7"
        ,"https://www.movesist" + urlExtension +"/data/scansists/");
    }

  }

  @Subscribe
  public void onRegisterScanSistTaskResultEvent(RegisterScanSistTaskResultEvent event) {
    //Must use the default preference file!
    delaydialogueClose(false);
    if (event.getResult()) {
      u.displayMessage(context, "Registration Completed");
    } else {
      u.displayMessage(context, "Registration Completed\n\n" + "Warning - ScanSist™ not added to MoveSist™ database");
    }
    //licence ok so download data
    new DownloadDataTask().execute("https://www.movesist" + urlExtension + "/data/scansists/?CompanyID=" + mCompanyID + "&getType=3&AndroidId=" + androidId);
    //ToDo We cannot down Truck data until radio button is selected
    //new DownloadTrunkDataTask().execute(downloadtrunkdata);
  }

  @Subscribe
  public void onDownloadTaskResultEvent(DownloadDataTaskResultEvent event) {

    delaydialogueClose(false);
    if (event.getResult() != null) {
      SharedPreferences dp = PreferenceManager.getDefaultSharedPreferences(context);
      //now get the Editor
      SharedPreferences.Editor editor = dp.edit();
      //String depotNumber = "";
      int scanSistCode = 0;
      try {
        JSONArray data_array = new JSONArray(event.getResult());
        for (int i = 0; i < data_array.length(); i++) {
          JSONObject obj = new JSONObject(data_array.get(i).toString());
          //depotNumber = obj.getString("DepotNumber");
          scanSistCode = obj.getInt("ScanSistCode");
          break;
        }
        editor.putString("RegKey", deviceUu);
        //editor.putString("DepotNumber", depotNumber);
        editor.putInt("ScanSistCode", scanSistCode);
        //was added in 2.3, it commits without returning a boolean indicating success or failure
        editor.apply();
        u.displayMessage(context, "ScanSist™ Registration Information Saved.");
      } catch (JSONException e) {
        e.printStackTrace();
        u.displayMessage(context, "Warning - No ScanSist™ Registration Data Available.");
      }
    } else {
      u.displayMessage(context, "Warning - No ScanSist™ Registration Data Available.");
    }
    Intent mainActivity = new Intent(MainActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //nextScreen.putExtras(bundle);
    startActivity(mainActivity);
  }

  @Subscribe
  public void onDownloadTrunkTaskResultEvent(DownloadTrunkDataTaskResultEvent event) {
    delaydialogueClose(false);
    //progressDialog.dismiss();
    boolean wasEmpty = false;
    if (event.getResult() != null) {
      if (u.trunks.isEmpty()) {
        wasEmpty = true;
      }
      u.trunkAdapter = new ListTrunkAdapter(u);
      u.trunks.clear();
      u.trunkAdapter.notifyDataSetChanged();
      Trunk defaultEntry = new Trunk(0, "Select a Trunk");
      u.trunks.add(defaultEntry);
      try {
        JSONArray data_array = new JSONArray(event.getResult());
        for (int i = 0; i < data_array.length(); i++) {
          JSONObject obj = new JSONObject(data_array.get(i).toString());
          Trunk add = new Trunk(obj.getInt("TrunkNumber"), obj.getString("TrunkDescription"));
          u.trunks.add(add);
        }
        u.trunkAdapter.notifyDataSetChanged();
      } catch (JSONException e) {
        e.printStackTrace();
      }
    } else {
      u.displayMessage(context, "Warning - No ScanSist™ Trunk Data available.");
    }
    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);
    setupTrunkSpinner(sharedPref);
    u.saveTrunks();
  }

  private TextView setProgressTitle() {
    // Create a TextView programmatically.
    TextView tv = new TextView(context);

    // Set the layout parameters for TextView
    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT, // Width of TextView
        RelativeLayout.LayoutParams.WRAP_CONTENT); // Height of TextView
    tv.setLayoutParams(lp);
    tv.setPadding(15, 10, 15, 10);
    tv.setGravity(Gravity.CENTER);
    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
    tv.setText(getResources().getString(R.string.progress_title));
    tv.setTextColor(Color.WHITE);
    tv.setBackgroundColor(Color.DKGRAY);
    return tv;
  }

  private String getVersionName(Context context) {
    try {
      PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      return pInfo.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      return "";
    }
  }

  private void delaydialogueClose(final Boolean goBack) {

    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
      // Do something after 2s = 2000ms
      if (progressDialog!=null) {
        progressDialog.dismiss();
      }
      if (goBack) {
        onBackPressed();
      }
      }
    }, 2000);
  }
  /*
  private long getAppUpdatedOnDevice() {
    PackageInfo packageInfo = null;
    try {
      packageInfo = getPackageManager()
              .getPackageInfo(getClass().getPackage().getName(), PackageManager.GET_PERMISSIONS);
    } catch (PackageManager.NameNotFoundException e) {
      Log.d(TAG,
              "CANTHAPPEN: Failed to get package info for own package!");
      return -1;
    }
    return packageInfo.lastUpdateTime;
  }
  */
  protected void triggerUpdate(Context context) {
    //Intent mainActivity = new Intent(MainActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);;
    try {
      //get destination to update file and set Uri
      //TODO: First I wanted to store my update .apk file on internal storage for my app but apparently android does not allow you to open and install
      //aplication with existing package from there. So for me, alternative solution is Download directory in external storage. If there is better
      //solution, please inform us in comment
      String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
      String fileName = mcompany + "_scansist_" + liveVersion + ".apk";
      destination += fileName;
      final Uri uri = Uri.parse("file://" + destination);

      //Delete update file if exists
      File file = new File(destination);
      if (file.exists()) {
        //file.delete() - test this, I think sometimes it doesnt work
        file.delete();
      }
      //set downloadmanager
      DownloadManager.Request request = new DownloadManager.Request(Uri.parse(releaseDownloadUrl + liveVersion + ".apk"));
      request.setDescription("Latest Scansist™ Download");
      request.setTitle("ScanSist™");

      //set destination
      request.setDestinationUri(uri);

      // get download service and enqueue file
      final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
      final long downloadId = manager.enqueue(request);

      //set BroadcastReceiver to install app when .apk is downloaded
      BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          Intent install = new Intent(Intent.ACTION_VIEW);
          //install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          install.setDataAndType(uri,
                  manager.getMimeTypeForDownloadedFile(downloadId));

          startActivity(install);
          u.displayMessage(context,"ScanSist™ new version: v" + liveVersion +"\nPlease install and restart app");
          try {
            unregisterReceiver(this);
          } catch(IllegalArgumentException e) {
            e.printStackTrace();
          }
          //This causes app crash!
          //finish();
        }
      };
      //register receiver for when .apk download is compete
      try {
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
      } catch(IllegalArgumentException e) {
        e.printStackTrace();
      }
      //nextScreen.putExtras(bundle);
      //startActivity(mainActivity);
    } catch (Exception e) {
      e.printStackTrace();
      //startActivity(mainActivity);
    }
  }
}
