package com.example.findinglogs.model.repo.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.findinglogs.model.model.Weather;
import com.example.findinglogs.model.model.WeatherDetail;
import com.example.findinglogs.model.util.Utils;
import com.example.findinglogs.provider.WeatherContract.WeatherEntry;

import java.util.List;

public class WeatherCacheDataSource {

    private final Context context;
    private final WeatherDbHelper dbHelper;

    public WeatherCacheDataSource(Context context) {
        this.context = context.getApplicationContext();
        dbHelper = new WeatherDbHelper(this.context);
    }

    public void saveWeatherItems(List<Weather> weatherItems) {
        saveWeatherItems(weatherItems, null);
    }

    public void saveWeatherItems(List<Weather> weatherItems, List<String> localizations) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long updatedAt = System.currentTimeMillis();

        database.beginTransaction();
        try {
            database.delete(WeatherEntry.TABLE_NAME, null, null);

            for (int index = 0; index < weatherItems.size(); index++) {
                Weather weather = weatherItems.get(index);
                ContentValues values = new ContentValues();
                values.put(WeatherEntry._ID, index + 1L);
                values.put(WeatherEntry.COLUMN_CITY, getCity(weather));
                values.put(WeatherEntry.COLUMN_TEMPERATURE, getTemperature(weather));
                values.put(WeatherEntry.COLUMN_CONDITION, getCondition(weather));
                putCoordinates(values, localizations, index);
                values.put(WeatherEntry.COLUMN_UPDATED_AT, updatedAt);
                database.insertOrThrow(WeatherEntry.TABLE_NAME, null, values);
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        // Repository refreshes the cache; provider observers can react to new rows.
        context.getContentResolver().notifyChange(WeatherEntry.CONTENT_URI, null);
    }

    public Cursor queryWeather(
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        String order = sortOrder == null ? WeatherEntry._ID + " ASC" : sortOrder;
        return dbHelper.getReadableDatabase().query(
                WeatherEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                order);
    }

    public Cursor queryWeatherById(long id, String[] projection) {
        return queryWeather(
                projection,
                WeatherEntry._ID + " = ?",
                new String[] { String.valueOf(id) },
                null);
    }

    private void putCoordinates(
            ContentValues values, List<String> localizations, int index) {
        if (localizations == null || index >= localizations.size()) {
            values.putNull(WeatherEntry.COLUMN_LATITUDE);
            values.putNull(WeatherEntry.COLUMN_LONGITUDE);
            return;
        }

        String[] coordinates = localizations.get(index).split(",", 2);
        if (coordinates.length != 2) {
            values.putNull(WeatherEntry.COLUMN_LATITUDE);
            values.putNull(WeatherEntry.COLUMN_LONGITUDE);
            return;
        }

        values.put(WeatherEntry.COLUMN_LATITUDE, coordinates[0]);
        values.put(WeatherEntry.COLUMN_LONGITUDE, coordinates[1]);
    }

    private String getCity(Weather weather) {
        if (weather == null || weather.getName() == null || weather.getName().trim().isEmpty()) {
            return "Unknown city";
        }
        return weather.getName();
    }

    private String getTemperature(Weather weather) {
        if (weather == null || weather.getMain() == null) {
            return null;
        }
        return Utils.getCelsiusTemperatureFromKevin(weather.getMain().getTemp());
    }

    private String getCondition(Weather weather) {
        if (weather == null || weather.getWeather() == null || weather.getWeather().isEmpty()) {
            return null;
        }

        WeatherDetail detail = weather.getWeather().get(0);
        if (detail == null) {
            return null;
        }
        if (detail.getDescription() != null && !detail.getDescription().trim().isEmpty()) {
            return detail.getDescription();
        }
        return detail.getMain();
    }
}
