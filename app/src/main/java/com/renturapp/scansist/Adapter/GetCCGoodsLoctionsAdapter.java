package com.renturapp.scansist.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.renturapp.scansist.Model.GetCCGoodsLocationModel;
import com.renturapp.scansist.Model.GetCCGoodsModel;
import com.renturapp.scansist.R;

import java.util.Collections;
import java.util.List;

public class GetCCGoodsLoctionsAdapter extends ArrayAdapter<GetCCGoodsModel> {

    List<GetCCGoodsLocationModel> data = Collections.emptyList();
    public static GetCCGoodsModel golbalCurrent;

    public GetCCGoodsLoctionsAdapter(Context context, int resource, List<GetCCGoodsLocationModel> data) {
        super(context, resource);
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater
                    .from(getContext())
                    .inflate(R.layout.single_frame_get_ccgoods_locations, parent, false);
        }

        TextView textViewOne = (TextView) convertView.findViewById(R.id.textViewOne);
        TextView three = (TextView) convertView.findViewById(R.id.three);
        TextView four = (TextView) convertView.findViewById(R.id.four);
        TextView five = (TextView) convertView.findViewById(R.id.five);
        TextView six = (TextView) convertView.findViewById(R.id.six);

        GetCCGoodsLocationModel current = data.get(position);

        textViewOne.setText(current.cCGoodDescription);
        three.setText("Length: " + current.cCGoodLength.toString());
        four.setText("Width:" + current.cCGoodWidth.toString());
        five.setText("Height: " + current.cCGoodHeight.toString());
        six.setText("Weight: " + current.cCGoodGrossWeight.toString());

        return convertView;
    }

    //This Count is important to Show Data on Screen
    @Override
    public int getCount() {
        return data.size();
    }
}
