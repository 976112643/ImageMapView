package com.quanwe;

import android.graphics.Bitmap;

/**
 * 地图标记物实体类
 * Created by WQ on 2017/8/30.
 */

public class ImageMark {
    /**
     * 经纬度,偏移量
     */
    public float lat, lng,xOffect,yOffect;
    /**
     * 附加参数
     */
    public Object data;
    public Bitmap markeBitmap;
    public float getXOfect(){
        return xOffect;
    }
    public float getYOfect(){
        return yOffect;
    }

    public static ImageMark createMark(double lat,double lng,Bitmap markeBitmap){
        return createMark(lat, lng, markeBitmap,0,0);
    }
    public static ImageMark createMark(double lat,double lng,Bitmap markeBitmap,float xOffect,float yOffect){
        ImageMark mark = new ImageMark();
        mark.lat= (float) lat;
        mark.lng= (float) lng;
        mark.xOffect=xOffect;
        mark.yOffect=yOffect;
        mark.markeBitmap=markeBitmap;
        return mark;
    }

    public static ImageMark createTestMark(double lat,double lng,Bitmap markeBitmap){
        //这里对demo中的标记物的偏移量做了设置,具体使用时根据位置标记物的图片来设置
        return createMark(lat, lng, markeBitmap,-markeBitmap.getWidth()*(31f/87f),-markeBitmap.getHeight());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
       return hashCode()==String.valueOf(obj).hashCode();
    }
    @Override
    public String toString() {
        return "ImageMark{" +
                "lat=" + lat +
                ", lng=" + lng +
                ", xOffect=" + xOffect +
                ", yOffect=" + yOffect +
                '}';
    }
}
