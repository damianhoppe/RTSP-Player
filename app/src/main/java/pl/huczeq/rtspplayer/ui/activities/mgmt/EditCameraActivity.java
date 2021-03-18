package pl.huczeq.rtspplayer.ui.activities.mgmt;

import android.os.Bundle;

import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.activities.base.BaseCameraActivity;
import pl.huczeq.rtspplayer.ui.activities.base.BasePreviewcameraActivity;

public class EditCameraActivity extends BaseCameraActivity {

    private final String TAG = "EditCameraActivity";

    String cameraName;
    Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addedit_camera);


        cameraName = getIntent().getStringExtra(EXTRA_CAMERA_NAME);
        if((this.camera = dataManager.getCamera(cameraName)) == null) {
            finish();
            return;
        }
        this.camera = new Camera(this.camera);
        this.cameraToLoad = this.camera;

        setViewsWidgets();

        loadCameraToForm();
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();

        setToolbarTitle(R.string.title_activity_edit_camera);
    }

    @Override
    protected String isCameraFormCorrectGetReason() {
        Camera camera = dataManager.getCamera(etCameraName.getText().toString());
        if(camera != null && !camera.getName().equals(this.cameraName)) {
            return getResources().getString(R.string.camera_name_is_used);
        }
        return super.isCameraFormCorrectGetReason();
    }

    protected void onClickButtonSaveCamera() {
        if(!isFormCorrect(true)) return;
        dataManager.updateCamera(this.cameraName, getCamera());
        finish();
    }
}
