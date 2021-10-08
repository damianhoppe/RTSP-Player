package pl.huczeq.rtspplayer.ui.activities.settings;

import android.Manifest;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
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
import pl.huczeq.rtspplayer.interfaces.IOnTaskFinished;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.viewmodels.CreateBackupViewModel;
import pl.huczeq.rtspplayer.viewmodels.RestoreBackupViewModel;
import pl.huczeq.rtspplayer.viewmodels.factories.DataManagerViewModelFactory;

public class CreateBackupActivity extends BaseActivity {

    private final static String TAG = "CreateBackupActivity";

    private final static int RESULT_CODE_SELECT_FILE = 1;

    private CheckBox cbCameras, cbSettings;
    private TextView tvNumberOfCameras;
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
                cbCameras.setEnabled(!b);
                cbSettings.setEnabled(!b);
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

        buttonCreateBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBackup();
            }
        });


        tvNumberOfCameras.setText(getString(R.string.number_of_cameras));
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

        boolean camerasBackup = cbCameras.isClickable() && cbCameras.isChecked();
        boolean settingsBackup = cbSettings.isChecked();

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, createNewBackupFileName(camerasBackup, settingsBackup));

        startActivityForResult(intent, RESULT_CODE_SELECT_FILE);
    }

    private String createNewBackupFileName(boolean camerasBackup, boolean settingsBackup) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        String dataExported = "";
        if(camerasBackup)
            dataExported += getString(R.string.cameras);
        if(settingsBackup) {
            if(!dataExported.isEmpty())
                dataExported += ", ";
            dataExported += getString(R.string.settings);
        }
        return date + " - " + dataExported + ".json";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if(requestCode == RESULT_CODE_SELECT_FILE && data != null) {
            boolean camerasBackup = cbCameras.isClickable() && cbCameras.isChecked();
            boolean settingsBackup = cbSettings.isChecked();
            ParcelFileDescriptor fileDescriptor = null;
            try {
                fileDescriptor = getContentResolver().openFileDescriptor(data.getData(), "w");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                showErrorMessage(e.getMessage());
                return;
            }
            FileOutputStream fileOutputStream = new FileOutputStream(fileDescriptor.getFileDescriptor());
            this.viewModel.createBackup(fileOutputStream, camerasBackup, settingsBackup, new IOnTaskFinished() {
                @Override
                public void onComplete() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateBackupActivity.this, R.string.success_created, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showErrorMessage(exception.getMessage());
                        }
                    });
                }
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showErrorMessage(String message) {
        Toast.makeText(CreateBackupActivity.this, CreateBackupActivity.this.getString(R.string.error) + ": " + message, Toast.LENGTH_SHORT).show();
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
}
