package com.wcp.cutoutpicture.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.wcp.cutoutpicture.R;
import com.wcp.cutoutpicture.util.BitmapUtil;

/**
 * Created by wcp on 2016/8/23.
 */
public class CutOutLayout extends RelativeLayout{
    private ImageView showPic;
    private MasklayerCircleView masklayerCircleView;
    private Context mContext;

    public CutOutLayout(Context context) {
        this(context,null);
    }

    public CutOutLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CutOutLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        showPic = (ImageView) findViewById(R.id.show_image);
        masklayerCircleView = (MasklayerCircleView)findViewById(R.id.mask_layer);
        super.onFinishInflate();
    }

    /**
     * 设置将要被截取的图片
     * @param bitmap
     */
    public void setShowPic(Bitmap bitmap) {
        Bitmap tempBitmap = BitmapUtil.compressBitmap(bitmap,(Activity) mContext);
        showPic.setImageBitmap(tempBitmap);
        masklayerCircleView.setShiftRange(showPic.getDrawable().getIntrinsicWidth(),showPic.getDrawable().getIntrinsicHeight());
    }

    /**
     * 获取指定位置的bitmap
     * @return
     */
    public Bitmap getCutOutBitmap() {
        masklayerCircleView.setVisibility(View.GONE);
       return BitmapUtil.toShapeBitmap(BitmapUtil.cutOutScreen((Activity) mContext),masklayerCircleView.getmCenterX()
                ,masklayerCircleView.getmCenterY(),masklayerCircleView.getmRadius(),masklayerCircleView.getmShapeType());
    }


}
