package pl.huczeq.rtspplayer.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import pl.huczeq.rtspplayer.ui.activities.mgmt.AddCameraActivity;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.ui.activities.mgmt.BaseCameraActivity;
import pl.huczeq.rtspplayer.ui.activities.mgmt.EditCameraActivity;
import pl.huczeq.rtspplayer.ui.activities.base.BasePreviewcameraActivity;
import pl.huczeq.rtspplayer.ui.activities.settings.SettingsActivity;
import pl.huczeq.rtspplayer.interfaces.OnMenuItemSelected;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.adapters.ListAdapter;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.interfaces.OnListItemSelected;

public class MainActivity extends BaseActivity implements OnListItemSelected, OnMenuItemSelected {

    private final String TAG = "ListCamerasActivity";

    private ListView listView;
    private FloatingActionButton fABaddNewCamera;

    private ListAdapter listAdapter;
    private LinearLayout adContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_cameras);
        setViewsWidgets();

        listAdapter = new ListAdapter(getBaseContext(), dataManager.getCameraList());
        listView.setAdapter(listAdapter);
        
        listAdapter.setOnItemSelectedListener(this);
        listAdapter.setOnMenuItemClick(this);

        adContainer = findViewById(R.id.adContainer);

        enableOnDataChangeListener();
    }
/*
    public void enableBarAds() {
        Log.d(TAG, "enableBarAds");
        if(adView != null) {
            disableBarAds();
        }
        adView = new AdView(this);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId("ca-app-pub-8191844178329148/8504824792");
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adContainer.setVisibility(View.VISIBLE);
        adContainer.addView(adView);
    }
    public void disableBarAds() {
        Log.d(TAG, "disableBarAds");
        if(adView == null) return;
        adContainer.removeView(adView);
        adContainer.setVisibility(View.GONE);
        adView.destroy();
        adView = null;
    }

    public void showFullscreenAd() {
        Log.d(TAG, "showFullscreenAd");
        if(interstitialAd == null) {
            MobileAds.setRequestConfiguration(
                    new RequestConfiguration.Builder().build());
            interstitialAd = new InterstitialAd(this);
            interstitialAd.setAdUnitId("ca-app-pub-8191844178329148/6089047466");
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdOpened() {
                    super.onAdLoaded();
                    settings.setLastAddTime(System.currentTimeMillis());
                }
            });
        }

        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.show();
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!this.dataManager.isDataSaved()) this.dataManager.saveData();
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();

        if(this.toolbar != null) {
            enableToolbarIcon(getDrawable(R.drawable.is_settings), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }
            });
        }
        this.listView = findViewById(R.id.listCameras);
        this.fABaddNewCamera = findViewById(R.id.addCameraFAButton);
        fABaddNewCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddCameraActivity.class));
            }
        });
    }

    @Override
    public void onListItemSelected(Camera camera) {
        Intent intent;
        if(settings.isEnabledNewPlayer()) {
            intent = new Intent(getApplicationContext(), GLPreviewCameraActivity.class);
        }else {
            intent = new Intent(getApplicationContext(), PreviewCameraActivity.class);
        }
        intent.putExtra(BasePreviewcameraActivity.EXTRA_CAMERA_NAME, camera.getName());
        startActivity(intent);
    }

    @Override
    public void onMenuItemSelected(MenuItem menuItem, final Camera camera) {
        Intent intent;
        switch(menuItem.getItemId()) {
            case R.id.watch:
                this.onListItemSelected(camera);
                break;
            case R.id.edit:
                intent = new Intent(getApplicationContext(), EditCameraActivity.class);
                intent.putExtra(BaseCameraActivity.EXTRA_CAMERA_NAME, camera.getName());
                startActivity(intent);
                break;
            case R.id.duplicate:
                intent = new Intent(getApplicationContext(), AddCameraActivity.class);
                intent.putExtra(BaseCameraActivity.EXTRA_CAMERA_NAME, camera.getName());
                startActivity(intent);
                break;
            case R.id.remove:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.dialog_remove_title)
                        .setMessage(R.string.dialog_remove_message)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dataManager.deleteCamera(camera);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
    }

    @Override
    protected void onDataChangedWAA() {
        super.onDataChangedWAA();
        listAdapter.notifyDataSetChanged();
    }
}
