package com.example.myweather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    RelativeLayout idhome;
    ProgressBar pbload;
    TextView cityName, tvTemp, tvCond;
    TextInputEditText edInCity;
    ImageView back, search, ivTemp;
    RecyclerView rvWeather;
    ArrayList<WeatherModel> weatherModelArrayList;
    WeatherAdapter weatherAdapter;
    LocationManager locationManager;
    private static final int PERMISSION_CODE = 1;

    String cityn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


        idhome = findViewById(R.id.idHome);
        pbload = findViewById(R.id.pbLoading);
        cityName = findViewById(R.id.tvCity);
        tvTemp = findViewById(R.id.tvTemperature);
        tvCond = findViewById(R.id.tvCondition);
        edInCity = findViewById(R.id.edIPCityName);
        back = findViewById(R.id.bgImage);
        search = findViewById(R.id.idSearchIv);
        ivTemp = findViewById(R.id.ivTemp);
        rvWeather = findViewById(R.id.rvWeather);

        weatherModelArrayList = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this, weatherModelArrayList);
        rvWeather.setAdapter(weatherAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Get location or request permissions
        getLocation();

        //Search button click
        search.setOnClickListener(v -> {
            String city = Objects.requireNonNull(edInCity.getText()).toString();
            if (city.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
            } else {
                cityName.setText(city);
                getWeatherInfo(city);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE);
            return;
        }

        if (!isLocationEnabled()) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location == null) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    cityn = getCity(location.getLongitude(), location.getLatitude());
                    getWeatherInfo(cityn);
                    locationManager.removeUpdates(this); // Stop updates once location is fetched
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(@NonNull String provider) {}

                @Override
                public void onProviderDisabled(@NonNull String provider) {}
            });
        } else {
            cityn = getCity(location.getLongitude(), location.getLatitude());
            getWeatherInfo(cityn);
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private String getCity(double longitude, double latitude) {
        String cityn = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);
            for (Address adr : addresses) {
                if (adr != null) {
                    String city = adr.getLocality();
                    if (city != null && !city.equals("")) {
                        cityn = city;
                        break;
                    } else {
                        Log.d("TAG", "CITY NOT FOUND");
                        Toast.makeText(this, "User City Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityn;
    }

    private void getWeatherInfo(String citynma) {
        String url = "http://api.weatherapi.com/v1/forecast.json?key=a69e60b3e1db473789565755242312&q=" + citynma + "&days=1&aqi=yes&alerts=yes";
        cityName.setText(citynma);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            pbload.setVisibility(View.GONE);
            idhome.setVisibility(View.VISIBLE);
            weatherModelArrayList.clear();
            try {
                String temperature = response.getJSONObject("current").getString("temp_c");
                tvTemp.setText(temperature+" °C");
                String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                Picasso.get().load("http:".concat(conditionIcon)).into(ivTemp);
                tvCond.setText(condition);

                JSONArray hourArr = response.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(0).getJSONArray("hour");
                for (int i = 0; i < hourArr.length(); i++) {
                    JSONObject hourObj = hourArr.getJSONObject(i);
                    String time = hourObj.getString("time");
                    String temp = hourObj.getString("temp_c");
                    String img = hourObj.getJSONObject("condition").getString("icon");
                    String wind = hourObj.getString("wind_kph");
                    weatherModelArrayList.add(new WeatherModel(time, temp, img, wind));
                }
                weatherAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Toast.makeText(MainActivity.this, "Please enter valid city name", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
