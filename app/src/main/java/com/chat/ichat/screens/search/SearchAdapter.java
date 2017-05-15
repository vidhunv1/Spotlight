package com.chat.ichat.screens.search;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.chat.ichat.R;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.user.UserApi;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.api.user._User;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.core.lib.CircleTransformation;
import com.chat.ichat.core.lib.ImageUtils;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.screens.new_chat.AddContactUseCase;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
/**
 * Created by vidhun on 17/12/16.
 */
class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ContactResult> suggestedUsersList;
    private List<ContactResult> contactsList;
    private String searchQuery;

    private Context context;
    private ContactClickListener contactClickListener;

    private final int VIEW_TYPE_CONTACT = 1;
    private final int VIEW_TYPE_CATEGORY_CONTACTS = 2;;
    private final int VIEW_TYPE_CATEGORY_SUGGESTED = 3;
    private final int VIEW_TYPE_NO_RESULT = 4;
    private final int VIEW_TYPE_SUGGESTIONS = 5;
    private final int VIEW_TYPE_CATEGORY_USERNAME_SEARCH = 6;
    private final int VIEW_TYPE_USERNAME_SEARCH = 7;

    private List<Integer> itemType;

    public SearchAdapter(Context context, ContactClickListener contactClickListener) {
        this.context = context;
        this.contactClickListener = contactClickListener;
        this.contactsList = new ArrayList<>();
        this.suggestedUsersList = new ArrayList<>();
        this.searchQuery = "";
        this.itemType = new ArrayList<>();
    }

    public void displaySearch(String searchTerm, List<ContactResult> contactsModelList, List<ContactResult> suggestedModelList, ContactResult searchUser) {
        this.searchQuery = searchTerm;
        this.contactsList.clear();
        if(contactsModelList!=null)
            this.contactsList.addAll(contactsModelList);
        this.suggestedUsersList.clear();
        if(suggestedModelList!=null)
            this.suggestedUsersList.addAll(suggestedModelList);
        handleDtasetChanged();
    }

    public void initSearch(String searchTerm, List<ContactResult> contactsModelList, List<ContactResult> suggestedModelList, ContactResult searchUser) {
        this.searchQuery = searchTerm;
        this.contactsList.clear();
        if(contactsModelList!=null)
            this.contactsList.addAll(contactsModelList);
        this.suggestedUsersList.clear();
        if(suggestedModelList!=null)
            this.suggestedUsersList.addAll(suggestedModelList);
        handleDtasetChanged();
    }

    public void handleDtasetChanged() {
        itemType.clear();
        if(searchQuery==null || searchQuery.length()==0) {
            itemType.add(VIEW_TYPE_CATEGORY_SUGGESTED);
            if (suggestedUsersList != null && suggestedUsersList.size() > 0) {
                itemType.add(VIEW_TYPE_SUGGESTIONS);
            }
        } else {
            if (contactsList != null && contactsList.size() > 0) {
                itemType.add(VIEW_TYPE_CATEGORY_CONTACTS);
                for (int i = 0; i < contactsList.size(); i++) {
                    itemType.add(VIEW_TYPE_CONTACT);
                }

                itemType.add(0, VIEW_TYPE_CATEGORY_USERNAME_SEARCH);
                itemType.add(1, VIEW_TYPE_USERNAME_SEARCH);
            } else {
                itemType.add(0, VIEW_TYPE_CATEGORY_USERNAME_SEARCH);
                itemType.add(1, VIEW_TYPE_USERNAME_SEARCH);
            }
        }

        if(itemType.size() == 0)
            itemType.add(VIEW_TYPE_NO_RESULT);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_CONTACT:
                View contactView = inflater.inflate(R.layout.item_chat, parent, false);
                viewHolder = new ContactViewHolder(contactView);
                break;
            case VIEW_TYPE_CATEGORY_CONTACTS:
                View categoryView = inflater.inflate(R.layout.item_search_category, parent, false);
                viewHolder = new CategoryViewHolder(categoryView);
                break;
            case VIEW_TYPE_NO_RESULT:
                View noResults = inflater.inflate(R.layout.item_no_result, parent, false);
                viewHolder = new NoResultViewHolder(noResults);
                break;
            case VIEW_TYPE_CATEGORY_SUGGESTED:
                View suggestedCategoryView = inflater.inflate(R.layout.item_search_category, parent, false);
                viewHolder = new CategoryViewHolder(suggestedCategoryView);
                break;
            case VIEW_TYPE_SUGGESTIONS:
                View suggestionsView = inflater.inflate(R.layout.item_search_suggestions, parent, false);
                viewHolder = new SuggestionsViewHolder(suggestionsView);
                break;
            case VIEW_TYPE_CATEGORY_USERNAME_SEARCH:
                View categoryUsernameSearch = inflater.inflate(R.layout.item_search_category, parent, false);
                viewHolder = new CategoryViewHolder(categoryUsernameSearch);
                break;
            case VIEW_TYPE_USERNAME_SEARCH:
                View usernameSearch = inflater.inflate(R.layout.item_search_username, parent, false);
                viewHolder = new UsernameSearchViewHolder(usernameSearch);
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
                contactViewHolder.renderContactItem(contactsList.get(position-3), searchQuery);
                break;
            case VIEW_TYPE_CATEGORY_CONTACTS:
                CategoryViewHolder categoryNameViewHolder1 = (CategoryViewHolder) holder;
                categoryNameViewHolder1.renderItem("Contacts");
                break;
            case VIEW_TYPE_CATEGORY_SUGGESTED:
                CategoryViewHolder categoryNameViewHolder2 = (CategoryViewHolder) holder;
                categoryNameViewHolder2.renderItem("Suggested");
                break;
            case VIEW_TYPE_SUGGESTIONS:
                SuggestionsViewHolder suggestionsViewHolder = (SuggestionsViewHolder) holder;
                suggestionsViewHolder.renderItem(suggestedUsersList);
                break;
            case VIEW_TYPE_NO_RESULT:
                NoResultViewHolder noResultViewHolder = (NoResultViewHolder) holder;
                noResultViewHolder.renderItem(searchQuery);
                break;
            case VIEW_TYPE_CATEGORY_USERNAME_SEARCH:
                CategoryViewHolder categoryNameViewHolder3 = (CategoryViewHolder) holder;
                categoryNameViewHolder3.renderItem("Username Search");
                break;
            case VIEW_TYPE_USERNAME_SEARCH:
                UsernameSearchViewHolder usernameSearchViewHolder = (UsernameSearchViewHolder) holder;
                usernameSearchViewHolder.renderItem(searchQuery);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if(itemType == null)
            return 0;
        return itemType.size();
    }

    @Override
    public int getItemViewType(int position) {
        return  itemType.get(position);
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

        @Bind(R.id.iv_delivery_status)
        ImageView deliveryStatus;

        @Bind(R.id.tv_chatItem_time)
        TextView time;

        ContactViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressWarnings("deprecation")
        void renderContactItem(ContactResult contactItem, String searchQuery) {
            deliveryStatus.setVisibility(View.GONE);
            time.setVisibility(View.GONE);
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
            contactName.setTag(contactItem.getUsername());
            divider.setVisibility(View.GONE);

            profileImage.setImageDrawable(ImageUtils.getDefaultProfileImage(contactItem.getContactName(), contactItem.getUserId(), 18));

            if(contactItem.getProfileDP()!=null && !contactItem.getProfileDP().isEmpty()) {
                Logger.d(this, "Setting profile dp: "+contactItem.getProfileDP());
                Glide.with(context)
                        .load(contactItem.getProfileDP().replace("https://", "http://"))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .crossFade()
                        .placeholder(ImageUtils.getDefaultProfileImage(contactItem.getContactName(), contactItem.getUserId(), 18))
                        .bitmapTransform(new CenterCrop(context), new CircleTransformation(context))
                        .into(profileImage);
            } else {
                profileImage.setImageDrawable(ImageUtils.getDefaultProfileImage(contactItem.getContactName(), contactItem.getUserId(), 18));
            }

            contactListContent.setOnClickListener(view -> {
                if(contactClickListener != null)
                    contactClickListener.onContactItemClicked(contactName.getTag().toString());
            });
        }
    }

    class SuggestionsViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.rv_search_suggestions)
        RecyclerView recyclerView;
        SuggestionsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(List<ContactResult> contactsModels) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(new SuggestionsAdapter(context, contactsModels, contactClickListener));
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

    class UsernameSearchViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.ll_item_chat)
        LinearLayout chatListContent;

        @Bind(R.id.iv_chatItem_profileImage)
        ImageView profileImage;

        @Bind(R.id.tv_chatItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_chatItem_message)
        TextView lastMessage;

        @Bind(R.id.progress_bar)
        ProgressBar progressBar;

        @Bind(R.id.searching)
        TextView searching;

        @Bind(R.id.text_content)
        LinearLayout textContent;

        ContactResult searchUser;

        UsernameSearchViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            chatListContent.setOnClickListener(view -> {
                if(searchUser!=null) {
                    progressBar.setVisibility(View.VISIBLE);
                    searching.setVisibility(View.VISIBLE);
                    profileImage.setVisibility(View.GONE);
                    textContent.setVisibility(View.GONE);
                    searching.setText("");

                    AddContactUseCase addContactUseCase = new AddContactUseCase(ApiManager.getUserApi(), ContactStore.getInstance(), ApiManager.getBotApi(), BotDetailsStore.getInstance());
                    addContactUseCase.execute(searchUser.getUserId(), false)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<ContactResult>() {
                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    textContent.setVisibility(View.GONE);
                                    searching.setVisibility(View.VISIBLE);
                                    profileImage.setVisibility(View.INVISIBLE);

                                    searching.setText("Network error.");
                                }

                                @Override
                                public void onNext(ContactResult contactResult) {
                                    contactClickListener.onContactItemClicked(searchUser.getUsername());
                                }
                            });
                }
            });

        }

        void renderItem(String query) {
            progressBar.setVisibility(View.VISIBLE);
            searching.setVisibility(View.VISIBLE);
            profileImage.setVisibility(View.GONE);
            textContent.setVisibility(View.GONE);
            searching.setText("Searching...");

            ApiManager.getUserApi().findUserByUserId(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        textContent.setVisibility(View.GONE);
                        searching.setVisibility(View.VISIBLE);
                        profileImage.setVisibility(View.INVISIBLE);

                        searching.setText("Network error.");
                        Logger.d(this, "OnError");
                    }

                    @Override
                    public void onNext(UserResponse userResponse) {
                        if(userResponse.isSuccess()) {
                            searchUser = new ContactResult();
                            searchUser.setUserId(userResponse.getUser().getUserId());
                            searchUser.setUsername(userResponse.getUser().getUsername());
                            searchUser.setDisplayName(userResponse.getUser().getName());
                            searchUser.setUserType(userResponse.getUser().getUserType());
                            searchUser.setProfileDP(userResponse.getUser().getProfileDP());
                            searchUser.setAdded(false);
                            searchUser.setBlocked(false);

                            Logger.d(this, "successUsername");
                            progressBar.setVisibility(View.INVISIBLE);
                            textContent.setVisibility(View.VISIBLE);
                            searching.setVisibility(View.GONE);
                            profileImage.setVisibility(View.VISIBLE);
                            _User user = userResponse.getUser();
                            contactName.setText(AndroidUtils.displayNameStyle(user.getName()));
                            lastMessage.setText(user.getUserId());

                            if(user.getProfileDP()!=null && !user.getProfileDP().isEmpty()) {
                                Glide.with(context)
                                        .load(user.getProfileDP().replace("https://", "http://"))
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .crossFade()
                                        .placeholder(ImageUtils.getDefaultProfileImage(user.getName(), user.getUserId(), 18))
                                        .bitmapTransform(new CenterCrop(context), new CircleTransformation(context))
                                        .into(profileImage);
                            } else {
                                profileImage.setImageDrawable(ImageUtils.getDefaultProfileImage(user.getName(), user.getUserId(), 18));
                            }
                            contactName.setTag(user.getUserId());
                        } else {
                            Logger.d(this, "noUsername");
                            progressBar.setVisibility(View.INVISIBLE);
                            textContent.setVisibility(View.GONE);
                            searching.setVisibility(View.VISIBLE);
                            profileImage.setVisibility(View.INVISIBLE);

                            searching.setText("No user found with '"+query+"'");
                        }
                    }
                });
        }
    }

    interface ContactClickListener {
        void onContactItemClicked(String username);
    }
}