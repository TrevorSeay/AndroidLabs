package com.example.androidlabs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telecom.Call;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
    private static final String COL_ID = "_id";

    private static final int FROM_SENT = 0;
    private static final int FROM_RECEIVED = 1;

    private static AsyncTask<Object, Object, Object> task;

    private static SQLiteDatabase DB;

    private static Drawable send_drawable;
    private static Drawable receive_drawable;

    private static Fragment fragment;

    @SuppressLint({"StaticFieldLeak", "UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        send_drawable = getDrawable(R.drawable.row_send);
        receive_drawable = getDrawable(R.drawable.row_receive);

        FrameLayout frameLayout = findViewById(R.id.frame_layout);
        boolean isTablet = frameLayout != null;

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
                saveMessage(msg, from, createAndDisplayMessage(msg, from));
            }

            private Message createAndDisplayMessage(String msg, int from)
            {
                Message m = (Message)inflater.inflate(from == FROM_RECEIVED ? R.layout.receive_row_layout : R.layout.send_row_layout, listView, false);
                TextView tv = (TextView)m.getChildAt(from);
                tv.setText(msg);
                adapter.getContents().add(m);
                listView.setSelection(listView.getCount() - 1);
                clearInput();
                return m;
            }

            private void populateExistingMessages()
            {
                Cursor cursor = DB.query(false, TABLE_NAME, new String[] { COL_ID, COL_MSG, COL_FROM }, null, null, null, null, null, null);
                while(cursor.moveToNext())
                    createAndDisplayMessage(
                            cursor.getString(1),
                            cursor.getInt(2))
                    .setInfo(cursor.getLong(0),
                            cursor.getString(1),
                            cursor.getInt(2) == FROM_SENT);
                printCursor(cursor, DB.getVersion()).close();
            }

            private void saveMessage(String msg, int from, Message using)
            {
                ContentValues cv = new ContentValues();
                cv.put(COL_MSG, msg);
                cv.put(COL_FROM, from);
                using.setInfo(DB.insert(TABLE_NAME, "NullColumnName", cv), msg, from == FROM_SENT);
            }
        }.execute();

        listView.setOnItemLongClickListener((arg0, arg1, pos, id) ->
        {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_alert_title)
                    .setMessage(getString(R.string.selected_row_is) + " " + pos + "\n" + getString(R.string.database_id_is) + " " + adapter.getContents().get(pos).getDbId())
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> { adapter.remove(pos); if(isTablet && fragment != null) getSupportFragmentManager().beginTransaction().remove(fragment).commit(); })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.cancel())
                    .show();
            return true;
        });

        listView.setOnItemClickListener((list, view, position, id) ->
        {
            Bundle bundle = new Bundle();
            Message msg = adapter.getContents().get(position);
            bundle.putString("msg", msg.getMsg());
            bundle.putLong("id", msg.getDbId());
            bundle.putBoolean("chk", msg.getIsSent());
            bundle.putBoolean("tblt", isTablet);
            if(isTablet)
            {
                fragment = new DetailsFragment();
                fragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, fragment).commit();
            }
            else
            {
                Intent intent = new Intent(ChatRoomActivity.this, EmptyActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
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
                        .append(": ")
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
            db.execSQL("Create TABLE " + TABLE_NAME + "(" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_MSG + " TEXT, " +
                COL_FROM + " INTEGER);"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
    }

    private class MessageAdapter extends BaseAdapter
    {
        private ArrayList<Message> contents = new ArrayList<Message>();

        public ArrayList<Message> getContents() { return this.contents; }

        @Override
        public int getCount() { return this.getContents().size(); }

        @Override
        public Object getItem(int position) { return this.getContents().get(position); }

        @Override
        public long getItemId(int position) { return getContents().get(position).getDbId(); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            return this.getContents().get(position);
        }

        public void remove(int position)
        {
            printCursor(DB.rawQuery("DELETE FROM " + TABLE_NAME + " WHERE " + COL_ID + "=?", new String[] { Long.toString(getItemId(position)) }), DB.getVersion()).close();
            this.getContents().remove(position);
            super.notifyDataSetChanged();
        }
    }

}

