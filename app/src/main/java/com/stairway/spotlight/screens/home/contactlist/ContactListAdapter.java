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
import com.stairway.spotlight.screens.register.signup.SignUpContract;

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
    private final int SEARCH=0, CONTACT=1;

    public ContactListAdapter(Context context, ContactClickListener contactClickListener, List<ContactListItemModel> contacts) {
        this.contactClickListener = contactClickListener;
        this.context = context;
        this.itemList = new ArrayList<>();
        this.itemList.add("Search");
        this.itemList.addAll(contacts);
    }

    public void setContacts(List<ContactListItemModel> contacts) {
        this.itemList.clear();
        this.itemList.add("Search");
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
        } else if(itemList.get(position) instanceof ContactListItemModel)
            return CONTACT;
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

            contactName.setTag(contactItem.getChatId());
        }
    }

    public interface ContactClickListener {
        void onContactItemClicked(String userId);
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
