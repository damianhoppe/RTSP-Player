package pl.huczeq.rtspplayer.ui.addeditcamera;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.util.interfaces.IOnListItemSelected;

public class CameraPreviewListAdapter extends RecyclerView.Adapter<CameraPreviewListAdapter.ViewHolder> {

    private Context context;
    private IOnListItemSelected<CameraInstance> onItemSelectedListener;
    private List<CameraInstance> cameraInstances;

    public CameraPreviewListAdapter(Context context, List<CameraInstance> cameraInstances, IOnListItemSelected<CameraInstance> onItemSelectedListener) {
        this.context = context;
        this.cameraInstances = cameraInstances;
        this.onItemSelectedListener = onItemSelectedListener;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_camera_preview, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.getBindingAdapterPosition() < 0)
                    return;
                onItemSelectedListener.onCameraItemSelected(cameraInstances.get(holder.getBindingAdapterPosition()));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        CameraInstance cameraInstance = cameraInstances.get(position);
        holder.tvName.setText(Strings.isNullOrEmpty(cameraInstance.getName())? String.valueOf(position + 1) : cameraInstance.getName());
        holder.tvUrl.setText(cameraInstance.getUrl());
    }

    @Override
    public int getItemCount() {
        return this.cameraInstances.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvName, tvUrl;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            this.tvName = itemView.findViewById(R.id.tvName);
            this.tvUrl = itemView.findViewById(R.id.tvUrl);
        }
    }
}
