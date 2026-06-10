package com.example.findinglogs.model.repo.remote;


import androidx.annotation.NonNull;

import com.example.findinglogs.BuildConfig;
import com.example.findinglogs.model.model.Weather;
import com.example.findinglogs.model.repo.remote.api.ServicesInterfaceWrapper;
import com.example.findinglogs.model.repo.remote.api.WeatherCallback;
import com.example.findinglogs.model.util.Logger;

import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class WeatherManager {
    private static final String TAG = WeatherManager.class.getSimpleName();

    public WeatherManager() {
        if (Logger.ISLOGABLE) Logger.d(TAG, "WeatherManager()");
    }

    public void retrieveForecast(String localization, WeatherCallback callback) {
        if (Logger.ISLOGABLE) Logger.d(TAG, "retrieveForecast()");
        createWeatherCall(localization).enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<Weather> call,
                                   @NonNull Response<Weather> resp) {
                if (resp.isSuccessful() && resp.code() == HttpsURLConnection.HTTP_OK) {
                    assert resp.body() != null;
                    callback.onSuccess(resp.body());
                } else {
                    if (Logger.ISLOGABLE)
                        Logger.w(TAG, "getForecast: status:" + resp.code());
                    callback.onFailure(String.valueOf(resp.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Weather> call,
                                  @NonNull Throwable throwable) {
                callback.onFailure(String.valueOf(throwable.getMessage()));
            }
        });
    }

    public Weather retrieveForecastSynchronously(String localization)
            throws IOException, HttpException {
        if (Logger.ISLOGABLE) Logger.d(TAG, "retrieveForecastSynchronously()");
        Response<Weather> response = createWeatherCall(localization).execute();

        if (!response.isSuccessful()) {
            throw new HttpException(response);
        }

        Weather weather = response.body();
        if (weather == null) {
            throw new IOException("Weather response body was empty");
        }

        return weather;
    }

    private Call<Weather> createWeatherCall(String localization) {
        String apiKey = BuildConfig.WEATHER_API_KEY;
        String[] split = localization.split(",");
        String lat = split[0];
        String lon = split[1];

        return ConnectionManager.getInstance()
                .getWeatherConnection()
                .create(ServicesInterfaceWrapper.WeatherService.class)
                .getWeather(lat, lon, apiKey);
    }
}
