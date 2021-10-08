package pl.huczeq.rtspplayer.adapters;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.adapters.CamerasListAdapter.CameraViewHolder;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.data.objects.CameraInstance;
import pl.huczeq.rtspplayer.data.objects.CameraPattern;
import pl.huczeq.rtspplayer.data.threads.ImageLoadingThread;
import pl.huczeq.rtspplayer.interfaces.IOnListItemSelected;
import pl.huczeq.rtspplayer.interfaces.IOnMenuItemListSelected;
import pl.huczeq.rtspplayer.ui.activities.cameraform.BaseCameraFormActivity;

public class CamerasListAdapter extends RecyclerView.Adapter<CameraViewHolder> {

    private static final String TAG = "CamerasListAdapter";

    private Context context;
    private List<Camera> cameras;
    private List<Camera> newCameras;
    private List<Camera> newCameras2;
    private IOnListItemSelected onItemSelectedListener;
    private IOnMenuItemListSelected onMenuItemClickListener;

    public CamerasListAdapter(Context context, IOnListItemSelected onItemSelectedListener, IOnMenuItemListSelected onMenuItemClickListener) {
        this.context = context;
        this.cameras = new ArrayList<>();
        this.onItemSelectedListener = onItemSelectedListener;
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    @NonNull
    @NotNull
    @Override
    public CameraViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_camera, parent, false);
        return new CameraViewHolder(view, onMenuItemClickListener, onItemSelectedListener);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CameraViewHolder holder, int position) {
        holder.bindTo(cameras.get(position));
    }

    @Override
    public int getItemCount() {
        return cameras.size();
    }

    public void updateList(List<Camera> newList) {
        synchronized (this) {
            if(this.newCameras == null) {
                this.newCameras = newList;
                this.newCameras2 = null;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean end = false;
                        do {
                            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CameraDiffCallback(cameras, newCameras));
                            synchronized (CamerasListAdapter.this) {
                                if (newCameras2 == null) {
                                    new Handler(context.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            CamerasListAdapter.this.cameras = newCameras;
                                            newCameras = null;
                                            diffResult.dispatchUpdatesTo(CamerasListAdapter.this);
                                        }
                                    });
                                    end = true;
                                } else {
                                    newCameras = newCameras2;
                                    newCameras2 = null;
                                    end = false;
                                }
                            }
                        } while(!end);
                    }
                }).start();
            }else {
                this.newCameras2 = newList;
            }
        }
    }

    public static class CameraDiffCallback extends DiffUtil.Callback{

        private List<Camera> oldList;
        private List<Camera> newList;

        public CameraDiffCallback(List<Camera> oldList, List<Camera> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            CameraInstance oldCamera = oldList.get(oldItemPosition).getCameraInstance();
            CameraInstance newCamera = newList.get(newItemPosition).getCameraInstance();
            return oldCamera.getId() == newCamera.getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            CameraInstance oldCameraInstance = oldList.get(oldItemPosition).getCameraInstance();
            CameraInstance newCameraInstance = newList.get(newItemPosition).getCameraInstance();
            CameraPattern oldCameraPattern = oldList.get(oldItemPosition).getCameraPattern();
            CameraPattern newCameraPattern = newList.get(newItemPosition).getCameraPattern();
            return oldCameraInstance.getName().equals(newCameraInstance.getName()) &&
                    oldCameraInstance.getPrevImgLastUpdateTime() == newCameraInstance.getPrevImgLastUpdateTime() &&
                    oldCameraPattern.getNumberOfInstances() == newCameraPattern.getNumberOfInstances();
        }
    }

    protected static class CameraViewHolder extends RecyclerView.ViewHolder {

        final TextView textView;
        final ImageView imageView;
        final ImageView imageView2;
        ImageButton imageButtonMenu;
        ContextThemeWrapper ctw;
        PopupMenu popupMenu;
        Menu menu;
        View itemView;
        IOnMenuItemListSelected IOnMenuItemListSelectedListener;
        IOnListItemSelected IOnListItemSelectedListener;
        public CameraInstance cameraInstance;
        private ImageLoadingThread.Callback imageLoadinThreadCallback = new ImageLoadingThread.Callback() {
            @Override
            public void onImageLoaded(ImageLoadingThread.Data data, Bitmap bitmap) {
                if(data.getCamera().getId() != cameraInstance.getId()) {
                    return;
                }
                if(imageView == null)
                    return;
                if(bitmap == null) {
                    imageView.setImageResource(R.mipmap.icon_camera);
                }else {
                    imageView.setImageBitmap(bitmap);
                }
            }
        };

        public CameraViewHolder(@NonNull @NotNull View itemView, IOnMenuItemListSelected IOnMenuItemListSelectedListener, IOnListItemSelected IOnListItemSelectedListener) {
            super(itemView);
            textView = itemView.findViewById(R.id.name);
            imageView = itemView.findViewById(R.id.icon);
            imageView2 = itemView.findViewById(R.id.icon2);
            imageButtonMenu = itemView.findViewById(R.id.itemMenu);

            ctw = new ContextThemeWrapper(itemView.getContext(), R.style.AppTheme_Default_PopupMenu);
            this.itemView = itemView;
            this.IOnMenuItemListSelectedListener = IOnMenuItemListSelectedListener;
            this.IOnListItemSelectedListener = IOnListItemSelectedListener;
        }

        public void bindTo(Camera camera) {
            this.cameraInstance = camera.getCameraInstance();
            setCameraTitle(textView, cameraInstance.getName());
            popupMenu = new PopupMenu(ctw, imageButtonMenu);
            menu = popupMenu.getMenu();
            if(camera.getCameraPattern() != null && camera.getCameraPattern().getNumberOfInstances() > 1) {
                popupMenu.getMenuInflater().inflate(R.menu.menu_camera_group_item, menu);
            }else {
                popupMenu.getMenuInflater().inflate(R.menu.menu_camera_item, menu);
            }
            imageView.setImageResource(R.mipmap.icon_camera);
            if(cameraInstance.getPreviewImg() != null && !cameraInstance.getPreviewImg().trim().isEmpty())
                DataManager.getInstance(ctw.getApplicationContext()).loadCameraPreviewImg(cameraInstance, this.imageLoadinThreadCallback);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    IOnListItemSelectedListener.onCameraItemSelected(camera);
                }
            });
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    IOnMenuItemListSelectedListener.onMenuItemSelected(menuItem, camera);
                    return false;
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    popupMenu.show();
                    return false;
                }
            });
            imageButtonMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupMenu.show();
                }
            });
        }

        private void setCameraTitle(TextView tv, String text) {
            TextView textView = new TextView(tv.getContext());
            textView.setTypeface(tv.getTypeface());
            textView.setLayoutParams(tv.getLayoutParams());

            textView.setText(text);
            textView.measure(0, 0);

            int maxTextWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            maxTextWidth = maxTextWidth/2-maxTextWidth/5;

            if(textView.getMeasuredWidth() > maxTextWidth) {
                StringBuilder newText = new StringBuilder();
                char[] textA = text.toCharArray();
                int i = 0;
                do {
                    newText.append(textA[i]);
                    textView.setText(newText+".");
                    textView.measure(0, 0);
                    i++;
                }while(textView.getMeasuredWidth() < maxTextWidth);
                newText.deleteCharAt(newText.length()-1);
                text = newText.toString() + "...";
            }
            tv.setText(text);
        }

    }
}
