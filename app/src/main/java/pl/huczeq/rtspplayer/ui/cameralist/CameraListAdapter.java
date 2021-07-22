package pl.huczeq.rtspplayer.ui.cameralist;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.subjects.PublishSubject;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.repositories.base.CameraThumbnailRepository;
import pl.huczeq.rtspplayer.ui.cameralist.CameraListAdapter.CameraViewHolder;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.ui.adapters.base.BaseRecyclerViewAdapter;
import pl.huczeq.rtspplayer.util.DiffCallback;
import pl.huczeq.rtspplayer.util.interfaces.IOnListItemSelected;
import pl.huczeq.rtspplayer.util.interfaces.IOnMenuItemListSelected;

public class CameraListAdapter extends BaseRecyclerViewAdapter<Camera, CameraViewHolder> {

    private Context context;
    private PublishSubject<String> thumbnailsUpdated;
    private IOnListItemSelected<Camera> onItemSelectedListener;
    private IOnMenuItemListSelected<Camera> onMenuItemClickListener;
    private CameraThumbnailRepository thumbnailRepository;

    public CameraListAdapter(Context context, IOnListItemSelected<Camera> onItemSelectedListener, IOnMenuItemListSelected<Camera> onMenuItemClickListener, CameraThumbnailRepository thumbnailRepository) {
        super(new ArrayList<>());
        this.context = context;
        this.thumbnailRepository = thumbnailRepository;
        this.thumbnailsUpdated = PublishSubject.create();
        this.onItemSelectedListener = onItemSelectedListener;
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    @NonNull
    @NotNull
    @Override
    public CameraViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_camera, parent, false);
        CameraViewHolder cameraViewHolder = new CameraViewHolder(view, context, thumbnailRepository);
        initEvents(cameraViewHolder);
        return cameraViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CameraViewHolder holder, int position) {
        holder.bindTo(dataSet.get(position));
        if(holder.disposableObserver == null) {
            holder.disposableObserver = new DisposableObserver<String>() {
                @Override
                public void onNext(@io.reactivex.rxjava3.annotations.NonNull String thumbnailName) {
                    Camera camera = dataSet.get(holder.getBindingAdapterPosition());
                    if(camera == null)
                        return;
                    if(thumbnailName.equals(camera.getCameraInstance().getPreviewImg()))
                        holder.loadCameraThumbnail(camera.getCameraInstance().getPreviewImg());
                }

                @Override
                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}

                @Override
                public void onComplete() {}
            };
            thumbnailsUpdated.subscribe(holder.disposableObserver);
        }
    }

    @Override
    public void onViewRecycled(@NonNull CameraViewHolder holder) {
        if(!holder.disposableObserver.isDisposed()) {
            holder.disposableObserver.dispose();
            holder.disposableObserver = null;
        }
        super.onViewRecycled(holder);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.thumbnailsUpdated.onComplete();
    }

    private void initEvents(CameraViewHolder cameraViewHolder) {
        cameraViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemSelectedListener.onCameraItemSelected(dataSet.get(cameraViewHolder.getBindingAdapterPosition()));
            }
        });
        cameraViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showPopupMenu(cameraViewHolder);
                return false;
            }
        });
        cameraViewHolder.imageButtonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(cameraViewHolder);
            }
        });
    }

    public void onThumbnailUpdated(String thumbnailName) {
        this.thumbnailsUpdated.onNext(thumbnailName);
    }

    @Override
    public DiffUtil.Callback buildDiffUtilCallback(List<Camera> oldDataSet, List<Camera> newDataSet) {
        return new CameraDiffCallback(oldDataSet, newDataSet);
    }

    public static class CameraDiffCallback extends DiffCallback<Camera> {

        public CameraDiffCallback(List<Camera> oldItems, List<Camera> newItems) {
            super(oldItems, newItems);
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            CameraInstance oldCamera = oldItems.get(oldItemPosition).getCameraInstance();
            CameraInstance newCamera = newItems.get(newItemPosition).getCameraInstance();
            return oldCamera.getId() == newCamera.getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            CameraInstance oldCameraInstance = oldItems.get(oldItemPosition).getCameraInstance();
            CameraInstance newCameraInstance = newItems.get(newItemPosition).getCameraInstance();
            return oldCameraInstance.getId() == newCameraInstance.getId() &&
                    Objects.equals(oldCameraInstance.getName(), newCameraInstance.getName()) &&
                    Objects.equals(oldCameraInstance.getPreviewImg(), newCameraInstance.getPreviewImg());
        }
    }

    private void showPopupMenu(CameraViewHolder holder) {
        if(holder.getBindingAdapterPosition() < 0)
            return;
        Camera camera = dataSet.get(holder.getBindingAdapterPosition());
        showPopupMenu(holder.imageButtonMenu, camera, context)
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        onMenuItemClickListener.onMenuItemSelected(menuItem, camera);
                        return false;
                    }
                });
    }

    private PopupMenu showPopupMenu(View anchor, Camera camera, Context context) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        Menu menu = popupMenu.getMenu();
        if(camera.getCameraPattern() != null && camera.getCameraPattern().getNumberOfInstances() > 1) {
            popupMenu.getMenuInflater().inflate(R.menu.menu_camera_group_item, menu);
        }else {
            popupMenu.getMenuInflater().inflate(R.menu.menu_camera_item, menu);
        }
        popupMenu.show();
        return popupMenu;
    }

    protected static class CameraViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        ImageView imageView;
        ImageButton imageButtonMenu;
        View itemView;

        DisposableObserver<String> disposableObserver;
        CameraThumbnailRepository thumbnailRepository;
        Context context;

        public CameraViewHolder(@NonNull @NotNull View itemView, Context context, CameraThumbnailRepository thumbnailRepository) {
            super(itemView);
            this.context = context;
            this.thumbnailRepository = thumbnailRepository;
            textView = itemView.findViewById(R.id.name);
            imageView = itemView.findViewById(R.id.icon);
            imageButtonMenu = itemView.findViewById(R.id.itemMenu);

            this.itemView = itemView;
        }

        public void bindTo(Camera camera) {
            CameraInstance cameraInstance = camera.getCameraInstance();
            textView.setText(cameraInstance.getName());
            textView.setSelected(true);
            loadCameraThumbnail(cameraInstance.getPreviewImg());
        }

        public void loadCameraThumbnail(String image) {
            if(Strings.isNullOrEmpty(image)) {
                imageView.setImageResource(R.mipmap.icon_camera);
                return;
            }
            Bitmap bitmap = thumbnailRepository.getThumbnail(image);
            if(bitmap != null)
                imageView.setImageBitmap(bitmap);
            else
                imageView.setImageResource(R.mipmap.icon_camera);
        }
    }
}
