package com.example.scheduler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {

    public MyDBHelper(Context context) {
        super(context, "schedulerDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table tbl_scheduler" +
                "(" +
                "   id  integer primary key autoincrement not null, " +
                "   date  date  not null," +
                "   startTime  time not null," +
                "   endTime time  not null," +
                "   schedule varchar(30)," +
                "   location varchar(30)," +
                "   memo varchar(50)" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists tbl_scheduler");
        onCreate(sqLiteDatabase);
    }
}
