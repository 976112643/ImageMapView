package com.quanwe.util;

import android.content.Context;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

/**
 * 定位帮助类
 * Created by WQ on 2017/3/20.
 */

public class LocationUtil {
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;
    AMapLocation aMapLocation;
    Context mContext;

    public LocationUtil(Context mContext) {
        this.mContext = mContext;
    }

    public void initOnceLocation(AMapLocationListener listener){
        mlocationClient = new AMapLocationClient(mContext);
        mLocationOption = new AMapLocationClientOption();
        // 设置定位监听
        mlocationClient.setLocationListener(listener);
        // 设置为高精度定位模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setOnceLocation(true);
        mLocationOption.setNeedAddress(true);
        // 设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
    }
    public void initLocation(int Interval,AMapLocationListener listener){
        mlocationClient = new AMapLocationClient(mContext);
        mLocationOption = new AMapLocationClientOption();
        // 设置定位监听
        mlocationClient.setLocationListener(listener);
        // 设置为高精度定位模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        mLocationOption.setOnceLocation(false);
        mLocationOption.setNeedAddress(true);
        mLocationOption.setInterval(Interval);
        // 设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
    }
    public void getLocation(boolean hasDialog){
        if(hasDialog) {
            Toast.makeText(mContext, "正在获取位置信息", Toast.LENGTH_SHORT).show();
        }
        mlocationClient.startLocation();
    }
    public void stopLocation(boolean hasDialog){
        mlocationClient.stopLocation();
    }

    public static abstract class LocationChange implements AMapLocationListener {
        @Override
       final public void onLocationChanged(AMapLocation amapLocation) {

            if (amapLocation != null && amapLocation.getErrorCode() == 0) {
                locationChanged(amapLocation);
            } else {
                if(amapLocation!=null) {
                    if ( amapLocation.getErrorCode() == 12) {
                    }
                }
                locationChanged(null);
            }
        }
        public void  locationChanged(AMapLocation amapLocation){

        }
    }
}
