package com.stairway.spotlight.screens.message;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.stairway.spotlight.R;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.models.QuickReply;

import java.util.ArrayList;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 29/12/16.
 */

public class QuickRepliesAdapter extends RecyclerView.Adapter<QuickRepliesAdapter.QuickReplyViewHolder> {
    private MessagesAdapter.PostbackClickListener quickReplyClickListener;
    private List<QuickReply> quickReplies;
    public QuickRepliesAdapter(MessagesAdapter.PostbackClickListener quickReplyClickListener, List<QuickReply> quickReplies) {
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

        @Bind(R.id.rl_quick_reply)
        RelativeLayout bubbleView;

        @Bind(R.id.ll_quick_reply)
        LinearLayout quickReplyView;

        public QuickReplyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem(QuickReply qr) {
            textView.setText(qr.getTitle());

            quickReplyView.setOnTouchListener((v, event) -> {
                Logger.d(this, "Event: "+event.getAction());
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    bubbleView.setBackgroundResource(R.drawable.bg_quick_reply_active);
                    textView.setTextColor(0xffffffff);
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                    bubbleView.setBackgroundResource(R.drawable.bg_quick_reply_inactive);
                    textView.setTextColor(ContextCompat.getColor(SpotlightApplication.getContext(), R.color.sendMessageBubble));

                    if(event.getAction() == MotionEvent.ACTION_UP) {
                        quickReplyClickListener.sendPostbackMessage(qr.getTitle(), qr.getPayload());
                    }
                }
                return true;
            });
        }
    }
}
