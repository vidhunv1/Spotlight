package com.stairway.spotlight.screens.web_view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.stairway.data.config.Logger;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.di.component.ComponentContainer;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WebViewActivity extends BaseActivity {
    private static String KEY_WEB_URL = "WEB_URL";

    @Bind(R.id.web_view)
    WebView webView;

    public static Intent callingIntent(Context context, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(KEY_WEB_URL, url);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_WEB_URL))
            return;

        setContentView(R.layout.activity_web_view);

        ButterKnife.bind(this);

        Logger.d(this, "Loading Url:"+receivedIntent.getStringExtra(KEY_WEB_URL));
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(receivedIntent.getStringExtra(KEY_WEB_URL));
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
    }
}
