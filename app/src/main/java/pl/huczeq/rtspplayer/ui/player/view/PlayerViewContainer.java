package pl.huczeq.rtspplayer.ui.player.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

public class PlayerViewContainer extends FrameLayout {

    private boolean matrixUpdated = false;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector gestureDetector;

    private ValueAnimator valueAnimator;
    private View playerView;
    public static final float MIN_SCALE = 0.5f;
    public static final float MAX_SCALE = 5f;
    public static float DEFAULT_SCALE = 1f;
    private boolean scaleLocked = false;
    private float presentScale = DEFAULT_SCALE;
    private float[] matrixValue = new float[16];
    private Matrix matrix;

    float pointScaleOffset = 0.15f;
    float pointScale = 1f;

    public PlayerViewContainer(@NonNull Context context) {
        super(context);
        init();
    }

    public PlayerViewContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayerViewContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PlayerViewContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        DEFAULT_SCALE = MathUtils.clamp(DEFAULT_SCALE, MIN_SCALE, MAX_SCALE);
        setStaticTransformationsEnabled(true);
        this.mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        this.gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                performClick();
                return true;
            }

            @Override
            public boolean onDown(@NonNull MotionEvent e) {
                clearScaleAnimation();
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
                resetTransformationWithAnimation();
                return true;
            }

            @Override
            public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                matrix.postTranslate(-distanceX, -distanceY);
                matrixUpdated = true;
                return true;
            }
        });
        resetTransformation();
        setHapticFeedbackEnabled(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(getChildCount() == 0)
            throw new IllegalArgumentException("PlayerViewContainer should contains view.");
        this.playerView = getChildAt(0);
        applyTransformation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        gestureDetector.onTouchEvent(motionEvent);
        mScaleDetector.onTouchEvent(motionEvent);

        if(matrixUpdated) {
            clampDxDy();
            applyTransformation();
        }
        return true;
    }

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {
        Matrix lMatrix = new Matrix();
        this.matrix.getValues(matrixValue);
        lMatrix.setValues(matrixValue);
        t.getMatrix().set(lMatrix);
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        resetTransformation();
    }

    public void setPlayerView(PlayerSurfaceView playerView) {
        this.playerView = playerView;
        resetTransformation();
    }

    public void resetTransformation() {
        this.matrix = new Matrix();
        this.presentScale = DEFAULT_SCALE;
        this.matrix.setScale(presentScale, presentScale,0,0);
        if(playerView != null)
            applyTransformation();
    }

    public void resetTransformationWithAnimation() {
        animateToScale(1f);
    }

    private void clearScaleAnimation() {
        if(valueAnimator != null)
            valueAnimator.pause();
    }

    public void animateToScale(float scale) {
        clearScaleAnimation();

        valueAnimator = ValueAnimator.ofFloat(presentScale, scale);
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float newScale = (float) animation.getAnimatedValue();
                float scaleFactor = newScale/presentScale;
                presentScale = (float) animation.getAnimatedValue();
                matrix.postScale(scaleFactor, scaleFactor, playerView.getMeasuredWidth()/2f, playerView.getMeasuredHeight()/2f);
                clampDxDy();
                applyTransformation();
            }
        });
        valueAnimator.start();
    }

    private float getFittedTranslation(float mTranslate, float vSize, float cSize, float scale, float offset) {
        float center = (vSize - cSize * scale) / 2f - offset;
        float minimumTranslation = center;
        float maximumTranslation = center;
        if(cSize * scale > vSize) {
            float space = (cSize * scale - vSize) / 2f;
            minimumTranslation -= space;
            maximumTranslation += space;
        }
        if (mTranslate < minimumTranslation)
            return -mTranslate + minimumTranslation;
        if (mTranslate > maximumTranslation)
            return -mTranslate + maximumTranslation;
        return 0f;
    }

    private void clampDxDy() {
        matrix.getValues(matrixValue);
        float dx = matrixValue[Matrix.MTRANS_X];
        float dy = matrixValue[Matrix.MTRANS_Y];

        float topOffset = (getMeasuredHeight() - playerView.getMeasuredHeight()) / 2f;
        float leftOffset = (getMeasuredWidth() - playerView.getMeasuredWidth()) / 2f;

        dx = getFittedTranslation(dx, getMeasuredWidth(), playerView.getMeasuredWidth(), presentScale, leftOffset);
        dy = getFittedTranslation(dy, getMeasuredHeight(), playerView.getMeasuredHeight(), presentScale, topOffset);
        matrix.postTranslate(dx, dy);
    }

    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private boolean changed = false;
        private boolean isInPoint = false;

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {

            this.isInPoint = presentScale >= pointScale-pointScaleOffset && presentScale <= pointScale+pointScaleOffset;
            if(this.isInPoint) {
                performHapticFeedback(HapticFeedbackConstants.GESTURE_START);
            }
            this.changed = false;
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float topSurfaceOffset = (getMeasuredHeight() - playerView.getMeasuredHeight())/2f;
            float leftSurfaceOffset = (getMeasuredWidth() - playerView.getMeasuredWidth())/2f;

            float pointX = detector.getFocusX() - leftSurfaceOffset;
            float pointY = detector.getFocusY() - topSurfaceOffset;

            float previousScale = presentScale;
            float scaleFactor = detector.getScaleFactor();
            if(scaleLocked)
                scaleFactor = 1;

            presentScale *= scaleFactor;
            if(presentScale < MIN_SCALE) {
                presentScale = MIN_SCALE;
                scaleFactor = MIN_SCALE / previousScale;
            }else if(presentScale > MAX_SCALE){
                presentScale = MAX_SCALE;
                scaleFactor = MAX_SCALE / previousScale;
            }
            matrix.postScale(scaleFactor, scaleFactor, pointX, pointY);
            matrixUpdated = true;
            boolean previousInPoint = previousScale >= pointScale-pointScaleOffset && previousScale <= pointScale+pointScaleOffset;
            boolean presentInPoint = presentScale >= pointScale-pointScaleOffset && presentScale <= pointScale+pointScaleOffset;
            if(previousInPoint != presentInPoint) {
                performHapticFeedback(presentInPoint? HapticFeedbackConstants.GESTURE_START : HapticFeedbackConstants.GESTURE_END);
                this.changed = true;
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            if(presentScale >= pointScale-pointScaleOffset && presentScale <= pointScale+pointScaleOffset) {
                animateToScale(pointScale);
//                if(!changed)
                    performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            }
        }
    }

    private void applyTransformation() {
        this.playerView.invalidate();
    }
}
