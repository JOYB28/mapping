package kr.ac.kaist.mapping.mapping;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class GpsInfo implements LocationListener {

  private static GpsInfo instance;

  private static final LatLng INITIAL_LOCATION = new LatLng(36.374179, 127.365684);

  private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
  private static final long MIN_TIME_BW_UPDATES = 60000;

  private Location location;
  private Context context;

  private LocationManager locationManager;

  private GpsInfo(Context context) {
    this.context = context;
    locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
    try {
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
          MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
      locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
          MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
    } catch (SecurityException e) {
      e.printStackTrace();
    }
  }

  public static synchronized GpsInfo getInstance(Context context) {
    if (instance == null) {
      instance = new GpsInfo(context);
    }
    return instance;
  }

  /**
   * Returns current latitude.
   *
   * @return latitude
   */
  public LatLng getLocation() {
    updateLocation();
    if (location == null) {
      return INITIAL_LOCATION;
    }
    return new LatLng(location.getLatitude(), location.getLongitude());
  }

  /**
   * Fetches current location
   */
  private void updateLocation() {
    boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    if (isGpsEnabled || isNetworkEnabled) {
      location = getLastKnownLocation();
    } else {
      showSettingsAlert();
    }
  }

  private void showSettingsAlert() {
    new AlertDialog.Builder(context)
        .setTitle(context.getString(R.string.gps_disable_warning_title))
        .setMessage(context.getString(R.string.gps_disable_warning_msg))
        .setPositiveButton(context.getString(R.string.gps_disable_warning_confirm),
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        })
        .setCancelable(false)
        .show();
  }

  private Location getLastKnownLocation() {
    locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
    List<String> providers = locationManager.getProviders(true);
    Location bestLocation = null;
    for (String provider : providers) {
      try {
        Location l = locationManager.getLastKnownLocation(provider);
        if (l == null) {
          continue;
        }
        if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
          // Found best last known location: %s", l);
          bestLocation = l;
        }
      } catch (SecurityException e) {
        e.printStackTrace();
      }
    }
    return bestLocation;
  }

  public void onLocationChanged(Location location) {}

  public void onStatusChanged(String provider, int status, Bundle extras) {}

  public void onProviderEnabled(String provider) {}

  public void onProviderDisabled(String provider) {}
}
