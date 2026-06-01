package com.example.findinglogs.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.example.findinglogs.R;
import com.example.findinglogs.model.model.Weather;
import com.example.findinglogs.view.recyclerview.adapter.WeatherListAdapter;
import com.example.findinglogs.viewmodel.MainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel; // Foi preciso transformar a ViewModel em atributo de classe
    private WeatherListAdapter adapter;
    private final List<Weather> weathers = new ArrayList<>();
    private FloatingActionButton fetchButton;
    private FloatingActionButton openBrowserButton;
    private EditText citySearchEditText;
    private BroadcastReceiver connectivityReceiver;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainViewModel = new ViewModelProvider(this).get(
            MainViewModel.class
        );

        connectivityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Broadcast de conectividade recebido");
            }
        };

        RecyclerView recyclerView = findViewById(R.id.recycler_view_weather);
        fetchButton = findViewById(R.id.fetchButton);
        openBrowserButton = findViewById(R.id.openBrowserButton);
        citySearchEditText = findViewById(R.id.citySearchEditText);
        adapter = new WeatherListAdapter(this, weathers);
        recyclerView.setAdapter(adapter);
        mainViewModel
            .getWeatherList()
            .observe(this, weathers -> adapter.updateWeathers(weathers));

        fetchButton.setOnClickListener(view -> mainViewModel.refreshWeather());
        openBrowserButton.setOnClickListener(view -> openWeatherInBrowser());
        citySearchEditText.setOnEditorActionListener(
            (view, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    openWeatherInBrowser();
                    return true;
                }
                return false;
            }
        );
    }

    // Intent implícita: informamos a ação ACTION_VIEW e a URI.
    // O Android resolve qual app/componente consegue abrir esse conteúdo.
    private void openWeatherInBrowser() {
        String city = citySearchEditText.getText().toString().trim();
        String query = city.isEmpty() ? "weather" : "weather " + city;
        Uri uri = Uri.parse("https://www.google.com/search")
            .buildUpon()
            .appendQueryParameter("q", query)
            .build();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private boolean isDeviceConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        Network network = connectivityManager.getActiveNetwork();

        if (network == null) {
            return false;
        }

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);

        if (networkCapabilities == null) {
            return false;
        }

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");

        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, intentFilter);

        Log.d(TAG, "connectivity receiver registrado");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
