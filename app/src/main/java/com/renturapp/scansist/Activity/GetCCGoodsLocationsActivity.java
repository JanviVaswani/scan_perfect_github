package com.renturapp.scansist.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.renturapp.scansist.AsynTask.GetRackGoodsAsyncTask;
import com.renturapp.scansist.AsynTask.GetRackGoodsAsyncTaskResultEvent;
import com.renturapp.scansist.Model.GetCCGoodsLocationModel;
import com.renturapp.scansist.Adapter.GetCCGoodsLoctionsAdapter;
import com.renturapp.scansist.MainActivity;
import com.renturapp.scansist.MyAsyncBus;
import com.renturapp.scansist.R;
import com.renturapp.scansist.Utility;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GetCCGoodsLocationsActivity extends Activity {
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    private ListView listView;
    private GetCCGoodsLoctionsAdapter mAdapter;
    private Utility u;

    TextView rackName;
    Button finish;

    TextView goodId;
    TextView goodQuantity;
    TextView goodSellerStreet;
    TextView goodBuyerStreet;
    TextView rackGoodsDetails;

    int goodRackIDValue;
    String rackDescriptionValue;

    int requestIDValue;
    int goodQuantityValue;
    String goodSellerStreetValue;
    String goodBuyerStreetValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyAsyncBus.getInstance().register(this);
        setContentView(R.layout.activity_get_ccgoods_locations);

        u = (Utility) getApplicationContext();

        goodId = (TextView) findViewById(R.id.goodId);
        goodQuantity = (TextView) findViewById(R.id.goodQuantity);
        goodSellerStreet = (TextView) findViewById(R.id.goodSellerStreet);
        goodBuyerStreet = (TextView) findViewById(R.id.goodBuyerStreet);
        rackGoodsDetails = (TextView) findViewById(R.id.rackGoodsDetails);

        goodRackIDValue = getIntent().getIntExtra("goodRackID", 0);
        rackDescriptionValue = getIntent().getStringExtra("rackDescription");

        requestIDValue = getIntent().getIntExtra("requestID", 0);
        goodQuantityValue = getIntent().getIntExtra("goodQuantity", 0);
        goodSellerStreetValue = getIntent().getStringExtra("seller");
        goodBuyerStreetValue = getIntent().getStringExtra("buyer");

        rackName = (TextView) findViewById(R.id.rackName);
        finish = (Button) findViewById(R.id.finish);

        rackName.setText(rackDescriptionValue);

        goodId.setText("Shipment ID: " + requestIDValue);
        goodQuantity.setText("No of Pieces: " + goodQuantityValue);

        if (goodSellerStreetValue.equals("null")) {
            goodSellerStreet.setText("Shipper: " + "");
        } else {
            goodSellerStreet.setText("Shipper: " + goodSellerStreetValue.replace("\n", ""));
        }

        if (goodBuyerStreetValue.equals("null")) {
            goodBuyerStreet.setText("Consignee: " + "");
        } else {
            goodBuyerStreet.setText("Consignee: " + goodBuyerStreetValue.replace("\n", ""));
        }

        rackGoodsDetails.setText(rackDescriptionValue + " Goods List");

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        String jsonUrl = "https://movesist.uk/data/ccgoods/?CompanyID=" + u.RegCompanyId + "&CCGoodRackID=" + goodRackIDValue + "&getType=5";
        GetRackGoodsAsyncTask asyncTask = new GetRackGoodsAsyncTask();
        asyncTask.execute(jsonUrl);
    }

    @Override
    public void onBackPressed() {

        Bundle bundle = new Bundle();
        bundle.putBoolean("onBackPressed", true);

        Intent previousScreen = new Intent(GetCCGoodsLocationsActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        previousScreen.putExtras(bundle);
        startActivity(previousScreen);
        finish();
        super.onBackPressed();
    }

    //Async Result for Rack Locations
    @Subscribe
    public void onAsyncGetRackGoodsAsyncTaskResultEvent(GetRackGoodsAsyncTaskResultEvent event) {
        List<GetCCGoodsLocationModel> data = new ArrayList<>();
        try {
            JSONArray jArray = new JSONArray(event.getResult());

            // Extract data from json and store into ArrayList as class objects
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject json_data = jArray.getJSONObject(i);

                GetCCGoodsLocationModel goodsModel = new GetCCGoodsLocationModel();

                goodsModel.cCRequestID = json_data.getInt("CCRequestID"); //***NAME FROM API URL***
                goodsModel.cCGoodQuantity = json_data.getInt("CCGoodQuantity");
                goodsModel.cCGoodDescription = json_data.getString("CCGoodDescription");
                goodsModel.cCGoodLength = json_data.getInt("CCGoodLength");
                goodsModel.cCGoodWidth = json_data.getInt("CCGoodWidth");
                goodsModel.cCGoodHeight = json_data.getInt("CCGoodHeight");
                goodsModel.cCGoodGrossWeight = json_data.getDouble("CCGoodGrossWeight");

                //To Store Data in Alert Dialog Box
                goodsModel.cCGoodID = json_data.getInt("CCGoodID");

                data.add(goodsModel);
            }

            // Setup and Handover data to listview
            listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(null);

            mAdapter = new GetCCGoodsLoctionsAdapter(GetCCGoodsLocationsActivity.this, R.layout.single_frame_get_ccgoods, data);
            listView.setAdapter(mAdapter);

        } catch (JSONException e) {
            Toast.makeText(GetCCGoodsLocationsActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}

