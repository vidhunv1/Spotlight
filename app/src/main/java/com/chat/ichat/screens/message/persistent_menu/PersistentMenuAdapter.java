package com.chat.ichat.screens.message.persistent_menu;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.chat.ichat.R;
import com.chat.ichat.api.bot.PersistentMenu;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.AndroidUtils;

import java.util.List;

/**
 * Created by vidhun on 27/06/17.
 */
public class PersistentMenuAdapter extends BaseAdapter {
    private Context context;
    private PersistentMenuAdapter.ClickListener clickListener;
    private List<PersistentMenu> persistentMenus;
    public PersistentMenuAdapter(Context context, List<PersistentMenu> persistentMenuList, PersistentMenuAdapter.ClickListener clickListener) {
        Logger.d(this, "PMList: "+persistentMenuList.size());
        this.context = context;
        this.clickListener = clickListener;
        this.persistentMenus = persistentMenuList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return persistentMenus.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row;
            row = inflater.inflate(R.layout.item_persistent_menu, parent, false);
            Button pm = (Button) row.findViewById(R.id.btn_pm);

            PersistentMenu persistentMenu = persistentMenus.get(position);
            pm.setText(persistentMenu.getTitle());

            pm.setOnTouchListener((v, event) -> {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    pm.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_persistent_menu_selected));
                } else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    pm.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_persistent_menu));
                }
                return false;
            });
            pm.setOnClickListener(v -> {
                pm.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_persistent_menu_selected));
                if(clickListener!=null) {
                    if (persistentMenu.getType() == PersistentMenu.Type.postback) {
                        clickListener.onPostBackClicked(persistentMenu.getTitle(), persistentMenu.getPayload());
                    } else if (persistentMenu.getType() == PersistentMenu.Type.web_url) {
                        clickListener.onUrlClicked(persistentMenu.getUrl());
                    } else {
                        clickListener.onPostBackClicked(persistentMenu.getTitle(), persistentMenu.getPayload());
                    }
                }
            });

            return row;
        } else {
            return convertView;
        }
    }

    interface ClickListener {
        void onPostBackClicked(String title, String payload);
        void onUrlClicked(String url);
    }
}

