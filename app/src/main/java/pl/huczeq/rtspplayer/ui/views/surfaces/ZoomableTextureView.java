package pl.huczeq.rtspplayer.ui.views.surfaces;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.view.ViewGroup;

import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.ui.views.surfaces.gestures.MoveGestureDetector;

/*
Modified class from https://github.com/devlucem/ZoomableVideo
 */
public class ZoomableTextureView extends TextureView {

    private Matrix mMatrix;

    private ScaleGestureDetector mScaleDetector;

    private MoveGestureDetector mMoveDetector;

    private float mScaleFactor = 1.f;

    private float mFocusX = 0.f;

    private float mFocusY = 0.f;

    private OnClickListener onClickListener;

    public ZoomableTextureView(Context context) {
        super(context);
        init(context);
    }

    public ZoomableTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomableTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ZoomableTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {

        mMatrix = new Matrix();

        // Setup Gesture Detectors
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        mMoveDetector = new MoveGestureDetector(context, new MoveListener());

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
        long deltaTime = motionEvent.getEventTime() - motionEvent.getDownTime();
        if(deltaTime <= Settings.CLICK_MIN_DELTA_TIME && motionEvent.getAction() == MotionEvent.ACTION_UP && this.onClickListener != null) {
            this.onClickListener.onClick(this);
            return true;
        }

        mScaleDetector.onTouchEvent(motionEvent);

        mMoveDetector.onTouchEvent(motionEvent);

        float scaledImageCenterX = (getWidth() * mScaleFactor) / 2;

        float scaledImageCenterY = (getHeight() * mScaleFactor) / 2;

        mMatrix = new Matrix(myMatrix);

        mMatrix.postScale(mScaleFactor, mScaleFactor);

        float dx = mFocusX - scaledImageCenterX;

        float dy = mFocusY - scaledImageCenterY;

        if (dx < ((1 - mScaleFactor) * getWidth())) {

            dx = (1 - mScaleFactor) * getWidth();

            mFocusX = dx + scaledImageCenterX;

        }

        if (dy < ((1 - mScaleFactor) * getHeight())) {

            dy = (1 - mScaleFactor) * getHeight();

            mFocusY = dy + scaledImageCenterY;

        }
        if (dx > 0) {

            dx = 0;

            mFocusX = dx + scaledImageCenterX;
        }

        if (dy > 0) {

            dy = 0;

            mFocusY = dy + scaledImageCenterY;
        }

        mMatrix.postTranslate(dx, dy);

        setTransform(mMatrix);

        setAlpha(1);



        return true; // indicate event was handled

    }

    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            mScaleFactor *= detector.getScaleFactor(); // scale change since previous event

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(1.f, Math.min(mScaleFactor, 4.0f));

            return true;
        }
    }

    public class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {

            PointF d = detector.getFocusDelta();

            mFocusX += d.x;

            mFocusY += d.y;

            return true;
        }
    }

    private int width = 1, height = 1;
    private float scaleX = 1, scaleY = 1;
    private Matrix myMatrix;

    public void onNewVideoSize(int width, int height, int videoWidth, int videoHeight) {
        if(width == 0 || height == 0) return;
        this.width = videoWidth;
        this.height = videoHeight;
        updateScaleValues();
    }

    public void onConfigurationChanged() {
        mMatrix = new Matrix();
        mScaleFactor = 1.f;
        mFocusX = 0.f;
        mFocusY = 0.f;
        updateScaleValues();
    }

    private void updateScaleValues() {
        Log.d("TEST", "W: " + width + ", H: " + height);
        DisplayMetrics dMetrics = getContext().getResources().getDisplayMetrics();
        int pWidth = dMetrics.widthPixels;
        int pHeight = dMetrics.heightPixels;

        float mVideoWidth = width;
        float mVideoHeight = height;
        float screenAspectRatio = pHeight/pWidth;
        float videoAspectRatio = mVideoHeight/mVideoWidth;

        Log.d("TEST", "pW: " + pWidth + ", pH: " + pHeight);

        if(screenAspectRatio > videoAspectRatio) {
            scaleY = (pWidth / mVideoWidth) / (pHeight / mVideoHeight);
            scaleX = 1;
        }else {
            scaleX = (pHeight / mVideoHeight) / (pWidth / mVideoWidth);
            scaleY = 1;
        }

        ViewGroup.LayoutParams params = getLayoutParams();
        myMatrix = new Matrix();
        myMatrix.postScale(scaleX, scaleY, pWidth/2, pHeight/2);
        setTransform(myMatrix);
    }

    public Bitmap getMyBitmap() {
        Matrix m = new Matrix();
        getTransform(m);
        setTransform(new Matrix());
        Bitmap bitmap = this.getBitmap(this.width, this.height);
        setTransform(m);
        return bitmap;
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}