package com.chat.ichat.screens.message;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.chat.ichat.R;
import com.chat.ichat.application.SpotlightApplication;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.core.lib.RoundedCornerTransformation;
import com.chat.ichat.models.GenericTemplate;
import com.chat.ichat.models._Button;
import com.chat.ichat.models._DefaultAction;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 17/03/17.
 */
public class GenericTemplateAdapter extends RecyclerView.Adapter<GenericTemplateAdapter.ReceiveTemplateGenericViewHolder> {
    private MessagesAdapter.PostbackClickListener postbackClickListener;
    private MessagesAdapter.UrlClickListener urlClickListener;
    private List<GenericTemplate> genericTemplateList;
    private Context context;
    private int conversationBubbleType;

    public GenericTemplateAdapter(Context context, List<GenericTemplate> genericTemplates, int conversationBubbleType, MessagesAdapter.PostbackClickListener postbackClickListener, MessagesAdapter.UrlClickListener urlClickListener) {
        this.conversationBubbleType = conversationBubbleType;
        this.context = context;
        this.postbackClickListener = postbackClickListener;
        this.urlClickListener = urlClickListener;
        this.genericTemplateList = genericTemplates;
        this.conversationBubbleType = conversationBubbleType;
    }

    @Override
    public ReceiveTemplateGenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View templateView = inflater.inflate(R.layout.item_message_receive_template_generic, parent, false);
        return new ReceiveTemplateGenericViewHolder(templateView);
    }

    @Override
    public void onBindViewHolder(ReceiveTemplateGenericViewHolder holder, int position) {
        holder.renderItem(genericTemplateList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return genericTemplateList.size();
    }

    private int bubbleType(int position) {
        final int START = 1, MIDDLE = 2, END = 3, FULL = 0;
        if(position == 0) {
            if(genericTemplateList.size() == 0) {
                return FULL;
            } else {
                return START;
            }
        } else if(position > 0 && position < (genericTemplateList.size()-1)) {
            return MIDDLE;
        } else {
            return END;
        }
    }

    class ReceiveTemplateGenericViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_rcv_template_image)
        ImageView templateImage;
        @Bind(R.id.tv_rcv_template_title)
        TextView title;
        @Bind(R.id.tv_rcv_template_subtitle)
        TextView subtitle;
        @Bind(R.id.tv_rcv_template_url)
        TextView url;
        @Bind(R.id.ll_bubble)
        LinearLayout bubble;
        @Bind(R.id.rl_message_receive_generic)
        RelativeLayout bubbleLayout;
        @Bind(R.id.ll_rcv_template_text)
        LinearLayout textContent;
        @Bind(R.id.ll_rcv_template_buttons)
        LinearLayout buttonLayout;

        TextView buttons[];

        ReceiveTemplateGenericViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            buttons = new TextView[3];
            buttons[0] = (TextView) itemView.findViewById(R.id.tv_rcv_button1);
            buttons[1] = (TextView) itemView.findViewById(R.id.tv_rcv_button2);
            buttons[2] = (TextView) itemView.findViewById(R.id.tv_rcv_button3);

            buttons[0].setVisibility(View.GONE);
            buttons[1].setVisibility(View.GONE);
            buttons[2].setVisibility(View.GONE);
        }

        void renderItem(GenericTemplate genericTemplate, int position) {
            int fullRadius = (int)context.getResources().getDimension(R.dimen.bubble_full_corner_radius);
            int midRadius = (int)context.getResources().getDimension(R.dimen.bubble_mid_corner_radius);
            Logger.d(this, "GenericTemplate: "+genericTemplate.toString());
            int bubbleType = bubbleType(position);
            Resources resources = SpotlightApplication.getContext().getResources();
            int startPadding, endPadding;
            Logger.d(this, "BubbleType: "+bubbleType);
            if(bubbleType == 0 || bubbleType == 1) {
                startPadding = (int) AndroidUtils.px(60);
                endPadding = 0;
            } else if(bubbleType == 2) {
                startPadding = (int) AndroidUtils.px(4);
                endPadding = (int) AndroidUtils.px(0);
            } else {
                startPadding = (int) AndroidUtils.px(4);
                endPadding = (int)AndroidUtils.px(40);
            }

            RoundedCornerTransformation roundedCornerTransformationL = null;
            RoundedCornerTransformation roundedCornerTransformationR = null;
            if(position == 0) {
                roundedCornerTransformationR = new RoundedCornerTransformation(context, midRadius, 0, RoundedCornerTransformation.CornerType.TOP_RIGHT);
                if(conversationBubbleType == 0 || conversationBubbleType == 1) {
                    roundedCornerTransformationL = new RoundedCornerTransformation(context, fullRadius, 0, RoundedCornerTransformation.CornerType.TOP_LEFT);
                } else {
                    roundedCornerTransformationL = new RoundedCornerTransformation(context, midRadius, 0, RoundedCornerTransformation.CornerType.TOP_LEFT);
                }
            } else if(position<(genericTemplateList.size()-1)) {
                roundedCornerTransformationR = new RoundedCornerTransformation(context, midRadius, 0, RoundedCornerTransformation.CornerType.TOP_RIGHT);
                roundedCornerTransformationL = new RoundedCornerTransformation(context, midRadius, 0, RoundedCornerTransformation.CornerType.TOP_LEFT);
            } else {
                roundedCornerTransformationR = new RoundedCornerTransformation(context, fullRadius, 0, RoundedCornerTransformation.CornerType.TOP_RIGHT);
                roundedCornerTransformationL = new RoundedCornerTransformation(context, midRadius, 0, RoundedCornerTransformation.CornerType.TOP_LEFT);
            }

            switch (bubbleType) {
                case 0:
                    bubbleLayout.setPadding(startPadding, 0, endPadding, (int) resources.getDimension(R.dimen.bubble_start_top_space));
                    buttonLayout.setBackgroundResource(R.drawable.bg_lower_template_generic_bottom);

                    if(genericTemplate.getImageUrl()!=null && !genericTemplate.getImageUrl().isEmpty()) {
                        Glide.with(context).load(genericTemplate.getImageUrl())
                                .bitmapTransform(new CenterCrop(context), roundedCornerTransformationR, roundedCornerTransformationL)
                                .into(templateImage);
                    } else {
                        templateImage.setVisibility(View.GONE);
                    }
                case 1:
                    bubbleLayout.setPadding(startPadding, 0, endPadding, (int) resources.getDimension(R.dimen.bubble_start_top_space));
                    buttonLayout.setBackgroundResource(R.drawable.bg_lower_template_generic_start);

                    if(genericTemplate.getImageUrl()!=null && !genericTemplate.getImageUrl().isEmpty()) {
                        Glide.with(context).load(genericTemplate.getImageUrl())
                                .bitmapTransform(new CenterCrop(context), roundedCornerTransformationR, roundedCornerTransformationL)
                                .into(templateImage);
                    } else {
                        templateImage.setVisibility(View.GONE);
                    }
                case 2:
                    bubbleLayout.setPadding(startPadding, 0, endPadding, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    buttonLayout.setBackgroundResource(R.drawable.bg_lower_template_generic_center);
                    if(genericTemplate.getImageUrl()!=null && !genericTemplate.getImageUrl().isEmpty()) {
                        Glide.with(context)
                                .load(genericTemplate.getImageUrl())
                                .bitmapTransform(new CenterCrop(context), roundedCornerTransformationR, roundedCornerTransformationL)
                                .into(templateImage);
                    } else {
                        templateImage.setVisibility(View.GONE);
                    }
                    break;
                case 3:
                    bubbleLayout.setPadding(startPadding, 0, endPadding, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    buttonLayout.setBackgroundResource(R.drawable.bg_lower_template_generic_middle);
                    if(genericTemplate.getImageUrl()!=null && !genericTemplate.getImageUrl().isEmpty()) {
                        Glide.with(context)
                                .load(genericTemplate.getImageUrl())
                                .bitmapTransform(new CenterCrop(context), roundedCornerTransformationR, roundedCornerTransformationL)
                                .into(templateImage);
                    } else {
                        templateImage.setVisibility(View.GONE);
                    }
                    break;
            }

            title.setText(genericTemplate.getTitle());
            if (genericTemplate.getSubtitle() != null && !genericTemplate.getSubtitle().isEmpty()) {
                Logger.d(this, "Setting Subtitle:"+genericTemplate.getSubtitle());
                subtitle.setText(genericTemplate.getSubtitle());
            } else {
                subtitle.setVisibility(View.GONE);
            }
            if(genericTemplate.getDefaultAction()!=null) {
                if (genericTemplate.getDefaultAction().getType() == _DefaultAction.Type.web_url) {
                    url.setText(genericTemplate.getDefaultAction().getUrl());
                    templateImage.setOnClickListener(v -> {
                        if (urlClickListener != null) {
                            urlClickListener.urlButtonClicked(genericTemplate.getDefaultAction().getUrl());
                        }
                    });
                    textContent.setOnClickListener(v -> {
                        if (urlClickListener != null) {
                            urlClickListener.urlButtonClicked(genericTemplate.getDefaultAction().getUrl());
                        }
                    });
                } else if (genericTemplate.getDefaultAction().getType() == _DefaultAction.Type.postback) {
                    templateImage.setOnClickListener(v -> {
                        if (postbackClickListener != null) {
                            postbackClickListener.sendPostbackMessage(genericTemplate.getTitle(), null);
                        }
                    });
                    textContent.setOnClickListener(v -> {
                        if (postbackClickListener != null) {
                            postbackClickListener.sendPostbackMessage(genericTemplate.getTitle(), null);
                        }
                    });
                }
            } else {
                url.setVisibility(View.GONE);
            }

            if(genericTemplate.getButtons()!=null) {
                for (int i = 0; i < genericTemplate.getButtons().size(); i++) {
                    _Button btn = genericTemplate.getButtons().get(i);
                    if (!btn.getTitle().isEmpty()) {
                        buttons[i].setVisibility(View.VISIBLE);
                        buttons[i].setText(btn.getTitle());

                        buttons[i].setOnClickListener(v -> {
                            if (postbackClickListener != null && btn.getType() == _Button.Type.postback)
                                postbackClickListener.sendPostbackMessage(btn.getTitle(), btn.getPayload());
                            else if (urlClickListener != null && btn.getType() == _Button.Type.web_url)
                                urlClickListener.urlButtonClicked(btn.getUrl());
                        });
                    }
                }
            }
        }
    }
}