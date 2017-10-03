package in.sdev.android.inshorts.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import in.sdev.android.inshorts.NewsApplication;
import in.sdev.android.inshorts.R;

import static in.sdev.android.inshorts.utilities.NewsPreferences.isEmptyString;
import static in.sdev.android.inshorts.utilities.NewsPreferences.shareDataToOtherApps;


public class WebViewActivity extends AppCompatActivity {
    private final String LOG_TAG = WebViewActivity.class.getSimpleName();
    FirebaseAnalytics mFTracker;
    Boolean mIsVisibleShareMenuItem = true;
    String mTitle;
    private WebView mWebView;
    private String mWebUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        setContentView(R.layout.activity_webview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTitle = "Inshorts";
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        if (null != data && !isEmptyString(data.toString())) {
            mWebUrl = data.toString();
        } else if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                mWebUrl = "https://www.inshorts.com/";
            } else {
                mWebUrl = extras.getString("web_url");
                mTitle = extras.getString("web_title", mTitle);
                if (extras.containsKey("blockShare")) {
                    mIsVisibleShareMenuItem = !(extras.getBoolean("blockShare", false));
                }
            }
        } else {
            mWebUrl = (String) savedInstanceState.getSerializable("web_url");
            mTitle = (String) savedInstanceState.getSerializable("web_title");
        }
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        final Activity activity = this;
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                setTitle("Loading...");
                setProgress(progress * 100);
                if (progress == 100)
                    setTitle(mTitle);
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });
        mWebView.loadUrl(mWebUrl);
        setTitle(mTitle);
        mFTracker = ((NewsApplication) getApplication()).getFirebaseAnalytics();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_webview, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        item.setVisible(mIsVisibleShareMenuItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            shareDataToOtherApps(this, mFTracker, 0, getResources().getString(R.string.format_share_subject_for_link), mTitle, mWebUrl, "", "");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}