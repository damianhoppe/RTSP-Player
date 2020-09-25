package pl.huczeq.rtspplayer.adapters;

import android.content.Context;
import android.graphics.Bitmap;
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

public class ListAdapter extends ArrayAdapter<Camera> {

    private List<Camera> cameras;
    private Context context;
    private OnListItemSelected onItemSelectedListener;
    private OnMenuItemSelected onMenuItemClickListener;

    public ListAdapter(@NonNull Context context, List<Camera> cameraList) {
        super(context, -1, cameraList);
        this.context = context;
        this.cameras = cameraList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //TODO Loading images in thread
        final Camera camera = cameras.get(position);
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.view_camera_item, parent, false);
        TextView textView = rowView.findViewById(R.id.name);
        final ImageView imageView = rowView.findViewById(R.id.icon);
        final ImageView imageView2 = rowView.findViewById(R.id.icon2);
        ImageButton imageButton = rowView.findViewById(R.id.itemMenu);
        textView.setText(camera.getName());
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemSelectedListener != null) onItemSelectedListener.onListItemSelected(camera);
            }
        });
        final PopupMenu popupMenu = new PopupMenu(getContext(), imageButton);
        Menu menu = popupMenu.getMenu();
        popupMenu.getMenuInflater().inflate(R.menu.menu_camera_item, menu);
        imageButton.setOnClickListener(new View.OnClickListener() {
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
        DataManager dm = DataManager.getInstance(getContext());
        final Bitmap bitmap = dm.loadPreviewImg(camera);

        final Animation anim_out = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        imageView.startAnimation(anim_out);

        final Animation anim_in  = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        anim_in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                imageView2.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView2.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView2.setImageBitmap(bitmap);
        imageView2.startAnimation(anim_in);

        boolean icon1 = false;
        camera.addOnCameraChangedListener(new OnCameraChanged() {
            @Override
            public void onCameraPrevImgChanged() {

                /*final Animation anim_out = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
                anim_out.setDuration(anim_out.getDuration()/2);
                final Animation anim_in  = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
                anim_in.setDuration(anim_in.getDuration()/2);
                anim_out.setAnimationListener(new Animation.AnimationListener()
                {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation)
                    {
                        imageView.setImageBitmap(DataManager.getInstance(getContext()).loadPreviewImg(camera));
                        anim_in.setAnimationListener(new Animation.AnimationListener() {
                            @Override public void onAnimationStart(Animation animation) {}
                            @Override public void onAnimationRepeat(Animation animation) {}
                            @Override public void onAnimationEnd(Animation animation) {}
                        });
                        imageView.startAnimation(anim_in);
                    }
                });
                imageView.startAnimation(anim_out);*/
                final Bitmap nBitmap = DataManager.getInstance(getContext()).loadPreviewImg(camera);

                final Animation anim_out = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
                anim_out.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        imageView.setImageBitmap(nBitmap);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                imageView.startAnimation(anim_out);

                final Animation anim_in  = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        imageView2.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        imageView2.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView2.setImageBitmap(nBitmap);
                imageView2.startAnimation(anim_in);

                final OnCameraChanged onCameraChanged = this;//TODO czy da sie to zastapic?
                rowView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View view) {
                        //TODO ???: camera.addOnCameraChangedListener(onCameraChanged);
                    }

                    @Override
                    public void onViewDetachedFromWindow(View view) {
                        camera.removeOnCameraChangedListener(onCameraChanged);
                    }
                });
            }
        });
        return rowView;
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
}
