package com.example.findinglogs.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import com.example.findinglogs.BuildConfig;

public final class WeatherContract {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_WEATHER = "weather";

    private WeatherContract() {
    }

    public static final class WeatherEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_WEATHER)
                .build();
        public static final String TABLE_NAME = "weather_cache";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_TEMPERATURE = "temperature";
        public static final String COLUMN_CONDITION = "condition";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_UPDATED_AT = "updated_at";

        private WeatherEntry() {
        }
    }
}
