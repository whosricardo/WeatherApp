package com.example.findinglogs.model.repo.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.findinglogs.provider.WeatherContract.WeatherEntry;

public class WeatherDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weather_cache.db";
    private static final int DATABASE_VERSION = 1;

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + WeatherEntry.TABLE_NAME + " ("
                + WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + WeatherEntry.COLUMN_CITY + " TEXT NOT NULL, "
                + WeatherEntry.COLUMN_TEMPERATURE + " TEXT, "
                + WeatherEntry.COLUMN_CONDITION + " TEXT, "
                + WeatherEntry.COLUMN_LATITUDE + " TEXT, "
                + WeatherEntry.COLUMN_LONGITUDE + " TEXT, "
                + WeatherEntry.COLUMN_UPDATED_AT + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(database);
    }
}
