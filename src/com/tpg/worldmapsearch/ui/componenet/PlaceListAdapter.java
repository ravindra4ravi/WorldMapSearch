package com.tpg.worldmapsearch.ui.componenet;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tpg.worldmapsearch.bean.Item;
import com.tpg.worldmapsearch.ui.LocationMapActivity;
import com.tpg.worldmapsearch.ui.R;



public class PlaceListAdapter extends BaseAdapter {

    private List<Object> mItems;
    private Context ctx;
    public PlaceListAdapter(Context ctx,List<Object> items) {
        mItems = items;
        this.ctx = ctx;
       
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) instanceof Item ? 0 : 1;
    }

   
   

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Object item = getItem(position);

            if (v == null) {
                v = ((LocationMapActivity)ctx).getLayoutInflater().inflate(R.layout.menu_row_item, parent, false);
            }
       
            TextView tv = (TextView) v;
            tv.setText(((com.tpg.worldmapsearch.bean.Item) item).mTitle);
            tv.setCompoundDrawablesWithIntrinsicBounds(((com.tpg.worldmapsearch.bean.Item) item).mIconRes, 0, 0, 0);


       

        return v;
    }
}