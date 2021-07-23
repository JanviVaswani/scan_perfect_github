package com.renturapp.scansist.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.renturapp.scansist.Adapter.GetCCGoodsAdapter;
import com.renturapp.scansist.Adapter.GetCCPickListGoodsAdapter;
import com.renturapp.scansist.AsynTask.GetPicklistGoodsAsyncTask;
import com.renturapp.scansist.AsynTask.GetPicklistGoodsAsyncTaskResultEvent;
import com.renturapp.scansist.AsynTask.GetRackGoodsAsyncTask;
import com.renturapp.scansist.AsynTask.GetRackGoodsAsyncTaskResultEvent;
import com.renturapp.scansist.MainActivity;
import com.renturapp.scansist.Model.GetCCGoodsModel;
import com.renturapp.scansist.Model.ScanGoodsModel;
import com.renturapp.scansist.MyAsyncBus;
import com.renturapp.scansist.R;
import com.renturapp.scansist.Utility;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetCCGoodsActivity extends Activity {
    private Utility u;

    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;

    private GetCCGoodsAdapter mAdapter;
    private GetCCPickListGoodsAdapter mPicklistAdapter;
    List<ScanGoodsModel> picklstgoodsList = new ArrayList<>();

    private ListView listView;
    TextView rackName;
    TextView lblgoodsavailability;
    Button finish;
    Button btnprev, btnscangoods, btncancel;//Wizard Buttons


    int rackIDValue, picklistIdValue;
    String rackDescriptionValue, picklistDescriptionValue;
    String goodsUrl;
    int status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyAsyncBus.getInstance().register(this);
        setContentView(R.layout.activity_get_ccgoods);

        //Setup Utility class object
        u = (Utility) getApplicationContext();

        //Components Declaration
        listView = (ListView) findViewById(R.id.listView);

        rackName = (TextView) findViewById(R.id.rackName);
        lblgoodsavailability = (TextView) findViewById(R.id.lblgoodsavailability);
        finish = (Button) findViewById(R.id.finish);

        //Wizard Buttons
        btnprev = (Button) findViewById(R.id.btnPrevious);
        btnscangoods = (Button) findViewById(R.id.btnNext);
        btncancel = (Button) findViewById(R.id.btnCancel);

        // Create object of SharedPreferences.
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ScanSist", MODE_PRIVATE);

        //Set Intent Values and Activity Status
        status = getIntent().getIntExtra("status", -1);
        if (status == -1) {
            status = sharedPref.getInt("status", -1);
        }

        //Set List View for Goods(Racks/Picklist)
        if (status == 3) {
            //Get Intents for Picklist
            picklistIdValue = getIntent().getIntExtra("picklistID", 0);
            picklistDescriptionValue = getIntent().getStringExtra("picklistDescription");

            //Set Text and Manage Navigation Buttons
            btnscangoods.setText("Scan Goods");
            finish.setVisibility(View.GONE);

            //Set PickList URL to fetch Goods
            goodsUrl = "https://movesist.uk/data/ccgoods/?getType=4&CompanyID=" + u.RegCompanyId + "&CCGoodPickListID=" + picklistIdValue;

            rackName.setText(picklistDescriptionValue);
            if (u.scanpicklistgoods.isEmpty()) {
                GetPicklistGoodsAsyncTask asyncTaskpicklistgoods = new GetPicklistGoodsAsyncTask();
                asyncTaskpicklistgoods.execute(goodsUrl);
            } else {
                for (ScanGoodsModel singlegood : u.scanpicklistgoods) {
                    //Set an object of ScanGoodsModel for Scanned Goods Out Class
                    if (singlegood.isGoodScanned == "N") {
                        picklstgoodsList.add(singlegood);
                    }
                }

                listView.setAdapter(null);
                mPicklistAdapter = new GetCCPickListGoodsAdapter(GetCCGoodsActivity.this, R.layout.single_frame_get_ccpicklistgoods, picklstgoodsList);
                listView.setAdapter(mPicklistAdapter);

                //To check if scanned barcodes are present for upload
                if (picklstgoodsList.size() <= 0) {
                    if (!u.scans.isEmpty()) {
                        lblgoodsavailability.setText("All Goods are Scanned");
                        lblgoodsavailability.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);

                        btnscangoods.setText("Continue");
                    } else {
                        btnscangoods.setEnabled(false);
                        btnscangoods.setClickable(false);
                    }
                }
            }
        } else if (status == 1) {
            rackIDValue = getIntent().getIntExtra("rackID", 0);
            rackDescriptionValue = getIntent().getStringExtra("rackDescription");

            rackName.setText(rackDescriptionValue);
            listView.setAdapter(null);

            String goodsUrl = "https://movesist.uk/data/ccgoods/?CompanyID=" + u.RegCompanyId + "&CCGoodRackID=" + rackIDValue + "&getType=5";
            GetRackGoodsAsyncTask asyncTask = new GetRackGoodsAsyncTask();
            asyncTask.execute(goodsUrl);

            //Set Text and Manage Navigation Buttons
            finish.setText("Finish");

            btnprev.setVisibility(View.GONE);
            btnscangoods.setVisibility(View.GONE);
            btncancel.setVisibility(View.GONE);
        }

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void onWizardButtonClicked(View v) {
        switch (v.getId()) {
            case R.id.btnPrevious:
                onBackPressed();
                break;
            case R.id.btnNext:
                //Scan Goods Button for Scanning picklist goods
                Bundle bundle = new Bundle();
                bundle.putInt("picklistid", picklistIdValue);
                bundle.putString("picklistdesc", picklistDescriptionValue);

                Intent intent = new Intent(GetCCGoodsActivity.this, ScanPickListGoodsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();

                break;
            case R.id.btnCancel:
                u.messageBox(GetCCGoodsActivity.this, 2, false);
                break;
            default:
                throw new RuntimeException("Unknow button ID");
        }
    }

    @Override
    public void onBackPressed() {
        u.scans.clear();
        u.scanpicklistgoods = Collections.EMPTY_LIST;

        Bundle bundle = new Bundle();
        bundle.putBoolean("onBackPressed", true);

        Intent previousScreen = new Intent(GetCCGoodsActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        previousScreen.putExtras(bundle);
        startActivity(previousScreen);
        finish();
        super.onBackPressed();
    }

    //Async Result for Fetching Rack Goods
    @Subscribe
    public void onAsyncGetRackGoodsAsyncTaskResultEvent(GetRackGoodsAsyncTaskResultEvent event) {
        //this method will be running on UI thread
        List<GetCCGoodsModel> data = new ArrayList<>();
        try {
            JSONArray jArray = new JSONArray(event.getResult());

            // Extract data from json and store into ArrayList as class objects
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject json_data = jArray.getJSONObject(i);

                GetCCGoodsModel goodsModel = new GetCCGoodsModel();

                goodsModel.cCRequestID = json_data.getInt("CCRequestID"); //***NAME FROM API URL***
                goodsModel.cCGoodQuantity = json_data.getInt("CCGoodQuantity");
                goodsModel.buyerStreet = json_data.getString("BuyerStreet");
                goodsModel.sellerStreet = json_data.getString("SellerStreet");
                goodsModel.rackDescription = json_data.getString("RackDescription");
                goodsModel.cCGoodPackagingType = json_data.getString("CCGoodPackagingType");
                goodsModel.cCGoodDescription = json_data.getString("CCGoodDescription");
                goodsModel.cCGoodLength = json_data.getInt("CCGoodLength");
                goodsModel.cCGoodWidth = json_data.getInt("CCGoodWidth");
                goodsModel.cCGoodHeight = json_data.getInt("CCGoodHeight");
                goodsModel.cCGoodGrossWeight = json_data.getDouble("CCGoodGrossWeight");
                goodsModel.cCGoodValue = json_data.getDouble("CCGoodValue");

                data.add(goodsModel);
            }

            if (data.isEmpty()) {
                lblgoodsavailability.setText("No Goods found for " + rackDescriptionValue);
                lblgoodsavailability.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
            } else {
                listView.setAdapter(null);
                mAdapter = new GetCCGoodsAdapter(GetCCGoodsActivity.this, R.layout.single_frame_get_ccgoods, data);
                listView.setAdapter(mAdapter);
            }

        } catch (JSONException e) {
            Toast.makeText(GetCCGoodsActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    //Async Result for Picklist Locations
    @Subscribe
    public void onAsyncGetPicklistGoodsAsyncTaskResultEvent(GetPicklistGoodsAsyncTaskResultEvent event) {
        try {
            JSONArray jArray = new JSONArray(event.getResult());

            // Extract data from json and store into ArrayList as class objects for PickList
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject json_data = jArray.getJSONObject(i);

                ScanGoodsModel goodsModel = new ScanGoodsModel();

                String gstatus = json_data.getString("CCGoodStatus");
                goodsModel.cCGoodRackID = Integer.parseInt(String.valueOf((json_data.getString("CCGoodRackID"))=="null"?0:(json_data.getString("CCGoodRackID"))));

                    if (gstatus.equals("1") && goodsModel.cCGoodRackID!=0) {
                        goodsModel.cCGoodID = json_data.getInt("CCGoodID");
                        goodsModel.cCGoodQuantity = json_data.getInt("CCGoodQuantity");
                        goodsModel.buyerStreet = json_data.getString("BuyerStreet");
                        goodsModel.sellerStreet = json_data.getString("SellerStreet");
                        goodsModel.rackDescription = json_data.getString("RackDescription");
                        goodsModel.cCGoodDescription = json_data.getString("CCGoodDescription");
                        goodsModel.cCGoodQuantity = json_data.getInt("CCGoodQuantity");
                        goodsModel.cCGoodPackagingType = json_data.getString("CCGoodPackagingType");
                        goodsModel.cCGoodGrossWeight = json_data.getDouble("CCGoodGrossWeight");
                        goodsModel.cCGoodNetWeight = json_data.getDouble("CCGoodNetWeight");
                        goodsModel.cCGoodLength = json_data.getInt("CCGoodLength");
                        goodsModel.cCGoodHeight = json_data.getInt("CCGoodHeight");
                        goodsModel.cCGoodWidth = json_data.getInt("CCGoodWidth");
                        goodsModel.cCGoodValue = json_data.getInt("CCGoodValue");
                        goodsModel.cCGoodBarcode = json_data.getString("CCGoodBarcode");
                        goodsModel.companyID = json_data.getInt("CompanyID");
                        //goodsModel.cCGoodRackID = Integer.parseInt(String.valueOf((json_data.getString("CCGoodRackID"))=="null"?0:(json_data.getString("CCGoodRackID"))));
                        goodsModel.cCGoodPickListID = json_data.getInt("CCGoodPickListID");
                        goodsModel.cCGoodStatus = "2";

                        picklstgoodsList.add(goodsModel);
                    }

            }

            if (picklstgoodsList.isEmpty()) {
                lblgoodsavailability.setText("No Goods found for " + picklistDescriptionValue);
                lblgoodsavailability.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);

                btnscangoods.setEnabled(false);
                btnscangoods.setClickable(false);
            } else {
                u.scanpicklistgoods = picklstgoodsList;

                // Setup data to listview
                listView.setAdapter(null);
                mPicklistAdapter = new GetCCPickListGoodsAdapter(GetCCGoodsActivity.this, R.layout.single_frame_get_ccpicklistgoods, picklstgoodsList);
                listView.setAdapter(mPicklistAdapter);
            }
        } catch (JSONException e) {
            Toast.makeText(GetCCGoodsActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

}

