package pl.huczeq.rtspplayer.activities.mgmt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import pl.huczeq.rtspplayer.activities.PreviewCameraActivity;
import pl.huczeq.rtspplayer.activities.main.BaseActivity;
import pl.huczeq.rtspplayer.utils.data.Camera;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.views.VideoView;

public class EditCameraActivity extends BaseActivity {

    private final String TAG = "AddCameraActivity";

    public static String EXTRA_CAMERA_NAME = "camera_name";

    EditText etCameraUrl;
    EditText etCameraName;
    EditText etUserName;
    EditText etPassword;
    FloatingActionButton buttonAddCamera;
    FloatingActionButton buttonStartCameraPreview;

    String cameraName;
    Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addedit_camera);

        setViewsWidgets();

        cameraName = getIntent().getStringExtra(EXTRA_CAMERA_NAME);
        camera = dataManager.getCamera(cameraName);
        if(camera == null) {
            finish();
            return;
        }

        etCameraName.setText(camera.getName());
        etCameraUrl.setText(camera.getUrl());
        etUserName.setText(camera.getUserName());
        etPassword.setText(camera.getPassword());
    }

    @Override
    protected void setViewsWidgets() {
        etCameraName = findViewById(R.id.etCameraName);
        etCameraUrl = findViewById(R.id.etCameraUrl);
        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);
        buttonAddCamera = findViewById(R.id.saveCameraFAButton);
        buttonStartCameraPreview = findViewById(R.id.startPreviewFAButton);

        buttonAddCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickButtonAddCamera();
            }
        });
        buttonStartCameraPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickButtonStartCameraPreview();
            }
        });
    }

    private boolean isFormCorrect() {
        if(etCameraUrl.getText().length() == 0)
        {
            Toast.makeText(this, getResources().getString(R.string.incorrect_camera_url), Toast.LENGTH_SHORT).show();
            return false;
        }//TODO
        return true;
    }

    private Camera getCamera() {
        Camera nCamera = new Camera(etCameraName.getText().toString());
        nCamera.setUrl(etCameraUrl.getText().toString());
        nCamera.setUserName(etUserName.getText().toString());
        nCamera.setPassword(etPassword.getText().toString());
        return nCamera;
    }

    private void onClickButtonAddCamera() {
        if(!isFormCorrect()) return;
        if(etCameraName.getText().length() == 0)
        {
            Toast.makeText(this, getResources().getString(R.string.incorrect_camera_name), Toast.LENGTH_SHORT).show();
            return;
        }
        if(dataManager.getCamera(etCameraName.getText().toString()) != null && !etCameraName.getText().toString().equalsIgnoreCase(this.cameraName)) {
            Toast.makeText(this, getResources().getString(R.string.camera_name_is_used), Toast.LENGTH_SHORT).show();
            return;
        }
        dataManager.updateCamera(this.cameraName, getCamera());
        finish();
    }

    private void onClickButtonStartCameraPreview() {
        Intent intent = new Intent(getApplicationContext(), PreviewCameraActivity.class);
        intent.putExtra(PreviewCameraActivity.EXTRA_URL, etCameraUrl.getText().toString());
        startActivity(intent);
    }
}
