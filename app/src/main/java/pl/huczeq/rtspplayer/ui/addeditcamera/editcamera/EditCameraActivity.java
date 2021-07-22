package pl.huczeq.rtspplayer.ui.addeditcamera.editcamera;

import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.addeditcamera.BaseCameraFormActivity;
import pl.huczeq.rtspplayer.ui.addeditcamera.CameraFormViewModel;

@AndroidEntryPoint
public class EditCameraActivity extends BaseCameraFormActivity {

    @Inject
    public EditCameraViewModel.AssistedFactory viewModelAssistedFactory;

    private CameraFormViewModel viewModel;

    @Override
    protected CameraFormViewModel buildViewModel() {
        long cameraInstanceId = getIntent().getLongExtra(EXTRA_CAMERA_INSTANCE_ID, -1);
        if(cameraInstanceId < 0) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            return null;
        }
        viewModel = new ViewModelProvider(this, new EditCameraViewModel.Factory(this.viewModelAssistedFactory, cameraInstanceId)).get(EditCameraViewModel.class);
        return viewModel;
    }
}
