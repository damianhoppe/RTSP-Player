package pl.huczeq.rtspplayer.activities.mgmt;

import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import pl.huczeq.rtspplayer.activities.main.BaseActivity;
import pl.huczeq.rtspplayer.utils.data.Camera;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.views.VideoView;

public class AddCameraActivity extends BaseActivity {

    private final String TAG = "AddCameraActivity";

    EditText etCameraUrl;
    EditText etCameraName;
    EditText etUserName;
    EditText etPassword;
    Button buttonAddCamera;
    Button buttonStartCameraPreview;
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camera);

        setViewsWidgets();
    }

    @Override
    protected void setViewsWidgets() {
        etCameraName = findViewById(R.id.etCameraName);
        etCameraUrl = findViewById(R.id.etCameraUrl);
        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);
        buttonAddCamera = findViewById(R.id.buttonAddCamera);
        buttonStartCameraPreview = findViewById(R.id.buttonStartCameraPreview);
        videoView = findViewById(R.id.cameraPreview);

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
        }
        return true;
    }

    private Camera getCamera() {
        Camera nCamera = new Camera(etCameraName.getText().toString());
        nCamera.setUrl(etCameraUrl.getText().toString());
        return nCamera;
    }

    private void onClickButtonAddCamera() {
        if(!isFormCorrect()) return;
        if(etCameraName.getText().length() == 0)
        {
            Toast.makeText(this, getResources().getString(R.string.incorrect_camera_name), Toast.LENGTH_SHORT).show();
            return;
        }
        if(dataManager.getCamera(etCameraName.getText().toString()) != null) {
            Toast.makeText(this, getResources().getString(R.string.camera_name_is_used), Toast.LENGTH_SHORT).show();
            return;
        }
        dataManager.addCamera(getCamera());
        finish();
    }

    private void onClickButtonStartCameraPreview() {
        if(!isFormCorrect()) return;
        videoView.setData(Uri.parse(getCamera().getUrl()));
        videoView.play();
    }
}
