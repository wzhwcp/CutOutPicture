package com.wcp.cutoutpicture.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.wcp.cutoutpicture.R;
import com.wcp.cutoutpicture.util.DensityUtil;

/**
 * Created by wcp on 2016/8/11.
 */
public class MasklayerCircleView extends View {
    private String TAG = MasklayerCircleView.class.getSimpleName();

    private int CIRCLE_TYPE = 0;
    private int RECT_TYPE = 1;

    private int INIT_TYPE = 0;
    private int TOP_SCALE_TYPE = 1;  //顶部扩大
    private int BOTTOM_SCALE_TYPE = 2;  //底部扩大
    private int LEFT_SCALE_TYPE = 3;    //左边扩大
    private int RIGHT_SCALE_TYPE = 4;   //右边扩大
    private int OVERALL_SHIFT_TYPE = 5;
    private int mChangeType = INIT_TYPE;
    private float mCenterX;
    private float mCenterY;
    private float mRadius;
    private int mBigCircleColor;
    private int mSmallCircleColor;
    private int mDottedLineColor;
    private float smallCircleRadius;
    private int mShapeType;  //输出形状 0 圆形 1矩形

    //改变前的圆心坐标
    private float mLastCenterX, mLastCenterY, mLastRadius;
    private Paint mBigCirclePaint, mSmallCirclePaint,mLinePaint;
    private int smallTouchRangeRadius = 25;   //上下左右边触摸范围
    private Rect topRect,bottomRect,leftRect,rightRect;
    private Context mContext;
    private float lastX;
    private float lastY;
    private float offsetX = 0;
    private float offsetY = 0;
    private float mMinRadius;
    private float mMaxRadius;
    private Paint mPaint = new Paint();
    //画重叠遮罩
    private Paint masklayerPaint;
    //根据图片的宽高来，设置截图圆移动范围
    private float mShiftBorderTop;
    private float mShiftBorderLeft;
    private float mShiftBorderRight;
    private float mShiftBorderBottom;
    private Canvas topCircleCanvas = new Canvas();
    private Canvas bottomRectCanvas = new Canvas();

    public MasklayerCircleView(Context context) {
        this(context,null);
    }

    public MasklayerCircleView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MasklayerCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        TypedArray mTypedArray= context.obtainStyledAttributes(attrs,R.styleable.Masklayer);

        mCenterX = mTypedArray.getFloat(R.styleable.Masklayer_centerX,0);
        mCenterY = mTypedArray.getFloat(R.styleable.Masklayer_centerY,0);
        mRadius = mTypedArray.getFloat(R.styleable.Masklayer_bigCircleRadius,0);
        smallCircleRadius = mTypedArray.getFloat(R.styleable.Masklayer_smallCircleRadius,17);
        mMinRadius = mTypedArray.getFloat(R.styleable.Masklayer_bigCircleMinRadius,80);
        mMaxRadius = mTypedArray.getFloat(R.styleable.Masklayer_bigCircleMaxRadius,(DensityUtil.getScreenSize((Activity) mContext).widthPixels/2));
        mShapeType = mTypedArray.getInt(R.styleable.Masklayer_outPutShape,CIRCLE_TYPE);

