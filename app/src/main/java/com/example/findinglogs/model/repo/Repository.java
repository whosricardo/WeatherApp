package com.example.findinglogs.model.repo;


import android.app.Application;

import com.example.findinglogs.model.model.Weather;
import com.example.findinglogs.model.repo.local.SharedPrefManager;
import com.example.findinglogs.model.repo.local.WeatherCacheDataSource;
import com.example.findinglogs.model.repo.remote.WeatherManager;
import com.example.findinglogs.model.repo.remote.api.WeatherCallback;
import com.example.findinglogs.model.util.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.HttpException;

public class Repository {
    private static final String TAG = Repository.class.getSimpleName();
    private static final String LAST_WEATHER_REFRESH_TIMESTAMP =
            "last_weather_refresh_timestamp";

    private final WeatherManager weatherManager;
    private final SharedPrefManager sharedPrefManagerManager;
    private final WeatherCacheDataSource weatherCacheDataSource;

    public Repository(Application application) {
        if (Logger.ISLOGABLE) Logger.d(TAG, "Repository()");
        weatherManager = new WeatherManager();
        sharedPrefManagerManager = SharedPrefManager.getInstance(application);
        weatherCacheDataSource = new WeatherCacheDataSource(application);
    }

    public void retrieveForecast(String latLon, WeatherCallback callback) {
        if (Logger.ISLOGABLE) Logger.d(TAG, "retrieveForecast for:" + latLon);
        weatherManager.retrieveForecast(latLon, callback);
    }

    /*
     * Owns the data refresh. ViewModels decide how results affect UI state, while
     * Workers only map this result to WorkManager success, retry, or failure.
     */
    public RefreshResult refreshWeather() {
        List<Weather> refreshedWeather = new ArrayList<>();
        List<String> localizations = new ArrayList<>(getLocalizations().values());

        try {
            for (String latLon : localizations) {
                refreshedWeather.add(weatherManager.retrieveForecastSynchronously(latLon));
            }

            // Repository owns persistence; WorkManager and ViewModel share this path.
            weatherCacheDataSource.saveWeatherItems(refreshedWeather, localizations);
            saveString(LAST_WEATHER_REFRESH_TIMESTAMP,
                    String.valueOf(System.currentTimeMillis()));
            return RefreshResult.success(refreshedWeather);
        } catch (IOException exception) {
            return RefreshResult.retry(exception.getMessage());
        } catch (HttpException exception) {
            int statusCode = exception.code();
            String message = "HTTP " + statusCode;

            if (statusCode == 408 || statusCode == 429 || statusCode >= 500) {
                return RefreshResult.retry(message);
            }

            return RefreshResult.failure(message);
        } catch (RuntimeException exception) {
            return RefreshResult.failure(exception.getMessage());
        }
    }

    public void saveString(String key, String value) {
        if (Logger.ISLOGABLE) Logger.d(TAG, "saveString()");
        sharedPrefManagerManager.writeString(key, value);
    }

    public String readString(String key) {
        if (Logger.ISLOGABLE) Logger.d(TAG, "readString()");
        return sharedPrefManagerManager.readString(key);
    }

    public HashMap<String, String> getLocalizations() {
        HashMap<String, String> localizations = new HashMap<>();
        localizations.put("1", "-8.05428,-34.8813");
        localizations.put("2", "-9.39416,-40.5096");
        localizations.put("3", "-8.284547,-35.969863");
        localizations.put("4", "-9.6658,-35.7353");
        return localizations;
    }

    public static class RefreshResult {
        public enum Status {
            SUCCESS,
            RETRY,
            FAILURE
        }

        private final Status status;
        private final List<Weather> weatherItems;
        private final String errorMessage;

        private RefreshResult(Status status, List<Weather> weatherItems, String errorMessage) {
            this.status = status;
            this.weatherItems = weatherItems;
            this.errorMessage = errorMessage;
        }

        public static RefreshResult success(List<Weather> weatherItems) {
            return new RefreshResult(Status.SUCCESS, weatherItems, null);
        }

        public static RefreshResult retry(String errorMessage) {
            return new RefreshResult(Status.RETRY, new ArrayList<>(), errorMessage);
        }

        public static RefreshResult failure(String errorMessage) {
            return new RefreshResult(Status.FAILURE, new ArrayList<>(), errorMessage);
        }

        public Status getStatus() {
            return status;
        }

        public List<Weather> getWeatherItems() {
            return weatherItems;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
