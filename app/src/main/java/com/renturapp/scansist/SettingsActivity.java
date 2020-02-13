package com.renturapp.scansist;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class SettingsActivity extends Activity {

  //private MainActivity mA;
  Context context;
  private Button btnClose;
  TextView txtScannerID;
  TextView txtDepotNumber;
  TextView txtAndroidID;
  TextView txtCompanyName;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.scansist_settings);

    context = SettingsActivity.this;
    btnClose = (Button) findViewById(R.id.btnSettings);


    btnClose.setOnClickListener( new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        btnClose.setAlpha(0.5f);
        //SettingsActivity.super.onBackPressed();
        finish();
      }
    });


    txtScannerID = (TextView) findViewById(R.id.txtScannerID);
    txtDepotNumber = (TextView) findViewById(R.id.txtDepotNumber);
    txtAndroidID = (TextView) findViewById(R.id.txtAndroidID);
    txtCompanyName = (TextView) findViewById(R.id.txtCompanyName);


    //Must use default preference file!
    //String androidID = PreferenceManager.getDefaultSharedPreferences(context).getString("RegKey", "NothingFound");
    String androidID   = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    String depotNumber = PreferenceManager.getDefaultSharedPreferences(context).getString("DepotNumber", "NothingFound");
    String scannerID   =  String.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getInt("ScanSistCode", MODE_PRIVATE));
    String companyName = PreferenceManager.getDefaultSharedPreferences(context).getString("CompanyName", "NothingFound");
    txtAndroidID.setText(androidID);
    txtDepotNumber.setText(depotNumber);
    txtScannerID.setText(scannerID);
    txtCompanyName.setText(companyName);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
      WindowManager.LayoutParams.FLAG_FULLSCREEN);
  }
}