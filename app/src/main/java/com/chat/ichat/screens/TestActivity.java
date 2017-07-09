package com.chat.ichat.screens;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.chat.ichat.R;
import com.chat.ichat.core.Logger;

/**
 * Created by vidhun on 04/07/17.
 */
public class TestActivity extends AppCompatActivity {
    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, TestActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }
}
