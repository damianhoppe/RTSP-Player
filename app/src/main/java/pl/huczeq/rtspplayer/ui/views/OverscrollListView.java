package pl.huczeq.rtspplayer.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ListView;

public class OverscrollListView extends ListView {


    private static final int MAX_Y_OVERSCROLL_DISTANCE = 50;
    private static final int ANIMATION_DURATION = 300;
    private static final float SLOW_COEFFICIENT = 0.8F;

    private int mOverscrollDistance;

    private int mMaxYOverscrollDistance;



    private void setAttr(Context ctx, AttributeSet attrs) {
        mOverscrollDistance = MAX_Y_OVERSCROLL_DISTANCE;
    }


    public OverscrollListView(Context context) {
        super(context);


        mOverscrollDistance = MAX_Y_OVERSCROLL_DISTANCE;
        initOverscrollListView();
    }

    public OverscrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttr(context, attrs);
        initOverscrollListView();
    }

    public OverscrollListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setAttr(context, attrs);
        initOverscrollListView();
    }

    private void initOverscrollListView() {
        final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        final float density = metrics.density;
        mMaxYOverscrollDistance = (int) (density * mOverscrollDistance);
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
                                   int scrollY, int scrollRangeX, int scrollRangeY,
                                   int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {


        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY,
                scrollRangeX, scrollRangeY, maxOverScrollX,
                mMaxYOverscrollDistance, isTouchEvent);
    }
}