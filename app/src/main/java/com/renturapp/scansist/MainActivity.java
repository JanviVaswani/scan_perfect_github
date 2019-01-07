package com.renturapp.scansist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.lang.Integer.parseInt;


public class MainActivity extends Activity {

  //Registration
  private ProgressDialog progressDialog;
  private Context context;
  private String deviceUu;
  private String deviceFilePath;
  private String tmLineNumber;
  private String tmNetworkOperator;
  private String tmNetworkOperatorName;
  private String tmSimOperator;
  private String tmSimOperatorName;
  private String tmCellLocation;
  private String tmDevice;
  private String tmSerial;
  private String androidId;
  private String regdatetime;
  String licencedatetime;
  private String downloadtrunkdata = "";
  private TelephonyManager tm = null;
  private static boolean uploadregfile = false;
  private static String mCompanyID = "2";
  static boolean localData = false;
  private Utility u;

  private Calendar myCalendar;
  private EditText dateText;
  private DatePickerDialog.OnDateSetListener date;
  private Date mDate;
  private Spinner spnTrunk;

  private Button btnNext;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    String mcompany = "demo";
    uploadregfile = false;
    context = MainActivity.this;

    u = (Utility) getApplicationContext();

    if (!isOnline()) {

      u.displayMessage(context, "No Internet connection.\n\nLicence cannot be verified.");
      MainActivity.super.onBackPressed();
    }
    //New instance is set to null
    progressDialog = null;
    //Debug Mode no Licence check
    Boolean debugMode = false;
    if (debugMode) {
      u.displayMessage(context, "ScanSist™ - Trial Application - Please wait.");
    }

    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);

    /*
     *
     *
     *   Register ScanSist App
     *
     *
     *                          */
    Intent intent = getIntent();
    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    if (!localData) {
      downloadtrunkdata = "https://www.movesist.com/data/trunks/?CompanyID=" + mCompanyID + "&getType=7&AndroidId=" + androidId;
    } else {
      downloadtrunkdata = "http://192.168.0.5/data/trunks/?CompanyID=" + mCompanyID + "&getType=7&AndroidId=" + androidId;
    }
    // getting attached intent data
    Boolean previousPressed = intent.getBooleanExtra("onBackPressed", false);
    /* Called when the activity is first created. */
    if (!previousPressed) {

      deviceUu = "scansist_" + mcompany + "_" + androidId;

      String deviceFullPath = "https://www.movesist.com/clients/" + mcompany + "/users/scansist/" + deviceUu + "_p.html";
      deviceFilePath = "/" + mcompany + "/users/scansist/" + deviceUu + "_p.html";

      //Must use default preference file!
      String deviceUuPref = PreferenceManager.getDefaultSharedPreferences(context).getString("RegKey", "NothingFound");
      if (deviceUuPref.equals("NothingFound")) {

        //Do we need to upload a file
        progressDialog = new ProgressDialog(context);
        progressDialog.setCustomTitle(setProgressTitle());
        progressDialog.setMessage("Registering Licence - v" + getVersionName(context) + "\n\n         Please wait.");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        uploadregfile = true;
        new CheckLicenceTask().execute(deviceFullPath);
      } else {
        if (!isOnline()) {

          u.displayMessage(context, "No Internet connection.\n\nLicence cannot be verified.");
          MainActivity.super.onBackPressed();
        } else {
          //Check for the occurrence of the file that is shown in the saved registration key adding a '_p' will ensure it runs once registered
          String deviceRegFullPath = "https://www.movesist.com/clients/" + mcompany + "/users/scansist/" + deviceUuPref + "_p.html";
          progressDialog = new ProgressDialog(context);
          progressDialog.setCustomTitle(setProgressTitle());
          progressDialog.setMessage("Checking Licence - v" + getVersionName(context) + "\n\n    Company (" + mcompany + ")\n\n         Please wait.");
          progressDialog.setIndeterminate(true);
          progressDialog.setCancelable(false);
          progressDialog.show();
          new CheckLicenceTask().execute(deviceRegFullPath);
          uploadregfile = false;
        }
      }
    } else {
      String trunksJson = sharedPref.getString("trunks", "NothingFound");
      if (!trunksJson.equals("NothingFound")) {
        if (u.trunkAdapter == null) {
          u.trunkAdapter = new ListTrunkAdapter(u);
        }
        u.setupTrunks(trunksJson);
        setupTrunkSpinner(sharedPref);
      } else {
        //May have just finished so reload
        new DownloadTrunkDataTask().execute(downloadtrunkdata);
      }
    }
    /*
     *
     *
     *     End of ScanSist registration checking
     *
     *
     *                                              */

    MyAsyncBus.getInstance().register(this);

    if (u.scanAdapter == null) {
      u.scanAdapter = new ListScanAdapter(u);
    }

    btnNext = (Button) findViewById(R.id.btnNext);
    btnNext.setClickable(false);
    btnNext.setAlpha(0.5f);


    myCalendar = Calendar.getInstance();

    dateText = (EditText) findViewById(R.id.txtManifestDate);
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
      setRadioButton(status);
    }
    if (u.clauses.isEmpty()) {
      u.setupClauses(((RadioGroup) findViewById(R.id.rBtnG)).getCheckedRadioButtonId());
    }

    String scansJson = sharedPref.getString("scans", "NothingFound");

    if (!scansJson.equals("NothingFound")) {
      u.setupScans(scansJson);
    }
    setBtnNextEnable();
  }

  // very important when rotating !!! http://stackoverflow.com/questions/456211/activity-restart-on-rotation-android
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
  }

  private void updateLabel() {
    String myFormat = "dd/MM/yy"; //In which you need put here
    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);

    mDate = myCalendar.getTime();
    dateText.setText(sdf.format(mDate));

    setBtnNextEnable();
  }

  public void onRadioButtonClicked(View v) {

    setBtnNextEnable();
    u.setupClauses(((RadioGroup) findViewById(R.id.rBtnG)).getCheckedRadioButtonId());
  }

  public void onWizardButtonClicked(View v) {
    RadioGroup mainGroup = (RadioGroup) findViewById(R.id.rBtnG);

    switch (v.getId()) {

      case R.id.btnPrevious:
        onBackPressed();
        break;

      case R.id.btnNext:
        // Check which radio button was clicked
        switch (mainGroup.getCheckedRadioButtonId()) {
          case R.id.rBtnFromHub:
            selectPage(0);
            break;
          case R.id.rBtnToHub:
            selectPage(1);
            break;
          case R.id.rBtnOntoDelivery:
            selectPage(2);
            break;
        }
        break;
      case R.id.btnCancel:
        u.messageBox(context, false, false);
        break;
      default:
        throw new RuntimeException("Unknow button ID");
    }
  }

  private void setupTrunkSpinner(SharedPreferences sharedPref) {

    spnTrunk = (Spinner) findViewById(R.id.spnTrunk);
    spnTrunk.setVisibility(View.VISIBLE);
    spnTrunk.setAdapter(u.trunkAdapter);

    spnTrunk.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            setBtnNextEnable();
          }

          @Override
          public void onNothingSelected(AdapterView<?> arg0) {
          }
        });
    int trunkNumber = sharedPref.getInt("trunkNumber", 0);
    for (int i = 0; i <= u.trunkAdapter.getCount(); i++) {
      Trunk t = (Trunk) spnTrunk.getItemAtPosition(i);
      if (t != null && trunkNumber == t.trunkNumber) {
        spnTrunk.setSelection(i);
        break;
      }
    }
  }

  private void setBtnNextEnable() {

    TextView id = (TextView) findViewById(R.id.ID);

    int trunkNumber = 0;
    if (id != null) {
      trunkNumber = parseInt(id.getText().toString());
    }

    RadioGroup g = (RadioGroup) findViewById(R.id.rBtnG);

    if (dateText.length() > 0 && trunkNumber > 0 && mDate != null && g.getCheckedRadioButtonId() != -1) {
      btnNext.setClickable(true);
      btnNext.setAlpha(1f);
    } else {
      btnNext.setClickable(false);
      btnNext.setAlpha(0.5f);
    }
  }

  private void setRadioButton(Integer s) {
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
    setBtnNextEnable();
  }

  private void selectPage(Integer status) {

    Bundle bundle = new Bundle();
    bundle.putInt("status", status);
    Trunk trunk = (Trunk) spnTrunk.getSelectedItem();
    bundle.putInt("trunkNumber", trunk.trunkNumber);

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
    switch (item.getItemId()) {
      case R.id.action_settings:
        // User chose the "Settings" item, show the app settings UI...
        return true;
      case R.id.action_about:
        // User chose the "Settings" item, show the app settings UI...
        return true;
      case R.id.action_home:
        // User choose the "Setup home Option" action, go to home screen
        u.changeMenuItemState(u.getHome(), false, true, false);
        u.changeMenuItemState(u.getBarCode(), false, false, false);
        onBackPressed();
        return true;
      case R.id.action_barcode:
        return true;
      case R.id.action_flash:
        return true;
      default:
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onBackPressed() {
    MainActivity.super.onBackPressed();
  }

  @Override
  protected void onDestroy() {

    int status;

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

    RadioGroup rBg = (RadioGroup) findViewById(R.id.rBtnG);

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
        status = 0;
        break;
    }

    editor.putInt("status", status);
    editor.apply();
    MyAsyncBus.getInstance().unregister(this);
    super.onDestroy();
  }

  @Subscribe
  public void onCheckLicenceTaskResultEvent(CheckLicenceTaskResultEvent event) {

    if (!uploadregfile) {
      if (!event.getResult()) {
        u.displayMessage(context, "Not a registered user.\n\nPlease contact sales on 01788 523800 to register your application.");
        delaydialogueClose(true);
      } else {
        //licence ok so download data if required
        new DownloadTrunkDataTask().execute(downloadtrunkdata);
        delaydialogueClose(false);
      }
    } else {
      //Upload file
      SimpleDateFormat sdf = new SimpleDateFormat("EEEE MMM dd yyyy, hh:mm:ss aa", Locale.UK);
      licencedatetime = sdf.format(new Date());
      SimpleDateFormat reg_sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.UK);
      regdatetime = reg_sdf.format(new Date());

      /*TODO:

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        readTelephoneDetails(true);
      } else {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
          // TODO: Consider calling
          //    Activity#requestPermissions
          // here to request the missing permissions, and then overriding
          //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
          //                                          int[] grantResults)
          // to handle the case where the user grants the permission. See the documentation
          // for Activity#requestPermissions for more details.
          readTelephoneDetails(false);
        } else {
          readTelephoneDetails(true);
        }
      }*/

      readTelephoneDetails(true);


      String simInfo;
      simInfo = "Serial: " + tmSerial
          + "\nTel: " + tmLineNumber
          + "\nNetwork: " + tmNetworkOperator
          + "\nName: " + tmNetworkOperatorName
          + "\nSimOp: " + tmSimOperator
          + "\nSimOpName: " + tmSimOperatorName
          + "\nCellLocation: " + tmCellLocation
          + "\nDeviceId: " + tmDevice
          + "\nAndroidId: " + androidId
          + "\nRegDateTime: " + licencedatetime
          + "\nScanSistIsDeleted: false"
          + "\nCompanyID: " + mCompanyID;
      new FTPFileUploadTask().execute("www.movesist.com",
          "clients",
          "wdrcv227qt",
          simInfo,
          deviceFilePath);
    }
  }

  @Subscribe
  public void onAsyncFTPFileUploadTaskResultEvent(FTPFileUploadTaskResultEvent event) {

    if (!event.getResult()) {
      delaydialogueClose(true);
      u.displayMessage(context, "Registration Failed.");
    } else {
      new RegisterScanSistTask().execute(tmSerial
          , tmLineNumber
          , tmNetworkOperator
          , tmNetworkOperatorName
          , tmSimOperator
          , tmSimOperatorName
          , tmCellLocation
          , tmDevice
          , androidId
          , deviceUu + ".html"
          , regdatetime
          , "false"
          , mCompanyID
          , "7");
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
    new DownloadDataTask().execute("https://www.movesist.com/data/scansists/?CompanyID=" + mCompanyID + "&getType=3&AndroidId=" + androidId);
    new DownloadTrunkDataTask().execute(downloadtrunkdata);
  }

  @Subscribe
  public void onDownloadTaskResultEvent(DownloadDataTaskResultEvent event) {
    delaydialogueClose(false);
    if (event.getResult() != null) {
      SharedPreferences dp = PreferenceManager.getDefaultSharedPreferences(context);
      //now get the Editor
      SharedPreferences.Editor editor = dp.edit();
      String depotNumber = "";
      int scanSistCode = 0;
      try {
        JSONArray data_array = new JSONArray(event.getResult());
        for (int i = 0; i < data_array.length(); i++) {
          JSONObject obj = new JSONObject(data_array.get(i).toString());
          depotNumber = obj.getString("DepotNumber");
          scanSistCode = obj.getInt("ScanSistCode");
          break;
        }
        editor.putString("RegKey", deviceUu);
        editor.putString("DepotNumber", depotNumber);
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

  private void readTelephoneDetails(boolean canReadPhoneSate) {

    tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
    if (canReadPhoneSate && tm != null && tm.getDeviceId() != null) {
      tmDevice = "" + tm.getDeviceId();
    } else {
      tmDevice = "";
    }
    if (canReadPhoneSate && tm != null && tm.getSimSerialNumber() != null) {
      tmSerial = "" + tm.getSimSerialNumber();
    } else {
      tmSerial = "";
    }
    if (canReadPhoneSate && tm.getLine1Number() != null) {
      tmLineNumber = "" + tm.getLine1Number();
    } else {
      tmLineNumber = "";
    }
    if (tm.getNetworkOperator() != null) {
      tmNetworkOperator = tm.getNetworkOperator();
    } else {
      tmNetworkOperator = "";
    }
    if (tm.getNetworkOperatorName() != null) {
      tmNetworkOperatorName = tm.getNetworkOperatorName();
    } else {
      tmNetworkOperatorName = "";
    }
    if (tm.getSimOperator() != null) {
      tmSimOperator = tm.getSimOperator();
    } else {
      tmSimOperator = "";
    }
    if (tm.getSimOperatorName() != null) {
      tmSimOperatorName = tm.getSimOperatorName();
    } else {
      tmSimOperatorName = "";
    }
    if (canReadPhoneSate && tm.getCellLocation() != null) {
      tmCellLocation = tm.getCellLocation().toString();
    } else {
      tmCellLocation = "";
    }

  }


  private String getVersionName(Context context) {
    try {
      PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      return pInfo.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      return "";
    }
  }

  private boolean isOnline() {
    ConnectivityManager cm =
        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
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
}
