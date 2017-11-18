package com.hackathon.smessage.fragments;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.hackathon.smessage.R;
import com.hackathon.smessage.adapters.BlockedArrayAdapter;
import com.hackathon.smessage.controllers.BlockedOperation;
import com.hackathon.smessage.controllers.ContactOpearation;
import com.hackathon.smessage.models.Blocked;
import com.hackathon.smessage.models.Contact;

import java.util.ArrayList;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class BlockedFragment extends Fragment {

    private ArrayList<Blocked> mList;
    private ArrayList<String> mDisplayList;
    private ListView lvBlocked;
    private BlockedArrayAdapter mAdapter;

    private ActionMode mActionMode;
    private MenuItem mItemCheckedAll;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_blocked, container, false);

        init();
        getWidgets(rootView);
        setWidgets();
        addWidgetsListener();

        return rootView;
    }

    public void setData(ArrayList<Blocked> list){
        if(mList == null){
            mList = new ArrayList<>();
        }
        if(mDisplayList == null){
            mDisplayList = new ArrayList<>();
        }

        mList.clear();
        mDisplayList.clear();

        for(Blocked blocked : list){
            mList.add(blocked);

            String display = blocked.getContent();
            if(blocked.isMessage() && blocked.isContact() || !blocked.isMessage()){
                Contact contact = ContactOpearation.getInstance().getContactWithPhoneNumber(blocked.getContent());
                if(contact != null) {
                    display = contact.getName();
                }
            }
            mDisplayList.add(display);

            if(mAdapter != null){
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void init(){
        if(mList == null){
            mList = new ArrayList<>();
        }
        if(mDisplayList == null){
            mDisplayList = new ArrayList<>();
        }
        mAdapter = new BlockedArrayAdapter(getActivity(), R.layout.item_blocked, mDisplayList);
    }

    private void getWidgets(View view){
        lvBlocked = (ListView)view.findViewById(R.id.lvBlocked);
    }

    private void setWidgets(){
        lvBlocked.setAdapter(mAdapter);
        lvBlocked.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
    }

    private void addWidgetsListener(){
        lvBlocked.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                mAdapter.setItemChecked(i, b);
                int numSelected = mAdapter.getCheckedCount();
                boolean isCheckedAll = numSelected == mList.size();
                setViewCheckeddAll(isCheckedAll);
                actionMode.setTitle(numSelected + " " + getActivity().getString(R.string.selected));
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                mActionMode = actionMode;
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.fragment_blocked_action, menu);
                mItemCheckedAll = (MenuItem)menu.findItem(R.id.menu_checked_all);
                mAdapter.clear();
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_checked_all:
                        int numSelected = mAdapter.getCheckedCount();
                        boolean isChecked = !(numSelected == mList.size());
                        setViewCheckeddAll(isChecked);
                        menuItem.setChecked(isChecked);
                         for(int i = 0; i < mDisplayList.size(); i++){
                            lvBlocked.setItemChecked(i, isChecked);
                        }
                        mAdapter.setAllChecked(isChecked);
                        break;
                    case R.id.menu_delete:
                        //delete
                        showConfirmDelete();
                        break;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                mActionMode = null;
            }
        });
    }

    private void setViewCheckeddAll(boolean isAll){
        if(isAll){
            mItemCheckedAll.setIcon(android.R.drawable.checkbox_on_background);
        }
        else{
            mItemCheckedAll.setIcon(android.R.drawable.checkbox_off_background);
        }
    }

    private void showConfirmDelete(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_blocked_warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for(int index = mDisplayList.size() - 1; index >= 0; index--){
                            if(mAdapter.isItemChecked(index)){
                                BlockedOperation.getInstance().delete(mList.get(index));
                                mDisplayList.remove(index);
                            }
                        }
                        mAdapter.clear();
                        mAdapter.notifyDataSetChanged();
                        mActionMode.finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create()
                .show();
    }
}
