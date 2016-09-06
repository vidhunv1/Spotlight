package com.stairway.spotlight.screens.home.contactlist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stairway.spotlight.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 01/09/16.
 */
public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder>{
    private ContactClickListener contactClickListener;
    private Context context;
    private List<ContactListItemModel> contactList;

    public ContactListAdapter(Context context, List<ContactListItemModel> contactList, ContactClickListener contactClickListener) {
        this.contactClickListener = contactClickListener;
        this.context = context;
        this.contactList = contactList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_contact_list, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.renderItem(contactList.get(position));
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_contactItem_content)
        LinearLayout contactListContent;

        @Bind(R.id.iv_contactItem_profileImage)
        ImageView profileImage;

        @Bind(R.id.tv_contactItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_contactItem_status)
        TextView status;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            contactListContent.setOnClickListener(view -> {
                if(contactClickListener != null)
                    contactClickListener.onContactItemClicked(contactName.getTag().toString());
            });
        }

        public void renderItem(ContactListItemModel contactItem) {
            contactName.setText(contactItem.getContactName());

            contactName.setTag(contactItem.getChatId());
        }
    }

    public interface ContactClickListener {
        void onContactItemClicked(String userId);
    }
}
