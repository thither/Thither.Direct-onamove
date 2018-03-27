package direct.thither.onamove.receivers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import direct.thither.onamove.App;
import direct.thither.onamove.properties.PropsHolder;


public class LocationReceiver extends BroadcastReceiver implements LocationListener {
    private PropsHolder m_props;

    protected LocationManager locationManager;
    Location m_location = null; // Location
    private Context mCtx;

    double latitude; // Latitude
    double longitude; // Longitude

    public LocationReceiver() {
        m_props = App.getInstance().props;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mCtx = context;
        //Toast.makeText(mCtx, "Loc Receiver", Toast.LENGTH_SHORT).show();
        if (ContextCompat.checkSelfPermission(mCtx, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(mCtx, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
         {
             ActivityCompat.requestPermissions((Activity)mCtx,new String[]{
                     android.Manifest.permission.ACCESS_FINE_LOCATION,
                     android.Manifest.permission.ACCESS_COARSE_LOCATION
             }, 1);
             return;
        }

        locationManager = (LocationManager) mCtx.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String mprovider = locationManager.getBestProvider(criteria, false);
        //Toast.makeText(mCtx, "Checking location data availability: "+mprovider, Toast.LENGTH_SHORT).show();
        if (!is_gps_enabled() && !is_inet_enabled()) {
            m_props.set_location(null);
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mCtx);
            alertDialog.setTitle("Location settings");
            alertDialog.setMessage("Location data is not enabled. " +
                    "Do you want to go to settings menu?");

            // On pressing the Settings button.
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mCtx.startActivity(intent);
                }
            });
            // On pressing the cancel button
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(mCtx, "Location resolution has stopped! ", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                }
            });
            alertDialog.show();
        } else {
            set_location();
            set_coordinates();
            //Toast.makeText(mCtx, "Location data is available!", Toast.LENGTH_SHORT).show();
            //        "Your Location is - \nLat: " + latitude + "\n" +
            //        "Long: " + longitude, Toast.LENGTH_LONG).show();
            m_props.set_location(this);
        }
    }
    public void set_location() {
        if(is_gps_enabled()){
            set_gps_location();
        }
        if (m_location==null && is_inet_enabled()){
            set_inet_location();
        }

    }
    public boolean is_gps_enabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    public boolean is_inet_enabled() {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void set_gps_location() {
        if(locationManager != null)
            locationManager.removeUpdates(this);
        ContextCompat.checkSelfPermission(mCtx, android.Manifest.permission.ACCESS_FINE_LOCATION);
        ContextCompat.checkSelfPermission(mCtx, android.Manifest.permission.ACCESS_COARSE_LOCATION);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, m_props.update_freq,
                m_props.get_rad_distance_for_update(), this);
        Location new_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(new_loc!=null) m_location = new_loc;
    }

    public void set_inet_location() {
        if(locationManager != null)
            locationManager.removeUpdates(this);
        ContextCompat.checkSelfPermission(mCtx, android.Manifest.permission.ACCESS_FINE_LOCATION);
        ContextCompat.checkSelfPermission(mCtx, android.Manifest.permission.ACCESS_COARSE_LOCATION);

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, m_props.update_freq,
                Long.parseLong(Float.toString(m_props.get_rad_distance_for_update())), this);
        Location new_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(new_loc!=null) m_location = new_loc;
    }

    @Override
    public void onLocationChanged(Location location) {
        m_location = location;
        set_coordinates();
    }
    private void set_coordinates(){
        if(m_location==null) return;

        latitude = Math.round(m_location.getLatitude()*100000.0d)/100000.0d;
        longitude = Math.round(m_location.getLongitude()*100000.0d)/100000.0d;
        m_props.params.get("main").set_param("lat", Double.toString(latitude));
        m_props.params.get("main").set_param("lng", Double.toString(longitude));
        //Toast.makeText(mCtx, "Your Location is - \nLat: " + latitude + "\n" +
        //        "Long: " + longitude, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onProviderDisabled(String provider) {
    }


    @Override
    public void onProviderEnabled(String provider) {
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                //Toast.makeText(mCtx, "Status Changed: Out of Service",
                  ///      Toast.LENGTH_SHORT).show();
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
               //Toast.makeText(mCtx, "Status Changed: Temporarily Unavailable",
                 //       Toast.LENGTH_SHORT).show();
                break;
            case LocationProvider.AVAILABLE:
               //Toast.makeText(mCtx, "Status Changed: Available",
               //         Toast.LENGTH_SHORT).show();
                break;
        }

    }

}
