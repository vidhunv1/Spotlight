package com.chat.ichat.screens.discover_bots;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chat.ichat.R;
import com.chat.ichat.api.bot.DiscoverBotsResponse;
import com.chat.ichat.core.Logger;
import java.util.ArrayList;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;
/**
 * Created by vidhun on 01/06/17.
 */
public class DiscoverBotsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> categories;
    private List<List<DiscoverBotsResponse.Bots>> bots;
    private final int VIEW_TYPE_BOT = 1;
    private final int VIEW_TYPE_CATEGORY = 2;
    private List<Integer> itemType;

    private Context context;
    private ContactClickListener contactClickListener;
    public DiscoverBotsAdapter(Context context, DiscoverBotsResponse discoverBotsResponse, ContactClickListener contactClickListener) {
        this.context = context;
        this.contactClickListener = contactClickListener;

        categories = new ArrayList<>();
        bots = new ArrayList<>();
        itemType = new ArrayList<>();
        int pos;
        for (DiscoverBotsResponse.Bots bots1 : discoverBotsResponse.getBotsList()) {
            Logger.d(this, "Bot:: "+bots1.toString());
            if(categories.contains(bots1.getCategory())) {
                pos = categories.indexOf(bots1.getCategory());
            } else if(bots1.getCategory()!=null && !bots1.getCategory().isEmpty()){
                pos = categories.size();
                categories.add(bots1.getCategory());
                bots.add(pos, new ArrayList<>());
                itemType.add(VIEW_TYPE_CATEGORY);
                itemType.add(VIEW_TYPE_BOT);
            } else {
                continue;
            }
            bots.get(pos).add(bots1);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_CATEGORY:
                View category = inflater.inflate(R.layout.item_dicover_category, parent, false);
                viewHolder = new DiscoverBotsAdapter.CategoryViewHolder(category);
                break;
            case VIEW_TYPE_BOT:
                View b = inflater.inflate(R.layout.item_search_suggestions, parent, false);
                viewHolder = new DiscoverBotsAdapter.BotViewHolder(b);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_BOT:
                BotViewHolder botViewHolder = (BotViewHolder) holder;
                botViewHolder.renderItem(bots.get(position/2));
                break;
            case VIEW_TYPE_CATEGORY:
                CategoryViewHolder categoryViewHolder = (CategoryViewHolder) holder;
                categoryViewHolder.renderItem(categories.get(position/2));
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return itemType.get(position);
    }

    @Override
    public int getItemCount() {
        return itemType.size();
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

    class BotViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.rv_search_suggestions)
        RecyclerView recyclerView;
        BotViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(List<DiscoverBotsResponse.Bots> botses) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(new BotsAdapter(context, botses, contactClickListener));
        }
    }

    interface ContactClickListener {
        void onContactItemClicked(String userId, String coverPicture, String botDescription, String category);
    }
}
