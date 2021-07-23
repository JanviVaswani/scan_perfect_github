package com.renturapp.scansist;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class AboutActivity extends Activity {

  private Context context;
  private Button btnClose;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.scansist_about);

    context = AboutActivity.this;

    btnClose = (Button) findViewById(R.id.btnAbout);
    btnClose.setOnClickListener( new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        btnClose.setAlpha(0.5f);
        //AboutActivity.super.onBackPressed();
        finish();
      }
    });

  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
      WindowManager.LayoutParams.FLAG_FULLSCREEN);
  }
}
