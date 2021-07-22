package pl.huczeq.rtspplayer.ui.cameralist;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pl.huczeq.rtspplayer.AppNavigator;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.repositories.base.CameraThumbnailRepository;
import pl.huczeq.rtspplayer.databinding.ActivityMainBinding;
import pl.huczeq.rtspplayer.ui.BaseActivity;
import pl.huczeq.rtspplayer.util.interfaces.IOnMenuItemListSelected;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.util.interfaces.IOnListItemSelected;

@AndroidEntryPoint
public class CameraListActivity extends BaseActivity implements IOnListItemSelected<Camera>, IOnMenuItemListSelected<Camera> {

    @Inject
    public AppNavigator navigator;
    @Inject
    public CameraThumbnailRepository cameraThumbnailRepository;

    private CameraListAdapter cameraListAdapter;

    private CameraListViewModel viewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(CameraListViewModel.class);

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        this.binding.setLifecycleOwner(this);
        this.binding.setViewmodel(this.viewModel);
        this.binding.setNavigator(this.navigator);

        this.cameraListAdapter = new CameraListAdapter(this, this, this, cameraThumbnailRepository);

        this.binding.listCameras.setAdapter(this.cameraListAdapter);
        this.binding.listCameras.setLayoutManager(new LinearLayoutManager(this));

        this.viewModel.getAllCameras().observe(this, new Observer<List<Camera>>() {
            @Override
            public void onChanged(List<Camera> cameras) {
                binding.tvEmptyCameraListMessage.setVisibility((cameras.isEmpty())? View.VISIBLE : View.GONE);
                cameraListAdapter.updateDataSet(cameras);
            }
        });
        this.viewModel.getThumbnailsUpdatedList().observe(this, new Observer<LinkedList<String>>() {
            @Override
            public void onChanged(LinkedList<String> thumbnailsUpdated) {
                while(!thumbnailsUpdated.isEmpty()) {
                    String thumbnail = thumbnailsUpdated.pollFirst();
                    cameraListAdapter.onThumbnailUpdated(thumbnail);
                }
            }
        });

        this.binding.listCameras.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int scrollX, int scrollY, int oldXcrollX, int oldScrollY) {
                if (scrollY > oldScrollY && binding.addCameraFAButton.isShown()) {
                    binding.addCameraFAButton.hide();
                } else if (scrollY < oldScrollY && !binding.addCameraFAButton.isShown()) {
                    binding.addCameraFAButton.show();
                }
            }
        });
    }

    @Override
    protected void initToolbar(Toolbar toolbar) {
        super.initToolbar(toolbar);
        enableToolbarIcon(AppCompatResources.getDrawable(this, R.drawable.ic_settings), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigator.startSettingsActivity();
            }
        });
    }

    @Override
    public void onCameraItemSelected(Camera camera) {
        navigator.startPlayerCameraActivity(camera);
    }

    @Override
    public void onMenuItemSelected(MenuItem menuItem, Camera camera) {
        int menuItemId = menuItem.getItemId();
        if(menuItemId == R.id.watch) {
            navigator.startPlayerCameraActivity(camera);
        }else if(menuItemId == R.id.edit) {
            navigator.startEditCameraActivity(camera.getCameraInstance());
        }else if(menuItemId == R.id.duplicate_group) {
            navigator.startDuplicateCameraGroupActivity(camera.getCameraInstance());
        }else if(menuItemId == R.id.duplicate_camera) {
            navigator.startDuplicateCameraActivity(camera.getCameraInstance());
        }else if(menuItemId == R.id.remove) {
            confirmCameraDeleting(camera);
        }
    }

    public void confirmCameraDeleting(Camera camera) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteCamera(camera);
                    }
                });
        if(camera.getCameraPattern().getNumberOfInstances() == 1) {
            builder.setTitle(R.string.dialog_delete_title)
                    .setMessage(R.string.dialog_delete_message);
        }else {
            builder.setNeutralButton(R.string.edit_group, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    navigator.startEditCameraActivity(camera.getCameraInstance());
                }
            });
            String message = getString(R.string.dialog_delete_group_message);
            builder.setTitle(R.string.dialog_delete_group_title)
                    .setMessage(message);
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void deleteCamera(Camera camera) {
        this.viewModel.deleteCamera(camera);
    }
}
