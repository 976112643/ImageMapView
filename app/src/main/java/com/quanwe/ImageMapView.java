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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by WQ on 2017/9/8.
 * @Describe 图片地图控件
 * 结合可手势缩放的ImageView进行的扩展
 */

public class ImageMapView extends ImageView implements IScaleView {
    private final ScaleViewAttacher mAttacher;
    private ScaleType mPendingScaleType;
    private Paint markePaint;//点的画笔
    /**
     * 地图所在范围的经纬度
     */
    private float posStartX = 0, posStartY = 0, posEndX = 0, posEndY = 0;
    private  int ACCURACY=1;//精度
    /**
     * 点的集合
     */
    private List<ImageMark> pointList = new ArrayList<>();



    /**
     * 设置地图经纬度所在区域
     * @param latStart 起始经纬度(左上角)
     * @param lngStart
     * @param latEnd 结束经纬度(右下角)
     * @param lngEnd
     */
    public void setMapRange(float latStart, float lngStart, float latEnd, float lngEnd) {
        posStartY =latStart*ACCURACY;
        posStartX=lngStart*ACCURACY;
        posEndY=latEnd*ACCURACY;
        posEndX=lngEnd*ACCURACY;
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
    }

    //===================================================地图标记=====================

    /**
     * 添加标记点
     * @param imageMark 标记
     */
   public void  addImageMark(ImageMark imageMark){
       pointList.add(imageMark);
       invalidate();
   }

    /**
     * 移除标记点
     * @param imageMark
     */
    public void removeImageMark(ImageMark imageMark){
        pointList.remove(imageMark);
        invalidate();
    }

    /**
     * 清除所有标记
     */
    public void clearAllImageMark(){
        pointList.clear();
        invalidate();
    }

    /**
     * 获取所有标记
     * @return
     */
    public List<ImageMark> getImageMarks(){
        return pointList;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable drawable = getDrawable();
        if (drawable != null) {
            RectF matrixRect = getMatrixRect(getImageMatrix());
            for (ImageMark point : pointList) {
                float xy[] = relativePostion( point.lng,point.lat);
                canvas.drawBitmap(point.markeBitmap, xy[0]+point.getXOfect(), xy[1]+point.getYOfect(), markePaint);
            }
            canvas.drawRect(matrixRect, markePaint);
        }
    }

    /**
     * 根据点的经纬度获取相对坐标
     * @param absX
     * @param absY
     * @return
     */
    private float[] relativePostion(float absX, float absY) {

        absX=Math.abs(posStartX-absX*ACCURACY);//换算成画布上的坐标
        absY=Math.abs(posStartY-absY*ACCURACY);
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

}