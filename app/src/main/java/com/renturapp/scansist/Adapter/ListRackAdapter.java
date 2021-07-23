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

public class ListRackAdapter extends BaseAdapter implements SpinnerAdapter {

    private final Utility u;

    public ListRackAdapter(Utility u) {
        this.u = u;
    }

    @Override
    public int getCount() {
        return u.racks.size();
    }

    @Override
    public Object getItem(int position) {
        //return null;
        return u.racks.get(position);
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
        TextView Description;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem holder = new ViewHolderItem();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) u.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cell, null);

            holder.ID = (TextView) convertView.findViewById(R.id.ID);
            holder.Description = (TextView) convertView.findViewById(R.id.Description);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolderItem) convertView.getTag();
        }
        holder.ID.setText(String.valueOf(this.u.racks.get(position).rackID));
        holder.Description.setText(String.valueOf(this.u.racks.get(position).rackDescription));

        return convertView;
    }

}
