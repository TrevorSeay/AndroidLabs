package com.example.androidlabs;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class Message extends LinearLayout
{
    private long dbId;
    private String msg;
    private boolean isSent;

    public Message(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    long getDbId()
    { return this.dbId; }
    String getMsg()
    { return this.msg; }
    boolean getIsSent()
    { return this.isSent; }

    void setInfo(long dbId, String msg, boolean isSent)
    { this.dbId = dbId; this.msg = msg; this.isSent = isSent; }
}
