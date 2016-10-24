package com.stairway.spotlight.screens.home.contactlist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stairway.data.manager.Logger;
import com.stairway.spotlight.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 01/09/16.
 */
public class ContactListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ContactClickListener contactClickListener;
    private SearchClickListener searchClickListener;
    private Context context;
    private List<Object> itemList;
    private final int SEARCH=0, CONTACT=1, INVITE=2;

    public ContactListAdapter(Context context, ContactClickListener contactClickListener, List<ContactListItemModel> contacts) {
        this.contactClickListener = contactClickListener;
        this.context = context;
        this.itemList = new ArrayList<>();
//        this.itemList.add("Search");
        this.itemList.addAll(contacts);
    }

    public void setContacts(List<ContactListItemModel> contacts) {
        this.itemList.clear();
//        this.itemList.add("Search");
        this.itemList.addAll(contacts);
        this.notifyItemRangeInserted(0, itemList.size() - 1);
    }

    public void addContact(ContactListItemModel contact) {
        itemList.add(contact);
        this.notifyItemInserted(itemList.size()-1);
    }

    public void addContacts(List<ContactListItemModel> contacts) {
        int position = itemList.size()-1;
        itemList.addAll(contacts);
        this.notifyItemRangeChanged(position, itemList.size());
    }

    @Override
    public int getItemViewType(int position) {
        if(itemList.get(position) instanceof String) {
            return SEARCH;
        } else if(itemList.get(position) instanceof ContactListItemModel) {
            if(((ContactListItemModel) itemList.get(position)).getInviteFlag())
                return INVITE;
            else
                return CONTACT;
        }
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case SEARCH:
                View searchView = inflater.inflate(R.layout.item_contact_search, parent, false);
                viewHolder = new SearchViewHolder(searchView);
                break;
            case CONTACT:
                View contactView = inflater.inflate(R.layout.item_contact_list, parent, false);
                viewHolder = new ContactsViewHolder(contactView);
                break;
            case INVITE:
                View inviteView = inflater.inflate(R.layout.item_contact_list_invite, parent, false);
                viewHolder = new InviteViewHolder(inviteView);
                break;
            default:
                return null;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case SEARCH:
                SearchViewHolder sVH = (SearchViewHolder) holder;
                sVH.renderItem();
                break;
            case CONTACT:
                ContactsViewHolder cVH = (ContactsViewHolder) holder;
                cVH.renderItem((ContactListItemModel) itemList.get(position));
                break;
            case INVITE:
                InviteViewHolder iVH = (InviteViewHolder) holder;
                iVH.renderItem((ContactListItemModel) itemList.get(position));
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

        public void renderItem(ContactListItemModel contactItem) {
            contactName.setText(contactItem.getContactName());
            status.setText(contactItem.getMobileNumber());
            profileImage.setImageResource(R.drawable.default_profile_image);
            Logger.d("Invite flag"+contactItem.getInviteFlag());

            contactName.setTag(contactItem.getChatId());
        }
    }

    public interface ContactClickListener {
        void onContactItemClicked(String userId);
    }

    public class InviteViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_contactItem_content)
        LinearLayout contactListContent;

        @Bind(R.id.btn_contactItem_invite)
        Button inviteButton;

        @Bind(R.id.tv_contactItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_contactItem_number)
        TextView number;

        @Bind(R.id.iv_contactItem_profileImage)
        ImageView profileImage;

        public InviteViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
        public void renderItem(ContactListItemModel contactItem) {
            contactName.setText(contactItem.getContactName());
            number.setText(contactItem.getMobileNumber());
            profileImage.setImageResource(R.drawable.default_profile_image);
            Logger.d("Invite flag"+contactItem.getInviteFlag());

            contactName.setTag(contactItem.getChatId());
        }
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_contactItem_Search)
        LinearLayout contactSearch;

        @Bind(R.id.tv_contactItem_search)
        TextView search;

        public SearchViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            contactSearch.setOnClickListener(view -> {
                if(searchClickListener != null)
                    searchClickListener.onContactItemClicked();
            });
        }

        public void renderItem() {
        }
    }

    public interface SearchClickListener {
        void onContactItemClicked();
    }
}
