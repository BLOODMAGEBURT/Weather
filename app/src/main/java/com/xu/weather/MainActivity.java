package com.xu.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.qweather.sdk.bean.base.Code;
import com.qweather.sdk.bean.base.Lang;
import com.qweather.sdk.bean.base.Unit;
import com.qweather.sdk.bean.weather.WeatherNowBean;
import com.qweather.sdk.view.QWeather;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView updateTime;
    private TextView degreeText;
    private TextView weatherText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        init();

    }

    private void initView() {
        updateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherText = findViewById(R.id.weather_info_text);


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
                    degreeText.setText(now.getTemp());
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

    }

    private void getAqi() {

    }

    private void getSuggestion() {

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