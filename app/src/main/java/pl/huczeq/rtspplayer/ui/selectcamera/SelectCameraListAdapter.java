package pl.huczeq.rtspplayer.ui.selectcamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.radiobutton.MaterialRadioButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.repositories.base.CameraThumbnailRepository;
import pl.huczeq.rtspplayer.ui.adapters.base.BaseRecyclerViewAdapter;

public class SelectCameraListAdapter extends BaseRecyclerViewAdapter<Camera, SelectCameraListAdapter.CameraViewHolder> {

    private Context context;
    private CameraThumbnailRepository thumbnailRepository;
    private long selectedId;
    private int lastSelectedIndex = 0;

    public SelectCameraListAdapter(Context context, CameraThumbnailRepository thumbnailRepository) {
        super(new ArrayList<>());
        this.context = context;
        this.thumbnailRepository = thumbnailRepository;
    }

    @NonNull
    @NotNull
    @Override
    public CameraViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_camera_select_radiobutton, parent, false);
        CameraViewHolder cameraViewHolder = new CameraViewHolder(view, context, thumbnailRepository);
        cameraViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cameraViewHolder.getBindingAdapterPosition() == RecyclerView.NO_POSITION)
                    return;
                if(cameraViewHolder.getBindingAdapterPosition() == 0) {
                    selectedId = 0;
                }else {
                    Camera camera = dataSet.get(cameraViewHolder.getBindingAdapterPosition()-1);
                    selectedId = camera.getCameraInstance().getId();
                }
                notifyItemChanged(lastSelectedIndex);
                lastSelectedIndex = cameraViewHolder.getBindingAdapterPosition();
                cameraViewHolder.radioButton.setChecked(true);
            }
        });
        return cameraViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CameraViewHolder holder, int position) {
        boolean selected;
        if(position == 0) {
            holder.ivIcon.setVisibility(View.GONE);
            holder.tvName.setText(R.string.none);
            selected = selectedId <= 0;
        }else {
            Camera camera = this.dataSet.get(position - 1);
            holder.ivIcon.setVisibility(View.VISIBLE);
            Bitmap bitmap = thumbnailRepository.getThumbnail(camera.getCameraInstance().getPreviewImg());
            if (bitmap != null)
                holder.ivIcon.setImageBitmap(bitmap);
            else
                holder.ivIcon.setImageResource(R.mipmap.icon_camera);
            holder.tvName.setText(camera.getCameraInstance().getName());
            selected = selectedId == camera.getCameraInstance().getId();
        }
        holder.radioButton.setChecked(selected);
        if(selected)
            lastSelectedIndex = holder.getBindingAdapterPosition();
    }

    @Override
    public DiffUtil.Callback buildDiffUtilCallback(List<Camera> oldDataSet, List<Camera> newDataSet) {
        return null;
    }

    @Override
    public void updateDataSet(List<Camera> newDataSet) {
        this.dataSet = newDataSet;
        notifyItemRangeChanged(0, this.dataSet.size()+1);
    }

    @Override
    public int getItemCount() {
        if(super.getItemCount() == 0)
            return 0;
        return super.getItemCount() + 1;
    }

    public void setSelected(long selectedId) {
        this.selectedId = selectedId;
        notifyItemRangeChanged(0, this.dataSet.size()+1);
    }

    public long getSelected() {
        return this.selectedId;
    }

    protected static class CameraViewHolder extends RecyclerView.ViewHolder{

        MaterialRadioButton radioButton;
        TextView tvName;
        ImageView ivIcon;
        View root;

        CameraThumbnailRepository thumbnailRepository;
        Context context;

        public CameraViewHolder(@NonNull @NotNull View itemView, Context context, CameraThumbnailRepository thumbnailRepository) {
            super(itemView);
            this.root = itemView;
            this.context = context;
            this.thumbnailRepository = thumbnailRepository;
            this.radioButton = itemView.findViewById(R.id.radioButton);
            this.tvName = itemView.findViewById(R.id.name);
            this.ivIcon = itemView.findViewById(R.id.icon);
        }
    }
}
