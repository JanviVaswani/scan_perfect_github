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
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;
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
import android.widget.Toast;

import com.renturapp.scansist.Activity.GetCCGoodsActivity;
import com.renturapp.scansist.Activity.ScanActivity;
import com.renturapp.scansist.Activity.ScanGoodsActivity;
import com.renturapp.scansist.Activity.ScanGoodsForRackLocationRadioOneActivity;
import com.renturapp.scansist.Activity.ScanRackLocationRadioOneActivity;
import com.renturapp.scansist.Adapter.ListPickListAdapter;
import com.renturapp.scansist.Adapter.ListRackAdapter;
import com.renturapp.scansist.AsynTask.DownloadPickListDataTask;
import com.renturapp.scansist.AsynTask.DownloadPickListDataTaskResultEvent;
import com.renturapp.scansist.AsynTask.DownloadRackDataTask;
import com.renturapp.scansist.AsynTask.DownloadRackDataTaskResultEvent;
import com.renturapp.scansist.Model.PickList;
import com.renturapp.scansist.Model.Rack;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
    private String tmLineNumber;
    private String tmNetworkOperator;
    private String tmNetworkOperatorName;
    private String tmSimOperator;
    private String tmSimOperatorName;
    private String tmCellLocation;
    private String tmDevice;
    private String tmSerial;
    String licenspnRackcedatetime;
    private String downloadrackdata = "";
    private TelephonyManager tm = null;
    private static boolean uploadregfile = false;
    public static String mCompanyID = "2";
    public static String mcompany = "demo";
    public static String urlExtension = ".uk";
    private static String liveVersion;
    private static String installedVersion;
    private static String releaseDownloadUrl = "https://www.movesist" + urlExtension + "/clients/" + mcompany + "/downloads/scansist/" + mcompany + "_wmscansist_";
    static boolean localData = false;
    private Utility u;
    private Calendar myCalendar;
    private TextView dateText;
    private TextView lblDateText;
    private DatePickerDialog.OnDateSetListener date;
    private Date mDate;
    private Spinner spnRack;
    private RadioGroup rBg;

    private JSONArray ta;

    private Button btnNext;
    public static String notRegistered = "Not a registered user.\n\nPlease contact sales on 01788 523800 to register your application.";
    public static boolean testMode = false;
    private BroadcastReceiver onComplete;

    //Edited Code: Handler to show repeating select rack location Toast
    private Handler handler = new Handler();
    int globalrackId;
    int globalStatus;
    private static final int SECOND_ACTIVITY_REQUEST_CODE = 0;

    Button previousBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("inside oncreate start");
        super.onCreate(savedInstanceState);
        MyAsyncBus.getInstance().register(this);
        setContentView(R.layout.activity_main);

        previousBtn = (Button) findViewById(R.id.btnPrevious);
        previousBtn.setEnabled(false);
        previousBtn.setClickable(false);

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
            u.RegCompanyId = companyID;

            btnNext = (Button) findViewById(R.id.btnNext);
            btnNext.setClickable(false);
            btnNext.setAlpha(0.5f);

            rBg = (RadioGroup) findViewById(R.id.rBtnG);
            spnRack = (Spinner) findViewById(R.id.spnRack);

            myCalendar = Calendar.getInstance();

            dateText = (TextView) findViewById(R.id.txtManifestDate);
            dateText.setVisibility(View.GONE);

            lblDateText = (TextView) findViewById(R.id.lblManifestDate);
            lblDateText.setVisibility(View.GONE);

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
            mcompany = company;

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
                    downloadrackdata = "https://www.movesist" + urlExtension + "/data/racks/?CompanyID=" + mCompanyID + "&getType=0&AndroidId=" + androidId;
                } else {
                    downloadrackdata = "http://192.168.0.5/data/racks/?CompanyID=" + mCompanyID + "&getType=0&AndroidId=" + androidId;
                }
                /* Called when the activity is first created. */
                if (!previousPressed && !hasScansToProcess) { // && !Utility.isActivityBackground()) {

                    deviceUu = "scansist_" + mcompany + "_" + androidId;
                    deviceFullPath = "https://www.movesist" + urlExtension + "/clients/" + mcompany + "/users/scansist/" + deviceUu + "_p.html";
                    deviceFilePath = "/" + mcompany + "/users/scansist/" + deviceUu + "_p.html";
                    String scanSistCode = String.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getInt("ScanSistCode", MODE_PRIVATE));

                    if (!u.isOnline()) {
                        u.displayMessage(context, "No Internet connection.\n\nLicence cannot be verified.");
                        MainActivity.super.onBackPressed();
                    } else {
                        //Check for the occurrence of the file that is shown in the saved registration key adding a '_p' will ensure it runs once registered
                        String deviceRegFullPath = "https://www.movesist" + urlExtension + "/clients/" + mcompany + "/users/scansist/" + deviceUu + "_p.html";
                        if (u.showlicensedialog) {
                            progressDialog = new ProgressDialog(context);
                            progressDialog.setCustomTitle(u.setProgressTitle(context));
                            progressDialog.setMessage("Checking Licence - v" + getVersionName(context) + "\n\n" + companyName + "\n\nDepot " + depotNumber + " Scanner ID: " + scanSistCode + "\n\n         Please wait.");
                            progressDialog.setIndeterminate(true);
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            u.showlicensedialog = false;
                        }
                        new CheckLicenceTask().execute(deviceRegFullPath);
                        uploadregfile = false;
                        //}
                    }
                } else {
                    String racksJson = sharedPref.getString("racks", "NothingFound"); //NothingFound
                    try {
                        ta = new JSONArray(racksJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (!racksJson.equals("NothingFound") && !u.isOnline()) {
                        if (u.rackAdapter == null) {
                            u.rackAdapter = new ListRackAdapter(u);
                        }
                        u.setupRacks(racksJson);
                        setupRackSpinner(sharedPref);

                    } else {
                        //May have just finished so reload
                        //ToDo This occurs when we select the radio buttons so not needed
                        //ToDo but would occur if the radio button is already selected!
                        if (ta != null && ta.length() == 1) {
                            new DownloadRackDataTask(context).execute(downloadrackdata);
                        }
                    }
                }
            }
            /*
             *
             *
             *
             *     End of ScanSist registration checking
             *
             *String string = "004-034556";
String[] parts = string.split("-");
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
                setRadioButton(status, sharedPref);
            }

            //setBtnNextEnable(sharedPref);

            int rackHub = hubStatus();
            if (rackHub != -1) {
                checkRadioButtonClick(rackHub);
            }
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

        //setBtnNextEnable(sharedPref);
    }

    public void onRadioButtonClicked(View v) {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);
        int rackHub = hubStatus();

        //setBtnNextEnable(sharedPref);
        checkRadioButtonClick(rackHub);
        setBtnNextEnable(sharedPref);
    }

    private void checkRadioButtonClick(int rackHub) {
        if (rackHub == 0 || rackHub == 1) {
            btnNext.setClickable(false);
            spnRack.setEnabled(true);
            spnRack.setClickable(true);
            spnRack.setVisibility(View.VISIBLE);
            new DownloadRackDataTask(context).execute(downloadrackdata + "&RackType=" + rackHub);
        } else if (rackHub == 2) {
            spnRack.setEnabled(false);
            spnRack.setClickable(false);
            spnRack.setVisibility(View.INVISIBLE);
        } else if (rackHub == 3) {
            btnNext.setClickable(false);
            spnRack.setEnabled(true);
            spnRack.setClickable(true);
            spnRack.setVisibility(View.VISIBLE);
            String url = "https://www.movesist.uk/data/picklists?getType=5&CompanyID=" + u.RegCompanyId;
            new DownloadPickListDataTask(context).execute(url);
        }
    }

    private void enableRbg(Boolean enabled) {
        for (int i = 0; i < rBg.getChildCount(); i++) {
            rBg.getChildAt(i).setEnabled(enabled);
        }
    }

    private void enableScanSetup(Boolean enabled) {
        if (enabled) {
            if (lblDateText != null) {
                lblDateText.setAlpha(1f);
            }
            if (dateText != null) {
                dateText.setClickable(true);
                dateText.setEnabled(true);
                dateText.setAlpha(1f);
            }
            if (spnRack != null) {
                spnRack.setClickable(true);
                spnRack.setEnabled(true);
                spnRack.setAlpha(1f);
            }
        } else {
            if (lblDateText != null) {
                lblDateText.setAlpha(0.5f);
            }
            if (dateText != null) {
                dateText.setClickable(false);
                dateText.setEnabled(false);
                dateText.setAlpha(0.33f);
            }
            if (spnRack != null) {
                spnRack.setClickable(false);
                spnRack.setEnabled(false);
                spnRack.setAlpha(0.5f);
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

    private void setupRackSpinner(SharedPreferences sharedPref) {

        final SharedPreferences sp = sharedPref;

        spnRack.setVisibility(View.VISIBLE);
        spnRack.setAdapter(u.rackAdapter);

        spnRack.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        setBtnNextEnable(sp);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
        int rackID = sharedPref.getInt("rackID", 0);
        //ToDo The download list may not be in sync with the stored list or may be length of 1 (the default)
        if (u.rackAdapter.getCount() > 1) {
            for (int i = 0; i <= u.rackAdapter.getCount(); i++) {
                Rack t = (Rack) spnRack.getItemAtPosition(i);
                if (t != null && rackID == t.rackID) {
                    spnRack.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setupPicklistSpinner(SharedPreferences sharedPref) {
        final SharedPreferences sp = sharedPref;
        spnRack = (Spinner) findViewById(R.id.spnRack);

        spnRack.setVisibility(View.VISIBLE);
        spnRack.setAdapter(u.pickListAdapter);

        spnRack.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        setBtnNextEnable(sp);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
        int picklistID = sharedPref.getInt("PickListID", 0);
        //ToDo The download list may not be in sync with the stored list or may be length of 1 (the default)
        if (u.pickListAdapter.getCount() > 1) {
            for (int i = 0; i <= u.pickListAdapter.getCount(); i++) {
                PickList t = (PickList) spnRack.getItemAtPosition(i);
                if (t != null && picklistID == t.pickListID) {
                    spnRack.setSelection(i);
                    break;
                }
            }
        }
    }

    private int hubStatus() {
        int status;
        switch (rBg.getCheckedRadioButtonId()) {
            case R.id.rBtnIntoLocation:
                status = 0;
                break;
            case R.id.rBtnCheckRackGoods:
                status = 1;
                break;
            case R.id.rBtnCheckGoods:
                status = 2;
                break;
            case R.id.rBtnGoodsOut:
                status = 3;
                break;
            default:
                status = -1;
                break;
        }
        globalStatus = status;
        return status;
    }

    private void setBtnNextEnable(SharedPreferences sharedPref) {

        TextView id = (TextView) findViewById(R.id.ID);

        int rackID = 0;
        if (id != null) {
            rackID = parseInt(id.getText().toString());
        }

        if (rBg.getCheckedRadioButtonId() != -1) {
            btnNext.setClickable(true);
            btnNext.setAlpha(1f);

        } else {
            btnNext.setClickable(false);
            btnNext.setAlpha(0.5f);
        }
        if (u.HasScans(sharedPref)) {
            enableScanSetup(false); // HasScans false
            enableRbg(false); // HasScans false
        } else {
            enableScanSetup(true);
            enableRbg(true);
        }
    }

    private void setRadioButton(Integer s, SharedPreferences sharedPref) {
        switch (s) {
            case 0:
                RadioButton fh = (RadioButton) findViewById(R.id.rBtnIntoLocation);
                fh.setChecked(true);
                break;
            case 1:
                RadioButton th = (RadioButton) findViewById(R.id.rBtnCheckRackGoods);
                th.setChecked(true);
                break;
            case 2:
                RadioButton d = (RadioButton) findViewById(R.id.rBtnCheckGoods);
                d.setChecked(true);
                break;
            case 3:
                RadioButton o = (RadioButton) findViewById(R.id.rBtnGoodsOut);
                o.setChecked(true);
                break;
        }
        setBtnNextEnable(sharedPref);
    }

    private void selectPage(Integer status) {
        globalStatus = status;
        Bundle bundle = new Bundle();
        bundle.putInt("status", status);

        //hh 12hour format - HH 24 hr format
        SimpleDateFormat scan_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
        String scanDateTime = "";
        if (mDate != null) {
            scanDateTime = scan_sdf.format(mDate);
        } else {
            mDate = new Date();
            scanDateTime = scan_sdf.format(mDate);
        }
        bundle.putString("scanDateTime", scanDateTime);

        //Radio Button 1 - Scan Goods into Rack Location
        if (globalStatus == 0) {
            Rack rack = (Rack) spnRack.getSelectedItem();
            System.out.println("Rack Object" + rack);

            bundle.putInt("rackID", rack.rackID);
            bundle.putString("rackDescription", rack.rackDescription);

            globalrackId = rack.rackID;

            if (rack.rackID == 0) {
                Intent nextScreen = new Intent(MainActivity.this, ScanRackLocationRadioOneActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(nextScreen, SECOND_ACTIVITY_REQUEST_CODE);
                nextScreen.putExtras(bundle);
                startActivity(nextScreen);
                finish();
            } else if (rack.rackID != 0) {
                Intent nextScreen = new Intent(MainActivity.this, ScanGoodsForRackLocationRadioOneActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                nextScreen.putExtras(bundle);
                startActivity(nextScreen);
                finish();
            }
        }
        //Radio Button 2 - Check Goods at Rack Location
        else if (globalStatus == 1) {
            Rack rack = (Rack) spnRack.getSelectedItem();
            System.out.println("Rack Object" + rack);

            bundle.putInt("rackID", rack.rackID);
            bundle.putString("rackDescription", rack.rackDescription);

            globalrackId = rack.rackID;
            System.out.println();

            if (rack.rackID == 0) {
                Intent nextScreen = new Intent(MainActivity.this, ScanActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                nextScreen.putExtras(bundle);
                startActivity(nextScreen);
                finish();
            } else if (rack.rackID != 0) {
                Intent nextScreen = new Intent(MainActivity.this, GetCCGoodsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                nextScreen.putExtras(bundle);
                startActivity(nextScreen);
                finish();
            }
        }
        //Radio Button 3 - Check Goods
        else if (globalStatus == 2) {
            Intent nextScreen = new Intent(MainActivity.this, ScanGoodsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            nextScreen.putExtras(bundle);
            startActivity(nextScreen);
        }
        //Radio Button 4 - Check Goods out
        else if (globalStatus == 3) {
            PickList picklist = (PickList) spnRack.getSelectedItem();

            bundle.putInt("picklistID", picklist.pickListID);
            bundle.putString("picklistDescription", picklist.pickListDescription);

            if (picklist.pickListID == 0) {
                Intent nextScreen = new Intent(MainActivity.this, ScanActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(nextScreen, SECOND_ACTIVITY_REQUEST_CODE);
                nextScreen.putExtras(bundle);
                startActivity(nextScreen);
                finish();
            } else if (picklist.pickListID != 0) {
                Intent nextScreen = new Intent(MainActivity.this, GetCCGoodsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                nextScreen.putExtras(bundle);
                startActivity(nextScreen);
                finish();
            }
        } else {
            Toast.makeText(MainActivity.this, "Select Check Goods at Rack Location in Radio Button", Toast.LENGTH_LONG).show();
        }

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
                startActivity(nextScreen);
                //finish();
                break;

            case R.id.action_about:
                nextScreen = new Intent(MainActivity.this, AboutActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(nextScreen);
                //finish();
                break;


            case R.id.action_licence:
                nextScreen = new Intent(MainActivity.this, LicenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
        if (spnRack != null) {

            status = hubStatus();

            if (spnRack != null) {
                int elementId = 0;
                String elementName = "";
                if (status == 3) {
                    PickList pickList = (PickList) spnRack.getSelectedItem();
                    elementName = "picklistID";
                    elementId = pickList.pickListID;
                } else if (status == 1 || status == 0) {
                    Rack rack = (Rack) spnRack.getSelectedItem();
                    elementName = "rackID";
                    elementId = rack.rackID;
                }
            }

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
            editor.putInt("status", status);
            editor.apply();
        }
    }


    @Subscribe
    public void onCheckLicenceTaskResultEvent(CheckLicenceTaskResultEvent event) {
        String vn = getVersionName(context);
        installedVersion = vn.replace(".", "_");
        if (!uploadregfile) {
            if (!event.getResult()) {
                u.displayMessage(context, "Not a registered user.\n\nPlease contact sales on 01788 523800 to register your application.");
                delaydialogueClose(true);
            } else {
                //licence ok so download data if required
                //already called in setRadioButton
                //new DownloadRackDataTask().execute(downloadrackdata);
                //delaydialogueClose(false);
                //new CheckReleaseTask().execute(releaseDownloadUrl);
                new CheckReleaseTask().execute(releaseDownloadUrl + "version.html", installedVersion);
            }
        } else {
            new CheckReleaseTask().execute(releaseDownloadUrl + "version.html", installedVersion);
        }
    }

    @Subscribe
    public void onCheckReleaseTaskResultEvent(CheckReleaseTaskResultEvent event) {

        liveVersion = event.getResult();
        if (liveVersion.length() > 0 && !installedVersion.equals(liveVersion)) {
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
                    , "https://www.movesist" + urlExtension + "/data/scansists/");
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
        //new DownloadRackDataTask().execute(downloadrackdata);
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
    public void onDownloadRackTaskResultEvent(DownloadRackDataTaskResultEvent event) {
        delaydialogueClose(false);
        //progressDialog.dismiss();
        boolean wasEmpty = false;
        if (event.getResult() != null) {
            if (u.racks.isEmpty()) {
                wasEmpty = true;
            }
            u.rackAdapter = new ListRackAdapter(u);
            u.racks.clear();
            u.rackAdapter.notifyDataSetChanged();
            Rack defaultEntry = new Rack(0, "Select a Racklist");
            u.racks.add(defaultEntry);
            try {
                JSONArray data_array = new JSONArray(event.getResult());
                for (int i = 0; i < data_array.length(); i++) {
                    JSONObject obj = new JSONObject(data_array.get(i).toString());
                    Rack add = new Rack(obj.getInt("RackID"), obj.getString("RackDescription"));
                    u.racks.add(add);
                }
                u.rackAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            u.displayMessage(context, "Warning - No ScanSist™ Rack Data available.");
        }
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);
        setupRackSpinner(sharedPref);
        u.saveRacks();
        //setBtnNextEnable(sharedPref);
    }

    @Subscribe
    public void onDownloadPickListDataTaskResultEvent(DownloadPickListDataTaskResultEvent event) {
        boolean wasEmpty = false;

        if (event.getResult() != null) {
            if (u.pickLists.isEmpty()) {
                wasEmpty = true;
            }
            u.pickListAdapter = new ListPickListAdapter(u);
            u.pickLists.clear();
            u.pickListAdapter.notifyDataSetChanged();
            //Rack defaultEntry = new Rack(0, "Select a Rack");
            PickList defaultEntry = new PickList(0, "Select a Picklist");
            u.pickLists.add(defaultEntry);

            try {
                JSONArray data_array = new JSONArray(event.getResult());
                for (int i = 0; i < data_array.length(); i++) {
                    JSONObject obj = new JSONObject(data_array.get(i).toString());
                    PickList addpicklistdata = new PickList(obj.getInt("PickListID"), obj.getString("PickListDescription"));
                    u.pickLists.add(addpicklistdata);
                }
                u.pickListAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            u.displayMessage(context, "Warning - No ScanSist™ PickList Data available.");
        }

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);
        setupPicklistSpinner(sharedPref);
        u.savePicklists();
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
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                if (goBack) {
                    onBackPressed();
                }
            }
        }, 2000);
    }

    private int downLoadStatus() {
        //https://stackoverflow.com/questions/10258395/how-to-get-status-of-downloading
        //Check if download manger running
        DownloadManager.Query query = null;
        Cursor c = null;
        int status = DownloadManager.STATUS_SUCCESSFUL;
        DownloadManager downloadManager = null;
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        query = new DownloadManager.Query();
        if (query != null) {
            query.setFilterByStatus(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_SUCCESSFUL |
                    DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PENDING);
        } else {
            return status;
        }
        c = downloadManager.query(query);
        if (c.moveToFirst()) {
            status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    Log.i("Tag", "Download Manager Paused");
                    break;
                case DownloadManager.STATUS_PENDING:
                    Log.i("Tag", "Download Manager Pending");
                    break;
                case DownloadManager.STATUS_RUNNING:
                    Log.i("Tag", "Download Manager Running");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    Log.i("Tag", "Download Manager Successful");
                    break;
                case DownloadManager.STATUS_FAILED:
                    Log.i("Tag", "Download Manager Failed");
                    break;
            }
        }
        return status;
    }

    protected void triggerUpdate(Context context) {
        //Intent mainActivity = new Intent(MainActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);;
        try {
            //get destination to update file and set Uri
            //TODO: First I wanted to store my update .apk file on internal storage for my app but apparently android does not allow you to open and install
            //aplication with existing package from there. So for me, alternative solution is Download directory in external storage. If there is better
            //solution, please inform us in comment

            String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
            String fileName = mcompany + "_wmscansist_" + liveVersion + ".apk";
            destination += fileName;
            Uri uri = Uri.parse("file://" + destination);
            File file = new File(destination);
            int status = downLoadStatus();
            if (status == DownloadManager.STATUS_RUNNING) {
                u.displayMessage(context, "ScanSist™ new version: v" + liveVersion + "\nDownload in progress");
                registerCompleted(file);
                return;
            } else {
                //if (status != DownloadManager.STATUS_SUCCESSFUL) {
                u.displayMessage(context, "ScanSist™ new version: v" + liveVersion + "\nDownloading Update");
            }
            //Delete update file if exists
            if (file.exists()) {
                //file.delete() - test this, I think sometimes it doesnt work
                file.delete();
            }
            //set download manager
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(releaseDownloadUrl + liveVersion + ".apk"));
            request.setDescription(getString(R.string.app_download));
            request.setTitle(getString(R.string.app_name));

            //set destination
            request.setDestinationUri(uri);

            // get download service and enqueue file
            final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            final long downloadId = manager.enqueue(request);

            registerCompleted(file);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //https://stackoverflow.com/questions/58374527/installing-apk-that-updates-the-same-app-fails-on-android-10-java-lang-security

    private void registerCompleted(File _file) {
        final File file = _file;
        //set BroadcastReceiver to install app when .apk is downloaded
        onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent install = new Intent();
                install.setAction(Intent.ACTION_VIEW);
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= 24) {
                    Uri apkUri = FileProvider.getUriForFile(context, "com.renturapp.podsist.fileprovider", file);
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    install.setDataAndType(apkUri, "application/vnd.android.package-archive");
                } else {
                    install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                }
                context.startActivity(install);
                try {
                    unregisterReceiver(this);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                //Incase we cancel then keep app running so don'nt finish()
            }
        };
        //register receiver for when .apk download is compete
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        //Down files just in case they don't want to use the new version
        //downloadRepositories();
    }
}
