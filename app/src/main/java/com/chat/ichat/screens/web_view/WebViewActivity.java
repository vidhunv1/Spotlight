package com.chat.ichat.screens.web_view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.chat.ichat.R;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.core.Logger;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WebViewActivity extends BaseActivity {
    private static String KEY_WEB_URL = "WEB_URL";

    @Bind(R.id.web_view)
    WebView webView;

    @Bind(R.id.web_view_url)
    TextView webViewUrl;

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
        String url = receivedIntent.getStringExtra(KEY_WEB_URL);
        webViewUrl.setText(url);
        Logger.d(this, "Loading Url:"+receivedIntent.getStringExtra(KEY_WEB_URL));
        webView.setWebViewClient(new CustomWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.loadUrl(receivedIntent.getStringExtra(KEY_WEB_URL));
    }

    @OnClick(R.id.close)
    public void onCloseClicked() {
        this.finish();
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            webViewUrl.setText(url);
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @SuppressWarnings("unused")
        public void onReceivedSslError(WebView view, SslErrorHandler handler) {
            Log.e("Error", "Exception caught!");
            handler.cancel();
        }
    }

}
