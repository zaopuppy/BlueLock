package com.example.zero.androidskeleton.sort;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BtLeDevice;

public class SortAdapter extends ArrayAdapter<BtLeDevice> implements SectionIndexer {
    private List<BtLeDevice> list = null;
    private Context mContext;
    private int mResourceId;

    public SortAdapter(Context context, int resource, List<BtLeDevice> list) {
        super(context, resource, list);
        this.mContext = context;
        this.list = list;
        this.mResourceId = resource;
    }


    public void updateListView(List<BtLeDevice> list){
        this.list = list;
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.list.size();
    }

    public BtLeDevice getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup arg2) {
        ViewHolder viewHolder = null;
        final BtLeDevice mContent = list.get(position);
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(mResourceId, null);
            viewHolder.tvTitle = (TextView) view.findViewById(R.id.name);
            viewHolder.tvLetter = (TextView) view.findViewById(R.id.address);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }


        int section = getSectionForPosition(position);

    
        viewHolder.tvTitle.setText(this.list.get(position).getName());
        viewHolder.tvLetter.setText(this.list.get(position).getAddress());
        return view;

    }
    


    final static class ViewHolder {
        TextView tvLetter;
        TextView tvTitle;
    }


    public int getSectionForPosition(int position) {
        return list.get(position).getAddress().charAt(0);
    }


    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = list.get(i).getAddress();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        
        return -1;
    }
    

    private String getAlpha(String str) {
        String  sortStr = str.trim().substring(0, 1).toUpperCase();
        if (sortStr.matches("[A-Z]")) {
            return sortStr;
        } else {
            return "#";
        }
    }

    @Override
    public Object[] getSections() {
        return null;
    }
}