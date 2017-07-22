package com.chat.ichat.screens.search;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.chat.ichat.R;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.CircleTransformation;
import com.chat.ichat.core.lib.ImageUtils;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.screens.new_chat.AddContactUseCase;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
/**
 * Created by vidhun on 13/05/17.
 */

public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.SuggestionsViewHolder> {
    private List<ContactResult> contactsModels;
    private SearchAdapter.ContactClickListener contactClickListener;
    private Context context;
    public SuggestionsAdapter(Context context, List<ContactResult> contactsModels, SearchAdapter.ContactClickListener contactClickListener) {
        this.context = context;
        this.contactsModels = contactsModels;
        this.contactClickListener = contactClickListener;
    }

    @Override
    public SuggestionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_search_contact_suggested_small, parent, false);
        return new SuggestionsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SuggestionsViewHolder holder, int position) {
        holder.renderItem(contactsModels.get(position));
    }

    @Override
    public int getItemCount() {
        return contactsModels.size();
    }

    public class SuggestionsViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_chatItem_profileImage)
        ImageView imageView;

        @Bind(R.id.suggested_view)
        LinearLayout suggestedView;

        @Bind(R.id.tv_chatItem_name)
        TextView name;

        private ContactResult contactResult;
        public SuggestionsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem(ContactResult contactsModel) {
            Logger.d(this, "ContactsModel: "+contactsModel);
            this.contactResult = contactsModel;
            name.setText(contactsModel.getContactName());
            name.setTag(contactsModel.getUsername());
            if(contactsModel.getProfileDP()!=null && !contactsModel.getProfileDP().isEmpty()) {
                DrawableRequestBuilder dp = Glide.with(context)
                        .load(contactsModel.getProfileDP().replace("https://", "http://"))
                        .crossFade()
                        .bitmapTransform(new CenterCrop(context), new CircleTransformation(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                dp.into(imageView);
            } else {
                Drawable textProfileDrawable = ImageUtils.getDefaultProfileImage(contactsModel.getContactName(), contactsModel.getUsername(), 18);
                imageView.setImageDrawable(textProfileDrawable);
            }
        }

        @OnClick(R.id.suggested_view)
        public void onViewClick() {
            Logger.d(this, "Clicked: "+name.getTag().toString());
            if(contactClickListener!=null) {
                ProgressDialog progressDialog = ProgressDialog.show(context, "", "Loading. Please wait...", true);
                AddContactUseCase addContactUseCase = new AddContactUseCase(ApiManager.getUserApi(), ContactStore.getInstance(), ApiManager.getBotApi(), BotDetailsStore.getInstance());
                addContactUseCase.execute(contactResult.getUserId(), false)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<ContactResult>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onNext(ContactResult contactResult) {
                                progressDialog.dismiss();
                                contactClickListener.onContactItemClicked(name.getTag().toString(),2);
                            }
                        });
            }
        }
    }
}
