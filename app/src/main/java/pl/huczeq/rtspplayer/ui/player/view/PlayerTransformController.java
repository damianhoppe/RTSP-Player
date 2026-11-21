package pl.huczeq.rtspplayer.ui.player.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;
import androidx.core.view.HapticFeedbackConstantsCompat;
import androidx.core.view.ViewCompat;

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

    private final Context context;
    private final View view;
    private View playerView;
    private OnTransformationMatrixChanged onTransformationMatrixChanged;
    private final OverScroller overScroller;
    private int lastFlingX;
    private int lastFlingY;
    private boolean flingRunning;

    private final Runnable flingRunnable = new Runnable() {
        @Override
        public void run() {
            if(!flingRunning || overScroller.isFinished()) {
                flingRunning = false;
                return;
            }

            if(overScroller.computeScrollOffset()) {
                int currentX = overScroller.getCurrX();
                int currentY = overScroller.getCurrY();

                float dx = currentX - lastFlingX;
                float dy = currentY - lastFlingY;

                lastFlingX = currentX;
                lastFlingY = currentY;

                matrix.postTranslate(dy, dx);

                if(clampMatrixTranslation(true, true)) {
                    overScroller.forceFinished(true);
                    return;
                }
                applyTransformation();

                view.postOnAnimation(this);
            }else {
                flingRunning = false;
            }
        }
    };

    public PlayerTransformController(View view, View playerView, OnTransformationMatrixChanged onTransformationMatrixChanged) {
        this.onTransformationMatrixChanged = onTransformationMatrixChanged;
        this.view = view;
        this.playerView = playerView;
        this.context = view.getContext();
        this.DEFAULT_SCALE = MathUtils.clamp(DEFAULT_SCALE, MIN_SCALE, MAX_SCALE);
        this.scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        this.gestureDetector = new GestureDetector(context, new GestureListener());
        this.overScroller = new OverScroller(context);
        this.swipeThreshold = 300 * view.getResources().getDisplayMetrics().density;
        reset();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
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

    public void ensureValidVideoTransform() {
        if(clampMatrixTranslation(true, true)) {
            applyTransformation();
        }
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
        animateToScale(DEFAULT_SCALE);
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

    private PointF calculateClampedTranslationWithScale(float scale, float pointX, float pointY) {
        Matrix m = new Matrix(matrix);
        float ds = scale/presentScale;
        m.postScale(ds,ds,pointX,pointY);

        float[] mv = new float[9];
        m.getValues(mv);

        float leftOffset = (view.getMeasuredWidth() - playerView.getMeasuredWidth()) / 2f;
        float clampedX = getFittedTranslation(mv[Matrix.MTRANS_X], view.getMeasuredWidth(), playerView.getMeasuredWidth(), scale, leftOffset);

        float topOffset = (view.getMeasuredHeight() - playerView.getMeasuredHeight()) / 2f;
        float clampedY = getFittedTranslation(mv[Matrix.MTRANS_Y], view.getMeasuredHeight(), playerView.getMeasuredHeight(), scale, topOffset);

        return new PointF(
                mv[Matrix.MTRANS_X] + clampedX,
                mv[Matrix.MTRANS_Y] + clampedY
        );
    }

    private void animateToScale(float targetScale) {
        animateToScale(
                targetScale,
                view.getMeasuredWidth() / 2f,
                view.getMeasuredHeight() / 2f
        );
    }

    private void animateToScale(float proposedTargetScale, float pointX, float pointY) {
        clearScaleAnimation();
        float targetScale = Math.min(MAX_SCALE, Math.max(MIN_SCALE, proposedTargetScale));

        this.matrix.getValues(matrixValues);

        float startScale = presentScale;

        float startX = matrixValues[Matrix.MTRANS_X];
        float startY = matrixValues[Matrix.MTRANS_Y];

        PointF targetTranslation = calculateClampedTranslationWithScale(targetScale, pointX, pointY);

        float targetX = targetTranslation.x;
        float targetY = targetTranslation.y;

        scaleAnimator = ValueAnimator.ofFloat(0f, 1f);
        scaleAnimator.setDuration(200);

        scaleAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();

            float newScale = startScale + (targetScale - startScale) * fraction;
            float scaleFactor = newScale / presentScale;

            presentScale = newScale;

            matrix.postScale(
                    scaleFactor,
                    scaleFactor,
                    pointX,
                    pointY
            );

            this.matrix.getValues(matrixValues);

            float targetCurrentX = startX + (targetX - startX) * fraction;
            float targetCurrentY = startY + (targetY - startY) * fraction;

            matrix.postTranslate(
                    targetCurrentX - matrixValues[Matrix.MTRANS_X],
                    targetCurrentY - matrixValues[Matrix.MTRANS_Y]
            );

            applyTransformation();
        });

        scaleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                presentScale = targetScale;
                scaleAnimator = null;
                clampMatrixTranslation(true, true);
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

        translationAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                translationAnimator = null;
                clampMatrixTranslation(true, true);
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

    private float calculateFillScale() {
        float viewportWidth = view.getMeasuredWidth();
        float viewportHeight = view.getMeasuredHeight();

        float videoWidth = playerView.getMeasuredWidth();
        float videoHeight = playerView.getMeasuredHeight();

        if(viewportWidth <= 0 || viewportHeight <= 0 ||
                videoWidth <= 0 || videoHeight <= 0) {
            return DEFAULT_SCALE;
        }

        float widthScale = viewportWidth / videoWidth;
        float heightScale = viewportHeight / videoHeight;

        float fitScale = Math.min(widthScale, heightScale);
        float fillScale = Math.max(widthScale, heightScale);

        return fillScale / fitScale;
    }

    private float easeOutCubic(float t) {
        return 1f - (float) Math.pow(1f - t, 3);
    }

    private float getEdgeResistance(float drag) {
        float t = Math.min(1f, drag / swipeThreshold);

        float eased = 1f - easeOutCubic(t);

        return MIN_EDGE_RESISTANCE + (1f - MIN_EDGE_RESISTANCE) * eased;
    }

    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private boolean isInPoint = false;

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
            this.isInPoint = presentScale >= pointScale-pointScaleOffset && presentScale <= pointScale+pointScaleOffset;
            if(this.isInPoint) {
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.GESTURE_START);
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
                ViewCompat.performHapticFeedback(view, presentInPoint? HapticFeedbackConstantsCompat.GESTURE_START : HapticFeedbackConstantsCompat.GESTURE_END);
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            if(presentScale >= pointScale-pointScaleOffset && presentScale <= pointScale+pointScaleOffset) {
                animateToScale(pointScale);
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM);
            }
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            view.performClick();
            return true;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            if(overScroller != null) {
                overScroller.forceFinished(true);
            }
            flingRunning = false;

            clearScaleAnimation();
            clearTranslationAnimation();
            if(isTranslationXAtLimit()) {
                isClampingEnabled = false;
            }
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
            if(e.getActionMasked() == MotionEvent.ACTION_DOWN)
                return false;

            if(scaleDetector.isInProgress())
                return false;

            if(e.getActionMasked() == MotionEvent.ACTION_UP) {
                float topSurfaceOffset = (view.getMeasuredHeight() - playerView.getMeasuredHeight())/2f;
                float leftSurfaceOffset = (view.getMeasuredWidth() - playerView.getMeasuredWidth())/2f;

                float pointX = e.getX() - leftSurfaceOffset;
                float pointY = e.getY() - topSurfaceOffset;

                float firstStep = Math.max(calculateFillScale(), 2f);
                if(Math.abs(presentScale - DEFAULT_SCALE) <= 0.1f) {
                    animateToScale(firstStep,pointX,pointY);
                }else if(Math.abs(presentScale - firstStep) <= 0.1f) {
                    animateToScale(presentScale * 2f,pointX,pointY);
                }else {
                    animateToScale(DEFAULT_SCALE, pointX, pointY);
                }
                isClampingEnabled = true;
                return true;
            }
            
            if(e.getActionMasked() == MotionEvent.ACTION_UP) {
                isClampingEnabled = true;
                float pointX = e.getX();
                float pointY = e.getY();
                animateToScale(DEFAULT_SCALE, pointX, pointY);
                return true;
            }

            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            overScroller.forceFinished(true);

            lastFlingX = 0;
            lastFlingY = 0;

            overScroller.fling(
                    0, 0,
                    (int) velocityY,
                    (int) velocityX,
                    -100000, 100000,
                    -100000, 100000
            );

            flingRunning = true;
            view.postOnAnimation(flingRunnable);

            return true;
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
                    ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.GESTURE_THRESHOLD_ACTIVATE);
                else
                    ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.GESTURE_THRESHOLD_DEACTIVATE);
                swipeDirection = currentSwipeDirection;
            }

            return false;
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
