package com.renturapp.scansist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.renturapp.scansist.Adapter.GetCCGoodsAdapter;
import com.renturapp.scansist.Adapter.ListPickListAdapter;
import com.renturapp.scansist.Adapter.ListRackAdapter;
import com.renturapp.scansist.Adapter.ListScanAdapter;
import com.renturapp.scansist.Model.GetCCGoodsModel;
import com.renturapp.scansist.Model.PickList;
import com.renturapp.scansist.Model.Rack;
import com.renturapp.scansist.Model.Scan;
import com.renturapp.scansist.Model.ScanGoodsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import java.util.List;

//https://github.com/codepath/android_guides/wiki/Understanding-the-Android-Application-Class
public class Utility extends Application {

    //Edited Code
    public ArrayList<GetCCGoodsModel> getCCGoodsModels = new ArrayList<GetCCGoodsModel>();
    public GetCCGoodsAdapter getCCGoodsAdapter;
    public ArrayList<Rack> racks = new ArrayList<Rack>();
    public ListRackAdapter rackAdapter;
    public ArrayList<PickList> pickLists = new ArrayList<PickList>();
    public ListPickListAdapter pickListAdapter;

    public ArrayList<Clause> clauses = new ArrayList<Clause>();
    public ListClauseAdapter clauseAdapter;
    public ArrayList<Scan> scans = new ArrayList<Scan>();
    public ListScanAdapter scanAdapter;

    public List<ScanGoodsModel> scanpicklistgoods = new ArrayList<ScanGoodsModel>();

    public List<ScanGoodsModel> scannedgoodoutList = scannedgoodoutList = new ArrayList<>();

    public static boolean showlicensedialog;

    public String goodsIds="";

    public Utility() {

    }

    public String tmLineNumber;
    public String tmNetworkOperator;
    public String tmNetworkOperatorName;
    public String tmSimOperator;
    public String tmSimOperatorName;
    public String tmCellLocation;
    public String tmDevice;
    public String tmSerial;

    public static String RegCompanyId;

    /*Menu Items*/
    private MenuItem home;

    public MenuItem getHome() {
        return this.home;
    }

    public void setHome(MenuItem home) {
        this.home = home;
    }

    private MenuItem barCode;

    public MenuItem getBarCode() {
        return this.barCode;
    }

    public void setBarCode(MenuItem barCode) {
        this.barCode = barCode;
    }

    private MenuItem flash;

    public MenuItem getFlash() {
        return this.flash;
    }

    public void setFlash(MenuItem flash) {
        this.flash = flash;
    }

    public void changeMenuItemState(MenuItem m, Boolean enabled, Boolean visibility, Boolean enabledState) {
        m.setEnabled(enabled);
        m.setVisible(visibility);
        if (enabledState) {
            m.getIcon().setAlpha(255);
        } else {
            m.getIcon().setAlpha(100);
        }
    }
    /*        */

