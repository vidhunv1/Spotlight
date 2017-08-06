//package com.chat.ichat.screens.discover_bots;
//
//import android.content.Context;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.chat.ichat.R;
//import com.chat.ichat.api.bot.DiscoverBotsResponse;
//import com.chat.ichat.core.Logger;
//import com.chat.ichat.core.lib.AndroidUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
///**
// * Created by vidhun on 01/06/17.
// */
//public class DiscoverBotsAdapter1 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//    private List<String> categories;
//    private List<List<DiscoverBotsResponse.Bots>> bots;
//    private final int VIEW_TYPE_BOT_VERTICAL = 1;
//    private final int VIEW_TYPE_CATEGORY = 2;
//    private final int VIEW_TYPE_BOT_HORIZONTAL = 3;
//    private List<Integer> itemType;
//
//    private Context context;
//    private ActionListener actionListener;
//    public DiscoverBotsAdapter1(Context context, DiscoverBotsResponse discoverBotsResponse, ActionListener actionListener) {
//        this.context = context;
//        this.actionListener = actionListener;
//
//        categories = new ArrayList<>();
//        bots = new ArrayList<>();
//        itemType = new ArrayList<>();
//        int pos;
//        for (DiscoverBotsResponse.Bots bots1 : discoverBotsResponse.getBotsList()) {
//            Logger.d(this, "Bot:: "+bots1.toString());
//            if(categories.contains(bots1.getCategory())) {
//                pos = categories.indexOf(bots1.getCategory());
//            } else if(bots1.getCategory()!=null && !bots1.getCategory().isEmpty()){
//                pos = categories.size();
//                categories.add(bots1.getCategory());
//                bots.add(pos, new ArrayList<>());
//                itemType.add(VIEW_TYPE_CATEGORY);
//                itemType.add(VIEW_TYPE_BOT_VERTICAL);
//            } else {
//                continue;
//            }
//            bots.get(pos).add(bots1);
//        }
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        RecyclerView.ViewHolder viewHolder = null;
//        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
//
//        switch (viewType) {
//            case VIEW_TYPE_CATEGORY:
//                View category = inflater.inflate(R.layout.item_discover_category, parent, false);
//                viewHolder = new DiscoverBotsAdapter1.CategoryViewHolder(category);
//                break;
//            case VIEW_TYPE_BOT_HORIZONTAL:
//                View b = inflater.inflate(R.layout.recycler_view, parent, false);
//                viewHolder = new DiscoverBotsAdapter1.BotViewHolder(b);
//                break;
//            case VIEW_TYPE_BOT_VERTICAL:
//                View c = inflater.inflate(R.layout.recycler_view, parent, false);
//                viewHolder = new DiscoverBotsAdapter1.BotViewHolder(c);
//                break;
//        }
//        return viewHolder;
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        switch (holder.getItemViewType()) {
//            case VIEW_TYPE_BOT_HORIZONTAL:
//                BotViewHolder botViewHolder = (BotViewHolder) holder;
//                botViewHolder.renderItem(bots.get(position/2), true);
//                break;
//            case VIEW_TYPE_BOT_VERTICAL:
//                BotViewHolder botViewHolder1 = (BotViewHolder) holder;
//                botViewHolder1.renderItem(bots.get(position/2), false);
//                break;
//            case VIEW_TYPE_CATEGORY:
//                CategoryViewHolder categoryViewHolder = (CategoryViewHolder) holder;
//                categoryViewHolder.renderItem(categories.get(position/2), bots.get(position/2));
//                break;
//        }
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return itemType.get(position);
//    }
//
//    @Override
//    public int getItemCount() {
//        return itemType.size();
//    }
//
//    class CategoryViewHolder extends RecyclerView.ViewHolder {
//        @Bind(R.id.rv_layout)
//        RelativeLayout layout;
//        @Bind(R.id.tv_category)
//        TextView categoryTextView;
//        @Bind(R.id.tv_description)
//        TextView descriptionTextView;
//
//        private List<DiscoverBotsResponse.Bots> botses;
//        CategoryViewHolder(View itemView) {
//            super(itemView);
//            ButterKnife.bind(this, itemView);
//        }
//        void renderItem(String categoryName, List<DiscoverBotsResponse.Bots> botses) {
//            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT, (int)AndroidUtils.px(56));
//            layout.setLayoutParams(layoutParams);
//            categoryTextView.setText(categoryName);
//            descriptionTextView.setVisibility(View.GONE);
//            this.botses = botses;
//        }
//
//        void renderItem(String categoryName, String description, List<DiscoverBotsResponse.Bots> botses) {
//            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT, (int)AndroidUtils.px(66));
//            layout.setLayoutParams(layoutParams);
//            this.botses = botses;
//            categoryTextView.setText(categoryName);
//            descriptionTextView.setVisibility(View.VISIBLE);
//            descriptionTextView.setText(description);
//        }
//
//        @OnClick(R.id.tv_see_all)
//        public void onSeeAllClick() {
//            actionListener.navigateToDiscoverCategory(botses);
//        }
//    }
//
//    class BotViewHolder extends RecyclerView.ViewHolder {
//        @Bind(R.id.rv_search_suggestions)
//        RecyclerView recyclerView;
//        BotViewHolder(View itemView) {
//            super(itemView);
//            ButterKnife.bind(this, itemView);
//        }
//
//        void renderItem(List<DiscoverBotsResponse.Bots> botses, boolean isHorizontal) {
//            if(isHorizontal) {
//                LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
//                recyclerView.setLayoutManager(layoutManager);
//                recyclerView.setAdapter(new BotsAdapter(context, botses, true, actionListener));
//            } else {
//                LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
//                recyclerView.setLayoutManager(layoutManager);
//                recyclerView.setAdapter(new BotsAdapter(context, botses, false, actionListener));
//            }
//        }
//    }
//
//    interface ActionListener {
//        void onContactItemClicked(String userId, String coverPicture, String botDescription, String category);
//        void navigateToDiscoverCategory(List<DiscoverBotsResponse.Bots> botses);
//    }
//}
