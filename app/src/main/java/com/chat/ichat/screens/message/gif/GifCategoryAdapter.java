package com.chat.ichat.screens.message.gif;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.chat.ichat.R;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.AndroidUtils;

/**
 * Created by vidhun on 28/05/17.
 */
class GifCategoryAdapter extends BaseAdapter {
    private Context context;
    private TenorTagsResponse tags;
    private ClickListener clickListener;
    GifCategoryAdapter(Context context, TenorTagsResponse tenorTagsResponse, ClickListener clickListener) {
        this.context = context;
        this.tags = tenorTagsResponse;
        this.clickListener = clickListener;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return tags.getTags().size();
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
        View v;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.layout_gif_category, null, false).findViewById(R.id.gif_category);
        }
        else {
            v = convertView;
        }

        ImageView imageView = (ImageView) v.findViewById(R.id.category_image);
        TextView textView = (TextView) v.findViewById(R.id.category_tag);
        textView.setText(tags.getTags().get(position).getName());

        if(tags.getTags().get(position).getImage()!=null && !tags.getTags().get(position).getImage().isEmpty()) {
            Glide.with(context).load(tags.getTags().get(position).getImage().replace("https://", "http://"))
                    .bitmapTransform(new CenterCrop(context))
                    .placeholder(R.color.messageBackground)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageView);

        } else {
            imageView.setBackgroundColor(0xff000000);
        }

        imageView.setOnClickListener(vi -> {
            if(clickListener!=null) {
                clickListener.onGifClicked(tags.getTags().get(position).getSearchTerm());
            }
        });
        return v;
    }

    interface ClickListener {
        void onGifClicked(String searchQuery);
    }
}

