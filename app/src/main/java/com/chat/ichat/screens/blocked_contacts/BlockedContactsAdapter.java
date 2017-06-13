package com.chat.ichat.screens.blocked_contacts;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
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
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.core.lib.CircleTransformation;
import com.chat.ichat.core.lib.ImageUtils;
import com.chat.ichat.models.ContactResult;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by vidhun on 12/06/17.
 */

public class BlockedContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ClickListener clickListener;
    private List<ContactResult> contacts;
    public BlockedContactsAdapter(Context context, List<ContactResult> contactResults, ClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.contacts = contactResults;
        this.notifyDataSetChanged();
    }

    public void setBlockedList(List<ContactResult> contactResults) {
        this.contacts = contactResults;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if(position<contacts.size())
            return 0;
        return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if(viewType == 0) {
            View blockedUsers = inflater.inflate(R.layout.item_chat, parent, false);
            viewHolder = new BlockedContactsViewHolder(blockedUsers);
        } else {
            TextView textView1 = new TextView(context);
            textView1.setText("Tap and hold to unblock");
            textView1.setTextColor(ContextCompat.getColor(context, R.color.appElement));
            textView1.setTextSize(16);
            textView1.setGravity(Gravity.CENTER);
            textView1.setHeight((int) AndroidUtils.px(48));

            viewHolder = new BottomViewHolder(textView1);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position < contacts.size())
            ((BlockedContactsViewHolder) holder).renderItem(contacts.get(position));
    }

    @Override
    public int getItemCount() {
        return contacts.size()+1;
    }

    class BlockedContactsViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_item_chat)
        LinearLayout chatListContent;

        @Bind(R.id.iv_chatItem_profileImage)
        ImageView profileImage;

        @Bind(R.id.tv_chatItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_chatItem_message)
        TextView lastMessage;

        @Bind(R.id.tv_chatItem_time)
        TextView time;

        @Bind(R.id.view_contactItem_divider)
        View dividerLine;

        @Bind(R.id.tv_chatlist_notification)
        TextView notification;

        @Bind(R.id.iv_delivery_status)
        ImageView deliveryStatus;

        BlockedContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
        void renderItem(ContactResult contactResult) {
            time.setVisibility(View.INVISIBLE);
            notification.setVisibility(View.INVISIBLE);
            deliveryStatus.setVisibility(View.INVISIBLE);

            if(contactResult.getProfileDP()!=null && !contactResult.getProfileDP().isEmpty()) {
                Glide.with(context)
                        .load(contactResult.getProfileDP().replace("https://", "http://"))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .crossFade()
                        .placeholder(ImageUtils.getDefaultProfileImage(contactResult.getContactName(), contactResult.getUserId(), 18))
                        .bitmapTransform(new CenterCrop(context), new CircleTransformation(context))
                        .into(profileImage);
            } else {
                profileImage.setImageDrawable(ImageUtils.getDefaultProfileImage(contactResult.getContactName(), contactResult.getUserId(), 18));
            }

            lastMessage.setText("ID: "+contactResult.getUserId());
            contactName.setText(contactResult.getContactName());
            contactName.setTag(contactResult.getUsername());

            chatListContent.setOnClickListener(view -> {
                if(clickListener != null)
                    clickListener.onContactClicked(contactName.getTag().toString());
            });
        }
    }

    class BottomViewHolder extends RecyclerView.ViewHolder {
        public BottomViewHolder(View itemView) {
            super(itemView);
        }
    }

    interface ClickListener {
        void onContactClicked(String userName);
    }
}

