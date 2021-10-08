package pl.huczeq.rtspplayer.ui.activities.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.viewmodels.CamerasListViewModel;
import pl.huczeq.rtspplayer.viewmodels.factories.DataManagerViewModelFactory;

public class SelectCameraActivity extends BaseActivity {

    private static final String TAG = "SelectCameraActivity";

    public static final int RESULT_CODE = 1;
    public static final String EXTRA_SELECTED_CAMERA_ID = "selectedId";
    public static final String EXTRA_SHOW_NONE_OPTION = "showNone";

    private boolean showNoneOption;
    private int defaultSelectedCamera;

    private ProgressBar progressBar;
    private RadioGroup rgCameras;
    private FloatingActionButton fabConfirm;

    private CamerasListViewModel viewModel;
    private Executor bgExecutor;

    public SelectCameraActivity() {
        this.bgExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_camera);

        setToolbarTitle(R.string.title_activity_select_camera);
        setViewsWidgets();

        loadIntentData(getIntent());

        this.viewModel = ViewModelProviders.of(this, new DataManagerViewModelFactory(DataManager.getInstance(getApplicationContext()))).get(CamerasListViewModel.class);
        this.viewModel.getAllCameras().observe(this, new Observer<List<Camera>>() {
            @Override
            public void onChanged(List<Camera> cameras) {
                if(cameras != null) {
                    viewModel.getAllCameras().removeObserver(this);
                    loadCameras(cameras);
                }
            }
        });

        this.fabConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton radioButton = findViewById(rgCameras.getCheckedRadioButtonId());
                int cameraId = (int) radioButton.getTag();
                Log.d(TAG, "Selected id: " + cameraId);
                //dataManager.getSettings().setStartingCameraId(cameraId);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_SELECTED_CAMERA_ID, cameraId);
                setResult(RESULT_CODE, intent);
                finish();
            }
        });
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();
        this.progressBar = findViewById(R.id.progressBar);
        this.rgCameras = findViewById(R.id.rg_cameras);
        this.fabConfirm = findViewById(R.id.button_confirm);
    }

    private void loadIntentData(Intent intent) {
        this.defaultSelectedCamera = intent.getIntExtra(EXTRA_SELECTED_CAMERA_ID, -1);
        this.showNoneOption = intent.getBooleanExtra(EXTRA_SHOW_NONE_OPTION, true);
    }

    private void loadCameras(List<Camera> cameras) {
        bgExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int checkedViewId = 0;
                List<RadioButton> radioButtons = new ArrayList<>();
                if(showNoneOption) {
                    RadioButton radioButton = createRadioButton();
                    radioButton.setTag(-1);
                    radioButton.setText(R.string.none);
                    radioButtons.add(radioButton);
                    checkedViewId = radioButton.getId();
                }
                for(Camera camera : cameras) {
                    RadioButton radioButton = createRadioButton();
                    radioButton.setTag(camera.getCameraInstance().getId());
                    radioButton.setText(camera.getCameraInstance().getName());
                    radioButtons.add(radioButton);
                    if(camera.getCameraInstance().getId() == defaultSelectedCamera) {
                        checkedViewId = radioButton.getId();
                    }
                }
                final int checkedViewIdFinal = checkedViewId;
                SelectCameraActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rgCameras.removeAllViews();
                        if(radioButtons.size() == 0)
                            return;
                        for(RadioButton radioButton : radioButtons) {
                            rgCameras.addView(radioButton);
                        }
                        Log.d(TAG, "check: " + checkedViewIdFinal);
                        rgCameras.check(checkedViewIdFinal);
                        progressBar.setVisibility(View.GONE);
                        rgCameras.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private RadioButton createRadioButton() {
        int padding = 16;
        RadioButton radioButton = new RadioButton(SelectCameraActivity.this);
        radioButton.setId(View.generateViewId());
        radioButton.setTextSize(24);
        radioButton.setPadding(padding, padding, padding, padding);
        return radioButton;
    }
}
