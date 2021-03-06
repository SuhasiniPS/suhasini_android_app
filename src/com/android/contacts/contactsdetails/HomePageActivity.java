package com.android.contacts.contactsdetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.contacts.handler.ServiceHandler;

public class HomePageActivity extends ListActivity {

	private ProgressDialog pDialog;

	private static final String TAG_CONTACTS = "contacts";
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "name";
	private static final String TAG_EMAIL = "email";
	private static final String TAG_PH = "phone";
	private static final String TAG_PHONE = "mobile";

	private static String url = "http://api.androidhive.info/contacts/";
	Button syncBtn, logoutBtn;
	JSONArray contacts = null;
	ArrayList<HashMap<String, String>> contactList;
	ListAdapter adapter = null;
	ListView lv = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_page);
		
		lv = (ListView) findViewById(android.R.id.list);
		contactList = new ArrayList<HashMap<String, String>>();

		getListView();

		displayContacts();
		
		lv.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("unchecked")
			@Override
		    public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
	
				view.setSelected(true);
				Map<String, String> itemValue = new HashMap<String, String>(); 
				itemValue  =  (HashMap<String, String>)lv.getItemAtPosition(position);
				String mobile = itemValue.get(TAG_PHONE);
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse("tel:"+mobile));
				startActivity(callIntent);				
		    }
		});
		syncBtn = (Button) findViewById(R.id.syncBtn);

		syncBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new GetContacts().execute();
				
			}
		});
		logoutBtn = (Button) findViewById(R.id.logoutBtn);
		logoutBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences sharedpreferences = getSharedPreferences(MainActivity.MyPREFERENCES, Context.MODE_PRIVATE);
			    SharedPreferences.Editor editor = sharedpreferences.edit();
			    editor.clear();
			    editor.commit();	
			    finish();
			}
		});
	}

	private class GetContacts extends AsyncTask<Void, Void, Void> {
		Context cntx = getApplicationContext();
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = new ProgressDialog(HomePageActivity.this);
			pDialog.setMessage("Please wait...");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@Override
		protected Void doInBackground(Void... arg0) {
			// Creating service handler class instance
			ServiceHandler sh = new ServiceHandler();

			// Making a request to url and getting response
			String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

			if (jsonStr != null) {
				try {
					JSONObject jsonObj = new JSONObject(jsonStr);

					// Getting JSON Array node
					contacts = jsonObj.getJSONArray(TAG_CONTACTS);

					// looping through All Contacts
					for (int i = 0; i < contacts.length(); i++) {
						JSONObject c = contacts.getJSONObject(i);

						String id = c.getString(TAG_ID);
						String name = c.getString(TAG_NAME);
						String email = c.getString(TAG_EMAIL);

						// Phone node is JSON Object
						JSONObject phone = c.getJSONObject(TAG_PH);
						String mobile = phone.getString(TAG_PHONE);
						
						// Write the contact in mobile contacts
						WritePhoneContact(name, mobile, email, cntx);

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Log.e("ServiceHandler", "Couldn't get any data from the url");
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// Dismiss the progress dialog
			if (pDialog.isShowing())
				contactList.clear();
			displayContacts();

				pDialog.dismiss();
		}
	}

	public void displayContacts() {
		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				String phoneno = new String();
				String email = new String();

				if (Integer
						.parseInt(cur.getString(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = cr.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = ?", new String[] { id }, null);
					while (pCur.moveToNext()) {
						phoneno = pCur
								.getString(pCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					}
					pCur.close();

					Cursor emailCur = cr.query(
							ContactsContract.CommonDataKinds.Email.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Email.CONTACT_ID
									+ " = ?", new String[] { id }, null);
					while (emailCur.moveToNext()) {
						email = emailCur
								.getString(emailCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
						String emailType = emailCur
								.getString(emailCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));

						System.out.println("Email " + email + " Email Type : "
								+ emailType);
					}
					emailCur.close();

					if ((name != null) && (phoneno != null) && (email != null)) {
						// tmp hashmap for single contact
						HashMap<String, String> contact = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						contact.put(TAG_ID, id);
						contact.put(TAG_NAME, name);
						contact.put(TAG_PHONE, phoneno);
						contact.put(TAG_EMAIL, email);

						// adding contact to contact list
						contactList.add(contact);
					}
					/* List */
					adapter = new SimpleAdapter(
							HomePageActivity.this, contactList,
							R.layout.list_item, new String[] { TAG_NAME,
									TAG_EMAIL, TAG_PHONE }, new int[] {
									R.id.listname, R.id.listemail,
									R.id.listmobile });

					setListAdapter(adapter);
				}
			}
		}
	}

	public void WritePhoneContact(String displayName, String number,
										String emailID, Context cntx) {
		Context contetx = cntx; // Application's context or Activity's context
	
		ArrayList<ContentProviderOperation> cntProOper = new ArrayList<ContentProviderOperation>();
		int contactIndex = cntProOper.size();// ContactSize

		// Newly Inserted contact
		// A raw contact will be inserted ContactsContract.RawContacts table in
		// contacts database.
		cntProOper.add(ContentProviderOperation
					.newInsert(RawContacts.CONTENT_URI)
					.withValue(RawContacts.ACCOUNT_TYPE, null)
					.withValue(RawContacts.ACCOUNT_NAME, null).build());
		if ((displayName != null) && (number != null) && (emailID != null)) {
			// Display name will be inserted in ContactsContract.Data table
			cntProOper.add(ContentProviderOperation
					.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID, contactIndex)
					.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
					.withValue(StructuredName.DISPLAY_NAME, displayName) // Name of the contact
					.build());

			// Mobile number will be inserted in ContactsContract.Data table
			cntProOper.add(ContentProviderOperation
					.newInsert(Data.CONTENT_URI)
					.withValueBackReference(
							ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
					.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
					.withValue(Phone.NUMBER, number) // Number to be added
					.withValue(Phone.TYPE, Phone.TYPE_MOBILE).build()); // Type like HOME, MOBILE etc

			// Email ID will be inserted in ContactsContract.Data table
			cntProOper
					.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.CommonDataKinds.Email.DATA, emailID)
							.withValue(ContactsContract.CommonDataKinds.Email.TYPE,	ContactsContract.CommonDataKinds.Email.TYPE_WORK)
							.build());
			try {
				// We will do batch operation to insert all above data
				// Contains the output of the app of a ContentProviderOperation.
				// It is sure to have exactly one of uri or count set
				ContentProviderResult[] contentProresult = null;
				// apply above data insertion into contacts list
				contentProresult = contetx.getContentResolver().applyBatch(
						ContactsContract.AUTHORITY, cntProOper);
				

			} catch (RemoteException exp) {
				// logs;
			} catch (OperationApplicationException exp) {
				// logs
			}
		} else {
			Toast.makeText(getApplicationContext(), "Enter correctly",
					Toast.LENGTH_LONG).show();
		}
	}

	private void resume() {
		// TODO Auto-generated method stub
		contactList.clear();
		displayContacts();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.home_page, menu);
        return true;
	}
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        
        switch (item.getItemId())
        {
        case R.id.action_refresh:
    		contactList.clear();
    		displayContacts();
        	return true;
                    
        default:
            return super.onOptionsItemSelected(item);
        }
    }

}
