package com.android.contacts.contactsdetails;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	Button loginBtn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		loginBtn = (Button) findViewById(R.id.email_sign_in_button);

		loginBtn.setOnClickListener(new View.OnClickListener() {
			
			EditText user_name, password;	
			
			@Override
			public void onClick(View v) {
				user_name = (EditText) findViewById(R.id.email);
				password = (EditText) findViewById(R.id.password);
				String user_name_txt = user_name.getText().toString();
				String password_txt = password.getText().toString();
				if(user_name_txt.equals(password_txt))
				{
				Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
				startActivity(intent);
				}
				else
				{
					Toast.makeText(getApplicationContext(),"Username and Password are not Matching", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
}
