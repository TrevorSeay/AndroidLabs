package com.example.androidlabs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatRoomActivity extends AppCompatActivity
{

    private static Drawable send_drawable;
    private static Drawable receive_drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        send_drawable = getDrawable(R.drawable.row_send);
        receive_drawable = getDrawable(R.drawable.row_receive);

        ListView listView = findViewById(R.id.messages_view);
        MessageAdapter adapter = new MessageAdapter();
        listView.setAdapter(adapter);

        EditText input = findViewById(R.id.message_input);

        LayoutInflater inflater = getLayoutInflater();

        ((Button)findViewById(R.id.send_button)).setOnClickListener((View v) ->
        {
            LinearLayout ll = (LinearLayout)inflater.inflate(R.layout.send_row_layout, listView, false);
            TextView tv = (TextView)ll.getChildAt(0);
            tv.setText(input.getText());

            adapter.getContents().add(ll);
            listView.setSelection(adapter.getCount() - 1);
            input.setText("");
        });

        ((Button)findViewById(R.id.receive_button)).setOnClickListener((View v) ->
        {
            LinearLayout ll = (LinearLayout)inflater.inflate(R.layout.receive_row_layout, listView, false);
            TextView tv = (TextView)ll.getChildAt(1);
            tv.setText(input.getText());

            adapter.getContents().add(ll);
            listView.setSelection(adapter.getCount() - 1);
            input.setText("");
        });

        listView.setOnItemLongClickListener((arg0, arg1, pos, id) ->
        {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_alert_title)
                    .setMessage(getString(R.string.selected_row_is) + " " + pos + "\n" + getString(R.string.database_id_is) + " " + id)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> adapter.remove(pos))
                    .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.cancel())
                    .show();
            return true;
        });
    }

    private class MessageAdapter extends BaseAdapter
    {
        private ArrayList<View> contents = new ArrayList<View>();

        public ArrayList<View> getContents() { return this.contents; }

        @Override
        public int getCount() { return this.getContents().size(); }

        @Override
        public Object getItem(int position) { return this.getContents().get(position); }

        @Override
        public long getItemId(int position) { return (long)position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            return this.getContents().get(position);
        }

        public void remove(int position)
        {
            this.getContents().remove(position);
            super.notifyDataSetChanged();
        }
    }

}