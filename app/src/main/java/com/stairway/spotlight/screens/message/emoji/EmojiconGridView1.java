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
import com.stairway.spotlight.screens.message.emoji.emoji.Emojicon;
import com.stairway.spotlight.screens.message.emoji.emoji.People;

import java.util.Arrays;


/**
 * @author Hieu Rocker (rockerhieu@gmail.com)
 * @author 	Ankush Sachdeva (sankush@yahoo.co.in)
 */
public class EmojiconGridView1 {
    public View rootView;
    EmojiPicker emojiPicker;
    EmojiconRecents mRecents;
    Emojicon[] mData;

    public EmojiconGridView1(Context context, Emojicon[] emojicons, EmojiconRecents recents, EmojiPicker emojiconPopup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        emojiPicker = emojiconPopup;
        rootView = inflater.inflate(R.layout.emojicon_grid, null);
        setRecents(recents);
        GridView gridView = (GridView) rootView.findViewById(R.id.Emoji_GridView);
        if (emojicons== null) {
            mData = People.DATA;
        } else {
            Object[] o = (Object[]) emojicons;
            mData = Arrays.asList(o).toArray(new Emojicon[o.length]);
        }
        EmojiAdapter1 mAdapter = new EmojiAdapter1(rootView.getContext(), mData);
        mAdapter.setEmojiClickListener(new OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (emojiPicker.onEmojiconClickedListener != null) {
                    emojiPicker.onEmojiconClickedListener.onEmojiconClicked(emojicon);
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
