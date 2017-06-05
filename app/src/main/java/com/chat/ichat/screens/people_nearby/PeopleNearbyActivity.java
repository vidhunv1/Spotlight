package com.chat.ichat.screens.people_nearby;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chat.ichat.R;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.location.UserLocation;
import com.chat.ichat.application.SpotlightApplication;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.UserSession;
import com.chat.ichat.screens.message.MessageActivity;
import com.chat.ichat.screens.new_chat.AddContactUseCase;
import com.chat.ichat.screens.sign_up.SignUpActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
public class PeopleNearbyActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, PeopleNearbyContract.View, PeopleNearbyAdapter.ContactClickListener{
    @Bind(R.id.tb)
    Toolbar toolbar;

    @Bind(R.id.ll_permission)
    LinearLayout permissionLayout;

    @Bind(R.id.rv_people_nearby)
    RecyclerView recyclerView;

    private GoogleApiClient googleApiClient;
    private LocationManager locationManager;
    private PeopleNearbyPresenter peopleNearbyPresenter;
    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    public static Intent callingIntent(Context context) {
        return new Intent(context, PeopleNearbyActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_nearby);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (hasLocationPermission()) { //has permission
            permissionLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            googleApiClient.connect();
        } else {
            permissionLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        peopleNearbyPresenter = new PeopleNearbyPresenter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        peopleNearbyPresenter.detachView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        peopleNearbyPresenter.attachView(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.people_nearby_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if ((id == android.R.id.home)) {
            super.onBackPressed();
            finish();
            return true;
        } else if(id == R.id.action_clear_location) {
            peopleNearbyPresenter.clearLocation();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        super.onBackPressed();
    }

    @OnClick(R.id.btn_allow)
    public void onAllowClicked() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    permissionLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    googleApiClient.connect();
                } else {
                    //not granted
                    permissionLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasLocationPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat
                        .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED);
    }

    public Observable<Location> fetchCurrentLocation() {
        return Observable
                .create((Observable.OnSubscribe<android.location.Location>) subscriber -> {
                    try {
                        if (!LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient)
                                .isLocationAvailable()) {
                            Timber.v("[FusedLocationApi] Last Known Location Unavailable");
                            subscriber.onNext(null);
                            subscriber.onCompleted();
                        } else {
                            Timber.v("[FusedLocationApi] Last Known Location Available");


                            android.location.Location location = LocationServices
                                    .FusedLocationApi
                                    .getLastLocation(googleApiClient);
                            Logger.d(this, "Location:: "+location.toString());
                            subscriber.onNext(location);
                            subscriber.onCompleted();
                        }
                    }
                    catch (Exception e) {
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Func1<android.location.Location, Observable<android.location.Location>>() {
                    @Override
                    public Observable<android.location.Location> call(android.location.Location location) {
                        if (location != null) {
                            return Observable.just(location);
                        }
                        else {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            if (location == null) {
                                Timber.v("[LocationManager] Last Known Location Unavailable");
                            }
                            else {
                                Timber.v("[LocationManager] Last Known Location Available");
                            }

                            return Observable.just(location);
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Func1<android.location.Location, Observable<android.location.Location>>() {
                    @Override
                    public Observable<android.location.Location> call(android.location.Location location) {
                        if (location != null) {
                            return Observable.just(location);
                        }

                        Timber.v("[FusedLocationApi] Refreshing Location ...");

                        return Observable
                                .create(subscriber -> {
                                    LocationRequest locationRequest = new LocationRequest();
                                    locationRequest
                                            .setPriority(LocationRequest
                                                    .PRIORITY_BALANCED_POWER_ACCURACY);

                                    LocationListener locationListener = location13 -> {
                                        Timber.v("[FusedLocationApi] Received Updated" +
                                                " Location");
                                        subscriber.onNext(location13);
                                        subscriber.onCompleted();
                                    };

                                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener, Looper.getMainLooper());
                                });
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(googleApiLocation -> {
                    Location location1 = new Location();
                    location1.setLongitude(googleApiLocation.getLongitude());
                    location1.setLatitude(googleApiLocation.getLatitude());

                    return location1;
                })
                .subscribeOn(Schedulers.newThread());
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Context context = this;
        Logger.d(this, "Connected location");
        progressDialog[0] = ProgressDialog.show(context, "", "loading location data", true);
        fetchCurrentLocation()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Location>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Location location) {
                        SharedPreferences sharedPreferences = SpotlightApplication.getContext().getSharedPreferences("last_known_location", Context.MODE_PRIVATE);
                        if(location!=null) {
                            peopleNearbyPresenter.getPeopleNearby(location.getLatitude(), location.getLongitude());
                            sharedPreferences.edit().putLong("latitude", Double.doubleToRawLongBits(location.getLatitude())).apply();
                            sharedPreferences.edit().putLong("longitude", Double.doubleToRawLongBits(location.getLongitude())).apply();
                            if(progressDialog[0].isShowing())
                                progressDialog[0].dismiss();
                            progressDialog[0] = ProgressDialog.show(context, "", "Searching...", true);
                        } else {
                            Logger.d(this, "Null Location: ");
                            double latitude = Double.longBitsToDouble(sharedPreferences.getLong("latitude", -99999999));
                            double longitude = Double.longBitsToDouble(sharedPreferences.getLong("longitude", -99999999));

                            if(latitude!=-99999999 && longitude!=-99999999) {
                                peopleNearbyPresenter.getPeopleNearby(latitude, longitude);
                            }
                            updateLocationNotAvailable();
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Logger.d(this, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //show google play services not available dialog.
        Logger.d(this, "Connected failed");
        updateLocationNotAvailable();
    }

    @Override
    public void showError(String title, String message) {

    }

    @Override
    public void updatePeopleNearby(List<UserLocation> nearbyPeopleResponseList) {
        progressDialog[0].dismiss();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        PeopleNearbyAdapter peopleNearbyAdapter = new PeopleNearbyAdapter(this, nearbyPeopleResponseList, this);
        recyclerView.setAdapter(peopleNearbyAdapter);
    }

    @Override
    public void updateLocationNotAvailable() {}

    @Override
    public void onLocationCleared() {
        SharedPreferences sharedPreferences = SpotlightApplication.getContext().getSharedPreferences("last_known_location", Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong("latitude", -99999999).apply();
        sharedPreferences.edit().putLong("longitude", -99999999).apply();
        finish();
    }

    @Override
    public void onContactItemClicked(String username, String userId) {
        progressDialog[0].dismiss();
        Context context = this;
        Activity activity = PeopleNearbyActivity.this;
        progressDialog[0] = ProgressDialog.show(context, "", "loading...", true);
        AddContactUseCase addContactUseCase = new AddContactUseCase(ApiManager.getUserApi(), ContactStore.getInstance(), ApiManager.getBotApi(), BotDetailsStore.getInstance());
        addContactUseCase.execute(userId, false)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ContactResult>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(ContactResult contactResult) {
                        progressDialog[0].dismiss();
                        activity.finish();
                        startActivity(MessageActivity.callingIntent(context, username));
                    }
                });
    }
}
