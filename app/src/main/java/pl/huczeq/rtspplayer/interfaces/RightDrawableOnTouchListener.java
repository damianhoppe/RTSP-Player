package pl.huczeq.rtspplayer.interfaces;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public abstract class RightDrawableOnTouchListener implements View.OnTouchListener {

    Drawable drawable;
    private int fuzz = 70;

    public RightDrawableOnTouchListener(EditText view) {
        super();
        initDrawable(view);
    }

    public void initDrawable(EditText view) {
        final Drawable[] drawables = view.getCompoundDrawables();
        if (drawables != null && drawables.length == 4)
            this.drawable = drawables[2];
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        Log.d("TEST", "TEST");
        if(drawable == null) initDrawable((EditText) v);
        if (event.getAction() == MotionEvent.ACTION_DOWN && drawable != null) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();
            final Rect bounds = drawable.getBounds();
            if (x >= (v.getRight() - bounds.width() - fuzz) && x <= (v.getRight() - v.getPaddingRight() + fuzz)
                    && y >= (v.getPaddingTop() - fuzz) && y <= (v.getHeight() - v.getPaddingBottom()) + fuzz) {
                Log.d("TEST", "1");
                return onDrawableTouch(event);
            }
        }
        return false;
    }

    public abstract boolean onDrawableTouch(final MotionEvent event);

}
