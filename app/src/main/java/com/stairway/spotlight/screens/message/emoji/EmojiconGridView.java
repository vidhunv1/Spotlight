package com.stairway.spotlight.screens.message.emoji;

/**
 * Created by vidhun on 15/02/17.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;


import com.stairway.spotlight.R;
import com.stairway.spotlight.screens.message.emoji.emoji_objects.Emojicon;
import com.stairway.spotlight.screens.message.emoji.emoji_objects.People;

import java.util.Arrays;


/**
 * @author Hieu Rocker (rockerhieu@gmail.com)
 * @author 	Ankush Sachdeva (sankush@yahoo.co.in)
 */
public class EmojiconGridView {
    public View rootView;
    EmojiViewHelper emojiViewHelper;
    EmojiconRecents mRecents;
    Emojicon[] mData;

    public EmojiconGridView(Context context, Emojicon[] emojicons, EmojiconRecents recents, EmojiViewHelper emojiconPopup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        emojiViewHelper = emojiconPopup;
        rootView = inflater.inflate(R.layout.emojicon_grid, null);
        setRecents(recents);
        GridView gridView = (GridView) rootView.findViewById(R.id.Emoji_GridView);
        if (emojicons== null) {
            mData = People.DATA;
        } else {
            Object[] o = (Object[]) emojicons;
            mData = Arrays.asList(o).toArray(new Emojicon[o.length]);
        }
        EmojiAdapter mAdapter = new EmojiAdapter(rootView.getContext(), mData);
        mAdapter.setEmojiClickListener(new OnEmojiconClickedListener() {
            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (emojiViewHelper.onEmojiconClickedListener != null) {
                    emojiViewHelper.onEmojiconClickedListener.onEmojiconClicked(emojicon);
                }
                if (mRecents != null) {
                    mRecents.addRecentEmoji(rootView.getContext(), emojicon);
                }
            }
        });
        gridView.setAdapter(mAdapter);
    }

    private void setRecents(EmojiconRecents recents) {
        mRecents = recents;
    }

    public interface OnEmojiconClickedListener {
        void onEmojiconClicked(Emojicon emojicon);
    }

}
