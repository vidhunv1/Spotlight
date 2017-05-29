package com.chat.ichat.screens.message.gif;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.chat.ichat.R;
import com.chat.ichat.core.lib.AndroidUtils;

/**
 * Created by vidhun on 22/05/17.
 */
public class TenorGifAdapter extends BaseAdapter {
    private Context context;
    private TenorGifResponse gifs;
    private ClickListener clickListener;
    public TenorGifAdapter(Context context, TenorGifResponse tenorGifResponse, ClickListener clickListener) {
        this.context = context;
        this.gifs = tenorGifResponse;
        this.clickListener = clickListener;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return gifs.getResults().size();
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
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)AndroidUtils.px(100)));
            imageView.setPadding((int)AndroidUtils.px(2), (int)AndroidUtils.px(4), (int)AndroidUtils.px(2), (int)AndroidUtils.px(0));
        }
        else {
            imageView = (ImageView) convertView;
        }

        if(gifs.getResults().get(position).getNanoGif()!=null && !gifs.getResults().get(position).getNanoGif().getPreview().isEmpty()) {
            Glide.with(context).load(gifs.getResults().get(position).getNanoGif().getPreview().replace("https://", "http://"))
                    .bitmapTransform(new CenterCrop(context))
                    .placeholder(R.color.messageBackground)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageView);
        } else {
            imageView.setBackgroundColor(0xff000000);
        }

        imageView.setOnClickListener(v -> {
            if(clickListener!=null) {
                clickListener.onGifClicked(position);
            }
        });
        return imageView;
    }

    interface ClickListener {
        void onGifClicked(int position);
    }
}
