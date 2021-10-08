package pl.huczeq.rtspplayer.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import pl.huczeq.rtspplayer.adapters.CamerasListAdapter;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.interfaces.IGetCameraCallback;
import pl.huczeq.rtspplayer.ui.activities.cameraform.BaseCameraFormActivity;
import pl.huczeq.rtspplayer.ui.activities.camerapreviews.BasePreviewCameraActivity;
import pl.huczeq.rtspplayer.ui.activities.cameraform.AddCameraFormActivity;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.ui.activities.cameraform.EditCameraFormActivity;
import pl.huczeq.rtspplayer.ui.activities.settings.SettingsActivity;
import pl.huczeq.rtspplayer.interfaces.IOnMenuItemListSelected;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.interfaces.IOnListItemSelected;
import pl.huczeq.rtspplayer.viewmodels.CamerasListViewModel;
import pl.huczeq.rtspplayer.viewmodels.factories.DataManagerViewModelFactory;

public class MainActivity extends BaseActivity implements IOnListItemSelected, IOnMenuItemListSelected {

    private final String TAG = "ListCamerasActivity";
    public static final String KEY_STARTING_CAMERA_ID = "startingCameraId";

    private RecyclerView listView;
    private FloatingActionButton fABaddNewCamera;

    private CamerasListAdapter camerasListAdapter;
    private TextView tvEmptyCameraListMessage;

    private CamerasListViewModel camerasListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_cameras);
        setViewsWidgets();

        camerasListAdapter = new CamerasListAdapter(this,this, this);
        listView.setAdapter(camerasListAdapter);
        listView.setLayoutManager(new LinearLayoutManager(this));

        this.camerasListViewModel = ViewModelProviders.of(this, new DataManagerViewModelFactory(DataManager.getInstance(getApplicationContext()))).get(CamerasListViewModel.class);
        this.camerasListViewModel.getAllCameras().observe(this, new Observer<List<Camera>>() {
            @Override
            public void onChanged(List<Camera> cameras) {
                camerasListAdapter.updateList(cameras);
                tvEmptyCameraListMessage.setVisibility((cameras.size()==0? View.VISIBLE : View.GONE));
            }
        });

        int startingCameraId = getIntent().getIntExtra(KEY_STARTING_CAMERA_ID, -1);
        if(startingCameraId >= 0) {
            camerasListViewModel.getCamera(startingCameraId, new IGetCameraCallback() {
                @Override
                public void onGetCamera(Camera camera) {
                    if(camera != null) {
                        startActivity(BasePreviewCameraActivity.getStartIntent(getApplicationContext(), camera));
                    }
                }
            });
        }
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
                startActivity(new Intent(getApplicationContext(), AddCameraFormActivity.class));
            }
        });

        tvEmptyCameraListMessage = findViewById(R.id.tvEmptyCameraListMessage);
        tvEmptyCameraListMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddCameraFormActivity.class));
            }
        });
    }

    @Override
    public void onCameraItemSelected(Camera camera) {
        startActivity(BasePreviewCameraActivity.getStartIntent(getApplicationContext(), camera));
    }

    @Override
    public void onMenuItemSelected(MenuItem menuItem, Camera camera) {
        Intent intent;
        switch(menuItem.getItemId()) {
            case R.id.watch:
                this.onCameraItemSelected(camera);
                break;
            case R.id.edit:
                intent = new Intent(getApplicationContext(), EditCameraFormActivity.class);
                intent.putExtra(BaseCameraFormActivity.EXTRA_CAMERA_ID, camera.getCameraInstance().getId());
                startActivity(intent);
                break;
            case R.id.duplicate:
                intent = new Intent(getApplicationContext(), AddCameraFormActivity.class);
                intent.putExtra(BaseCameraFormActivity.EXTRA_CAMERA_ID, camera.getCameraInstance().getId());
                startActivity(intent);
                break;
            case R.id.duplicate_camera:
                intent = new Intent(getApplicationContext(), AddCameraFormActivity.class);
                intent.putExtra(BaseCameraFormActivity.EXTRA_CAMERA_ID, camera.getCameraInstance().getId());
                intent.putExtra(BaseCameraFormActivity.EXTRA_DATA_FROM_CAMERA_INSTANCE, true);
                startActivity(intent);
                break;
            case R.id.remove:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                camerasListViewModel.deleteCameras(camera.getCameraPattern());
                            }
                        });
                Log.d(TAG, "Camera info: " + camera.getCameraPattern().getNumberOfInstances());
                if(camera.getCameraPattern().getNumberOfInstances() == 1) {
                    builder.setTitle(R.string.dialog_remove_title)
                            .setMessage(R.string.dialog_remove_message);
                }else {
                    builder.setTitle(R.string.dialog_remove_group_title)
                            .setMessage(R.string.dialog_remove_group_message);
                }
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
    }
}
