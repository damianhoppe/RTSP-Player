package pl.huczeq.rtspplayer.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.interfaces.OnCameraChanged;
import pl.huczeq.rtspplayer.interfaces.OnMenuItemSelected;
import pl.huczeq.rtspplayer.utils.data.Camera;
import pl.huczeq.rtspplayer.interfaces.OnListItemSelected;
import pl.huczeq.rtspplayer.utils.data.DataManager;
import pl.huczeq.rtspplayer.utils.data.threads.ImageLoadingThread;

public class ListAdapter extends ArrayAdapter<Camera> {

    private final String TAG = "ListAdapter";

    private List<Camera> cameras;
    private Context context;
    private OnListItemSelected onItemSelectedListener;
    private OnMenuItemSelected onMenuItemClickListener;
    private DataManager dataManager;

    public ListAdapter(@NonNull Context context, List<Camera> cameraList) {
        super(context, -1, cameraList);
        this.context = context;
        this.cameras = cameraList;
        this.dataManager = DataManager.getInstance(context);
    }

    //TODO Loading images in thread
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Camera camera = cameras.get(position);
        final String image = camera.getPreviewImg();
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View view = inflater.inflate(R.layout.view_camera_item, parent, false);

        TextView textView = view.findViewById(R.id.name);
        final ImageView imageView = view.findViewById(R.id.icon);
        final ImageView imageView2 = view.findViewById(R.id.icon2);
        ImageButton imageButtonMenu = view.findViewById(R.id.itemMenu);

        final PopupMenu popupMenu = new PopupMenu(getContext(), imageButtonMenu);
        Menu menu = popupMenu.getMenu();
        popupMenu.getMenuInflater().inflate(R.menu.menu_camera_item, menu);

        textView.setText(camera.getName());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemSelectedListener != null) onItemSelectedListener.onListItemSelected(camera);
            }
        });
        imageButtonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenu.show();
            }
        });
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(onMenuItemClickListener != null) onMenuItemClickListener.onMenuItemSelected(menuItem, camera);
                return false;
            }
        });

        ImageLoadingThread.Callback imageLoadingCallback = new ImageLoadingThread.Callback() {
            @Override
            public void onImageLoaded(ImageLoadingThread.Data data, Bitmap bitmap) {
                setImage(imageView, imageView2, bitmap);
            }
        };
        final ImageLoadingThread.Data data = new ImageLoadingThread.Data(camera, imageLoadingCallback);
        this.dataManager.loadPreviewImg(data);

        final OnCameraChanged onCameraChanged = new OnCameraChanged() {
            @Override
            public void onCameraPrevImgChanged() {
                dataManager.loadPreviewImg(data);
            }
        };
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
                if(camera.getPreviewImg() != image)
                    onCameraChanged.onCameraPrevImgChanged();
                camera.addOnCameraChangedListener(onCameraChanged);
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                camera.removeOnCameraChangedListener(onCameraChanged);
            }
        });
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return super.getDropDownView(position, convertView, parent);
    }

    public void setOnItemSelectedListener(OnListItemSelected listener) {
        this.onItemSelectedListener = listener;
    }

    public void setOnMenuItemClick(OnMenuItemSelected onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    private void setImage(final ImageView imageView, final ImageView imageView2, final Bitmap bitmap) {
        final Animation anim_in = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        anim_in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                imageView2.setVisibility(View.VISIBLE);
                imageView2.setImageBitmap(bitmap);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.setImageBitmap(bitmap);
                imageView2.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        imageView2.startAnimation(anim_in);
    }
}
