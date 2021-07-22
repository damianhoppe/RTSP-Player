package pl.huczeq.rtspplayer.viewmodels;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
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
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.data.objects.CameraInstance;
import pl.huczeq.rtspplayer.viewmodels.base.DataManagerViewModel;

public class RestoreBackupViewModel extends DataManagerViewModel {

    private MutableLiveData<List<String>> fileList;
    private MutableLiveData<Boolean> dataIsBeingRestored;

    private Thread threadLoadingFileList;
    private Thread threadDataRestoring;

    public RestoreBackupViewModel(DataManager dataManager) {
        super(dataManager);
        this.fileList = new MutableLiveData<>();
        this.fileList.setValue(null);
        this.dataIsBeingRestored = new MutableLiveData<>();
        this.dataIsBeingRestored.setValue(false);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        interruptLoadingFileList();
    }

    public void refreshFileList() {
        interruptLoadingFileList();
        this.threadLoadingFileList = new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> backups =  new ArrayList<>();
                File dir = dataManager.getSettings().getBackupsDir();

                if(dir.isDirectory()) {
                    for(File f : dir.listFiles()) {
                        if(f.isFile()) {
                            backups.add(f.getName());
                        }
                    }
                }

                fileList.postValue(backups);
            }
        });
        this.threadLoadingFileList.start();
    }

    private void interruptLoadingFileList() {
        if(threadLoadingFileList == null)
            return;
        if(threadLoadingFileList.isAlive())
            threadLoadingFileList.interrupt();
        threadLoadingFileList = null;
        dataIsBeingRestored.setValue(false);
    }

    public void loadBackup(String fileName, boolean clearData) {
        if(threadDataRestoring != null) {
            return;
        }
        threadDataRestoring = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = dataManager.loadBackup(fileName, clearData);
                finish(success);
            }

            private void finish(final boolean success) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(success)
                            Toast.makeText(dataManager.getSettings().getAppContext(), R.string.success_restore, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(dataManager.getSettings().getAppContext(), R.string.error_restore, Toast.LENGTH_SHORT).show();
                        dataIsBeingRestored.postValue(false);
                    }
                });
            }
        });
        dataIsBeingRestored.setValue(true);
        this.threadDataRestoring.start();
    }

    public MutableLiveData<List<String>> getFileList() {
        return fileList;
    }

    public MutableLiveData<Boolean> getDataIsBeingRestored() {
        return dataIsBeingRestored;
    }
}
