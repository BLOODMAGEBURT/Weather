package com.xu.weather;

import android.app.Application;

import com.qweather.sdk.view.HeConfig;

/**
 * author : xujianbo
 * date : 2/5/21 2:53 PM
 * description :
 */
public class WeatherApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化和风天气
        HeConfig.init("HE2102051431101772", "bf2c4c8148bc4987ac6b9c316b9ca529");
        // 切换到开发版
        HeConfig.switchToDevService();
    }
}
