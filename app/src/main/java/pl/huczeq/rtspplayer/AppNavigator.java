package pl.huczeq.rtspplayer;

import static pl.huczeq.rtspplayer.ui.addeditcamera.BaseCameraFormActivity.EXTRA_CAMERA_INSTANCE_ID;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.android.scopes.ActivityScoped;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.ui.addeditcamera.addcamera.AddCameraActivity;
import pl.huczeq.rtspplayer.ui.cameralist.CameraListActivity;
import pl.huczeq.rtspplayer.ui.addeditcamera.editcamera.EditCameraActivity;
import pl.huczeq.rtspplayer.ui.player.PlayerCameraActivity;
import pl.huczeq.rtspplayer.ui.selectcamera.SelectCameraActivity;
import pl.huczeq.rtspplayer.ui.settings.SettingsActivity;
import pl.huczeq.rtspplayer.ui.settings.backup.CreateBackupActivity;
import pl.huczeq.rtspplayer.ui.settings.backup.RestoreBackupActivity;
import pl.huczeq.rtspplayer.ui.settings.info.AboutAppActivity;
import pl.huczeq.rtspplayer.ui.settings.info.LicenseViewerActivity;

@ActivityScoped
public class AppNavigator {

    private final Context context;
    private final Settings settings;

    @Inject
    public AppNavigator(@ActivityContext Context context, Settings settings) {
        this.context = context;
        this.settings = settings;
    }

    public Intent buildMainActivityIntent() {
        return new Intent(context, CameraListActivity.class);
    }

    public void startMainActivity() {
        context.startActivity(buildMainActivityIntent());
    }

    public Intent buildPlayerCameraActivityIntent(Camera camera) {
        Intent intent = new Intent(this.context, PlayerCameraActivity.class);
        if(settings.autoEnterPipModeEnabled()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PlayerCameraActivity.putCameraIntoIntent(intent, camera.getCameraInstance().getUrl(), settings.isForceUseRtspTcpEnabled(), camera.getCameraInstance().getId());
        return intent;
    }

    public void startPlayerCameraActivity(Camera camera) {
        context.startActivity(buildPlayerCameraActivityIntent(camera));
    }

    public void startEditCameraActivity(CameraInstance cameraInstance) {
        Intent intent = new Intent(context, EditCameraActivity.class);
        intent.putExtra(EXTRA_CAMERA_INSTANCE_ID, cameraInstance.getId());
        context.startActivity(intent);
    }

    public void startDuplicateCameraGroupActivity(CameraInstance cameraInstance) {
        Intent intent = new Intent(context, AddCameraActivity.class);
        intent.putExtra(EXTRA_CAMERA_INSTANCE_ID, cameraInstance.getId());
        intent.putExtra(AddCameraActivity.EXTRA_MODE, AddCameraActivity.MODE_DUPLICATE_CAMERA_GROUP);
        context.startActivity(intent);
    }

    public void startDuplicateCameraActivity(CameraInstance cameraInstance) {
        Intent intent = new Intent(context, AddCameraActivity.class);
        intent.putExtra(EXTRA_CAMERA_INSTANCE_ID, cameraInstance.getId());
        intent.putExtra(AddCameraActivity.EXTRA_MODE, AddCameraActivity.MODE_DUPLICATE_CAMERA_INSTACE);
        context.startActivity(intent);
    }

    public void startSettingsActivity() {
        context.startActivity(new Intent(context, SettingsActivity.class));
    }

    public void startAddCameraActivity() {
        context.startActivity(new Intent(context, AddCameraActivity.class));
    }

    public Intent buildSelectCameraActivityIntent(long cameraId) {
        Intent intent = new Intent(context, SelectCameraActivity.class);
        intent.putExtra(SelectCameraActivity.EXTRA_SELECTED_CAMERA_ID, cameraId);
        return intent;
    }

    public void startCreateBackupActivity() {
        context.startActivity(new Intent(context, CreateBackupActivity.class));
    }

    public void startRestoreBackupActivity() {
        context.startActivity(new Intent(context, RestoreBackupActivity.class));
    }

    public void startAboutAppActivity() {
        context.startActivity(new Intent(context, AboutAppActivity.class));
    }

    public void startLicenseViewerActivity() {
        context.startActivity(new Intent(context, LicenseViewerActivity.class));
    }

    public void startNewCameraModelRequestForm() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.URL_ADD_MODEL_FORM));
        context.startActivity(intent);
    }
}
