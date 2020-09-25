package pl.huczeq.rtspplayer.activities.mgmt;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import pl.huczeq.rtspplayer.activities.main.BaseActivity;
import pl.huczeq.rtspplayer.utils.data.Camera;
import pl.huczeq.rtspplayer.utils.data.Data;
import pl.huczeq.rtspplayer.R;

public class EditCameraActivity extends BaseActivity {

    private final String TAG = "AddCameraActivity";

    EditText etCameraUrl;
    EditText etCameraName;
    Button buttonAddCamera;
    Button buttonStartCameraPreview;
    SurfaceView svCameraPreview;
    TabLayout tabLayout;

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
        buttonAddCamera = findViewById(R.id.buttonAddCamera);
        buttonStartCameraPreview = findViewById(R.id.buttonStartCameraPreview);
        svCameraPreview = findViewById(R.id.svCameraPreview);

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

    private void onClickButtonAddCamera() {
        if(etCameraName.getText().length() == 0)
        {
            Toast.makeText(this, getResources().getString(R.string.incorrect_camera_name), Toast.LENGTH_SHORT).show();
            return;
        }
        if(etCameraUrl.getText().length() == 0)
        {
            Toast.makeText(this, getResources().getString(R.string.incorrect_camera_url), Toast.LENGTH_SHORT).show();
            return;
        }
        Camera nCamera = new Camera(etCameraName.getText().toString());
        nCamera.setUrl(etCameraUrl.getText().toString());
        dataManager.addCamera(nCamera);
    }

    private void onClickButtonStartCameraPreview() {
        if(etCameraUrl.getText().toString().length() < 1) {
            Toast.makeText(this, getResources().getString(R.string.incorrect_camera_url), Toast.LENGTH_SHORT).show();
            return;
        }
       /* VlcVideoLibrary vlcVideoLibrary = new VlcVideoLibrary(this, vlcListener, svCameraPreview);
        vlcVideoLibrary.play(etCameraUrl.getText().toString());*/
    }

    /*VlcListener vlcListener = new VlcListener() {
        @Override
        public void onComplete() {
            Toast.makeText(getApplicationContext(), "onComplete", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError() {
            Toast.makeText(getApplicationContext(), "onError", Toast.LENGTH_SHORT).show();
        }
    };*/
}
