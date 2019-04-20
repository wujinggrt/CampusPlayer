package com.guochuang.campusplayer.geocomponent;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.guochuang.campusplayer.HuxiActivity;
import com.guochuang.campusplayer.R;
import com.supermap.realspace.SceneControl;

/**
 *
 * 用来控制长按弹出地标的选项，然后控制一些列地标操作。
 *
 */
public class GestureListenerForLandmark implements GestureDetector.OnGestureListener {

    public static final String TAG = "ListenerForPlacemark";

    private HuxiActivity context;
    private LandmarkComponent placemarkerComponent;

    public GestureListenerForLandmark(HuxiActivity context, LandmarkComponent placemarkerComponent) {
        super();
        this.context = context;
        this.placemarkerComponent = placemarkerComponent;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Point point = getPointFromEvent(e);
        placemarkerComponent.processSingleTap(point);
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private Point getPointFromEvent(MotionEvent event) {
        double x = event.getX() - 28.1;
        double y = event.getY() - 0.5;
        return new android.graphics.Point((int)x, (int)y);
    }
}
