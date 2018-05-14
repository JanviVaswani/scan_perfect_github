package com.renturapp.scansist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
        setupTrunks();
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

    public  MenuItem getFlash() { return this.flash; }

    public  void setFlash(MenuItem flash) { this.flash = flash; }

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
    public TextView setTitle(Context c, String s) {
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
    public TextView setText(Context c,String s){
        TextView m = new TextView(c);
        m.setText("\n" + s);
        m.setTextSize(15);
        m.setGravity(Gravity.CENTER_HORIZONTAL);
        return m;
    }
    public void messageBox(Context ctx,Boolean finish, Boolean timeout) {

        final Context c = ctx;
        final Boolean f = finish;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        String message = "";
        if (f) {
            builder.setCustomTitle(setTitle(c, getString(R.string.confirm_finish)));
        } else {
            builder.setCustomTitle(setTitle(c, getString(R.string.confirm_cancel)));
        }
        if (!scans.isEmpty()) {
          if (f){
            int d = 0;
            for (Scan s : scans) {
              if (s.clauseID>0){
                d++;
              }
            }
            if (d>0) {
              message = "Upload " + scanAdapter.getCount() + " Scanned Job" + (scanAdapter.getCount() > 1 ? "s" : "") + "\n"
              + d +" Damaged:\n\n";
            } else {
              message = "Upload " + scanAdapter.getCount() + " Scanned Job" + (scanAdapter.getCount() > 1 ? "s" : "") + ":\n\n";
            }

            for (Scan s : scans) {
              message += "i" + s.scanID + s.scanBarCode + " " + s.clauseCode + " " +"\n";
            }
          } else {
            message = "Cancel the ScanSist™ App?\n\nClear " + scanAdapter.getCount() + " Scanned Job" + (scanAdapter.getCount() > 1 ? "s" : "") + ":\n\n";
            for (Scan s : scans) {
                if (s.clauseID>0) {
                  message += "i" + s.scanID + " " + s.scanBarCode + " " + s.clauseCode + " " + "\n";
                } else {
                  message += "i" + s.scanID + " " + s.scanBarCode + "               " + "\n";
                }
            }

          }

          builder.setView(setText(c,message));
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
                 ((ScanActivity)c).uploadScans();
             } else {
               // Clear shared preferences
               SharedPreferences sharedPref = c.getApplicationContext().getSharedPreferences("ScanSist", 0);
               SharedPreferences.Editor editor = sharedPref.edit();
               editor.clear();   //its clear all data.
               editor.commit();  //Don't forgot to commit  SharedPreferences.

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
        Button p = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        // Set the layout parameters for TextView
        //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
        //        LinearLayout.LayoutParams.WRAP_CONTENT, // Width of Button
        //        LinearLayout.LayoutParams.WRAP_CONTENT);//, // Height of Button
                //1);//Weight of Button

        //lp.setMargins(0,5,0,0);

        //n.setLayoutParams(lp);
        //n.setBackgroundColor(getResources().getColor(R.color.materialGrey300));
        //n.setTextColor(getResources().getColor(R.color.black));
        n.setGravity(Gravity.LEFT);
        n.setWidth(150);

        //p.setLayoutParams(lp);
        //p.setBackgroundColor(getResources().getColor(R.color.materialGrey300));
        //p.setTextColor(getResources().getColor(R.color.black));
        //p.setGravity(Gravity.RIGHT);
        //p.setWidth(250);

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
    public void displayMessage(Context c,String m) {
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
                clauses.add(new Clause( 0, "    ", "Select a Clause for the Pallet/Parcel"));
                clauses.add(new Clause( 6, "DBDD", "Damaged on Delivery At Depot"));
                clauses.add(new Clause( 7, "DOAD", "Damaged on Arrival At Depot"));
                clauses.add(new Clause( 8, "INPK", "Inadequate Packaging"));
                clauses.add(new Clause( 9, "MISR", "Mis-Routed"));
                clauses.add(new Clause(10, "NOPW", "No Customer Paperwork"));
                clauses.add(new Clause(11, "NOTM", "Not Manifested"));
                clauses.add(new Clause(12, "PLTD", "Pallet Discrepancy"));
                clauses.add(new Clause(13, "SHRT", "Shortage"));
                break;
            case R.id.rBtnToHub:
                clauses.add(new Clause( 0, "    ", "Select a Clause for the Pallet/Parcel"));
                clauses.add(new Clause( 4, "DRFT", "Depot Removed From Trunk"));
                clauses.add(new Clause( 5, "DACD", "Damaged on Collecting At Depot"));
                clauses.add(new Clause(10, "NOPW", "No Customer Paperwork"));
                break;
            case R.id.rBtnOntoDelivery:
                clauses.add(new Clause( 0, "    ", "Select a Clause for the Pallet/Parcel"));
                clauses.add(new Clause( 7, "DOAD", "Damaged on Arrival At Depot"));
                clauses.add(new Clause(10, "NOPW", "No Customer Paperwork"));
                clauses.add(new Clause(11, "NOTM", "Not Manifested"));
                break;
            default:
                //Do Nothing;
                break;
        }
        clauseAdapter.notifyDataSetChanged();
    }
    private void setupTrunks() {

        trunkAdapter = new ListTrunkAdapter(this);
        //trunks.clear();
        trunks.add(new Trunk(0, "Select a Trunk"));
        trunks.add(new Trunk(1, "Trunk 1"));
        trunks.add(new Trunk(2, "Trunk 2"));
        trunks.add(new Trunk(3, "Trunk 3"));
        trunks.add(new Trunk(4, "Trunk 4"));
        trunks.add(new Trunk(5, "Trunk 5"));
        trunks.add(new Trunk(6, "Trunk 6"));
        trunks.add(new Trunk(7, "Trunk 7"));
        trunks.add(new Trunk(8, "Trunk 8"));
        trunks.add(new Trunk(9, "Trunk 9"));
        trunks.add(new Trunk(10,"Trunk 10"));
        trunkAdapter.notifyDataSetChanged();

    }
    public void sortScans() {
        Collections.sort(scans, new Comparator<Scan>() {
            @Override
            public int compare(Scan s1, Scan s2) {
                return s1.scanBarCode.compareTo(s2.scanBarCode); // if you want to short by barcode
            }
        });
        int i = 1;
        for (Scan s:scans){
            s.scanID = i++;
        }
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
        editor.commit();
        return sa.toString();
    }
}