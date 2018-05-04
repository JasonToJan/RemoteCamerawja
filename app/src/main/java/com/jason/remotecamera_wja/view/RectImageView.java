package com.jason.remotecamera_wja.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.jason.remotecamera_wja.util.DebugUtil;
import com.jason.remotecamera_wja.util.DensityUtil;
import com.jason.remotecamera_wja.util.DisplayUtil;

public class RectImageView extends AppCompatImageView{

    private static final String TAG = "RectImageView";
    public static final int RECTRADIU=600;
    private Paint mLinePaint;
    private Paint mAreaPaint;
    private Rect mCenterRect = null;
    private Context mContext;
    private Paint paint;
    private int mFristPointX , mFristPointY ;
    private int mSecondPointX , mSecondPointY ;
    private boolean isFirstDown = true;
    private int mOldX = 0, mOldY = 0;


    public RectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        mContext = context;
        Point p = DisplayUtil.getScreenMetrics(mContext);
        widthScreen = p.x;
        heightScreen = p.y;

        mFristPointX= DensityUtil.getScreenWidth(mContext)/2-RECTRADIU/2;
        mSecondPointX=mFristPointX+RECTRADIU;
        mFristPointY=DensityUtil.getScreenHeight(mContext)/2-RECTRADIU/2;
        mSecondPointY=mFristPointY+RECTRADIU;
    }

    private void initPaint(){
        //绘制中间透明区域矩形边界的Paint  
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.BLUE);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(10f);
        mLinePaint.setAlpha(60);

        //绘制四周阴影区域  
        mAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAreaPaint.setColor(Color.GRAY);
        mAreaPaint.setStyle(Paint.Style.FILL);
        mAreaPaint.setAlpha(180);

    }
    public void setCenterRect(Rect r){
        Log.i(TAG, "setCenterRect...");
        this.mCenterRect = r;
        postInvalidate();
    }
    public void clearCenterRect(Rect r){
        this.mCenterRect = null;
    }

    int widthScreen, heightScreen;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "onDraw...");
        if(mCenterRect==null) return;
       /* //绘制四周阴影区域
        canvas.drawRect(0, 0, widthScreen, mCenterRect.top, mAreaPaint);
        canvas.drawRect(0, mCenterRect.bottom + 1, widthScreen, heightScreen, mAreaPaint);
        canvas.drawRect(0, mCenterRect.top, mCenterRect.left - 1, mCenterRect.bottom  + 1, mAreaPaint);
        canvas.drawRect(mCenterRect.right + 1, mCenterRect.top, widthScreen, mCenterRect.bottom + 1, mAreaPaint);*/
        //绘制目标透明区域
        canvas.drawRect(new Rect(getmFristPointX(), getmFristPointY(), getmSecondPointX(), getmSecondPointY()), mLinePaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() != MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            Rect mRect = new Rect(getmFristPointX(), getmFristPointY(), getmSecondPointX(), getmSecondPointY());
            if (mRect.contains(x, y)) {
                if (isFirstDown) {
                    mOldX = x;
                    mOldY = y;
                    isFirstDown = false;
                } else {
                    int mXDis = x - mOldX;
                    int mYDis = y - mOldY;
                    mOldX = x;
                    mOldY = y;
                    //超出了当前屏幕范围
                    if(getmFristPointX()+mXDis<0||getmFristPointY()+mYDis<0
                            ||getmSecondPointX()+mXDis>DensityUtil.getScreenWidth(mContext)
                            ||getmSecondPointY()+mYDis>DensityUtil.getScreenHeight(mContext)
                            ){
                        DebugUtil.debug("当前矩形左上角坐标：...");
                        return true;
                    }
                    ReSetVaue(mXDis, mYDis);
                }
            }
        } else {
            isFirstDown = true;
        }
        return true;
    }

    public void ReSetVaue(int xDis, int yDis) {
        setmFristPointX(getmFristPointX() + xDis);
        setmFristPointY(getmFristPointY() + yDis);
        setmSecondPointX(getmFristPointX() + RECTRADIU);
        setmSecondPointY(getmFristPointY() + RECTRADIU);
        invalidate();
    }

    public int getmFristPointX() {
        return mFristPointX;
    }

    public void setmFristPointX(int mFristPointX) {
        this.mFristPointX = mFristPointX;
    }

    public int getmFristPointY() {
        return mFristPointY;
    }

    public void setmFristPointY(int mFristPointY) {
        this.mFristPointY = mFristPointY;
    }

    public int getmSecondPointX() {
        return mSecondPointX;
    }

    public void setmSecondPointX(int mSecondPointX) {
        this.mSecondPointX = mSecondPointX;
    }

    public int getmSecondPointY() {
        return mSecondPointY;
    }

    public void setmSecondPointY(int mSeconPointY) {
        this.mSecondPointY = mSeconPointY;
    }


}
