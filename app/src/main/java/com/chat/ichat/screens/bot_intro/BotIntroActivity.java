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

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, BotIntroActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_intro);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

