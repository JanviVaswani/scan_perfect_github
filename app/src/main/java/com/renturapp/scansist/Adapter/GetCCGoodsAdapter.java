package com.renturapp.scansist.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.renturapp.scansist.Model.GetCCGoodsModel;
import com.renturapp.scansist.R;

import java.util.Collections;
import java.util.List;

public class GetCCGoodsAdapter extends ArrayAdapter<GetCCGoodsModel> {

    List<GetCCGoodsModel> data = Collections.emptyList();
    public static GetCCGoodsModel golbalCurrent;

    public GetCCGoodsAdapter(Context context, int resource, List<GetCCGoodsModel> data) {
        super(context, resource);
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater
                    .from(getContext())
                    .inflate(R.layout.single_frame_get_ccgoods, parent, false);
        }

        /*TextView one = convertView.findViewById(R.id.one);
        TextView two = convertView.findViewById(R.id.two);
        TextView three = convertView.findViewById(R.id.three);
        TextView four = convertView.findViewById(R.id.four);*/

        TextView one = (TextView) convertView.findViewById(R.id.one);
        TextView two = (TextView) convertView.findViewById(R.id.two);
        TextView three = (TextView) convertView.findViewById(R.id.three);
        TextView four = (TextView) convertView.findViewById(R.id.four);

        GetCCGoodsModel current = data.get(position);

        one.setText("Shipment ID: " + current.cCRequestID.toString());
        two.setText("No. Pieces: " + current.cCGoodQuantity.toString());
        three.setText("Consignee: " + current.buyerStreet);
        //three.setText("Consignee: " + current.buyerStreet.replace("\n", ""));
        four.setText("Shipper: " + current.sellerStreet);
        //four.setText("Shipper: " + current.sellerStreet.replace("\n", ""));


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle("Good Details for")
                        .setCancelable(false)
                        .setMessage("Rack Location: " + current.rackDescription + "\n" +
                                "Good Packaging Type: " + current.cCGoodPackagingType + "\n" +
                                "Good Description: " + current.cCGoodDescription + "\n" +
                                "Good Quantity: " + current.cCGoodQuantity + "\n" +
                                "Dimensions (LxWxH): " + current.cCGoodLength + " x " + current.cCGoodWidth + " x " + current.cCGoodHeight + "\n" +
                                "Good Weight: " + current.cCGoodGrossWeight + "\n" +
                                "Good Value: " + current.cCGoodValue + "\n"
                        ).
                                setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .create();

                dialog.show();

            }
        });
        return convertView;
    }

    //This Count is important to Show Data on Screen
    @Override
    public int getCount() {
        return data.size();
    }
}
