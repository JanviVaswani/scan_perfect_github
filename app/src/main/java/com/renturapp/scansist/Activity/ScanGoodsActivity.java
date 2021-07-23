package com.renturapp.scansist.Activity;

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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;

import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.renturapp.scansist.AboutActivity;
import com.renturapp.scansist.AsynTask.GetRackGoodsAsyncTask;
import com.renturapp.scansist.AsynTask.GetRackGoodsAsyncTaskResultEvent;

import com.renturapp.scansist.LicenceActivity;
import com.renturapp.scansist.MainActivity;
import com.renturapp.scansist.Model.ScanGoodsModel;
import com.renturapp.scansist.MyAsyncBus;
import com.renturapp.scansist.R;
import com.renturapp.scansist.Model.ScanModel;
import com.renturapp.scansist.SettingsActivity;
import com.renturapp.scansist.Utility;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScanGoodsActivity extends Activity implements
        DecoratedBarcodeView.TorchListener {


  private Utility u;

  private static final String TAG = ScanGoodsActivity.class.getSimpleName();
  private DecoratedBarcodeView barcodeView;
  private BeepManager beepManager;
  private String lastText;
  private TextView lblScan;
  private Boolean initClause;
  private Spinner spnClause;
  private Button btnNext;
  private int scanCount;

  private Context context;

  private String mRack,mManifestDate,mDirection,mStatus;


  List<ScanGoodsModel> globalDataGoods = Collections.emptyList();
  List<ScanModel> globalDataLocation = Collections.emptyList();

  private static final int DELAY = 2000; // 2 second

  private BarcodeCallback callback = new BarcodeCallback() {
    private long lastTimestamp = 0;

    @Override
    public void barcodeResult(BarcodeResult result) {
      if(System.currentTimeMillis() - lastTimestamp < DELAY) {
        return;
      }

      if (result.getText() == null) {
        // Prevent nulls
        return;
      }


      boolean barcodeexists=false;
      int rId=0;
      String rDesc="";
      int rGoodRackId=0;

      int requestID=0;
      int goodQuantity=0;
      String buyer="";
      String seller="";


      if(result.getText().startsWith("00")) {
        for (ScanGoodsModel singleGood : globalDataGoods) {
          if (singleGood.cCGoodBarcode.equals(result.getText())) {
            barcodeexists = true;
            rDesc = singleGood.rackDescription;
            rGoodRackId = singleGood.cCGoodRackID;

            requestID = singleGood.cCRequestID;
            goodQuantity = singleGood.cCGoodQuantity;
            buyer = (String) singleGood.buyerStreet;
            seller = (String) singleGood.sellerStreet;

          }
        }

        if (barcodeexists) {
          Intent intent = new Intent(ScanGoodsActivity.this, GetCCGoodsLocationsActivity.class);
          intent.putExtra("rackDescription", rDesc);
          intent.putExtra("goodRackID", rGoodRackId);

          intent.putExtra("requestID", requestID);
          intent.putExtra("goodQuantity", goodQuantity);
          intent.putExtra("buyer", buyer);
          intent.putExtra("seller", seller);
          startActivity(intent);
          finish();
        } else {
          // Too soon after the last barcode - ignore.
          Toast.makeText(ScanGoodsActivity.this, "Invalid Good Barcode", Toast.LENGTH_SHORT).show();
          beepManager.playBeepSoundAndVibrate();
          lastTimestamp = System.currentTimeMillis();
          return;

        }
      }
      else{
        Toast.makeText(ScanGoodsActivity.this, "Not a Good Barcode", Toast.LENGTH_SHORT).show();
        beepManager.playBeepSoundAndVibrate();
        lastTimestamp = System.currentTimeMillis();
        return;
      }
    }
    @Override
    public void possibleResultPoints(List<ResultPoint> resultPoints) {
    }
  };


  private void setNextFinish(Boolean enabled){
    if(enabled) {
      btnNext.setClickable(true);
      btnNext.setAlpha(1f);
    } else {
      btnNext.setClickable(false);
      btnNext.setAlpha(0.5f);
    }
  }



  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_scan_goods);

    context = ScanGoodsActivity.this;
    String jsonUrl ="https://www.movesist.uk/data/ccgoods/?CompanyID=" + u.RegCompanyId + "&getType=6";
    GetRackGoodsAsyncTask scanAsyncTask  = new GetRackGoodsAsyncTask();
    scanAsyncTask.execute(jsonUrl);

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
      ((TextView) findViewById(R.id.lblScanRack)).setText("Scan Goods");
      //Scan GoodsRack: " + rackID
    }
    mRack = String.format(Locale.UK, "%02d",rackID);

    String scanDateTime = intent.getStringExtra("scanDateTime");

    if (!scanDateTime.isEmpty()) {
    } else {
      scanDateTime = sharedPref.getString("scanDateTime", "NothingFound");
      if (!scanDateTime.equals("NothingFound")){
      }
    }
    mManifestDate = scanDateTime.substring(0,10);

    setNextFinish(u.HasScans(sharedPref) && u.isOnline() );

    barcodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_scanner);
    barcodeView.decodeContinuous(callback);

    beepManager = new BeepManager(this);
    beepManager.setVibrateEnabled(true);

    barcodeView.setTorchListener(this);
  }

  //Async Result for All Goods
  @Subscribe
  public void onAsyncGetRackGoodsAsyncTaskResultEvent(GetRackGoodsAsyncTaskResultEvent event) {
    List<ScanGoodsModel> data=new ArrayList<>();
    try {
      JSONArray jArray = new JSONArray(event.getResult());

      // Extract data from json and store into ArrayList as class objects
      for(int i = 0; i < jArray.length(); i++){
        JSONObject json_data = jArray.getJSONObject(i);

        ScanGoodsModel scanGoodsModel = new ScanGoodsModel();

        scanGoodsModel.cCGoodBarcode= json_data.getString("CCGoodBarcode");

        scanGoodsModel.rackDescription= json_data.getString("RackDescription");
        scanGoodsModel.cCGoodRackID= json_data.getInt("CCGoodRackID");

        scanGoodsModel.cCRequestID= json_data.getInt("CCRequestID");
        scanGoodsModel.cCGoodQuantity= json_data.getInt("CCGoodQuantity");
        scanGoodsModel.buyerStreet= json_data.getString("BuyerStreet");
        scanGoodsModel.sellerStreet= json_data.getString("SellerStreet");

        data.add(scanGoodsModel);
      }
      globalDataGoods = data;
    } catch (JSONException e) {
      Toast.makeText(ScanGoodsActivity.this, e.toString(), Toast.LENGTH_LONG).show();
    }
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
      registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
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
      if (today.toString().contains("BST") || today.toString().contains("GMT+01:00") || today.toString().contains("GMT+05:30")) {
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
        u.messageBox(context, 1, false);
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
        nextScreen = new Intent(ScanGoodsActivity.this, SettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(nextScreen);
        //finish();
        break;
      case R.id.action_about:
        nextScreen = new Intent(ScanGoodsActivity.this, AboutActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(nextScreen);
        //finish();
        break;
      case R.id.action_licence:
        nextScreen = new Intent(ScanGoodsActivity.this, LicenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(nextScreen);
        //finish();
        break;
      case R.id.action_home:
        // User choose the "Setup home Option" action, go to home screen
        //changeMenuItemState(h, false, true, false);
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

    Intent previousScreen = new Intent(ScanGoodsActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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


  //Inner Class 1 for Goods
  /*public class ScanGoodsAsyncTask extends AsyncTask<String, String, String>{
    //ProgressDialog pdLoading = new ProgressDialog(ScanGoodsActivity.this);
    HttpURLConnection conn;
    URL url = null;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
      try {
        String jsonUrl ="https://www.movesist.uk/data/ccgoods/?CompanyID=" + u.RegCompanyId + "&getType=6";
        String jsonGetString;

        URL url = new URL(jsonUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setInstanceFollowRedirects(true);
        InputStream inputStream = httpURLConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuffer stringBuffer = new StringBuffer();
        while ((jsonGetString = bufferedReader.readLine()) != null){
          stringBuffer.append(jsonGetString + "\n");
        }

        bufferedReader.close();
        inputStream.close();
        httpURLConnection.disconnect();
        return stringBuffer.toString().trim();
      }
      catch (MalformedURLException e) {
        e.printStackTrace();
      }
      catch (IOException e) {
        e.printStackTrace();
      }

      return null;
    }

    @Override
    protected void onPostExecute(String result) {
      //this method will be running on UI thread
      //pdLoading.dismiss();
      List<ScanGoodsModel> data=new ArrayList<>();
      //pdLoading.dismiss();
      try {
        JSONArray jArray = new JSONArray(result);

        // Extract data from json and store into ArrayList as class objects
        for(int i = 0; i < jArray.length(); i++){
          JSONObject json_data = jArray.getJSONObject(i);

          ScanGoodsModel scanGoodsModel = new ScanGoodsModel();

          scanGoodsModel.cCGoodBarcode= json_data.getString("CCGoodBarcode");

          scanGoodsModel.rackDescription= json_data.getString("RackDescription");
          scanGoodsModel.cCGoodRackID= json_data.getInt("CCGoodRackID");

          scanGoodsModel.cCRequestID= json_data.getInt("CCRequestID");
          scanGoodsModel.cCGoodQuantity= json_data.getInt("CCGoodQuantity");
          scanGoodsModel.buyerStreet= json_data.getString("BuyerStreet");
          scanGoodsModel.sellerStreet= json_data.getString("SellerStreet");

          data.add(scanGoodsModel);
        }
        globalDataGoods = data;
      } catch (JSONException e) {
        Toast.makeText(ScanGoodsActivity.this, e.toString(), Toast.LENGTH_LONG).show();
      }
    }
  }*/

}