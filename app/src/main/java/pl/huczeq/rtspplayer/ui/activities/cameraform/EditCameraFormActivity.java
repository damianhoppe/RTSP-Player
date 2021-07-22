package pl.huczeq.rtspplayer.ui.activities.cameraform;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.objects.CameraPattern;
import pl.huczeq.rtspplayer.interfaces.IOnDataUpdated;
import pl.huczeq.rtspplayer.ui.activities.camerapreviews.BasePreviewCameraActivity;
import pl.huczeq.rtspplayer.data.utils.DataState;
import pl.huczeq.rtspplayer.viewmodels.CameraFormViewModel;
import pl.huczeq.rtspplayer.viewmodels.factories.CameraFormViewModelFactory;

public class EditCameraFormActivity extends BaseCameraFormActivity {

    private final String TAG = "EditCameraActivity";

    protected int cameraId;
    protected CameraFormViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_form);


        cameraId = getIntent().getIntExtra(EXTRA_CAMERA_ID, -1);

        boolean loadDataFromCameraInstance = getIntent().getBooleanExtra(EXTRA_DATA_FROM_CAMERA_INSTANCE, false);
        Log.d(TAG, "Camera id: " + cameraId);
        if(cameraId < 0) {
            finish();
            return;
        }
        setViewsWidgets();
        this.viewModel = ViewModelProviders.of(this, new CameraFormViewModelFactory(DataManager.getInstance(getApplicationContext()), cameraId)).get(CameraFormViewModel.class);

        if(cameraId >= 0 && this.viewModel.getCameraLoadingState() != null) {
            buttonAddCamera.setEnabled(false);
            this.viewModel.getCameraLoadingState().observe(this, new Observer<DataState>() {
                @Override
                public void onChanged(DataState dataState) {
                    if(dataState == DataState.LOADED) {
                        if(viewModel.getCameraPattern() != null) {
                            EditCameraFormActivity.this.cameraPatternToLoad = viewModel.getCameraPattern();
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

        setToolbarTitle(R.string.title_activity_edit_camera);
    }

    @Override
    protected String isCameraFormCorrectGetReason() {/*
        Camera camera = dataManager.getCamera(etCameraName.getText().toString());
        if(camera != null && !camera.getName().equals(this.cameraName)) {
            return getResources().getString(R.string.camera_name_is_used);
        }*/
        return super.isCameraFormCorrectGetReason();
    }

    protected void onClickButtonSaveCamera() {
        if(!isFormCorrect(true)) return;
        CameraPattern cameraPattern = getCamera();
        cameraPattern.setId(this.cameraPatternToLoad.getId());
        onStartDataUpdate();
        DataManager.getInstance(getApplicationContext()).updateCamera(cameraPattern, this);
    }

    @Override
    protected void onClickButtonStartCameraPreview() {
        if(!isUrlFormCorrect()) {
            Toast.makeText(this, getResources().getString(R.string.incorrect_camera_url), Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(BasePreviewCameraActivity.getPreviewCameraIntent(getApplicationContext(), this.etCameraUrl.getText().toString()));
    }
}
