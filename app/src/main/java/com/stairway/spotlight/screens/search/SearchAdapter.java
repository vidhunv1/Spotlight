package com.stairway.spotlight.screens.search;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stairway.data.config.Logger;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.lib.MessageParser;
import com.stairway.spotlight.screens.message.view_models.TextMessage;

import java.text.ParseException;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 17/12/16.
 */

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private SearchModel searchList;
    private ContactClickListener contactClickListener;
    private MessageClickListener messageClickListener;
    private FindContactClickListener findContactClickListener;

    private final int VIEW_TYPE_MESSAGE = 0;
    private final int VIEW_TYPE_CONTACT = 1;
    private final int VIEW_TYPE_CATEGORY_CONTACTS = 2;
    private final int VIEW_TYPE_CATEGORY_MESSAGES = 3;
    private final int VIEW_TYPE_FIND = 4;

    public SearchAdapter(ContactClickListener contactClickListener, MessageClickListener messageClickListener, FindContactClickListener findContactClickListener) {
        this.contactClickListener = contactClickListener;
        this.messageClickListener = messageClickListener;
        this.findContactClickListener = findContactClickListener;
    }

    public void displaySearch(SearchModel searchModel) {
        searchList = searchModel;
        this.notifyDataSetChanged();
    }

    private int getCategoryContactPos() { // VIEW_TYPE_CATEGORY_CONTACTS
        if(searchList.getContactsModelList().size()>0)
            return 0;
        return -1;
    }
    private int getCategoryMessagePos() { // VIEW_TYPE_CATEGORY_MESSAGES
        if(searchList.getMessagesModelList().size()>0) {
            if (getCategoryContactPos() >= 0)
                return searchList.getContactsModelList().size() + 1;
            else
                return 0;
        }
        return -1;
    }
    private int getFindContactPos() { // VIEW_TYPE_FIND
        if(getCategoryMessagePos()>=0 && getCategoryContactPos()>=0)
            return searchList.getMessagesModelList().size() +  searchList.getContactsModelList().size() + 2;
        if(getCategoryContactPos()>=0)
            return searchList.getContactsModelList().size() + 1;
        if(getCategoryMessagePos()>=0)
            return searchList.getMessagesModelList().size() + 1;
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_MESSAGE:
                View messageView = inflater.inflate(R.layout.item_search_message, parent, false);
                viewHolder = new MessageViewHolder(messageView);
                break;
            case VIEW_TYPE_CONTACT:
                View contactView = inflater.inflate(R.layout.item_contact, parent, false);
                viewHolder = new ContactViewHolder(contactView);
                break;
            case VIEW_TYPE_CATEGORY_CONTACTS:
                View categoryView = inflater.inflate(R.layout.item_search_category, parent, false);
                viewHolder = new CategoryViewHolder(categoryView);
                break;
            case VIEW_TYPE_CATEGORY_MESSAGES:
                View categoryView1 = inflater.inflate(R.layout.item_search_category, parent, false);
                viewHolder = new CategoryViewHolder(categoryView1);
                break;
            case VIEW_TYPE_FIND:
                View contactView1 = inflater.inflate(R.layout.item_search_contact, parent, false);
                viewHolder = new ContactViewHolder(contactView1);
                break;
            default:
                return null;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_CONTACT:
                ContactViewHolder contactViewHolder = (ContactViewHolder) holder;
                contactViewHolder.renderContactItem(searchList.getContactsModelList().get(position-1), searchList.getSearchTerm());
                break;
            case VIEW_TYPE_MESSAGE:
                MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
                if(getCategoryMessagePos()>=0 && getCategoryContactPos()>=0)
                    messageViewHolder.renderItem(searchList.getMessagesModelList().get(position-searchList.getContactsModelList().size()-2), searchList.getSearchTerm());
                else
                    messageViewHolder.renderItem(searchList.getMessagesModelList().get(position-searchList.getContactsModelList().size()-1), searchList.getSearchTerm());
                break;
            case VIEW_TYPE_CATEGORY_CONTACTS:
                CategoryViewHolder categoryNameViewHolder1 = (CategoryViewHolder) holder;
                categoryNameViewHolder1.renderItem("Contacts");
                break;
            case VIEW_TYPE_CATEGORY_MESSAGES:
                CategoryViewHolder categoryNameViewHolder2 = (CategoryViewHolder) holder;
                categoryNameViewHolder2.renderItem("Messages");
                break;
            case VIEW_TYPE_FIND:
                Logger.d(this, "bind find "+searchList.getSearchTerm());
                ContactViewHolder contactViewHolder2 = (ContactViewHolder) holder;
                contactViewHolder2.renderFindItem(searchList.getSearchTerm());
        }
    }

    @Override
    public int getItemCount() {
        if(searchList == null)
            return  0;
        return getFindContactPos()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==getFindContactPos())
            return VIEW_TYPE_FIND;
        else if(position == getCategoryContactPos())
            return VIEW_TYPE_CATEGORY_CONTACTS;
        else if(position == getCategoryMessagePos())
            return VIEW_TYPE_CATEGORY_MESSAGES;
        else if((getCategoryContactPos()>=0 && (position<getCategoryMessagePos() && getCategoryMessagePos()>=0)) || getCategoryMessagePos()==-1)
            return VIEW_TYPE_CONTACT;
        else if(getCategoryMessagePos()>=0)
            return VIEW_TYPE_MESSAGE;
        return -1;
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_contactItem_content)
        LinearLayout contactListContent;

        @Bind(R.id.tv_contactItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_contactItem_status)
        TextView status;

        @Bind(R.id.iv_contactItem_profileImage)
        ImageView profileImage;

        @Bind(R.id.view_contactItem_divider)
        View divider;

        ContactViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderContactItem(ContactsModel contactItem, String searchQuery) {
            int queryPosStart = contactItem.getContactName().indexOf(searchQuery);
            int queryPosEnd = queryPosStart+searchQuery.length();
            contactName.setText(contactItem.getContactName());
            status.setText("@"+contactItem.getUserId());
            contactName.setTag(contactItem.getUserName());
            divider.setVisibility(View.GONE);
            profileImage.setImageResource(R.drawable.default_profile_image);

            contactListContent.setOnClickListener(view -> {
                if(contactClickListener != null)
                    contactClickListener.onContactItemClicked(contactName.getTag().toString());
            });
        }

        void renderFindItem(String searchQuery) {
            contactName.setText("Search by ID");
            status.setText("@"+searchQuery);
            divider.setVisibility(View.GONE);

            contactListContent.setOnClickListener(v -> {
                findContactClickListener.onFindContactItemClicked(searchQuery);
            });
        }
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_searchMessage_content)
        LinearLayout messagesLayout;

        @Bind(R.id.tv_searchMessage_contactName)
        TextView name;

        @Bind(R.id.tv_searchMessage_time)
        TextView time;

        @Bind(R.id.tv_searchMessage_message)
        TextView message;

        public MessageViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            messagesLayout.setOnClickListener(v -> {
                if(messageClickListener != null)
                    messageClickListener.onMessageItemClicked(name.getTag().toString());
            });
        }

        void renderItem(MessagesModel messagesModel, String searchQuery) {
            name.setText(messagesModel.getUserId());
            time.setText(messagesModel.getTime());
            MessageParser messageParser = new MessageParser(messagesModel.getMessage());
            try {
                TextMessage textMessage = new TextMessage("");
                Object o = messageParser.parseMessage();
                if(messageParser.getMessageType() == MessageParser.MessageType.text)
                    textMessage = (TextMessage)o;
                    message.setText(textMessage.getText());
            } catch (ParseException e) {}
            name.setTag(messagesModel.getUserId());
        }
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_search_category)
        TextView categoryTextView;

        CategoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(String categoryName) {
            categoryTextView.setText(categoryName);
        }
    }

    interface ContactClickListener {
        void onContactItemClicked(String userId);
    }

    interface MessageClickListener {
        void onMessageItemClicked(String userId);
    }

    interface FindContactClickListener {
        void onFindContactItemClicked(String userName);
    }
}