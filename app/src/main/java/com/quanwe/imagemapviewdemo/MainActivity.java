package com.quanwe.imagemapviewdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.quanwe.ImageMapView;
import com.quanwe.ImageMark;
import com.quanwe.util.LocationUtil;

import static com.quanwe.ImageMark.createTestMark;

/**
 * 使用示例
 */
public class MainActivity extends AppCompatActivity {
    ImageMapView imgMapView;
    ImageMark currentImageMark=new ImageMark();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgMapView = (ImageMapView) findViewById(R.id.imgMapView);

        //以下需要配置你申请的高德key
        LocationUtil locationUtil=new LocationUtil(MainActivity.this);
        /**
         * 构建标记物并添加到地图上,用来指示当前位置  (这里换个颜色,区别下当前位置的点
         */
        currentImageMark=createTestMark(0,0,getAlphaBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_test_marke), Color.RED));
        imgMapView.addImageMark(currentImageMark);
        locationUtil.initOnceLocation(new LocationUtil.LocationChange() {
            @Override
            public void locationChanged(AMapLocation amapLocation) {
                if(amapLocation!=null){
                    //更新当前位置经纬度
                    currentImageMark.lat = (float) amapLocation.getLatitude();
                    currentImageMark.lng = (float) amapLocation.getLongitude();
                    imgMapView.invalidate();//刷新界面
                }else {
                    Toast.makeText(MainActivity.this, "请检查位置权限是否正常开启", Toast.LENGTH_SHORT).show();
                }
            }
        });
        locationUtil.getLocation(true);

//        生成一些测试点
        initTestData();
    }

    /**
     * 测试数据
     */
    void initTestData(){
        //测试用地图区域
        float latStart=30.5378686253f;
        float lngStart=114.3372917175f;
        float latEnd=30.4739760743f;
        float lngEnd=114.4658660889f;
        imgMapView.setMapRange(latStart, lngStart, latEnd, lngEnd);//设置地图图片对应的边界
        Bitmap markeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_test_marke);
        for (int i = 0; i < 2; i++) {
            float lat = (float) (latStart + Math.random() * (latEnd - latStart));//随机一些坐标
            float lng = (float) (lngStart + Math.random() * (lngEnd - lngStart));
            imgMapView.addImageMark(createTestMark(lat,lng,markeBitmap));
        }
    }


    /**
     * 修改位图颜色
     * @param mBitmap
     * @param mColor
     * @return
     */
    public static Bitmap getAlphaBitmap(Bitmap mBitmap, int mColor) {
        Bitmap mAlphaBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(mAlphaBitmap);
        Paint mPaint = new Paint();
        mPaint.setColor(mColor);
        //从原位图中提取只包含alpha的位图
        Bitmap alphaBitmap = mBitmap.extractAlpha();
        //在画布上（mAlphaBitmap）绘制alpha位图
        mCanvas.drawBitmap(alphaBitmap, 0, 0, mPaint);
        return mAlphaBitmap;
    }
}
