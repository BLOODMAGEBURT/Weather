package com.xu.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.qweather.sdk.bean.IndicesBean;
import com.qweather.sdk.bean.air.AirNowBean;
import com.qweather.sdk.bean.base.Code;
import com.qweather.sdk.bean.base.IndicesType;
import com.qweather.sdk.bean.base.Lang;
import com.qweather.sdk.bean.base.Unit;
import com.qweather.sdk.bean.weather.WeatherDailyBean;
import com.qweather.sdk.bean.weather.WeatherNowBean;
import com.qweather.sdk.view.QWeather;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView updateTime;
    private TextView degreeText;
    private TextView weatherText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ScrollView weatherLayout;
    private ImageView bingPic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        translucentStatusBar();
        setContentView(R.layout.activity_main);
        initView();
        init();

    }

    private void translucentStatusBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            Log.d(TAG, "verdion code :" + BuildConfig.VERSION_CODE);
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

        }
    }

    private void initView() {

        // ScrollView
        weatherLayout = findViewById(R.id.weather_layout);
        // 背景图片
        bingPic = findViewById(R.id.bing_pic_img);

        // 实时天气
        updateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherText = findViewById(R.id.weather_info_text);
        // 预报
        forecastLayout = findViewById(R.id.forecast_layout);
        // 空气质量
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);

        // 生活指数
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
    }

    private void init() {

        // 获取当前天气
        getNow();

        // 获取预报天气
        getForecast();

        // 获取空气质量
        getAqi();

        // 获取生活建议
        getSuggestion();

        // 加载图片
        loadPic();

    }

    private void loadPic() {
        Glide.with(MainActivity.this).load("https://cn.bing.com/th?id=OHR.MountSefton_ROW6091468010_1920x1080.jpg&rf=LaDigue_1920x1081920x1080.jpg")
                .into(bingPic);
    }

    private void getNow() {

        QWeather.getWeatherNow(MainActivity.this, "CN101010100", Lang.ZH_HANS, Unit.METRIC, new QWeather.OnResultWeatherNowListener() {


            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "getWeather onError: " + e);
            }

            @Override
            public void onSuccess(WeatherNowBean weatherBean) {
                Log.i(TAG, "getWeather onSuccess: " + new Gson().toJson(weatherBean));
                //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                if (Code.OK.getCode().equalsIgnoreCase(weatherBean.getCode())) {
                    WeatherNowBean.NowBaseBean now = weatherBean.getNow();

                    updateTime.setText(getSubUtilSimple(now.getObsTime(), "T(.*?)\\+"));
                    degreeText.setText(String.format("%s°C", now.getTemp()));
                    weatherText.setText(now.getText());
                } else {
                    //在此查看返回数据失败的原因
                    String status = weatherBean.getCode();
                    Code code = Code.toEnum(status);
                    Log.i(TAG, "failed code: " + code);
                }
            }
        });

    }

    private void getForecast() {
        QWeather.getWeather3D(MainActivity.this, "CN101010100", new QWeather.OnResultWeatherDailyListener() {
            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onSuccess(WeatherDailyBean weatherDailyBean) {
                Log.i(TAG, "getWeatherDaily onSuccess: " + new Gson().toJson(weatherDailyBean));
                if (Code.OK.getCode().equalsIgnoreCase(weatherDailyBean.getCode())) {
                    List<WeatherDailyBean.DailyBean> daily = weatherDailyBean.getDaily();
                    for (WeatherDailyBean.DailyBean dailyBean : daily) {
                        Log.d(TAG, dailyBean.getFxDate() + ":" + dailyBean.getTextDay());

                        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.forecast_item, forecastLayout, false);

                        TextView dateText = view.findViewById(R.id.date_text);
                        TextView infoText = view.findViewById(R.id.info_text);
                        TextView maxText = view.findViewById(R.id.max_text);
                        TextView minText = view.findViewById(R.id.min_text);

                        dateText.setText(dailyBean.getFxDate());
                        infoText.setText(dailyBean.getTextDay());
                        maxText.setText(String.format("%s°C", dailyBean.getTempMax()));
                        minText.setText(String.format("%s°C", dailyBean.getTempMin()));

                        forecastLayout.addView(view);
                    }
                }

            }
        });
    }

    private void getAqi() {
        QWeather.getAirNow(MainActivity.this, "CN101010100", Lang.ZH_HANS, new QWeather.OnResultAirNowListener() {
            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onSuccess(AirNowBean airNowBean) {
                if (Code.OK.getCode().equalsIgnoreCase(airNowBean.getCode())) {
                    Log.i(TAG, "getAirNow onSuccess: " + new Gson().toJson(airNowBean));
                    AirNowBean.NowBean airNowBeanNow = airNowBean.getNow();
                    aqiText.setText(airNowBeanNow.getAqi());
                    pm25Text.setText(airNowBeanNow.getPm2p5());
                }
            }
        });
    }

    private void getSuggestion() {
        ArrayList<IndicesType> indicesTypes = new ArrayList<>();
        indicesTypes.add(IndicesType.COMF);
        indicesTypes.add(IndicesType.CW);
        indicesTypes.add(IndicesType.SPT);
        QWeather.getIndices1D(MainActivity.this, "CN101010100", Lang.ZH_HANS, indicesTypes, new QWeather.OnResultIndicesListener() {
            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onSuccess(IndicesBean indicesBean) {
                if (Code.OK.getCode().equalsIgnoreCase(indicesBean.getCode())) {
                    Log.i(TAG, "getIndices1D onSuccess: " + new Gson().toJson(indicesBean));

                    List<IndicesBean.DailyBean> dailyList = indicesBean.getDailyList();
                    for (IndicesBean.DailyBean dailyBean : dailyList) {
                        String type = dailyBean.getType();
                        String text = dailyBean.getText();
                        if (type.equalsIgnoreCase("1")) {
                            // 运动指数
                            sportText.setText(String.format("运动建议： %s", text));
                        } else if (type.equalsIgnoreCase("2")) {
                            // 洗车指数
                            carWashText.setText(String.format("洗车建议： %s", text));
                        } else if (type.equalsIgnoreCase("8")) {
                            // 舒适度
                            comfortText.setText(String.format("舒适度： %s", text));
                        } else {
                            Log.i(TAG, "匹配不上");
                        }
                    }
                }
            }
        });
    }

    /**
     * 返回单个字符串，若匹配到多个的话就返回第一个，方法与getSubUtil一样
     *
     * @param soap
     * @param regex
     * @return
     */

    public static String getSubUtilSimple(String soap, String regex) {
        Pattern pattern = Pattern.compile(regex);// 匹配的模式
        Matcher m = pattern.matcher(soap);
        while (m.find()) {
            return m.group(1);
        }
        return "";
    }
}