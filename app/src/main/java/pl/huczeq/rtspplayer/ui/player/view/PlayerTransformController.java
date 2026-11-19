package pl.huczeq.rtspplayer.ui.player.view;

import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;

public class PlayerTransformController implements View.OnTouchListener {

    private boolean matrixUpdated = false;

    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;

    private ValueAnimator scaleAnimator;
    private ValueAnimator translationAnimator;
    private boolean hasScalingGestureOccurredDuringTouchInteraction = false;
    private boolean swipeGestureAvailable = true;
    int swipeDirection = 0;// 0 -NONE, <0 - LEFT, >0 - RIGHT
    private boolean isClampingEnabled = true;

    float swipeThreshold = 300;
    public final float MIN_SCALE = 0.5f;
    public final float MAX_SCALE = 20f;
    public float DEFAULT_SCALE = 1f;
    private boolean scaleLocked = false;
    private float presentScale = DEFAULT_SCALE;
    private float[] matrixValues = new float[16];
    private Matrix matrix;

    float pointScaleOffset = 0.15f;
    float pointScale = 1f;

    private static final float MIN_EDGE_RESISTANCE = 0.15f;

    private float edgeDragX = 0f;

    private final View view;
    private View playerView;
    private final OnTransformationMatrixChanged onTransformationMatrixChanged;

    public PlayerTransformController(View view, OnTransformationMatrixChanged onTransformationMatrixChanged) {
        this.onTransformationMatrixChanged = onTransformationMatrixChanged;
        this.view = view;
        this.DEFAULT_SCALE = MathUtils.clamp(DEFAULT_SCALE, MIN_SCALE, MAX_SCALE);
        this.scaleDetector = new ScaleGestureDetector(view.getContext(), new ScaleListener());
        this.gestureDetector = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                view.performClick();
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

                if(scaleDetector.isInProgress())
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
                matrix.getValues(matrixValues);

                float currentX = matrixValues[Matrix.MTRANS_X];
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

                if(scaleDetector.isInProgress()) {
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
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE);
                    else
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_THRESHOLD_DEACTIVATE);
                    swipeDirection = currentSwipeDirection;
                }

                return false;
            }
        });
        this.swipeThreshold = 300 * view.getResources().getDisplayMetrics().density;
        reset();
    }

    public void setPlayerView(View playerView) {
        this.playerView = playerView;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN && motionEvent.getPointerCount() == 1) {
            if(isTranslationXAtLimit()) {
                isClampingEnabled = false;
            }
        }

        gestureDetector.onTouchEvent(motionEvent);
        scaleDetector.onTouchEvent(motionEvent);

        if(motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
            edgeDragX = 0.0f;
            matrix.getValues(matrixValues);
            float x = calculateClampedXTranslation(matrixValues[Matrix.MTRANS_X]);
            if(swipeDirection != 0 && !hasScalingGestureOccurredDuringTouchInteraction) {
                onSwipeListener.onSwipe(swipeDirection < 0 ? -1 : 1);
                swipeDirection = 0;
                matrixUpdated = false;
            }
            if(x != 0) {
                animateToX(matrixValues[Matrix.MTRANS_X] + x);
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

    private boolean isTranslationXAtLimit() {
        return isTranslationXAtLimit(5f);
    }

    private boolean isTranslationXAtLimit(float tolerance) {
        matrix.getValues(matrixValues);

        float translationX = matrixValues[Matrix.MTRANS_X];

        float leftOffset = (view.getMeasuredWidth() - playerView.getMeasuredWidth()) / 2f;

        float center = (view.getMeasuredWidth()
                - playerView.getMeasuredWidth() * presentScale) / 2f - leftOffset;

        float min = center;
        float max = center;

        if(playerView.getMeasuredWidth() * presentScale > view.getMeasuredWidth()) {
            float space = (playerView.getMeasuredWidth() * presentScale
                    - view.getMeasuredWidth()) / 2f;

            min -= space;
            max += space;
        }

        return translationX <= min + tolerance || translationX >= max - tolerance;
    }

    private float getTranslationXOverflow(float translationX) {
        float leftOffset = (view.getMeasuredWidth() - playerView.getMeasuredWidth()) / 2f;

        float center = (view.getMeasuredWidth()
                - playerView.getMeasuredWidth() * presentScale) / 2f - leftOffset;

        float min = center;
        float max = center;

        if(playerView.getMeasuredWidth() * presentScale > view.getMeasuredWidth()) {
            float space = (playerView.getMeasuredWidth() * presentScale
                    - view.getMeasuredWidth()) / 2f;

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
        matrix.getValues(matrixValues);
        float x = calculateClampedXTranslation(matrixValues[Matrix.MTRANS_X]);
        animateToX(matrixValues[Matrix.MTRANS_X] + x);
    }

    private void clearScaleAnimation() {
        if(scaleAnimator != null)
            scaleAnimator.pause();
    }

    private void animateToScale(float scale) {
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

        translationAnimator = ValueAnimator.ofFloat(matrixValues[Matrix.MTRANS_X], x);
        translationAnimator.setDuration(200);
        translationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float newX = (float) animation.getAnimatedValue();

                matrix.getValues(matrixValues);

                float currentX = matrixValues[Matrix.MTRANS_X];
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
        float leftOffset = (view.getMeasuredWidth() - playerView.getMeasuredWidth()) / 2f;
        return getFittedTranslation(presentXTranslation, view.getMeasuredWidth(), playerView.getMeasuredWidth(), presentScale, leftOffset);
    }

    private float calculateClampedYTranslation(float presentYTranslation) {
        float topOffset = (view.getMeasuredHeight() - playerView.getMeasuredHeight()) / 2f;
        return getFittedTranslation(presentYTranslation, view.getMeasuredHeight(), playerView.getMeasuredHeight(), presentScale, topOffset);
    }

    private boolean clampMatrixTranslation(boolean clampX, boolean clampY) {
        matrix.getValues(matrixValues);

        float dx = 0, dy = 0;

        if(clampX) {
            dx = calculateClampedXTranslation(matrixValues[Matrix.MTRANS_X]);
        }

        if(clampY) {
            dy = calculateClampedYTranslation(matrixValues[Matrix.MTRANS_Y]);
        }

        matrix.postTranslate(dx, dy);

        return dx > 0 && dy > 0;
    }

    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private boolean isInPoint = false;

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
            this.isInPoint = presentScale >= pointScale-pointScaleOffset && presentScale <= pointScale+pointScaleOffset;
            if(this.isInPoint) {
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START);
            }
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float topSurfaceOffset = (view.getMeasuredHeight() - playerView.getMeasuredHeight())/2f;
            float leftSurfaceOffset = (view.getMeasuredWidth() - playerView.getMeasuredWidth())/2f;

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
                view.performHapticFeedback(presentInPoint? HapticFeedbackConstants.GESTURE_START : HapticFeedbackConstants.GESTURE_END);
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            if(presentScale >= pointScale-pointScaleOffset && presentScale <= pointScale+pointScaleOffset) {
                animateToScale(pointScale);
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            }
        }
    }

    private void applyTransformation() {
        onTransformationMatrixChanged.onTransformationMatrixChanged(this.matrix);
    }

    public void setSwipeGestureAvailable(boolean available) {
        this.swipeGestureAvailable = available;
    }

    public interface OnTransformationMatrixChanged {
        void onTransformationMatrixChanged(Matrix matrix);
    }
}
