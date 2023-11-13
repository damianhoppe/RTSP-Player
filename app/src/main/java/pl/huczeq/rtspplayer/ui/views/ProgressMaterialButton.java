package pl.huczeq.rtspplayer.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProgressMaterialButton extends MaterialButton {

    private boolean progressVisible = false;
    private Drawable drawable;
    private Drawable progressDrawable;

    public ProgressMaterialButton(@NonNull Context context) {
        super(context);
        init(null);
    }

    public ProgressMaterialButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ProgressMaterialButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        if(attrs != null) {
            int iconResource = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res-auto", "icon", 0);
            this.drawable = ContextCompat.getDrawable(getContext(), iconResource);
        }

        this.progressDrawable = getProgressBarDrawable(getContext());
        if(this.progressDrawable instanceof Animatable)
            ((Animatable) this.progressDrawable).start();
    }

    @Override
    public void setIcon(@Nullable Drawable drawable) {
        if(drawable == this.drawable)
            return;
        super.setIcon(drawable);
        this.drawable = drawable;
        drawableUpdated();
    }

    protected void drawableUpdated() {
        if(!this.progressVisible)
            updateImage();
    }

    public void setProgressVisible(boolean progressVisible) {
        if(this.progressVisible == progressVisible)
            return;
        this.progressVisible = progressVisible;
        updateImage();
    }

    private void updateImage() {
        super.setIcon((this.progressVisible)? this.progressDrawable : this.drawable);
    }

    private Drawable getProgressBarDrawable(final Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.progressBarStyleSmall, value, false);
        int progressBarStyle = value.data;
        int[] attributes = new int[]{android.R.attr.indeterminateDrawable};
        TypedArray typedArray = context.obtainStyledAttributes(progressBarStyle, attributes);
        Drawable drawable = typedArray.getDrawable(0);
        typedArray.recycle();

        return drawable;
    }

    @Override
    public boolean callOnClick() {
        if(this.progressVisible)
            return false;
        return super.callOnClick();
    }
}
