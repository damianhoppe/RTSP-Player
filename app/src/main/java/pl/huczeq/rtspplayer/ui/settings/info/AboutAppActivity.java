package pl.huczeq.rtspplayer.ui.settings.info;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pl.huczeq.rtspplayer.AppConfiguration;
import pl.huczeq.rtspplayer.AppNavigator;
import pl.huczeq.rtspplayer.BuildConfig;
import pl.huczeq.rtspplayer.R;

@AndroidEntryPoint
public class AboutAppActivity extends AppCompatActivity {

    @Inject
    public AppNavigator navigator;

    private TextView tvVersion;
    private TextView tvSiteUrl;
    private TextView tvShowLicense;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);

        tvVersion = findViewById(R.id.tv_version);
        tvSiteUrl = findViewById(R.id.tv_siteUrl);
        tvShowLicense = findViewById(R.id.tv_showLicense);

        tvVersion.setText(AppConfiguration.getFullAppVersion());
        tvSiteUrl.setText(BuildConfig.SITE_URL);
        tvShowLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigator.startLicenseViewerActivity();
            }
        });
    }
}