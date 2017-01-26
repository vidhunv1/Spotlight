package com.stairway.spotlight.screens.new_chat;

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
    private final int NO_RESULT = 3;
    private Context context;

    private List<Integer> filteredList;
    private String filterQuery;

    public NewChatAdapter(Context context, ContactClickListener contactClickListener, List<NewChatItemModel> contacts) {
        this.context = context;
        this.contactClickListener = contactClickListener;
        this.itemList = new ArrayList<>();
        filteredList = new ArrayList<>();
        filterQuery = "";
        this.itemList.addAll(contacts);
    }

    public void filterList(String query) {
        int modPos = 0, temp, item;

        filterQuery = query;
        String queryLower = filterQuery.toLowerCase();
        String contactNameLower;
        filteredList.clear();
        if(query.isEmpty()) {
            notifyDataSetChanged();
            return;
        }
        for (NewChatItemModel newChatItemModel : itemList) {
            contactNameLower = newChatItemModel.getContactName().toLowerCase();
            if (contactNameLower.contains(queryLower)) {
                item = itemList.indexOf(newChatItemModel);
                filteredList.add(item);

                if (contactNameLower.startsWith(queryLower)) {
                    temp = filteredList.get(modPos);
                    filteredList.set(modPos, item);
                    filteredList.set(filteredList.size() - 1, temp);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if(filteredList.size()==0 && !filterQuery.isEmpty())
            return NO_RESULT;
        if(position==itemList.size() && filterQuery.isEmpty())
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
            case NO_RESULT:
                View noResultView = inflater.inflate(R.layout.item_no_result, parent, false);
                viewHolder = new NoResultViewHolder(noResultView);
                break;
            default:
                return null;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int vPos) {
        int position;
        if(!filterQuery.isEmpty() && filteredList.size()>0)
            position = filteredList.get(vPos);
        else
            position = vPos;

        Logger.d(this, "Position: "+position+" vpos: "+vPos+" itemSize: "+itemList.size()+" itemViewType: "+holder.getItemViewType());
        switch (holder.getItemViewType()) {
            case CONTACT:
                ContactsViewHolder cVH = (ContactsViewHolder) holder;
                cVH.renderItem(itemList.get(position), filterQuery);
                break;
            case CATEGORY:
                CategoryViewHolder catVH = (CategoryViewHolder) holder;
                catVH.renderItem(itemList.size(), filterQuery);
                break;
            case NO_RESULT:
                NoResultViewHolder noResultViewHolder = (NoResultViewHolder) holder;
                noResultViewHolder.renderItem(filterQuery);
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        if(!filterQuery.isEmpty()) {
            if(filteredList.size()==0)
                return 1;
            else
                return filteredList.size();
        }
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

        @Bind(R.id.view_contactItem_divider)
        View divider;

        ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            contactListContent.setOnClickListener(view -> {
                if(contactClickListener != null)
                    contactClickListener.onContactItemClicked(contactName.getTag().toString());
            });
        }

        @SuppressWarnings("deprecation")
        void renderItem(NewChatItemModel contactItem, String query) {
            String highlightColor = "#"+Integer.toHexString(ContextCompat.getColor( context, R.color.searchHighlight) & 0x00ffffff );

            String contactNameLower = contactItem.getContactName().toLowerCase();
            int startPos = contactNameLower.indexOf(query.toLowerCase());
            if(!query.isEmpty() && startPos>=0) {
                //color
//                String textHTML = contactItem.getContactName().substring(0,startPos)
//                        +"<font color=\""+highlightColor+"\">"+contactItem.getContactName().substring(startPos, startPos+query.length()) +"</font>"
//                        +contactItem.getContactName().substring(startPos+query.length());

                String textHTML = contactItem.getContactName().substring(0,startPos)
                        +"<b>"+contactItem.getContactName().substring(startPos, startPos+query.length()) +"</b>"
                        +contactItem.getContactName().substring(startPos+query.length());

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                    contactName.setText(Html.fromHtml(textHTML, Html.FROM_HTML_MODE_LEGACY));
                else
                    contactName.setText(Html.fromHtml(textHTML));
            } else
                contactName.setText(contactItem.getContactName());

            status.setText("ID: " + contactItem.getUserId());
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

        public void renderItem(int count, String filterQuery) {
            categoryName.setText(count+" contacts");
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
}
