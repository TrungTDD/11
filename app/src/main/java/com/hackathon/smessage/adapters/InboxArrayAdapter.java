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
import android.widget.TextView;

import com.hackathon.smessage.R;
import com.hackathon.smessage.configs.Defines;
import com.hackathon.smessage.customViews.CircularImageView;
import com.hackathon.smessage.models.Contact;
import com.hackathon.smessage.models.Message;
import com.hackathon.smessage.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Locale;

import static android.view.View.GONE;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class InboxArrayAdapter extends ArrayAdapter<Message> {

    private Context mContext;
    private int mLayout;
    private ArrayList<Message> mList;
    private ArrayList<Message> tmpList;

    public InboxArrayAdapter(Context context, int resource, ArrayList<Message> list) {
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
            viewHolder.ivBackground = (CircularImageView)convertView.findViewById(R.id.ivBackground);
            viewHolder.ivAvatar = (CircularImageView)convertView.findViewById(R.id.ivAvatar);
            viewHolder.tvName = (TextView)convertView.findViewById(R.id.tvName);
            viewHolder.tvFailed = (TextView)convertView.findViewById(R.id.tvFailed);
            viewHolder.tvUnreadNumber = (TextView)convertView.findViewById(R.id.tvUnreadNumber);
            viewHolder.tvMessage = (TextView)convertView.findViewById(R.id.tvMessage);
            viewHolder.tvTime = (TextView)convertView.findViewById(R.id.tvTime);
            viewHolder.tvLine = (TextView)convertView.findViewById(R.id.tvLine);

            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Message message = mList.get(position);

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), Defines.AVARTA_COLOR[position % Defines.AVARTA_COLOR.length]);
        viewHolder.ivBackground.setImageBitmap(bitmap);

        Contact contact = message.getContact();
        //set Icon late
        if (contact.getPhotoUri() == null){
            viewHolder.ivAvatar.setImageResource(R.drawable.fake_face);
        }
        else{
            viewHolder.ivAvatar.setImageURI(Uri.parse(contact.getPhotoUri()));
        }

        //change name later
        viewHolder.tvName.setText(contact.getName());

        //set Send status if send to
        if(message.isReceive() || message.getSendStatus() == Message.SEND_TO_STATUS_SENT){
            viewHolder.tvFailed.setVisibility(GONE);
        }
        else{
            viewHolder.tvFailed.setVisibility(View.VISIBLE);
            int statusId = R.string.failed;
            int colorId = R.color.item_message_inbox_failed;
            if(message.getSendStatus() == Message.SEND_TO_STATUS_SENDING){
                statusId = R.string.sending;
                colorId = R.color.item_message_inbox_sending;
            }
            viewHolder.tvFailed.setText(statusId);
            viewHolder.tvFailed.setTextColor(ContextCompat.getColor(mContext, colorId));
        }

        //number unread
        int unreadNumber = message.getUnreadNumber();
        if(unreadNumber == 0 || !message.isReceive()){
            viewHolder.tvUnreadNumber.setVisibility(View.GONE);
        }
        else{
            viewHolder.tvUnreadNumber.setVisibility(View.VISIBLE);
            viewHolder.tvUnreadNumber.setText(String.valueOf(unreadNumber));
        }

        //body
        viewHolder.tvMessage.setText(message.getBody());

        //set today or full time later
        String format = TimeUtils.getInstance().isToday(message.getDate()) ? TimeUtils.TIME_FORMAT : TimeUtils.DATE_FORMAT;
        String dateFormat = TimeUtils.getInstance().getTimeFormat(format, message.getDate());
        viewHolder.tvTime.setText(dateFormat);


        //hide line if the last message
        if(position == mList.size() - 1){
            viewHolder.tvLine.setVisibility(View.INVISIBLE);
        }
        else{
            viewHolder.tvLine.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    private class ViewHolder{
        public CircularImageView ivBackground, ivAvatar;
        public TextView tvName, tvFailed, tvUnreadNumber, tvMessage, tvTime, tvLine;
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
