package com.renturapp.scansist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class ListClauseAdapter extends BaseAdapter implements SpinnerAdapter {

    private final Utility u;

    ListClauseAdapter(Utility u) {
        this.u = u;
    }

    @Override
    public int getCount() {
        return u.clauses.size();
    }

    @Override
    public Object getItem(int position) {
        return u.clauses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolderItem {
        TextView ID;
        TextView Code;
        TextView Description;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListClauseAdapter.ViewHolderItem holder = new ListClauseAdapter.ViewHolderItem();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) u.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cell, null);

            holder.ID = (TextView) convertView.findViewById(R.id.ID);
            holder.Code = (TextView) convertView.findViewById(R.id.Code);
            holder.Description = (TextView) convertView.findViewById(R.id.Description);
            convertView.setTag(holder);
        } else {
            holder = (ListClauseAdapter.ViewHolderItem) convertView.getTag();
        }
        holder.ID.setText(String.valueOf(this.u.clauses.get(position).clauseID));
        holder.Code.setText(String.valueOf(this.u.clauses.get(position).clauseCode));
        holder.Description.setText(String.valueOf(this.u.clauses.get(position).clauseDescription));

        return convertView;
    }

}
