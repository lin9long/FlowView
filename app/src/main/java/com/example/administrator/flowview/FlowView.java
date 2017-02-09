package com.example.administrator.flowview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * Created by Administrator on 2017/2/9.
 */

public class FlowView extends View {


    public FlowView(Context context) {
        super(context);
        init();
    }

    public FlowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
    }

    private float dragCircleRadius = 20f;
    private float stickyCircleRadius = 20f;
    private PointF dragCenter = new PointF(100f, 300f);
    private PointF stickyCenter = new PointF(200f, 300f);
    private PointF[] dragPoints = {new PointF(100f, 280f), new PointF(100f, 320f)};
    private PointF[] stickyPoints = {new PointF(300f, 280f), new PointF(300f, 320f)};
    private PointF controlPoint = new PointF(150f, 300f);

    private int maxDistance = 150;
    private Paint paint;
    //角度比值
    private double linek;


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //将画布往上偏移一个状态栏大小
        canvas.translate(0, -Utils.getStatusBarHeight(getResources()));

        stickyCircleRadius = getStickyRadius();

        canvas.drawCircle(dragCenter.x, dragCenter.y, dragCircleRadius, paint);

        //动态计算贝塞尔曲线与圆切线的两个点
        float xOffset = stickyCenter.x - dragCenter.x;
        float yOffset = stickyCenter.y - dragCenter.y;
        if (xOffset != 0) {
            linek = yOffset / xOffset;
        }
        dragPoints = GeometryUtil.getIntersectionPoints(dragCenter, dragCircleRadius, linek);
        stickyPoints = GeometryUtil.getIntersectionPoints(stickyCenter, stickyCircleRadius, linek);
        //动态变换中心点
        controlPoint = GeometryUtil.getPointByPercent(dragCenter, stickyCenter, 0.618f);


        if (!isDraw) {
            canvas.drawCircle(stickyCenter.x, stickyCenter.y, stickyCircleRadius, paint);
            Path path = new Path();
            path.moveTo(stickyPoints[0].x, stickyPoints[0].y);
            path.quadTo(controlPoint.x, controlPoint.y, dragPoints[0].x, dragPoints[0].y);
            path.lineTo(dragPoints[1].x, dragPoints[1].y);
            path.quadTo(controlPoint.x, controlPoint.y, stickyPoints[1].x, stickyPoints[1].y);
            canvas.drawPath(path, paint);
        }
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(stickyCenter.x, stickyCenter.y, maxDistance, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private float getStickyRadius() {
        float distance = GeometryUtil.getDistanceBetween2Points(dragCenter, stickyCenter);
        float faction = distance / maxDistance;
        float srickyRadius = GeometryUtil.evaluateValue(faction, 20, 4);
        return srickyRadius;
    }

    public boolean isDraw = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dragCenter.set(event.getRawX(), event.getRawY());
                isDraw = false;
                break;
            case MotionEvent.ACTION_MOVE:
                dragCenter.set(event.getRawX(), event.getRawY());

                if (GeometryUtil.getDistanceBetween2Points(dragCenter, stickyCenter) > maxDistance) {
                    isDraw = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (GeometryUtil.getDistanceBetween2Points(dragCenter, stickyCenter) > maxDistance) {
                    dragCenter.set(stickyCenter.x, stickyCenter.y);
                } else {
                    com.nineoldandroids.animation.ValueAnimator animator = com.nineoldandroids.animation.ValueAnimator.ofInt(1);
                    final PointF startPoint = new PointF(dragCenter.x, dragCenter.y);
                    animator.addUpdateListener(new com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(com.nineoldandroids.animation.ValueAnimator valueAnimator) {
                            //拿到状态变换的百分比值
                            float fraction = valueAnimator.getAnimatedFraction();
                            PointF pointByPercent = GeometryUtil.getPointByPercent(startPoint, stickyCenter, fraction);
                            dragCenter.set(pointByPercent);
                            //此处位置改变了需要重新绘制界面
                            invalidate();
                        }
                    });
                    animator.setDuration(300);
                    animator.setInterpolator(new OvershootInterpolator(2));
                    animator.start();

                }

                break;

        }
        invalidate();
        return true;
    }
}
