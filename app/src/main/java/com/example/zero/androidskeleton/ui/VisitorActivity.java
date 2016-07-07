package com.example.zero.androidskeleton.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.example.zero.androidskeleton.R;

public class VisitorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor);

        setupUiComp();
    }

    private void setupUiComp() {
        WebView webView = (WebView) findViewById(R.id.webview);
        assert webView != null;

        webView.setWebViewClient(new WebViewClient());

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        // FIXME: should not use this
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        webView.loadUrl("http://transee.net:82/visit/apply");
    }
}
