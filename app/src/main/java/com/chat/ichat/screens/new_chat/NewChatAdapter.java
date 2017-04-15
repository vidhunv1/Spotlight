package com.chat.ichat.screens.new_chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.chat.ichat.R;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.ImageUtils;

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
    private final int CONTACTS_NUMBER = 2;
    private final int NO_RESULT = 3;
    private final int HEADER = 4;
    private final int NO_CONTACTS = 5;


    private Context context;

    private List<Integer> filteredList;
    private String filterQuery;

    public NewChatAdapter(Context context, ContactClickListener contactClickListener) {
        this.context = context;
        this.contactClickListener = contactClickListener;
        this.itemList = new ArrayList<>();
        filteredList = new ArrayList<>();
        filterQuery = "";
    }

    public void setContactList(List<NewChatItemModel> contactItems) {
        this.itemList = contactItems;
        this.notifyItemRangeChanged(0, contactItems.size());
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
        if(filteredList.size()==0 && !filterQuery.isEmpty()) {
            return NO_RESULT;
        } else if(position==filteredList.size() && !filterQuery.isEmpty() && filteredList.size()!=0) {
            return CONTACTS_NUMBER;
        } else if(position == itemList.size()+1 && filterQuery.isEmpty() && filteredList.size()==0) {
            if(itemList.size()==0) {
                return NO_CONTACTS;
            } else {
                return CONTACTS_NUMBER;
            }
        } else if(position == 0 && filterQuery.isEmpty()) {
            Logger.d(this, "NewGroup");
            return HEADER;
        } else {
            return CONTACT;
        }
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
            case CONTACTS_NUMBER:
                View categoryView = inflater.inflate(R.layout.item_contacts_number, parent, false);
                viewHolder = new ContactsNumber(categoryView);
                break;
            case NO_RESULT:
                View noResultView = inflater.inflate(R.layout.item_no_result, parent, false);
                viewHolder = new NoResultViewHolder(noResultView);
                break;
            case HEADER:
                View newGroup = inflater.inflate(R.layout.item_header_contact, parent, false);
                viewHolder = new HeaderViewHolder(newGroup);
                break;
            case NO_CONTACTS:
                View noContacts = inflater.inflate(R.layout.item_no_contacts, parent, false);
                viewHolder = new NoContactsViewHolder(noContacts);
                break;
            default:
                return null;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int vPos) {
        int position;
        if(!filterQuery.isEmpty() && filteredList.size() > 0 && vPos < filteredList.size())
            position = filteredList.get(vPos);
        else
            position = vPos - 1;

        Logger.d(this, "Position: "+position+" vpos: "+vPos+" itemSize: "+itemList.size()+" itemViewType: "+holder.getItemViewType());
        switch (holder.getItemViewType()) {
            case CONTACT:
                ContactsViewHolder cVH = (ContactsViewHolder) holder;
                cVH.renderItem(itemList.get(position), filterQuery);
                break;
            case CONTACTS_NUMBER:
                ContactsNumber iVH = (ContactsNumber) holder;
                iVH.renderItem(itemList.size());
                break;
            case NO_RESULT:
                NoResultViewHolder noResultViewHolder = (NoResultViewHolder) holder;
                noResultViewHolder.renderItem(filterQuery);
                break;
            case HEADER:
//                NewGroupViewHolder newGroupViewHolder = (NewGroupViewHolder) holder;
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
                return filteredList.size() + 1;
        }
        return itemList.size() + 2;
    }

    class ContactsViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_item_chat)
        LinearLayout contactListContent;

        @Bind(R.id.iv_chatItem_profileImage)
        ImageView profileImage;

        @Bind(R.id.tv_chatItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_chatItem_message)
        TextView statusView;

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
                String textHTML = contactItem.getContactName().substring(0,startPos)
                        +"<font color=\""+highlightColor+"\">"+contactItem.getContactName().substring(startPos, startPos+query.length()) +"</font>"
                        +contactItem.getContactName().substring(startPos+query.length());

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    contactName.setText(Html.fromHtml(textHTML, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    contactName.setText(Html.fromHtml(textHTML));
                }
            } else {
                contactName.setText(contactItem.getContactName());
            }

            if(contactItem.getProfileDP()!=null && !contactItem.getProfileDP().isEmpty()) {
                Glide.with(context)
                        .load(contactItem.getProfileDP().replace("https://", "http://"))
                        .asBitmap().centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new BitmapImageViewTarget(profileImage) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                profileImage.setImageDrawable(circularBitmapDrawable);
                            }
                        });
            } else {
                profileImage.setImageDrawable(ImageUtils.getDefaultProfileImage(contactItem.getContactName(), contactItem.getUserName(), 18));
            }
            statusView.setText(contactItem.getUserId());
            contactName.setTag(contactItem.getUserName());
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class ContactsNumber extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_chatItem_contact)
        TextView contactNumber;

        public ContactsNumber(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem(int number) {
            if(number == 1) {
                contactNumber.setText(number + " contact");
            } else if(number >=1){
                contactNumber.setText(number + " contacts");
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

    class NoContactsViewHolder extends RecyclerView.ViewHolder {
        public NoContactsViewHolder(View itemView) {
            super(itemView);
        }
    }

    interface ContactClickListener {
        void onContactItemClicked(String userId);
    }
}