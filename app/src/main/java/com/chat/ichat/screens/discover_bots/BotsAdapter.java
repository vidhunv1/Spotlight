package com.chat.ichat.screens.discover_bots;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.chat.ichat.R;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.core.lib.CircleTransformation;
import com.chat.ichat.core.lib.ImageUtils;
import com.chat.ichat.models.ContactResult;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vidhun on 13/05/17.
 */
public class BotsAdapter extends RecyclerView.Adapter<BotsAdapter.DiscoverBotsViewHolder> {
    private List<ContactResult> contactsModels;
    private DiscoverBotsAdapter.ContactClickListener contactClickListener;
    private Context context;
    public BotsAdapter(Context context, List<ContactResult> contactsModels, DiscoverBotsAdapter.ContactClickListener contactClickListener) {
        this.context = context;
        this.contactsModels = contactsModels;
        this.contactClickListener = contactClickListener;
    }

    @Override
    public DiscoverBotsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_search_contact_suggested, parent, false);
        return new DiscoverBotsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DiscoverBotsViewHolder holder, int position) {
        holder.renderItem(contactsModels.get(position), position);
    }

    @Override
    public int getItemCount() {
        return contactsModels.size();
    }

    public class DiscoverBotsViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_chatItem_profileImage)
        ImageView imageView;

        @Bind(R.id.suggested_view)
        LinearLayout discoverBotsView;

        @Bind(R.id.tv_chatItem_name)
        TextView name;

        public DiscoverBotsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem(ContactResult contactsModel, int position) {
            Logger.d(this, "ContactsModel: "+contactsModel);
            if(position == 0) {
                ViewGroup.MarginLayoutParams i = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
                i.setMargins((int) AndroidUtils.px(17),(int) AndroidUtils.px(5),(int) AndroidUtils.px(12),0);
                imageView.requestLayout();
            }
            name.setText(contactsModel.getContactName());
            name.setTag(contactsModel.getUserId());
            if(contactsModel.getProfileDP()!=null && !contactsModel.getProfileDP().isEmpty()) {
                DrawableRequestBuilder dp = Glide.with(context)
                        .load(contactsModel.getProfileDP().replace("https://", "http://"))
                        .crossFade()
                        .bitmapTransform(new CenterCrop(context), new CircleTransformation(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                dp.into(imageView);
            } else {
                Drawable textProfileDrawable = ImageUtils.getDefaultProfileImage(contactsModel.getContactName(), contactsModel.getUsername(), 18);
                imageView.setImageDrawable(textProfileDrawable);
            }
        }

        @OnClick(R.id.suggested_view)
        public void onViewClick() {
            Logger.d(this, "Clicked: "+name.getTag().toString());
            if(contactClickListener!=null) {
                contactClickListener.onContactItemClicked(name.getTag().toString());
            }
        }
    }
}
