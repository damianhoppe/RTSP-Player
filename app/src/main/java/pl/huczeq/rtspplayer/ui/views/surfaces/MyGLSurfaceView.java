package pl.huczeq.rtspplayer.ui.views.surfaces;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.ui.renderers.GLSurfaceViewRenderer;
import pl.huczeq.rtspplayer.ui.renderers.OnTakeImageCallback;
import pl.huczeq.rtspplayer.ui.renderers.base.RendererCallback;
import pl.huczeq.rtspplayer.ui.renderers.base.SurfaceRenderer;
import pl.huczeq.rtspplayer.ui.views.surfaces.base.BaseSurfaceView;
import pl.huczeq.rtspplayer.ui.views.surfaces.gestures.MoveGestureDetector;

public class MyGLSurfaceView extends GLSurfaceView implements BaseSurfaceView{

    private GLSurfaceViewRenderer renderer;
    private DataManager dataManager;

    private ScaleGestureDetector mScaleDetector;
    private MoveGestureDetector mMoveDetector;
    private OnClickListener onClickListener;
    private float mScaleFactor = 1.f;
    private float mFocusX = 0.f;
    private float mFocusY = 0.f;
    private boolean canCallClick;
    private float startTouchX;
    private float startTouchY;

    public MyGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        this.renderer = new GLSurfaceViewRenderer(getContext());
        this.dataManager = DataManager.getInstance(getContext());
        setEGLContextClientVersion(2);
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        mMoveDetector = new MoveGestureDetector(getContext(), new MoveListener());
    }

    public void initRenderer(RendererCallback rendererCallback, OnTakeImageCallback takeImageCallback) {
        renderer.init(rendererCallback, takeImageCallback);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void takePicture() {
        this.renderer.takePicture();
    }

    public void onNewVideoSize(int width, int height, int videoWidth, int videoHeight) {
        this.renderer.onNewVideoSize(width, height, videoWidth, videoHeight);
    }

    public void onConfigurationChanged() {
        mFocusX = 0;
        mFocusY = 0;
        mScaleFactor = 1;
        this.renderer.onConfigurationChanged();
    }

    @Override
    public SurfaceRenderer getSurfaceRenderer() {
        return this.renderer;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
        long deltaTime = motionEvent.getEventTime() - motionEvent.getDownTime();
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            this.canCallClick = true;
            this.startTouchX = motionEvent.getX();
            this.startTouchY = motionEvent.getY();
        }
        if(canCallClick && motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            float distance = (float)Math.sqrt(Math.pow(motionEvent.getX() - this.startTouchX,2) + Math.pow(motionEvent.getY() - this.startTouchY, 2));
            if(distance >= 5)
                canCallClick = false;
        }
        if(canCallClick && deltaTime <= Settings.CLICK_MIN_DELTA_TIME && motionEvent.getAction() == MotionEvent.ACTION_UP && this.onClickListener != null) {
            this.onClickListener.onClick(this);
            return true;
        }

        android.graphics.Matrix matrix = new android.graphics.Matrix();

        mScaleDetector.onTouchEvent(motionEvent);
        this.renderer.scale = mScaleFactor;
        float scale = 1 - (2/(mScaleFactor*2));
        matrix.setScale(scale * this.renderer.videoScaleX, scale * this.renderer.videoScaleY, 0, 0);

        mMoveDetector.onTouchEvent(motionEvent);
        float dx = mFocusX;
        float dy = mFocusY;

        RectF rect = new RectF(-1,1,1,-1);
        matrix.mapRect(rect);

        if(dx < rect.left) {
            dx = rect.left;
            mFocusX = dx;
        }
        if(dx > rect.right) {
            dx = rect.right;
            mFocusX = dx;
        }
        if(dy > rect.bottom) {
            dy = rect.bottom;
            mFocusY = dy;
        }
        if(dy < rect.top) {
            dy = rect.top;
            mFocusY = dy;
        }
        this.renderer.dx = -dx;
        this.renderer.dy = dy;
        return true;
    }

    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(1.f, Math.min(mScaleFactor, 4.0f));
            return true;
        }
    }

    public class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            PointF d = detector.getFocusDelta();
            mFocusX += d.x * 0.0007;
            mFocusY += d.y * 0.0007;
            return true;
        }
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
