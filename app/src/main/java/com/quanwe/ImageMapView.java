/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.quanwe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;


import java.util.ArrayList;
import java.util.List;


/**
 * Created by WQ on 2017/9/8.
 *
 * @Describe 图片地图控件
 * 结合可手势缩放的ImageView进行的扩展
 */

@SuppressLint("AppCompatCustomView")
public class ImageMapView extends ImageView implements IScaleView {
    private final ScaleViewAttacher mAttacher;
    private ScaleType mPendingScaleType;
    private Paint markePaint;//点的画笔
    /**
     * 地图所在范围的经纬度
     */
    private float posStartX = 0, posStartY = 0, posEndX = 0, posEndY = 0;
    private int ACCURACY = 1;//精度
    private  OnMarkClickListener onMarkClickListener;//标记点击监听
    private float touchX, touchY;
    /**
     * 标记的集合
     */
    private List<ImageMark> pointList = new ArrayList<>();

    private  boolean isDebug = false;//debug模式,绘制标记物及触摸的范围
    private int touchAbleWidth = 0;//触摸检测的范围

    /**
     * 设置地图经纬度所在区域, 坐标系为普通坐标系时,所有的lat对应y lng对应x
     *
     * @param latStart 起始经纬度(左上角)
     * @param lngStart
     * @param latEnd   结束经纬度(右下角)
     * @param lngEnd
     */
    public void setMapRange(float latStart, float lngStart, float latEnd, float lngEnd) {
        posStartY = latStart * ACCURACY;
        posStartX = lngStart * ACCURACY;
        posEndY = latEnd * ACCURACY;
        posEndX = lngEnd * ACCURACY;
    }

    public ImageMapView(Context context) {
        this(context, null);
        setZoomable(false);
    }

