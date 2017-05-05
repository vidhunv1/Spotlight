package com.chat.ichat.screens.message.emoji;

/**
 * Created by vidhun on 16/02/17.
 */

import android.content.Context;
import android.widget.GridView;

import com.chat.ichat.R;
import com.chat.ichat.core.Logger;
import com.chat.ichat.screens.message.emoji.emoji_objects.Emojicon;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com)
 * @author 	Ankush Sachdeva (sankush@yahoo.co.in)
 */
public class EmojiconRecentsGridView extends EmojiconGridView implements EmojiconRecents {
    EmojiAdapter mAdapter;

    public EmojiconRecentsGridView(Context context, Emojicon[] emojicons,
                                   EmojiconRecents recents, final EmojiViewHelper emojiViewHelper) {
        super(context, emojicons, recents, emojiViewHelper);
        EmojiconRecentsManager recents1 = EmojiconRecentsManager
                .getInstance(rootView.getContext());
        mAdapter = new EmojiAdapter(rootView.getContext(),  recents1);
        mAdapter.setEmojiClickListener(emojicon -> {
            if (emojiViewHelper.onEmojiconClickedListener != null) {
                emojiViewHelper.onEmojiconClickedListener.onEmojiconClicked(emojicon);
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

