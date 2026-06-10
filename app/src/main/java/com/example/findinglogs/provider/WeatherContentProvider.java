package com.example.findinglogs.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.findinglogs.model.repo.local.WeatherCacheDataSource;
import com.example.findinglogs.provider.WeatherContract.WeatherEntry;

public class WeatherContentProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_ID = 101;

    private static final UriMatcher URI_MATCHER = buildUriMatcher();

    private WeatherCacheDataSource cacheDataSource;

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(WeatherContract.AUTHORITY, WeatherContract.PATH_WEATHER, WEATHER);
        matcher.addURI(WeatherContract.AUTHORITY, WeatherContract.PATH_WEATHER + "/#", WEATHER_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        if (getContext() == null) {
            return false;
        }
        cacheDataSource = new WeatherCacheDataSource(getContext());
        return true;
    }

    /*
     * This provider exposes only SQLite cache rows. WorkManager may trigger a
     * repository refresh, but provider queries never perform network requests.
     */
    @Nullable
    @Override
    public Cursor query(
            @NonNull Uri uri,
            @Nullable String[] projection,
            @Nullable String selection,
            @Nullable String[] selectionArgs,
            @Nullable String sortOrder) {
        Cursor cursor;

        switch (URI_MATCHER.match(uri)) {
            case WEATHER:
                cursor = cacheDataSource.queryWeather(
                        projection, selection, selectionArgs, sortOrder);
                break;
            case WEATHER_ID:
                cursor = cacheDataSource.queryWeatherById(
                        Long.parseLong(uri.getLastPathSegment()), projection);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case WEATHER:
                return "vnd.android.cursor.dir/vnd." + WeatherContract.AUTHORITY + ".weather";
            case WEATHER_ID:
                return "vnd.android.cursor.item/vnd." + WeatherContract.AUTHORITY + ".weather";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("Read-only provider");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Read-only provider");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Read-only provider");
    }
}
