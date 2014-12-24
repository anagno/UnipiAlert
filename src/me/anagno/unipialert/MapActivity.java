package me.anagno.unipialert;

import me.anagno.unipialert.R.drawable;

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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

public class MapActivity extends Activity implements LocationListener
{
  
  protected MapView map_;
  protected IMapController map_controller_;
  protected LocationManager location_manager_;
  
  private Switch route_switch_;
  private SQLiteDatabase db_;
  private int route_count_ = 0; //Για να μετράμε τα group των συντεταγμένων
  
  // Μεταβλητή για να καταγράφουμε αν ο χρήστης θέλει ο χάρτης
  // να ακολουθεί την τωρινή του θέση.
  private boolean follow_current_position_ = false, help_enable_ = false;
  
  // Variables for the current position
  protected GeoPoint current_position_point_ ;
  protected Marker current_position_marker_ ;
 
  
  // Τα κουμπιά της φόρμας
  Button button_help_;
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);
    
    // Getting the prefrerensces
    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    
    db_ = openOrCreateDatabase ("places", Context.MODE_PRIVATE,null);
    db_.execSQL("CREATE TABLE IF NOT EXISTS places ("
             + "count INTEGER, "
             + "longitude REAL, "
             + "latitude REAL, "
             + "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP );");
    
    db_.execSQL("CREATE TABLE IF NOT EXISTS description ("
        + "count INTEGER, "
        + "description TEXT );");
    
    Cursor c = db_.rawQuery("SELECT MAX(count) FROM places ",null);
    
    if(c.moveToFirst())
    {
      route_count_ = c.getInt(0); //Για να βρίσκουμε 
                             // την μέγιστη τιμή που έχει ήδη καταχωρηθεί 
    }
    
    Toast.makeText(getApplicationContext(), 
        "route count: " + route_count_ ,
        Toast.LENGTH_SHORT).show();
    
    route_switch_= (Switch) findViewById (R.id.routeSwitch);
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
    switch(item.getItemId())
    {
      case R.id.itemCurrentPosition:
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
        return true;        
      case R.id.itemTrackingMode:
        if(follow_current_position_)
        {
          follow_current_position_ = false;
          item.setIcon(R.drawable.btn_tracking_off);
          Toast.makeText(getApplicationContext(), 
              R.string.tracking_mode_off, Toast.LENGTH_SHORT).show();
        }
        else
        {
          follow_current_position_ = true;
          item.setIcon(R.drawable.btn_tracking_on);
          Toast.makeText(getApplicationContext(), 
              R.string.tracking_mode_on, Toast.LENGTH_SHORT).show();
        } 
        return true;
      case R.id.itemRoutes:
        Intent intent_routes = new Intent(this, RoutesActivity.class);
        startActivityForResult(intent_routes,1);
        return true;
      case R.id.itemClearMap:
        map_.getOverlays().clear();
        current_position_marker_.setPosition(current_position_point_);
        current_position_marker_.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map_.getOverlays().add(current_position_marker_);
        map_.invalidate();
        return true;
      case R.id.action_settings:
        //TODO
        Intent intent_settings = new Intent(this, SettingsActivity.class);
        startActivity(intent_settings);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }

  }
  
  //http://stackoverflow.com/questions/920306/sending-data-back-to-the-main-activity-in-android
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    super.onActivityResult(requestCode, resultCode, data);
    switch(requestCode)
    {
      // Δηλώθηκε στο startActivityForResult(intent_routes,1);
      case (1):
      {
        if(resultCode == Activity.RESULT_OK)
        {
          // Δηλώθηκε στο Routes Activity 
          int result = data.getIntExtra("returned_route", 0);
          
          Cursor cursor_place = db_.rawQuery("SELECT * FROM places WHERE "
              + "count ='" + result + "'", null);
          
          Cursor cursor_descrition = db_.rawQuery("SELECT * FROM description WHERE "
              + "count ='" + result + "'", null);
          
          Toast.makeText(getApplicationContext(), 
              "Record: " + result + " (" + cursor_descrition.getCount() +")", Toast.LENGTH_LONG).show();
          
          Drawable node_icon = getResources().getDrawable(R.drawable.marker_node);
          cursor_descrition.moveToFirst();
          while(cursor_place.moveToNext())
          {
            Marker node = new Marker(map_);
            node.setPosition(new GeoPoint(cursor_place.getDouble(2), cursor_place.getDouble(1)));
            node.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            node.setIcon(node_icon);
            node.setSnippet(cursor_descrition.getString(1));
            map_.getOverlays().add(node);
          } 
          map_.invalidate();
        }
        break;
      }
      default:
        break;
    }
  }
   
  @Override
  public void onLocationChanged(Location location)
  {
    current_position_point_ = new GeoPoint(location);
    if(!route_switch_.isChecked())
    {
      //Toast.makeText(getApplicationContext(), 
      //    "position: " + current_position_point_.getLongitude() + ", " + current_position_point_.getLatitude(),
      //    Toast.LENGTH_SHORT).show();
    }
          
    
    if(follow_current_position_)
    {
      map_controller_.animateTo(current_position_point_);
      map_controller_.setCenter(current_position_point_);
    }

    current_position_marker_.setPosition(current_position_point_);
    current_position_marker_.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
    map_.getOverlays().add(current_position_marker_);
    map_.invalidate();
    
    // Άμα είναι ενεργοποιημένος ο διακόπτης να καταγράφετε η θέση
    if(route_switch_.isChecked())
    {
      db_.execSQL("Insert INTO places(count,longitude,latitude) VALUES("
          + "'" + route_count_ + "',"
          + "'" + current_position_point_.getLongitude()+ "',"
          + "'" + current_position_point_.getLatitude() +"');");
      
      Cursor query = db_.rawQuery("SELECT * FROM description WHERE "
          + "count = '" + route_count_ +"'", null);
      
      if (!query.moveToFirst())
      {
        db_.execSQL("Insert INTO description(count,description) VALUES("
            + "'" + route_count_ + "',"
            + "'" + this.getString(R.string.record_route) + " " + route_count_ +"');");
      }
      
      //Toast.makeText(getApplicationContext(), 
      //    "route count: " + route_count_ +" - position: " + current_position_point_.getLongitude() + ", " + current_position_point_.getLatitude(),
      //    Toast.LENGTH_SHORT).show();
    }
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
  
  public void recordRoute(View v)
  {
    if (route_switch_.isChecked())
    {
      ++route_count_;
    }
  }
  
  // Πρόχειρη συνάρτηση
  public void acivations(View v)
  {
    Cursor c = db_.rawQuery("SELECT * FROM description", null);
    
    if (c.getCount() == 0)
    {
      Toast.makeText(getApplicationContext(), 
          "No values were found", Toast.LENGTH_LONG).show();
    }
    
    StringBuffer buffer = new StringBuffer();
    
    while (c.moveToNext())
    {
      buffer.append("route count: " + c.getString(0) + "\n");
      buffer.append("description: " + c.getString(1) + "\n");
      buffer.append("\n");
    }
    
    Toast.makeText(getApplicationContext(), 
        buffer.toString(), Toast.LENGTH_LONG).show();
    
  }
    
  //private void stopGps()
  //{
  //  location_manager_.removeUpdates(this);
  //  //++group_count;
  //  Toast.makeText(getApplicationContext(), 
  //      "GPS was disabled", Toast.LENGTH_SHORT).show();
  //}
}
