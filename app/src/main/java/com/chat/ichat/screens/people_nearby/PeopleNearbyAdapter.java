package com.chat.ichat.screens.people_nearby;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.chat.ichat.R;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.location.UserLocation;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.CircleTransformation;
import com.chat.ichat.core.lib.ImageUtils;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.models.UserSession;
import com.chat.ichat.screens.new_chat.AddContactUseCase;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 21/05/17.
 */
public class PeopleNearbyAdapter extends RecyclerView.Adapter<PeopleNearbyAdapter.ContactViewHolder> {
    private List<UserLocation> nearbyUsers;
    private Context context;
    private ContactClickListener contactClickListener;

    public PeopleNearbyAdapter(Context context, List<UserLocation> userLocations, ContactClickListener contactClickListener) {
        this.context = context;
        this.nearbyUsers = userLocations;
        this.contactClickListener = contactClickListener;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_chat, parent, false);
        return new ContactViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        holder.renderContactItem(nearbyUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return nearbyUsers.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_item_chat)
        LinearLayout contactListContent;

        @Bind(R.id.tv_chatItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_chatItem_message)
        TextView distance;

        @Bind(R.id.iv_chatItem_profileImage)
        ImageView profileImage;

        @Bind(R.id.view_contactItem_divider)
        View divider;

        @Bind(R.id.iv_delivery_status)
        ImageView deliveryStatus;

        @Bind(R.id.tv_chatItem_time)
        TextView time;

        ContactViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderContactItem(UserLocation userLocation) {
            deliveryStatus.setVisibility(View.INVISIBLE);
            time.setVisibility(View.INVISIBLE);

            profileImage.setImageDrawable(ImageUtils.getDefaultProfileImage(userLocation.getUser().getName(), userLocation.getUser().getUserId(), 18));
            int dst = (int) userLocation.getDistance();
            if(dst==0) {
                dst = 1;
            }
            distance.setText("within "+dst+"km");
            contactName.setText(userLocation.getUser().getName());
            if(userLocation.getUser().getProfileDP()!=null && !userLocation.getUser().getProfileDP().isEmpty()) {
                Glide.with(context)
                        .load(userLocation.getUser().getProfileDP().replace("https://", "http://"))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .crossFade()
                        .placeholder(ImageUtils.getDefaultProfileImage(userLocation.getUser().getName(), userLocation.getUser().getUserId(), 18))
                        .bitmapTransform(new CenterCrop(context), new CircleTransformation(context))
                        .into(profileImage);
            } else {
                profileImage.setImageDrawable(ImageUtils.getDefaultProfileImage(userLocation.getUser().getName(), userLocation.getUser().getUserId(), 18));
            }

            contactListContent.setOnClickListener(view -> {
                if(contactClickListener != null) {
                    contactClickListener.onContactItemClicked(userLocation.getUser().getUsername(), userLocation.getUser().getUserId());
                }
            });
        }
    }

    interface ContactClickListener {
        void onContactItemClicked(String username, String userId);
    }
}

