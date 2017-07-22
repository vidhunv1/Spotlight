//package com.chat.ichat.screens.shared_media;
//
//import android.content.Context;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.GridView;
//import android.widget.ImageView;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.resource.bitmap.CenterCrop;
//import com.chat.ichat.core.lib.AndroidUtils;
//
//import java.util.List;
//
///**
// * Created by vidhun on 19/05/17.
// */
//
//public class MediaAdapter extends BaseAdapter {
//    private Context context;
//    private List<String> imageUrls;
//    public MediaAdapter(Context context, List<String> imageUrls) {
//        this.context = context;
//        this.imageUrls = imageUrls;
//        notifyDataSetChanged();
//    }
//
//    @Override
//    public int getCount() {
//        return imageUrls.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return null;
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ImageView imageView;
//        if (convertView == null) {
//            imageView = new ImageView(context);
//            imageView.setLayoutParams(new GridView.LayoutParams((int)AndroidUtils.px(88), (int)AndroidUtils.px(88)));
//            imageView.setPadding((int)AndroidUtils.px(4), (int)AndroidUtils.px(0), (int)AndroidUtils.px(0), (int)AndroidUtils.px(4));
//        }
//        else {
//            imageView = (ImageView) convertView;
//        }
//
//        if(imageUrls.get(position)!=null && !imageUrls.get(position).isEmpty()) {
//            Glide.with(context).load(imageUrls.get(position).replace("https://", "http://"))
//                    .bitmapTransform(new CenterCrop(context))
//                    .into(imageView);
//        } else {
//            imageView.setBackgroundColor(0xff000000);
//        }
//
//        return imageView;
//    }
//}
