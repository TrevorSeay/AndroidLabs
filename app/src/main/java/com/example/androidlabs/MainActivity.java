package com.example.androidlabs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_grid);

        findViewById(R.id.button).setOnClickListener(view -> Toast.makeText(getApplicationContext(), R.string.toast_message, Toast.LENGTH_LONG).show());

        ((Switch)findViewById(R.id.switch1)).setOnCheckedChangeListener((s, b) ->
        {
            Snackbar sb = Snackbar.make(s, getString(R.string.switch_is_now_text) + " " + (b ? getString(R.string.on_text) : getString(R.string.off_text)), Snackbar.LENGTH_LONG);
            sb.setAction(R.string.undo_text, click -> s.setChecked(!b));
            sb.show();
        });
    }
}