package com.hackathon.smessage.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hackathon.smessage.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.view.View.GONE;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class BlockedArrayAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private int mLayout;
    private ArrayList<String> mList, tmpList;
    private HashMap<Integer, Boolean> mSelected = new HashMap<Integer, Boolean>();

    public BlockedArrayAdapter(Context context, int resource, ArrayList<String> list) {
        super(context, resource, list);
        mContext = context;
        mLayout = resource;
        mList = list;
        mSelected = new HashMap<>();
        tmpList = new ArrayList<>();
        tmpList.addAll(list);
    }

    public int getCheckedCount(){
        int count = 0;
        for(int i = 0; i < mList.size(); i++){
            if(isItemChecked(i)){
                count++;
            }
        }
        return count;
    }

    public void setItemChecked(int position, boolean value) {
        mSelected.put(position, value);
        notifyDataSetChanged();
    }

    public void setAllChecked(boolean value) {
        for(int i = 0; i < mList.size(); i++) {
            mSelected.put(i, value);
        }
        notifyDataSetChanged();
    }

    public boolean isItemChecked(int position){
        Boolean icChecked = mSelected.get(position);
        if(icChecked == null){
            return false;
        }
        return icChecked;
    }

    public void removeChecked(int position) {
        mSelected.remove(position);
        notifyDataSetChanged();
    }

    public void clear() {
        mSelected.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mLayout, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.ivSelected = (ImageView) convertView.findViewById(R.id.ivSelected);
            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tvContent);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.tvContent.setText(mList.get(position));
        if(isItemChecked(position)){
            viewHolder.ivSelected.setVisibility(View.VISIBLE);
        }
        else{
            viewHolder.ivSelected.setVisibility(View.GONE);
        }
        return convertView;
    }

    private class ViewHolder{
        public ImageView ivSelected;
        public TextView tvContent;
    }

    public void filter(String query){
        query = query.toLowerCase(Locale.getDefault());
        mList.clear();

        if(query.length() == 0){
            mList.addAll(tmpList);
        }
        else {
            for(String string : tmpList){
                if(string.toLowerCase(Locale.getDefault()).equals(query)){
                    mList.add(string);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setFilter(ArrayList<String> arrayList) {

        //arrayList = new ArrayList<>(); // remove this line
        mList.clear(); // add this so that it will clear old data
        mList.addAll(arrayList);
        notifyDataSetChanged();
    }
}
