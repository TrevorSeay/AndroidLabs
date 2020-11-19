package com.example.androidlabs;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DetailsFragment extends Fragment
{
    private AppCompatActivity parentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        Bundle args = getArguments();
        RelativeLayout rl = (RelativeLayout)inflater.inflate(R.layout.fragment_details, container, false);
        ((TextView)rl.findViewById(R.id.message_textview)).setText(args.getString("msg"));
        ((TextView)rl.findViewById(R.id.id_textview)).setText( getString(R.string.id_text, args.getLong("id")) );
        ((CheckBox)rl.findViewById(R.id.is_sent_chk)).setChecked(args.getBoolean("chk"));

        Button hideButton = (Button)rl.findViewById(R.id.hide_button);
        hideButton.setOnClickListener(clk ->
        {
            parentActivity.getSupportFragmentManager().beginTransaction().remove(this).commit();
            if(!args.getBoolean("tblt")) parentActivity.onBackPressed();
        });
        return rl;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //context will either be FragmentExample for a tablet, or EmptyActivity for phone
        parentActivity = (AppCompatActivity)context;
    }
}