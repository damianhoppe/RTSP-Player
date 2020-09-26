package pl.huczeq.rtspplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import pl.huczeq.rtspplayer.activities.mgmt.AddCameraActivity;
import pl.huczeq.rtspplayer.activities.main.BaseActivity;
import pl.huczeq.rtspplayer.interfaces.OnMenuItemSelected;
import pl.huczeq.rtspplayer.utils.data.Camera;
import pl.huczeq.rtspplayer.adapters.ListAdapter;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.interfaces.OnListItemSelected;

public class MainActivity extends BaseActivity implements OnListItemSelected, OnMenuItemSelected {

    private final String TAG = "ListCamerasActivity";

    private ListView listView;
    private FloatingActionButton fABaddNewCamera;

    private ListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_cameras);
        setViewsWidgets();

        //TODO loading camera in thread
        ArrayList<Camera> cameras = new ArrayList<Camera>();
        cameras.addAll(dataManager.getData().getCameraList());
        Log.d(TAG, String.valueOf(cameras.size()));
        listAdapter = new ListAdapter(getApplicationContext(), dataManager.getCameraList());
        this.listView.setAdapter(listAdapter);
        listAdapter.setOnItemSelectedListener(this);
        listAdapter.setOnMenuItemClick(this);

        enableOnDataChangeListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();

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
        switch(menuItem.getItemId()) {
            case R.id.watch:
                Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
                intent.putExtra(VideoActivity.LOCATION, camera.getUrl());
                startActivity(intent);
                //TODO ???????? delete this item?
                break;
            case R.id.edit:
                //TODO
                break;
            case R.id.delete:
                //TODO
                break;
        }
    }

    @Override
    protected void onDataChangedWAA() {
        super.onDataChangedWAA();
        listAdapter.notifyDataSetChanged();
    }
}
