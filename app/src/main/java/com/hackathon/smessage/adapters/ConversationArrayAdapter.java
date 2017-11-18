package com.hackathon.smessage.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hackathon.smessage.R;
import com.hackathon.smessage.controllers.MessageOpearation;
import com.hackathon.smessage.models.Message;
import com.hackathon.smessage.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class ConversationArrayAdapter extends ArrayAdapter<Message> {

    private Context mContext;
    private int mLayout;
    private ArrayList<Message> mList;
    private ArrayList<Message> tmpList;

    public ConversationArrayAdapter(Context context, int resource, ArrayList<Message> list) {
        super(context, resource, list);
        mContext = context;
        mLayout = resource;
        mList = list;

        tmpList = new ArrayList<>();
        tmpList.addAll(list);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mLayout, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.layoutMe = (LinearLayout)convertView.findViewById(R.id.layoutMe);
            viewHolder.layoutYou = (LinearLayout)convertView.findViewById(R.id.layoutYou);
            viewHolder.tvMessageMe = (TextView)convertView.findViewById(R.id.tvMessageMe);
            viewHolder.tvMessageYou = (TextView)convertView.findViewById(R.id.tvMessageYou);
            viewHolder.tvTimeMe = (TextView)convertView.findViewById(R.id.tvTimeMe);
            viewHolder.tvTimeYou = (TextView)convertView.findViewById(R.id.tvTimeYou);
            viewHolder.tvFailedMe = (TextView)convertView.findViewById(R.id.tvFailedMe);
            viewHolder.btnRetryMe = (Button) convertView.findViewById(R.id.btnRetryMe);

            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Message message = mList.get(position);
        if(!message.isRead()){
            message.setIsRead(true);
            //encrypt to save
            message.encrypt();
            MessageOpearation.getInstance().update(message);
            //decrypt to show
            message.decrypt();
        }
        if(message.isReceive()){
            viewHolder.layoutYou.setVisibility(View.VISIBLE);
            viewHolder.layoutMe.setVisibility(View.GONE);

            viewHolder.tvMessageYou.setText(message.getBody());
            String time = message.getDate();
            if(TimeUtils.getInstance().isToday(time)){
                time = TimeUtils.getInstance().getTimeFormat(TimeUtils.TIME_FORMAT, time);
            }
            viewHolder.tvTimeYou.setText(time);
        }
        else{
            viewHolder.layoutYou.setVisibility(View.GONE);
            viewHolder.layoutMe.setVisibility(View.VISIBLE);

            viewHolder.tvMessageMe.setText(message.getBody());
            String time = message.getDate();
            if(TimeUtils.getInstance().isToday(time)){
                time = TimeUtils.getInstance().getTimeFormat(TimeUtils.TIME_FORMAT, time);
            }
            viewHolder.tvTimeMe.setText(time);

            //failed
            if(message.getSendStatus() == Message.SEND_TO_STATUS_SENT){
                viewHolder.tvFailedMe.setVisibility(View.GONE);
                viewHolder.btnRetryMe.setVisibility(View.GONE);
            }
            else if(message.getSendStatus() == Message.SEND_TO_STATUS_SENDING){
                viewHolder.tvFailedMe.setVisibility(View.VISIBLE);
                viewHolder.btnRetryMe.setVisibility(View.GONE);
                viewHolder.tvFailedMe.setText(R.string.sending);
                viewHolder.tvFailedMe.setTextColor(ContextCompat.getColor(mContext, R.color.item_message_inbox_sending));
            }
            else{
                viewHolder.tvFailedMe.setVisibility(View.VISIBLE);
                viewHolder.btnRetryMe.setVisibility(View.VISIBLE);
                viewHolder.tvFailedMe.setText(R.string.failed);
                viewHolder.tvFailedMe.setTextColor(ContextCompat.getColor(mContext, R.color.item_message_inbox_failed));
            }
        }
        return convertView;
    }

    private class ViewHolder{
        public LinearLayout layoutMe, layoutYou;
        public TextView tvMessageMe, tvMessageYou;
        public TextView tvTimeMe, tvTimeYou;
        public TextView tvFailedMe;
        public Button btnRetryMe;
    }

    public void filter(String query){
        query = query.toLowerCase(Locale.getDefault());
        mList.clear();
        if(query.length() == 0){
            mList.addAll(tmpList);
        }
        else {
            for(Message message: tmpList){
                if(message.getPhone().toLowerCase(Locale.getDefault()).contains(query) ||
                        message.getBody().toLowerCase(Locale.getDefault()).contains(query) ||
                        String.valueOf(message.getUnreadNumber()).toLowerCase(Locale.getDefault()).contains(query)){
                    mList.add(message);
                }
            }
        }
        notifyDataSetChanged();
    }
}
