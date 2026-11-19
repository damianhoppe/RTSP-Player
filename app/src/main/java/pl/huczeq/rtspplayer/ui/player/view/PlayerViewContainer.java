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

public class PlayerViewContainer extends FrameLayout implements PlayerTransformController.OnTransformationMatrixChanged {

    private View playerView;
    private PlayerTransformController transformController;
    private final Matrix matrix = new Matrix();


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
        setStaticTransformationsEnabled(true);
        setHapticFeedbackEnabled(true);

        this.transformController = new PlayerTransformController(this, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(getChildCount() == 0)
            throw new IllegalArgumentException("PlayerViewContainer should contains view.");
        this.playerView = getChildAt(0);
        this.transformController.setPlayerView(this.playerView);
        this.playerView.invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        this.transformController.onTouch(this, motionEvent);
        return true;
    }

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {
        t.getMatrix().set(this.matrix);
        return true;
    }

    public void setOnSwipeListener(PlayerTransformController.OnSwipeListener listener) {
        this.transformController.setOnSwipeListener(listener);
    }

    public void setPlayerView(View playerView) {
        this.playerView = playerView;
    }

    public PlayerTransformController getTransformController() {
        return this.transformController;
    }

    public void setSwipeGestureAvailable(boolean available) {
        this.transformController.setSwipeGestureAvailable(available);
    }

    @Override
    public void onTransformationMatrixChanged(Matrix matrix) {
        this.matrix.set(matrix);
        this.playerView.invalidate();
    }
}
