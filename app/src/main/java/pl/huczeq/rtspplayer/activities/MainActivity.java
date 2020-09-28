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
import pl.huczeq.rtspplayer.activities.mgmt.EditCameraActivity;
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

        ArrayList<Camera> cameras = new ArrayList<Camera>();
        cameras.addAll(dataManager.getData().getCameraList());

        listAdapter = new ListAdapter(getApplicationContext(), dataManager.getCameraList());
        listView.setAdapter(listAdapter);
        
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
