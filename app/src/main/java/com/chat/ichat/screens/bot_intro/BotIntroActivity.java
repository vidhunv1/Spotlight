package com.chat.ichat.screens.bot_intro;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.chat.ichat.R;
import butterknife.ButterKnife;
/**
 * Created by vidhun on 05/07/17.
 */
public class BotIntroActivity extends AppCompatActivity {
    final static String KEY_USERNAME = "BotIntroActivity.KEY_USERNAME";

    private String username;
    public static Intent callingIntent(Context context, String username) {
        Intent intent = new Intent(context, BotIntroActivity.class);
        intent.putExtra(KEY_USERNAME, username);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_intro);
        ButterKnife.bind(this);

        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_USERNAME))
            return;
        this.username = receivedIntent.getStringExtra(KEY_USERNAME);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

