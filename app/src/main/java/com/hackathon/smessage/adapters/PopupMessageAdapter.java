package com.hackathon.smessage.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hackathon.smessage.R;
import com.hackathon.smessage.configs.Defines;
import com.hackathon.smessage.controllers.MessageOpearation;
import com.hackathon.smessage.customViews.CircularImageView;
import com.hackathon.smessage.models.Contact;
import com.hackathon.smessage.models.Message;
import com.hackathon.smessage.utils.TimeUtils;

import java.util.ArrayList;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class PopupMessageAdapter extends PagerAdapter{

    private Context mContext;
    private int mLayout; //layout
    private ArrayList<Message> mList;

    private ArrayList<String> mAvatarMap;

    public PopupMessageAdapter(Context context, int resource, ArrayList<Message> list) {
        mContext = context;
        mLayout = resource;
        mList = list;
        mAvatarMap = new ArrayList();
    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View layout = inflater.inflate(mLayout, container, false);

        CircularImageView ivBackground = (CircularImageView)layout.findViewById(R.id.ivBackground);
        CircularImageView ivAvatar = (CircularImageView)layout.findViewById(R.id.ivAvatar);
        TextView tvName = (TextView)layout.findViewById(R.id.tvName);
        TextView tvMessage = (TextView)layout.findViewById(R.id.tvMessage);
        TextView tvTime = (TextView)layout.findViewById(R.id.tvTime);

        Message message = mList.get(position);

        if(!message.isRead()){
            Message savedSms = MessageOpearation.getInstance().getMessage(message.getId());
            message.setSendStatus(savedSms.getSendStatus());
            message.setIsRead(true);
            //encrypt to save
            message.encrypt();
            MessageOpearation.getInstance().update(message);
            //decrypt to show
            message.decrypt();
        }

        //get avatar index
        int avatarIndex = mAvatarMap.indexOf(message.getPhone());
        if(avatarIndex == -1){
            mAvatarMap.add(message.getPhone());
            avatarIndex = mAvatarMap.size() - 1;
        }
        avatarIndex = avatarIndex % Defines.AVARTA_COLOR.length;

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), Defines.AVARTA_COLOR[avatarIndex]);
        ivBackground.setImageBitmap(bitmap);

        Contact contact = message.getContact();
        //set Icon late
        if (contact.getPhotoUri() == null){
            ivAvatar.setImageResource(R.drawable.fake_face);
        }
        else{
            ivAvatar.setImageURI(Uri.parse(contact.getPhotoUri()));
        }

        //change name later
        tvName.setText(contact.getName());

        //body
        tvMessage.setText(message.getBody());

        //set today or full time later
        String format = TimeUtils.getInstance().isToday(message.getDate()) ? TimeUtils.TIME_FORMAT : TimeUtils.DATE_FORMAT;
        String dateFormat = TimeUtils.getInstance().getTimeFormat(format, message.getDate());
        tvTime.setText(dateFormat);
        container.addView(layout);
        return layout;
    }
}
