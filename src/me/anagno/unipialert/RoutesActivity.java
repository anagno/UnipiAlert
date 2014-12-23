package me.anagno.unipialert;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class RoutesActivity extends Activity
{
  
  private ListView list_view ;
  private SQLiteDatabase db_;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_routes);
    
    db_ = openOrCreateDatabase ("places", Context.MODE_PRIVATE,null);    
    list_view =  (ListView) findViewById(R.id.listViewRoute);
    
    /*
    
    listView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view,
        int position, long id) {
        Toast.makeText(getApplicationContext(),
          "Click ListItem Number " + position, Toast.LENGTH_LONG)
          .show();
      }
    }); 

    */   
    
    Cursor c = db_.rawQuery("SELECT * FROM description", null);
    
    if (c.getCount() == 0)
    {
      Toast.makeText(getApplicationContext(), 
          R.string.routes_no_values, Toast.LENGTH_LONG).show();
    }
    
    
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    // Δεν θα έχουμε μενού σε αυτή την Activity
    //getMenuInflater().inflate(R.menu.routes, menu);
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
