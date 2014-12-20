package me.anagno.unipialert;

import org.osmdroid.api.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

public class MapActivity extends Activity
{
  
  protected MapView map_;
  
  // Variables for the current position
  protected GeoPoint current_position_point;
  protected Marker current_position_marker;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);
    
    map_ = (MapView) findViewById(R.id.map);
    
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
    
    map_.invalidate();
  }
  
  /**
   * Συνάρτηση που ελέγχει αν υπάρχει σύνδεση σε δίκτυο
   * http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-timeouts
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
}
