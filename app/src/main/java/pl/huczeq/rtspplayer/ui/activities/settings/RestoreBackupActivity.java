package pl.huczeq.rtspplayer.ui.activities.settings;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.adapters.BackupsListAdapter;
import pl.huczeq.rtspplayer.data.DataConverter;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.data.objects.CameraInstance;
import pl.huczeq.rtspplayer.interfaces.IOnTaskFinished;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.ui.activities.cameraform.BaseCameraFormActivity;
import pl.huczeq.rtspplayer.viewmodels.RestoreBackupViewModel;
import pl.huczeq.rtspplayer.viewmodels.factories.DataManagerViewModelFactory;

public class RestoreBackupActivity extends BaseActivity {

    private final static String TAG = "RestoreBackupActivity";

    private final static int RESULT_CODE_SELECT_FILE = 1;

    private CheckBox cbCameras, cbSettings;
    private TextView tvTitle, tvNumberOfCameras, tvCamerasAttention, tvSettingsAttention;
    private Button buttonSelectFile, buttonRestoreBackup;
    private ProgressBar progressBar;

    private RestoreBackupViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_backup);
        setViewsWidgets();

        setToolbarTitle(R.string.title_activity_restore_backup);

        this.viewModel = ViewModelProviders.of(this, new DataManagerViewModelFactory(this.dataManager)).get(RestoreBackupViewModel.class);
        this.viewModel.getIsRunning().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean bool) {
                /*
                tvCamerasAttention.setEnabled(!bool);
                tvSettingsAttention.setEnabled(!bool);
                cbCameras.setClickable(!bool);
                cbSettings.setClickable(!bool);*/
                buttonRestoreBackup.setClickable(!bool);
                progressBar.setVisibility((bool)? View.VISIBLE : View.GONE);
            }
        });
        this.viewModel.getData().observe(this, new Observer<RestoreBackupViewModel.DataModel>() {
            @Override
            public void onChanged(RestoreBackupViewModel.DataModel dataModel) {
                buttonRestoreBackup.setEnabled(dataModel != null);
                if(dataModel == null) {
                    tvNumberOfCameras.setText(getString(R.string.number_of_cameras));
                    cbCameras.setEnabled(false);
                    cbCameras.setEnabled(false);
                }else {
                    tvNumberOfCameras.setText(getString(R.string.number_of_cameras) + ": " + dataModel.numberOfCameras);
                    cbCameras.setEnabled(dataModel.containsCameras);
                    cbSettings.setEnabled(dataModel.containsSettings);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if(requestCode == RESULT_CODE_SELECT_FILE) {
            //TODO
            if(data == null) {

            }else {
                ParcelFileDescriptor fileDescriptor = null;
                try {
                    fileDescriptor = getContentResolver().openFileDescriptor(data.getData(), "r");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    //TODO
                    tvTitle.setText("");
                    return;
                }
                String fileName = data.getData().getLastPathSegment();
                FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
                viewModel.initData(fileName, inputStream, new IOnTaskFinished() {
                    @Override
                    public void onComplete() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvTitle.setText(fileName);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception exception) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvTitle.setText("");
                            }
                        });
                    }
                });
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if(this.viewModel.getIsRunning().getValue() && this.viewModel.getData() != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setPositiveButton(":D", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    RestoreBackupActivity.this.finish();
                }
            });
            alertDialogBuilder.setNegativeButton(":(", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    return;
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();

        cbCameras = findViewById(R.id.cbCameras);
        tvCamerasAttention = findViewById(R.id.tvCamerasAttention);
        cbSettings = findViewById(R.id.cbSettings);
        tvSettingsAttention = findViewById(R.id.tvSettingsAttention);
        tvTitle = findViewById(R.id.tvTitle);
        tvNumberOfCameras = findViewById(R.id.tvNumberOfCameras);
        buttonSelectFile = findViewById(R.id.buttonSelectFile);
        buttonRestoreBackup = findViewById(R.id.buttonRestoreBackup);
        progressBar = findViewById(R.id.progressBar);

        buttonSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFile();
            }
        });

        buttonRestoreBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!viewModel.canRestoreData())
                    return;
                viewModel.restoreData(cbCameras.isChecked() && cbCameras.isChecked(), cbSettings.isClickable() && cbSettings.isChecked(), new IOnTaskFinished() {
                    @Override
                    public void onComplete() {
                        //TODO
                    }

                    @Override
                    public void onError(Exception exception) {
                        //TODO
                    }
                });
            }
        });
    }

    private boolean arePermissionsGranted() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, R.string.permissions_storage_rationale, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    return false;
                }
                String[] p = new String[1];
                p[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
                requestPermissions(p, 1);
                return false;
            }
        }
        return true;
    }

    boolean showSettingsPermissions = false;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean granted = false;
        for(int i = 0; i < grantResults.length; i++) {
            if(Manifest.permission.READ_EXTERNAL_STORAGE.equals(permissions[i]) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted = true;
                break;
            }
        }
        if(granted) {
            //this.viewModel.refreshFileList();
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

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");

        startActivityForResult(intent, RESULT_CODE_SELECT_FILE);
    }
}
