package com.stairway.spotlight.screens.contacts;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.stairway.spotlight.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by vidhun on 13/12/16.
 */

public class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_ADDED=0, VIEW_NOT_ADDED=1;
    private ContactsAdapter.ContactClickListener contactClickListener;
    private ContactsAdapter.ContactAddClickListener contactAddClickListener;
    private List<ContactItemModel> addedUsers;
    private List<ContactItemModel> notAddedUsers;

    public ContactsAdapter(ContactsAdapter.ContactClickListener contactClickListener, ContactsAdapter.ContactAddClickListener contactAddClickListener) {
        this.contactClickListener = contactClickListener;
        this.contactAddClickListener = contactAddClickListener;
        this.addedUsers = new ArrayList<>();
        this.notAddedUsers = new ArrayList<>();
    }

    public void setContacts(List<ContactItemModel> contacts) {
        if(contacts.size()>0) {
            for(int i=0;i<contacts.size();i++) {
                if(contacts.get(i).isAdded())
                    addedUsers.add(contacts.get(i));
                else
                    notAddedUsers.add(contacts.get(i));
            }
            Collections.sort(addedUsers, (o1, o2) -> o1.getContactName().compareTo(o2.getContactName()));
            Collections.sort(notAddedUsers, (o1, o2) -> o1.getContactName().compareTo(o2.getContactName()));
            this.notifyItemRangeInserted(0, contacts.size() - 1);
        }
    }

    public void onContactAdded(String userName) {
        ContactItemModel addContact = null;
        for(int i=0; i<notAddedUsers.size(); i++) {
            if(notAddedUsers.get(i).getUserName().equals(userName)) {
                addContact = notAddedUsers.get(i);
                notAddedUsers.remove(i);
                this.notifyItemRemoved(i + addedUsers.size());
                break;
            }
        }
        if(addedUsers.size()==0) {
            addedUsers.add(0, addContact);
            this.notifyItemInserted(0);
        } else {
            for(int i=0;i<addedUsers.size();i++) {
                if (addContact.getContactName().compareTo(addedUsers.get(i).getContactName()) <= 0) {
                    addedUsers.add(i, addContact);
                    this.notifyItemInserted(i);
                    break;
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position > (addedUsers.size()-1))
            return VIEW_NOT_ADDED;
        else
            return VIEW_ADDED;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_NOT_ADDED:
                View contactAddView = inflater.inflate(R.layout.item_contact_add, parent, false);
                viewHolder = new ContactAddViewHolder(contactAddView);
                break;
            case VIEW_ADDED:
                View contactView = inflater.inflate(R.layout.item_contact, parent, false);
                viewHolder = new ContactViewHolder(contactView);
                break;
            default:
                return null;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_ADDED:
                ContactViewHolder contactViewHolder = (ContactViewHolder)holder;
                contactViewHolder.renderItem(addedUsers.get(position));
                break;
            case VIEW_NOT_ADDED:
                ContactAddViewHolder contactAddViewHolder = (ContactAddViewHolder)holder;
                contactAddViewHolder.renderItem(notAddedUsers.get(position - addedUsers.size()));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return addedUsers.size() + notAddedUsers.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_contactItem_content)
        LinearLayout contactListContent;

        @Bind(R.id.tv_chatItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_chatItem_message)
        TextView status;

        public ContactViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            contactListContent.setOnClickListener(view -> {
                if(contactClickListener != null)
                    contactClickListener.onContactItemClicked(contactName.getTag().toString());
            });
        }

        public void renderItem(ContactItemModel contactItem) {
            contactName.setText(contactItem.getContactName());
            status.setText("@"+contactItem.getUserId());

            contactName.setTag(contactItem.getUserName());
        }
    }

    public interface ContactClickListener {
        void onContactItemClicked(String userName);
    }

    public class ContactAddViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.iv_contactItem_profileImage)
        ImageView profileImage;

        @Bind(R.id.tv_contactItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_contactItem_status)
        TextView status;

        @Bind(R.id.btn_add)
        Button add;

        public ContactAddViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.btn_add)
        public void onAddClick() {
            if(contactAddClickListener != null)
                contactAddClickListener.onContactAddClicked(contactName.getTag().toString());
        }

        public void renderItem(ContactItemModel contactItem) {
            contactName.setText(contactItem.getContactName());
            status.setText("@"+contactItem.getUserId());
            profileImage.setImageResource(R.drawable.default_profile_image);

            contactName.setTag(contactItem.getUserName());
        }
    }

    public interface ContactAddClickListener {
        void onContactAddClicked(String userName);
    }
}
