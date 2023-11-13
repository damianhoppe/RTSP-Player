package pl.huczeq.rtspplayer.ui.settings.exportbackup;

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
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import dagger.hilt.android.AndroidEntryPoint;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.databinding.ActivityCreateBackupBinding;
import pl.huczeq.rtspplayer.ui.BaseActivity;
import pl.huczeq.rtspplayer.ui.settings.importbackup.ImportBackupActivity;

@AndroidEntryPoint
public class ExportBackupActivity extends BaseActivity implements ExportBackupHandler {

    public ExportBackupViewModel viewModel;
    public ActivityCreateBackupBinding binding;
    public ActivityResultLauncher<String> createDocumentAction = registerForActivityResult(new ActivityResultContracts.CreateDocument(MimeTypes.normalizeMimeType("*/*")), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            if(result == null)
                return;
            OutputStream outputStream;
            try {
                outputStream = getContentResolver().openOutputStream(result);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(ExportBackupActivity.this, getString(R.string.something_went_wrong) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.exportDataTo(outputStream);
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_backup);

        this.viewModel = new ViewModelProvider(this).get(ExportBackupViewModel.class);

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_create_backup);
        this.binding.setLifecycleOwner(this);
        this.binding.setViewmodel(this.viewModel);
        this.binding.setHandler(this);

        this.viewModel.getExportingInProgress().observe(this, isInProgress -> {
            if(isInProgress)
                return;
            if(viewModel.isExportCompletedSuccessfully() == null)
                return;
            Toast.makeText(ExportBackupActivity.this,
                    viewModel.isExportCompletedSuccessfully()?
                            R.string.exported_successfully : R.string.failed,
                    Toast.LENGTH_SHORT).show();
            viewModel.resetExportCompletedSuccessfully();
            if(viewModel.isExportCompletedSuccessfully())
                finish();
        });
    }

    @Override
    public void selectBackupDestinationFile() {
        DateFormat df = SimpleDateFormat.getDateTimeInstance();
        String fileName = getString(R.string.app_name) + " - " + df.format(new Date()) + ".backup";
        createDocumentAction.launch(fileName);
    }
}
