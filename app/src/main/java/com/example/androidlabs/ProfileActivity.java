package com.example.androidlabs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.Console;

public class ProfileActivity extends AppCompatActivity
{
    public static final String ACTIVITY_NAME = "PROFILE_ACTIVITY";
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageButton mImageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.e(ACTIVITY_NAME, "In function: " + new Throwable().getStackTrace()[0].getMethodName());

        setContentView(R.layout.activity_profile);

        (mImageButton = findViewById(R.id.image_button)).setOnClickListener((v) -> dispatchTakePictureIntent());

        Intent fromMain = getIntent();
        ((EditText)findViewById(R.id.email_profile_input)).setText(fromMain.getStringExtra(MainActivity.EXTRA_EMAIL));
    }

    @Override
    protected void onStart()
    {
        Log.e(ACTIVITY_NAME, "In function: " + new Throwable().getStackTrace()[0].getMethodName());
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        Log.e(ACTIVITY_NAME, "In function: " + new Throwable().getStackTrace()[0].getMethodName());
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        Log.e(ACTIVITY_NAME, "In function: " + new Throwable().getStackTrace()[0].getMethodName());
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        Log.e(ACTIVITY_NAME, "In function: " + new Throwable().getStackTrace()[0].getMethodName());
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Log.e(ACTIVITY_NAME, "In function: " + new Throwable().getStackTrace()[0].getMethodName());
        super.onDestroy();
    }

    private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.e(ACTIVITY_NAME, "In function: " + new Throwable().getStackTrace()[0].getMethodName());
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageButton.setImageBitmap(imageBitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}