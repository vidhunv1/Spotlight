package com.chat.ichat.screens.web_view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.chat.ichat.R;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.core.Logger;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WebViewActivity extends AppCompatActivity{
    private static String KEY_WEB_URL = "WEB_URL";

    @Bind(R.id.web_view)
    WebView webView;

    @Bind(R.id.web_view_url)
    TextView webViewUrl;

    Activity webViewActivity;

    FirebaseAnalytics firebaseAnalytics;

    public static Intent callingIntent(Context context, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(KEY_WEB_URL, url);

        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_WEB_URL))
            return;

        setContentView(R.layout.activity_web_view);

        ButterKnife.bind(this);
        webViewActivity = this;
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

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAnalytics.setCurrentScreen(this, AnalyticsConstants.Event.WEBVIEW_SCREEN, null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        firebaseAnalytics.setCurrentScreen(this, AnalyticsConstants.Event.WEBVIEW_BACK, null);
    }

    @OnClick(R.id.close)
    public void onCloseClicked() {
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.WEBVIEW_CLOSE, null);
        this.finish();
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.contains("http://closewebview")) {
                finish();
            }
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

        public void onCloseWindow(WebView w) {
            Logger.d(this, "On Closed");
            webViewActivity.finish();
        }
    }

}
