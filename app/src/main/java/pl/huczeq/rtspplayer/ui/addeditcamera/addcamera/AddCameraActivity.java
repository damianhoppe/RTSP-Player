package pl.huczeq.rtspplayer.ui.addeditcamera.addcamera;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.domain.usecases.LoadCameraToPatternUseCase;
import pl.huczeq.rtspplayer.ui.addeditcamera.BaseCameraFormActivity;
import pl.huczeq.rtspplayer.ui.addeditcamera.CameraFormViewModel;

@AndroidEntryPoint
public class AddCameraActivity extends BaseCameraFormActivity {

    private static final String TAG = AddCameraActivity.class.getSimpleName();

    public static final String EXTRA_MODE = "mode";

    public static final int MODE_DUPLICATE_CAMERA_INSTACE = 1;
    public static final int MODE_DUPLICATE_CAMERA_GROUP = 2;

    @Inject
    public AddCameraViewModel.AssistedFactory viewModelAssistedFactory;

    private CameraFormViewModel viewModel;

    @Override
    protected CameraFormViewModel buildViewModel() {
        LoadCameraToPatternUseCase.Params params = null;
        long cameraInstanceId = getIntent().getLongExtra(EXTRA_CAMERA_INSTANCE_ID, -1);
        if(cameraInstanceId > 0) {
            int mode  = getIntent().getIntExtra(EXTRA_MODE, 0);
            if(mode != 0) {
                switch (mode) {
                    case MODE_DUPLICATE_CAMERA_INSTACE:
                        params = new LoadCameraToPatternUseCase.Params(cameraInstanceId, LoadCameraToPatternUseCase.Mode.LOAD_CAMERA_INSTANCE);
                        break;
                    case MODE_DUPLICATE_CAMERA_GROUP:
                        params = new LoadCameraToPatternUseCase.Params(cameraInstanceId, LoadCameraToPatternUseCase.Mode.LOAD_CAMERA_GROUP);
                        break;
                    default:
                        break;
                }
            }
        }
        viewModel = new ViewModelProvider(this, new AddCameraViewModel.Factory(this.viewModelAssistedFactory, params)).get(AddCameraViewModel.class);
        return viewModel;
    }
}
