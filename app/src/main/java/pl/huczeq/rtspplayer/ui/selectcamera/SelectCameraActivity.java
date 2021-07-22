package pl.huczeq.rtspplayer.ui.selectcamera;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.data.repositories.base.CameraThumbnailRepository;
import pl.huczeq.rtspplayer.ui.BaseActivity;
import pl.huczeq.rtspplayer.util.interfaces.IOnListItemSelected;

@AndroidEntryPoint
public class SelectCameraActivity extends BaseActivity {

    private static final String TAG = "SelectCameraActivity";

    public static final int RESULT_CODE = 1;
    public static final String EXTRA_SELECTED_CAMERA_ID = "selectedId";

    @Inject
    public CameraRepository cameraRepository;

    @Inject
    public CameraThumbnailRepository thumbnailRepository;

    private long selectedId;

    private RecyclerView listView;
    private FloatingActionButton fabConfirm;

    private SelectCameraListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_camera);

        this.selectedId = getIntent().getLongExtra(EXTRA_SELECTED_CAMERA_ID, -1);

        this.listView = findViewById(R.id.listView);
        this.listView.setLayoutManager(new LinearLayoutManager(this));
        this.adapter = new SelectCameraListAdapter(this, this.thumbnailRepository);
        this.listView.setAdapter(this.adapter);

        this.fabConfirm = findViewById(R.id.button_confirm);

        this.fabConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_SELECTED_CAMERA_ID, adapter.getSelected());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        this.cameraRepository.getAllCameras().observe(this, new Observer<List<Camera>>() {
            @Override
            public void onChanged(List<Camera> cameras) {
                adapter.updateDataSet(cameras);
            }
        });
        adapter.setSelected(selectedId);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_SELECTED_CAMERA_ID, adapter.getSelected());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.selectedId = savedInstanceState.getLong(EXTRA_SELECTED_CAMERA_ID, -1);
        adapter.setSelected(selectedId);
    }
}
