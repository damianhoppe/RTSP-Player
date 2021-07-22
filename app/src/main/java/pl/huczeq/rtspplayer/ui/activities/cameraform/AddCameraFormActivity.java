package pl.huczeq.rtspplayer.ui.activities.cameraform;

import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.objects.CameraPattern;
import pl.huczeq.rtspplayer.interfaces.IOnDataUpdated;
import pl.huczeq.rtspplayer.data.utils.DataState;
import pl.huczeq.rtspplayer.viewmodels.CameraFormViewModel;
import pl.huczeq.rtspplayer.viewmodels.factories.CameraFormViewModelFactory;

public class AddCameraFormActivity extends BaseCameraFormActivity {

    private final String TAG = "AddCameraActivity";

    private CameraFormViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_form);

        setViewsWidgets();

        int cameraId = getIntent().getIntExtra(EXTRA_CAMERA_ID, -1);
        boolean loadDataFromCameraInstance = getIntent().getBooleanExtra(EXTRA_DATA_FROM_CAMERA_INSTANCE, false);

        this.viewModel = ViewModelProviders.of(this, new CameraFormViewModelFactory(DataManager.getInstance(getApplicationContext()), cameraId)).get(CameraFormViewModel.class);

        if(cameraId >= 0 && this.viewModel.getCameraLoadingState() != null) {
            this.viewModel.getCameraLoadingState().observe(this, new Observer<DataState>() {
                @Override
                public void onChanged(DataState dataState) {
                    if(dataState == DataState.LOADED) {
                        if(viewModel.getCameraPattern() != null) {
                            AddCameraFormActivity.this.cameraPatternToLoad = viewModel.getCameraPattern();
                            loadCameraToForm();
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();

        setToolbarTitle(R.string.title_activity_add_camera);
    }

    @Override
    protected String isCameraFormCorrectGetReason() {/*
        Camera camera = dataManager.getCamera(etCameraName.getText().toString());
        if(camera != null) {
            return getResources().getString(R.string.camera_name_is_used);
        }*/
        return super.isCameraFormCorrectGetReason();
    }

    protected void onClickButtonSaveCamera() {
        Log.d(TAG, "SpinModel.selectedItemPosition: " + Integer.toString(spinModel.getSelectedItemPosition()));
        if(!isFormCorrect(true)) {
            return;
        }
        CameraPattern cameraPattern = getCamera();
        Log.d(TAG, "add: " + cameraPattern.getName());
        onStartDataUpdate();
        DataManager.getInstance(getApplicationContext()).addCamera(getCamera(), this);
    }
}
