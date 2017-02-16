package com.stairway.spotlight.screens.message.emoji;

/**
 * Created by vidhun on 16/02/17.
 */

import android.content.Context;
import android.widget.GridView;

import com.stairway.spotlight.R;
import com.stairway.spotlight.screens.message.emoji.emoji.Emojicon;

/**
 * Created by vidhun on 15/02/17.
 */
public class EmojiconRecentsGridView1 extends EmojiconGridView1 implements EmojiconRecents {
    EmojiAdapter1 mAdapter;

    public EmojiconRecentsGridView1(Context context, Emojicon[] emojicons,
                                    EmojiconRecents recents, final EmojiPicker emojiPicker) {
        super(context, emojicons, recents, emojiPicker);
        EmojiconRecentsManager recents1 = EmojiconRecentsManager
                .getInstance(rootView.getContext());
        mAdapter = new EmojiAdapter1(rootView.getContext(),  recents1);
        mAdapter.setEmojiClickListener(new OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (emojiPicker.onEmojiconClickedListener != null) {
                    emojiPicker.onEmojiconClickedListener.onEmojiconClicked(emojicon);
                }
            }
        });
        GridView gridView = (GridView) rootView.findViewById(R.id.Emoji_GridView);
        gridView.setAdapter(mAdapter);
    }

    @Override
    public void addRecentEmoji(Context context, Emojicon emojicon) {
        EmojiconRecentsManager recents = EmojiconRecentsManager
                .getInstance(context);
        recents.push(emojicon);

        // notify dataset changed
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

}

