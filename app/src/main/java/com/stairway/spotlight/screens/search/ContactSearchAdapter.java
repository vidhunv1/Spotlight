package com.stairway.spotlight.screens.search;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stairway.data.config.Logger;
import com.stairway.spotlight.R;

import java.util.ArrayList;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 17/12/16.
 */

public class ContactSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ContactsModel> itemList;
    private ContactClickListener contactClickListener;
    private String searchQuery;

    public ContactSearchAdapter(ContactClickListener contactClickListener) {
        this.contactClickListener = contactClickListener;
        this.itemList = new ArrayList<>();
        this.searchQuery = "";
    }

    public void setContacts(String searchQuery, List<ContactsModel> itemList) {
        this.searchQuery = searchQuery;
        this.itemList.clear();
        this.itemList.addAll(itemList);
        notifyDataSetChanged();
        Logger.d("ContactSearchAdapter");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View contactAddView = inflater.inflate(R.layout.item_contact_list, parent, false);
        viewHolder = new ContactViewHolder(contactAddView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ContactViewHolder contactViewHolder = (ContactViewHolder) holder;
        contactViewHolder.renderItem(itemList.get(position), searchQuery);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_contactItem_content)
        LinearLayout contactListContent;

        @Bind(R.id.tv_contactItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_contactItem_status)
        TextView status;

        public ContactViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            contactListContent.setOnClickListener(view -> {
                if(contactClickListener != null)
                    contactClickListener.onContactItemClicked(contactName.getTag().toString());
            });
        }

        public void renderItem(ContactsModel contactItem, String searchQuery) {
            int queryPosStart = contactItem.getContactName().indexOf(searchQuery);
            int queryPosEnd = queryPosStart+searchQuery.length();
            contactName.setText(contactItem.getContactName());
            status.setText("@"+contactItem.getUserId());
            contactName.setTag(contactItem.getUserName());
        }
    }

    public interface ContactClickListener {
        void onContactItemClicked(String userId);
    }
}
