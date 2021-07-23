package com.renturapp.scansist.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.renturapp.scansist.R;
import com.renturapp.scansist.Utility;

public class ListScanAdapter extends BaseAdapter implements SpinnerAdapter {

    private final Utility u;

    public ListScanAdapter(Utility u) {
        this.u = u;
    }

    @Override
    public int getCount() {
        return u.scans.size();
    }

    @Override
    public Object getItem(int position) {
        //return null;
        return u.scans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
        //return 0;
        //http://stackoverflow.com/questions/6497871/create-adapter-to-fill-spinner-with-objects
        //return main.racks.get(position).getId();
    }

    static class ViewHolderItem {

        TextView ID;
        TextView Code;
        TextView Description;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem holder = new ViewHolderItem();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) u.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cell, null);

            holder.ID = (TextView) convertView.findViewById(R.id.ID);
            holder.Code = (TextView) convertView.findViewById(R.id.Code);
            holder.Description = (TextView) convertView.findViewById(R.id.Description);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolderItem) convertView.getTag();
        }
        holder.ID.setText(String.valueOf(this.u.scans.get(position).scanID));
        holder.Code.setText(String.valueOf(this.u.scans.get(position).clauseCode));
        holder.Description.setText(String.valueOf(this.u.scans.get(position).scanBarCode));

        return convertView;
    }

}
