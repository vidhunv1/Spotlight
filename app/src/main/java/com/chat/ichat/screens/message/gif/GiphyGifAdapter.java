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
class GiphyGifAdapter extends BaseAdapter {
    private Context context;
    private GiphyGifResponse gifs;
    private ClickListener clickListener;
    public GiphyGifAdapter(Context context, GiphyGifResponse giphyGifResponse, ClickListener clickListener) {
        this.context = context;
        this.gifs = giphyGifResponse;
        this.clickListener = clickListener;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return gifs.getData().size();
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

        if(gifs.getData().get(position).getLowGifUrl()!=null && !gifs.getData().get(position).getLowGifUrl().isEmpty()) {
            Glide.with(context).load(gifs.getData().get(position).getLowGifUrl().replace("https://", "http://"))
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
                clickListener.onGifClicked(gifs.getData().get(position).getId());
            }
        });
        return imageView;
    }

    interface ClickListener {
        void onGifClicked(String id);
    }
}
