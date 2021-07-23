package com.renturapp.scansist.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.renturapp.scansist.Model.GetCCGoodsModel;
import com.renturapp.scansist.R;
import com.renturapp.scansist.Model.ScanGoodsModel;

import java.util.Collections;
import java.util.List;

public class GetCCPickListGoodsAdapter extends ArrayAdapter<GetCCGoodsModel> {

    List<ScanGoodsModel> data = Collections.emptyList();
    public static GetCCGoodsModel golbalCurrent;

    public GetCCPickListGoodsAdapter(Context context, int resource, List<ScanGoodsModel> data) {
        super(context, resource);
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater
                    .from(getContext())
                    .inflate(R.layout.single_frame_get_ccpicklistgoods, parent, false);
        }

        ScanGoodsModel current = data.get(position);

        //Read View Ids of Components
        TextView txtgoodsId = (TextView) convertView.findViewById(R.id.goodsIdText);
        TextView txtrackDesc = (TextView) convertView.findViewById(R.id.rackDescText);
        TextView txtgoodsDesc = (TextView) convertView.findViewById(R.id.goodsDescText);
        TextView txtnoOfPieces = (TextView) convertView.findViewById(R.id.noOfPiecesText);
        TextView txtpackageType = (TextView) convertView.findViewById(R.id.packagingTypeText);
        TextView txtweight = (TextView) convertView.findViewById(R.id.weightText);
        TextView txtdimension = (TextView) convertView.findViewById(R.id.dimensionsText);
        TextView txtgoodsvalue = (TextView) convertView.findViewById(R.id.valueText);

        //Set Values to Components
        txtgoodsId.setText(current.cCGoodID.toString());
        txtrackDesc.setText(current.rackDescription.toString());
        txtgoodsDesc.setText(current.cCGoodDescription.toString());
        txtnoOfPieces.setText(current.cCGoodQuantity.toString());
        txtpackageType.setText(current.cCGoodPackagingType);
        txtweight.setText("Gross - " + current.cCGoodGrossWeight.toString() + ",Net - " + current.cCGoodNetWeight.toString());
        txtdimension.setText(current.cCGoodLength + " X " + current.cCGoodWidth + " X " + current.cCGoodHeight);
        txtgoodsvalue.setText(current.cCGoodValue.toString());

        return convertView;
    }

    //This Count is important to Show Data on Screen
    @Override
    public int getCount() {
        return data.size();
    }
}
