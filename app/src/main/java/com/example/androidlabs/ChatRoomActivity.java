package com.example.androidlabs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

    private static final String DATABASE_NAME = "ChatRoom";
    private static final int VERSION_NUM = 1;
    private static final String TABLE_NAME = "Message_T";
    private static final String COL_MSG = "msg";
    private static final String COL_FROM = "frm";

    private static final int FROM_SENT = 0;
    private static final int FROM_RECEIVED = 1;

    private static AsyncTask<Object, Object, Object> task;

    private static SQLiteDatabase DB;

    private static Drawable send_drawable;
    private static Drawable receive_drawable;

    @SuppressLint({"StaticFieldLeak", "UseCompatLoadingForDrawables"})
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

        MyOpener dbHelper = new MyOpener(this);

        task = new AsyncTask<Object, Object, Object>()
        {
            @Override
            protected Object doInBackground(Object... objects)
            {
                DB = dbHelper.getWritableDatabase();
                populateExistingMessages();

                ((Button)findViewById(R.id.send_button)).setOnClickListener((View v) -> saveAndDisplayMessage(FROM_SENT));
                ((Button)findViewById(R.id.receive_button)).setOnClickListener((View v) -> saveAndDisplayMessage(FROM_RECEIVED));

                return null;
            }

            private void clearInput() { input.setText(""); }

            private void saveAndDisplayMessage(int from)
            {
                String msg = input.getText().toString();
                saveMessage(msg, from);
                displayMessage(msg, from);
            }

            private void displayMessage(String msg, int from)
            {
                LinearLayout ll = (LinearLayout)inflater.inflate(from == FROM_RECEIVED ? R.layout.receive_row_layout : R.layout.send_row_layout, listView, false);
                TextView tv = (TextView)ll.getChildAt(from);
                tv.setText(msg);
                adapter.getContents().add(ll);
                listView.setSelection(adapter.getCount() - 1);
                clearInput();
            }

            private void populateExistingMessages()
            {
                Cursor cursor = DB.query(false, TABLE_NAME, new String[] { COL_MSG, COL_FROM }, null, null, null, null, null, null);
                while(cursor.moveToNext())
                    displayMessage(cursor.getString(0), cursor.getInt(1));
                printCursor(cursor, DB.getVersion()).close();
            }

            private void saveMessage(String msg, int from)
            {
                ContentValues cv = new ContentValues();
                cv.put(COL_MSG, msg);
                cv.put(COL_FROM, from);
                DB.insert(TABLE_NAME, "NullColumnName", cv);
            }
        }.execute();

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

    private Cursor printCursor(Cursor c, int version)
    {
        StringBuilder contents = new StringBuilder();
        c.moveToFirst();
        if(c.getCount() != 0)
            do
                contents = contents
                        .append("\t")
                        .append(c.getColumnName(0))
                        .append(": ")
                        .append(c.getString(0))
                        .append(", ")
                        .append(c.getColumnName(1))
                        .append(c.getInt(1))
                        .append("\n");
            while(c.moveToNext());

        StringBuilder colNames = new StringBuilder().append("( ");
        for(String col : c.getColumnNames())
            colNames = colNames.append(col).append(" ");
        colNames = colNames.append(")");


        Log.i("CursorInfo",
                String.format
                (
                    "Version #: %d\nCursor Columns: %d\nCursor Column Names: %s\nCursor Rows: %d\nCursor Row Contents:\n%s",
                    version, c.getColumnCount(), colNames.toString(), c.getCount(), contents.toString()
                )
        );

        return c;
    }

    @Override
    protected void onStop() {
        task.cancel(true);
        super.onStop();
    }

    private class MyOpener extends SQLiteOpenHelper
    {

        public MyOpener(Context ctx)
        {
            super(ctx, DATABASE_NAME, null, VERSION_NUM);

        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL("Create TABLE " + TABLE_NAME + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_MSG + " TEXT, " +
                COL_FROM + " INTEGER);"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
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
            printCursor(DB.rawQuery("DELETE FROM " + TABLE_NAME + " WHERE _id=?", new String[] { Integer.toString(position) }), DB.getVersion()).close();
            super.notifyDataSetChanged();
        }
    }

}