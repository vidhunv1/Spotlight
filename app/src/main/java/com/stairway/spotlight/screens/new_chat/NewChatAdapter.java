package com.stairway.spotlight.screens.new_chat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stairway.data.config.Logger;
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
    private List<NewChatItemModel> itemList;
    private final int CONTACT  = 1;
    private final int CATEGORY = 2;


    private List<Integer> filteredList;
    private String filterQuery;

    public NewChatAdapter(ContactClickListener contactClickListener, List<NewChatItemModel> contacts) {
        this.contactClickListener = contactClickListener;
        this.itemList = new ArrayList<>();
        filteredList = new ArrayList<>();
        filterQuery = "";
        this.itemList.addAll(contacts);
    }

    public void setContacts(List<NewChatItemModel> contacts) {
        this.itemList.clear();
        this.itemList.addAll(contacts);
        this.notifyItemRangeInserted(1, itemList.size());
    }

    public void addContact(NewChatItemModel contact) {
        itemList.add(contact);
        this.notifyItemInserted(itemList.size());
    }

    public void addContacts(List<NewChatItemModel> contacts) {
        int position = itemList.size();
        itemList.addAll(contacts);
        this.notifyItemRangeChanged(position, itemList.size()+1);
    }

    public void filterList(String query) {
        filterQuery = query;
        if(query.isEmpty()) {
            filteredList.clear();
            notifyDataSetChanged();
            return;
        }
        for (NewChatItemModel newChatItemModel : itemList)
            if(newChatItemModel.getContactName().toLowerCase().matches(query+".*") || newChatItemModel.getContactName().toLowerCase().matches(".* "+query+".*")) {
                filteredList.add(itemList.indexOf(newChatItemModel));
                Logger.d(this, "Filtering: "+newChatItemModel.toString()+" at "+itemList.indexOf(newChatItemModel));
            }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0  && filterQuery.isEmpty())
            return CATEGORY;
        return CONTACT;
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
            case CATEGORY:
                View categoryView = inflater.inflate(R.layout.item_new_chat_category, parent, false);
                viewHolder = new CategoryViewHolder(categoryView);
                break;
            default:
                return null;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int vPos) {
        int position = vPos;
        if(!filterQuery.isEmpty())
            position = filteredList.get(vPos);
        else
            position = position - 1;

        switch (holder.getItemViewType()) {
            case CONTACT:
                ContactsViewHolder cVH = (ContactsViewHolder) holder;
                cVH.renderItem(itemList.get(position));
                break;
            case CATEGORY:
                CategoryViewHolder catVH = (CategoryViewHolder) holder;
                catVH.renderItem();
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        if(!filterQuery.isEmpty())
            return filteredList.size();
        return itemList.size() + 1;
    }

    class ContactsViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_chatItem_content)
        LinearLayout contactListContent;

        @Bind(R.id.iv_chatItem_profileImage)
        ImageView profileImage;

        @Bind(R.id.tv_chatItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_chatItem_message)
        TextView status;

        ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            contactListContent.setOnClickListener(view -> {
                if(contactClickListener != null)
                    contactClickListener.onContactItemClicked(contactName.getTag().toString());
            });
        }

        void renderItem(NewChatItemModel contactItem) {
            contactName.setText(contactItem.getContactName());
            status.setText("ID: "+contactItem.getUserId());

            contactName.setTag(contactItem.getUserName());
        }
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_new_chat_category)
        TextView categoryName;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem() {
            categoryName.setText("Contacts");
        }
    }

    interface ContactClickListener {
        void onContactItemClicked(String userId);
    }
}
