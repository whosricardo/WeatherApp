package com.example.findinglogs.worker;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.findinglogs.model.repo.Repository;

public class WeatherRefreshWorker extends Worker {

    private static final String TAG = "WeatherRefreshWorker";
    private static final String INPUT_SOURCE = "source";

    public WeatherRefreshWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Background weather refresh started");

        String source = getInputData().getString(INPUT_SOURCE);
        if (source != null) {
            Log.d(TAG, "Work requested by: " + source);
        }

        // The Worker executes background work; the repository owns the refresh logic.
        Application application = (Application) getApplicationContext();
        Repository repository = new Repository(application);
        Repository.RefreshResult refreshResult = repository.refreshWeather();

        switch (refreshResult.getStatus()) {
            case SUCCESS:
                Log.d(TAG, "Background weather refresh succeeded: "
                        + refreshResult.getWeatherItems().size() + " items refreshed");
                return Result.success();
            case RETRY:
                Log.d(TAG, "Background weather refresh retry requested: "
                        + refreshResult.getErrorMessage());
                return Result.retry();
            case FAILURE:
            default:
                Log.e(TAG, "Background weather refresh failed: "
                        + refreshResult.getErrorMessage());
                return Result.failure();
        }
    }
}
