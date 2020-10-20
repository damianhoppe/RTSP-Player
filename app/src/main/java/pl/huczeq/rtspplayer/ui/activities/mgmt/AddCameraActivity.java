package pl.huczeq.rtspplayer.ui.activities.mgmt;

import android.os.Bundle;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.objects.Camera;

public class AddCameraActivity extends BaseCameraActivity {

    private final String TAG = "AddCameraActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addedit_camera);

        setViewsWidgets();

        String cameraName = getIntent().getStringExtra(EXTRA_CAMERA_NAME);
        Camera camera = dataManager.getCamera(cameraName);
        if(camera != null) {
            loadToForm(camera);
        }
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();

        setToolbarTitle(R.string.title_activity_add_camera);
    }

    @Override
    protected String isCameraFormCorrectGetReason() {
        Camera camera = dataManager.getCamera(etCameraName.getText().toString());
        if(camera != null) {
            return getResources().getString(R.string.camera_name_is_used);
        }
        return super.isCameraFormCorrectGetReason();
    }

    protected void onClickButtonSaveCamera() {
        if(!isFormCorrect(true)) {
            return;
        }
        dataManager.addCamera(getCamera());
        finish();
    }
}
