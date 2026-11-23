package pl.huczeq.rtspplayer.ui.player.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;

import java.util.Set;

import lombok.Getter;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.player.PlayerEventListener;
import pl.huczeq.rtspplayer.player.VideoInfoChangeListener;
import pl.huczeq.rtspplayer.player.VideoInfoChangeListenerProvider;
import pl.huczeq.rtspplayer.player.VideoLayout;
import pl.huczeq.rtspplayer.ui.player.view.renderer.OnFrameCaptureListener;
import pl.huczeq.rtspplayer.ui.player.view.renderer.PlayerRendererCallback;

public class PlayerView extends FrameLayout implements VideoInfoChangeListenerProvider, PlayerTransformController.OnTransformationMatrixChanged {

    @Getter
    private PlayerSurfaceView playerSurfaceView;
    private PlayerTransformController transformController;
    private final Matrix playerTransformationMatrix = new Matrix();
    private final Set<SurfaceLifecycleListener> surfaceLifecycleListeners = new ArraySet<>();
    private final Set<OnVideoBoundsChangeListener> onVideoBoundsChangeListeners = new ArraySet<>();
    private VideoLayout videoLayout;

    @Getter
    private final PlayerEventListener playerEventListener = new PlayerEventListener() {
        @Override
        public void onStartRendering() {
            playerSurfaceView.drawOnlyNewFrames();
            transformController.ensureValidVideoTransform();
        }

        @Override
        public void onPlaying() {}

        @Override
        public void onEndReached() {}

        @Override
        public void onEncounteredError() {}
    };


    public PlayerView(@NonNull Context context) {
        super(context);
        init();
    }

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private ImageView ivPrevious;
    private ImageView ivNext;

    private void init() {
        this.playerSurfaceView = new PlayerSurfaceView(getContext());
        this.playerSurfaceView.setOnSurfaceCreatedListener(new PlayerRendererCallback() {
            @Override
            public void onSurfaceCreated(SurfaceTexture surfaceTexture) {
                for(SurfaceLifecycleListener listener : surfaceLifecycleListeners) {
                    listener.onSurfaceCreated(surfaceTexture);
                }
            }
        });
        this.playerSurfaceView.setOnVideoBoundsChanged(new OnVideoBoundsChangeListener() {
            @Override
            public void onVideoBoundsChanged(Rect videoBounds) {
                for (OnVideoBoundsChangeListener listener : onVideoBoundsChangeListeners) {
                    listener.onVideoBoundsChanged(videoBounds);
                }
            }
        });

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        );

        this.playerSurfaceView.setLayoutParams(params);
        this.addView(this.playerSurfaceView);
        this.transformController = new PlayerTransformController(this, this.playerSurfaceView, this);

        int size = (int) (26 * getResources().getDisplayMetrics().density);

        this.ivPrevious = new ImageView(getContext());
        this.ivPrevious.setImageResource(R.drawable.ic_chevrons_left);
        FrameLayout.LayoutParams pLayoutParams = new FrameLayout.LayoutParams(size, size, Gravity.START | Gravity.CENTER_VERTICAL);
        pLayoutParams.leftMargin = -size;
        this.ivPrevious.setLayoutParams(pLayoutParams);
        this.addView(ivPrevious);

        this.ivNext = new ImageView(getContext());
        this.ivNext.setImageResource(R.drawable.ic_chevrons_right);
        FrameLayout.LayoutParams nLayoutParams = new FrameLayout.LayoutParams(size, size, Gravity.END | Gravity.CENTER_VERTICAL);
        nLayoutParams.rightMargin = -size;
        this.ivNext.setLayoutParams(nLayoutParams);
        this.addView(ivNext);

        ivPrevious.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        ivNext.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        setHapticFeedbackEnabled(true);
        setStaticTransformationsEnabled(true);

