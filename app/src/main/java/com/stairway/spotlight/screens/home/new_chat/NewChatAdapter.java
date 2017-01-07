package com.stairway.spotlight.screens.home.new_chat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stairway.spotlight.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 01/09/16.
 */
public class NewChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ContactClickListener contactClickListener;
    private List<Object> itemList;
    private final int CONTACT=1;

    public NewChatAdapter(ContactClickListener contactClickListener, List<NewChatItemModel> contacts) {
        this.contactClickListener = contactClickListener;
        this.itemList = new ArrayList<>();
        this.itemList.addAll(contacts);
    }

    public void setContacts(List<NewChatItemModel> contacts) {
        this.itemList.clear();
        this.itemList.addAll(contacts);
        this.notifyItemRangeInserted(0, itemList.size() - 1);
    }

    public void addContact(NewChatItemModel contact) {
        itemList.add(contact);
        this.notifyItemInserted(itemList.size()-1);
    }

    public void addContacts(List<NewChatItemModel> contacts) {
        int position = itemList.size()-1;
        itemList.addAll(contacts);
        this.notifyItemRangeChanged(position, itemList.size());
    }

    @Override
    public int getItemViewType(int position) {
        if(itemList.get(position) instanceof NewChatItemModel)
            return CONTACT;
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case CONTACT:
                View contactView = inflater.inflate(R.layout.item_contact, parent, false);
                viewHolder = new ContactsViewHolder(contactView);
                break;
            default:
                return null;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case CONTACT:
                ContactsViewHolder cVH = (ContactsViewHolder) holder;
                cVH.renderItem((NewChatItemModel) itemList.get(position));
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_contactItem_content)
        LinearLayout contactListContent;

        @Bind(R.id.iv_contactItem_profileImage)
        ImageView profileImage;

        @Bind(R.id.tv_contactItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_contactItem_status)
        TextView status;

        public ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            contactListContent.setOnClickListener(view -> {
                if(contactClickListener != null)
                    contactClickListener.onContactItemClicked(contactName.getTag().toString());
            });
        }

        public void renderItem(NewChatItemModel contactItem) {
            contactName.setText(contactItem.getContactName());
            status.setText("@"+contactItem.getUserId());
            profileImage.setImageResource(R.drawable.default_profile_image);

            contactName.setTag(contactItem.getUserName());
        }
    }

    public interface ContactClickListener {
        void onContactItemClicked(String userId);
    }
}
