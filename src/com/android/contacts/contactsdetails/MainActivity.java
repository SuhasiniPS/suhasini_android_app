package com.example.peoples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {
	
	
    CustomAdapter adapter;
    private ActionBar actionBar;
    
    private MenuItem refreshMenuItem;
    
    Context context = this;
    
    ListView list,list1;
    EditText add_edit_name,add_edit_num;
    Button save,clear,update,delete;

    public  MainActivity CustomListView = null;

    List<ListModel> rowItem = new ArrayList<ListModel>();
    List<Integer> idArr = new ArrayList<Integer>();
    List<String> nameArr = new ArrayList<String>();
    List<String> numArr = new ArrayList<String>();
   

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		actionBar = getActionBar();
		CustomListView = this;
		 
		 Db entry = new Db(MainActivity.this);
			entry.open();
			idArr = entry.getId();
			nameArr = entry.getName();
			numArr = entry.getNum();
		
			list= ( ListView )findViewById( R.id.list );
			setListData();
			
	     list.setOnItemClickListener(new OnItemClickListener() {
	        	
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position,
						long arg3) {
					// TODO Auto-generated method stub
					ListModel data = rowItem.get(position);
					final int data1 = data.getSno();
		    		String data2 = data.getName();
		    		String data3 = data.getNum();
					
					final Dialog ud = new Dialog(context);
					ud.setContentView(R.layout.up_del_dialog);
					ud.setTitle("Edit");
					
					update = (Button)ud.findViewById(R.id.Update);
					delete = (Button)ud.findViewById(R.id.Delete);
					final EditText uname = (EditText)ud.findViewById(R.id.up_edit_name);
					final EditText unum = (EditText)ud.findViewById(R.id.up_edit_num);
					
					//update------------------------------------------
					update.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub					
								
								String nameu = uname.getText().toString();
								String numu = unum.getText().toString();
								
								Db up_people = new Db(MainActivity.this);
								up_people.open();
								up_people.update(data1,nameu,numu);
								up_people.close();

								Toast.makeText(MainActivity.this, "People Edited",
										   Toast.LENGTH_LONG).show();
				
								ud.dismiss();
								refresh();
							}
					});
					
					//delete------------------------------
					delete.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
						Db del_people = new Db(MainActivity.this);
						del_people.open();
						del_people.delete(data1);
							
							Toast.makeText(MainActivity.this, "People Deleted",
									   Toast.LENGTH_LONG).show();
							
							ud.dismiss();
							refresh();
						}
					});
					
					ud.show();
				}
	     });
	     
	}
	
	public void setListData()
    {
         
		for (int i = 0; i < idArr.size(); i++) 
		{
	           ListModel item = new ListModel(idArr.get(i),nameArr.get(i),numArr.get(i));
	            //adding RowItem object to rowItems
	           rowItem.add(item);
	    }
		CustomAdapter adapter = new CustomAdapter(MainActivity.this,R.layout.list_item_view, rowItem);
	    list.setAdapter(adapter);
	     
    }

	
	 /****** Initiating Menu XML file (menu.xml)************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        
        switch (item.getItemId())
        {
        case R.id.action_refresh:
        	refreshMenuItem=item;
        	new SyncData().execute();
        	refresh();
            return true;
            
        case R.id.add_people:
        	dialogBox();
        	return true;
        
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private class SyncData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            // set the progress bar view
            refreshMenuItem.setActionView(R.layout.action_progressbar);
 
            refreshMenuItem.expandActionView();
        }
 
        @Override
        protected String doInBackground(String... params) {
            // not making real request in this demo
            // for now we use a timer to wait for sometime
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
 
        @Override
        protected void onPostExecute(String result) {
            refreshMenuItem.collapseActionView();
            // remove the progress bar view
            refreshMenuItem.setActionView(null);
        }
    };
    
    //Refresh---------------------------------------------
    public void refresh()
    {
    	rowItem = new ArrayList<ListModel>();
		
		Db entry1 = new Db(MainActivity.this);
		entry1.open();
		idArr = entry1.getId();
		nameArr = entry1.getName();
		numArr = entry1.getNum();
		Log.d("MainActivity", "ID : - " + idArr);
		Log.d("MainActivity", "Name : - " + nameArr);
		Log.d("MainActivity", "Phone : - " + numArr);
		

		for (int i = 0; i < idArr.size(); i++) {
			ListModel item = new ListModel(idArr.get(i),nameArr.get(i),numArr.get(i));
	            //adding RowItem object to rowItems
	        Log.d("MainActivity", "Name : - ");
			Log.d("MainActivity", "Phone : - ");
				
	           rowItem.add(item);
	        }
		
		list1 = (ListView)findViewById(R.id.list);
        CustomAdapter adapter = new CustomAdapter(MainActivity.this,R.layout.list_item_view, rowItem);
        list1.setAdapter(adapter);
    }
    
    //Save and Clear dialog Box---------------------------
    public void dialogBox()
    {
    	final Dialog d = new Dialog(context);
		d.setContentView(R.layout.dialog_add);
		d.setTitle("Add New People");
    
		save=(Button) d.findViewById(R.id.save);
		clear=(Button) d.findViewById(R.id.clear);
		add_edit_name=(EditText) d.findViewById(R.id.add_edit_name);
		add_edit_num=(EditText) d.findViewById(R.id.add_edit_num);
		
		//Save Action------------------------------------
		save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				String names = add_edit_name.getText().toString();
				String nums = add_edit_num.getText().toString();
				
				Db database = new Db(MainActivity.this);
				database.open();
				database.save(names,nums);
				database.close();
				
				Log.d("Data Stored", "Storing..");
				Toast.makeText(MainActivity.this, "People Saved", Toast.LENGTH_SHORT).show();
	            
				d.dismiss();
				refresh();
			}
		});	
		
		//Clear Action-------------------------------------
		clear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				add_edit_name.setText(" ");
				add_edit_num.setText(" ");
				
			}
		});
		
		d.show();
    }
}
