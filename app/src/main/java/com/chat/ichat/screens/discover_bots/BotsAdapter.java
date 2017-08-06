package com.chat.ichat.screens.discover_bots;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.chat.ichat.R;
import com.chat.ichat.api.bot.DiscoverBotsResponse;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.CircleTransformation;
import com.chat.ichat.core.lib.ImageUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
/**
 * Created by vidhun on 13/05/17.
 */
public class BotsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<DiscoverBotsResponse.Bots> botses;
    private DiscoverBotsAdapter.ActionListener actionListener;
    private Context context;
    private final int VIEW_TYPE_VERTICAL = 1;
    private final int VIEW_TYPE_HORIZONTAL = 2;

    private boolean isHorizontal = true;
    public BotsAdapter(Context context, List<DiscoverBotsResponse.Bots> botses, boolean isHorizontal, DiscoverBotsAdapter.ActionListener actionListener) {
        this.context = context;
        this.botses = botses;
        this.isHorizontal = isHorizontal;
        this.actionListener = actionListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
//            case VIEW_TYPE_VERTICAL:
//                View vertical = inflater.inflate(R.layout.item_discover_vertical, parent, false);
//                viewHolder = new DiscoverBotsVerticalViewHolder(vertical);
//                break;
            case VIEW_TYPE_HORIZONTAL:
                View horizontal = inflater.inflate(R.layout.item_search_contact_suggested, parent, false);
                viewHolder = new DiscoverBotsHorizontalViewHolder(horizontal);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_VERTICAL:
                DiscoverBotsVerticalViewHolder verticalViewHolder = (DiscoverBotsVerticalViewHolder) holder;
                verticalViewHolder.renderItem(botses.get(position), position);
                break;
            case VIEW_TYPE_HORIZONTAL:
                DiscoverBotsHorizontalViewHolder ho = (DiscoverBotsHorizontalViewHolder) holder;
                ho.renderItem(botses.get(position), position);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(isHorizontal)
            return VIEW_TYPE_HORIZONTAL;
        return VIEW_TYPE_VERTICAL;
    }

    @Override
    public int getItemCount() {
        return botses.size() >= 10 ? 10 : botses.size();
    }

    public class DiscoverBotsHorizontalViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_chatItem_profileImage)
        ImageView imageView;

        @Bind(R.id.tv_chatItem_name)
        TextView name;

        private DiscoverBotsResponse.Bots bots;

        public DiscoverBotsHorizontalViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem(DiscoverBotsResponse.Bots bots, int position) {
            this.bots = bots;
//            if(position == 0) {
//                ViewGroup.MarginLayoutParams i = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
//                i.setMargins((int) AndroidUtils.px(12),(int) AndroidUtils.px(5),(int) AndroidUtils.px(12),0);
//                imageView.requestLayout();
//            }
            name.setText(bots.getBot().getName());
            name.setTag(bots.getBot().getUserId());
            if(bots.getBot().getProfileDP()!=null && !bots.getBot().getProfileDP().isEmpty()) {
                DrawableRequestBuilder dp = Glide.with(context)
                        .load(bots.getBot().getProfileDP().replace("https://", "http://"))
                        .crossFade()
                        .bitmapTransform(new CenterCrop(context), new CircleTransformation(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                dp.into(imageView);
            } else {
                Drawable textProfileDrawable = ImageUtils.getDefaultProfileImage(bots.getBot().getName(), bots.getBot().getUsername(), 18);
                imageView.setImageDrawable(textProfileDrawable);
            }
        }

        @OnClick(R.id.suggested_view)
        public void onViewClick() {
            Logger.d(this, "Clicked: "+name.getTag().toString());
            if(actionListener !=null) {
                actionListener.onContactItemClicked(bots.getBot().getUserId(), bots.getCoverPicure(), bots.getDescription(), bots.getCategory());
            }
        }
    }

    public class DiscoverBotsVerticalViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_chatItem_profileImage)
        ImageView imageView;

        @Bind(R.id.tv_name)
        TextView name;

        @Bind(R.id.tv_stars)
        TextView stars;

        @Bind(R.id.tv_category)
        TextView category;

        private DiscoverBotsResponse.Bots bots;

        public DiscoverBotsVerticalViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem(DiscoverBotsResponse.Bots bots, int position) {
            this.bots = bots;
//            if(position == 0) {
//                ViewGroup.MarginLayoutParams i = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
//                i.setMargins((int) AndroidUtils.px(12),(int) AndroidUtils.px(5),(int) AndroidUtils.px(12),0);
//                imageView.requestLayout();
//            }
            category.setText(bots.getDescription());
            stars.setText("100 people using this");
            name.setText(bots.getBot().getName());
            name.setTag(bots.getBot().getUserId());
            if(bots.getBot().getProfileDP()!=null && !bots.getBot().getProfileDP().isEmpty()) {
                DrawableRequestBuilder dp = Glide.with(context)
                        .load(bots.getBot().getProfileDP().replace("https://", "http://"))
                        .crossFade()
                        .bitmapTransform(new CenterCrop(context), new CircleTransformation(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                dp.into(imageView);
            } else {
                Drawable textProfileDrawable = ImageUtils.getDefaultProfileImage(bots.getBot().getName(), bots.getBot().getUsername(), 18);
                imageView.setImageDrawable(textProfileDrawable);
            }
        }

        @OnClick(R.id.suggested_view)
        public void onViewClick() {
            Logger.d(this, "Clicked: "+name.getTag().toString());
            if(actionListener !=null) {
                actionListener.onContactItemClicked(bots.getBot().getUserId(), bots.getCoverPicure(), bots.getDescription(), bots.getCategory());
            }
        }
    }
}