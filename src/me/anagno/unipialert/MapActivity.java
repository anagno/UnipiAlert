package me.anagno.unipialert;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MapActivity extends Activity implements LocationListener
{
  
  protected MapView map_;
  protected IMapController map_controller_;
  protected LocationManager location_manager_;
  
  // Μεταβλητή για να καταγράφουμε αν ο χρήστης θέλει ο χάρτης
  // να ακολουθεί την τωρινή του θέση.
  private boolean follow_current_position_ = false, help_enable_ = false;
  
  // Variables for the current position
  protected GeoPoint current_position_point_ ;
  protected Marker current_position_marker_ ;
 
  
  // Τα κουμπιά της φόρμας
  Button button_tracking_mode_, button_help_;
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);
    
    button_tracking_mode_ = (Button) findViewById(R.id.buttonTrackingMode);
    button_help_ = (Button) findViewById (R.id.buttonHelp);
    
    map_ = (MapView) findViewById(R.id.map);
    map_controller_ = map_.getController();
    current_position_marker_= new Marker(map_);
    
    location_manager_ = (LocationManager) this.getSystemService(
        Context.LOCATION_SERVICE);    
    
    // Ενεργοποιούμε την θέση μέσω του δικτύου
    location_manager_.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
        0, 0, this);
    
    // Ρωτάμε τον χρήστη αν θέλει να ενεργοποιήση το GPS
    // http://stackoverflow.com/questions/24837418/checking-if-gps-is-enabled-in-android
    // Να τα ξανακοιτάξω... Δεν είναι και τόσο καλό
    if(!location_manager_.isProviderEnabled( LocationManager.GPS_PROVIDER))
    {
      AlertDialog.Builder alert_box = new AlertDialog.Builder(this);
      
      alert_box.setMessage(R.string.gps_error)
        .setTitle(R.string.gps_error_title)
        .setPositiveButton(R.string.gps_error_continue, 
            new DialogInterface.OnClickListener()
            {
              
              @Override
              public void onClick(DialogInterface dialog, int which)
              {
                // Do nothing and continue execution of the program
                // If we want we can end the application
                // finish();
              }
            } )
        .setNegativeButton(R.string.gps_error_settings, 
            new DialogInterface.OnClickListener()
            {
              
              @Override
              public void onClick(DialogInterface dialog, int which)
              {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
              }
            });
      
      AlertDialog alert_dialog = alert_box.create();
      alert_dialog.show();
    }
    else
    {
      // http://developer.android.com/reference/android/location/LocationManager.html#requestLocationUpdates%28java.lang.String,%20long,%20float,%20android.location.LocationListener%29
      location_manager_.requestLocationUpdates(LocationManager.GPS_PROVIDER,
          0, 0, this);
      Toast.makeText(getApplicationContext(), 
          R.string.gps_enable, Toast.LENGTH_SHORT).show();
    }
    
    // Χρησιμοποιούμε αν είναι διαθέσιμη η τελευταία γνωστή τοποθεσία από το δίκτυο
    if(null != location_manager_.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
    {
      current_position_point_ = new GeoPoint( location_manager_.getLastKnownLocation(
          LocationManager.NETWORK_PROVIDER) );
    }
    
    if( !isNetworkConnected() )
    {
      // http://stackoverflow.com/questions/15456428/ask-user-to-start-wifi-or-3g-on-launching-an-android-app-if-not-connected-to-int
      AlertDialog.Builder alert_box = new AlertDialog.Builder(this);
      
      alert_box.setMessage(R.string.connection_error)
        .setTitle(R.string.connection_error_title)
        .setPositiveButton(R.string.connection_error_continue, 
            new DialogInterface.OnClickListener()
            {
              
              @Override
              public void onClick(DialogInterface dialog, int which)
              {
                // Do nothing and continue execution of the program
                // If we want we can end the application
                // finish();
              }
            } )
        .setNegativeButton(R.string.connection_error_settings, 
            new DialogInterface.OnClickListener()
            {
              
              @Override
              public void onClick(DialogInterface dialog, int which)
              {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
              }
            });
      
      AlertDialog alert_dialog = alert_box.create();
      alert_dialog.show();
    }
    
    // http://wiki.openstreetmap.org/wiki/Tiles
    map_.setTileSource(TileSourceFactory.MAPNIK);
    map_.setBuiltInZoomControls(false);
    map_.setMultiTouchControls(true);
    
    // Centering over Greece

    map_controller_.setZoom(7);
    map_controller_.setCenter(new GeoPoint(42.5,19.5));
    
    
    map_.invalidate();
  }
  
  /**
   * Συνάρτηση που ελέγχει αν υπάρχει σύνδεση σε δίκτυο
   * http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-timeouts
   * 
   * Να κοιτάξω το εξής:
   * http://stackoverflow.com/questions/9570237/android-check-internet-connection
   * http://stackoverflow.com/questions/14922098/how-to-check-internet-access-is-enabled-in-android
   * 
   * @return 
   */
  protected boolean isNetworkConnected()
  {
    ConnectivityManager manager = (ConnectivityManager) getSystemService(
        Context.CONNECTIVITY_SERVICE);
    NetworkInfo network = manager.getActiveNetworkInfo();
    return network!=null && network.isConnected();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.map, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings)
    {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onLocationChanged(Location location)
  {
    current_position_point_ = new GeoPoint(location);
    Toast.makeText(getApplicationContext(), 
        "current= " + current_position_point_.getLongitude() + ", " + current_position_point_.getLatitude(),
        Toast.LENGTH_SHORT).show();
    
    if(follow_current_position_)
    {
      map_controller_.animateTo(current_position_point_);
      map_controller_.setCenter(current_position_point_);
    }

    current_position_marker_.setPosition(current_position_point_);
    current_position_marker_.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
    map_.getOverlays().add(current_position_marker_);
    map_.invalidate();
  }
  
  @Override
  public void onStatusChanged(String provider, int status, Bundle extras)
  {
    // TODO Auto-generated method stub
  }

  @Override
  public void onProviderEnabled(String provider)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onProviderDisabled(String provider)
  {
    // TODO Auto-generated method stub
    
  }
  
  public void currentPosition(View view)
  {
    if (null != current_position_point_)
    {
      map_controller_.animateTo(current_position_point_);
      map_controller_.setCenter(current_position_point_);
      map_controller_.setZoom(17);   
    }
    else
    {
      Toast.makeText(getApplicationContext(), 
          R.string.gps_location, Toast.LENGTH_SHORT).show();
    }
  }
  
  public void trackingMode (View view)
  {
    if(follow_current_position_)
    {
      follow_current_position_ = false;
      button_tracking_mode_.setBackgroundResource(R.drawable.btn_tracking_off);
      Toast.makeText(getApplicationContext(), 
          R.string.tracking_mode_off, Toast.LENGTH_SHORT).show();
    }
    else
    {
      follow_current_position_ = true;
      button_tracking_mode_.setBackgroundResource(R.drawable.btn_tracking_on);
      Toast.makeText(getApplicationContext(), 
          R.string.tracking_mode_on, Toast.LENGTH_SHORT).show();
    }      
  }
  
  public void help (View view)
  {
    if(help_enable_)
    {
      help_enable_ = false;
      button_help_.setText(R.string.help);
      Toast.makeText(getApplicationContext(), 
          R.string.help_message, Toast.LENGTH_SHORT).show();
    }
    else
    {
      help_enable_ = true;
      button_help_.setText(R.string.help_abort);
      Toast.makeText(getApplicationContext(), 
          R.string.help_message, Toast.LENGTH_SHORT).show();
    }      
  }
  
  
  //private void stopGps()
  //{
  //  location_manager_.removeUpdates(this);
  //  //++group_count;
  //  Toast.makeText(getApplicationContext(), 
  //      "GPS was disabled", Toast.LENGTH_SHORT).show();
  //}
}
