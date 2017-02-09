package com.stairway.spotlight.screens.message;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.stairway.spotlight.R;
import com.stairway.spotlight.models.QuickReply;

import java.util.ArrayList;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 29/12/16.
 */

public class QuickRepliesAdapter extends RecyclerView.Adapter<QuickRepliesAdapter.QuickReplyViewHolder> {
    private QuickReplyClickListener quickReplyClickListener;
    private List<QuickReply> quickReplies;
    public QuickRepliesAdapter(QuickReplyClickListener quickReplyClickListener, List<QuickReply> quickReplies) {
        this.quickReplyClickListener = quickReplyClickListener;
        this.quickReplies = new ArrayList<>();
        this.quickReplies.addAll(quickReplies);
    }

    @Override
    public QuickReplyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View contactView = inflater.inflate(R.layout.item_quick_reply, parent, false);
        return new QuickReplyViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(QuickReplyViewHolder holder, int position) {
        holder.renderItem(quickReplies.get(position));
    }

    @Override
    public int getItemCount() {
        return quickReplies.size();
    }

    public class QuickReplyViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_item_quick_reply)
        TextView textView;

        public QuickReplyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem(QuickReply qr) {
            textView.setText(qr.getTitle());
            textView.setOnClickListener(v -> quickReplyClickListener.onQuickReplyClicked(qr.getTitle()));
        }
    }

    public interface QuickReplyClickListener {
        void onQuickReplyClicked(String text);
    }
}