    public void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private TextView setTitle(Context c, String s) {
        TextView title = new TextView(c);
        // You Can Customise your Title here
        title.setText(s);
        title.setBackgroundColor(Color.DKGRAY);//getResources().getColor(R.color.gray));
        title.setPadding(15, 10, 15, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        return title;
    }

    private TextView setText(Context c, String s, int finish) {
        TextView m = new TextView(c);
        String text = "\n" + s;
        m.setText(text);
        m.setTextSize(15);
        if (finish == 3) {
            m.setTextColor(Color.RED);
        }
        m.setGravity(Gravity.CENTER_HORIZONTAL);
        return m;
    }

    public void messageBox(Context ctx, int finish, Boolean timeout) {
        final Context c = ctx;
        final int f = finish;
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        StringBuilder message;
        if (f == 1) {
            builder.setCustomTitle(setTitle(c, getString(R.string.confirm_finish)));
        } else if (f == 2) {
            builder.setCustomTitle(setTitle(c, getString(R.string.confirm_cancel)));
        } else {
            builder.setCustomTitle(setTitle(c, getString(R.string.confirm_clear)));
        }
        if (!scans.isEmpty()) {
            if (f == 1) {
                int d = 0;
                for (Scan s : scans) {
                    if (s.clauseID > 0) {
                        d++;
                    }
                }
                if (d > 0) {
                    message = new StringBuilder("Upload " + scanAdapter.getCount() + " Scanned Job" + (scanAdapter.getCount() > 1 ? "s" : "") + "\n"
                            + d + " Damaged:\n\n");
                } else {
                    message = new StringBuilder("Upload " + scanAdapter.getCount() + " Scanned Job" + (scanAdapter.getCount() > 1 ? "s" : "") + ":\n\n");
                }

                for (Scan s : scans) {
                    message.append(s.scanBarCode).append(" ").append(s.clauseCode).append(" ").append("\n");
                }
            } else {
                if (f == 3) {
                    message = new StringBuilder("Cancel the ScanSist™ App?\n\nClear Previous " + scanAdapter.getCount() + " Scanned Job" + (scanAdapter.getCount() > 1 ? "s" : "") + ":\n\n");
                } else {
                    message = new StringBuilder("Cancel the ScanSist™ App?\n\nClear " + scanAdapter.getCount() + " Scanned Job" + (scanAdapter.getCount() > 1 ? "s" : "") + ":\n\n");
                }
                for (Scan s : scans) {
                    if (s.clauseID > 0) {
                        message.append(s.scanBarCode).append(" ").append(s.clauseCode).append(" ").append("\n");
                    } else {
                        message.append(s.scanBarCode).append("               ").append("\n");
                    }
                }
            }

            builder.setView(setText(c, message.toString(), f));
        } else {
            if (f == 1) {
                builder.setView(setText(c, getString(R.string.confirm_message_finish), f));
            } else if (f == 2) {
                builder.setView(setText(c, getString(R.string.confirm_message_cancel), f));
            } else {
                builder.setView(setText(c, getString(R.string.confirm_message_clear), f));
            }
        }

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                if (f == 1) {
                    dialog.dismiss();
                    //((ScanActivity) c).uploadScans();
                    //((ScanGoodsForRackLocationRadioOneActivity) c).uploadScans();
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
                    //View vb = ((ScanActivity) c).findViewById(R.id.barcode_scanner);
                    //((ScanActivity) c).resume(vb);
                    if (f == 3) {
                        //((ScanActivity) c).onResume();
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

    public void displayMessage(Context c, String m) {
        Toast toast = Toast.makeText(c, m, Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) {
            v.setGravity(Gravity.CENTER);
            toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 275);
            toast.show();
        }
    }

    public void sortScans() {
        Collections.sort(scans, new Comparator<Scan>() {
            @Override
            public int compare(Scan s1, Scan s2) {
                return s1.scanBarCode.compareTo(s2.scanBarCode); // if you want to short by barcode
            }
        });
        int i = 1;
        for (Scan s : scans) {
            s.scanID = i++;
        }
    }

    public Boolean HasScans(SharedPreferences sharedPref) {

        String scansJson = sharedPref.getString("scans", "NothingFound");
        Boolean hasScans = false;
        if (scanAdapter == null) {
            scanAdapter = new ListScanAdapter(this);
            if (!scansJson.equals("NothingFound")) {
                setupScans(scansJson);
                sortScans();
            }
        }
        if (!scansJson.equals("NothingFound")) {
            if (scanAdapter.getCount() > 0) {
                hasScans = true;
            }
        }
        return hasScans;
    }

    public void setupScans(String scansJson) {
        scans.clear();
        try {
            //JSONObject s = new JSONObject(scansJson);
            JSONArray sa = new JSONArray(scansJson);
            for (int i = 0; i < sa.length(); i++) {
                JSONObject s = sa.getJSONObject(i);
                scans.add(new Scan(
                        s.getInt("scanID"),
                        s.getInt("clauseID"),
                        s.getString("clauseCode"),
                        s.getString("scanBarCode"),
                        s.getString("scanDateTime")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        scanAdapter.notifyDataSetChanged();
    }

    public String saveScans() {
        // Create object of SharedPreferences.
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);
        //now get Editor
        SharedPreferences.Editor editor = sharedPref.edit();

        /*Save Logic*/
        JSONArray sa = new JSONArray();
        for (Scan s : scans) {
            JSONObject sc = new JSONObject();
            try {
                sc.put("scanID", s.scanID);
                sc.put("clauseID", s.clauseID);
                sc.put("clauseCode", s.clauseCode);
                sc.put("scanBarCode", s.scanBarCode);
                sc.put("scanDateTime", s.scanDateTime);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sa.put(sc);
        }
        editor.putString("scans", sa.toString());
        editor.apply();
        return sa.toString();
    }

    public void setupRacks(String racksJson) {
        racks.clear();
        try {
            //JSONObject s = new JSONObject(scansJson);
            JSONArray ta = new JSONArray(racksJson);
            for (int i = 0; i < ta.length(); i++) {
                JSONObject t = ta.getJSONObject(i);
                racks.add(new Rack(
                        t.getInt("rackID"),
                        t.getString("rackDescription")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        rackAdapter.notifyDataSetChanged();
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public String saveRacks() {
        // Create object of SharedPreferences.
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);
        //now get Editor
        SharedPreferences.Editor editor = sharedPref.edit();

        /*Save Logic*/
        JSONArray ta = new JSONArray();
        for (Rack t : racks) {
            JSONObject tc = new JSONObject();
            try {
                tc.put("rackID", t.rackID);
                tc.put("rackDescription", t.rackDescription);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ta.put(tc);
        }
        editor.putString("racks", ta.toString());
        editor.apply();
        return ta.toString();
    }

    public String savePicklists() {
        // Create object of SharedPreferences.
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);
        //now get Editor
        SharedPreferences.Editor editor = sharedPref.edit();

        /*Save Logic*/
        JSONArray ta = new JSONArray();
        for (PickList t : pickLists) {
            JSONObject tc = new JSONObject();
            try {
                tc.put("picklistID", t.pickListID);
                tc.put("picklistDescription", t.pickListDescription);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ta.put(tc);
        }
        editor.putString("picklists", ta.toString());
        editor.apply();
        return ta.toString();
    }

    public TextView setProgressTitle(Context c) {
        // Create a TextView programmatically.
        TextView tv = new TextView(c);

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

    public void readTelephoneDetails() {

        TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null && tm.getDeviceId() != null) {
            tmDevice = "" + tm.getDeviceId();
        } else {
            tmDevice = "";
        }
        if (tm != null && tm.getSimSerialNumber() != null) {
            tmSerial = "" + tm.getSimSerialNumber();
        } else {
            tmSerial = "";
        }
        if (tm.getLine1Number() != null) {
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
        if (tm.getCellLocation() != null) {
            tmCellLocation = tm.getCellLocation().toString();
        } else {
            tmCellLocation = "";
        }
    }
}