package pl.huczeq.rtspplayer.ui.player.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

public class PlayerViewContainer extends FrameLayout {

    private boolean matrixUpdated = false;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector gestureDetector;

    private ValueAnimator scaleAnimator;
    private ValueAnimator translationAnimator;
    private boolean hasScalingGestureOccurredDuringTouchInteraction = false;
    private boolean swipeGestureAvailable = true;
    int swipeDirection = 0;// 0 -NONE, <0 - LEFT, >0 - RIGHT
    private boolean isClampingEnabled = true;

    float swipeThreshold = 300;
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

    private static final float MIN_EDGE_RESISTANCE = 0.15f;

    private float edgeDragX = 0f;

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
                clearTranslationAnimation();
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
                if(e.getActionMasked() == MotionEvent.ACTION_DOWN)
                    return false;

                if(mScaleDetector.isInProgress())
                    return false;

                if(e.getActionMasked() == MotionEvent.ACTION_UP) {
                    isClampingEnabled = true;
                    resetScaleWithAnimation();
                    return true;
                }

                return false;
            }

            private float easeOutCubic(float t) {
                return 1f - (float) Math.pow(1f - t, 3);
            }

            private float getEdgeResistance(float drag) {
                float t = Math.min(1f, drag / swipeThreshold);

                float eased = 1f - easeOutCubic(t);

                return MIN_EDGE_RESISTANCE + (1f - MIN_EDGE_RESISTANCE) * eased;
            }

            @Override
            public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                matrix.getValues(matrixValue);

                float currentX = matrixValue[Matrix.MTRANS_X];
                float newX = currentX - distanceX;
                float overflow = -getTranslationXOverflow(newX);

                float effectiveDistanceX = distanceX;
                if(!isClampingEnabled && isTranslationXAtLimit() && overflow != 0) {
                    edgeDragX += distanceX;
                    float resistance = getEdgeResistance(Math.abs(edgeDragX));
                    effectiveDistanceX = distanceX * resistance;
                }else {
                    edgeDragX = 0f;
                }

                matrix.postTranslate(-effectiveDistanceX, -distanceY);
                matrixUpdated = true;

                if(overflow == 0 && !isTranslationXAtLimit()) {
                    isClampingEnabled = true;
                    edgeDragX = 0.f;
                }

                if(!swipeGestureAvailable) {
                    return false;
                }

                int currentSwipeDirection;

                if(mScaleDetector.isInProgress()) {
                    currentSwipeDirection = 0;
                    hasScalingGestureOccurredDuringTouchInteraction = true;
                }else {
                    currentSwipeDirection = (edgeDragX > swipeThreshold * 0.5f) ?
                            1
                            : (edgeDragX < -(swipeThreshold * 0.5f)) ?
                                -1 : 0;
                }

                if(currentSwipeDirection != swipeDirection) {
                    if(currentSwipeDirection != 0)
                        performHapticFeedback(HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE);
                    else
                        performHapticFeedback(HapticFeedbackConstants.GESTURE_THRESHOLD_DEACTIVATE);
                    swipeDirection = currentSwipeDirection;
                }

                return false;
            }
        });
        reset();
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
        if(motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN && motionEvent.getPointerCount() == 1) {
            if(isTranslationXAtLimit()) {
                isClampingEnabled = false;
            }
        }
        gestureDetector.onTouchEvent(motionEvent);
        mScaleDetector.onTouchEvent(motionEvent);

        if(motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
            edgeDragX = 0.0f;
            matrix.getValues(matrixValue);
            float x = calculateClampedXTranslation(matrixValue[Matrix.MTRANS_X]);
            if(swipeDirection != 0 && !hasScalingGestureOccurredDuringTouchInteraction) {
                onSwipeListener.onSwipe(swipeDirection < 0 ? -1 : 1);
                swipeDirection = 0;
                matrixUpdated = false;
            }
            if(x != 0) {
                animateToX(matrixValue[Matrix.MTRANS_X] + x);
                matrixUpdated = true;
                isClampingEnabled = true;
            }
            hasScalingGestureOccurredDuringTouchInteraction = false;
        }

        if(matrixUpdated) {
            clampMatrixTranslation(isClampingEnabled || !swipeGestureAvailable, true);
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
        swipeThreshold = 300 * getResources().getDisplayMetrics().density;
        reset();
    }

    private boolean isTranslationXAtLimit() {
        return isTranslationXAtLimit(5f);
    }

    private boolean isTranslationXAtLimit(float tolerance) {
        matrix.getValues(matrixValue);

        float translationX = matrixValue[Matrix.MTRANS_X];

        float leftOffset = (getMeasuredWidth() - playerView.getMeasuredWidth()) / 2f;

        float center = (getMeasuredWidth()
                - playerView.getMeasuredWidth() * presentScale) / 2f - leftOffset;

        float min = center;
        float max = center;

        if(playerView.getMeasuredWidth() * presentScale > getMeasuredWidth()) {
            float space = (playerView.getMeasuredWidth() * presentScale
                    - getMeasuredWidth()) / 2f;

            min -= space;
            max += space;
        }

        return translationX <= min + tolerance || translationX >= max - tolerance;
    }

    private float getTranslationXOverflow(float translationX) {
        float leftOffset = (getMeasuredWidth() - playerView.getMeasuredWidth()) / 2f;

        float center = (getMeasuredWidth()
                - playerView.getMeasuredWidth() * presentScale) / 2f - leftOffset;

        float min = center;
        float max = center;

        if(playerView.getMeasuredWidth() * presentScale > getMeasuredWidth()) {
            float space = (playerView.getMeasuredWidth() * presentScale
                    - getMeasuredWidth()) / 2f;

            min -= space;
            max += space;
        }

        if(translationX < min) {
            return translationX - min;
        }

        if(translationX > max) {
            return translationX - max;
        }

        return 0f;
    }

    public interface OnSwipeListener {
        /** @param direction 1 for next (swipe left), -1 for previous (swipe right) */
        void onSwipe(int direction);
    }

    private OnSwipeListener onSwipeListener;

    public void setOnSwipeListener(OnSwipeListener listener) {
        this.onSwipeListener = listener;
    }

    public void setPlayerView(PlayerSurfaceView playerView) {
        this.playerView = playerView;
        reset();
    }

    public void reset() {
        this.matrix = new Matrix();
        this.presentScale = DEFAULT_SCALE;
        this.matrix.setScale(presentScale, presentScale,0,0);

        this.edgeDragX = 0;
        this.hasScalingGestureOccurredDuringTouchInteraction = false;
        this.swipeDirection = 0;
        this.isClampingEnabled = true;

        clearScaleAnimation();
        clearTranslationAnimation();

        if(playerView != null)
            applyTransformation();
    }

    public void resetScaleWithAnimation() {
        animateToScale(1f);
    }

    public void resetTranslationWithAnimation() {
        matrix.getValues(matrixValue);
        float x = calculateClampedXTranslation(matrixValue[Matrix.MTRANS_X]);
        animateToX(matrixValue[Matrix.MTRANS_X] + x);
    }

    private void clearScaleAnimation() {
        if(scaleAnimator != null)
            scaleAnimator.pause();
    }

    public void animateToScale(float scale) {
        clearScaleAnimation();

        scaleAnimator = ValueAnimator.ofFloat(presentScale, scale);
        scaleAnimator.setDuration(200);
        scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float newScale = (float) animation.getAnimatedValue();
                float scaleFactor = newScale/presentScale;
                presentScale = (float) animation.getAnimatedValue();
                matrix.postScale(scaleFactor, scaleFactor, playerView.getMeasuredWidth()/2f, playerView.getMeasuredHeight()/2f);
                clampMatrixTranslation(isClampingEnabled || !swipeGestureAvailable, true);
                applyTransformation();
            }
        });
        scaleAnimator.start();
    }

    private void clearTranslationAnimation() {
        if(translationAnimator != null)
            translationAnimator.pause();
    }

    private void animateToX(float x) {
        clearTranslationAnimation();

        translationAnimator = ValueAnimator.ofFloat(matrixValue[Matrix.MTRANS_X], x);
        translationAnimator.setDuration(200);
        translationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float newX = (float) animation.getAnimatedValue();

                matrix.getValues(matrixValue);

                float currentX = matrixValue[Matrix.MTRANS_X];
                matrix.postTranslate(newX - currentX, 0);
                clampMatrixTranslation(false, true);

                applyTransformation();
            }
        });
        translationAnimator.start();
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
        if(mTranslate < minimumTranslation)
            return -mTranslate + minimumTranslation;
        if(mTranslate > maximumTranslation)
            return -mTranslate + maximumTranslation;
        return 0f;
    }

    private float calculateClampedXTranslation(float presentXTranslation) {
        float leftOffset = (getMeasuredWidth() - playerView.getMeasuredWidth()) / 2f;
        return getFittedTranslation(presentXTranslation, getMeasuredWidth(), playerView.getMeasuredWidth(), presentScale, leftOffset);
    }

    private float calculateClampedYTranslation(float presentYTranslation) {
        float topOffset = (getMeasuredHeight() - playerView.getMeasuredHeight()) / 2f;
        return getFittedTranslation(presentYTranslation, getMeasuredHeight(), playerView.getMeasuredHeight(), presentScale, topOffset);
    }

    private void clampMatrixTranslation(boolean clampX, boolean clampY) {
        matrix.getValues(matrixValue);

        float dx = 0, dy = 0;

        if(clampX) {
            dx = calculateClampedXTranslation(matrixValue[Matrix.MTRANS_X]);
        }

        if(clampY) {
            dy = calculateClampedYTranslation(matrixValue[Matrix.MTRANS_Y]);
        }

        matrix.postTranslate(dx, dy);
    }

    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private boolean isInPoint = false;

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
            this.isInPoint = presentScale >= pointScale-pointScaleOffset && presentScale <= pointScale+pointScaleOffset;
            if(this.isInPoint) {
                performHapticFeedback(HapticFeedbackConstants.GESTURE_START);
            }
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
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            if(presentScale >= pointScale-pointScaleOffset && presentScale <= pointScale+pointScaleOffset) {
                animateToScale(pointScale);
                performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            }
        }
    }

    private void applyTransformation() {
        this.playerView.invalidate();
    }

    public void setSwipeGestureAvailable(boolean available) {
        this.swipeGestureAvailable = available;
    }
}
