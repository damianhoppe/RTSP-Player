package pl.huczeq.rtspplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;

import pl.huczeq.rtspplayer.activities.mgmt.AddCameraActivity;
import pl.huczeq.rtspplayer.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.activities.mgmt.EditCameraActivity;
import pl.huczeq.rtspplayer.interfaces.OnMenuItemSelected;
import pl.huczeq.rtspplayer.utils.Settings;
import pl.huczeq.rtspplayer.utils.data.Camera;
import pl.huczeq.rtspplayer.adapters.ListAdapter;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.interfaces.OnListItemSelected;

public class MainActivity extends BaseActivity implements OnListItemSelected, OnMenuItemSelected {

    private final String TAG = "ListCamerasActivity";

    private ListView listView;
    private FloatingActionButton fABaddNewCamera;

    private ListAdapter listAdapter;

    AdView adView;
    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_cameras);
        setViewsWidgets();

        ArrayList<Camera> cameras = new ArrayList<Camera>();
        cameras.addAll(dataManager.getData().getCameraList());

        listAdapter = new ListAdapter(getApplicationContext(), dataManager.getCameraList());
        listView.setAdapter(listAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                if(listView.getLastVisiblePosition() == listAdapter.getCount() - 1) {
                    
                }
            }
        });
        
        listAdapter.setOnItemSelectedListener(this);
        listAdapter.setOnMenuItemClick(this);

        enableOnDataChangeListener();


        /*MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("F92D93BC6578B5C76F266DBC1A9D8793")).build());*/

        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();//.addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        adView.loadAd(adRequest);

        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder().build());

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-8191844178329148/6089047466");
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdOpened() {
                super.onAdLoaded();
                settings.setLastAddTime(System.currentTimeMillis());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adView.resume();
        if(System.currentTimeMillis() - settings.getLastAddTime() > Settings.adFullscreenDelay) {
            interstitialAd.loadAd(new AdRequest.Builder().build());
            interstitialAd.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        adView.pause();
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();

        if(this.toolbar != null) {
            enableToolbarIcon(getDrawable(R.drawable.is_settings), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), InfoAppActivity.class));
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
        Intent intent = new Intent(getApplicationContext(), PreviewCameraActivity.class);
        intent.putExtra(PreviewCameraActivity.EXTRA_CAMERA_NAME, camera.getName());
        startActivity(intent);
    }

    @Override
    public void onMenuItemSelected(MenuItem menuItem, Camera camera) {
        Intent intent;
        switch(menuItem.getItemId()) {
            case R.id.watch:
                this.onListItemSelected(camera);
                break;
            case R.id.edit:
                intent = new Intent(getApplicationContext(), EditCameraActivity.class);
                intent.putExtra(EditCameraActivity.EXTRA_CAMERA_NAME, camera.getName());
                startActivity(intent);
                break;
            case R.id.move:
                //TODO
                break;
            case R.id.delete:
                dataManager.deleteCamera(camera);
                break;
        }
    }

    @Override
    protected void onDataChangedWAA() {
        super.onDataChangedWAA();
        listAdapter.notifyDataSetChanged();
    }
}
