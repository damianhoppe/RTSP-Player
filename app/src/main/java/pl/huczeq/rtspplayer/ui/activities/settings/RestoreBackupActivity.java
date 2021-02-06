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
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.adapters.BackupsListAdapter;
import pl.huczeq.rtspplayer.data.Data;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;

//TODO Checking permissions
public class RestoreBackupActivity extends BaseActivity {

    private final static String TAG = "RestoreBackupActivity";

    ListView lvBackups;
    ProgressBar progressBar;
    BackupsListAdapter backupsListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_backup);
        setViewsWidgets();

        backupsListAdapter = new BackupsListAdapter(this, new ArrayList<String>());
        lvBackups.setAdapter(backupsListAdapter);
        lvBackups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                loadBackup(i);
            }
        });
        lvBackups.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int index, long l) {
                if(index > -1) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RestoreBackupActivity.this)
                            .setTitle(getResources().getString(R.string.dialog_alert_delete_title))
                            .setMessage(getResources().getString(R.string.are_your_sure))
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (!arePermissionsGranted())
                                        return;
                                    File file = new File(settings.getBackupsDir(), backupsListAdapter.getItem(index));
                                    file.delete();
                                    backupsListAdapter.remove(index);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null);
                    alertDialogBuilder.create().show();
                }
                return true;
            }
        });

        setToolbarTitle(R.string.title_activity_restore_backup);

        if(arePermissionsGranted()) loadBackups();
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();

        lvBackups = findViewById(R.id.lvBackups);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadBackups() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<String> backupsList=  new ArrayList<>();
                File dir = settings.getBackupsDir();
                Log.d(TAG, dir.getAbsolutePath());

                if(dir.isDirectory()) {
                    for(File f : dir.listFiles()) {
                        if(f.isFile()) {
                            backupsList.add(f.getName());
                        }
                    }
                }
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        backupsListAdapter.setList(backupsList);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }).start();
    }

    private boolean arePermissionsGranted() {
        /*if(Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            if(!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
                Toast.makeText(this, R.string.permissions_storage_rationale, Toast.LENGTH_SHORT).show();
                return false;
            }
        }else */
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
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
            if(permissions[i] == Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted = true;
                break;
            }
        }
        if(granted) {
            loadBackups();
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

    private void loadBackup(final int index) {
        if(progressBar.getVisibility() != View.VISIBLE) {
            //Toast.makeText(getApplicationContext(), backupsListAdapter.getItem(i), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    File file = new File(settings.getBackupsDir(), backupsListAdapter.getItem(index));
                    if(!file.exists()) {
                        Log.d(TAG, "File not exists!");
                        finish(false);
                        return;
                    }
                    if(!file.canRead()){
                        Log.d(TAG, "Data can not be loaded");
                        finish(false);
                        return;
                    }

                    StringBuilder text = new StringBuilder();
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line;
                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            text.append('\n');
                        }
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        finish(false);
                        return;
                    }
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(text.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        finish(false);
                        return;
                    }

                    ArrayList<Camera> cameras = new ArrayList<>();
                    JSONArray array;
                    try {
                        array = jsonObject.getJSONArray(DataManager.JSONCamerasDataArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        array = new JSONArray();
                    }
                    for (int i = 0; i < array.length(); i++) {
                        Camera camera;
                        try {
                            Log.d(TAG, array.get(i).toString());
                            camera = new Camera(array.getJSONObject(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            continue;
                        }
                        if (camera.getPreviewImg() != null) {
                            File f = new File(settings.getPreviewImagesDir(), camera.getPreviewImg());
                            if (!f.exists()) {
                                camera.setPreviewImg(null);
                            }
                        }
                        cameras.add(camera);
                    }
                    if(!cameras.isEmpty())
                        dataManager.updateCamerasList(cameras);
                    JSONObject jsonSettings = null;
                    try {
                        jsonSettings = jsonObject.getJSONObject(DataManager.JSONSettings);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(jsonSettings != null) {
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = pref.edit();
                        if (!jsonSettings.optString(Settings.KEY_THEME, "").equals("")) editor.putString(Settings.KEY_THEME, jsonSettings.optString(Settings.KEY_THEME, "0"));
                        editor.commit();
                    }
                    dataManager.saveData();
                    finish(true);
                }

                private void finish(final boolean success) {
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if(success)
                                Toast.makeText(getApplicationContext(), R.string.success_restore, Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getApplicationContext(), R.string.error_restore, Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }).start();
        }
    }
}