        transformController.setOnSwipeAnimateListener(new PlayerTransformController.OnSwipeAnimateListener() {
            private float easeInCustom(float t) {
                float tt = Math.max(0f, (t - 0.5f) * 2f);
                return (float) Math.pow(t, 2 + 4 * tt);
            }

            @Override
            public void onSwipe(float distance, float effectiveDistance, float threshold) {
                float swipeFactor = distance / threshold;
                float absSwipeFactor = Math.abs(swipeFactor);

                float alpha = easeInCustom(absSwipeFactor);

                float scale = 0.2f + absSwipeFactor * 0.6f;

                if(swipeFactor < 0) {
                    ivPrevious.setTranslationX(-(effectiveDistance - size) / 2);
                    ivPrevious.setAlpha(alpha);
                    ivPrevious.setScaleX(scale);
                    ivPrevious.setScaleY(scale);
                }else if(swipeFactor > 0){
                    ivNext.setTranslationX(-(effectiveDistance + size) / 2);
                    ivNext.setAlpha(alpha);
                    ivNext.setScaleX(scale);
                    ivNext.setScaleY(scale);
                }
            }

            @Override
            public void onSwipeEnd(float direction) {
                if(direction < 0) {
                    ivPrevious.animate()
                            .alpha(0f)
                            .translationX(ivPrevious.getTranslationX() * 2)
                            .setDuration(200)
                            .scaleX(0.2f)
                            .scaleY(0.2f)
                            .withEndAction(() -> ivPrevious.setTranslationX(0));
                }else {
                    ivPrevious.animate()
                            .alpha(0f)
                            .translationX(0f)
                            .scaleX(0.2f)
                            .scaleY(0.2f)
                            .setDuration(200);
                }

                if(direction > 0) {
                    ivNext.animate()
                            .alpha(0f)
                            .translationX(ivNext.getTranslationX() * 2)
                            .scaleX(0.2f)
                            .scaleY(0.2f)
                            .setDuration(200)
                            .withEndAction(() -> ivNext.setTranslationX(0));
                }else {
                    ivNext.animate()
                            .alpha(0f)
                            .translationX(0f)
                            .scaleX(0.2f)
                            .scaleY(0.2f)
                            .setDuration(200);
                }
            }
        });
    }

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation transformation) {
        transformation.getMatrix().set(playerTransformationMatrix);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return this.transformController.onTouch(this, motionEvent);
    }

    public void setOnSwipeListener(PlayerTransformController.OnSwipeListener listener) {
        this.transformController.setOnSwipeListener(listener);
    }

    public void setSwipeGestureAvailable(boolean available) {
        this.transformController.setSwipeGestureAvailable(available);
    }

    public void registerSurfaceLifecycleListener(SurfaceLifecycleListener listener, boolean notifyIfCreated) {
        this.surfaceLifecycleListeners.add(listener);

        if(!notifyIfCreated)
            return;

        SurfaceTexture surfaceTexture = playerSurfaceView.getSurfaceTexture();
        if(surfaceTexture != null)
            listener.onSurfaceCreated(surfaceTexture);
    }

    public void unregisterSurfaceLifecycleListener(SurfaceLifecycleListener listener) {
        this.surfaceLifecycleListeners.remove(listener);
    }

    private final VideoInfoChangeListener videoInfoChangeListener = new VideoInfoChangeListener() {
        @Override
        public void onVideoLayoutChanged(VideoLayout videoLayout) {
            PlayerView.this.videoLayout = videoLayout;
            playerSurfaceView.onVideoLayoutChanged(videoLayout);
            updateSurfaceSize();
        }
    };


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(changed)
            updateSurfaceSize();
    }

    private void updateSurfaceSize() {
        if(videoLayout == null)
            return;

        int width = getWidth();
        int height = getHeight();

        float videoAspectRatio = videoLayout.getHeightToWidthRatio();
        float surfaceAspectRatio = (float)height / width;

        float aspectDeformation = videoAspectRatio / surfaceAspectRatio - 1;
        if(aspectDeformation < 0) {
            height = (int) (width * videoAspectRatio);
        }else {
            width = (int) (height / videoAspectRatio);
        }

        FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams) playerSurfaceView.getLayoutParams();

        params.width = width;
        params.height = height;
        params.gravity = Gravity.CENTER;

        playerSurfaceView.setLayoutParams(params);
    }

    @Override
    public VideoInfoChangeListener getVideoInfoChangeListener() {
        return this.videoInfoChangeListener;
    }

    public void cancelFrameCapture() {
        this.playerSurfaceView.cancelFrameCapture();
    }

    public void captureFrame() {
        this.playerSurfaceView.captureFrame();
    }

    public void registerOnFrameCaptureListener(OnFrameCaptureListener onFrameCaptureListener) {
        this.playerSurfaceView.registerOnFrameCaptureListener(onFrameCaptureListener);
    }

    public void registerOnVideoBoundsChangeListener(OnVideoBoundsChangeListener listener) {
        onVideoBoundsChangeListeners.add(listener);
    }

    @Override
    public void onTransformationMatrixChanged(Matrix matrix) {
        this.playerTransformationMatrix.set(matrix);
        playerSurfaceView.invalidate();
    }

    public interface SurfaceLifecycleListener {
        void onSurfaceCreated(SurfaceTexture surfaceTexture);
    }

    public interface OnVideoBoundsChangeListener {
        void onVideoBoundsChanged(Rect videoBounds);
    }

    public PlayerTransformController getTransformController() {
        return this.transformController;
    }
}
