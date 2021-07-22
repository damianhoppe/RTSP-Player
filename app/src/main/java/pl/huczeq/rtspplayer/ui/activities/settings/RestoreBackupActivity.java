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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
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
import pl.huczeq.rtspplayer.data.DataConverter;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.data.objects.CameraInstance;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.viewmodels.RestoreBackupViewModel;
import pl.huczeq.rtspplayer.viewmodels.factories.DataManagerViewModelFactory;

public class RestoreBackupActivity extends BaseActivity {

    private final static String TAG = "RestoreBackupActivity";

    private ListView lvBackups;
    private ProgressBar progressBar;
    private BackupsListAdapter backupsListAdapter;

    private RestoreBackupViewModel viewModel;

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
                if(progressBar.getVisibility() != View.VISIBLE)
                    viewModel.loadBackup(backupsListAdapter.getItem(i), true);
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

        this.viewModel = ViewModelProviders.of(this, new DataManagerViewModelFactory(this.dataManager)).get(RestoreBackupViewModel.class);
        this.viewModel.getFileList().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                if(strings == null) {
                    return;
                }
                backupsListAdapter.setList(strings);
            }
        });
        this.viewModel.getDataIsBeingRestored().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean b) {
                progressBar.setVisibility((b)? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(arePermissionsGranted()) viewModel.refreshFileList();
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();

        lvBackups = findViewById(R.id.lvBackups);
        progressBar = findViewById(R.id.progressBar);
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
            this.viewModel.refreshFileList();
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
}
