package pl.huczeq.rtspplayer.ui.activities.settings;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import pl.huczeq.rtspplayer.BuildConfig;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;

//TODO Checking permissions
public class CreateBackupActivity extends BaseActivity {

    private final static String TAG = "CreateBackupActivity";
    CheckBox cbCameras, cbSettings;
    TextView tvNumberOfCameras, tvPath;
    Button buttonCreateBackup;
    ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_backup);
        setViewsWidgets();

        setToolbarTitle(R.string.title_activity_create_backup);

        onCompleteCreating();
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
                startCreateBackup();
            }
        });


        updatePathTextView();

        String nOfC = getString(R.string.number_of_cameras) + ": " + dataManager.getCameraList().size();
        tvNumberOfCameras.setText(nOfC);

        if(dataManager.getCameraList().size() > 0)
            cbCameras.setChecked(true);
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

    private void startCreateBackup() {
        if(!cbCameras.isChecked() && !cbSettings.isChecked()) return;
        if(!arePermissionsGranted()) return;
        onStartCreating();
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                if(cbCameras.isChecked()) {
                    JSONArray jsonCameras = new JSONArray();
                    List<Camera> cameras = dataManager.getCameraList();
                    for (Camera c : cameras) {
                        JSONObject obj = new JSONObject();
                        try {
                            if (c.getName() != null && !c.getName().isEmpty())
                                obj.put(Camera.JSONName, c.getName());
                            if (c.getUrl() != null && !c.getUrl().isEmpty())
                                obj.put(Camera.JSONUrl, c.getUrl());
                            if (c.getProducer() != null && !c.getProducer().isEmpty())
                                obj.put(Camera.JSONProducer, c.getProducer());
                            if (c.getModel() != null && !c.getModel().isEmpty())
                                obj.put(Camera.JSONModel, c.getModel());
                            if (c.getUserName() != null && !c.getUserName().isEmpty())
                                obj.put(Camera.JSONUserName, c.getUserName());
                            if (c.getPassword() != null && !c.getPassword().isEmpty())
                                obj.put(Camera.JSONPassword, c.getPassword());
                            if (c.getAddressIp() != null && !c.getAddressIp().isEmpty())
                                obj.put(Camera.JSONAddressIP, c.getAddressIp());
                            if (c.getPort() != null && !c.getPort().isEmpty())
                                obj.put(Camera.JSONPort, c.getPort());
                            if (c.getChannel() != null && !c.getChannel().isEmpty())
                                obj.put(Camera.JSONChannel, c.getChannel());
                            if (c.getStream() != null && !c.getStream().isEmpty())
                                obj.put(Camera.JSONStream, c.getStream());
                            if (c.getServerUrl() != null && !c.getServerUrl().isEmpty())
                                obj.put(Camera.JSONServerUrl, c.getServerUrl());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            break;
                        }
                        jsonCameras.put(obj);
                    }
                    try {
                        json.put(DataManager.JSONCamerasDataArray, jsonCameras);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(cbSettings.isChecked()) {
                    JSONObject jsonSettings = new JSONObject();
                    try {
                        jsonSettings.put(Settings.KEY_THEME, settings.getTheme());
                        jsonSettings.put(Settings.KEY_DEFAULT_ORIENTATION, settings.getDefaultOrientationValue());
                        jsonSettings.put(Settings.KEY_ORIENTATION_MODE, settings.getOrientationModeValue());
                        jsonSettings.put(Settings.KEY_USE_NEW_PLAYER, settings.isEnabledNewPlayer());
                        jsonSettings.put(Settings.KEY_CACHING_BUFFER_SIZE, settings.getCachingBufferSize());
                        jsonSettings.put(Settings.KEY_HARDWARE_ACCELERATION, settings.isEnabledHardwareAcceleration());
                        jsonSettings.put(Settings.KEY_AVCODES_FAST, settings.isEnabledAVCodes());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        json.put(DataManager.JSONSettings, jsonSettings);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String fileName = df.format(new Date()) + " - ";
                String exported = "";
                if(cbCameras.isChecked())
                    exported += getString(R.string.cameras);
                if(cbSettings.isChecked()) {
                    if(!exported.isEmpty())
                        exported += ", ";
                    exported += getString(R.string.settings);
                }
                fileName += exported;
                File file = new File(settings.getBackupsDir(), fileName + ".json");
                if(!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                try {
                    if(!file.exists())
                        file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileOutputStream stream = null;
                try {
                    stream = new FileOutputStream(file);
                    stream.write(json.toString().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, fileName + ".json");
                Log.d(TAG, file.getAbsolutePath());
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.success_created, Toast.LENGTH_SHORT).show();
                        onCompleteCreating();
                    }
                });
            }
        }).start();
    }

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
            startCreateBackup();
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

    private void onCompleteCreating() {
        if(dataManager.getCameraList().size() > 0) {
            cbCameras.setEnabled(true);
        }
        cbSettings.setEnabled(true);
        buttonCreateBackup.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void onStartCreating() {
        cbCameras.setEnabled(false);
        cbSettings.setEnabled(false);
        buttonCreateBackup.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

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
