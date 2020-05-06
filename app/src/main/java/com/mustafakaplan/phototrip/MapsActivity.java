package com.mustafakaplan.phototrip;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener
{
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    String locationLatitude;
    String  locationLongitude;
    String locationAddress;

    SupportMapFragment mapFragment;
    //SearchView searchView;

    PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        /*searchView = findViewById(R.id.sv_location);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;

                if(location != null || !location.equals(""))
                {
                    Geocoder geocoder = new Geocoder(MapsActivity.this);

                    try
                    {
                        addressList = geocoder.getFromLocationName(location,1);

                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                    }
                    catch (IOException e)
                    {
                        e.getLocalizedMessage();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                return false;
            }


        });*/

        String apiKey = "AIzaSyCArh3iYb0-1ZlsfZSw7Wx907Cmr1uwrTI";

        if (!Places.isInitialized())
        {
            Places.initialize(getApplicationContext(),apiKey);
        }

        try {
            placesClient = Places.createClient(this);

            final AutocompleteSupportFragment autocompleteSupportFragment =
                    (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

            autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
            placesClient = Places.createClient(this);
        }
        catch (Exception e)
        {
            e.getLocalizedMessage();
        }


        final AutocompleteSupportFragment autocompleteSupportFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                final LatLng latLng = place.getLatLng();

                String address = place.getName();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

                if(locationLongitude == null) // Paylaşılan fotoğrafın konumuna bakılmıyorsa
                {
                    if(UploadActivity.location != null)
                    {
                        mMap.clear();
                    }

                    mMap.addMarker(new MarkerOptions().title(address).position(latLng));

                    UploadActivity.address = address;
                    UploadActivity.location = latLng;
                    UploadActivity.locationText.setText(address);

                    UploadActivity.locationSwitch.setVisibility(View.VISIBLE);
                    UploadActivity.locationSwitch.setChecked(true);

                    Toast.makeText(getApplicationContext(),"Konum Eklendi",Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.clear();

        mMap.setOnMapLongClickListener(this); // Harita ve longClickListener Bağlanıyor

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        Intent intent = getIntent();

        locationLatitude = intent.getStringExtra("locationLatitude");
        locationLongitude = intent.getStringExtra("locationLongitude");
        locationAddress = intent.getStringExtra("locationAddress");

        locationListener = new LocationListener()
        {

            @Override
            public void onLocationChanged(Location location)
            {
                if(locationLatitude == null)
                {
                    LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if(Build.VERSION.SDK_INT >= 23)
        {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)//İzin Yoksa
            {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1); // İzin İste
            }

            else // İzin Varsa
            {   // Lokasyonu Almaya Başla
                locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER,2000,20,locationListener);

                mMap.clear();

                Location lastLocation = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);

                if(lastLocation != null && locationLatitude == null)
                {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                }

                if(UploadActivity.location != null)
                {
                    mMap.addMarker(new MarkerOptions().title(UploadActivity.address).position(UploadActivity.location));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UploadActivity.location,15));
                }

                if(locationLatitude != null)
                {
                    LatLng tripLocation = new LatLng(Double.parseDouble(locationLatitude), Double.parseDouble(locationLongitude));
                    mMap.addMarker(new MarkerOptions().title(locationAddress).position(tripLocation));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tripLocation,15));
                }

            }
        }

        else // 23'ten Küçükse İzin İstemeye Gerek Yok
        {
            locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER,2000,20,locationListener);

            mMap.clear();

            Location lastLocation = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);

            if(lastLocation != null && locationLatitude == null)
            {
                LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
            }

            if(UploadActivity.location != null)
            {
                mMap.addMarker(new MarkerOptions().title(UploadActivity.address).position(UploadActivity.location));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UploadActivity.location,15));
            }

            if(locationLatitude != null)
            {
                LatLng tripLocation = new LatLng(Double.parseDouble(locationLatitude), Double.parseDouble(locationLongitude));
                mMap.addMarker(new MarkerOptions().title(locationAddress).position(tripLocation));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tripLocation,15));
            }
        }
    }


    @Override
    public void onMapLongClick(LatLng latLng)
    {
        if(locationLongitude == null)
        {
            if(UploadActivity.location != null)
            {
                mMap.clear();
            }

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

            String address = "";

            try
            {
                List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);

                if(addressList != null && addressList.size() > 0)
                {
                    if(addressList.get(0).getSubAdminArea() != null) // İlçe
                    {
                        address += addressList.get(0).getSubAdminArea();
                    }

                    if(addressList.get(0).getAdminArea() != null) // Şehir
                    {
                        address += " "  + addressList.get(0).getAdminArea();
                    }

                    if(addressList.get(0).getCountryName() != null) // Ülke
                    {
                        address += " " + addressList.get(0).getCountryName();
                    }
                }

                else
                {
                    address = "New Place";
                }
            }

            catch (IOException e)
            {
                e.printStackTrace();
            }

            finally
            {
                mMap.addMarker(new MarkerOptions().title(address).position(latLng));

                UploadActivity.address = address;
                UploadActivity.location = latLng;
                UploadActivity.locationText.setText(address);

                UploadActivity.locationSwitch.setVisibility(View.VISIBLE);
                UploadActivity.locationSwitch.setChecked(true);

                Toast.makeText(getApplicationContext(),"Konum Eklendi",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)  // İlk Kez İzin Verilince Yapılır
    {
        if(grantResults.length > 0) // Dizide İzin Varsa
        {
            if(requestCode == 1) // Verdiğimiz Kod Uyuşuyorsa
            {
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) //İzin Verilmişse
                { // Lokasyonu Almaya Başla
                    locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER,2000,20,locationListener);

                        mMap.clear();
                        Location lastLocation = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);

                        if(lastLocation != null && locationLongitude == null)
                        {
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                        }

                        if(UploadActivity.location != null)
                        {
                            mMap.addMarker(new MarkerOptions().title(UploadActivity.address).position(UploadActivity.location));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UploadActivity.location,15));
                        }

                        if(locationLongitude != null)
                        {
                            LatLng tripLocation = new LatLng(Double.parseDouble(locationLatitude), Double.parseDouble(locationLongitude));
                            mMap.addMarker(new MarkerOptions().title(locationAddress).position(tripLocation));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tripLocation,15));
                        }

                    else
                    {
                        mMap.clear();

                        lastLocation = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);

                        if(lastLocation != null && locationLongitude == null)
                        {
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                        }

                        if(UploadActivity.location != null)
                        {
                            mMap.addMarker(new MarkerOptions().title(UploadActivity.address).position(UploadActivity.location));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UploadActivity.location,15));
                        }

                        if(locationLongitude != null)
                        {
                            LatLng tripLocation = new LatLng(Double.parseDouble(locationLatitude), Double.parseDouble(locationLongitude));
                            mMap.addMarker(new MarkerOptions().title(locationAddress).position(tripLocation));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tripLocation,15));
                        }
                    }

                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
