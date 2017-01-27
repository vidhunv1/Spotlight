package com.stairway.spotlight.screens.search;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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
    private Context context;
    private ContactClickListener contactClickListener;
    private MessageClickListener messageClickListener;
    private FindContactClickListener findContactClickListener;

    private final int VIEW_TYPE_MESSAGE = 0;
    private final int VIEW_TYPE_CONTACT = 1;
    private final int VIEW_TYPE_CATEGORY_CONTACTS = 2;
    private final int VIEW_TYPE_CATEGORY_MESSAGES = 3;
    private final int VIEW_TYPE_FIND = 4;
    private final int VIEW_TYPE_NO_RESULT = 5;

    private String highlightColor = "#0084ff";

    public SearchAdapter(Context context, ContactClickListener contactClickListener, MessageClickListener messageClickListener, FindContactClickListener findContactClickListener) {
        this.context = context;
        this.contactClickListener = contactClickListener;
        this.messageClickListener = messageClickListener;
        this.findContactClickListener = findContactClickListener;
    }

    public void displaySearch(SearchModel searchModel) {
        searchList = searchModel;
        this.notifyDataSetChanged();
    }

    private int getCategoryMessagePos() { // VIEW_TYPE_CATEGORY_MESSAGES
        if(searchList.getMessagesModelList().size()>0)
            return searchList.getContactsModelList().size()+1;
        return -1;
    }
    private int getContactLastPos() { // VIEW_TYPE_FIND
        return searchList.getContactsModelList().size();
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
                View contactView = inflater.inflate(R.layout.item_chat, parent, false);
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
                viewHolder = new FindContactViewHolder(contactView1);
                break;
            case VIEW_TYPE_NO_RESULT:
                View noResults = inflater.inflate(R.layout.item_no_result, parent, false);
                viewHolder = new NoResultViewHolder(noResults);
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
                messageViewHolder.renderItem(searchList.getMessagesModelList().get(position-getCategoryMessagePos()-1), searchList.getSearchTerm());
                break;
            case VIEW_TYPE_CATEGORY_CONTACTS:
                CategoryViewHolder categoryNameViewHolder1 = (CategoryViewHolder) holder;
                categoryNameViewHolder1.renderItem("Contacts", searchList.getContactsModelList().size()>0);
                break;
            case VIEW_TYPE_CATEGORY_MESSAGES:
                CategoryViewHolder categoryNameViewHolder2 = (CategoryViewHolder) holder;
                categoryNameViewHolder2.renderItem("Messages", true);
                break;
            case VIEW_TYPE_FIND:
                FindContactViewHolder findContactViewHolder = (FindContactViewHolder) holder;
                findContactViewHolder.renderFindItem(searchList.getSearchTerm());
                break;
            case VIEW_TYPE_NO_RESULT:
                NoResultViewHolder noResultViewHolder = (NoResultViewHolder) holder;
                noResultViewHolder.renderItem(searchList.getSearchTerm());
        }
    }

    @Override
    public int getItemCount() {
        if(searchList == null || searchList.getSearchTerm().length()==0)
            return  0;
        if(searchList.getSearchTerm().length()>0 && searchList.getContactsModelList().size()==0 && searchList.getMessagesModelList().size()==0)
            return 1;
        if(searchList.getMessagesModelList().size()>0)
            return searchList.getContactsModelList().size() + searchList.getMessagesModelList().size() + 2;
        return searchList.getContactsModelList().size()+searchList.getMessagesModelList().size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if(searchList.getSearchTerm().length()>0 && searchList.getContactsModelList().size()==0 && searchList.getMessagesModelList().size()==0)
            return VIEW_TYPE_NO_RESULT;
        if(position==0)
            return VIEW_TYPE_CATEGORY_CONTACTS;
//        if(position==getContactLastPos())
//            return VIEW_TYPE_FIND;
        else if(position == getCategoryMessagePos())
            return VIEW_TYPE_CATEGORY_MESSAGES;
        else if(position<=getContactLastPos())
            return VIEW_TYPE_CONTACT;
        else if(position>getCategoryMessagePos() && getCategoryMessagePos()>0)
            return VIEW_TYPE_MESSAGE;
        return -1;
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_item_chat)
        LinearLayout contactListContent;

        @Bind(R.id.tv_chatItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_chatItem_message)
        TextView status;

        @Bind(R.id.iv_chatItem_profileImage)
        ImageView profileImage;

        @Bind(R.id.view_contactItem_divider)
        View divider;

        ContactViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressWarnings("deprecation")
        void renderContactItem(ContactsModel contactItem, String searchQuery) {
            String highlightColor = "#"+Integer.toHexString(ContextCompat.getColor( context, R.color.searchHighlight) & 0x00ffffff );

            String contactLower = contactItem.getContactName().toLowerCase();
            int startPos = contactLower.indexOf(searchQuery.toLowerCase());
            if(!searchQuery.isEmpty() && startPos>=0) {
                String textHTML = contactItem.getContactName().substring(0,startPos)
                        +"<font color=\""+highlightColor+"\">"+contactItem.getContactName().substring(startPos, startPos+searchQuery.length()) +"</font>"
                        +contactItem.getContactName().substring(startPos+searchQuery.length());

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                    contactName.setText(Html.fromHtml(textHTML, Html.FROM_HTML_MODE_LEGACY));
                else
                    contactName.setText(Html.fromHtml(textHTML));
            } else {
                contactName.setText(contactItem.getContactName());
            }

            status.setText("ID: "+contactItem.getUserId());
            contactName.setTag(contactItem.getUserName());
            divider.setVisibility(View.GONE);
            profileImage.setImageResource(R.drawable.default_profile_image);

            contactListContent.setOnClickListener(view -> {
                if(contactClickListener != null)
                    contactClickListener.onContactItemClicked(contactName.getTag().toString());
            });
        }
    }

    class FindContactViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_item_search_contact)
        LinearLayout searchContent;

        @Bind(R.id.tv_chatItem_contactName)
        TextView searchText;

        FindContactViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressWarnings("deprecation")
        void renderFindItem(String searchQuery) {
            String textHTML = "Search iChat ID: "+"<font color=\""+highlightColor+"\">"+searchQuery+"</font>";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                searchText.setText(Html.fromHtml(textHTML, Html.FROM_HTML_MODE_LEGACY));
            } else {
                searchText.setText(Html.fromHtml(textHTML));
            }
            searchContent.setOnClickListener(v -> findContactClickListener.onFindContactItemClicked(searchQuery));
        }
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_item_chat)
        LinearLayout messagesLayout;

        @Bind(R.id.tv_chatItem_contactName)
        TextView name;

        @Bind(R.id.tv_chatItem_time)
        TextView time;

        @Bind(R.id.tv_chatItem_message)
        TextView message;

        public MessageViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            messagesLayout.setOnClickListener(v -> {
                if(messageClickListener != null)
                    messageClickListener.onMessageItemClicked(name.getTag().toString());
            });
        }

        @SuppressWarnings("deprecation")
        void renderItem(MessagesModel messagesModel, String searchQuery) {
            String highlightColor = "#"+Integer.toHexString(ContextCompat.getColor( context, R.color.searchHighlight) & 0x00ffffff );

            name.setText(messagesModel.getContactName());
            time.setText(messagesModel.getTime());
            try {
                MessageParser messageParser = new MessageParser(messagesModel.getMessage());
                TextMessage textMessage = new TextMessage("");
                if(messageParser.getMessageType() == MessageParser.MessageType.text)
                    textMessage = (TextMessage)messageParser.getMessageObject();

                String messageLower = textMessage.getText().toLowerCase();
                int startPos = messageLower.indexOf(searchQuery.toLowerCase());
                if(!searchQuery.isEmpty() && startPos>=0) {
                    String textHTML = textMessage.getText().substring(0,startPos)
                            +"<font color=\""+highlightColor+"\">"+textMessage.getText().substring(startPos, startPos+searchQuery.length()) +"</font>"
                            +textMessage.getText().substring(startPos+searchQuery.length());

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                        message.setText(Html.fromHtml(textHTML, Html.FROM_HTML_MODE_LEGACY));
                    else
                        message.setText(Html.fromHtml(textHTML));
                } else {
                    message.setText(textMessage.getText());
                }

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

        void renderItem(String categoryName, boolean isVisible) {
            if(isVisible) {
                categoryTextView.setVisibility(View.VISIBLE);
                categoryTextView.setText(categoryName);
            }
            else {
                categoryTextView.setVisibility(View.GONE);
            }
        }
    }

    class NoResultViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.no_result)
        TextView noResult;

        public NoResultViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem(String filterQuery) {
            noResult.setText("No results found for '"+filterQuery+"'");
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