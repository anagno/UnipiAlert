package me.anagno.unipialert;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


// http://www.vogella.com/tutorials/AndroidListView/article.html
public class RoutesActivity extends ListActivity
{
  
  private SQLiteDatabase db_;
  ArrayList<Integer> route_number_;
  ArrayList<String> route_description_;
  ArrayAdapter<String> adapter_;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_routes);

    db_ = openOrCreateDatabase ("places", Context.MODE_PRIVATE,null);    

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
    
    route_number_ = new ArrayList<Integer>();
    route_description_ = new ArrayList<String>();   
    
    Cursor c = db_.rawQuery("SELECT * FROM description", null);
    
    if (c.getCount() == 0 )
    {
      Toast.makeText(getApplicationContext(), 
          R.string.routes_no_values, Toast.LENGTH_LONG).show();
      finish();
    }
    
    while(c.moveToNext())
    {
      int count = c.getInt(0);
      String description = c.getString(1);
      
      route_number_.add(count);
      route_description_.add(description);       
    }
       
    c.close();
        
    adapter_ = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1,route_description_);
    setListAdapter(adapter_);
    
    registerForContextMenu(getListView());
 
  }
  
  @Override
  public void onListItemClick(ListView l, View v, int position, long id)
  {
    Intent result_intent = new Intent();
    result_intent.putExtra("returned_route", route_number_.get(position) );
    setResult(Activity.RESULT_OK,result_intent);
    finish();
  }
  
  @Override
  public void onCreateContextMenu (ContextMenu menu, View v, 
      ContextMenu.ContextMenuInfo menuInfo)
  {
    super.onCreateContextMenu(menu, v, menuInfo);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.routes, menu);
  }
  
  @Override
  public boolean onContextItemSelected(MenuItem item) 
  {
      final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      switch (item.getItemId()) 
      {
        case R.id.deleteItem:
          route_description_.remove(info.position);
          adapter_.notifyDataSetChanged();
          db_.execSQL("DELETE FROM description "
              + "WHERE count ='" + route_number_.get(info.position) + "'");
          db_.execSQL("DELETE FROM places "
              + "WHERE count ='" + route_number_.get(info.position) + "'");
          return true;
        case R.id.editItem:
          AlertDialog.Builder box = new AlertDialog.Builder(this);
          final EditText input = new EditText(this);
          box.setMessage(R.string.routes_edit_message)
            .setTitle(R.string.routes_edit_title)
            .setView(input)
            .setPositiveButton(R.string.routes_edit_ok, 
                new DialogInterface.OnClickListener()
                {
                  
                  @Override
                  public void onClick(DialogInterface dialog, int which)
                  {
                    String value = input.getText().toString();
                    route_description_.set(info.position, value);
                    adapter_.notifyDataSetChanged();
                    db_.execSQL("UPDATE description SET "
                        + "description= '" + value +"' "
                        + "WHERE count ='" + route_number_.get(info.position) + "'");
                  }
                } )
            .setNegativeButton(R.string.routes_edit_cancel, 
                new DialogInterface.OnClickListener()
                {
                  
                  @Override
                  public void onClick(DialogInterface dialog, int which)
                  {
                    // Do nothing and continue execution of the program
                    // If we want we can end the application
                    // finish();
                  }
                });
          
          AlertDialog dialog = box.create();
          dialog.show();
          return true;
        default:
          return super.onContextItemSelected(item);
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
