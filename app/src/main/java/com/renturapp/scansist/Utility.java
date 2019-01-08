package com.renturapp.scansist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

//https://github.com/codepath/android_guides/wiki/Understanding-the-Android-Application-Class
public class Utility extends Application {

  public ArrayList<Trunk> trunks = new ArrayList<Trunk>();
  public ListTrunkAdapter trunkAdapter;
  public ArrayList<Clause> clauses = new ArrayList<Clause>();
  public ListClauseAdapter clauseAdapter;
  public ArrayList<Scan> scans = new ArrayList<Scan>();
  public ListScanAdapter scanAdapter;

  public Utility() {
    //setupTrunks();
  }

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

  private TextView setText(Context c, String s) {
    TextView m = new TextView(c);
    String text = "\n" + s;
    m.setText(text);
    m.setTextSize(15);
    m.setGravity(Gravity.CENTER_HORIZONTAL);
    return m;
  }

  public void messageBox(Context ctx, Boolean finish, Boolean timeout) {

    final Context c = ctx;
    final Boolean f = finish;
    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
    StringBuilder message;
    if (f) {
      builder.setCustomTitle(setTitle(c, getString(R.string.confirm_finish)));
    } else {
      builder.setCustomTitle(setTitle(c, getString(R.string.confirm_cancel)));
    }
    if (!scans.isEmpty()) {
      if (f) {
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
        message = new StringBuilder("Cancel the ScanSist™ App?\n\nClear " + scanAdapter.getCount() + " Scanned Job" + (scanAdapter.getCount() > 1 ? "s" : "") + ":\n\n");
        for (Scan s : scans) {
          if (s.clauseID > 0) {
            message.append(s.scanBarCode).append(" ").append(s.clauseCode).append(" ").append("\n");
          } else {
            message.append(s.scanBarCode).append("               ").append("\n");
          }
        }

      }

      builder.setView(setText(c, message.toString()));
    } else {
      if (f) {
        builder.setView(setText(c, getString(R.string.confirm_message_finish)));
      } else {
        builder.setView(setText(c, getString(R.string.confirm_message_cancel)));
      }
    }

    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

      public void onClick(DialogInterface dialog, int which) {

        if (f) {
          dialog.dismiss();
          ((ScanActivity) c).uploadScans();
        } else {
          // Clear shared preferences
          SharedPreferences sharedPref = c.getApplicationContext().getSharedPreferences("ScanSist", 0);
          SharedPreferences.Editor editor = sharedPref.edit();
          editor.clear();   //its clear all data.
          editor.apply();  //Don't forgot to commit  SharedPreferences.

          dialog.dismiss();
          ((Activity) c).finish();
          android.os.Process.killProcess(android.os.Process.myPid());
        }
      }
    });


    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        try {
          View vb = ((ScanActivity) c).findViewById(R.id.barcode_scanner);
          ((ScanActivity) c).resume(vb);
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

  public void setupClauses(int Id) {

    if (clauseAdapter == null) {
      clauseAdapter = new ListClauseAdapter(this);
    }
    if (!clauses.isEmpty()) {
      clauses.clear();
    }
    switch (Id) {
      case R.id.rBtnFromHub:
        clauses.add(new Clause(0, "    ", "Select a Clause for the Pallet/Parcel"));
        clauses.add(new Clause(6, "DBDD", "Damaged on Delivery At Depot"));
        clauses.add(new Clause(7, "DOAD", "Damaged on Arrival At Depot"));
        clauses.add(new Clause(8, "INPK", "Inadequate Packaging"));
        clauses.add(new Clause(9, "MISR", "Mis-Routed"));
        clauses.add(new Clause(10, "NOPW", "No Customer Paperwork"));
        clauses.add(new Clause(11, "NOTM", "Not Manifested"));
        clauses.add(new Clause(12, "PLTD", "Pallet Discrepancy"));
        clauses.add(new Clause(13, "SHRT", "Shortage"));
        break;
      case R.id.rBtnToHub:
        clauses.add(new Clause(0, "    ", "Select a Clause for the Pallet/Parcel"));
        clauses.add(new Clause(4, "DRFT", "Depot Removed From Trunk"));
        clauses.add(new Clause(5, "DACD", "Damaged on Collecting At Depot"));
        clauses.add(new Clause(10, "NOPW", "No Customer Paperwork"));
        break;
      case R.id.rBtnOntoDelivery:
        clauses.add(new Clause(0, "    ", "Select a Clause for the Pallet/Parcel"));
        clauses.add(new Clause(7, "DOAD", "Damaged on Arrival At Depot"));
        clauses.add(new Clause(10, "NOPW", "No Customer Paperwork"));
        clauses.add(new Clause(11, "NOTM", "Not Manifested"));
        break;
      default:
        //Do Nothing;
        break;
    }
    clauseAdapter.notifyDataSetChanged();
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
      setupScans(scansJson);
      sortScans();
    }
    if(!scansJson.equals("NothingFound"))
    {

      if (scanAdapter.getCount() > 0) {
        hasScans =  true;
      } else {
        hasScans = false;
      }
    }
    return hasScans;
  }

  public void setupScans(String scansJson) {
        scans.clear();
        try {
            //JSONObject s = new JSONObject(scansJson);
            JSONArray sa = new JSONArray(scansJson);
            for (int i=0; i<sa.length();i++){
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
    public String saveScans () {
        // Create object of SharedPreferences.
        SharedPreferences sharedPref= getApplicationContext().getSharedPreferences("ScanSist",MODE_PRIVATE);
        //now get Editor
        SharedPreferences.Editor editor= sharedPref.edit();

        /*Save Logic*/
        JSONArray sa = new JSONArray();
        for (Scan s: scans) {
            JSONObject sc = new JSONObject();
            try {
                sc.put("scanID",s.scanID);
                sc.put("clauseID",s.clauseID);
                sc.put("clauseCode",s.clauseCode);
                sc.put("scanBarCode",s.scanBarCode);
                sc.put("scanDateTime",s.scanDateTime);
            } catch (JSONException e){
                e.printStackTrace();
            }
            sa.put(sc);
        }
        editor.putString("scans",sa.toString());
        editor.apply();
        return sa.toString();
    }

    public void setupTrunks(String trunksJson) {
        trunks.clear();
        try {
            //JSONObject s = new JSONObject(scansJson);
            JSONArray ta = new JSONArray(trunksJson);
            for (int i=0; i<ta.length();i++){
                JSONObject t = ta.getJSONObject(i);
                trunks.add(new Trunk(
                    t.getInt("trunkNumber"),
                    t.getString("trunkDescription")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        trunkAdapter.notifyDataSetChanged();
    }
  public boolean isOnline() {
    ConnectivityManager cm =
        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
  }
  public String saveTrunks () {
    // Create object of SharedPreferences.
    SharedPreferences sharedPref= getApplicationContext().getSharedPreferences("ScanSist",MODE_PRIVATE);
    //now get Editor
    SharedPreferences.Editor editor= sharedPref.edit();

    /*Save Logic*/
    JSONArray ta = new JSONArray();
    for (Trunk t: trunks) {
      JSONObject tc= new JSONObject();
      try {
        tc.put("trunkNumber",t.trunkNumber);
        tc.put("trunkDescription",t.trunkDescription);
      } catch (JSONException e){
        e.printStackTrace();
      }
      ta.put(tc);
    }
    editor.putString("trunks",ta.toString());
    editor.apply();
    return ta.toString();
  }

}