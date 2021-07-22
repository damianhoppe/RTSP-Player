package pl.huczeq.rtspplayer.ui.activities.settings;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.DataConverter;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.data.database.CamerasStats;
import pl.huczeq.rtspplayer.data.objects.CameraInstance;
import pl.huczeq.rtspplayer.data.objects.CameraPattern;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.viewmodels.CreateBackupViewModel;
import pl.huczeq.rtspplayer.viewmodels.RestoreBackupViewModel;
import pl.huczeq.rtspplayer.viewmodels.factories.DataManagerViewModelFactory;

public class CreateBackupActivity extends BaseActivity {

    private final static String TAG = "CreateBackupActivity";

    private CheckBox cbCameras, cbSettings;
    private TextView tvNumberOfCameras, tvPath;
    private Button buttonCreateBackup;
    private ProgressBar progressBar;

    private CreateBackupViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_backup);
        setViewsWidgets();

        setToolbarTitle(R.string.title_activity_create_backup);

        this.viewModel = ViewModelProviders.of(this, new DataManagerViewModelFactory(this.dataManager)).get(CreateBackupViewModel.class);
        this.viewModel.getCamerasStats().observe(this, new Observer<CamerasStats>() {
            @Override
            public void onChanged(CamerasStats camerasStats) {
                String nOfC = getString(R.string.number_of_cameras) + ": " + camerasStats.getCameraInstancesCount();
                tvNumberOfCameras.setText(nOfC);
                cbCameras.setEnabled(camerasStats.getCameraInstancesCount() != 0);
                cbCameras.setChecked(camerasStats.getCameraInstancesCount() != 0);
            }
        });
        this.viewModel.getIsBackupCreating().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean b) {
                progressBar.setVisibility(b? View.VISIBLE : View.INVISIBLE);
                buttonCreateBackup.setEnabled(!b);
            }
        });
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();

        cbCameras = findViewById(R.id.cbCameras);
        cbSettings = findViewById(R.id.cbSettings);
        tvNumberOfCameras = findViewById(R.id.tvNumberOfCameras);
        buttonCreateBackup = findViewById(R.id.buttonCreateBackup);
        progressBar = findViewById(R.id.progressBar);
        tvPath = findViewById(R.id.tvPath);

        buttonCreateBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBackup();
            }
        });


        updatePathTextView();
        tvNumberOfCameras.setText(getString(R.string.number_of_cameras) + ": -");
    }

    private boolean arePermissionsGranted() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, R.string.permissions_storage_rationale, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    return false;
                }
                String[] p = new String[1];
                p[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                requestPermissions(p, 1);
                return false;
            }
        }/*else {
            if(!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
                Toast.makeText(this, R.string.permissions_storage_rationale, Toast.LENGTH_SHORT).show();
                return false;
            }
        }*/
        return true;
    }

    private void createBackup() {
        if(!cbCameras.isClickable() || !cbCameras.isChecked() && !cbSettings.isChecked())
            return;
        if(!arePermissionsGranted())
            return;
        this.viewModel.createBackup(this,cbCameras.isClickable() && cbCameras.isChecked(), cbSettings.isChecked());
    }

    /*private void startCreateBackup() {

        if(!cbCameras.isChecked() && !cbSettings.isChecked()) return;
        if(!arePermissionsGranted()) return;
        onStartCreating();
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }*/

    boolean showSettingsPermissions = false;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        updatePathTextView();
        boolean granted = false;
        for(int i = 0; i < grantResults.length; i++) {
            if(Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[i]) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted = true;
                break;
            }
        }
        if(granted) {
            createBackup();
        }else {
            if(!showSettingsPermissions) {
                showSettingsPermissions = true;
            }else {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
            Toast.makeText(this, R.string.permissions_storage_rationale, Toast.LENGTH_SHORT).show();
        }
    }
/*
    private void onCompleteCreating() {/*
        if(dataManager.getCameraList().size() > 0) {
            cbCameras.setEnabled(true);
        }*//*
        cbSettings.setEnabled(true);
        buttonCreateBackup.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void onStartCreating() {
        cbCameras.setEnabled(false);
        cbSettings.setEnabled(false);
        buttonCreateBackup.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }*/

    private void updatePathTextView() {
        String path = settings.getBackupsDir().getPath();
        int i = -1;
        if((i = path.indexOf(File.separator + "Android")) > -1) {
            path = path.substring(i);
        }else if((i = path.indexOf(File.separator + "RTSP")) > -1) {
            path = path.substring(i);
        }
        tvPath.setText(path);
    }
}
