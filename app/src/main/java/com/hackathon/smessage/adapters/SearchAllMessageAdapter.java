package com.hackathon.smessage.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hackathon.smessage.R;
import com.hackathon.smessage.configs.Defines;
import com.hackathon.smessage.customViews.CircularImageView;
import com.hackathon.smessage.models.Contact;
import com.hackathon.smessage.models.Message;
import com.hackathon.smessage.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by ADMIN on 18/11/2017.
 */

public class SearchAllMessageAdapter extends BaseAdapter{

    private Context mContext;
    private int mLayout;
    private LayoutInflater mInflater;
    private ArrayList<Message> mList;
    private ArrayList<Message> tmpList;


    public SearchAllMessageAdapter(Context context, ArrayList<Message> list){
        mContext = context;
        mList = list;
        mInflater = LayoutInflater.from(mContext);
        tmpList = new ArrayList<>();
        tmpList.addAll(list);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;
        if(view == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.item_search, null);

            viewHolder = new ViewHolder();
            viewHolder.tvName =  view.findViewById(R.id.tvNameSearch);
            viewHolder.tvBody =  view.findViewById(R.id.tvMessageSearch);
            viewHolder.tvTime =  view.findViewById(R.id.tvTimeSearch);
            viewHolder.ivAvatar = (CircularImageView) view.findViewById(R.id.ivAvatarSearch);

            view.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Message message = mList.get(i);

        viewHolder.tvBody.setText(message.getBody());
        //set today or full time later
        String format = TimeUtils.getInstance().isToday(message.getDate()) ? TimeUtils.TIME_FORMAT : TimeUtils.DATE_FORMAT;
        String dateFormat = TimeUtils.getInstance().getTimeFormat(format, message.getDate());
        viewHolder.tvTime.setText(dateFormat);

        Contact contact = message.getContact();
        //set Icon late
        if (contact.getPhotoUri() == null){

            viewHolder.ivAvatar.setImageResource(R.drawable.fake_face);
        }
        else{
            viewHolder.ivAvatar.setImageURI(Uri.parse(contact.getPhotoUri()));
        }

        viewHolder.tvName.setText(contact.getName());
        return view;
    }

    public class ViewHolder{
        TextView tvBody, tvTime, tvName;
        CircularImageView ivAvatar;
    }

    public void filter(String query){
        query = query.toLowerCase(Locale.getDefault()).trim();
        mList.clear();
        if(query.length() == 0){
            mList.addAll(tmpList);
        }
        else {
            for(Message message : tmpList){
                if(message.getPhone().toLowerCase(Locale.getDefault()).contains(query) ||
                        message.getBody().toLowerCase(Locale.getDefault()).contains(query) ||
                        String.valueOf(message.getUnreadNumber()).contains(query) ||
                        message.getContact().getName().toLowerCase(Locale.getDefault()).contains(query)){
                    mList.add(message);
                }
            }
        }
        notifyDataSetChanged();
    }

}