        mBigCircleColor =  mTypedArray.getColor(R.styleable.Masklayer_bigCircleColor,getResources().getColor(R.color.circle_color));
        mSmallCircleColor = mTypedArray.getColor(R.styleable.Masklayer_smallCircleColor,getResources().getColor(R.color.circle_color));
        mDottedLineColor =  mTypedArray.getColor(R.styleable.Masklayer_dottedLineColor,getResources().getColor(R.color.circle_color));
        init();
    }

    private void init() {
        mPaint.setAntiAlias(true);//设置消除锯齿

        /**俩图片相交模式*/
        masklayerPaint = new Paint();
        masklayerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        /**大圆*/
        mBigCirclePaint = new Paint();
        mBigCirclePaint.setStyle(Paint.Style.STROKE);
        mBigCirclePaint.setAntiAlias(true);
        mBigCirclePaint.setColor(mBigCircleColor);
        mBigCirclePaint.setStrokeWidth(4);

        /**四个小圆*/
        mSmallCirclePaint = new Paint();
        mSmallCirclePaint.setAntiAlias(true);
        mSmallCirclePaint.setColor(mSmallCircleColor);
        mSmallCirclePaint.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000);

        /**虚线*/
        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(3);
        mLinePaint.setColor(mDottedLineColor);
        PathEffect effects = new DashPathEffect(new float[]{5,5,5,5},1);
        mLinePaint.setPathEffect(effects);

        topRect = new Rect();
        bottomRect = new Rect();
        leftRect = new Rect();
        rightRect = new Rect();

        updateRect();
    }

    /**
     * 获取截图蒙板
     * @return
     */
    private Bitmap getMaskLayer() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ALPHA_8);
        bottomRectCanvas.setBitmap(bitmap);
        mPaint.setColor(mContext.getResources().getColor(R.color.transparent_black));
        bottomRectCanvas.drawRect(0,0,DensityUtil.getScreenSize((Activity) mContext).widthPixels
                ,DensityUtil.getScreenSize((Activity) mContext).heightPixels,mPaint);

        Bitmap bitmap1 = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ALPHA_8);
        topCircleCanvas.setBitmap(bitmap1);
        mPaint.reset();
        if (mShapeType == CIRCLE_TYPE) {
            topCircleCanvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
        } else {
            topCircleCanvas.drawRect(mCenterX - mRadius,mCenterY - mRadius,mCenterX + mRadius,mCenterY +mRadius,mPaint);
        }

        bottomRectCanvas.drawBitmap(bitmap1,0,0,masklayerPaint);
        bitmap1.recycle();
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Bitmap bitmap = getMaskLayer();
        mPaint.reset();
        canvas.drawBitmap(bitmap,0,0,mPaint);

        if (mShapeType == CIRCLE_TYPE) {
            canvas.drawCircle(mCenterX,mCenterY,mRadius, mBigCirclePaint);
        }

        canvas.drawCircle(mCenterX,mCenterY - mRadius, smallCircleRadius, mSmallCirclePaint);
        canvas.drawCircle(mCenterX,mCenterY + mRadius, smallCircleRadius, mSmallCirclePaint);
        canvas.drawCircle(mCenterX - mRadius ,mCenterY, smallCircleRadius, mSmallCirclePaint);
        canvas.drawCircle(mCenterX + mRadius, mCenterY, smallCircleRadius, mSmallCirclePaint);

        canvas.drawRect((int)mCenterX - mRadius,(int)mCenterY - mRadius,(int)mCenterX + mRadius,(int)mCenterY + mRadius,mLinePaint);

        updateRect();
        bitmap.recycle();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (topRect.contains((int)x, (int)y)) {
//                    Log.i(TAG,"touchRect is top ");
                    mChangeType = TOP_SCALE_TYPE;
                } else if (bottomRect.contains((int) x, (int) y)) {
                    mChangeType = BOTTOM_SCALE_TYPE;
                } else if (leftRect.contains((int) x, (int) y)) {
//                    Log.i(TAG,"touchRect is left ");
                    mChangeType = LEFT_SCALE_TYPE;
                } else if (rightRect.contains((int) x, (int) y)) {
                    mChangeType = RIGHT_SCALE_TYPE;
                } else {
                    mChangeType = OVERALL_SHIFT_TYPE;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                offsetX = x - lastX;
                offsetY = y - lastY;
//                Log.i(TAG,"offsetX=" + offsetX + "||offsetY=" + offsetY);
                mLastCenterX = mCenterX;
                mLastCenterY = mCenterY;
                mLastRadius = mRadius;
                if (mChangeType == TOP_SCALE_TYPE) {
                    float var = offsetY / 2;
                    mCenterY += var;
                    mRadius -= var;
                } else if (mChangeType == BOTTOM_SCALE_TYPE) {
                    float var = -(offsetY / 2);
                    mCenterY -= var;
                    mRadius -= var;
                } else if (mChangeType == RIGHT_SCALE_TYPE) {
                    float var = -(offsetX / 2);
                    mCenterX -= var;
                    mRadius -= var;
                } else if (mChangeType == LEFT_SCALE_TYPE) {
                    float var = offsetX / 2;
                    mCenterX += var;
                    mRadius -= var;
                } else {
                    mCenterX += offsetX;
                    mCenterY += offsetY;
                }
                checkRaius();
                checkCircleBounds();
                break;
            case MotionEvent.ACTION_UP:
                mChangeType = INIT_TYPE;
                break;
        }
        lastX = x;
        lastY = y;
        invalidate();
        return true;
    }

    public float getmCenterX() {
        return mCenterX;
    }

    public float getmCenterY() {
        return mCenterY;
    }

    public float getmRadius() {
        return mRadius;
    }

    /**
     *     设置移动范围和初始话圆的位置和大小
     */
    public void setShiftRange(float photoWidth,float photoHigh) {
        mShiftBorderTop = (DensityUtil.getScreenSize((Activity) mContext).heightPixels - photoHigh)/2 ;
        mShiftBorderBottom = mShiftBorderTop + photoHigh;
        mShiftBorderLeft = 0;
        mShiftBorderRight = photoWidth;
        if (photoWidth > photoHigh) {
            mRadius = photoHigh/2;
            mCenterX = photoWidth/2;
            mCenterY = mShiftBorderTop + mRadius;
        }
        invalidate();
    }

    /**
     * 更新上下左右拖拽点的范围
     */
    private  void updateRect() {
        topRect.set((int)mCenterX- smallTouchRangeRadius,(int)(mCenterY- mRadius - smallTouchRangeRadius),(int)mCenterX + smallTouchRangeRadius,(int) (mCenterY - mRadius + smallTouchRangeRadius));
        bottomRect.set((int)mCenterX- smallTouchRangeRadius,(int)(mCenterY + mRadius - smallTouchRangeRadius),(int)mCenterX + smallTouchRangeRadius,(int) (mCenterY + mRadius + smallTouchRangeRadius));
        leftRect.set((int)(mCenterX - mRadius- smallTouchRangeRadius),(int)mCenterY - smallTouchRangeRadius,(int)(mCenterX - mRadius + smallTouchRangeRadius),(int)mCenterY + smallTouchRangeRadius);
        rightRect.set((int)(mCenterX + mRadius - smallTouchRangeRadius),(int)mCenterY - smallTouchRangeRadius,(int)(mCenterX + mRadius + smallTouchRangeRadius),(int)mCenterY + smallTouchRangeRadius);
    }

    /**
     * 设置圆的半径范围
     */
    private void checkRaius() {
        if (mRadius < mMinRadius) {
            mRadius = mMinRadius;
            mCenterX = mLastCenterX;
            mCenterY = mLastCenterY;
        } else if(mRadius > mMaxRadius){
            mRadius = mMaxRadius;
            mCenterX = mLastCenterX;
            mCenterY = mLastCenterY;
        }
    }

    /**
     *
     * @return
     */
    public int getmShapeType() {
        return mShapeType;
    }

    /**
     * 圆位于要截取的图片内
     */
    private void checkCircleBounds() {
        if (mCenterY - mRadius < mShiftBorderTop || mCenterY + mRadius > mShiftBorderBottom) {
            mCenterY = mLastCenterY;
            mRadius = mLastRadius;
        }
        if (mCenterX - mRadius < mShiftBorderLeft || mCenterX + mRadius > mShiftBorderRight) {
            mCenterX = mLastCenterX;
            mRadius = mLastRadius;
        }
    }
}
