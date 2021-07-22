package pl.huczeq.rtspplayer.ui.settings.info;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import pl.huczeq.rtspplayer.R;

public class LicenseViewerActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private WebView webView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_license);

        progressBar = findViewById(R.id.progressBar);
        webView = findViewById(R.id.webView);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
        webView.loadUrl("file:///android_asset/license.txt");
    }
}
