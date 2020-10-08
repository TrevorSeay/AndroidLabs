package com.example.androidlabs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{

    static final String SHARED_PREFS_EMAIL = "emailPrefs";
    static final String SHARED_PREFS_EMAIL_ADDRESS = "email";

    static final String EXTRA_EMAIL = "EMAIL";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_request_layout);

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_EMAIL, Context.MODE_PRIVATE);
        String emailAddress = prefs.getString(SHARED_PREFS_EMAIL_ADDRESS, "");

        EditText emailInput = findViewById(R.id.email_input);
        emailInput.setText(emailAddress);

        findViewById(R.id.login_button).setOnClickListener(v ->
        {
            Intent goToProfile = new Intent(MainActivity.this, ProfileActivity.class);
            String email = ((EditText)findViewById(R.id.email_input)).getText().toString();
            if(email.equals(""))
                Toast.makeText(getApplicationContext(), R.string.empty_email_toast, Toast.LENGTH_SHORT).show();
            else
            {
                goToProfile.putExtra(EXTRA_EMAIL, email);
                startActivity(goToProfile);
            }
        });
    }

    @Override
    protected void onPause()
    {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_EMAIL, Context.MODE_PRIVATE);
        String emailInput = ((EditText)findViewById(R.id.email_input)).getText().toString();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SHARED_PREFS_EMAIL_ADDRESS, emailInput);
        editor.commit();

        super.onPause();
    }
}