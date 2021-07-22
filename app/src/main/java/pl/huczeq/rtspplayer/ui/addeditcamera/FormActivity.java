package pl.huczeq.rtspplayer.ui.addeditcamera;

import android.os.Bundle;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pl.huczeq.rtspplayer.AppNavigator;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.model.CameraGroup;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.model.urltemplates.Producer;
import pl.huczeq.rtspplayer.databinding.ActivityCameraFormBinding;
import pl.huczeq.rtspplayer.ui.BaseActivity;
import pl.huczeq.rtspplayer.ui.adapters.dropdown.ModelsDropdownAdapter;
import pl.huczeq.rtspplayer.ui.adapters.dropdown.ProducersDropdownAdapter;
import pl.huczeq.rtspplayer.ui.adapters.dropdown.StreamTypesDropdownAdapter;
import pl.huczeq.rtspplayer.util.interfaces.IOnListItemSelected;
import pl.huczeq.rtspplayer.util.states.CompletableState;

@AndroidEntryPoint
public class FormActivity extends BaseActivity implements CameraFormHandler {

    public static final String EXTRA_CAMERA_INSTANCE_ID = "cameraInstanceId";

    @Inject
    public AppNavigator navigator;

    private long backPressedAt;
    protected ActivityCameraFormBinding binding;

    private ProducersDropdownAdapter producersAdapter;
    private ModelsDropdownAdapter modelsAdapter;
    private StreamTypesDropdownAdapter streamTypesAdapter;

    private CompletableState processedSavingStateCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_camera_form);
        this.binding.setLifecycleOwner(this);
        this.binding.setHandler(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
//                boolean isSoftKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
//                if(isSoftKeyboardVisible) {
//                    binding.fabConfirm.setVisibility(View.GONE);
//                    binding.fabStartPreview.setVisibility(View.GONE);
//                }else {
//                    binding.fabConfirm.show();
//                    binding.fabStartPreview.show();
//                }
                return insets;
            }
        });

        initViews();
    }

    public void confirmForm() {
    }

    protected void initViews() {
        this.producersAdapter = new ProducersDropdownAdapter(this);
        binding.ddProducers.setAdapter(producersAdapter);
        this.modelsAdapter = new ModelsDropdownAdapter(this);
        binding.ddModels.setAdapter(modelsAdapter);
        this.streamTypesAdapter = new StreamTypesDropdownAdapter(this);
        binding.ddStreamType.setAdapter(streamTypesAdapter);

        this.binding.ddProducers.setOnItemClickListener((adapterView, view, i, l) -> {
            Producer producer = producersAdapter.getItem(i);
            if(producersAdapter.isNone(producer))
                producer = null;
        });
    }

    Toast toast;

    @Override
    public void onBackPressed() {
        long delta = (System.currentTimeMillis() - backPressedAt);
        if (delta < ViewConfiguration.getKeyRepeatTimeout() * 4L) {
            super.onBackPressed();
            return;
        }
        this.backPressedAt = System.currentTimeMillis();
        if(toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, getResources().getString(R.string.double_back_2_exit_info), Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void toggleAdvancedVisibility() {
        if(this.binding.llAdvanced.getVisibility() == View.VISIBLE) {
            this.binding.llAdvanced.setVisibility(View.GONE);
        }else {
            this.binding.llAdvanced.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void startPreview() {
    }

    public void showPreviewCameraGroupDialog(CameraGroup cameraGroup) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CameraPreviewListAdapter adapter = new CameraPreviewListAdapter(this, cameraGroup.getCameraInstances(), new IOnListItemSelected<CameraInstance>() {
            @Override
            public void onCameraItemSelected(CameraInstance item) {
                startPreview(new Camera(item, cameraGroup.getCameraPattern()));
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setPadding(0,30,0,30);
        builder.setView(recyclerView);
        AlertDialog dialog = builder.create();
        dialog.show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        if(textView != null) {
            textView.setTextSize(16);
            textView.setPadding(0, 0, 0, 0);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) textView.getLayoutParams();
            layoutParams.setMargins(4,4,4,0);
            textView.setLayoutParams(layoutParams);
        }
    }

    public void startPreview(Camera camera) {
        navigator.startPlayerCameraActivity(camera);
    }
}
