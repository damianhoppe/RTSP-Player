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

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

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
        /*if(position >= cameras.size()) {
            AdView adView = new AdView(getContext());
            adView.setAdSize(AdSize.BANNER);
            //adView.setAdUnitId("ca-app-pub-8191844178329148/4118873131");
            adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

            AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
            adView.loadAd(adRequest);
            return adView;
        }*/
        final Camera camera = cameras.get(position);
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final String[] image = {null};

        final View view = inflater.inflate(R.layout.view_camera_item, parent, false);

        final TextView textView = view.findViewById(R.id.name);
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
        view.setOnLongClickListener(new View.OnLongClickListener() {
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
                image[0] = camera.getPreviewImg();
            }
        };
        final ImageLoadingThread.Data data = new ImageLoadingThread.Data(camera, imageLoadingCallback);
        //this.dataManager.loadPreviewImg(data);//TODO ?

        final OnCameraChanged onCameraChanged = new OnCameraChanged() {
            @Override
            public void onCameraPrevImgChanged() {
                dataManager.loadPreviewImg(data);
            }

            @Override
            public void onCameraUpdated() {
                textView.setText(camera.getName());
            }
        };
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
                Log.d(TAG, "onViewAttachedToWindow");
                if(camera.getPreviewImg() != image[0])
                    onCameraChanged.onCameraPrevImgChanged();
                camera.addOnCameraChangedListener(onCameraChanged);
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                Log.d(TAG, "onViewDetachedFromWindow");
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

    @Override
    public int getCount() {
        return this.cameras.size();// + 1;
    }

    private void setImage(final ImageView imageView, final ImageView imageView2, final Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        /*
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
        imageView2.startAnimation(anim_in);*/
    }
}
