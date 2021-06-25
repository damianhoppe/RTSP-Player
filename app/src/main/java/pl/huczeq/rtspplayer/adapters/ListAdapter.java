package pl.huczeq.rtspplayer.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import java.util.List;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.interfaces.OnCameraChanged;
import pl.huczeq.rtspplayer.interfaces.OnMenuItemSelected;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.interfaces.OnListItemSelected;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.threads.ImageLoadingThread;

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
        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View view = inflater.inflate(R.layout.item_view_camera, parent, false);

        final TextView textView = view.findViewById(R.id.name);
        final ImageView imageView = view.findViewById(R.id.icon);
        final ImageView imageView2 = view.findViewById(R.id.icon2);
        ImageButton imageButtonMenu = view.findViewById(R.id.itemMenu);

        ContextThemeWrapper ctw = new ContextThemeWrapper(view.getContext(), R.style.AppTheme_Default_PopupMenu);
        final PopupMenu popupMenu = new PopupMenu(ctw, imageButtonMenu);
        Menu menu = popupMenu.getMenu();
        popupMenu.getMenuInflater().inflate(R.menu.menu_camera_item, menu);

        String cameraName = camera.getName();
        setCameraTitle(textView, cameraName);

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
                Log.d(TAG, "Loading " + data.getCamera().getPreviewImg());
                setImage(imageView, imageView2, bitmap);
            }
        };
        final ImageLoadingThread.Data data = new ImageLoadingThread.Data(camera, imageLoadingCallback);
        this.dataManager.loadPreviewImg(data);

        final OnCameraChanged onCameraChanged = new OnCameraChanged() {
            @Override
            public void onCameraPrevImgChanged() {
                Log.d(TAG, "onCameraPrevImgChanged");
                dataManager.loadPreviewImg(data);
            }

            @Override
            public void onCameraUpdated() {
                setCameraTitle(textView, camera.getName());
            }
        };

        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
                camera.addOnCameraChangedListener(onCameraChanged);
            }

            @Override
            public void onViewDetachedFromWindow(final View view) {
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
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView2.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    private void setCameraTitle(TextView tv, String text) {
        TextView textView = new TextView(getContext());
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

    public void setList(List<Camera> list) {
        this.cameras = list;
        notifyDataSetChanged();
    }
}
