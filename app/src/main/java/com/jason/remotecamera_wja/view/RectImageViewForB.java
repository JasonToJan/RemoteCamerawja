package com.jason.remotecamera_wja.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.app.Constant;
import com.jason.remotecamera_wja.partb.SendThread;
import com.jason.remotecamera_wja.util.DebugUtil;
import com.jason.remotecamera_wja.util.DensityUtil;
import com.jason.remotecamera_wja.util.DisplayUtil;

import java.net.Socket;

/**
 * 自定义区域拍照的视图
 */
public class RectImageViewForB extends AppCompatImageView {

    private static final String TAG = "RectImageView";
    public static final int RECTRADIU=Constant.RECTHEIGHTFORB;//区域视图的宽高
    private Paint mLinePaint;//视图边框画笔
    private Paint mAreaPaint;//视图外部画笔
    private Context mContext;//上下文
    int widthScreen, heightScreen;//屏幕的宽高
    private int mFristPointX , mFristPointY ;//左上角的坐标
    private int mSecondPointX , mSecondPointY ;//右下角的坐标
    private boolean isFirstDown = true;//是否第一次触摸
    private int mOldX = 0, mOldY = 0;//保存第一次触摸的坐标点
    private int max_width= DensityUtil.getScreenWidth(InitApp.AppContext)
            -2*DensityUtil.dip2px(InitApp.AppContext,16);//最大的宽度
    private int max_height=DensityUtil.getScreenHeight(InitApp.AppContext)-700;//最大的高度
    private int min_width=0;//最小的宽度
    private int min_height=0;//最小的高度
    public Socket socket;//传进来一个socket，当改变了区域矩形的位置就发送一个事件给A端

    public RectImageViewForB(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        mContext = context;
        Point p = DisplayUtil.getScreenMetrics(mContext);
        widthScreen = p.x;
        heightScreen = p.y;
        //在拍照区域的正中心画一个矩形
        mFristPointX= DensityUtil.getScreenWidth(InitApp.AppContext)/2
                - DensityUtil.dip2px(InitApp.AppContext,16)
                -RECTRADIU/2;
        mSecondPointX=mFristPointX+RECTRADIU;
        mFristPointY=400;
        mSecondPointY=mFristPointY+RECTRADIU;
        DebugUtil.debug("坐标："+getmFristPointX()+" "+getmFristPointY());

    }

    public void setSocket(Socket socket){
        this.socket=socket;
    }

    /**
     * 设置最大宽度
     * @param max_width
     */
    public void setMax_width(int max_width){
        this.max_width= max_width;
    }

    /**
     * 设置最大高度
     * @param max_height
     */
    public void setMax_height(int max_height){
        this.min_height=(DensityUtil.getScreenHeight(InitApp.AppContext)-Constant.DEFAULT_HEIGHT-max_height-DensityUtil.getStatusBarHeight(InitApp.AppContext))/2;
        this.max_height=this.min_height+max_height;
    }

    /**
     * 初始化画笔
     */
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

    /**
     * 设置中心矩形
     * @param r
     */
    public void setCenterRect(Rect r){
        DebugUtil.debug("setCenterRect..."+r.left+" "+r.top);
        setmFristPointX(r.left);
        setmFristPointY(r.top);
        setmSecondPointX(r.right);
        setmSecondPointY(r.bottom);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(new Rect(getmFristPointX(), getmFristPointY(), getmSecondPointX(), getmSecondPointY()), mLinePaint);
    }

    /**
     * 触摸事件，保证矩形随着手势滑动，但是不能超过边界
     * @param event
     * @return
     */
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
                    DebugUtil.debug("当前矩形左上角坐标：..."+" min_height="+min_height+" max_height="+max_height);
                    if(getmFristPointX()+mXDis<min_width||getmFristPointY()+mYDis<min_height
                            ||getmSecondPointX()+mXDis>max_width
                            ||getmSecondPointY()+mYDis>max_height
                            ){
                        DebugUtil.debug("当前矩形左上角坐标：..."+" min_height="+min_height+" max_height="+max_height);
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 获取宽-测量规则的模式和大小
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        // 获取高-测量规则的模式和大小
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // 设置wrap_content的默认宽 / 高值
        // 默认宽/高的设定并无固定依据,根据需要灵活设置
        // 类似TextView,ImageView等针对wrap_content均在onMeasure()对设置默认宽 / 高值有特殊处理,具体读者可以自行查看
        int mWidth = 400;
        int mHeight = 400;

        // 当布局参数设置为wrap_content时，设置默认值
        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT && getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mWidth, mHeight);
        } else if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mWidth, heightSize);
        } else if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthSize, mHeight);
        }
    }

    /**
     * 重新设置矩形的坐标
     * @param xDis
     * @param yDis
     */
    public void ReSetVaue(int xDis, int yDis) {
        setmFristPointX(getmFristPointX() + xDis);
        setmFristPointY(getmFristPointY() + yDis);
        setmSecondPointX(getmSecondPointX()+xDis);
        setmSecondPointY(getmSecondPointY()+yDis);

        int first_x=getmFristPointX();
        int first_y=getmFristPointY();

        //发送改变的区域坐标的比例给A端
        float ratio_x=first_x/(float)max_width;
        float ratio_y=first_y/(float)max_height;

        if(socket!=null){
            new SendThread(socket,Constant.AREAPOINTFALG,ratio_x+"x"+ratio_y).start();
        }
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
