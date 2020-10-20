package pl.huczeq.rtspplayer.ui.activities.settings.info;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import pl.huczeq.rtspplayer.BuildConfig;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.Settings;

public class AppInfoActivity extends AppCompatActivity {

    TextView tvVersion;
    TextView tvSiteUrl;
    TextView tvShowLicense;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);

        tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText(Settings.getFullVersion());

        tvSiteUrl = findViewById(R.id.tv_siteUrl);
        tvSiteUrl.setText(BuildConfig.SITE_URL);

        tvShowLicense = findViewById(R.id.tv_showLicense);
        tvShowLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LicenseActivity.class));
            }
        });
    }
}
