package pl.huczeq.rtspplayer.ui.settings.importbackup;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.exoplayer2.util.MimeTypes;

import java.io.FileNotFoundException;
import java.io.InputStream;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.databinding.ActivityCreateBackupBinding;
import pl.huczeq.rtspplayer.databinding.ActivityRestoreBackupBinding;
import pl.huczeq.rtspplayer.ui.BaseActivity;
import pl.huczeq.rtspplayer.ui.settings.exportbackup.ExportBackupActivity;
import pl.huczeq.rtspplayer.ui.settings.exportbackup.ExportBackupViewModel;
import pl.huczeq.rtspplayer.util.states.CompletableState;

public class ImportBackupActivity extends BaseActivity implements ImportBackupHandler {

    public ImportBackupViewModel viewModel;
    public ActivityRestoreBackupBinding binding;
    public ActivityResultLauncher<String[]> openDocumentAction = registerForActivityResult(new ActivityResultContracts.OpenDocument(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            if(result == null)
                return;
            InputStream outputStream = null;
            try {
                outputStream = getContentResolver().openInputStream(result);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(ImportBackupActivity.this, getString(R.string.something_went_wrong) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.loadBackup(outputStream);
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_backup);

        this.viewModel = new ViewModelProvider(this).get(ImportBackupViewModel.class);

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_restore_backup);
        this.binding.setLifecycleOwner(this);
        this.binding.setViewModel(this.viewModel);
        this.binding.setHandler(this);

        this.viewModel.getImportingBackupState().observe(this, new Observer<CompletableState>() {
            @Override
            public void onChanged(CompletableState importingBackupState) {
                if(importingBackupState == null)
                    return;
                if(!importingBackupState.isCompleted())
                    return;
                Toast.makeText(ImportBackupActivity.this,
                        importingBackupState.isCompletedSuccessfully()?
                                R.string.imported_successfully : R.string.failed,
                        Toast.LENGTH_SHORT).show();
                viewModel.resetImportingBackupStateToIdle();
                if(importingBackupState.isCompletedSuccessfully())
                    finish();
            }
        });
    }

    @Override
    public void selectBackupFile() {
        openDocumentAction.launch(new String[]{MimeTypes.normalizeMimeType("*/*")});
    }

    @Override
    public void restoreBackup() {
        viewModel.restore();
    }
}
