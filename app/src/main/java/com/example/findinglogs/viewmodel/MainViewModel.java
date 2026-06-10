package com.example.findinglogs.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.findinglogs.model.model.Weather;
import com.example.findinglogs.model.repo.Repository;
import com.example.findinglogs.model.repo.remote.api.WeatherCallback;
import com.example.findinglogs.model.util.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();
    private static final int FETCH_INTERVAL = 120_000;
    private final Repository mRepository;
    private final MutableLiveData<List<Weather>> _weatherList =
        new MutableLiveData<>(new ArrayList<>());
    private final LiveData<List<Weather>> weatherList = _weatherList;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService refreshExecutor = Executors.newSingleThreadExecutor();
    private final Runnable fetchRunnable = this::fetchAllForecasts;
    private volatile boolean isCleared = false;

    public MainViewModel(Application application) {
        super(application);
        mRepository = new Repository(application);
        startFetching();
    }

    public LiveData<List<Weather>> getWeatherList() {
        return weatherList;
    }

    public void refreshWeather() {
        handler.removeCallbacks(fetchRunnable);
        fetchAllForecasts();
    }

    private void startFetching() {
        fetchAllForecasts();
    }

    private void fetchAllForecasts() {
        if (isCleared) {
            return;
        }

        if (Logger.ISLOGABLE) Logger.d(TAG, "fetchAllForecasts()");

        // The repository owns data refresh; this ViewModel owns UI state and timing.
        refreshExecutor.execute(() -> {
            Repository.RefreshResult result = mRepository.refreshWeather();

            if (isCleared) {
                return;
            }

            if (result.getStatus() == Repository.RefreshResult.Status.SUCCESS) {
                _weatherList.postValue(result.getWeatherItems());
            } else if (Logger.ISLOGABLE) {
                Logger.w(TAG, "Weather refresh failed: " + result.getErrorMessage());
            }

            if (!isCleared) {
                handler.postDelayed(fetchRunnable, FETCH_INTERVAL);
            }
        });
    }

    @Override
    protected void onCleared() {
        isCleared = true;
        handler.removeCallbacks(fetchRunnable);
        refreshExecutor.shutdownNow();
        super.onCleared();
    }

    public void retrieveForecast(String latLon, WeatherCallback callback) {
        mRepository.retrieveForecast(latLon, callback);
    }
}