    public ImageMapView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public ImageMapView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        super.setScaleType(ScaleType.MATRIX);
        mAttacher = new ScaleViewAttacher(this);
        if (null != mPendingScaleType) {
            setScaleType(mPendingScaleType);
            mPendingScaleType = null;
        }
        markePaint = new Paint();
        markePaint.setStyle(Paint.Style.STROKE);
        markePaint.setColor(Color.RED);
        touchAbleWidth = dp2px( 15);
    }

    //===================================================地图标记=====================

    /**
     * 添加标记点
     *
     * @param imageMark 标记
     */
    public void addImageMark(ImageMark imageMark) {
        pointList.add(imageMark);
        invalidate();
    }

    /**
     * 移除标记点
     *
     * @param imageMark
     */
    public void removeImageMark(ImageMark imageMark) {
        pointList.remove(imageMark);
        invalidate();
    }

    /**
     * 清除所有标记
     */
    public void clearAllImageMark() {
        pointList.clear();
        invalidate();
    }

    /**
     * 获取所有标记
     *
     * @return
     */
    public List<ImageMark> getImageMarks() {
        return pointList;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable drawable = getDrawable();
        if (drawable != null) {
            RectF matrixRect = getMatrixRect(getImageMatrix());
            for (ImageMark point : pointList) {
                float xy[] = relativePostion(point.lng, point.lat);
                canvas.drawBitmap(point.markeBitmap, xy[0] + point.getXOfect(), xy[1] + point.getYOfect(), markePaint);

                if (isDebug) {
                    //测试代码
                    Bitmap markeBitmap = point.markeBitmap;
                    RectF mDisplayRect = new RectF();
                    float left = xy[0] + point.getXOfect();
                    float top = xy[1] + point.getYOfect();
                    mDisplayRect.set(left, top, left + markeBitmap.getWidth(), top + markeBitmap.getHeight());
                    canvas.drawRect(mDisplayRect, markePaint);
                }
            }
            if (isDebug) {
                canvas.drawRect(matrixRect, markePaint);
            }
        }
        if (isDebug) {
            if (touchY != 0) {
                RectF mDisplayRect = new RectF();
                mDisplayRect.set(touchX - touchAbleWidth, touchY - touchAbleWidth, touchX + touchAbleWidth, touchY + touchAbleWidth);
                canvas.drawRect(mDisplayRect, markePaint);
            }
        }
    }

    /**
     * 根据点的经纬度获取相对坐标
     *
     * @param absX
     * @param absY
     * @return
     */
    private float[] relativePostion(float absX, float absY) {

        absX = Math.abs(posStartX - absX * ACCURACY);//换算成画布上的坐标
        absY = Math.abs(posStartY - absY * ACCURACY);
        //根据缩放比,计算点的实际位置坐标
        RectF matrixRect = getMatrixRect(getImageMatrix());
        float width = Math.abs(posEndX - posStartX);
        float height = Math.abs(posEndY - posStartY);
        float xScale = matrixRect.width() / width;
        float yScale = matrixRect.height() / height;
        float relativeX = matrixRect.left + absX * xScale;
        float relativeY = matrixRect.top + absY * yScale;
        return new float[]{relativeX, relativeY};
    }

    RectF tapRect = new RectF();

    /**
     * 设置标记物点击监听
     * @param onMarkClickListener
     */
    public void setOnMarkClickListener(final OnMarkClickListener onMarkClickListener) {
        this.onMarkClickListener = onMarkClickListener;
        setOnViewTapListener(new ScaleViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                tapRect.set(x - touchAbleWidth, y - touchAbleWidth, x + touchAbleWidth, y + touchAbleWidth);

                touchX = x;
                touchY = y;
                invalidate();
                RectF pointRect = new RectF();
                for (ImageMark imageMark : pointList) {
                    float xy[] = relativePostion(imageMark.lng, imageMark.lat);
                    float left = xy[0] + imageMark.getXOfect();
                    float top = xy[1] + imageMark.getYOfect();
                    Bitmap markeBitmap = imageMark.markeBitmap;
                    pointRect.set(left, top, left + markeBitmap.getWidth(), top + markeBitmap.getHeight());


                    if (checkRectCollsion(tapRect.left, tapRect.top, tapRect.width(), tapRect.height()
                            , pointRect.left, pointRect.top, pointRect.width(), pointRect.height()
                    )) {
                        onMarkClickListener.onMarkClick(imageMark);
                        break;
                    }
                }
            }
        });
    }

    //碰撞检测
    public boolean checkRectCollsion(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2) {
        if (x1 >= x2 && x1 >= x2 + w2) {
            return false;
        } else if (x1 <= x2 && x1 + w1 <= x2) {
            return false;
        } else if (y1 >= y2 && y1 >= y2 + h2) {
            return false;
        } else if (y1 <= y2 && y1 + h1 <= y2) {
            return false;
        }
        return true;
    }
    //===============================缩放相关========================================

    public void setOnClickListener(OnClickListener listener) {
        mAttacher.setOnClickLinstener(listener);
    }

    @Override
    public boolean canZoom() {
        return mAttacher.canZoom();
    }

    @Override
    public RectF getDisplayRect() {
        return mAttacher.getDisplayRect();
    }

    @Override
    public float getMinScale() {
        return mAttacher.getMinScale();
    }

    @Override
    public float getMidScale() {
        return mAttacher.getMidScale();
    }

    @Override
    public float getMaxScale() {
        return mAttacher.getMaxScale();
    }

    @Override
    public float getScale() {
        return mAttacher.getScale();
    }

    @Override
    public ScaleType getScaleType() {
        return mAttacher.getScaleType();
    }


    public RectF getMatrixRect(Matrix matrix) {
        RectF mDisplayRect = new RectF();
        Drawable d = getDrawable();
        if (null != d) {
            mDisplayRect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(mDisplayRect);
            return mDisplayRect;
        }
        return mDisplayRect;
    }

    @Override
    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAttacher.setAllowParentInterceptOnEdge(allow);
    }

    @Override
    public void setMinScale(float minScale) {
        mAttacher.setMinScale(minScale);
    }

    @Override
    public void setMidScale(float midScale) {
        mAttacher.setMidScale(midScale);
    }

    @Override
    public void setMaxScale(float maxScale) {
        mAttacher.setMaxScale(maxScale);
    }

    @Override
    // setImageBitmap calls through to this method
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (null != mAttacher) {
            mAttacher.update();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (null != mAttacher) {
            mAttacher.update();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (null != mAttacher) {
            mAttacher.update();
        }
    }

    @Override
    public void setOnMatrixChangeListener(ScaleViewAttacher.OnMatrixChangedListener listener) {
        mAttacher.setOnMatrixChangeListener(listener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mAttacher.setOnLongClickListener(l);
    }

    @Override
    public void setOnScaleTapListener(ScaleViewAttacher.OnScaleTapListener listener) {
        mAttacher.setOnScaleTapListener(listener);
    }

    @Override
    public void setOnViewTapListener(ScaleViewAttacher.OnViewTapListener listener) {
        mAttacher.setOnViewTapListener(listener);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (null != mAttacher) {
            mAttacher.setScaleType(scaleType);
        } else {
            mPendingScaleType = scaleType;
        }
    }

    @Override
    public void setZoomable(boolean zoomable) {
        mAttacher.setZoomable(zoomable);
    }

    @Override
    public void zoomTo(float scale, float focalX, float focalY) {
        mAttacher.zoomTo(scale, focalX, focalY);
    }

    @Override
    protected void onDetachedFromWindow() {
        mAttacher.cleanup();
        super.onDetachedFromWindow();
    }
    /**
     * dp转px
     *
     * @param dp
     * @return
     */
    public  int dp2px( int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
    public interface OnMarkClickListener {
        void onMarkClick(ImageMark imageMark);
    }

}