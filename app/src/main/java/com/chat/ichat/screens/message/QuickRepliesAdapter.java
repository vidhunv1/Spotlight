package com.chat.ichat.screens.message;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chat.ichat.R;
import com.chat.ichat.application.SpotlightApplication;
import com.chat.ichat.core.Logger;
import com.chat.ichat.models.QuickReply;

import java.util.ArrayList;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 29/12/16.
 */

public class QuickRepliesAdapter extends RecyclerView.Adapter<QuickRepliesAdapter.QuickReplyViewHolder> {
    private MessagesAdapter.PostbackClickListener quickReplyClickListener;
    private MessagesAdapter.QuickReplyActionListener quickReplyActionListener;

    private List<QuickReply> quickReplies;
    private Context context;
    public QuickRepliesAdapter(Context context, MessagesAdapter.PostbackClickListener quickReplyClickListener, MessagesAdapter.QuickReplyActionListener quickReplyActionListener, List<QuickReply> quickReplies) {
        this.quickReplyClickListener = quickReplyClickListener;
        this.quickReplyActionListener = quickReplyActionListener;
        this.context = context;

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
        int count = 0;
        for (QuickReply quickReply : quickReplies) {
            if(quickReply.getContentType() == QuickReply.ContentType.text || quickReply.getContentType()==null) { //text
                if(quickReply.getTitle()!=null && quickReply.getTitle().length()!=0) {
                    count++;
                }
            } else if(quickReply.getContentType() == QuickReply.ContentType.location){
                count++;
            }
        }
        return count;
    }

    public class QuickReplyViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_item_quick_reply)
        TextView textView;

        @Bind(R.id.rl_quick_reply)
        RelativeLayout bubbleView;

        @Bind(R.id.ll_quick_reply)
        LinearLayout quickReplyView;

        @Bind(R.id.iv_qr)
        ImageView image;

        public QuickReplyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem(QuickReply qr) {
            if(qr.getContentType() == QuickReply.ContentType.location) {
                textView.setText("Send Location");
                image.setVisibility(View.VISIBLE);
                image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_location_qr));
            } else {
                image.setVisibility(View.GONE);
                textView.setText(qr.getTitle());
            }

            quickReplyView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    bubbleView.setBackgroundResource(R.drawable.bg_quick_reply_active);
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                    bubbleView.setBackgroundResource(R.drawable.bg_quick_reply_inactive);
                    if(event.getAction() == MotionEvent.ACTION_UP) {
                        if(qr.getContentType()!=null && qr.getContentType() == QuickReply.ContentType.location) {
                            quickReplyActionListener.navigateToGetLocation();
                        } else {
                            quickReplyClickListener.sendPostbackMessage(qr.getTitle(), qr.getPayload());
                        }
                    }
                }
                return true;
            });
        }
    }
}
